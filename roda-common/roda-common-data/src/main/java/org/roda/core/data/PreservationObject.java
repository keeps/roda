package org.roda.core.data;

import java.util.Date;

/**
 * This is an abstract preservation object. Each specific type of preservation
 * object has to extend this class.
 * 
 * @author Rui Castro
 */
public abstract class PreservationObject extends RODAObject {
  private static final long serialVersionUID = -1666581836175629839L;

  private String ID = null;

  /**
   * Constructs an empty {@link PreservationObject}.
   */
  public PreservationObject() {
    super();
  }

  /**
   * Constructs a {@link PreservationObject} cloning an existing
   * {@link PreservationObject}.
   * 
   * @param rObject
   * @param ID
   */
  public PreservationObject(RODAObject rObject, String ID) {
    super(rObject);
    setID(ID);
  }

  /**
   * Constructs a {@link PreservationObject} cloning an existing
   * {@link PreservationObject}.
   * 
   * @param preservationObject
   *          the {@link PreservationObject} to clone.
   */
  public PreservationObject(PreservationObject preservationObject) {
    this(preservationObject, preservationObject.getID());
  }

  /**
   * Constructs a new {@link PreservationObject} with the given parameters.
   * 
   * @param pid
   *          object PID
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
  public PreservationObject(String pid, String label, String cModel, Date lastModifiedDate, Date createdDate,
    String state, String ID) {
    super(pid, label, cModel, lastModifiedDate, createdDate, state);
    setID(ID);
  }

  /**
   * @see RODAObject#toString()
   */
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

  /**
   * Get the type of this {@link PreservationObject} from the ContentModel
   * {@link RODAObject#getContentModel()}. The type is equal to the ContentModel
   * removing the starting <strong>roda:p:</strong>.
   * 
   * @return a {@link String} with the type of this {@link PreservationObject} .
   */
  public String getType() {
    return getContentModel().split(":")[2].toLowerCase();
  }

  /**
   * @param type
   */
  public void setType(String type) {
    setContentModel("roda:p:" + type.toLowerCase());
  }

}
