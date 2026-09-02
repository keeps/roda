/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.util.List;

import org.roda.core.data.v2.jobs.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Job entities. Used to store running jobs in the database
 * before they are flushed to file storage upon completion.
 *
 * @author RODA Development Team
 */
@Repository
public interface JobRepository extends JpaRepository<Job, String> {

  /**
   * Finds a page of jobs that have already been flushed to file storage and are
   * therefore eligible for removal from the database.
   *
   * @param pageable
   *          paging/limit information (used to bound batch size)
   * @return a batch of flushed jobs
   */
  List<Job> findByFlushedAtIsNotNull(Pageable pageable);
}
