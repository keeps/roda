package org.roda.core.data.v2.synchronization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.common.RodaConstants;

import java.io.Serializable;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class EntitySummary implements Serializable {

  private String entityClass = null;
  private int count = 0;
  private int countAddedUpdated = 0;
  private int countRemoved = 0;
  private int countIssues = 0;

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

  public int getCountAddedUpdated() {
    return countAddedUpdated;
  }

  public void setCountAddedUpdated(int countAddedUpdated) {
    this.countAddedUpdated = countAddedUpdated;
  }

  public int getCountRemoved() {
    return countRemoved;
  }

  public void setCountRemoved(int countRemoved) {
    this.countRemoved = countRemoved;
  }

  public int getCountIssues() {
    return countIssues;
  }

  public void setCountIssues(int countIssues) {
    this.countIssues = countIssues;
  }

  /**
   * Increment the counters by type.
   * 
   * @param type
   *          {@link String}
   */
  @JsonIgnore
  public void increment(String type) {
    if (RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_REMOVED.equals(type)) {
      countRemoved++;
    } else if (RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_UPDATED.equals(type)) {
      countAddedUpdated++;
    } else if (RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_ISSUE.equals(type)) {
      countIssues++;
    }
  }

  /**
   * Sum the value given to the counter by the type.
   * 
   * @param type
   *          {@link String}
   * @param value
   *          the value
   */
  @JsonIgnore
  public void sumValue(String type, int value) {
    if (RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_REMOVED.equals(type)) {
      countRemoved += value;
    } else if (RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_UPDATED.equals(type)) {
      countAddedUpdated += value;
    } else if (RodaConstants.SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_ISSUE.equals(type)) {
      countIssues += value;
    }
  }
}
