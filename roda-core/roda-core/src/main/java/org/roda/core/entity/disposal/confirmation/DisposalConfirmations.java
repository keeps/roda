package org.roda.core.entity.disposal.confirmation;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.roda.core.entity.converts.UUIDStringConverter;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Entity
@Table(name = "disposal_confirmations")
public class DisposalConfirmations {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Convert(converter = UUIDStringConverter.class)
  @Column(name = "job_id", nullable = false, unique = true, columnDefinition = "UUID")
  private String jobId;

  @Convert(converter = UUIDStringConverter.class)
  @Column(name = "disposal_confirmation_id", nullable = false, unique = true, columnDefinition = "UUID")
  private String disposalConfirmationId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public DisposalConfirmations() {
    // Empty constructor
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now(Clock.systemDefaultZone());
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getDisposalConfirmationId() {
    return disposalConfirmationId;
  }

  public void setDisposalConfirmationId(String disposalConfirmationId) {
    this.disposalConfirmationId = disposalConfirmationId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
