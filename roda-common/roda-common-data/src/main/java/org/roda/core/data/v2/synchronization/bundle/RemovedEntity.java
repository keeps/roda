package org.roda.core.data.v2.synchronization.bundle;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class RemovedEntity implements Serializable {

  private String id = null;
  private String entity_class = null;

  public RemovedEntity(String id, String entity_class) {
    this.id = id;
    this.entity_class = entity_class;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getEntity_class() {
    return entity_class;
  }

  public void setEntity_class(String entity_class) {
    this.entity_class = entity_class;
  }
}
