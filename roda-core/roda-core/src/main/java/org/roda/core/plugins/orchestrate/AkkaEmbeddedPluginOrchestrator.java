/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

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
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaJobWorkerActor;
import org.roda.core.plugins.orchestrate.akka.AkkaWorkerActor;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Kill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.Broadcast;
import akka.routing.RoundRobinPool;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class AkkaEmbeddedPluginOrchestrator implements PluginOrchestrator {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEmbeddedPluginOrchestrator.class);

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private ActorSystem workersSystem;
  private ActorRef workersRouter;
  private ActorRef jobWorkersRouter;
  private int numberOfWorkers;

  // Map< jobId, JobInfo>
  private Map<String, JobInfo> runningJobs;

  public AkkaEmbeddedPluginOrchestrator() {
    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    runningJobs = new HashMap<>();

    numberOfWorkers = JobsHelper.getNumberOfPluginWorkers();
    workersSystem = ActorSystem.create("WorkersSystem");

    Props workersProps = new RoundRobinPool(numberOfWorkers)
      .props(Props.create(AkkaWorkerActor.class, storage, model, index));
    workersRouter = workersSystem.actorOf(workersProps, "WorkersRouter");

    Props jobsProps = new RoundRobinPool(numberOfWorkers).props(Props.create(AkkaJobWorkerActor.class));
    jobWorkersRouter = workersSystem.actorOf(jobsProps, "JobWorkersRouter");

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
  public <T extends IsIndexed> List<Report> runPluginFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> plugin) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      IndexResult<T> find;
      int offset = 0;
      int multiplier = 0;
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<T>> innerPlugins = new ArrayList<>();
      Plugin<T> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      do {
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, Sorter.NONE,
          new Sublist(offset, blockSize));
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, classToActOn, innerPlugins,
          (int) find.getLimit());
        offset += find.getLimit();
        multiplier++;

        futures.add(Patterns.ask(workersRouter, new PluginMessage<T>(find.getResults(), innerPlugin), timeout));

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<T> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin from index", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnAIPs(Plugin<AIP> plugin, List<String> ids) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      Iterator<String> iter = ids.iterator();
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<AIP>> innerPlugins = new ArrayList<>();
      Plugin<AIP> innerPlugin;
      String aipId;

      plugin.beforeAllExecute(index, model, storage);

      List<AIP> block = new ArrayList<AIP>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, AIP.class, innerPlugins, blockSize);
          futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, innerPlugin), timeout));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        aipId = iter.next();
        block.add(model.retrieveAIP(aipId));

      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, AIP.class, innerPlugins, block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, innerPlugin), timeout));
        multiplier++;
      }

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<AIP> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on AIPs", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnRepresentations(Plugin<Representation> plugin, List<String> ids) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      List<Representation> representations = JobsHelper.getRepresentations(model, index, ids);
      Iterator<Representation> iter = representations.iterator();
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<Representation>> innerPlugins = new ArrayList<>();
      Plugin<Representation> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<Representation> block = new ArrayList<Representation>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, Representation.class, innerPlugins, blockSize);
          futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, innerPlugin), timeout));
          block = new ArrayList<Representation>();
          multiplier++;
        }

        block.add(iter.next());

      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, Representation.class, innerPlugins, block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, innerPlugin), timeout));
        multiplier++;
      }

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<Representation> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on Representations", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnFiles(Plugin<File> plugin, List<String> ids) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      List<File> files = JobsHelper.getFiles(model, index, ids);
      Iterator<File> iter = files.iterator();
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<File>> innerPlugins = new ArrayList<>();
      Plugin<File> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<File> block = new ArrayList<File>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, File.class, innerPlugins, blockSize);
          futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, innerPlugin), timeout));
          block = new ArrayList<File>();
          multiplier++;
        }

        block.add(iter.next());

      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, File.class, innerPlugins, block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, innerPlugin), timeout));
        multiplier++;
      }

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<File> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on Files", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnAllAIPs(Plugin<AIP> plugin) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> iter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<AIP>> innerPlugins = new ArrayList<>();
      Plugin<AIP> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<AIP> block = new ArrayList<AIP>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      while (iter.hasNext()) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, AIP.class, innerPlugins, blockSize);
          futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, innerPlugin), timeout));
          block = new ArrayList<AIP>();
          multiplier++;
        }
        OptionalWithCause<AIP> nextAIP = iter.next();
        if (nextAIP.isPresent()) {
          block.add(nextAIP.get());
        } else {
          LOGGER.error("Cannot process AIP", nextAIP.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, AIP.class, innerPlugins, block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, innerPlugin), timeout));
        multiplier++;
      }

      IOUtils.closeQuietly(aips);

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<AIP> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all AIPs", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnAllRepresentations(Plugin<Representation> plugin) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<Representation>> innerPlugins = new ArrayList<>();
      Plugin<Representation> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<Representation> block = new ArrayList<Representation>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      while (aipIter.hasNext()) {
        OptionalWithCause<AIP> aip = aipIter.next();
        if (aip.isPresent()) {
          for (Representation representation : aip.get().getRepresentations()) {
            if (block.size() == blockSize) {
              innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, Representation.class, innerPlugins,
                blockSize);
              futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, innerPlugin), timeout));
              block = new ArrayList<Representation>();
              multiplier++;
            }
            block.add(representation);
          }
        } else {
          LOGGER.error("Cannot process AIP", aip.getCause());
        }
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, Representation.class, innerPlugins, block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, innerPlugin), timeout));
        multiplier++;
      }

      IOUtils.closeQuietly(aips);

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<Representation> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all representations", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnAllFiles(Plugin<File> plugin) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<File>> innerPlugins = new ArrayList<>();
      Plugin<File> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<File> block = new ArrayList<File>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
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
                innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, File.class, innerPlugins, blockSize);
                futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, innerPlugin), timeout));
                block = new ArrayList<File>();
                multiplier++;
              }

              OptionalWithCause<File> file = fileIter.next();
              if (file.isPresent()) {
                if (!file.get().isDirectory()) {
                  block.add(file.get());
                }
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
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, File.class, innerPlugins, block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, innerPlugin), timeout));
        multiplier++;
      }

      IOUtils.closeQuietly(aips);

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<File> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on all files", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnTransferredResources(Plugin<TransferredResource> plugin,
    List<TransferredResource> resources) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      int blockSize = JobsHelper.getBlockSize();
      int multiplier = 0;
      List<Future<Object>> futures = new ArrayList<>();
      List<Plugin<TransferredResource>> innerPlugins = new ArrayList<>();
      Plugin<TransferredResource> innerPlugin;

      plugin.beforeAllExecute(index, model, storage);

      List<TransferredResource> block = new ArrayList<TransferredResource>();
      Timeout timeout = JobsHelper.getPluginTimeout(plugin.getClass());
      for (TransferredResource resource : resources) {
        if (block.size() == blockSize) {
          innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, TransferredResource.class, innerPlugins,
            blockSize);
          futures.add(Patterns.ask(workersRouter, new PluginMessage<TransferredResource>(block, innerPlugin), timeout));
          block = new ArrayList<TransferredResource>();
          multiplier++;
        }

        block.add(resource);
      }

      if (!block.isEmpty()) {
        innerPlugin = getNewPluginInstanceAndRunBeforeExecute(plugin, TransferredResource.class, innerPlugins,
          block.size());
        futures.add(Patterns.ask(workersRouter, new PluginMessage<TransferredResource>(block, innerPlugin), timeout));
        multiplier++;
      }

      Optional<Iterable<Object>> reports = waitForFuturesToComplete(plugin, multiplier, futures);

      for (Plugin<TransferredResource> p : innerPlugins) {
        p.afterBlockExecute(index, model, storage);
      }

      plugin.afterAllExecute(index, model, storage);

      PluginHelper.updateJobPercentage(plugin, 100);
      LOGGER.info("Ended {}", plugin.getName());
      return mapToReports(reports);

    } catch (Exception e) {
      LOGGER.error("Error running plugin on transferred resources", e);
    }

    PluginHelper.updateJobPercentage(plugin, 100);
    LOGGER.info("Ended {}", plugin.getName());
    return null;
  }

  @Override
  public <T extends Serializable> void runPlugin(Plugin<T> plugin) {
    try {
      LOGGER.info("Started {}", plugin.getName());
      plugin.beforeAllExecute(index, model, storage);
      plugin.beforeBlockExecute(index, model, storage);

      // FIXME what to do with the askFuture???
      Future<Object> future = Patterns.ask(workersRouter, new PluginMessage<T>(new ArrayList<T>(), plugin),
        JobsHelper.getPluginTimeout(plugin.getClass()));

      future.onSuccess(new OnSuccess<Object>() {
        @Override
        public void onSuccess(Object msg) throws Throwable {
          // FIXME this should be sent inside a message that can be easily
          // identified as a list of reports
          if (msg != null && msg instanceof List<?>) {
            LOGGER.info("Success running plugin: {}", (List<Report>) msg);
          }

          plugin.afterBlockExecute(index, model, storage);
          plugin.afterAllExecute(index, model, storage);

          PluginHelper.updateJobPercentage(plugin, 100);

          LOGGER.info("Ended {}", plugin.getName());
        }
      }, workersSystem.dispatcher());
      future.onFailure(new OnFailure() {
        @Override
        public void onFailure(Throwable error) throws Throwable {
          LOGGER.error("Failure running plugin: {}", error);

          plugin.afterBlockExecute(index, model, storage);
          plugin.afterAllExecute(index, model, storage);

          PluginHelper.updateJobPercentage(plugin, 100);

          LOGGER.info("Ended {}", plugin.getName());
        }
      }, workersSystem.dispatcher());

    } catch (Exception e) {
      LOGGER.error("Error running plugin", e);
    }
  }

  @Override
  public <T extends Serializable> void runPluginOnObjects(Plugin<T> plugin, List<String> ids) {
    // FIXME
    LOGGER.error("Method runPluginOnObjects@{} still not implemented!", this.getClass().getName());
  }

  private <T extends Serializable> Plugin<T> getNewPluginInstanceAndRunBeforeExecute(Plugin<T> plugin,
    Class<T> pluginClass, List<Plugin<T>> innerPlugins, int objectsCount)
    throws InvalidParameterException, PluginException {
    Plugin<T> innerPlugin = RodaCoreFactory.getPluginManager().getPlugin(plugin.getClass().getCanonicalName(),
      pluginClass);
    innerPlugin.setParameterValues(plugin.getParameterValues());
    innerPlugins.add(innerPlugin);
    innerPlugin.beforeBlockExecute(index, model, storage);

    // keep track of each job/plugin relation
    String jobId = PluginHelper.getJobId(innerPlugin);
    if (jobId != null) {
      synchronized (runningJobs) {
        if (PluginType.INGEST == plugin.getType()) {
          IngestJobPluginInfo jobPluginInfo = new IngestJobPluginInfo();
          initJobPluginInfo(innerPlugin, jobId, jobPluginInfo, objectsCount);
        } else if (PluginType.MISC == plugin.getType()) {
          SimpleJobPluginInfo pluginInfo = new SimpleJobPluginInfo(objectsCount);
          initJobPluginInfo(innerPlugin, jobId, pluginInfo, objectsCount);
        } else if (PluginType.AIP_TO_AIP == plugin.getType()) {
          SimpleJobPluginInfo pluginInfo = new SimpleJobPluginInfo(objectsCount);
          initJobPluginInfo(innerPlugin, jobId, pluginInfo, objectsCount);
        }
      }
    }

    return innerPlugin;
  }

  private <T extends Serializable> Optional<Iterable<Object>> waitForFuturesToComplete(Plugin<T> plugin, int multiplier,
    List<Future<Object>> futures) {
    final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
    Optional<Iterable<Object>> reports = Optional.empty();
    try {
      reports = Optional
        .ofNullable(Await.result(sequenceResult, JobsHelper.getDuration(plugin.getClass(), multiplier)));
    } catch (TimeoutException e) {
      String jobId = PluginHelper.getJobId(plugin);
      if (jobId != null) {
        runningJobs.get(jobId).setHasTimeoutOccurred(true);
      }
      LOGGER.error("A timeout occured while waiting for futures to complete", e);
    } catch (Exception e) {
      LOGGER.error("Error while waiting for futures to complete", e);
    }
    return reports;
  }

  private List<Report> mapToReports(Optional<Iterable<Object>> reports) {
    List<Report> ret = new ArrayList<>();
    if (reports.isPresent()) {
      for (Object o : reports.get()) {
        if (o instanceof Report) {
          ret.add((Report) o);
        } else {
          LOGGER.warn("Got a response that was not a report: {}", o.getClass().getName());
        }
      }
    }
    return ret;
  }

  private <T extends Serializable> void initJobPluginInfo(Plugin<T> innerPlugin, String jobId,
    JobPluginInfo jobPluginInfo, int objectsCount) {
    jobPluginInfo.setObjectsCount(objectsCount);
    jobPluginInfo.setObjectsWaitingToBeProcessed(objectsCount);
    if (runningJobs.get(jobId) != null) {
      runningJobs.get(jobId).put(innerPlugin, jobPluginInfo, objectsCount);
    } else {
      JobInfo jobState = new JobInfo();
      jobState.put(innerPlugin, jobPluginInfo, objectsCount);
      runningJobs.put(jobId, jobState);
    }
  }

  @Override
  public void executeJob(Job job) throws JobAlreadyStartedException {
    LOGGER.info("Adding job '{}' ({}) to be executed", job.getName(), job.getId());

    if (runningJobs.containsKey(job.getId())) {
      LOGGER.info("Job '{}' ({}) is already queued to be executed", job.getName(), job.getId());
      throw new JobAlreadyStartedException();
    } else {
      jobWorkersRouter.tell(job, ActorRef.noSender());
      LOGGER.info("Success adding job '{}' ({})", job.getName(), job.getId());
    }

  }

  @Override
  public void stopJob(Job job) {
    // FIXME 20160328 hsilva: this is not the solution as the messages are sent
    // async and until the processing of the current message is done, no other
    // is read
    Timeout defaultTimeout = JobsHelper.getDefaultTimeout();
    Patterns.ask(workersRouter, new Broadcast(Kill.getInstance()), defaultTimeout);
    Patterns.ask(workersRouter, Kill.getInstance(), defaultTimeout);
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
          executeJob(model.retrieveJob(job.getId()));
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
  public <T extends Serializable> void updateJobPercentage(Plugin<T> plugin, int percentage) {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null) {
      // FIXME 20160331 hsilva: this will block the update of all jobs (whereas
      // it should block per job)
      synchronized (runningJobs) {
        PluginHelper.updateJobPercentage(plugin, model, percentage);
        if (percentage == 100) {
          runningJobs.remove(jobId);
        }
      }
    } else {
      LOGGER.error("Got a NULL jobId when updating Job percentage");
    }
  }

  @Override
  public <T extends Serializable> void updateJobInformation(Plugin<T> plugin, JobPluginInfo info) throws JobException {
    String jobId = PluginHelper.getJobId(plugin);
    if (jobId != null) {
      // FIXME 20160331 hsilva: this will block the update of all jobs (whereas
      // it should block per job)
      synchronized (runningJobs) {
        JobInfo jobInfo = runningJobs.get(jobId);
        if (jobInfo != null) {
          if (jobInfo.isHasTimeoutOccurred()) {
            throw new TimeoutJobException("Job timeout occurred");
          } else {
            Integer jobObjectsCount = jobInfo.getObjectsCount();
            JobPluginInfo infoUpdated = info.processJobPluginInformation(plugin, jobObjectsCount, jobInfo.getJobInfo());
            PluginHelper.updateJobInformation(plugin, model, infoUpdated);
          }
        }
      }
    } else {
      throw new JobException("Job id is null");
    }
  }

  // FIXME 20160329 hsilva: when this class is stable, add it to its own class
  // file
  public class PluginMessage<T extends Serializable> {
    private List<T> list;
    private Plugin<T> plugin;

    public PluginMessage(List<T> list, Plugin<T> plugin) {
      this.list = list;
      this.plugin = plugin;
    }

    public List<T> getList() {
      return list;
    }

    public void setList(List<T> list) {
      this.list = list;
    }

    public Plugin<T> getPlugin() {
      return plugin;
    }

    public void setPlugin(Plugin<T> plugin) {
      this.plugin = plugin;
    }
  }

}
