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
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
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
  public <T extends IsRODAObject, T1 extends IsIndexed> void runPluginFromIndex(Object context, Job job, Class<T1> classToActOn,
    Filter filter, Boolean justActive ,Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      boolean noObjectsOrchestrated = true;
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(plugin);
      int blockSize = JobsHelper.getBlockSize(plugin);
      Plugin<T> innerPlugin;
      Class<T> modelClassToActOn = (Class<T>) ModelUtils.giveRespectiveModelClass(classToActOn);

      jobStateInfoActor.tell(
        Messages.newPluginBeforeAllExecuteIsReady(plugin).withParallelism(job.getParallelism()).withJobPriority(job.getPriority()),
        jobActor);

      List<String> liteFields = SolrUtils.getClassLiteFields(classToActOn);
      try (IterableIndexResult<T1> findAll = index.findAll(classToActOn, filter,justActive, liteFields)) {
        Iterator<T1> findAllIterator = findAll.iterator();
        List<T1> indexObjects = new ArrayList<>();

        while (findAllIterator.hasNext()) {
          noObjectsOrchestrated = false;
          if (indexObjects.size() == blockSize) {
            innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, job, modelClassToActOn, blockSize, jobActor);
            jobStateInfoActor.tell(Messages
              .newPluginExecuteIsReady(innerPlugin,
                LiteRODAObjectFactory.transformIntoLiteWithCause(model, indexObjects))
              .withJobPriority(job.getPriority()).withParallelism(job.getParallelism()), jobActor);
            indexObjects = new ArrayList<>();
          }
          indexObjects.add(findAllIterator.next());
        }

        if (!indexObjects.isEmpty()) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, job, modelClassToActOn, indexObjects.size(),
            jobActor);
          jobStateInfoActor.tell(Messages
            .newPluginExecuteIsReady(innerPlugin, LiteRODAObjectFactory.transformIntoLiteWithCause(model, indexObjects))
            .withJobPriority(job.getPriority()).withParallelism(job.getParallelism()), jobActor);
        }
      }

      jobStateInfoActor.tell(Messages.newJobInitEnded(getJobPluginInfo(plugin), noObjectsOrchestrated)
        .withParallelism(job.getParallelism()).withJobPriority(job.getPriority()), jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin from index", e);
      JobsHelper.updateJobStateAsync(plugin, job.getPriority(), JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public <T extends IsRODAObject> void runPluginOnObjects(Object context, Job job, Plugin<T> plugin,
    Class<T> objectClass, List<String> uuids) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      boolean noObjectsOrchestrated = true;
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(plugin);
      int blockSize = JobsHelper.getBlockSize(plugin);
      List<T> objects = JobsHelper.getObjectsFromUUID(model, index, objectClass, uuids);
      Iterator<T> iter = objects.iterator();
      Plugin<T> innerPlugin;

      JobParallelism parallelism = job.getParallelism();
      JobPriority priority = job.getPriority();

      jobStateInfoActor.tell(
        Messages.newPluginBeforeAllExecuteIsReady(plugin).withParallelism(parallelism).withJobPriority(priority), jobActor);

      List<T> block = new ArrayList<>();
      while (iter.hasNext()) {
        noObjectsOrchestrated = false;
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, job, objectClass, blockSize, jobActor);
          jobStateInfoActor.tell(Messages
            .newPluginExecuteIsReady(innerPlugin, LiteRODAObjectFactory.transformIntoLiteWithCause(model, block))
            .withParallelism(parallelism).withJobPriority(priority), jobActor);
          block = new ArrayList<>();
        }
        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, job, objectClass, block.size(), jobActor);
        jobStateInfoActor.tell(
          Messages.newPluginExecuteIsReady(innerPlugin, LiteRODAObjectFactory.transformIntoLiteWithCause(model, block))
            .withParallelism(job.getParallelism()).withJobPriority(job.getPriority()),
          jobActor);
      }

      jobStateInfoActor.tell(Messages.newJobInitEnded(getJobPluginInfo(plugin), noObjectsOrchestrated)
        .withJobPriority(job.getPriority()).withParallelism(job.getParallelism()), jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin on RODA Objects ({})", objectClass.getSimpleName(), e);
      JobsHelper.updateJobStateAsync(plugin, job.getPriority(), JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public <T extends IsRODAObject> void runPluginOnAllObjects(Object context, Plugin<T> plugin, Job job,
    Class<T> objectClass) {
    LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
    boolean noObjectsOrchestrated = true;
    ActorRef jobActor = (ActorRef) context;
    ActorRef jobStateInfoActor = getJobContextInformation(plugin);
    int blockSize = JobsHelper.getBlockSize(plugin);

    try (CloseableIterable<OptionalWithCause<LiteRODAObject>> objects = model.listLite(objectClass)) {
      Iterator<OptionalWithCause<LiteRODAObject>> iter = objects.iterator();
      Plugin<T> innerPlugin;

      JobParallelism parallelism = job.getParallelism();
      JobPriority priority = job.getPriority();
      jobStateInfoActor.tell(
        Messages.newPluginBeforeAllExecuteIsReady(plugin).withParallelism(parallelism).withJobPriority(priority), jobActor);

      List<LiteOptionalWithCause> block = new ArrayList<>();
      while (iter.hasNext()) {
        noObjectsOrchestrated = false;
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, job, objectClass, blockSize, jobActor);
          jobStateInfoActor.tell(
            Messages.newPluginExecuteIsReady(innerPlugin, block).withParallelism(parallelism).withJobPriority(priority),
            jobActor);
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
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, job, objectClass, block.size(), jobActor);
        jobStateInfoActor.tell(
          Messages.newPluginExecuteIsReady(innerPlugin, block).withParallelism(parallelism).withJobPriority(priority),
          jobActor);
      }

      jobStateInfoActor.tell(Messages.newJobInitEnded(getJobPluginInfo(plugin), noObjectsOrchestrated)
        .withParallelism(parallelism).withJobPriority(priority), jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin on all objects", e);
      JobsHelper.updateJobStateAsync(plugin, job.getPriority(), JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  @Override
  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin, Job job) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(plugin);

      JobParallelism parallelism = job.getParallelism();
      JobPriority priority = job.getPriority();

      initJobPluginInfo(plugin, job, 0, jobActor);
      jobStateInfoActor.tell(
        Messages.newPluginBeforeAllExecuteIsReady(plugin).withParallelism(parallelism).withJobPriority(priority), jobActor);
      jobStateInfoActor.tell(Messages.newPluginExecuteIsReady(plugin, Collections.emptyList()).withJobPriority(priority)
        .withParallelism(parallelism), jobActor);
      jobStateInfoActor.tell(
        Messages.newJobInitEnded(getJobPluginInfo(plugin), false).withJobPriority(priority).withParallelism(parallelism),
        jobActor);

    } catch (JobIsStoppingException | JobInErrorException e) {
      // do nothing
    } catch (Exception e) {
      LOGGER.error("Error running plugin", e);
      JobsHelper.updateJobStateAsync(plugin, job.getPriority(), JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> plugin, Job job, int objectsCount,
    ActorRef jobActor) throws JobIsStoppingException, JobInErrorException {

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(plugin);
    ActorRef jobStateInfoActor = getJobContextInformation(jobId);
    if (jobStateInfoActor != null) {
      // see if job is stopping
      if (stoppingJobs.contains(jobId)) {
        throw new JobIsStoppingException();
      }
      // see if job is in error
      if (inErrorJobs.contains(jobId)) {
        throw new JobInErrorException();
      }

      JobPluginInfo jobPluginInfo = getJobPluginInfo(plugin);
      initJobPluginInfo(plugin, job, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
      plugin.injectJobPluginInfo(jobPluginInfo);
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }
  }

  private <T extends IsRODAObject> Plugin<T> getNewPluginInstanceAndInitJobPluginInfo(Plugin<T> plugin, Job job,
    Class<T> pluginClass, int objectsCount, ActorRef jobActor)
    throws InvalidParameterException, JobIsStoppingException, JobInErrorException {
    Plugin<T> innerPlugin = RodaCoreFactory.getPluginManager().getPlugin(plugin.getClass().getName(), pluginClass);
    innerPlugin.setParameterValues(new HashMap<>(plugin.getParameterValues()));

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(innerPlugin);
    ActorRef jobStateInfoActor = getJobContextInformation(jobId);
    if (jobStateInfoActor != null) {
      // see if job is stopping
      if (stoppingJobs.contains(jobId)) {
        throw new JobIsStoppingException();
      }
      // see if job is in error
      if (inErrorJobs.contains(jobId)) {
        throw new JobInErrorException();
      }

      JobPluginInfo jobPluginInfo = getJobPluginInfo(plugin);
      initJobPluginInfo(innerPlugin, job, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
      innerPlugin.injectJobPluginInfo(jobPluginInfo);
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }

    return innerPlugin;
  }

  private <T extends IsRODAObject> JobPluginInfo getJobPluginInfo(Plugin<T> plugin) {
    if (PluginType.INGEST == plugin.getType()) {
      return new IngestJobPluginInfo();
    } else if (PluginType.MULTI == plugin.getType()) {
      return new MultipleJobPluginInfo();
    } else {
      return new SimpleJobPluginInfo();
    }
  }

  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> innerPlugin, Job job, ActorRef jobActor,
    ActorRef jobStateInfoActor, JobPluginInfo jobPluginInfo, int objectsCount) {
    jobPluginInfo.setSourceObjectsCount(objectsCount);
    jobPluginInfo.setSourceObjectsWaitingToBeProcessed(objectsCount);
    jobStateInfoActor.tell(Messages.newJobInfoUpdated(innerPlugin, jobPluginInfo).withParallelism(job.getParallelism())
      .withJobPriority(job.getPriority()), jobActor);
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
    ActorRef jobStateInfoActor = getJobContextInformation(jobId);
    if (jobStateInfoActor != null) {
      stoppingJobs.add(jobId);
      jobStateInfoActor.tell(Messages.newJobStop().withJobPriority(job.getPriority()).withParallelism(job.getParallelism()),
        ActorRef.noSender());
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
    ActorRef jobStateInfoActor = getJobContextInformation(jobId);
    if (jobStateInfoActor != null) {
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

  public <T extends IsRODAObject> ActorRef getJobContextInformation(Plugin<T> plugin) {
    LOGGER.debug("Getting job context information; JobId: {}; Plugin name: {}", PluginHelper.getJobId(plugin),
      plugin.getClass().getName());
    LOGGER.debug("Plugin parameters map: {}", plugin.getParameterValues());
    return getJobContextInformation(PluginHelper.getJobId(plugin));
  }

  @Override
  public <T extends IsRODAObject> void updateJobInformationAsync(Plugin<T> plugin, JobPluginInfo info)
    throws JobException {
    ActorRef jobStateInfoActor = getJobContextInformation(plugin);

    try {
      Job job = PluginHelper.getJob(plugin, RodaCoreFactory.getModelService());
      JobParallelism parallelism = job.getParallelism();
      JobPriority priority = job.getPriority();

      if (jobStateInfoActor != null) {
        jobStateInfoActor.tell(
          Messages.newJobInfoUpdated(plugin, info).withParallelism(parallelism).withJobPriority(priority),
          ActorRef.noSender());
      } else {
        throw new JobException("Job id or job information is null");
      }
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      throw new JobException("Unable to fetch Job from model");
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
