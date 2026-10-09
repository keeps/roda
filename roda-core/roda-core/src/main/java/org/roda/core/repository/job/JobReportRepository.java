/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.Optional;

import org.roda.core.data.v2.db.jobs.JobReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Reports of running jobs ({@code job_reports}), keyed by their surrogate
 * {@code pk}. Used through {@link JobDatabaseService}.
 *
 * @author RODA Development Team
 */
@Repository
public interface JobReportRepository extends JpaRepository<JobReport, Long> {

  /**
   * @param id
   *          the report business id (jobId-sourceObjectId-outcomeObjectId)
   */
  @Query("from JobReport r where r.id = :id")
  Optional<JobReport> findByReportId(@Param("id") String id);
}
