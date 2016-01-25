/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.util.Date;

/**
 * This is an abstract preservation object. Each specific type of preservation
 * object has to extend this class.
 * 
 * @author Rui Castro
 */
public abstract class PreservationObject {

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

  private String type;

  private String fileID;

  private String representationID;

  private String aipID;

  private String model;

  /**
   * Constructs an empty {@link PreservationObject}.
   */
  public PreservationObject() {
    super();
  }

  /**
   * Constructs a new {@link PreservationObject} with the given parameters.
   * 
   * @param id
   *          object ID
   * @param label
   *          object label
   * @param cModel
   *          object content model (<strong>must start with roda:p:</strong>).
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param ID
   *          the identifier of the preservation object
   */
  public PreservationObject(String id, String label, Date lastModifiedDate, Date createdDate, String state) {
    this.id = id;
    this.label = label;
    this.lastModifiedDate = lastModifiedDate;
    this.createdDate = createdDate;
    this.state = state;
  }

  /**
   * @see RODAObject#toString()
   */
  @Override
  public String toString() {
    return "PreservationObject( " + super.toString() + ", type=" //$NON-NLS-1$ //$NON-NLS-2$
      + getType() + ", ID=" + getId() + " )"; //$NON-NLS-1$//$NON-NLS-2$
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFileID() {
    return fileID;
  }

  public void setFileID(String fileID) {
    this.fileID = fileID;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getRepresentationID() {
    return representationID;
  }

  public void setRepresentationID(String representationID) {
    this.representationID = representationID;
  }

  public String getAipID() {
    return aipID;
  }

  public void setAipID(String aipID) {
    this.aipID = aipID;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

}
