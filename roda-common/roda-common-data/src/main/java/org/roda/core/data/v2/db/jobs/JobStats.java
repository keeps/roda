/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.db.jobs;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The progress counters of a running job ({@code job_stats}), one row per
 * {@link Job}. Kept apart from the job row because they are updated far more
 * often.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_stats")
public class JobStats implements Serializable {
  @Serial
  private static final long serialVersionUID = 6151784125950713337L;

  @Id
  @Column(name = "job_id", columnDefinition = "uuid")
  private String jobId;

  @Column(name = "completion_percentage", nullable = false)
  private int completionPercentage;

  @Column(name = "source_objects_count", nullable = false)
  private int sourceObjectsCount;

  @Column(name = "being_processed", nullable = false)
  private int beingProcessed;

  @Column(name = "waiting_to_be_processed", nullable = false)
  private int waitingToBeProcessed;

  @Column(name = "processed_success", nullable = false)
  private int processedSuccess;

  @Column(name = "processed_partial_success", nullable = false)
  private int processedPartialSuccess;

  @Column(name = "processed_failure", nullable = false)
  private int processedFailure;

  @Column(name = "processed_skipped", nullable = false)
  private int processedSkipped;

  @Column(name = "manual_intervention", nullable = false)
  private int manualIntervention;

  public JobStats() {
    // used by JPA and serialization
  }

  public JobStats(String jobId) {
    this.jobId = jobId;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public int getCompletionPercentage() {
    return completionPercentage;
  }

  public void setCompletionPercentage(int completionPercentage) {
    this.completionPercentage = completionPercentage;
  }

  public int getSourceObjectsCount() {
    return sourceObjectsCount;
  }

  public void setSourceObjectsCount(int sourceObjectsCount) {
    this.sourceObjectsCount = sourceObjectsCount;
  }

  public int getBeingProcessed() {
    return beingProcessed;
  }

  public void setBeingProcessed(int beingProcessed) {
    this.beingProcessed = beingProcessed;
  }

  public int getWaitingToBeProcessed() {
    return waitingToBeProcessed;
  }

  public void setWaitingToBeProcessed(int waitingToBeProcessed) {
    this.waitingToBeProcessed = waitingToBeProcessed;
  }

  public int getProcessedSuccess() {
    return processedSuccess;
  }

  public void setProcessedSuccess(int processedSuccess) {
    this.processedSuccess = processedSuccess;
  }

  public int getProcessedPartialSuccess() {
    return processedPartialSuccess;
  }

  public void setProcessedPartialSuccess(int processedPartialSuccess) {
    this.processedPartialSuccess = processedPartialSuccess;
  }

  public int getProcessedFailure() {
    return processedFailure;
  }

  public void setProcessedFailure(int processedFailure) {
    this.processedFailure = processedFailure;
  }

  public int getProcessedSkipped() {
    return processedSkipped;
  }

  public void setProcessedSkipped(int processedSkipped) {
    this.processedSkipped = processedSkipped;
  }

  public int getManualIntervention() {
    return manualIntervention;
  }

  public void setManualIntervention(int manualIntervention) {
    this.manualIntervention = manualIntervention;
  }
}
