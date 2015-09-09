package pt.gov.dgarq.roda.core.data;

import java.util.Date;

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

  private String targetPID = null;
  private String agentPID = null;

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
    super(rObject, rObject.getPid());
    setType(TYPE);
  }

  /**
   * @param epo
   */
  public SimpleEventPreservationObject(SimpleEventPreservationObject epo) {
    this(epo.getPid(), epo.getLabel(), epo.getContentModel(), epo.getLastModifiedDate(), epo.getCreatedDate(), epo
      .getState(), epo.getID());
  }

  /**
   * @param pid
   * @param label
   * @param model
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param ID
   */
  public SimpleEventPreservationObject(String pid, String label, String model, Date lastModifiedDate, Date createdDate,
    String state, String ID) {
    super(pid, label, model, lastModifiedDate, createdDate, state, ID);
    setType(TYPE);
  }

  /**
   * @see PreservationObject#toString()
   */
  public String toString() {

    return "SimpleEventPreservationObject(" + super.toString() + ", targetPID=" + getTargetPID() + ", agentPID="
      + getAgentPID() + ")";
  }

  /**
   * @return the targetPID
   */
  public String getTargetPID() {
    return targetPID;
  }

  /**
   * @param targetPID
   *          the targetPID to set
   */
  public void setTargetPID(String targetPID) {
    this.targetPID = targetPID;
  }

  /**
   * @return the agentPID
   */
  public String getAgentPID() {
    return agentPID;
  }

  /**
   * @param agentPID
   *          the agentPID to set
   */
  public void setAgentPID(String agentPID) {
    this.agentPID = agentPID;
  }

}
