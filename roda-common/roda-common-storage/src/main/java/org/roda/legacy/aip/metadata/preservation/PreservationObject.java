package org.roda.legacy.aip.metadata.preservation;

import java.util.Date;

import org.roda.legacy.aip.metadata.RODAObject;

/**
 * This is an abstract preservation object. Each specific type of preservation
 * object has to extend this class.
 * 
 * @author Rui Castro
 */
public abstract class PreservationObject extends RODAObject {
	private static final long serialVersionUID = -1666581836175629839L;

	private String ID = null;
	private String type;

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
	 *            the {@link PreservationObject} to clone.
	 */
	public PreservationObject(PreservationObject preservationObject) {
		this(preservationObject, preservationObject.getID());
	}

	/**
	 * Constructs a new {@link PreservationObject} with the given parameters.
	 * 
	 * @param id
	 *            object ID
	 * @param label
	 *            object label
	 * @param cModel
	 *            object content model (<strong>must start with
	 *            roda:p:</strong>).
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param ID
	 *            the identifier of the preservation object
	 */
	public PreservationObject(String id, String label, Date lastModifiedDate,
			Date createdDate, String state, String ID) {
		super(id, label, lastModifiedDate, createdDate, state);
		setID(ID);
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

}
