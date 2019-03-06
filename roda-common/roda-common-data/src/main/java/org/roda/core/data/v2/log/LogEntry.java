/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.log;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.SetsUUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Rui Castro
 * 
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_LOG)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntry implements IsModelObject, IsIndexed, HasId, SetsUUID {
  private static final long serialVersionUID = -178083792639806983L;

  public enum LOG_ENTRY_STATE {
    SUCCESS, UNAUTHORIZED, FAILURE, UNKNOWN
  }

  private String uuid;
  private String id;
  private String address;
  private Date datetime;
  private String username;
  private String actionComponent;
  private String actionMethod;
  private String relatedObjectID;
  private long duration;

  private String instanceId = "";
  private long lineNumber = -1;

  private List<LogEntryParameter> parameters;

  private LOG_ENTRY_STATE state;
  
  private Map<String, Object> fields;

  /**
   * Constructs an empty {@link LogEntry}.
   */
  public LogEntry() {
    this.state = LOG_ENTRY_STATE.UNKNOWN;
  }

  /**
   * Constructs a new {@link LogEntry} cloning an existing {@link LogEntry}.
   * 
   * @param logEntry
   *          the {@link LogEntry} to clone.
   */
  public LogEntry(LogEntry logEntry) {
    this(logEntry.getUUID(), logEntry.getId(), logEntry.getAddress(), logEntry.getDatetime(), logEntry.getUsername(),
      logEntry.getActionComponent(), logEntry.getActionMethod(), logEntry.getParameters(),
      logEntry.getRelatedObjectID(), logEntry.getDuration(), logEntry.getState(), logEntry.getInstanceId(),
      logEntry.getLineNumber());
  }

  /**
   * Constructs a new {@link LogEntry} from the specified parameters.
   * 
   * @param id
   *          the unique identifier.
   * @param address
   *          the IP address.
   * @param datetime
   *          the datetime.
   * @param username
   *          the username.
   * @param actionComponent
   *          the action.
   * @param parameters
   *          the action parameters.
   * @param relatedObjectID
   *          the ID of the object related with this action.
   */
  public LogEntry(String uuid, String id, String address, Date datetime, String username, String actionComponent,
    String actionMethod, List<LogEntryParameter> parameters, String relatedObjectID, long duration,
    LOG_ENTRY_STATE state, String instanceId, long lineNumber) {

    setId(id);
    setAddress(address);
    setDatetime(datetime);
    setUsername(username);
    setActionComponent(actionComponent);
    setActionMethod(actionMethod);
    setParameters(parameters);
    setRelatedObjectID(relatedObjectID);
    setDuration(duration);
    setState(state);
    setInstanceId(instanceId);
    setLineNumber(lineNumber);
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @Override
  public String toString() {
    return "LogEntry [uuid=" + uuid + ", id=" + id + ", address=" + address + ", datetime=" + datetime + ", username="
      + username + ", actionComponent=" + actionComponent + ", actionMethod=" + actionMethod + ", relatedObjectID="
      + relatedObjectID + ", duration=" + duration + ", instanceId=" + instanceId + ", lineNumber=" + lineNumber
      + ", parameters=" + parameters + ", state=" + state + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("uuid", "id", "address", "datetime", "username", "actionComponent", "actionMethod",
      "relatedObjectID", "duration", "parameters", "state");
  }

  /**
   * @return the fields
   */
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * @param fields the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(uuid, id, address, datetime, username, actionComponent, actionMethod, relatedObjectID,
      duration, parameters, state);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((actionComponent == null) ? 0 : actionComponent.hashCode());
    result = prime * result + ((actionMethod == null) ? 0 : actionMethod.hashCode());
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((datetime == null) ? 0 : datetime.hashCode());
    result = prime * result + (int) (duration ^ (duration >>> 32));
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
    result = prime * result + (int) (lineNumber ^ (lineNumber >>> 32));
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
    result = prime * result + ((relatedObjectID == null) ? 0 : relatedObjectID.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LogEntry other = (LogEntry) obj;
    if (actionComponent == null) {
      if (other.actionComponent != null)
        return false;
    } else if (!actionComponent.equals(other.actionComponent))
      return false;
    if (actionMethod == null) {
      if (other.actionMethod != null)
        return false;
    } else if (!actionMethod.equals(other.actionMethod))
      return false;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    if (datetime == null) {
      if (other.datetime != null)
        return false;
    } else if (!datetime.equals(other.datetime))
      return false;
    if (duration != other.duration)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (instanceId == null) {
      if (other.instanceId != null)
        return false;
    } else if (!instanceId.equals(other.instanceId))
      return false;
    if (lineNumber != other.lineNumber)
      return false;
    if (parameters == null) {
      if (other.parameters != null)
        return false;
    } else if (!parameters.equals(other.parameters))
      return false;
    if (relatedObjectID == null) {
      if (other.relatedObjectID != null)
        return false;
    } else if (!relatedObjectID.equals(other.relatedObjectID))
      return false;
    if (state != other.state)
      return false;
    if (username == null) {
      if (other.username != null)
        return false;
    } else if (!username.equals(other.username))
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }

  /**
   * @return the id
   */
  @Override
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
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * @param address
   *          the address to set
   */
  public void setAddress(String address) {
    this.address = address;
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
   * @return the action
   */
  public String getActionComponent() {
    return actionComponent;
  }

  /**
   * @param action
   *          the action to set
   */
  public void setActionComponent(String action) {
    this.actionComponent = action;
  }

  public String getActionMethod() {
    return actionMethod;
  }

  public void setActionMethod(String actionMethod) {
    this.actionMethod = actionMethod;
  }

  /**
   * @return the parameters
   */
  public List<LogEntryParameter> getParameters() {
    return parameters;
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters(List<LogEntryParameter> parameters) {
    this.parameters = parameters;
  }

  /**
   * @return the relatedObjectID
   */
  public String getRelatedObjectID() {
    return relatedObjectID;
  }

  /**
   * @param relatedObjectID
   *          the ID of the related object to set
   */
  public void setRelatedObjectID(String relatedObjectID) {
    this.relatedObjectID = relatedObjectID;
  }

  /**
   * @return the duration
   */
  public long getDuration() {
    return duration;
  }

  /**
   * @param duration
   *          the duration to set
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public long getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(long lineNumber) {
    this.lineNumber = lineNumber;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public LOG_ENTRY_STATE getState() {
    return state;
  }

  public void setState(LOG_ENTRY_STATE state) {
    this.state = state;
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

}
