/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.Messages.JobPartialUpdate;
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
  public <T extends IsRODAObject, T1 extends IsIndexed> void runPluginFromIndex(Object context, Class<T1> classToActOn,
    Filter filter, Plugin<T> plugin) {
    try {

      IndexResult<T1> find;
      int offset = 0;
      do {
        // XXX block size could be recommended by plugin
        find = RodaCoreFactory.getIndexService().find(classToActOn, filter, SORTER, new Sublist(offset, BLOCK_SIZE));
        offset += find.getLimit();
        // submitPlugin(find.getResults(), plugin);

      } while (find.getTotalCount() > find.getOffset() + find.getLimit());

      finishedSubmit();

    } catch (GenericException | RequestNotValidException e) {
      // TODO this exception handling should be reviewed
      LOGGER.error("Error running plugin from index", e);
    }
  }

  private <T extends IsRODAObject> void submitPlugin(List<T> list, Plugin<T> plugin) {
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
  public <T extends IsRODAObject> void runPlugin(Object context, Plugin<T> plugin) {
    // TODO Auto-generated method stub

  }

  @Override
  public void executeJob(Job job, boolean async) throws JobAlreadyStartedException {
    // TODO
  }

  @Override
  public void stopJob(Job job) {
    // TODO
  }

  @Override
  public <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo) {
    // TODO Auto-generated method stub
  }

  @Override
  public void cleanUnfinishedJobs() {
    // TODO Auto-generated method stub
  }

  @Override
  public <T extends IsRODAObject> void updateJob(Plugin<T> plugin, JobPartialUpdate partialUpdate) {
    // TODO Auto-generated method stub
  }

  @Override
  public <T extends IsRODAObject> void runPluginOnAllObjects(Object context, Plugin<T> plugin, Class<T> objectClass) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T extends IsRODAObject> void runPluginOnObjects(Object context, Plugin<T> plugin, Class<T> objectClass,
    List<String> uuids) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setJobContextInformation(String jobId, Object object) {
    // TODO Auto-generated method stub

  }

}
