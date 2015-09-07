package org.roda.action.orchestrate.embed;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.ActionOrchestrator;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageService;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import pt.gov.dgarq.roda.common.RodaCoreFactory;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.Representation;

public class EmbeddedActionOrchestrator implements ActionOrchestrator {

  private static final int BLOCK_SIZE = 100;
  private static final Sorter SORTER = null;

  private static final int TIMEOUT = 1;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;

  private final Logger logger = Logger.getLogger(getClass());

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private final ExecutorService executorService;

  public EmbeddedActionOrchestrator() {
    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("plugin-%d").setDaemon(true).build();
    int threads = Runtime.getRuntime().availableProcessors() + 1;
    executorService = Executors.newFixedThreadPool(threads, threadFactory);
    logger.debug("Running embedded action orchestrator on a " + threads + " thread pool");

  }

  @Override
  public <T extends Serializable> void runActionFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> action) {
    try {
      action.beforeExecute(index, model, storage);
      IndexResult<T> find;
      do {
        // XXX block size could be recommended by plugin
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, SORTER, new Sublist(0, BLOCK_SIZE));
        submitAction(find.getResults(), action);

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      finishedSubmit();
      action.afterExecute(index, model, storage);

    } catch (IndexServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (PluginException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private <T extends Serializable> void submitAction(List<T> list, Plugin<T> action) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        try {
          action.init();
          action.execute(index, model, storage, list);
          action.shutdown();
        } catch (PluginException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
  }

  private boolean finishedSubmit() {
    executorService.shutdown();

    try {
      return executorService.awaitTermination(TIMEOUT, TIMEOUT_UNIT);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void runActionOnAllAIPs(Plugin<AIP> action) {
    try {
      action.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> iter = aips.iterator();

      List<AIP> block = new ArrayList<>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          submitAction(block, action);
          block = new ArrayList<>();
        }

        block.add(iter.next());
      }

      if (!block.isEmpty()) {
        submitAction(block, action);
      }

      aips.close();

      finishedSubmit();
      action.afterExecute(index, model, storage);

    } catch (ModelServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (PluginException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void runActionOnAllRepresentations(Plugin<Representation> action) {
    try {
      action.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> aipIter = aips.iterator();

      List<Representation> block = new ArrayList<>();
      while (aipIter.hasNext()) {
        AIP aip = aipIter.next();
        ClosableIterable<Representation> reps = model.listRepresentations(aip.getId());
        Iterator<Representation> repIter = reps.iterator();

        while (repIter.hasNext()) {

          if (block.size() == BLOCK_SIZE) {
            submitAction(block, action);
            block = new ArrayList<>();
          }

          block.add(repIter.next());
        }

        reps.close();
      }

      if (!block.isEmpty()) {
        submitAction(block, action);
      }

      aips.close();

      finishedSubmit();
      action.afterExecute(index, model, storage);

    } catch (ModelServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (PluginException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void runActionOnAllFiles(Plugin<File> action) {
    try {
      action.beforeExecute(index, model, storage);
      ClosableIterable<AIP> aips = model.listAIPs();
      Iterator<AIP> aipIter = aips.iterator();

      List<File> block = new ArrayList<>();
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
              submitAction(block, action);
              block = new ArrayList<>();
            }

            block.add(fileIter.next());
          }
        }

        reps.close();
      }

      if (!block.isEmpty()) {
        submitAction(block, action);
      }

      aips.close();

      finishedSubmit();
      action.afterExecute(index, model, storage);

    } catch (ModelServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (PluginException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
