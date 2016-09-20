/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaJobActor;
import org.roda.core.plugins.orchestrate.akka.Messages;
import org.roda.core.plugins.orchestrate.akka.Messages.JobPartialUpdate;
import org.roda.core.plugins.orchestrate.akka.Messages.JobStateUpdated;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
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
  private final StorageService storage;

  private ActorSystem workersSystem;
  private ActorRef jobWorkersRouter;
  private int maxNumberOfJobsInParallel;

  // Map<jobId, ActorRef>
  private Map<String, ActorRef> runningJobs;

  public AkkaEmbeddedPluginOrchestrator() {
    maxNumberOfJobsInParallel = JobsHelper.getMaxNumberOfJobsInParallel();

    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    runningJobs = new HashMap<>();

    Config akkaConfig = getAkkaConfiguration();
    workersSystem = ActorSystem.create("WorkersSystem", akkaConfig);

    Props jobsProps = new RoundRobinPool(maxNumberOfJobsInParallel).props(Props.create(AkkaJobActor.class));
    jobWorkersRouter = workersSystem.actorOf(jobsProps, "JobWorkersRouter");

  }

  private Config getAkkaConfiguration() {
    InputStream originStream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.CORE_ORCHESTRATOR_FOLDER + "/application.conf");

    Config akkaConfig = null;

    try {
      String configAsString = IOUtils.toString(originStream, RodaConstants.DEFAULT_ENCODING);
      akkaConfig = ConfigFactory.parseString(configAsString);
    } catch (IOException e) {
      LOGGER.error("Could not load Akka configuration", e);
    } finally {
      RodaUtils.closeQuietly(originStream);
    }

    return akkaConfig;
  }

  @Override
  public void setup() {
    // do nothing
  }

  @Override
  public void shutdown() {
    LOGGER.info("Going to shutdown actor system (which will be done asynchronously)");
    Future<Terminated> terminate = workersSystem.terminate();
    terminate.onComplete(new OnComplete<Terminated>() {
      public void onComplete(Throwable failure, Terminated result) {
        if (failure != null) {
          LOGGER.error("Error while shutting down actor system", failure);
        } else {
          LOGGER.info("Done shutting down actor system");
        }
      }
    }, workersSystem.dispatcher());
  }

  @Override
  public <T extends IsRODAObject, T1 extends IsIndexed> void runPluginFromIndex(Object context, Class<T1> classToActOn,
    Filter filter, Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
      int blockSize = JobsHelper.getBlockSize();
      IndexResult<T1> find;
      int offset = 0;
      Plugin<T> innerPlugin;
      Class<T> modelClassToActOn = (Class<T>) ModelUtils.giveRespectiveModelClass(classToActOn);

      jobStateInfoActor.tell(new Messages.PluginBeforeAllExecuteIsReady<>(plugin), jobActor);

      do {
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, Sorter.NONE,
          new Sublist(offset, blockSize));
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, modelClassToActOn, (int) find.getLimit(),
          jobActor);
        offset += find.getLimit();
        List<T> objects = JobsHelper.getObjectsFromIndexObjects(model, index, modelClassToActOn, find.getResults());
        jobStateInfoActor.tell(new Messages.PluginExecuteIsReady<>(innerPlugin, objects), jobActor);

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      jobStateInfoActor.tell(new Messages.JobInitEnded(), jobActor);

    } catch (Exception e) {
      LOGGER.error("Error running plugin from index", e);
      JobsHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
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

      jobStateInfoActor.tell(new Messages.PluginBeforeAllExecuteIsReady<>(plugin), jobActor);

      List<T> block = new ArrayList<T>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, blockSize, jobActor);
          jobStateInfoActor.tell(new Messages.PluginExecuteIsReady<>(innerPlugin, block), jobActor);
          block = new ArrayList<>();
        }
        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, block.size(), jobActor);
        jobStateInfoActor.tell(new Messages.PluginExecuteIsReady<>(innerPlugin, block), jobActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), jobActor);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on RODA Objects ({})", objectClass.getSimpleName(), e);
      JobsHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public <T extends IsRODAObject> void runPluginOnAllObjects(Object context, Plugin<T> plugin, Class<T> objectClass) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
      int blockSize = JobsHelper.getBlockSize();
      CloseableIterable<OptionalWithCause<T>> objects = model.list(objectClass);
      Iterator<OptionalWithCause<T>> iter = objects.iterator();
      Plugin<T> innerPlugin;

      jobStateInfoActor.tell(new Messages.PluginBeforeAllExecuteIsReady<>(plugin), jobActor);

      List<T> block = new ArrayList<T>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {

          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, blockSize, jobActor);
          jobStateInfoActor.tell(new Messages.PluginExecuteIsReady<>(innerPlugin, block), jobActor);
          block = new ArrayList<>();
        }

        OptionalWithCause<T> nextObject = iter.next();
        if (nextObject.isPresent()) {
          block.add(nextObject.get());
        } else {
          LOGGER.error("Cannot process object", nextObject.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, objectClass, block.size(), jobActor);
        jobStateInfoActor.tell(new Messages.PluginExecuteIsReady<>(innerPlugin, block), jobActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), jobActor);
      IOUtils.closeQuietly(objects);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all objects", e);
      JobsHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobActor = (ActorRef) context;
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));

      initJobPluginInfo(plugin, 0, jobActor);
      jobStateInfoActor.tell(new Messages.PluginBeforeAllExecuteIsReady<>(plugin), jobActor);
      jobStateInfoActor.tell(new Messages.PluginExecuteIsReady<>(plugin, Collections.emptyList()), jobActor);
      jobStateInfoActor.tell(new Messages.JobInitEnded(), jobActor);

    } catch (Exception e) {
      LOGGER.error("Error running plugin", e);
      JobsHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  // FIXME 20160629 hsilva: rename method
  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> plugin, int objectsCount, ActorRef jobActor)
    throws InvalidParameterException, PluginException {

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      if (PluginType.INGEST == plugin.getType()) {
        IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
        initJobPluginInfo(plugin, jobId, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        plugin.injectJobPluginInfo(jobPluginInfo);
      } else if (PluginType.MISC == plugin.getType() || PluginType.AIP_TO_AIP == plugin.getType()) {
        SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo();
        initJobPluginInfo(plugin, jobId, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        plugin.injectJobPluginInfo(jobPluginInfo);
      }
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }

  }

  private <T extends IsRODAObject> Plugin<T> getNewPluginInstanceAndInitJobPluginInfo(Plugin<T> plugin,
    Class<T> pluginClass, int objectsCount, ActorRef jobActor) throws InvalidParameterException, PluginException {
    Plugin<T> innerPlugin = RodaCoreFactory.getPluginManager().getPlugin(plugin.getClass().getName(), pluginClass);
    innerPlugin.setParameterValues(plugin.getParameterValues());

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(innerPlugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = getJobContextInformation(PluginHelper.getJobId(plugin));
      if (PluginType.INGEST == plugin.getType()) {
        IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
        initJobPluginInfo(innerPlugin, jobId, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        innerPlugin.injectJobPluginInfo(jobPluginInfo);
      } else {
        SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo();
        initJobPluginInfo(innerPlugin, jobId, jobActor, jobStateInfoActor, jobPluginInfo, objectsCount);
        innerPlugin.injectJobPluginInfo(jobPluginInfo);
      }
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }

    return innerPlugin;

  }

  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> innerPlugin, String jobId, ActorRef jobActor,
    ActorRef jobStateInfoActor, JobPluginInfo jobPluginInfo, int objectsCount) {
    jobPluginInfo.setSourceObjectsCount(objectsCount);
    jobPluginInfo.setSourceObjectsWaitingToBeProcessed(objectsCount);

    jobStateInfoActor.tell(new Messages.JobInfoUpdated(innerPlugin, jobPluginInfo), jobActor);
  }

  @Override
  public void executeJob(Job job, boolean async) throws JobAlreadyStartedException {
    LOGGER.info("Adding job '{}' ({}) to be executed", job.getName(), job.getId());

    if (runningJobs.containsKey(job.getId())) {
      LOGGER.info("Job '{}' ({}) is already queued to be executed", job.getName(), job.getId());
      throw new JobAlreadyStartedException();
    } else {
      if (async) {
        jobWorkersRouter.tell(job, ActorRef.noSender());
      } else {
        int timeoutInSeconds = JobsHelper.getSyncTimeout();
        Timeout timeout = new Timeout(Duration.create(timeoutInSeconds, "seconds"));
        Future<Object> future = Patterns.ask(jobWorkersRouter, job, timeout);
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
  public void stopJob(Job job) {
    String jobId = job.getId();
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      jobStateInfoActor.tell(new Messages.JobStop(), ActorRef.noSender());
    }
  }

  @Override
  public void cleanUnfinishedJobs() {
    cleanUnfinishedJobs(findUnfinishedJobs());
  }

  private List<Job> findUnfinishedJobs() {
    Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.JOB_STATE,
      Arrays.asList(Job.JOB_STATE.STARTED.toString(), Job.JOB_STATE.CREATED.toString())));
    Sublist sublist = new Sublist(0, RodaConstants.DEFAULT_PAGINATION_VALUE);
    IndexResult<Job> jobs;
    List<Job> unfinishedJobs = new ArrayList<>();
    try {
      do {
        jobs = index.find(Job.class, filter, null, sublist);
        unfinishedJobs.addAll(jobs.getResults());
        sublist.setFirstElementIndex(sublist.getFirstElementIndex() + Math.toIntExact(jobs.getLimit()));
      } while (jobs.getTotalCount() > jobs.getOffset() + jobs.getLimit());
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Unable to find Jobs still to be cleaned", e);
    }
    return unfinishedJobs;
  }

  private void cleanUnfinishedJobs(List<Job> unfinishedJobs) {
    if (!unfinishedJobs.isEmpty()) {
      List<String> jobsToBeDeletedFromIndex = new ArrayList<>();
      for (Job job : unfinishedJobs) {
        try {
          Job jobToBeCleaned = model.retrieveJob(job.getId());
          JobsHelper.updateJobInTheStateStartedOrCreated(jobToBeCleaned);
          model.createOrUpdateJob(jobToBeCleaned);

          // cleanup job related objects (aips, sips, etc.)
          JobsHelper.doJobObjectsCleanup(job, RodaCoreFactory.getModelService(), index);
        } catch (NotFoundException e) {
          jobsToBeDeletedFromIndex.add(job.getId());
        } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
          LOGGER.error("Unable to get/update Job", e);
        }
      }
      if (!jobsToBeDeletedFromIndex.isEmpty()) {
        index.deleteSilently(Job.class, jobsToBeDeletedFromIndex);
      }
    }
  }

  @Override
  public <T extends IsRODAObject> void updateJob(Plugin<T> plugin, JobPartialUpdate partialUpdate) {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      jobStateInfoActor.tell(partialUpdate, ActorRef.noSender());
      if (partialUpdate instanceof JobStateUpdated && Job.isFinalState(((JobStateUpdated) partialUpdate).getState())) {
        runningJobs.remove(jobId);
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
  public <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, JobPluginInfo info) throws JobException {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      jobStateInfoActor.tell(new Messages.JobInfoUpdated(plugin, info), ActorRef.noSender());
    } else {
      throw new JobException("Job id or job information is null");
    }
  }

}
