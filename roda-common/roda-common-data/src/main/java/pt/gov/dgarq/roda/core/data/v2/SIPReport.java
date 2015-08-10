package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import pt.gov.dgarq.roda.core.common.IllegalOperationException;

/**
 * This is the state of a SIP (Submission Information Package).
 * 
 * @author Rui Castro
 */
@XmlRootElement
public class SIPReport implements Serializable, Comparable<SIPReport> {
	private static final long serialVersionUID = -2028521062931876576L;
	
	private String id = null;
	private String username = null;
	private String originalFilename = null;
	private String state = null;
	private Date datetime = null;
	private boolean processing = false;
	private SIPStateTransition[] stateTransitions = null;
	private boolean complete = false;
	private float completePercentage = 0f;
	private String parentID = null;
	private String ingestedID = null;
	
	private String fileID = null;

	/**
	 * Constructs an empty (<strong>invalid</strong>) SIP.
	 */
	public SIPReport() {
	}

	/**
	 * Constructs a new {@link SIPReport} cloning an existing one.
	 * 
	 * @param sip
	 *            the {@link SIPReport} to clone.
	 */
	public SIPReport(SIPReport sip) {
		this(sip.getId(), sip.getUsername(), sip.getOriginalFilename(), sip
				.getState(), sip.getStateTransitions(), sip.isComplete(), sip
				.getCompletePercentage(), sip.getIngestedID(), sip
				.getParentID(), sip.getDatetime(), sip.isProcessing());
	}

	/**
	 * Constructs a new {@link SIPReport}.
	 * 
	 * @param id
	 * @param username
	 * @param originalFilename
	 * @param state
	 * @param stateTransitions
	 * @param complete
	 * @param completePercentage
	 * @param ingestedDOID
	 * @param parentDOID
	 * @param datetime 
	 * @param processing
	 */
	public SIPReport(String id, String username, String originalFilename,
			String state, SIPStateTransition[] stateTransitions,
			boolean complete, float completePercentage, String ingestedDOID,
			String parentDOID, Date datetime, boolean processing) {

		setId(id);
		setUsername(username);
		setOriginalFilename(originalFilename);
		setState(state);
		setStateTransitions(stateTransitions);
		setComplete(complete);
		setCompletePercentage(completePercentage);
		setIngestedID(ingestedDOID);
		setParentID(parentDOID);
		setDatetime(datetime);
		setProcessing(processing);
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof SIPReport) {
			SIPReport other = (SIPReport) obj;
			return getId().equals(other.getId());
		} else {
			return false;
		}
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "SIPState (id=" + getId() + ", username=" + getUsername()
				+ ", originalFilename=" + getOriginalFilename() + ", state="
				+ getState() + ", complete=" + isComplete()
				+ ", completePercentage=" + getCompletePercentage() + "% "
				+ ", ingestedID=" + getIngestedID() + ", parentID="
				+ getParentID() + ", datetime=" + getDatetime()
				+ ", processing=" + isProcessing() + ", stateTransitions="
				+ Arrays.toString(getStateTransitions()) + ")";
	}

	/**
	 * Compares two SIPStates
	 * @param other 
	 * @return greater than 0 if other is lesser than this
	 */
	public int compareTo(SIPReport other) {
		Date myDatetime = getStateTransitions()[0].getDatetime();
		Date otherDatetime = other.getStateTransitions()[0].getDatetime();
		return -myDatetime.compareTo(otherDatetime);

	}

	/**
	 * Compares two version of the same {@link SIPReport}. Two {@link SIPReport}
	 * are the same if {@link SIPReport#equals(Object)} returns <code>true</code>
	 * .
	 * 
	 * @param sip
	 *            the other version of this {@link SIPReport}.
	 * 
	 * @return
	 *         <code>true<code> if the {@link SIPReport}s differ in their states, <code>false<code> if nothing has changed in the {@link SIPReport}.
	 * 
	 * @throws IllegalOperationException
	 *             if argument {@link SIPReport} is not the same as this.
	 */
	public boolean hasChanges(SIPReport sip) throws IllegalOperationException {
		if (this.equals(sip)) {
			return getState() == null ? sip != null : !getState().equals(
					sip.getState());
		} else {
			throw new IllegalOperationException(
					"argument sip must be equal to this");
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	@XmlElement
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the originalFilename
	 */
	public String getOriginalFilename() {
		return originalFilename;
	}

	/**
	 * @param originalFilename
	 *            the originalFilename to set
	 */
	@XmlElement
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	@XmlElement
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the stateTransitions
	 */
	public SIPStateTransition[] getStateTransitions() {
		return stateTransitions;
	}

	/**
	 * @param stateTransitions
	 *            the stateTransitions to set
	 */
	@XmlElementWrapper(name = "transitions")
    @XmlElement(name = "transition")
	public void setStateTransitions(SIPStateTransition[] stateTransitions) {
		this.stateTransitions = stateTransitions;
	}

	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @param complete
	 *            the complete to set
	 */
	@XmlAttribute
	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	/**
	 * @return the completePercentage
	 */
	public float getCompletePercentage() {
		return completePercentage;
	}

	/**
	 * @param completePercentage
	 *            the completePercentage to set
	 */
	@XmlElement
	public void setCompletePercentage(float completePercentage) {
		this.completePercentage = completePercentage;
	}

	/**
	 * @return the ID of the {@link DescriptionObject} ingested.
	 */
	public String getIngestedID() {
		return ingestedID;
	}

	/**
	 * @param ingestedDOID
	 *            the ID of the {@link DescriptionObject} ingested.
	 */
	@XmlElement
	public void setIngestedID(String ingestedDOID) {
		this.ingestedID = ingestedDOID;
	}

	/**
	 * @return the parentID
	 */
	public String getParentID() {
		return parentID;
	}

	/**
	 * @param parentID
	 *            the parentID to set
	 */
	@XmlElement
	public void setParentID(String parentID) {
		this.parentID = parentID;
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
	@XmlElement
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	/**
	 * @return the processing
	 */
	public boolean isProcessing() {
		return processing;
	}

	/**
	 * @param processing
	 *            the processing to set
	 */
	@XmlAttribute
	public void setProcessing(boolean processing) {
		this.processing = processing;
	}

	public String getFileID() {
		return fileID;
	}

	@XmlElement
	public void setFileID(String fileID) {
		this.fileID = fileID;
	}
	
	

}
