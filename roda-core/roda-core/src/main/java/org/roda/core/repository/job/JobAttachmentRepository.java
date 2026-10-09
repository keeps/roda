/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;

import org.roda.core.data.v2.db.jobs.JobAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Attachments of running jobs ({@code job_attachments}). Used through {@link JobDatabaseService}.
 *
 * @author RODA Development Team
 */
@Repository
public interface JobAttachmentRepository extends JpaRepository<JobAttachment, Long> {
  List<JobAttachment> findByJobIdOrderById(String jobId);
}
