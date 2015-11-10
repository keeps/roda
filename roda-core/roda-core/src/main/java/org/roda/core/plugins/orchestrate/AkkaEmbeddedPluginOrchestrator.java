/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Representation;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.File;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.AkkaCoordinatorActor;
import org.roda.core.plugins.orchestrate.akka.AkkaWorkerActor;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.StorageService;

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
  private final Logger logger = Logger.getLogger(getClass());

  private static final int BLOCK_SIZE = 100;
  private static final Sorter SORTER = null;
  private static final int TIMEOUT = 1;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private ActorSystem workersSystem;
  private ActorRef workersRouter;
  private ActorRef boss;
  private int numberOfWorkers;

  public AkkaEmbeddedPluginOrchestrator() {
    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    numberOfWorkers = Runtime.getRuntime().availableProcessors() + 1;
    workersSystem = ActorSystem.create("WorkersSystem");

    // FIXME not in use: see if this actor is really needed
    boss = workersSystem.actorOf(Props.create(AkkaCoordinatorActor.class), "boss");
    Props roundRobinPoolProps = new RoundRobinPool(numberOfWorkers)
      .props(Props.create(AkkaWorkerActor.class, storage, model, index));
    workersRouter = workersSystem.actorOf(roundRobinPoolProps, "WorkersRouter");
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
      logger.info("Executing beforeExecute");
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
    logger.info("End of method");
  }

  @Override
  public void runPluginOnAllAIPs(Plugin<AIP> plugin) {
    try {
      int multiplier = 0;
      logger.info("Executing beforeExecute");
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
    logger.info("End of method");

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
        ClosableIterable<Representation> reps = model.listRepresentations(aip.getId());
        Iterator<Representation> repIter = reps.iterator();

        while (repIter.hasNext()) {

          if (block.size() == BLOCK_SIZE) {
            futures.add(Patterns.ask(workersRouter, new PluginMessage<Representation>(block, plugin),
              new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
            block = new ArrayList<Representation>();
            multiplier++;
          }

          block.add(repIter.next());
        }

        reps.close();
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
        ClosableIterable<Representation> reps = model.listRepresentations(aip.getId());
        Iterator<Representation> repIter = reps.iterator();

        while (repIter.hasNext()) {
          Representation rep = repIter.next();

          Iterable<File> files = model.listFiles(aip.getId(), rep.getId());
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

        reps.close();
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

  public class PluginMessage<T> {
    private List<? extends T> list;
    private Plugin<? extends T> plugin;

    public PluginMessage(List<? extends T> list, Plugin<? extends T> plugin) {
      this.list = list;
      this.plugin = plugin;
    }

    public List<? extends T> getList() {
      return list;
    }

    public void setList(List<? extends T> list) {
      this.list = list;
    }

    public Plugin<? extends T> getPlugin() {
      return plugin;
    }

    public void setPlugin(Plugin<? extends T> plugin) {
      this.plugin = plugin;
    }

  }

  @Override
  public void runPluginOnFiles(Plugin<String> plugin, List<Path> paths) {
    try {
      int multiplier = 0;
      logger.info("Executing beforeExecute");
      plugin.beforeExecute(index, model, storage);
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<String> block = new ArrayList<String>();
      for (Path path : paths) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(workersRouter, new PluginMessage<String>(block, plugin),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<String>();
          multiplier++;
        }

        block.add(path.toString());
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(workersRouter, new PluginMessage<String>(block, plugin),
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
    logger.info("End of method");

  }

}
