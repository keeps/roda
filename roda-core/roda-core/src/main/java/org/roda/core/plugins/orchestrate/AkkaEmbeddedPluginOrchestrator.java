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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaJobWorkerActor;
import org.roda.core.plugins.orchestrate.akka.AkkaWorkerActor;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class AkkaEmbeddedPluginOrchestrator implements PluginOrchestrator {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaEmbeddedPluginOrchestrator.class);

  private static final int BLOCK_SIZE = 100;
  private static final Sorter SORTER = null;
  private static final int TIMEOUT = 1;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;
  private static final Timeout DEFAULT_TIMEOUT = new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT));

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private ActorSystem workersSystem;
  private ActorRef workersRouter;
  private ActorRef jobWorkersRouter;
  private int numberOfWorkers;

  public AkkaEmbeddedPluginOrchestrator() {
    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    numberOfWorkers = Runtime.getRuntime().availableProcessors() + 1;
    workersSystem = ActorSystem.create("WorkersSystem");

    Props roundRobinPoolProps = new RoundRobinPool(numberOfWorkers)
      .props(Props.create(AkkaWorkerActor.class, storage, model, index));
    workersRouter = workersSystem.actorOf(roundRobinPoolProps, "WorkersRouter");

    Props roundRobinPoolProps2 = new RoundRobinPool(numberOfWorkers).props(Props.create(AkkaJobWorkerActor.class));
    jobWorkersRouter = workersSystem.actorOf(roundRobinPoolProps2, "JobWorkersRouter");
  }

  @Override
  public void setup() {
    // do nothing
  }

  @Override
  public void shutdown() {
    workersSystem.shutdown();
  }

  @Override
  public <T extends Serializable> void runPluginFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> plugin) {
    try {
      plugin.beforeExecute(index, model, storage);
      IndexResult<T> find;
      int offset = 0;
      int multiplier = 0;
      List<Future<Object>> futures = new ArrayList<Future<Object>>();
      do {
        // XXX block size could be recommended by plugin
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, SORTER, new Sublist(offset, BLOCK_SIZE));
        offset += find.getLimit();
        multiplier++;
        futures.add(Patterns.ask(workersRouter, new PluginMessage<T>(find.getResults(), plugin), DEFAULT_TIMEOUT));

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      LOGGER.error("Error running plugin from index", e);
    }
  }

  @Override
  public List<Report> runPluginOnAIPs(Plugin<AIP> plugin, List<String> ids) {
    try {
      int multiplier = 0;
      LOGGER.info("Started " + plugin.getName());
      plugin.beforeExecute(index, model, storage);
      Iterator<String> iter = ids.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();
      String aipId;

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin), DEFAULT_TIMEOUT));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        aipId = iter.next();
        block.add(model.retrieveAIP(aipId));

      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin), DEFAULT_TIMEOUT));
        multiplier++;
      }

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Iterable<Object> reports = Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

      return mapToReports(reports);

    } catch (Exception e) {
      // FIXME catch proper exception
      LOGGER.error("Error executing job", e);
    }
    LOGGER.info("Ended " + plugin.getName());
    return null;
  }

  private List<Report> mapToReports(Iterable<Object> reports) {
    List<Report> ret;
    ret = new ArrayList<>();
    for (Object o : reports) {
      if (o instanceof Report) {
        ret.add((Report) o);
      } else {
        LOGGER.warn("Got a response that was not a report: {}", o.getClass().getName());
      }
    }
    return ret;
  }

  @Override
  public List<Report> runPluginOnAllAIPs(Plugin<AIP> plugin) {
    try {
      int multiplier = 0;
      LOGGER.info("Started " + plugin.getName());
      plugin.beforeExecute(index, model, storage);
      CloseableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> iter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin), DEFAULT_TIMEOUT));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin), DEFAULT_TIMEOUT));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Iterable<Object> reports = Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

      return mapToReports(reports);

    } catch (Exception e) {
      // FIXME catch proper exception
      LOGGER.error("Error running plugin on all AIPs", e);
    }
    LOGGER.info("Ended " + plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnAllRepresentations(Plugin<Representation> plugin) {
    LOGGER.info("Started " + plugin.getName());
    try {
      int multiplier = 0;
      plugin.beforeExecute(index, model, storage);
      CloseableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> aipIter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<Representation> block = new ArrayList<Representation>();
      while (aipIter.hasNext()) {
        AIP aip = aipIter.next();
        for (Representation representation : aip.getRepresentations()) {
          if (block.size() == BLOCK_SIZE) {
            futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, plugin), DEFAULT_TIMEOUT));
            block = new ArrayList<Representation>();
            multiplier++;
          }

          block.add(representation);
        }
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, plugin), DEFAULT_TIMEOUT));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Iterable<Object> reports = Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

      return mapToReports(reports);
    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
      LOGGER.error("Error while runPluginOnAllRepresentations", e);
    }
    LOGGER.info("Ended " + plugin.getName());
    return null;
  }

  @Override
  public List<Report> runPluginOnAllFiles(Plugin<File> plugin) {
    try {
      int multiplier = 0;
      plugin.beforeExecute(index, model, storage);
      CloseableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> aipIter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<File> block = new ArrayList<File>();
      while (aipIter.hasNext()) {
        AIP aip = aipIter.next();
        for (Representation representation : aip.getRepresentations()) {
          boolean recursive = true;
          CloseableIterable<File> files = model.listFilesUnder(aip.getId(), representation.getId(), recursive);
          Iterator<File> fileIter = files.iterator();

          while (fileIter.hasNext()) {

            if (block.size() == BLOCK_SIZE) {
              futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, plugin), DEFAULT_TIMEOUT));
              block = new ArrayList<File>();
              multiplier++;
            }

            File file = fileIter.next();
            if (!file.isDirectory()) {
              block.add(file);
            }
          }
          IOUtils.closeQuietly(files);

        }

      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, plugin), DEFAULT_TIMEOUT));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Iterable<Object> reports = Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

      return mapToReports(reports);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      LOGGER.error("Error while runPluginOnAllFiles", e);
    }
    return null;

  }

  @Override
  public List<Report> runPluginOnTransferredResources(Plugin<TransferredResource> plugin,
    List<TransferredResource> resources) {
    try {
      int multiplier = 0;
      LOGGER.info("Started " + plugin.getName());
      plugin.beforeExecute(index, model, storage);
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<TransferredResource> block = new ArrayList<TransferredResource>();
      for (TransferredResource resource : resources) {
        if (block.size() == BLOCK_SIZE) {
          futures
            .add(Patterns.ask(workersRouter, new PluginMessage<TransferredResource>(block, plugin), DEFAULT_TIMEOUT));
          block = new ArrayList<TransferredResource>();
          multiplier++;
        }

        block.add(resource);
      }

      if (!block.isEmpty()) {
        futures
          .add(Patterns.ask(workersRouter, new PluginMessage<TransferredResource>(block, plugin), DEFAULT_TIMEOUT));
        multiplier++;
      }

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Iterable<Object> reports = Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

      return mapToReports(reports);

    } catch (Exception e) {
      // FIXME catch proper exception
      LOGGER.error("Error while runPluginOnTransferredResources", e);
    }
    LOGGER.info("Ended " + plugin.getName());
    return null;
  }

  @Override
  public void executeJob(Job job) {
    Future<Object> future = Patterns.ask(jobWorkersRouter, job, DEFAULT_TIMEOUT);

    future.onSuccess(new OnSuccess<Object>() {
      @Override
      public void onSuccess(Object msg) throws Throwable {
        LOGGER.info("Success executing job with id '{}'", job.getId());
      }
    }, workersSystem.dispatcher());
    future.onFailure(new OnFailure() {
      @Override
      public void onFailure(Throwable error) throws Throwable {
        LOGGER.error("Failure executing job with id '{}': {}", job.getId(), error);
      }
    }, workersSystem.dispatcher());

  }

  @Override
  public <T extends Serializable> void runPlugin(Plugin<T> plugin) {
    try {
      LOGGER.info("Started " + plugin.getName());
      plugin.beforeExecute(index, model, storage);

      // FIXME what to do with the askFuture???
      Future<Object> future = Patterns.ask(workersRouter, new PluginMessage<T>(new ArrayList<T>(), plugin),
        DEFAULT_TIMEOUT);

      future.onSuccess(new OnSuccess<Object>() {
        @Override
        public void onSuccess(Object msg) throws Throwable {
          // FIXME this should be sent inside a message that can be easily
          // identified as a list of reports
          if (msg != null && msg instanceof List<?>) {
            LOGGER.info("Success executing job: {}", (List<Report>) msg);
          }

          plugin.afterExecute(index, model, storage);
          LOGGER.info("Ended " + plugin.getName());
        }
      }, workersSystem.dispatcher());
      future.onFailure(new OnFailure() {
        @Override
        public void onFailure(Throwable error) throws Throwable {
          LOGGER.error("Failure executing job: {}", error);

          plugin.afterExecute(index, model, storage);
          LOGGER.info("Ended " + plugin.getName());
        }
      }, workersSystem.dispatcher());

    } catch (Exception e) {
      // // FIXME catch proper exception
      LOGGER.error("Error while runPlugin", e);
    }
  }

  @Override
  public <T extends Serializable> void runPluginOnObjects(Plugin<T> plugin, List<String> ids) {
    // try {
    // int multiplier = 0;
    // LOGGER.info("Executing beforeExecute");
    // plugin.beforeExecute(index, model, storage);
    // List<Future<Object>> futures = new ArrayList<Future<Object>>();
    //
    // List<String> block = new ArrayList<String>();
    // for (String id : ids) {
    // if (block.size() == BLOCK_SIZE) {
    // futures.add(Patterns.ask(workersRouter, new PluginMessage<T>(block,
    // plugin),
    // new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
    // block = new ArrayList<String>();
    // multiplier++;
    // }
    //
    // block.add(id);
    // }
    //
    // if (!block.isEmpty()) {
    // futures.add(Patterns.ask(workersRouter, new PluginMessage<T>(block,
    // plugin),
    // new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
    // multiplier++;
    // }
    //
    // final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures,
    // workersSystem.dispatcher());
    // Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT,
    // TIMEOUT_UNIT));
    //
    // plugin.afterExecute(index, model, storage);
    //
    // } catch (Exception e) {
    // // FIXME catch proper exception
    // e.printStackTrace();
    // }
    // LOGGER.info("End of method");

  }

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
