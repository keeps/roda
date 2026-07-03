package org.roda.core.repository.disposal;

import jakarta.persistence.QueryHint;
import org.roda.core.entity.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface DisposalConfirmationAIPEntryRepository extends JpaRepository<DisposalConfirmationAIPEntry, String> {
  @Query("SELECT DISTINCT e.aipDisposalScheduleId FROM DisposalConfirmationAIPEntry e WHERE e.jobId = :jobId")
  List<String> findUniqueScheduleIdsByJobId(@Param("jobId") String jobId);

  @Query("SELECT DISTINCT holdId FROM DisposalConfirmationAIPEntry e JOIN e.aipDisposalHoldIds holdId WHERE e.jobId = :jobId")
  List<String> findDistinctDisposalHoldIdsByJobId(@Param("jobId") String jobId);

  @Query("SELECT DISTINCT transId FROM DisposalConfirmationAIPEntry e JOIN e.aipDisposalHoldTransitiveIds transId WHERE e.jobId = :jobId")
  List<String> findDistinctDisposalHoldTransitiveIdsByJobId(@Param("jobId") String jobId);

  @EntityGraph(attributePaths = {"aipDisposalHoldIds", "aipDisposalHoldTransitiveIds"})
  @QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "500"))
  List<DisposalConfirmationAIPEntry> findByJobId(String jobId);

  @Query("SELECT COALESCE(SUM(e.aipSize), 0) FROM DisposalConfirmationAIPEntry e WHERE e.jobId = :jobId")
  long sumAipSizeByJobId(@Param("jobId") String jobId);

  long countByJobId(String jobId);
}
