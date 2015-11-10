/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import org.roda.core.data.common.InvalidStateException;

/**
 * This is a RODA Object. A RODA Object is a Fedora Object that has a ID and a
 * label.
 * 
 * @author Rui Castro
 */
public class RODAObject implements Serializable {
  private static final long serialVersionUID = 2359726657667866639L;

  /**
   * Inactive state
   */
  public static String STATE_INACTIVE = "inactive";

  /**
   * Active state
   */
  public static String STATE_ACTIVE = "active";

  /**
   * Deleted state
   */
  public static String STATE_DELETED = "deleted";

  /**
   * Possible states
   */
  public static String[] STATES = new String[] {STATE_INACTIVE, STATE_ACTIVE, STATE_DELETED};

  private String id = null;

  private String label = null;

  private Date lastModifiedDate = null;

  private Date createdDate = null;

  private String state = null;

  /**
   * Constructs an empty RODAObject.
   */
  public RODAObject() {
    setState(STATE_ACTIVE);
  }

  /**
   * Constructs a {@link RODAObject} cloning an existing {@link RODAObject}.
   * 
   * @param object
   *          the {@link RODAObject} to be cloned.
   */
  public RODAObject(RODAObject object) {
    this(object.getId(), object.getLabel(), object.getLastModifiedDate(), object.getCreatedDate(), object.getState());
  }

  /**
   * Constructs a new RODAObject.
   * 
   * @param id
   *          the ID of the fedora object.
   * @param label
   *          the label of the object.
   */
  public RODAObject(String id, String label) {
    this(id, label, null, null, STATE_ACTIVE);
  }

  /**
   * Constructs a new RODAObject.
   * 
   * @param id
   *          the ID of the Fedora object.
   * @param label
   *          the label of the object.
   * @param lastModifiedDate
   *          the date of the last modification.
   * @param createdDate
   * @param state
   */
  public RODAObject(String id, String label, Date lastModifiedDate, Date createdDate, String state) {
    setId(id);
    setLabel(label);
    setLastModifiedDate(lastModifiedDate);
    setCreatedDate(createdDate);
    setState(state);
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RODAObject) {
      RODAObject other = (RODAObject) obj;
      return getId() == other.getId() || getId().equals(other.getId());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "RODAObject (ID=" + getId() + ", label=" + getLabel() + ", lastModifiedDate=" + getLastModifiedDate()
      + ", createdDate=" + getCreatedDate() + ", state=" + getState() + ")";
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @param label
   *          the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the lastModifiedDate
   */
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  /**
   * @param lastModifiedDate
   *          the lastModifiedDate to set
   */
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  /**
   * @return the createdDate
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @param createdDate
   *          the createdDate to set
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * @return the state
   */
  public String getState() {
    return (state != null) ? state.toLowerCase() : null;
  }

  /**
   * @param state
   *          the state to set
   */
  public void setState(String state) {
    if (state == null) {
      this.state = STATE_ACTIVE;
    } else if (Arrays.asList(STATES).contains(state.toLowerCase())) {
      this.state = state.toLowerCase();
    } else {
      throw new InvalidStateException("'" + state + "' is not a valid state.");
    }
  }
}
