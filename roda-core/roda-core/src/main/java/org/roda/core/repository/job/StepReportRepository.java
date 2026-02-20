/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;

import org.roda.core.data.v2.jobs.StepReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Repository for StepReport entities. Used to store step reports in the database
 * while the associated job is running, before they are flushed to file storage.
 *
 * @author RODA Development Team
 */
@Repository
public interface StepReportRepository extends JpaRepository<StepReport, String> {

  /**
   * Find all step reports associated with a given parent report ID.
   *
   * @param parentReportId
   *          the parent report ID to search for
   * @return list of step reports for the specified parent report, ordered by step order
   */
  List<StepReport> findByParentReportIdOrderByStepOrderAsc(String parentReportId);

  /**
   * Delete all step reports associated with a given parent report ID.
   *
   * @param parentReportId
   *          the parent report ID whose step reports should be deleted
   */
  @Transactional
  void deleteByParentReportId(String parentReportId);
}
