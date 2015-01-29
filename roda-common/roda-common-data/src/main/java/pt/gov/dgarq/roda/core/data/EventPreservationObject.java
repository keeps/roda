package pt.gov.dgarq.roda.core.data;

import java.util.Date;

/**
 * This is an event preservation object
 * 
 * @author Rui Castro
 * 
 */
public class EventPreservationObject extends SimpleEventPreservationObject {
	private static final long serialVersionUID = 1555211337883930542L;

	public static final String PRESERVATION_EVENT_TYPE_INGESTION = "ingestion";
	public static final String PRESERVATION_EVENT_TYPE_FIXITY_CHECK = "fixity check";
	public static final String PRESERVATION_EVENT_TYPE_MIGRATION = "migration";
	public static final String PRESERVATION_EVENT_TYPE_DIGITALIZATION = "digitalization";
	public static final String PRESERVATION_EVENT_TYPE_NORMALIZATION = "normalization";

	public static final String PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK = "ingest task";
	public static final String PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK = "preservation task";

	public static final String PRESERVATION_EVENT_OBJECT_ROLE_TARGET = "target";

	// ID is already set in PreservationObject
	// getType() is already set in PreservationObject

	private Date datetime = null;
	private String eventType = null;
	private String eventDetail = null;

	private String outcome = null;
	private String outcomeDetailNote = null;
	private String outcomeDetailExtension = null;

	private String agentID = null;
	private String agentRole = null;

	private String[] objectIDs = null;

	/**
	 * Construct an empty {@link EventPreservationObject}.
	 */
	public EventPreservationObject() {
	}

	/**
	 * @param event
	 */
	public EventPreservationObject(EventPreservationObject event) {
		super(event);

		setID(event.getID());

		setDatetime(event.getDatetime());
		setEventType(event.getEventType());
		setEventDetail(event.getEventDetail());

		setOutcome(event.getOutcome());
		setOutcomeDetailNote(event.getOutcomeDetailNote());
		setOutcomeDetailExtension(event.getOutcomeDetailExtension());

		setAgentID(event.getAgentID());
		setAgentRole(event.getAgentRole());

		setObjectIDs(event.getObjectIDs());

		// this(event.getPid(), event.getLabel(), event.getContentModel(), event
		// .getLastModifiedDate(), event.getCreatedDate(), event
		// .getState());
	}

	/**
	 * @param pid
	 * @param label
	 * @param model
	 * @param lastModifiedDate
	 * @param createdDate
	 * @param state
	 */
	public EventPreservationObject(String pid, String label, String model,
			Date lastModifiedDate, Date createdDate, String state) {
		super(pid, label, model, lastModifiedDate, createdDate, state, pid);
	}

	/**
	 * @see PreservationObject#toString()
	 */
	public String toString() {

		int objectCount = (getObjectIDs() != null) ? getObjectIDs().length : 0;

		return "EventPreservationObject(" + super.toString() + ", datetime=" //$NON-NLS-1$ //$NON-NLS-2$
				+ getDatetime() + ", eventType=" + getEventType() //$NON-NLS-1$
				+ ", eventDetail=" + getEventDetail() + ", outcome=" //$NON-NLS-1$ //$NON-NLS-2$
				+ getOutcome() + ", agentID=" + getAgentID() + ", agentRole=" //$NON-NLS-1$ //$NON-NLS-2$
				+ getAgentRole() + ", objectIDs=" + objectCount + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the datetime
	 */
	public Date getDatetime() {
		return datetime;
	}

	/**
	 * @param datetime
	 *            the datetime to set
	 */
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @param eventType
	 *            the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
		setLabel(eventType);
	}

	/**
	 * @return the eventDetail
	 */
	public String getEventDetail() {
		return eventDetail;
	}

	/**
	 * @param eventDetail
	 *            the eventDetail to set
	 */
	public void setEventDetail(String eventDetail) {
		this.eventDetail = eventDetail;
	}

	/**
	 * @return the outcome
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * @param outcome
	 *            the outcome to set
	 */
	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	/**
	 * @return the outcomeDetailNote
	 */
	public String getOutcomeDetailNote() {
		return outcomeDetailNote;
	}

	/**
	 * @param outcomeDetailNote
	 *            the outcomeDetailNote to set
	 */
	public void setOutcomeDetailNote(String outcomeDetailNote) {
		this.outcomeDetailNote = outcomeDetailNote;
	}

	/**
	 * @return the outcomeDetailExtension
	 */
	public String getOutcomeDetailExtension() {
		return outcomeDetailExtension;
	}

	/**
	 * @param outcomeDetailExtension
	 *            the outcomeDetailExtension to set
	 */
	public void setOutcomeDetailExtension(String outcomeDetailExtension) {
		this.outcomeDetailExtension = outcomeDetailExtension;
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

	/**
	 * @return the agentRole
	 */
	public String getAgentRole() {
		return agentRole;
	}

	/**
	 * @param agentRole
	 *            the agentRole to set
	 */
	public void setAgentRole(String agentRole) {
		this.agentRole = agentRole;
	}

	/**
	 * @return the objectIDs
	 */
	public String[] getObjectIDs() {
		return objectIDs;
	}

	/**
	 * @param objectIDs
	 *            the objectIDs to set
	 */
	public void setObjectIDs(String[] objectIDs) {
		this.objectIDs = objectIDs;
	}

}
