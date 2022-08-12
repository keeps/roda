/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.bundle;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class Issue implements Serializable {

  private String id = null;
  private String issueType = null;
  private String entityClass = null;

  public Issue(final String id, final String issueType, final String entityClass) {
    this.id = id;
    this.issueType = issueType;
    this.entityClass = entityClass;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getIssueType() {
    return issueType;
  }

  public void setIssueType(final String issueType) {
    this.issueType = issueType;
  }

  public String getEntityClass() {
    return entityClass;
  }

  public void setEntityClass(final String entityClass) {
    this.entityClass = entityClass;
  }
}
