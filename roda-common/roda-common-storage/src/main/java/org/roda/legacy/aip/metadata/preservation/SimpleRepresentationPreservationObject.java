package org.roda.legacy.aip.metadata.preservation;

import java.util.Date;

import org.roda.legacy.aip.metadata.RODAObject;

/**
 * @author Rui Castro
 */
public class SimpleRepresentationPreservationObject extends PreservationObject {
	private static final long serialVersionUID = 3107262064744644722L;

	/**
	 * Preservation Object type - Representation
	 */
	public static final String TYPE = "representation";

	private String representationObjectID = null;

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
		super(rObject, ID);
		setType(TYPE);
	}

	/**
	 * Constructs a new {@link SimpleRepresentationPreservationObject}.
	 * 
	 * @param simpleRPO
	 */
	public SimpleRepresentationPreservationObject(
			SimpleRepresentationPreservationObject simpleRPO) {
		super(simpleRPO);
		setType(TYPE);
		setRepresentationObjectID(simpleRPO.getRepresentationObjectID());
	}

	/**
	 * Constructs a new {@link SimpleRepresentationPreservationObject} with the
	 * given parameters.
	 * 
	 * @param id
	 * @param label
	 * @param model
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param id
	 */
	public SimpleRepresentationPreservationObject(String id, String label,
			Date lastModifiedDate, Date createdDate, String state,
			String preservationObjectID) {
		super(id, label, lastModifiedDate, createdDate, state,
				preservationObjectID);
		setType(TYPE);
	}

	/**
	 * @see PreservationObject#toString()
	 */
	@Override
	public String toString() {

		return "SimpleRepresentationPreservationObject( " + super.toString() //$NON-NLS-1$
				+ ", representationObjectID=" + getRepresentationObjectID() //$NON-NLS-1$
				+ " )"; //$NON-NLS-1$

	}

	/**
	 * Sets the identifier of this
	 * {@link SimpleRepresentationPreservationObject}. This method automatically
	 * sets the label to be the same as the ID.
	 * 
	 * @param ID
	 */
	@Override
	public void setID(String ID) {
		super.setID(ID);
		setLabel(ID);
	}

	/**
	 * @return the representationObjectID
	 */
	public String getRepresentationObjectID() {
		return representationObjectID;
	}

	/**
	 * @param representationObjectID
	 *            the representationObjectID to set
	 */
	public void setRepresentationObjectID(String representationObjectID) {
		this.representationObjectID = representationObjectID;
	}

}
