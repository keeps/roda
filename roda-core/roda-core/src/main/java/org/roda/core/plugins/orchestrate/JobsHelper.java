/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.Date;

import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;

public final class JobsHelper {
  private JobsHelper() {

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
