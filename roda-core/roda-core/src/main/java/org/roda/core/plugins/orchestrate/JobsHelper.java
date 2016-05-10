/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
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
    if (timeout == -1) {
      // if the previous was not found, try configuration default
      key = "core.orchestrator.timeout_amount";
      timeout = RodaCoreFactory.getRodaConfiguration().getInt(key, -1);
      if (timeout == -1) {
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
    job.setObjectsProcessedWithSuccess(0);
    job.setObjectsProcessedWithFailure(job.getObjectsCount());
    job.setObjectsWaitingToBeProcessed(0);
    job.setEndDate(new Date());
    return job;
  }

  public static Job setJobCounters(Job job, JobPluginInfo jobPluginInfo) {
    job.setObjectsBeingProcessed(jobPluginInfo.getObjectsBeingProcessed());
    job.setObjectsProcessedWithSuccess(jobPluginInfo.getObjectsProcessedWithSuccess());
    job.setObjectsProcessedWithFailure(jobPluginInfo.getObjectsProcessedWithFailure());
    job.setObjectsWaitingToBeProcessed(job.getObjectsCount() - job.getObjectsBeingProcessed()
      - job.getObjectsProcessedWithFailure() - job.getObjectsProcessedWithSuccess());
    return job;
  }

}
