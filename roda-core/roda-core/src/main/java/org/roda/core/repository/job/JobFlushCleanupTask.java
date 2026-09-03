/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;
import java.util.stream.Collectors;

import org.roda.core.data.v2.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * decoupled from the ingest hot path. Because the storage flush already
 * happened before a job is marked {@code flushedAt}, a job left behind by a
 * crash before this task runs is simply picked up on the next run -- no data
 * is at risk.
 *
 * @author RODA Development Team
 */
@Component
public class JobFlushCleanupTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobFlushCleanupTask.class);

  private final JobRepository jobRepository;
  private final ReportRepository reportRepository;

  @Value("${jobs.flush-cleanup.batch-size:500}")
  private int batchSize;

  public JobFlushCleanupTask(JobRepository jobRepository, ReportRepository reportRepository) {
    this.jobRepository = jobRepository;
    this.reportRepository = reportRepository;
  }

  @Scheduled(fixedDelayString = "${jobs.flush-cleanup.interval.millis:60000}")
  public void cleanFlushedJobs() {
    Pageable batch = PageRequest.of(0, batchSize);
    int totalCleaned = 0;

    List<Job> flushedJobs = jobRepository.findByFlushedAtIsNotNull(batch);
    while (!flushedJobs.isEmpty()) {
      List<String> jobIds = flushedJobs.stream().map(Job::getId).collect(Collectors.toList());

      try {
        reportRepository.deleteByJobIdIn(jobIds);
        jobRepository.deleteAllByIdInBatch(jobIds);
        totalCleaned += jobIds.size();
      } catch (Exception e) {
        LOGGER.error("Error cleaning up flushed jobs from the database", e);
        break;
      }

      if (jobIds.size() < batchSize) {
        break;
      }
      flushedJobs = jobRepository.findByFlushedAtIsNotNull(batch);
    }

    if (totalCleaned > 0) {
      LOGGER.info("Cleaned up {} flushed jobs (and their reports) from the database", totalCleaned);
    }
  }
}
