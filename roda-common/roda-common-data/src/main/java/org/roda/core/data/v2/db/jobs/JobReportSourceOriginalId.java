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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An original identifier of the source object of a {@link JobReport} ({@code
 * job_report_source_original_ids}).
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_report_source_original_ids")
public class JobReportSourceOriginalId implements Serializable {
  @Serial
  private static final long serialVersionUID = 7376413102838432260L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "report_pk", nullable = false)
  private Long reportPk;

  @Column(name = "original_id", nullable = false)
  private String originalId;

  public JobReportSourceOriginalId() {
    // used by JPA and serialization
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getReportPk() {
    return reportPk;
  }

  public void setReportPk(Long reportPk) {
    this.reportPk = reportPk;
  }

  public String getOriginalId() {
    return originalId;
  }

  public void setOriginalId(String originalId) {
    this.originalId = originalId;
  }
}
