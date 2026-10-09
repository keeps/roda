/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;

import org.roda.core.data.v2.db.jobs.JobReportStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Plugin steps of job reports ({@code job_report_steps}). Used through {@link JobDatabaseService}.
 *
 * @author RODA Development Team
 */
@Repository
public interface JobReportStepRepository extends JpaRepository<JobReportStep, Long> {
  List<JobReportStep> findByReportPkOrderBySeq(Long reportPk);

  List<JobReportStep> findByReportPkInOrderByReportPkAscSeqAsc(List<Long> reportPks);
}
