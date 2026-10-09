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
 * A plugin parameter of a {@link Job} ({@code job_plugin_parameters}); names
 * are unique per job.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_plugin_parameters")
public class JobPluginParameter implements Serializable {
  @Serial
  private static final long serialVersionUID = 4728121944035523615L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
  private String jobId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "value", columnDefinition = "text")
  private String value;

  public JobPluginParameter() {
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
