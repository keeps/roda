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
 * A file attached to a {@link Job} ({@code job_attachments}); file names are
 * unique per job.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_attachments")
public class JobAttachment implements Serializable {
  @Serial
  private static final long serialVersionUID = -4207365480413285637L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
  private String jobId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  public JobAttachment() {
    // used by JPA and serialization
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

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
