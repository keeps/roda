package org.roda.core.data.v2.synchronization.bundle;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class Issue implements Serializable {

  private String id = null;
  private String issue_type = null;
  private String entity_class = null;

  public Issue(final String id, final String issue_type, final String entity_class) {
    this.id = id;
    this.issue_type = issue_type;
    this.entity_class = entity_class;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getIssue_type() {
    return issue_type;
  }

  public void setIssue_type(final String issue_type) {
    this.issue_type = issue_type;
  }

  public String getEntity_class() {
    return entity_class;
  }

  public void setEntity_class(final String entity_class) {
    this.entity_class = entity_class;
  }
}
