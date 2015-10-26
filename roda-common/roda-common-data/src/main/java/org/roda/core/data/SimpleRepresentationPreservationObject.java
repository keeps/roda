/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.util.Date;

import org.roda.core.data.v2.PreservationObject;
import org.roda.core.data.v2.RODAObject;

/**
 * @author Rui Castro
 */
public class SimpleRepresentationPreservationObject extends PreservationObject {
  private static final long serialVersionUID = 3107262064744644722L;

  /**
   * Preservation Object type - Representation
   */
  public static final String TYPE = "representation";

  private String representationObjectPID = null;

  /**
   * Constructs a new {@link SimpleRepresentationPreservationObject}.
   */
  public SimpleRepresentationPreservationObject() {
    setType(TYPE);
  }

  /**
   * Constructs a new {@link SimpleRepresentationPreservationObject}.
   * 
   * @param rObject
   * @param ID
   */
  public SimpleRepresentationPreservationObject(RODAObject rObject, String ID) {
    // FIXME
    // super(rObject, ID);
    setType(TYPE);
  }

  /**
   * Constructs a new {@link SimpleRepresentationPreservationObject}.
   * 
   * @param simpleRPO
   */
  public SimpleRepresentationPreservationObject(SimpleRepresentationPreservationObject simpleRPO) {
    // FIXME
    // super(simpleRPO);
    setType(TYPE);
    setRepresentationObjectPID(simpleRPO.getRepresentationObjectPID());
  }

  /**
   * Constructs a new {@link SimpleRepresentationPreservationObject} with the
   * given parameters.
   * 
   * @param pid
   * @param label
   * @param model
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param id
   */
  public SimpleRepresentationPreservationObject(String pid, String label, String model, Date lastModifiedDate,
    Date createdDate, String state, String id) {
    // FIXME
    // super(pid, label, model, lastModifiedDate, createdDate, state, id);
    setType(TYPE);
  }

  /**
   * @see PreservationObject#toString()
   */
  public String toString() {

    return "SimpleRepresentationPreservationObject( " + super.toString() //$NON-NLS-1$
      + ", representationObjectPID=" + getRepresentationObjectPID() //$NON-NLS-1$
      + " )"; //$NON-NLS-1$

  }

  /**
   * Sets the identifier of this {@link SimpleRepresentationPreservationObject}.
   * This method automatically sets the label to be the same as the ID.
   * 
   * @param ID
   */
  public void setID(String ID) {
    super.setId(ID);
    setLabel(ID);
  }

  /**
   * @return the representationObjectPID
   */
  public String getRepresentationObjectPID() {
    return representationObjectPID;
  }

  /**
   * @param representationObjectPID
   *          the representationObjectPID to set
   */
  public void setRepresentationObjectPID(String representationObjectPID) {
    this.representationObjectPID = representationObjectPID;
  }

}
