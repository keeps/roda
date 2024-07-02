/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public abstract class RODAInstance implements Serializable, IsModelObject {
  private static final int VERSION = 1;
  @Serial
  private static final long serialVersionUID = -3816835903133713036L;

  private String id;
  private String name;

  private Date lastSynchronizationDate;
  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  private SynchronizingStatus status;

  private List<EntitySummary> entitySummaryList;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getLastSynchronizationDate() {
    return lastSynchronizationDate;
  }

  public void setLastSynchronizationDate(Date lastSynchronizationDate) {
    this.lastSynchronizationDate = lastSynchronizationDate;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public List<EntitySummary> getEntitySummaryList() {
    return entitySummaryList;
  }

  public void setEntitySummaryList(List<EntitySummary> entitySummaryList) {
    this.entitySummaryList = entitySummaryList;
  }

  public SynchronizingStatus getStatus() {
    return this.status;
  }

  public void setStatus(SynchronizingStatus status) {
    this.status = status;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  /**
   * Choose the {@link List} to pass to the incremet method by the given type
   *
   * @param type
   *          type of {@link List}
   * @param entityClass
   *          the entity class name.
   */
  @JsonIgnore
  public void incrementEntityCounters(final String type, final String entityClass) {
    if (checkIfExistsEntitySummary(entitySummaryList, entityClass)) {
      for (EntitySummary entitySummary : entitySummaryList) {
        if (entitySummary.getEntityClass().equals(entityClass)) {
          entitySummary.increment(type);
        }
      }
    } else {
      EntitySummary newEntitySummary = new EntitySummary();
      newEntitySummary.setEntityClass(entityClass);
      newEntitySummary.increment(type);
      entitySummaryList.add(newEntitySummary);
    }
  }

  /**
   * Uses the given {@link String} type to choose the list to sum the value.
   *
   * @param type
   *          the type
   * @param entityClass
   *          the entity class name.
   * @param value
   *          the value to sum to the list.
   */
  @JsonIgnore
  public void sumValueToEntitiesCounter(final String type, final String entityClass, final int value) {
    if (checkIfExistsEntitySummary(entitySummaryList, entityClass)) {
      for (EntitySummary entitySummary : entitySummaryList) {
        if (entitySummary.getEntityClass().equals(entityClass)) {
          entitySummary.sumValue(type, value);
        }
      }
    } else {
      EntitySummary newEntitySummary = new EntitySummary();
      newEntitySummary.setEntityClass(entityClass);
      newEntitySummary.sumValue(type, value);
      entitySummaryList.add(newEntitySummary);
    }
  }

  /**
   * Checks if exists the entity in the given {@link List}
   *
   * @param entitySummaryList
   *          {@link List}
   * @param entityClass
   *          the entity class name
   * @return true if exists, false if not exists
   */
  @JsonIgnore
  private boolean checkIfExistsEntitySummary(final List<EntitySummary> entitySummaryList, final String entityClass) {
    boolean found = false;
    for (EntitySummary entitySummary : entitySummaryList) {
      if (entitySummary.getEntityClass().equals(entityClass)) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * Clean all {@link List} (removedEntitiesSummary, updatedEntitiesSummary,
   * syncErrorsSummary).
   */
  @JsonIgnore
  public void cleanEntitySummaryList() {
    entitySummaryList = new ArrayList<>();
  }

  /**
   * Get the total of issues (Errors) in synchronization.
   *
   * @return the total of issues.
   */
  @JsonIgnore
  public int getSyncErrors() {
    int errors = 0;
    for (EntitySummary entitySummary : entitySummaryList) {
      errors += entitySummary.getCountIssues();
    }
    return errors;
  }
}
