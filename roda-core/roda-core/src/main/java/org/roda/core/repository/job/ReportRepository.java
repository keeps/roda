/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;

import org.roda.core.data.v2.jobs.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Repository for Report entities. Used to store job reports in the database
 * while the associated job is running, before they are flushed to file storage.
 *
 * @author RODA Development Team
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

  /**
   * Find all reports associated with a given job ID.
   *
   * @param jobId
   *          the job ID to search for
   * @return list of reports for the specified job
   */
  List<Report> findByJobId(String jobId);

  /**
   * Delete all reports associated with a given job ID.
   *
   * @param jobId
   *          the job ID whose reports should be deleted
   */
  @Transactional
  void deleteByJobId(String jobId);
}
