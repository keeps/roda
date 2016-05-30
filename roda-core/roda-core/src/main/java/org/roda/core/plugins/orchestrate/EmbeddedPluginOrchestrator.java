/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
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
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 
 * @deprecated 20160323 hsilva: it's not safe to use this orchestrator in
 *             production as it wasn't fully tested and it might have (most
 *             certainly has) strange/not expected behavior
 */
public class EmbeddedPluginOrchestrator implements PluginOrchestrator {

  private static final int BLOCK_SIZE = 100;
  private static final Sorter SORTER = null;

  private static final int TIMEOUT = 1;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.HOURS;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedPluginOrchestrator.class);

  private final IndexService index;
  private final ModelService model;
  private final StorageService storage;

  private final ExecutorService executorService;

  public EmbeddedPluginOrchestrator() {
    index = RodaCoreFactory.getIndexService();
    model = RodaCoreFactory.getModelService();
    storage = RodaCoreFactory.getStorageService();

    final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("plugin-%d").setDaemon(true).build();
    int threads = Runtime.getRuntime().availableProcessors() + 1;
    executorService = Executors.newFixedThreadPool(threads, threadFactory);
    LOGGER.debug("Running embedded plugin orchestrator on a {} thread pool", threads);

  }

  @Override
  public void setup() {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public <T extends IsIndexed> void runPluginFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> plugin) {
    try {
      plugin.beforeBlockExecute(index, model, storage);
      IndexResult<T> find;
      int offset = 0;
      do {
        // XXX block size could be recommended by plugin
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, SORTER, new Sublist(offset, BLOCK_SIZE));
        offset += find.getLimit();
        submitPlugin(find.getResults(), plugin);

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      finishedSubmit();
      plugin.afterBlockExecute(index, model, storage);

    } catch (PluginException | GenericException | RequestNotValidException e) {
      // TODO this exception handling should be reviewed
      LOGGER.error("Error running plugin from index", e);
    }
  }

  private <T extends Serializable> void submitPlugin(List<T> list, Plugin<T> plugin) {
    executorService.submit(new Runnable() {

      @Override
      public void run() {
        try {
          plugin.init();
          plugin.execute(index, model, storage, list);
          plugin.shutdown();
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
  public void runPluginOnAIPs(Plugin<AIP> plugin, List<String> ids) {
    try {
      plugin.beforeBlockExecute(index, model, storage);
      Iterator<String> iter = ids.iterator();
      String aipId;

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          submitPlugin(block, plugin);
          block = new ArrayList<AIP>();
        }

        aipId = iter.next();
        block.add(model.retrieveAIP(aipId));
      }

      if (!block.isEmpty()) {
        submitPlugin(block, plugin);
      }

      finishedSubmit();
      plugin.afterBlockExecute(index, model, storage);

    } catch (PluginException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException e) {
      // TODO review this exception handling
      LOGGER.error("Error running plugin on AIPs: " + ids, e);
    }
  }

  @Override
  public void runPluginOnAllAIPs(Plugin<AIP> plugin) {
    try {
      plugin.beforeBlockExecute(index, model, storage);
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> iter = aips.iterator();

      List<AIP> block = new ArrayList<AIP>();
      while (iter.hasNext()) {
        if (block.size() == BLOCK_SIZE) {
          submitPlugin(block, plugin);
          block = new ArrayList<AIP>();
        }

        OptionalWithCause<AIP> nextAIP = iter.next();
        if (nextAIP.isPresent()) {
          block.add(nextAIP.get());
        } else {
          LOGGER.error("Cannot process AIP", nextAIP.getCause());
        }
      }

      if (!block.isEmpty()) {
        submitPlugin(block, plugin);
      }

      aips.close();

      finishedSubmit();
      plugin.afterBlockExecute(index, model, storage);

    } catch (IOException | PluginException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      // TODO review this exception handling
      LOGGER.error("Error running plugin on all AIPs", e);
    }
  }

  @Override
  public void runPluginOnAllRepresentations(Plugin<Representation> plugin) {
    try {
      plugin.beforeBlockExecute(index, model, storage);
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();

      List<Representation> block = new ArrayList<Representation>();
      while (aipIter.hasNext()) {
        OptionalWithCause<AIP> aip = aipIter.next();
        if (aip.isPresent()) {
          for (Representation representation : aip.get().getRepresentations()) {
            if (block.size() == BLOCK_SIZE) {
              submitPlugin(block, plugin);
              block = new ArrayList<Representation>();
            }

            block.add(representation);
          }
        } else {
          LOGGER.error("Cannot process AIP", aip.getCause());
        }
      }

      if (!block.isEmpty()) {
        submitPlugin(block, plugin);
      }

      aips.close();

      finishedSubmit();
      plugin.afterBlockExecute(index, model, storage);

    } catch (IOException | PluginException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      // TODO review this exception handling
      LOGGER.error("Error running plugin on all representations", e);
    }
  }

  @Override
  public void runPluginOnAllFiles(Plugin<File> plugin) {
    try {
      plugin.beforeBlockExecute(index, model, storage);
      CloseableIterable<OptionalWithCause<AIP>> aips = model.listAIPs();
      Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();

      List<File> block = new ArrayList<File>();
      while (aipIter.hasNext()) {
        OptionalWithCause<AIP> aip = aipIter.next();
        for (Representation rep : aip.get().getRepresentations()) {
          if (aip.isPresent()) {
            boolean recursive = true;
            CloseableIterable<OptionalWithCause<File>> files = model.listFilesUnder(aip.get().getId(), rep.getId(),
              recursive);
            Iterator<OptionalWithCause<File>> fileIter = files.iterator();

            while (fileIter.hasNext()) {
              if (block.size() == BLOCK_SIZE) {
                submitPlugin(block, plugin);
                block = new ArrayList<File>();
              }

              OptionalWithCause<File> file = fileIter.next();
              if (file.isPresent()) {
                if (!file.get().isDirectory()) {
                  block.add(file.get());
                }
              } else {
                LOGGER.error("Cannot process AIP representation file", file.getCause());
              }
            }
            IOUtils.closeQuietly(files);
          } else {
            LOGGER.error("Cannot process AIP", aip.getCause());
          }
        }
      }

      if (!block.isEmpty()) {
        submitPlugin(block, plugin);
      }

      aips.close();

      finishedSubmit();
      plugin.afterBlockExecute(index, model, storage);

    } catch (IOException | PluginException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException e) {
      // TODO review this exception handling
      LOGGER.error("Error running plugin on all files", e);
    }
  }

  @Override
  public void runPluginOnTransferredResources(Plugin<TransferredResource> plugin, List<String> uuids) {
    try {
      plugin.beforeBlockExecute(index, model, storage);

      List<TransferredResource> resources = JobsHelper.getTransferredResources(index, uuids);

      List<TransferredResource> block = new ArrayList<TransferredResource>();
      for (TransferredResource resource : resources) {
        if (block.size() == BLOCK_SIZE) {
          submitPlugin(block, plugin);
          block = new ArrayList<TransferredResource>();
        }
        block.add(resource);
      }

      if (!block.isEmpty()) {
        submitPlugin(block, plugin);
      }

      finishedSubmit();
      plugin.afterBlockExecute(index, model, storage);

    } catch (PluginException e) {
      // TODO review this exception handling
      LOGGER.error("Error running plugin on transferred resources", e);
    }
  }

  @Override
  public <T extends Serializable> void runPlugin(Plugin<T> plugin) {
    // TODO Auto-generated method stub

  }

  @Override
  public void executeJob(Job job) throws JobAlreadyStartedException {
    // TODO
  }

  @Override
  public void stopJob(Job job) {
    // TODO
  }

  @Override
  public <T extends Serializable> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo) {
    // TODO Auto-generated method stub
  }

  @Override
  public <T extends Serializable> void updateJobPercentage(Plugin<T> plugin, int percentage) {
    // TODO Auto-generated method stub
  }

  @Override
  public void startJobsInTheStateCreated() {
    // TODO Auto-generated method stub
  }

  @Override
  public void cleanUnfinishedJobs() {
    // TODO Auto-generated method stub
  }

  @Override
  public void runPluginOnRepresentations(Plugin<Representation> plugin, List<String> ids) {
    // TODO Auto-generated method stub
  }

  @Override
  public void runPluginOnFiles(Plugin<File> plugin, List<String> ids) {
    // TODO Auto-generated method stub
  }

}
