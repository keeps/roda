/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import org.roda.core.data.v2.db.jobs.JobStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Progress counters of running jobs ({@code job_stats}). Used through {@link JobDatabaseService}.
 *
 * @author RODA Development Team
 */
@Repository
public interface JobStatsRepository extends JpaRepository<JobStats, String> {
}
