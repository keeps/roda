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
 * A user involved in a {@link Job}, in a given role ({@code job_users}). Full
 * name and email are a snapshot taken when the job was created.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "job_users")
public class JobUser implements Serializable {
  @Serial
  private static final long serialVersionUID = 2896211375690573212L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
  private String jobId;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "role", nullable = false, length = 64)
  private String role = "";

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "email")
  private String email;

  public JobUser() {
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
