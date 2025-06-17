package org.roda.core.entity.job;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Entity
@Table(name = "job_information")
public class JobInformationEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 7709195413378189068L;

  @Id
  private UUID jobId;

  @Column(name = "completion_percentage", nullable = false)
  private int completionPercentage = 0;

  @Column(name = "source_objects_count", nullable = false)
  private int sourceObjectsCount = 0;

  @Column(name = "source_objects_being_processed", nullable = false)
  private int sourceObjectsBeingProcessed = 0;

  @Column(name = "source_objects_waiting_to_be_processed", nullable = false)
  private int sourceObjectsWaitingToBeProcessed = 0;

  @Column(name = "source_objects_processed_with_success", nullable = false)
  private int sourceObjectsProcessedWithSuccess = 0;

  @Column(name = "source_objects_processed_with_partial_success", nullable = false)
  private int sourceObjectsProcessedWithPartialSuccess = 0;

  @Column(name = "source_objects_processed_with_failure", nullable = false)
  private int sourceObjectsProcessedWithFailure = 0;

  @Column(name = "source_objects_processed_with_skipped", nullable = false)
  private int sourceObjectsProcessedWithSkipped = 0;

  @Column(name = "outcome_objects_with_manual_intervention", nullable = false)
  private int outcomeObjectsWithManualIntervention = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public JobInformationEntity() {
    // Default constructor
  }

  @PrePersist
  public void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public UUID getJobId() {
    return jobId;
  }

  public void setJobId(UUID jobId) {
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

  public int getSourceObjectsBeingProcessed() {
    return sourceObjectsBeingProcessed;
  }

  public void setSourceObjectsBeingProcessed(int sourceObjectsBeingProcessed) {
    this.sourceObjectsBeingProcessed = sourceObjectsBeingProcessed;
  }

  public int getSourceObjectsWaitingToBeProcessed() {
    return sourceObjectsWaitingToBeProcessed;
  }

  public void setSourceObjectsWaitingToBeProcessed(int sourceObjectsWaitingToBeProcessed) {
    this.sourceObjectsWaitingToBeProcessed = sourceObjectsWaitingToBeProcessed;
  }

  public int getSourceObjectsProcessedWithSuccess() {
    return sourceObjectsProcessedWithSuccess;
  }

  public void setSourceObjectsProcessedWithSuccess(int sourceObjectsProcessedWithSuccess) {
    this.sourceObjectsProcessedWithSuccess = sourceObjectsProcessedWithSuccess;
  }

  public int getSourceObjectsProcessedWithPartialSuccess() {
    return sourceObjectsProcessedWithPartialSuccess;
  }

  public void setSourceObjectsProcessedWithPartialSuccess(int sourceObjectsProcessedWithPartialSuccess) {
    this.sourceObjectsProcessedWithPartialSuccess = sourceObjectsProcessedWithPartialSuccess;
  }

  public int getSourceObjectsProcessedWithFailure() {
    return sourceObjectsProcessedWithFailure;
  }

  public void setSourceObjectsProcessedWithFailure(int sourceObjectsProcessedWithFailure) {
    this.sourceObjectsProcessedWithFailure = sourceObjectsProcessedWithFailure;
  }

  public int getSourceObjectsProcessedWithSkipped() {
    return sourceObjectsProcessedWithSkipped;
  }

  public void setSourceObjectsProcessedWithSkipped(int sourceObjectsProcessedWithSkipped) {
    this.sourceObjectsProcessedWithSkipped = sourceObjectsProcessedWithSkipped;
  }

  public int getOutcomeObjectsWithManualIntervention() {
    return outcomeObjectsWithManualIntervention;
  }

  public void setOutcomeObjectsWithManualIntervention(int outcomeObjectsWithManualIntervention) {
    this.outcomeObjectsWithManualIntervention = outcomeObjectsWithManualIntervention;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
