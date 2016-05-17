/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsFilter;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.index.IndexService;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.util.Timeout;
import scala.concurrent.duration.Duration;

public final class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final int DEFAULT_BLOCK_SIZE = 100;
  private static final int DEFAULT_TIMEOUT = 1;
  private static final String DEFAULT_TIMEOUT_TIMEUNIT = "HOURS";

  private JobsHelper() {

  }

  public static int getNumberOfPluginWorkers() {
    int defaultNumberOfWorkers = Runtime.getRuntime().availableProcessors() + 1;

    return RodaCoreFactory.getRodaConfiguration().getInt("core.orchestrator.nr_of_workers", defaultNumberOfWorkers);
  }

  public static int getBlockSize() {
    String key = "core.orchestrator.block_size";
    return RodaCoreFactory.getRodaConfiguration().getInt(key, DEFAULT_BLOCK_SIZE);
  }

  public static Duration getDuration(Class<? extends Plugin> pluginClass, int blocks) {
    return getDuration(pluginClass.getCanonicalName(), blocks);
  }

  public static Duration getDuration(String pluginClass, int blocks) {
    return Duration.create(getTimeout(pluginClass) * blocks, getTimeoutTimeUnit(pluginClass));
  }

  public static Timeout getJobTimeout(Job job, int blockSize) {
    return getTimeout(job.getPlugin(), job.getObjectsCount(), blockSize);
  }

  public static Timeout getDefaultTimeout() {
    return getTimeout("", 1, 1);
  }

  public static Timeout getPluginTimeout(Class<? extends Plugin> pluginClass) {
    return getTimeout(pluginClass.getCanonicalName(), 1, 1);
  }

  public static Timeout getPluginTimeout(Class<? extends Plugin> pluginClass, int objectsCount, int blockSize) {
    return getTimeout(pluginClass.getCanonicalName(), objectsCount, blockSize);
  }

  private static Timeout getTimeout(String pluginClass, int objectsCount, int blockSize) {
    int blocks = objectsCount == blockSize ? 1 : getBlocksCounter(objectsCount, blockSize);

    return new Timeout(Duration.create(getTimeout(pluginClass) * blocks, getTimeoutTimeUnit(pluginClass)));
  }

  private static int getBlocksCounter(int objectsCount, int blockSize) {
    int blocks = 1;
    if (objectsCount > 0) {
      blocks = (objectsCount / blockSize);
      if (objectsCount % blockSize != 0) {
        blocks += 1;
      }
    }
    return blocks;
  }

  private static int getTimeout(String pluginClass) {
    // try plugin timeout first
    String key = "core.orchestrator." + pluginClass + ".timeout_amount";
    int timeout = RodaCoreFactory.getRodaConfiguration().getInt(key, -1);
    if (timeout < 0) {
      // if the previous was not found, try configuration default
      key = "core.orchestrator.timeout_amount";
      timeout = RodaCoreFactory.getRodaConfiguration().getInt(key, -1);
      if (timeout < 0) {
        // if none of the above worked, set default value here
        timeout = DEFAULT_TIMEOUT;
      }
    }

    LOGGER.debug("Timeout for '{}': {}", pluginClass, timeout);

    return timeout;
  }

  private static TimeUnit getTimeoutTimeUnit(String pluginClass) {
    // try plugin timeout first
    String key = "core.orchestrator." + pluginClass + ".timeout_time_unit";
    String timeoutUnitString = RodaCoreFactory.getRodaConfiguration().getString(key, "");
    if ("".equals(timeoutUnitString)) {
      // if the previous was not found, try configuration default
      key = "core.orchestrator.timeout_time_unit";
      timeoutUnitString = RodaCoreFactory.getRodaConfiguration().getString(key, "");
      if ("".equals(timeoutUnitString)) {
        // if none of the above worked, set default value here
        timeoutUnitString = DEFAULT_TIMEOUT_TIMEUNIT;
      }
    }

    LOGGER.debug("TimeoutUnit for '{}': {}", pluginClass, timeoutUnitString);

    return TimeUnit.valueOf(timeoutUnitString);
  }

  public static Job updateJobInTheStateStartedOrCreated(Job job) {
    job.setState(JOB_STATE.FAILED_TO_COMPLETE);
    job.setObjectsBeingProcessed(0);
    job.setObjectsWaitingToBeProcessed(0);
    job.setObjectsProcessedWithFailure(job.getObjectsCount() - job.getObjectsProcessedWithSuccess());
    job.setEndDate(new Date());
    return job;
  }

  public static List<TransferredResource> getTransferredResourcesFromObjectIds(
    SelectedItems<TransferredResource> selectedItems) {
    List<TransferredResource> res = new ArrayList<TransferredResource>();
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<TransferredResource> list = (SelectedItemsList<TransferredResource>) selectedItems;
      for (String objectId : list.getIds()) {
        try {
          res.add(RodaCoreFactory.getIndexService().retrieve(TransferredResource.class, objectId));
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error retrieving TransferredResource", e);
        }
      }
    } else {
      LOGGER.error("Still not implemented!!!!!!!!");
    }
    return res;
  }

  public static List<String> getAIPs(SelectedItems<IndexedAIP> selectedItems) {
    List<String> res = new ArrayList<String>();
    if (selectedItems instanceof SelectedItemsList) {
      SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selectedItems;
      res.addAll(list.getIds());
    } else {
      try {
        IndexService index = RodaCoreFactory.getIndexService();
        SelectedItemsFilter<IndexedAIP> selectedItemsFilter = (SelectedItemsFilter<IndexedAIP>) selectedItems;
        long count = index.count(IndexedAIP.class, selectedItemsFilter.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedAIP> aips = index.find(IndexedAIP.class, selectedItemsFilter.getFilter(), Sorter.NONE,
            new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE)).getResults();
          for (IndexedAIP aip : aips) {
            res.add(aip.getId());
          }
        }
      } catch (Throwable e) {
        LOGGER.error("Error while retrieving AIPs from index", e);
      }
    }
    return res;
  }

  public static Pair<String, List<String>> getRepresentations(SelectedItems<IndexedRepresentation> selectedItems) {
    Pair<String, List<String>> resultPair = new Pair<String, List<String>>();
    try {

      String aipId = null;
      List<String> res = new ArrayList<String>();

      if (selectedItems instanceof SelectedItemsList) {
        SelectedItemsList<IndexedRepresentation> list = (SelectedItemsList<IndexedRepresentation>) selectedItems;

        if (!list.getIds().isEmpty()) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_UUID, list.getIds()));
          List<IndexedRepresentation> reps = RodaCoreFactory.getIndexService()
            .find(IndexedRepresentation.class, filter, Sorter.NONE, new Sublist(0, list.getIds().size())).getResults();
          for (IndexedRepresentation rep : reps) {
            res.add(rep.getId());
            aipId = rep.getAipId();
          }
        }
      } else {
        IndexService index = RodaCoreFactory.getIndexService();
        SelectedItemsFilter<IndexedRepresentation> selectedItemsFilter = (SelectedItemsFilter<IndexedRepresentation>) selectedItems;
        long count = index.count(IndexedRepresentation.class, selectedItemsFilter.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedRepresentation> reps = index.find(IndexedRepresentation.class, selectedItemsFilter.getFilter(),
            null, new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE), null).getResults();
          for (IndexedRepresentation rep : reps) {
            res.add(rep.getId());
            aipId = rep.getAipId();
          }
        }
      }

      resultPair.setFirst(aipId);
      resultPair.setSecond(res);
    } catch (Throwable e) {
      LOGGER.error("Error while retrieving Representations from index", e);
    }

    return resultPair;
  }

  public static Pair<Pair<String, String>, List<String>> getFiles(SelectedItems<IndexedFile> selectedItems) {
    Pair<Pair<String, String>, List<String>> resultPair = new Pair<Pair<String, String>, List<String>>();
    try {

      String aipId = null;
      String representationId = null;
      List<String> res = new ArrayList<String>();

      if (selectedItems instanceof SelectedItemsList) {
        SelectedItemsList<IndexedFile> list = (SelectedItemsList<IndexedFile>) selectedItems;

        if (!list.getIds().isEmpty()) {
          Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_UUID, list.getIds()));
          List<IndexedFile> files = RodaCoreFactory.getIndexService()
            .find(IndexedFile.class, filter, Sorter.NONE, new Sublist(0, list.getIds().size())).getResults();
          for (IndexedFile file : files) {
            res.add(file.getId());
            aipId = file.getAipId();
            representationId = file.getRepresentationId();
          }
        }
      } else {
        IndexService index = RodaCoreFactory.getIndexService();
        SelectedItemsFilter<IndexedFile> selectedItemsFilter = (SelectedItemsFilter<IndexedFile>) selectedItems;
        long count = index.count(IndexedFile.class, selectedItemsFilter.getFilter());
        for (int i = 0; i < count; i += RodaConstants.DEFAULT_PAGINATION_VALUE) {
          List<IndexedFile> files = index.find(IndexedFile.class, selectedItemsFilter.getFilter(), Sorter.NONE,
            new Sublist(i, RodaConstants.DEFAULT_PAGINATION_VALUE)).getResults();
          for (IndexedFile file : files) {
            res.add(file.getId());
            aipId = file.getAipId();
            representationId = file.getRepresentationId();
          }
        }
      }

      Pair<String, String> fileInfo = new Pair<String, String>(aipId, representationId);
      resultPair.setFirst(fileInfo);
      resultPair.setSecond(res);
    } catch (Throwable e) {
      LOGGER.error("Error while retrieving Representations from index", e);
    }

    return resultPair;
  }

}
