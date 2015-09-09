package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;
import java.util.Date;

/**
 * This class is a holds the information about a SIP state transition.
 * 
 * @author Rui Castro
 */
public class SIPStateTransition implements Serializable {
  private static final long serialVersionUID = 9187439651182355993L;

  private String sipID = null;
  private String fromState = null;
  private String toState = null;
  private Date datetime = null;
  private String taskID = null;
  private boolean success = false;
  private String description = null;

  /**
   * Constructs a new empty (<strong>invalid</strong>)
   * {@link SIPStateTransition}.
   */
  public SIPStateTransition() {
  }

  /**
   * Constructs a new {@link SIPStateTransition} by cloning an existing
   * {@link SIPStateTransition}.
   * 
   * @param sipStateTransition
   *          the {@link SIPStateTransition} to clone.
   */
  public SIPStateTransition(SIPStateTransition sipStateTransition) {
    this(sipStateTransition.getSipID(), sipStateTransition.getFromState(), sipStateTransition.getToState(),
      sipStateTransition.getDatetime(), sipStateTransition.getTaskID(), sipStateTransition.isSuccess(),
      sipStateTransition.getDescription());
  }

  /**
   * Constructs a new {@link SIPStateTransition} with the given parameters.
   * 
   * @param sipID
   * @param fromState
   * @param toState
   * @param datetime
   * @param taskID
   * @param success
   * @param description
   */
  public SIPStateTransition(String sipID, String fromState, String toState, Date datetime, String taskID,
    boolean success, String description) {

    setSipID(sipID);
    setFromState(fromState);
    setToState(toState);
    setDatetime(datetime);
    setTaskID(taskID);
    setSuccess(success);
    setDescription(description);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof SIPStateTransition) {
      SIPStateTransition other = (SIPStateTransition) obj;
      return getSipID().equals(other.getSipID());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "SIPStateTransition (sipID=" + getSipID() + ", fromState=" + getFromState() + ", toState=" + getToState()
      + ", datetime=" + getDatetime() + ", taskID=" + getTaskID() + ", success=" + isSuccess() + ", description="
      + getDescription() + ")";
  }

  /**
   * @return the sipID
   */
  public String getSipID() {
    return sipID;
  }

  /**
   * @param sipID
   *          the sipID to set
   */
  public void setSipID(String sipID) {
    this.sipID = sipID;
  }

  /**
   * @return the fromState
   */
  public String getFromState() {
    return fromState;
  }

  /**
   * @param fromState
   *          the fromState to set
   */
  public void setFromState(String fromState) {
    this.fromState = fromState;
  }

  /**
   * @return the toState
   */
  public String getToState() {
    return toState;
  }

  /**
   * @param toState
   *          the toState to set
   */
  public void setToState(String toState) {
    this.toState = toState;
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
   * @return the taskID
   */
  public String getTaskID() {
    return taskID;
  }

  /**
   * @param taskID
   *          the taskID to set
   */
  public void setTaskID(String taskID) {
    this.taskID = taskID;
  }

  /**
   * @return the success
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * @param success
   *          the success to set
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

}
