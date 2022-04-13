package org.roda.core.data.v2.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class EntitySummary implements Serializable {

  private String entityClass = null;
  private int count = 0;

  public String getEntityClass() {
    return entityClass;
  }

  public void setEntityClass(String entityClass) {
    this.entityClass = entityClass;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @JsonIgnore
  public void increment() {
    count++;
  }
}
