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

import org.roda.core.common.InvalidTaskStateException;

/**
 * This is an instance of a {@link Task}.
 * 
 * @author Rui Castro
 */
public class TaskInstance implements Serializable {
  private static final long serialVersionUID = -2273815664550806041L;

  public static final String STATE_RUNNING = "STATE_RUNNING";
  public static final String STATE_PAUSED = "STATE_PAUSED";
  public static final String STATE_STOPPED = "STATE_STOPPED";

  public static final String[] STATES = new String[] {STATE_RUNNING, STATE_PAUSED, STATE_STOPPED};

  private String id = null;

  private String name = null;
  private String description = null;

  private String username = null;

  private PluginInfo pluginInfo = null;

  private String state = null;

  private float completePercentage = 0f;

  private Date startDate = null;
  private Date finishDate = null;

  private String reportID = null;

  /**
   * Constructs an empty {@link TaskInstance}.
   */
  public TaskInstance() {
  }

  /**
   * Constructs a new {@link TaskInstance} cloning an existing
   * {@link TaskInstance}.
   * 
   * @param taskInstance
   *          the {@link TaskInstance} to clone.
   * 
   * @throws InvalidTaskStateException
   *           if the state is not a valid state.
   */
  public TaskInstance(TaskInstance taskInstance) throws InvalidTaskStateException {
    this(taskInstance.getId(), taskInstance.getName(), taskInstance.getDescription(), taskInstance.getUsername(),
      taskInstance.getPluginInfo(), taskInstance.getState(), taskInstance.getCompletePercentage(), taskInstance
        .getStartDate(), taskInstance.getFinishDate(), taskInstance.getReportID());
  }

  /**
   * Constructs a new {@link TaskInstance} with the given parameters.
   * 
   * @param id
   * @param name
   * @param description
   * @param username
   * @param pluginInfo
   * @param state
   * @param completePercentage
   * @param startDate
   * @param finishDate
   * @param reportID
   * 
   * @throws InvalidTaskStateException
   *           if the state is not a valid state.
   */
  public TaskInstance(String id, String name, String description, String username, PluginInfo pluginInfo, String state,
    float completePercentage, Date startDate, Date finishDate, String reportID) throws InvalidTaskStateException {
    setId(id);
    setName(name);
    setDescription(description);
    setUsername(username);
    setPluginInfo(pluginInfo);
    setState(state);
    setCompletePercentage(completePercentage);
    setStartDate(startDate);
    setFinishDate(finishDate);
    setReportID(reportID);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof TaskInstance) {
      TaskInstance other = (TaskInstance) obj;
      return getId() == other.getId() || getId().equals(other.getId());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {

    return "TaskInstance(" + "id=" + getId() + ", name=" + getName() + ", description=" + getDescription()
      + ", username=" + getUsername() + ", pluginInfo=" + getPluginInfo() + ", state=" + getState()
      + ", completePercentage=" + getCompletePercentage() + ", startDate=" + getStartDate() + ", finishDate="
      + getFinishDate() + ", report=" + getReportID() + ")";
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
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
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
   * @return the pluginInfo
   */
  public PluginInfo getPluginInfo() {
    return pluginInfo;
  }

  /**
   * @param pluginInfo
   *          the pluginInfo to set
   */
  public void setPluginInfo(PluginInfo pluginInfo) {
    this.pluginInfo = pluginInfo;
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
   * 
   * @throws InvalidTaskStateException
   *           if the state is not a valid state.
   */
  public void setState(String state) throws InvalidTaskStateException {
    if (Arrays.asList(STATES).contains(state)) {
      this.state = state;
    } else {
      throw new InvalidTaskStateException("state " + state + " is not a valid state");
    }
  }

  /**
   * @return the report ID
   */
  public String getReportID() {
    return reportID;
  }

  /**
   * @param reportID
   *          the ID of the report to set
   */
  public void setReportID(String reportID) {
    this.reportID = reportID;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param startDate
   *          the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * @return the finishDate
   */
  public Date getFinishDate() {
    return finishDate;
  }

  /**
   * @param finishDate
   *          the finishDate to set
   */
  public void setFinishDate(Date finishDate) {
    this.finishDate = finishDate;
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

}
