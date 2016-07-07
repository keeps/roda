/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaJobActor;
import org.roda.core.plugins.orchestrate.akka.AkkaWorkerActor;
import org.roda.core.plugins.orchestrate.akka.Messages;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

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
  private ActorRef workersRouter;
  private ActorRef jobWorkersRouter;
  private int maxNumberOfJobsInParallel;
  private int numberOfJobsWorkers;

  // Map<jobId, ActorRef>
  private Map<String, ActorRef> runningJobs;

  public AkkaEmbeddedPluginOrchestrator() {
    maxNumberOfJobsInParallel = JobsHelper.getMaxNumberOfJobsInParallel();
    numberOfJobsWorkers = JobsHelper.getNumberOfJobsWorkers();

    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    runningJobs = new HashMap<>();

    Config akkaConfig = RodaCoreFactory.getAkkaConfiguration();
    workersSystem = ActorSystem.create("WorkersSystem", akkaConfig);

    Props jobsProps = new RoundRobinPool(maxNumberOfJobsInParallel).props(Props.create(AkkaJobActor.class));
    jobWorkersRouter = workersSystem.actorOf(jobsProps, "JobWorkersRouter");

    Props workersProps = new RoundRobinPool(numberOfJobsWorkers)
      .props(Props.create(AkkaWorkerActor.class, storage, model, index));
    workersRouter = workersSystem.actorOf(workersProps, "WorkersRouter");

  }

  @Override
  public void setup() {
    // do nothing
  }

  @Override
  public void shutdown() {
    LOGGER.info("Going to shutdown actor system");
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
  public <T extends IsIndexed> void runPluginFromIndex(Object context, Class<T> classToActOn, Filter filter,
    Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      IndexResult<T> find;
      int offset = 0;
      Plugin<T> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      do {
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, Sorter.NONE,
          new Sublist(offset, blockSize));
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, classToActOn, (int) find.getLimit());
        offset += find.getLimit();
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<T>(innerPlugin, find.getResults()), jobStateInfoActor);

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

    } catch (Exception e) {
      LOGGER.error("Error running plugin from index", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public void runPluginOnAIPs(Object context, Plugin<AIP> plugin, List<String> uuids, boolean retrieveFromModel) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      List<AIP> aips = JobsHelper.getAIPs(model, index, uuids, retrieveFromModel);
      Iterator<AIP> iter = aips.iterator();
      Plugin<AIP> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, AIP.class, blockSize);
          jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
          workersRouter.tell(new Messages.PluginExecuteIsReady<AIP>(innerPlugin, block), jobStateInfoActor);
          block = new ArrayList<>();
        }
        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, AIP.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<AIP>(innerPlugin, block), jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

    } catch (Exception e) {
      LOGGER.error("Error running plugin on AIPs", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public void runPluginOnRepresentations(Object context, Plugin<Representation> plugin, List<String> uuids) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      List<Representation> representations = JobsHelper.getRepresentations(model, index, uuids);
      Iterator<Representation> iter = representations.iterator();
      Plugin<Representation> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<Representation> block = new ArrayList<Representation>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, Representation.class, blockSize);
          jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
          workersRouter.tell(new Messages.PluginExecuteIsReady<Representation>(innerPlugin, block), jobStateInfoActor);
          block = new ArrayList<>();
        }
        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, Representation.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<Representation>(innerPlugin, block), jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

    } catch (Exception e) {
      LOGGER.error("Error running plugin on Representations", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  @Override
  public void runPluginOnFiles(Object context, Plugin<File> plugin, List<String> uuids) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      List<File> files = JobsHelper.getFiles(model, index, uuids);
      Iterator<File> iter = files.iterator();
      Plugin<File> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<File> block = new ArrayList<File>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, File.class, blockSize);
          jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
          workersRouter.tell(new Messages.PluginExecuteIsReady<File>(innerPlugin, block), jobStateInfoActor);
          block = new ArrayList<>();
        }
        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, File.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<File>(innerPlugin, block), jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

    } catch (Exception e) {
      LOGGER.error("Error running plugin on Files", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public void runPluginOnAllAIPs(Object context, Plugin<AIP> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> iter = aips.iterator();
      Plugin<AIP> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, AIP.class, blockSize);
          jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
          workersRouter.tell(new Messages.PluginExecuteIsReady<AIP>(innerPlugin, block), jobStateInfoActor);
          block = new ArrayList<>();
        }
        OptionalWithCause<AIP> nextAIP = iter.next();
        if (nextAIP.isPresent()) {
          block.add(nextAIP.get());
        } else {
          LOGGER.error("Cannot process AIP", nextAIP.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, AIP.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<AIP>(innerPlugin, block), jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

      IOUtils.closeQuietly(aips);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all AIPs", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public void runPluginOnAllRepresentations(Object context, Plugin<Representation> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();
      Plugin<Representation> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<Representation> block = new ArrayList<Representation>();
      while (aipIter.hasNext()) {
        OptionalWithCause<AIP> aip = aipIter.next();
        if (aip.isPresent()) {
          for (Representation representation : aip.get().getRepresentations()) {
            if (block.size() == blockSize) {
              innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, Representation.class, blockSize);
              jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
              workersRouter.tell(new Messages.PluginExecuteIsReady<Representation>(innerPlugin, block),
                jobStateInfoActor);
              block = new ArrayList<>();
            }
            block.add(representation);
          }
        } else {
          LOGGER.error("Cannot process AIP", aip.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, Representation.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<Representation>(innerPlugin, block), jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

      IOUtils.closeQuietly(aips);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all representations", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public void runPluginOnAllFiles(Object context, Plugin<File> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();
      Plugin<File> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<File> block = new ArrayList<File>();
      while (aipIter.hasNext()) {
        OptionalWithCause<AIP> aip = aipIter.next();
        if (aip.isPresent()) {
          for (Representation representation : aip.get().getRepresentations()) {
            boolean recursive = true;
            CloseableIterable<OptionalWithCause<File>> files = model.listFilesUnder(aip.get().getId(),
              representation.getId(), recursive);
            Iterator<OptionalWithCause<File>> fileIter = files.iterator();

            while (fileIter.hasNext()) {

              if (block.size() == blockSize) {
                innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, File.class, blockSize);
                jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
                workersRouter.tell(new Messages.PluginExecuteIsReady<File>(innerPlugin, block), jobStateInfoActor);
                block = new ArrayList<>();
              }

              OptionalWithCause<File> file = fileIter.next();
              if (file.isPresent()) {
                block.add(file.get());
              } else {
                LOGGER.error("Cannot process File", file.getCause());
              }
            }
            IOUtils.closeQuietly(files);
          }
        } else {
          LOGGER.error("Cannot process AIP", aip.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, File.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<File>(innerPlugin, block), jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

      IOUtils.closeQuietly(aips);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all files", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }

  }

  @Override
  public void runPluginOnTransferredResources(Object context, Plugin<TransferredResource> plugin, List<String> uuids) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;
      int blockSize = JobsHelper.getBlockSize();
      List<TransferredResource> resources = JobsHelper.getTransferredResources(index, uuids);
      Plugin<TransferredResource> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<TransferredResource> block = new ArrayList<TransferredResource>();
      for (TransferredResource resource : resources) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, TransferredResource.class, blockSize);
          jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
          workersRouter.tell(new Messages.PluginExecuteIsReady<TransferredResource>(innerPlugin, block),
            jobStateInfoActor);
          block = new ArrayList<>();
        }
        block.add(resource);
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndInitJobPluginInfo(plugin, TransferredResource.class, block.size());
        jobStateInfoActor.tell(new Messages.PluginInitEnded<>(innerPlugin), ActorRef.noSender());
        workersRouter.tell(new Messages.PluginExecuteIsReady<TransferredResource>(innerPlugin, block),
          jobStateInfoActor);
      }

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

    } catch (Exception e) {
      LOGGER.error("Error running plugin on transferred resources", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  @Override
  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin) {
    try {
      LOGGER.info("Starting {} (which will be done asynchronously)", plugin.getName());
      ActorRef jobStateInfoActor = (ActorRef) context;

      plugin.beforeAllExecute(index, model, storage);

      initJobPluginInfo(plugin, 0);
      jobStateInfoActor.tell(new Messages.PluginInitEnded<>(plugin), ActorRef.noSender());
      workersRouter.tell(new Messages.PluginExecuteIsReady<T>(plugin, new ArrayList<T>()), jobStateInfoActor);

      jobStateInfoActor.tell(new Messages.JobInitEnded(), ActorRef.noSender());

    } catch (Exception e) {
      LOGGER.error("Error running plugin", e);
      PluginHelper.updateJobState(plugin, JOB_STATE.FAILED_TO_COMPLETE, e);
    }
  }

  // FIXME 20160629 hsilva: rename method
  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> plugin, int objectsCount)
    throws InvalidParameterException, PluginException {

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobInfoActor = runningJobs.get(jobId);
      if (PluginType.INGEST == plugin.getType()) {
        IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
        initJobPluginInfo(plugin, jobId, jobInfoActor, jobPluginInfo, objectsCount);
        plugin.injectJobPluginInfo(jobPluginInfo);
      } else if (PluginType.MISC == plugin.getType() || PluginType.AIP_TO_AIP == plugin.getType()) {
        SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo();
        initJobPluginInfo(plugin, jobId, jobInfoActor, jobPluginInfo, objectsCount);
        plugin.injectJobPluginInfo(jobPluginInfo);
      }
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }

  }

  private <T extends IsRODAObject> Plugin<T> getNewPluginInstanceAndInitJobPluginInfo(Plugin<T> plugin,
    Class<T> pluginClass, int objectsCount) throws InvalidParameterException, PluginException {
    Plugin<T> innerPlugin = RodaCoreFactory.getPluginManager().getPlugin(plugin.getClass().getCanonicalName(),
      pluginClass);
    innerPlugin.setParameterValues(plugin.getParameterValues());

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(innerPlugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobStateInfoActor = runningJobs.get(jobId);
      if (PluginType.INGEST == plugin.getType()) {
        IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
        initJobPluginInfo(innerPlugin, jobId, jobStateInfoActor, jobPluginInfo, objectsCount);
        innerPlugin.injectJobPluginInfo(jobPluginInfo);
      } else {
        SimpleJobPluginInfo jobPluginInfo = new SimpleJobPluginInfo();
        initJobPluginInfo(innerPlugin, jobId, jobStateInfoActor, jobPluginInfo, objectsCount);
        innerPlugin.injectJobPluginInfo(jobPluginInfo);
      }
    } else {
      LOGGER.error("Error while trying to init plugin. Cause: unable to find out job id");
    }

    return innerPlugin;

  }

  private <T extends IsRODAObject> void initJobPluginInfo(Plugin<T> innerPlugin, String jobId,
    ActorRef jobStateInfoActor, JobPluginInfo jobPluginInfo, int objectsCount) {
    jobPluginInfo.setSourceObjectsCount(objectsCount);
    jobPluginInfo.setSourceObjectsWaitingToBeProcessed(objectsCount);

    jobStateInfoActor.tell(new Messages.JobInfoUpdated(innerPlugin, jobPluginInfo), ActorRef.noSender());
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
        Timeout timeout = new Timeout(Duration.create(10, "minutes"));
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
    // FIXME 20160328 hsilva: this is not the solution as the messages are sent
    // async and until the processing of the current message is done, no other
    // is read
    // Timeout defaultTimeout = JobsHelper.getDefaultTimeout();
    // Patterns.ask(workersRouter, new Broadcast(Kill.getInstance()),
    // defaultTimeout);
    // Patterns.ask(workersRouter, Kill.getInstance(), defaultTimeout);
  }

  @Override
  public void startJobsInTheStateCreated() {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.JOB_STATE, Job.JOB_STATE.CREATED.toString()));
    Sublist sublist = new Sublist(0, RodaConstants.DEFAULT_PAGINATION_VALUE);
    IndexResult<Job> jobs = null;
    List<Job> jobsToBeStarted = new ArrayList<>();
    try {
      do {
        jobs = index.find(Job.class, filter, null, sublist);
        jobsToBeStarted.addAll(jobs.getResults());
        sublist.setFirstElementIndex(sublist.getFirstElementIndex() + Math.toIntExact(jobs.getLimit()));
      } while (jobs.getTotalCount() > jobs.getOffset() + jobs.getLimit());
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Unable to find Jobs still to be started", e);
    }

    if (!jobsToBeStarted.isEmpty()) {
      for (Job job : jobsToBeStarted) {
        try {
          executeJob(model.retrieveJob(job.getId()), true);
        } catch (JobAlreadyStartedException | RequestNotValidException | GenericException | NotFoundException
          | AuthorizationDeniedException e) {
          LOGGER.error("Unable to get Job", e);
        }
      }
    }
  }

  @Override
  public void cleanUnfinishedJobs() {
    Filter filter = new Filter();
    filter.add(new OneOfManyFilterParameter(RodaConstants.JOB_STATE,
      Arrays.asList(Job.JOB_STATE.STARTED.toString(), Job.JOB_STATE.CREATED.toString())));
    Sublist sublist = new Sublist(0, RodaConstants.DEFAULT_PAGINATION_VALUE);
    IndexResult<Job> jobs = null;
    List<Job> jobsToBeCleaned = new ArrayList<>();
    try {
      do {
        jobs = index.find(Job.class, filter, null, sublist);
        jobsToBeCleaned.addAll(jobs.getResults());
        sublist.setFirstElementIndex(sublist.getFirstElementIndex() + Math.toIntExact(jobs.getLimit()));
      } while (jobs.getTotalCount() > jobs.getOffset() + jobs.getLimit());
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Unable to find Jobs still to be cleaned", e);
    }

    if (!jobsToBeCleaned.isEmpty()) {
      for (Job job : jobsToBeCleaned) {
        try {
          Job jobToBeCleaned = model.retrieveJob(job.getId());
          JobsHelper.updateJobInTheStateStartedOrCreated(jobToBeCleaned);
          model.createOrUpdateJob(jobToBeCleaned);
        } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
          LOGGER.error("Unable to get/update Job", e);
        }
      }
    }
  }

  @Override
  public <T extends IsRODAObject> void updateJobState(Plugin<T> plugin, JOB_STATE state,
    Optional<String> stateDetails) {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobInfoActor = runningJobs.get(jobId);
      jobInfoActor.tell(new Messages.JobStateUpdated(plugin, state, stateDetails), ActorRef.noSender());
      if (state == JOB_STATE.COMPLETED) {
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

  @Override
  public <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, JobPluginInfo info) throws JobException {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null && runningJobs.get(jobId) != null) {
      ActorRef jobInfoActor = runningJobs.get(jobId);
      jobInfoActor.tell(new Messages.JobInfoUpdated(plugin, info), ActorRef.noSender());
    } else {
      throw new JobException("Job id or job information is null");
    }
  }

}
