/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import org.roda.core.data.common.IllegalOperationException;
import org.roda.core.data.v2.SIPStateTransition;

/**
 * This is the state of a SIP (Submission Information Package).
 * 
 * @author Rui Castro
 */
public class SIPState implements Serializable, Comparable<SIPState> {
  private static final long serialVersionUID = -2028521062931876576L;

  private String id = null;
  private String username = null;
  private String originalFilename = null;
  private String state = null;
  private Date datetime = null;
  private boolean processing = false;

  // FIXME replace by list
  private SIPStateTransition[] stateTransitions = null;

  private boolean complete = false;
  private float completePercentage = 0f;

  private String parentPID = null;
  private String ingestedPID = null;

  /**
   * Constructs an empty (<strong>invalid</strong>) SIP.
   */
  public SIPState() {
  }

  /**
   * Constructs a new {@link SIPState} cloning an existing one.
   * 
   * @param sip
   *          the {@link SIPState} to clone.
   */
  public SIPState(SIPState sip) {
    this(sip.getId(), sip.getUsername(), sip.getOriginalFilename(), sip.getState(), sip.getStateTransitions(),
      sip.isComplete(), sip.getCompletePercentage(), sip.getIngestedPID(), sip.getParentPID(), sip.getDatetime(),
      sip.isProcessing());
  }

  /**
   * Constructs a new {@link SIPState}.
   * 
   * @param id
   * @param username
   * @param originalFilename
   * @param state
   * @param stateTransitions
   * @param complete
   * @param completePercentage
   * @param ingestedDOPID
   * @param parentDOPID
   * @param datetime
   * @param processing
   */
  public SIPState(String id, String username, String originalFilename, String state,
    SIPStateTransition[] stateTransitions, boolean complete, float completePercentage, String ingestedDOPID,
    String parentDOPID, Date datetime, boolean processing) {

    setId(id);
    setUsername(username);
    setOriginalFilename(originalFilename);
    setState(state);
    setStateTransitions(stateTransitions);
    setComplete(complete);
    setCompletePercentage(completePercentage);
    setIngestedPID(ingestedDOPID);
    setParentPID(parentDOPID);
    setDatetime(datetime);
    setProcessing(processing);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof SIPState) {
      SIPState other = (SIPState) obj;
      return getId().equals(other.getId());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "SIPState (id=" + getId() + ", username=" + getUsername() + ", originalFilename=" + getOriginalFilename()
      + ", state=" + getState() + ", complete=" + isComplete() + ", completePercentage=" + getCompletePercentage()
      + "% " + ", ingestedPID=" + getIngestedPID() + ", parentPID=" + getParentPID() + ", datetime=" + getDatetime()
      + ", processing=" + isProcessing() + ", stateTransitions=" + Arrays.toString(getStateTransitions()) + ")";
  }

  /**
   * Compares two SIPStates
   * 
   * @param other
   * @return greater than 0 if other is lesser than this
   */
  public int compareTo(SIPState other) {
    Date myDatetime = getStateTransitions()[0].getDatetime();
    Date otherDatetime = other.getStateTransitions()[0].getDatetime();
    return -myDatetime.compareTo(otherDatetime);

  }

  /**
   * Compares two version of the same {@link SIPState}. Two {@link SIPState} are
   * the same if {@link SIPState#equals(Object)} returns <code>true</code> .
   * 
   * @param sip
   *          the other version of this {@link SIPState}.
   * 
   * @return <code>true<code> if the {@link SIPState}s differ in their states,
   *         <code>false<code> if nothing has changed in the {@link SIPState}.
   * 
   * @throws IllegalOperationException
   *           if argument {@link SIPState} is not the same as this.
   */
  public boolean hasChanges(SIPState sip) throws IllegalOperationException {
    if (this.equals(sip)) {
      return getState() == null ? sip != null : !getState().equals(sip.getState());
    } else {
      throw new IllegalOperationException("argument sip must be equal to this");
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
   *          the id to set
   */
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
   *          the username to set
   */
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
   *          the originalFilename to set
   */
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
   *          the state to set
   */
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
   *          the stateTransitions to set
   */
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
   *          the complete to set
   */
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
   *          the completePercentage to set
   */
  public void setCompletePercentage(float completePercentage) {
    this.completePercentage = completePercentage;
  }

  /**
   * @return the PID of the {@link DescriptionObject} ingested.
   */
  public String getIngestedPID() {
    return ingestedPID;
  }

  /**
   * @param ingestedDOPID
   *          the PID of the {@link DescriptionObject} ingested.
   */
  public void setIngestedPID(String ingestedDOPID) {
    this.ingestedPID = ingestedDOPID;
  }

  /**
   * @return the parentPID
   */
  public String getParentPID() {
    return parentPID;
  }

  /**
   * @param parentPID
   *          the parentPID to set
   */
  public void setParentPID(String parentPID) {
    this.parentPID = parentPID;
  }

  /**
   * @return the datetime
   */
  public Date getDatetime() {
    return datetime;
  }

  /**
   * @param datetime
   *          the datetime to set
   */
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
   *          the processing to set
   */
  public void setProcessing(boolean processing) {
    this.processing = processing;
  }

}
