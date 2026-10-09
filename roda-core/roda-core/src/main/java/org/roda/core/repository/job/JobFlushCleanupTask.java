/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task responsible for removing jobs (and their reports) from the
 * database once they have already been flushed to file storage.
 *
 * {@code DefaultModelService.flushJobToStorage} only writes the job/reports
 * to storage and marks the job row with {@code flushedAt} -- it deliberately
 * does not delete anything, since deleting one job at a time (one DELETE per
 * job, each its own transaction) becomes a bottleneck when many jobs finish
 * around the same time (e.g. many SIPs being ingested concurrently), each
 * competing with live ingest traffic for database connections and locks.
 *
 * This task instead removes flushed jobs in batches, on a fixed schedule,
 * decoupled from the ingest hot path, each batch with one set-based DELETE
 * per table (see {@link JobDatabaseService#deleteFlushedJobs(int)}). Because
 * the storage flush already happened before a job is marked {@code flushedAt},
 * a job left behind by a crash before this task runs is simply picked up on the
 * next run -- no data is at risk.
 *
 * @author RODA Development Team
 */
@Component
public class JobFlushCleanupTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobFlushCleanupTask.class);

  private final JobDatabaseService jobDatabaseService;

  @Value("${jobs.flush-cleanup.batch-size:500}")
  private int batchSize;

  public JobFlushCleanupTask(JobDatabaseService jobDatabaseService) {
    this.jobDatabaseService = jobDatabaseService;
  }

  @Scheduled(fixedDelayString = "${jobs.flush-cleanup.interval.millis:60000}")
  public void cleanFlushedJobs() {
    int totalCleaned = 0;
    try {
      int cleaned;
      do {
        cleaned = jobDatabaseService.deleteFlushedJobs(batchSize);
        totalCleaned += cleaned;
      } while (cleaned == batchSize);
    } catch (Exception e) {
      LOGGER.error("Error cleaning up flushed jobs from the database", e);
    }

    if (totalCleaned > 0) {
      LOGGER.info("Cleaned up {} flushed jobs (and their reports) from the database", totalCleaned);
    }
  }
}
