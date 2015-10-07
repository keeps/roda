package org.roda.action.orchestrate.embed;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.ActionOrchestrator;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.embed.actors.CoordinatorActor;
import org.roda.action.orchestrate.embed.actors.WorkerActor;
import org.roda.common.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.Representation;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageService;

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

public class AkkaEmbeddedActionOrchestrator implements ActionOrchestrator {
  private final Logger logger = Logger.getLogger(getClass());

  private static final int BLOCK_SIZE = 100;
  private static final Sorter SORTER = null;
  private static final int TIMEOUT = 1;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private ActorSystem actionSystem;
  private ActorRef actionRouter;
  private ActorRef boss;
  private int numberOfWorkers;

  public AkkaEmbeddedActionOrchestrator() {
    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    numberOfWorkers = Runtime.getRuntime().availableProcessors() + 1;
    actionSystem = ActorSystem.create("ActionSystem");

    // FIXME not in use: see if this actor is really needed
    boss = actionSystem.actorOf(Props.create(CoordinatorActor.class), "boss");
    Props roundRobinPoolProps = new RoundRobinPool(numberOfWorkers)
      .props(Props.create(WorkerActor.class, storage, model, index));
    actionRouter = actionSystem.actorOf(roundRobinPoolProps, "router");
  }

  @Override
  public void setup() {
    // do nothing
  }

  @Override
  public void shutdown() {
    actionSystem.shutdown();
  }

  @Override
  public <T extends Serializable> void runActionFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> action) {
    try {
      action.beforeExecute(index, model, storage);
      IndexResult<T> find;
      int offset = 0;
      int multiplier = 0;
      List<Future<Object>> futures = new ArrayList<Future<Object>>();
      do {
        // XXX block size could be recommended by plugin
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, SORTER, new Sublist(offset, BLOCK_SIZE));
        offset += find.getLimit();
        multiplier++;
        futures.add(Patterns.ask(actionRouter, new ActionMessage<T>(find.getResults(), action),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, actionSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      action.afterExecute(index, model, storage);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void runActionOnAIPs(Plugin<AIP> action, List<String> ids) {
    try {
      int multiplier = 0;
      logger.info("Executing beforeExecute");
      action.beforeExecute(index, model, storage);
      Iterator<String> iter = ids.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();
      String aipId;

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(actionRouter, new ActionMessage<AIP>(block, action),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        aipId = iter.next();
        block.add(model.retrieveAIP(aipId));

      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(actionRouter, new ActionMessage<AIP>(block, action),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, actionSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      action.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }
    logger.info("End of method");
  }

  @Override
  public void runActionOnAllAIPs(Plugin<AIP> action) {
    try {
      int multiplier = 0;
      logger.info("Executing beforeExecute");
      action.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> iter = aips.iterator();
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(actionRouter, new ActionMessage<AIP>(block, action),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<AIP>();
          multiplier++;
        }

        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(actionRouter, new ActionMessage<AIP>(block, action),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, actionSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      action.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }
    logger.info("End of method");

  }

  @Override
  public void runActionOnAllRepresentations(Plugin<Representation> action) {
    try {
      int multiplier = 0;
      action.beforeExecute(index, model, storage);
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
            futures.add(Patterns.ask(actionRouter, new ActionMessage<Representation>(block, action),
              new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
            block = new ArrayList<Representation>();
            multiplier++;
          }

          block.add(repIter.next());
        }

        reps.close();
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(actionRouter, new ActionMessage<Representation>(block, action),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, actionSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      action.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }

  }

  @Override
  public void runActionOnAllFiles(Plugin<File> action) {
    try {
      int multiplier = 0;
      action.beforeExecute(index, model, storage);
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
              futures.add(Patterns.ask(actionRouter, new ActionMessage<File>(block, action),
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
        futures.add(Patterns.ask(actionRouter, new ActionMessage<File>(block, action),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      aips.close();

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, actionSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      action.afterExecute(index, model, storage);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public class ActionMessage<T> {
    private List<? extends T> list;
    private Plugin<? extends T> action;

    public ActionMessage(List<? extends T> list, Plugin<? extends T> action) {
      this.list = list;
      this.action = action;
    }

    public List<? extends T> getList() {
      return list;
    }

    public void setList(List<? extends T> list) {
      this.list = list;
    }

    public Plugin<? extends T> getAction() {
      return action;
    }

    public void setAction(Plugin<? extends T> action) {
      this.action = action;
    }

  }

  @Override
  public void runActionOnFiles(Plugin<String> action, List<Path> paths) {
    try {
      int multiplier = 0;
      logger.info("Executing beforeExecute");
      action.beforeExecute(index, model, storage);
      List<Future<Object>> futures = new ArrayList<Future<Object>>();

      List<String> block = new ArrayList<String>();
      for (Path path : paths) {
        if (block.size() == BLOCK_SIZE) {
          futures.add(Patterns.ask(actionRouter, new ActionMessage<String>(block, action),
            new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
          block = new ArrayList<String>();
          multiplier++;
        }

        block.add(path.toString());
      }

      if (!block.isEmpty()) {
        futures.add(Patterns.ask(actionRouter, new ActionMessage<String>(block, action),
          new Timeout(Duration.create(TIMEOUT, TIMEOUT_UNIT))));
        multiplier++;
      }

      final Future<Iterable<Object>> sequenceResult = Futures.sequence(futures, actionSystem.dispatcher());
      Await.result(sequenceResult, Duration.create(multiplier * TIMEOUT, TIMEOUT_UNIT));

      action.afterExecute(index, model, storage);

    } catch (Exception e) {
      // FIXME catch proper exception
      e.printStackTrace();
    }
    logger.info("End of method");

  }

}
