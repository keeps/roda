/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.akka.AkkaUtils;
import org.roda.core.common.akka.DeadLetterActor;
import org.roda.core.common.akka.Messages;
import org.roda.core.common.akka.Messages.JobPartialUpdate;
import org.roda.core.common.akka.Messages.JobStateUpdated;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AcquireLockTimeoutException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.JobInErrorException;
import org.roda.core.data.exceptions.JobIsStoppingException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotLockableAtTheTimeException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaJobsManager;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.internal.CleanUnfinishedJobsPlugin;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.AllDeadLetters;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/*
 * 20160520 hsilva: use kamon to obtain metrics about akka, Graphite & Grafana for collecting and dashboard (http://kamon.io/integrations/akka/overview/ & 
 * http://www.lightbend.com/activator/template/akka-monitoring-kamon-statsd)
 * 
 * */
public class AkkaEmbeddedPluginOrchestrator implements PluginOrchestrator {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEmbeddedPluginOrchestrator.class);

  private final IndexService index;
  private final ModelService model;

  private ActorSystem jobsSystem;
  private ActorRef jobsManager;
  private int maxNumberOfJobsInParallel;

  // Map<jobId, ActorRef>
  private Map<String, ActorRef> runningJobs;
  // List<jobId>
  private List<String> stoppingJobs;
  // List<jobId>
  private List<String> inErrorJobs;

  public AkkaEmbeddedPluginOrchestrator() {
    maxNumberOfJobsInParallel = JobsHelper.getMaxNumberOfJobsInParallel();

    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();

    runningJobs = new HashMap<>();
    stoppingJobs = new ArrayList<>();
    inErrorJobs = new ArrayList<>();

    Config akkaConfig = AkkaUtils.getAkkaConfiguration("application.conf");
    jobsSystem = ActorSystem.create("JobsSystem", akkaConfig);
    // 20170105 hsilva: subscribe all dead letter so they are logged
    jobsSystem.eventStream().subscribe(jobsSystem.actorOf(Props.create(DeadLetterActor.class)), AllDeadLetters.class);

    jobsManager = jobsSystem.actorOf(Props.create(AkkaJobsManager.class, maxNumberOfJobsInParallel), "jobsManager");

  }

  @Override
  public void setup() {
    // do nothing
  }

  @Override
  public void shutdown() {
    LOGGER.info("Going to shutdown JOBS actor system");
    Future<Terminated> terminate = jobsSystem.terminate();
    terminate.onComplete(new OnComplete<Terminated>() {
      @Override
      public void onComplete(Throwable failure, Terminated result) {
        if (failure != null) {
          LOGGER.error("Error while shutting down JOBS actor system", failure);
        } else {
          LOGGER.info("Done shutting down JOBS actor system");
        }
      }
    }, jobsSystem.dispatcher());

    try {
      LOGGER.info("Waiting up to 30 seconds for JOBS actor system to shutdown");
      Await.result(jobsSystem.whenTerminated(), Duration.create(30, "seconds"));
    } catch (TimeoutException e) {
      LOGGER.warn("JOBS Actor system shutdown wait timed out, continuing...");
    } catch (Exception e) {
      LOGGER.error("Error while shutting down JOBS actor system", e);
    }
  }

  @Override
  public <T extends IsRODAObject, T1 extends IsIndexed> void runPluginFromIndex(Object context, Class<T1> classToActOn,
    Filter filter, Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
      int blockSize = JobsHelper.getBlockSize();
      Plugin<T> innerPlugin;
      Class<T> modelClassToActOn = (Class<T>) ModelUtils.giveRespectiveModelClass(classToActOn);

      jobStateInfoActor.tell(Messages.newPluginBeforeAllExecuteIsReady(plugin), jobActor);

      List<String> liteFields = SolrUtils.getClassLiteFields(classToActOn);
      try (IterableIndexResult<T1> findAll = index.findAll(classToActOn, filter, liteFields)) {
        Iterator<T1> findAllIterator = findAll.iterator();
        List<T1> indexObjects = new ArrayList<>();

        while (findAllIterator.hasNext()) {
          if (indexObjects.size() == blockSize) {
            innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, modelClassToActOn, blockSize, jobActor);
            jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(innerPlugin,
              LiteRODAObjectFactory.transformIntoLiteWithCause(model, indexObjects)), jobActor);
            indexObjects = new ArrayList<>();
          }
          indexObjects.add(findAllIterator.next());
        }

        if (!indexObjects.isEmpty()) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, modelClassToActOn, indexObjects.size(),
            jobActor);
          jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(innerPlugin,
            LiteRODAObjectFactory.transformIntoLiteWithCause(model, indexObjects)), jobActor);
        }
      }

      jobStateInfoActor.tell(Messages.newJobInitEnded(), jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin from index", e);
      JobsHelper.updateJobStateAsync(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public <T extends IsRODAObject> void runPluginOnObjects(Object context, Plugin<T> plugin, Class<T> objectClass,
    List<String> uuids) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
      int blockSize = JobsHelper.getBlockSize();
      List<T> objects = JobsHelper.getObjectsFromUUID(model, index, objectClass, uuids);
      Iterator<T> iter = objects.iterator();
      Plugin<T> innerPlugin;

      jobStateInfoActor.tell(Messages.newPluginBeforeAllExecuteIsReady(plugin), jobActor);

      List<T> block = new ArrayList<>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, blockSize, jobActor);
          jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(innerPlugin,
            LiteRODAObjectFactory.transformIntoLiteWithCause(model, block)), jobActor);
          block = new ArrayList<>();
        }
        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, block.size(), jobActor);
        jobStateInfoActor.tell(
          Messages.newPluginExecuteIsReady(innerPlugin, LiteRODAObjectFactory.transformIntoLiteWithCause(model, block)),
          jobActor);
      }

      jobStateInfoActor.tell(Messages.newJobInitEnded(), jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin on RODA Objects ({})", objectClass.getSimpleName(), e);
      JobsHelper.updateJobStateAsync(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public <T extends IsRODAObject> void runPluginOnAllObjects(Object context, Plugin<T> plugin, Class<T> objectClass) {
    LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
    ActorRef jobActor = (ActorRef) context;
    ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
    int blockSize = JobsHelper.getBlockSize();

    try (CloseableIterable<OptionalWithCause<LiteRODAObject>> objects = model.listLite(objectClass)) {
      Iterator<OptionalWithCause<LiteRODAObject>> iter = objects.iterator();
      Plugin<T> innerPlugin;

      jobStateInfoActor.tell(Messages.newPluginBeforeAllExecuteIsReady(plugin), jobActor);

      List<LiteOptionalWithCause> block = new ArrayList<>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, blockSize, jobActor);
          jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(innerPlugin, block), jobActor);
          block = new ArrayList<>();
        }

        OptionalWithCause<LiteRODAObject> nextObject = iter.next();
        if (nextObject.isPresent()) {
          block.add(LiteOptionalWithCause.of(nextObject.get()));
        } else {
          LOGGER.error("Cannot process object", nextObject.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, block.size(), jobActor);
        jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(innerPlugin, block), jobActor);
      }

      jobStateInfoActor.tell(Messages.newJobInitEnded(), jobActor);
    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin on all objects", e);
      JobsHelper.updateJobStateAsync(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  @Override
  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));

      initJobPluginInfo(plugin, 0, jobActor);
      jobStateInfoActor.tell(Messages.newPluginBeforeAllExecuteIsReady(plugin), jobActor);
      jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(plugin, Collections.emptyList()), jobActor);
      jobStateInfoActor.tell(Messages.newJobInitEnded(), jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin", e);
      JobsHelper.updateJobStateAsync(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> plugin, int objectsCount, ActorRef jobActor)
    throws JobIsStoppingException, JobInErrorException {

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      // see if job is stopping
      if (stoppingJobs.contains(jobId)) {
        throw new JobIsStoppingException();
      }
      // see if job is in error
      if (inErrorJobs.contains(jobId)) {
        throw new JobInErrorException();
      }

      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      if (PluginType.INGEST == plugin.getType()) {
        IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
        initJobPluginInfo(plugin, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        plugin.injectJobPluginInfo(jobPluginInfo);
      } else if (PluginType.MISC == plugin.getType() || PluginType.AIP_TO_AIP == plugin.getType()) {
        SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo();
        initJobPluginInfo(plugin, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        plugin.injectJobPluginInfo(jobPluginInfo);
      }
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }
  }

  private <T extends IsRODAObject> Plugin<T> getNewPluginInstanceAndInitJobPluginInfo(Plugin<T> plugin,
    Class<T> pluginClass, int objectsCount, ActorRef jobActor)
    throws InvalidParameterException, JobIsStoppingException, JobInErrorException {
    Plugin<T> innerPlugin = RodaCoreFactory.getPluginManager().getPlugin(plugin.getClass().getName(), pluginClass);
    innerPlugin.setParameterValues(new HashMap<>(plugin.getParameterValues()));

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(innerPlugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      // see if job is stopping
      if (stoppingJobs.contains(jobId)) {
        throw new JobIsStoppingException();
      }
      // see if job is in error
      if (inErrorJobs.contains(jobId)) {
        throw new JobInErrorException();
      }

      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
      if (PluginType.INGEST == plugin.getType()) {
        IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
        initJobPluginInfo(innerPlugin, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        innerPlugin.injectJobPluginInfo(jobPluginInfo);
      } else {
        SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo();
        initJobPluginInfo(innerPlugin, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        innerPlugin.injectJobPluginInfo(jobPluginInfo);
      }
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }

    return innerPlugin;
  }

  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> innerPlugin, ActorRef jobActor,
    ActorRef jobStateInfoActor, JobPluginInfo jobPluginInfo, int objectsCount) {
    jobPluginInfo.setSourceObjectsCount(objectsCount);
    jobPluginInfo.setSourceObjectsWaitingToBeProcessed(objectsCount);
    jobStateInfoActor.tell(Messages.newJobInfoUpdated(innerPlugin, jobPluginInfo), jobActor);
  }

  @Override
  public void executeJob(Job job, boolean async) throws JobAlreadyStartedException {
    LOGGER.info("Adding job '{}' ({}) to be executed", job.getName(), job.getId());

    if (runningJobs.containsKey(job.getId())) {
      LOGGER.info("Job '{}' ({}) is already queued to be executed", job.getName(), job.getId());
      throw new JobAlreadyStartedException();
    } else {
      if (async) {
        jobsManager.tell(job, ActorRef.noSender());
      } else {
        int timeoutInSeconds = JobsHelper.getSyncTimeout();
        Timeout timeout = new Timeout(Duration.create(timeoutInSeconds, "seconds"));
        Future<Object> future = Patterns.ask(jobsManager, job, timeout);
        try {
          Await.result(future, timeout.duration());
        } catch (Exception e) {
          LOGGER.error("Error executing job synchronously", e);
        }
      }
      LOGGER.info("Success adding job '{}' ({}) to be executed", job.getName(), job.getId());
    }
  }

  @Override
  public void stopJobAsync(Job job) {
    String jobId = job.getId();
    if (jobId != null && runningJobs.get(jobId) != null) {
      stoppingJobs.add(jobId);
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      jobStateInfoActor.tell(Messages.newJobStop(), ActorRef.noSender());
    }
  }

  @Override
  public void cleanUnfinishedJobsAsync() {
    List<String> unfinishedJobsIdsList = new ArrayList<>();
    try (IterableIndexResult<Job> result = JobsHelper.findUnfinishedJobs(index)) {
      // set all jobs state to TO_BE_CLEANED
      for (Job job : result) {
        unfinishedJobsIdsList.add(job.getId());
        Job jobToUpdate = model.retrieveJob(job.getId());
        jobToUpdate.setState(JOB_STATE.TO_BE_CLEANED);
        model.createOrUpdateJob(jobToUpdate);
      }

      if (!unfinishedJobsIdsList.isEmpty()) {
        // create job to clean the unfinished jobs
        Job job = new Job();
        job.setId(IdUtils.createUUID());
        job.setName("Clean unfinished jobs during startup");
        job.setSourceObjects(SelectedItemsList.create(Job.class, unfinishedJobsIdsList));
        job.setPlugin(CleanUnfinishedJobsPlugin.class.getCanonicalName());
        job.setPluginType(PluginType.INTERNAL);
        job.setUsername(RodaConstants.ADMIN);

        RodaCoreFactory.getModelService().createJob(job);
        RodaCoreFactory.getPluginOrchestrator().executeJob(job, true);
      }
    } catch (JobAlreadyStartedException | GenericException | RequestNotValidException | NotFoundException
      | AuthorizationDeniedException | IOException e) {
      LOGGER.error("Error while creating Job for cleaning unfinished jobs", e);
    }
  }

  @Override
  public <T extends IsRODAObject> void updateJobAsync(Plugin<T> plugin, JobPartialUpdate partialUpdate) {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      jobStateInfoActor.tell(partialUpdate, ActorRef.noSender());
      if (partialUpdate instanceof JobStateUpdated && Job.isFinalState(((JobStateUpdated) partialUpdate).getState())) {
        runningJobs.remove(jobId);
        stoppingJobs.remove(jobId);
        inErrorJobs.remove(jobId);
      }

    } else {
      LOGGER.error("Got job id or job information null when updating Job state");
    }
  }

  @Override
  public void setJobContextInformation(String jobId, Object object) {
    runningJobs.put(jobId, (ActorRef) object);
  }

  public ActorRef getJobContextInformation(String jobId) {
    return runningJobs.get(jobId);
  }

  @Override
  public <T extends IsRODAObject> void updateJobInformationAsync(Plugin<T> plugin, JobPluginInfo info)
    throws JobException {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      jobStateInfoActor.tell(Messages.newJobInfoUpdated(plugin, info), ActorRef.noSender());
    } else {
      throw new JobException("Job id or job information is null");
    }
  }

  @Override
  public void setJobInError(String jobId) {
    inErrorJobs.add(jobId);
  }

  @Override
  public void acquireObjectLock(List<String> lites, int timeoutInSeconds, boolean waitForLockIfLocked,
    String requestUuid) throws LockingException {
    Timeout timeout = new Timeout(Duration.create(timeoutInSeconds, "seconds"));

    if (StringUtils.isBlank(requestUuid)) {
      throw new LockingException("One must provide valid (i.e. non blank) request uuid!");
    }

    Object result = null;
    Future<Object> future = Patterns.ask(jobsManager,
      Messages.newJobsManagerAcquireLock(lites, waitForLockIfLocked, timeoutInSeconds, requestUuid), timeout);
    try {
      result = Await.result(future, timeout.duration());
    } catch (Exception e) {
      LOGGER.error("Unable to acquire locks for the objects being processed '{}'", lites, e);
      throw new AcquireLockTimeoutException("Unable to acquire locks for the objects being processed '" + lites + "'");
    }

    if (result != null && result instanceof Messages.JobsManagerNotLockableAtTheTime) {
      throw new NotLockableAtTheTimeException(
        "Not lockable at the time due to requester not willing to await to obtain the lock!");
    }
  }

  @Override
  public void releaseObjectLockAsync(List<String> lites, String requestUuid) {
    jobsManager.tell(Messages.newJobsManagerReleaseLock(lites, requestUuid), ActorRef.noSender());
  }

  @Override
  public void releaseAllObjectLocksAsync() {
    jobsManager.tell(Messages.newJobsManagerReleaseAllLocks(), ActorRef.noSender());
  }

}
