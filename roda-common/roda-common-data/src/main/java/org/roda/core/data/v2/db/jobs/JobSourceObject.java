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
 * An object selected by a {@link Job} whose source selection is {@link
 * JobSourceSelection#LIST} ({@code job_source_objects}).
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_source_objects")
public class JobSourceObject implements Serializable {
  @Serial
  private static final long serialVersionUID = -1683604213718850977L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
  private String jobId;

  @Column(name = "object_id", nullable = false)
  private String objectId;

  public JobSourceObject() {
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

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }
}
