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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
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

  public static Duration getDefaultDuration() {
    return getDuration("", 1);
  }

  public static Timeout getJobTimeout(Job job, int blockSize) {
    return getTimeout(job.getPlugin(), job.getJobStats().getSourceObjectsCount(), blockSize);
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
    JobStats jobStats = job.getJobStats();
    jobStats.setSourceObjectsBeingProcessed(0);
    jobStats.setSourceObjectsWaitingToBeProcessed(0);
    jobStats.setSourceObjectsProcessedWithFailure(
      jobStats.getSourceObjectsCount() - jobStats.getSourceObjectsProcessedWithSuccess());
    job.setEndDate(new Date());
    return job;
  }

  public static List<TransferredResource> getTransferredResources(IndexService index, List<String> uuids) {
    List<TransferredResource> res = new ArrayList<TransferredResource>();

    try {
      res.addAll(index.retrieve(TransferredResource.class, uuids));
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Error retrieving TransferredResource", e);
    }

    return res;
  }

  public static List<AIP> getAIPs(ModelService model, IndexService index, List<String> uuids) {
    List<AIP> aipsToReturn = new ArrayList<>();

    if (!uuids.isEmpty()) {
      try {
        List<IndexedAIP> retrieve = index.retrieve(IndexedAIP.class, uuids);

        for (IndexedAIP indexedAIP : retrieve) {
          aipsToReturn.add(model.retrieveAIP(indexedAIP.getId()));
        }

      } catch (Throwable e) {
        LOGGER.error("Error while retrieving representations from index", e);
      }
    }

    return aipsToReturn;
  }

  public static List<Representation> getRepresentations(ModelService model, IndexService index, List<String> uuids) {
    List<Representation> representationsToReturn = new ArrayList<>();

    if (!uuids.isEmpty()) {
      try {
        List<IndexedRepresentation> retrieve = index.retrieve(IndexedRepresentation.class, uuids);

        for (IndexedRepresentation indexedRepresentation : retrieve) {
          representationsToReturn
            .add(model.retrieveRepresentation(indexedRepresentation.getAipId(), indexedRepresentation.getId()));
        }

      } catch (Throwable e) {
        LOGGER.error("Error while retrieving representations from index", e);
      }
    }

    return representationsToReturn;
  }

  public static List<File> getFiles(ModelService model, IndexService index, List<String> uuids) {
    List<File> filesToReturn = new ArrayList<>();

    if (!uuids.isEmpty()) {
      try {
        List<IndexedFile> retrieve = index.retrieve(IndexedFile.class, uuids);

        for (IndexedFile indexedFile : retrieve) {
          filesToReturn.add(model.retrieveFile(indexedFile.getAipId(), indexedFile.getRepresentationId(),
            indexedFile.getPath(), indexedFile.getId()));
        }

      } catch (Throwable e) {
        LOGGER.error("Error while retrieving files from index", e);
      }
    }

    return filesToReturn;
  }

  public static Class<Serializable> getSelectedClassFromString(String selectedClass) throws GenericException {
    try {
      Class<?> clazz = Class.forName(selectedClass);
      if (clazz instanceof Serializable) {
        return (Class<Serializable>) clazz;
      } else {
        throw new GenericException("Error while getting class from string");
      }
    } catch (ClassNotFoundException e) {
      throw new GenericException("Error while getting class from string");
    }
  }

  public static Class<IsIndexed> getIsIndexedSelectedClassFromString(String selectedClass) throws GenericException {
    try {
      Class<?> clazz = Class.forName(selectedClass);
      if (clazz instanceof Serializable) {
        try {
          IsIndexed.class.cast(clazz);
          return (Class<IsIndexed>) clazz;
        } catch (ClassCastException e) {
          // do nothing and let exception in the end of the method be thrown
        }
      }
    } catch (ClassNotFoundException e) {
      // do nothing and let exception in the end of the method be thrown
    }

    throw new GenericException("Error while getting class from string");
  }

}
