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
import java.util.Date;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.PluginState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * One plugin step of a {@link JobReport} ({@code job_report_steps}). Steps are
 * only appended while a job runs, and are ordered by {@link #getSeq()}.
 *
 * The table's primary key is {@code (report_pk, seq)}, which keeps a report's
 * steps together; since {@code seq} alone is already unique (an identity), it
 * is the entity identifier.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_report_steps")
public class JobReportStep implements Serializable {
  @Serial
  private static final long serialVersionUID = -2718843017384405215L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "seq")
  private Long seq;

  @Column(name = "report_pk", nullable = false)
  private Long reportPk;

  @Column(name = "plugin_id")
  private Integer pluginId;

  @Enumerated(EnumType.STRING)
  @Column(name = "plugin_state", nullable = false, length = 16)
  private PluginState pluginState = PluginState.RUNNING;

  @Column(name = "plugin_is_mandatory", nullable = false)
  private boolean pluginIsMandatory = true;

  @Column(name = "plugin_details", columnDefinition = "text")
  private String pluginDetails;

  @Column(name = "html_plugin_details", nullable = false)
  private boolean htmlPluginDetails;

  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_object_state", length = 32)
  private AIPState outcomeObjectState;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_created")
  private Date dateCreated;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_updated", nullable = false)
  private Date dateUpdated;

  public JobReportStep() {
    // used by JPA and serialization
  }

  public Long getSeq() {
    return seq;
  }

  public void setSeq(Long seq) {
    this.seq = seq;
  }

  public Long getReportPk() {
    return reportPk;
  }

  public void setReportPk(Long reportPk) {
    this.reportPk = reportPk;
  }

  public Integer getPluginId() {
    return pluginId;
  }

  public void setPluginId(Integer pluginId) {
    this.pluginId = pluginId;
  }

  public PluginState getPluginState() {
    return pluginState;
  }

  public void setPluginState(PluginState pluginState) {
    this.pluginState = pluginState;
  }

  public boolean isPluginIsMandatory() {
    return pluginIsMandatory;
  }

  public void setPluginIsMandatory(boolean pluginIsMandatory) {
    this.pluginIsMandatory = pluginIsMandatory;
  }

  public String getPluginDetails() {
    return pluginDetails;
  }

  public void setPluginDetails(String pluginDetails) {
    this.pluginDetails = pluginDetails;
  }

  public boolean isHtmlPluginDetails() {
    return htmlPluginDetails;
  }

  public void setHtmlPluginDetails(boolean htmlPluginDetails) {
    this.htmlPluginDetails = htmlPluginDetails;
  }

  public AIPState getOutcomeObjectState() {
    return outcomeObjectState;
  }

  public void setOutcomeObjectState(AIPState outcomeObjectState) {
    this.outcomeObjectState = outcomeObjectState;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
  }
}
