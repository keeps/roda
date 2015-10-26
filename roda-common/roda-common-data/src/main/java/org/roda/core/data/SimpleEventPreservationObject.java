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
 * This is an event preservation object
 * 
 * @author Rui Castro
 * 
 */
public class SimpleEventPreservationObject extends PreservationObject {

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
    // FIXME
    // super(rObject, rObject.getPid());
    setType(TYPE);
  }

  /**
   * @param epo
   */
  public SimpleEventPreservationObject(SimpleEventPreservationObject epo) {
    // FIXME
    // this(epo.getPid(), epo.getLabel(), epo.getContentModel(),
    // epo.getLastModifiedDate(), epo.getCreatedDate(),
    // epo.getState(), epo.getID());
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
    // FIXME
    // super(pid, label, model, lastModifiedDate, createdDate, state, ID);
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
