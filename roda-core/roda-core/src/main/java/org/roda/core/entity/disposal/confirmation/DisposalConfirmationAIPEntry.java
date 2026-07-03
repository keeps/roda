package org.roda.core.entity.disposal.confirmation;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.roda.core.data.v2.disposal.confirmation.DestroyedSelectionState;
import org.roda.core.entity.converts.UUIDStringConverter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Entity
@Table(name = "disposal_confirmation_aip_entry")
public class DisposalConfirmationAIPEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Convert(converter = UUIDStringConverter.class)
  @Column(name = "job_id", nullable = false, columnDefinition = "UUID")
  private String jobId;

  @Convert(converter = UUIDStringConverter.class)
  @Column(name = "aip_id", nullable = false, columnDefinition = "UUID")
  private String aipId;

  @Column(name = "aip_title", nullable = false)
  private String aipTitle;

  @Column(name = "aip_level", nullable = false)
  private String aipLevel;

  @Convert(converter = UUIDStringConverter.class)
  @Column(name = "parent_id", columnDefinition = "UUID")
  private String parentId;

  @Column(name = "destroyed_transitive_source")
  private String destroyedTransitiveSource;

  @Enumerated(EnumType.STRING)
  @Column(name = "destroyed_selection", nullable = false)
  private DestroyedSelectionState destroyedSelection;

  @Column(name = "aip_collection")
  private String aipCollection;

  @Column(name = "aip_creation_date", nullable = false)
  private Date aipCreationDate;

  @Column(name = "aip_overdue_date", nullable = false)
  private Date aipOverdueDate;

  @Column(name = "aip_number_of_files", nullable = false)
  private long aipNumberOfFiles;

  @Column(name = "aip_size", nullable = false)
  private long aipSize;

  @Convert(converter = UUIDStringConverter.class)
  @Column(name = "aip_disposal_schedule_id", nullable = false, columnDefinition = "UUID")
  private String aipDisposalScheduleId;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "aip_entry_disposal_hold_ids", joinColumns = @JoinColumn(name = "aip_entry_id"))
  @Column(name = "hold_id")
  private Set<String> aipDisposalHoldIds;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "aip_entry_disposal_hold_trans_ids", joinColumns = @JoinColumn(name = "aip_entry_id"))
  @Column(name = "hold_transitive_id")
  private Set<String> aipDisposalHoldTransitiveIds;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public DisposalConfirmationAIPEntry() {
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

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getAipTitle() {
    return aipTitle;
  }

  public void setAipTitle(String aipTitle) {
    this.aipTitle = aipTitle;
  }

  public String getAipLevel() {
    return aipLevel;
  }

  public void setAipLevel(String aipLevel) {
    this.aipLevel = aipLevel;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getDestroyedTransitiveSource() {
    return destroyedTransitiveSource;
  }

  public void setDestroyedTransitiveSource(String destroyedTransitiveSource) {
    this.destroyedTransitiveSource = destroyedTransitiveSource;
  }

  public DestroyedSelectionState getDestroyedSelection() {
    return destroyedSelection;
  }

  public void setDestroyedSelection(DestroyedSelectionState destroyedSelection) {
    this.destroyedSelection = destroyedSelection;
  }

  public String getAipCollection() {
    return aipCollection;
  }

  public void setAipCollection(String aipCollection) {
    this.aipCollection = aipCollection;
  }

  public Date getAipCreationDate() {
    return aipCreationDate;
  }

  public void setAipCreationDate(Date aipCreationDate) {
    this.aipCreationDate = aipCreationDate;
  }

  public Date getAipOverdueDate() {
    return aipOverdueDate;
  }

  public void setAipOverdueDate(Date aipOverdueDate) {
    this.aipOverdueDate = aipOverdueDate;
  }

  public long getAipNumberOfFiles() {
    return aipNumberOfFiles;
  }

  public void setAipNumberOfFiles(long aipNumberOfFiles) {
    this.aipNumberOfFiles = aipNumberOfFiles;
  }

  public long getAipSize() {
    return aipSize;
  }

  public void setAipSize(long aipSize) {
    this.aipSize = aipSize;
  }

  public String getAipDisposalScheduleId() {
    return aipDisposalScheduleId;
  }

  public void setAipDisposalScheduleId(String aipDisposalScheduleId) {
    this.aipDisposalScheduleId = aipDisposalScheduleId;
  }

  public Set<String> getAipDisposalHoldIds() {
    return aipDisposalHoldIds;
  }

  public void setAipDisposalHoldIds(Set<String> aipDisposalHoldIds) {
    this.aipDisposalHoldIds = aipDisposalHoldIds;
  }

  public Set<String> getAipDisposalHoldTransitiveIds() {
    return aipDisposalHoldTransitiveIds;
  }

  public void setAipDisposalHoldTransitiveIds(Set<String> aipDisposalHoldTransitiveIds) {
    this.aipDisposalHoldTransitiveIds = aipDisposalHoldTransitiveIds;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
