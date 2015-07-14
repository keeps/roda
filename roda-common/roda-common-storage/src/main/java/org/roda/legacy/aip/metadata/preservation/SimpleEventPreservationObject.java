package org.roda.legacy.aip.metadata.preservation;

import java.util.Date;

import org.roda.legacy.aip.metadata.RODAObject;

/**
 * This is an event preservation object
 * 
 * @author Rui Castro
 * 
 */
public class SimpleEventPreservationObject extends PreservationObject {
	private static final long serialVersionUID = 8323346811022527722L;

	/**
	 * Preservation Object type - Event
	 */
	public static final String TYPE = "event";

	private String targetID = null;
	private String agentID = null;

	/**
	 * Construct an empty {@link SimpleEventPreservationObject}.
	 */
	public SimpleEventPreservationObject() {
		setType(TYPE);
	}

	/**
	 * Constructs a {@link SimpleEventPreservationObject}.
	 * 
	 * @param rObject
	 */
	public SimpleEventPreservationObject(RODAObject rObject) {
		super(rObject, rObject.getId());
		setType(TYPE);
	}

	/**
	 * @param epo
	 */
	public SimpleEventPreservationObject(SimpleEventPreservationObject epo) {
		this(epo.getId(), epo.getLabel(), epo.getLastModifiedDate(), epo
				.getCreatedDate(), epo.getState(), epo.getID());
	}

	/**
	 * @param id
	 * @param label
	 * @param model
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 * @param ID
	 */
	public SimpleEventPreservationObject(String id, String label,
			Date lastModifiedDate, Date createdDate, String state, String ID) {
		super(id, label, lastModifiedDate, createdDate, state, ID);
		setType(TYPE);
	}

	/**
	 * @see PreservationObject#toString()
	 */
	@Override
	public String toString() {

		return "SimpleEventPreservationObject(" + super.toString()
				+ ", targetID=" + getTargetID() + ", agentID=" + getAgentID()
				+ ")";
	}

	/**
	 * @return the targetID
	 */
	public String getTargetID() {
		return targetID;
	}

	/**
	 * @param targetID
	 *            the targetID to set
	 */
	public void setTargetID(String targetID) {
		this.targetID = targetID;
	}

	/**
	 * @return the agentID
	 */
	public String getAgentID() {
		return agentID;
	}

	/**
	 * @param agentID
	 *            the agentID to set
	 */
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}

}
