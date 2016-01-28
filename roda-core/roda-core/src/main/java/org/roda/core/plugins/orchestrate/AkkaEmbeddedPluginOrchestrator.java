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

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaJobWorkerActor;
import org.roda.core.plugins.orchestrate.akka.AkkaWorkerActor;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
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

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private ActorSystem workersSystem;
  private ActorRef workersRouter;
  private ActorRef jobWorkersRouter;
  // private ActorRef boss;
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

  public ActorRef getCoordinator() {
    return jobWorkersRouter;
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
        futures.add(Patterns.ask(workersRouter, new PluginMessage<T>(find.getResults(), plugin),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void runPluginOnAIPs(Plugin<AIP> plugin, List<String> ids) {
    try {
      int multiplier = 0;
      LOGGER.info("Executing beforeExecute");
      plugin.beforeExecute(index, model, storage);
      Iterator<String> iter = ids.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();
      String aipId;

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        aipId = iter.next();
        block.add(model.retrieveAIP(aipId));

      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }
    LOGGER.info("End of method");
  }

  @Override
  public void runPluginOnAllAIPs(Plugin<AIP> plugin) {
    try {
      int multiplier = 0;
      LOGGER.info("Executing beforeExecute");
      plugin.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> iter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<AIP>(block, plugin),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }
    LOGGER.info("End of method");

  }

  @Override
  public void runPluginOnAllRepresentations(Plugin<Representation> plugin) {
    try {
      int multiplier = 0;
      plugin.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> aipIter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<Representation> block = new ArrayList<Representation>();
      while (aipIter.hasNext()) {
        AIP aip = aipIter.next();
        for (Representation representation : aip.getRepresentations()) {
          if (block.size() == BLOCK_SIZE) {
            futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, plugin),
              new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
            block = new ArrayList<Representation>();
            multiplier++;
          }

          block.add(representation);
        }
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, plugin),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }

  }

  @Override
  public void runPluginOnAllFiles(Plugin<File> plugin) {
    try {
      int multiplier = 0;
      plugin.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> aipIter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<File> block = new ArrayList<File>();
      while (aipIter.hasNext()) {
        AIP aip = aipIter.next();
        for (Representation representation : aip.getRepresentations()) {
          Iterable<File> files = model.listFilesDirectlyUnder(aip.getId(), representation.getId());
          Iterator<File> fileIter = files.iterator();

          while (fileIter.hasNext()) {
            if (block.size() == BLOCK_SIZE) {
              futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, plugin),
                new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
              block = new ArrayList<File>();
              multiplier++;
            }

            block.add(fileIter.next());
          }
        }

      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<File>(block, plugin),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

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

  @Override
  public void runPluginOnTransferredResources(Plugin<TransferredResource> plugin, List<TransferredResource> resources) {
    try {
      int multiplier = 0;
      LOGGER.info("Executing beforeExecute");
      plugin.beforeExecute(index, model, storage);
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<TransferredResource> block = new ArrayList<TransferredResource>();
      for (TransferredResource resource : resources) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(workersRouter, new PluginMessage<TransferredResource>(block, plugin),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<TransferredResource>();
          multiplier++;
        }

        block.add(resource);
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<TransferredResource>(block, plugin),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, workersSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      plugin.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }
    LOGGER.info("End of method");

  }

  @Override
  public <T extends Serializable> void runPlugin(Plugin<T> plugin) {
    try {
      LOGGER.info("Executing beforeExecute");
      plugin.beforeExecute(index, model, storage);

      Future<Object> askFuture = Patterns.ask(workersRouter, new PluginMessage<T>(new ArrayList<T>(), plugin),
        new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT)));

      plugin.afterExecute(index, model, storage);
      LOGGER.info("End of method");
    } catch (Exception e) {
      // // FIXME catch proper exception
      e.printStackTrace();
    }
  }

  @Override
  public <T extends Serializable> void runPluginOnObjects(Plugin<T> plugin, List<String> ids) {
    // TODO Auto-generated method stub

  }

}
