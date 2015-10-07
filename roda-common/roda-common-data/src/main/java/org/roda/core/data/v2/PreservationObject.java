package org.roda.core.data.v2;

import java.util.Date;

/**
 * This is an abstract preservation object. Each specific type of preservation
 * object has to extend this class.
 * 
 * @author Rui Castro
 */
public abstract class PreservationObject {
  private static final long serialVersionUID = -1666581836175629839L;

  private String ID = null;
  private String type;
  private String fileID;

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
  public PreservationObject(String id, String label, Date lastModifiedDate, Date createdDate, String state, String ID) {
    this.id = id;
    this.label = label;
    this.lastModifiedDate = lastModifiedDate;
    this.createdDate = createdDate;
    this.state = state;
    this.ID = ID;
  }

  /**
   * @see RODAObject#toString()
   */
  @Override
  public String toString() {
    return "PreservationObject( " + super.toString() + ", type=" //$NON-NLS-1$ //$NON-NLS-2$
      + getType() + ", ID=" + getID() + " )"; //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Gets the identifier of this {@link PreservationObject}.
   * 
   * @return a {@link String}
   */
  public String getID() {
    return this.ID;
  }

  /**
   * Sets the identifier of this {@link PreservationObject}.
   * 
   * @param ID
   */
  public void setID(String ID) {
    this.ID = ID;
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

  protected String getId() {
    return id;
  }

  protected void setId(String id) {
    this.id = id;
  }

  protected String getLabel() {
    return label;
  }

  protected void setLabel(String label) {
    this.label = label;
  }

  protected Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  protected void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  protected Date getCreatedDate() {
    return createdDate;
  }

  protected void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  protected String getState() {
    return state;
  }

  protected void setState(String state) {
    this.state = state;
  }

}
