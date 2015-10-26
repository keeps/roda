/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Rui Castro
 */
public class Task implements Serializable {
  private static final long serialVersionUID = -2502819508296827930L;

  private String name = null;
  private String description = null;

  private String username = null;

  private Date startDate = null;
  private int repeatCount = 0;
  private long repeatInterval = 0;

  private boolean scheduled = false;
  private boolean paused = false;
  private boolean running = false;

  private PluginInfo pluginInfo = null;

  /**
   * Constructs an empty {@link Task}.
   */
  public Task() {
  }

  /**
   * Constructs a new {@link Task} with the given parameters.
   * 
   * @param name
   * @param description
   * @param username
   * @param startDate
   * @param repeatCount
   * @param repeatInterval
   * @param pluginInfo
   * 
   * @deprecated use
   *             {@link #Task(String, String, String, Date, int, long, boolean, boolean, boolean, PluginInfo)}
   *             instead.
   */
  public Task(String name, String description, String username, Date startDate, int repeatCount, long repeatInterval,
    PluginInfo pluginInfo) {
    this(name, description, username, startDate, repeatCount, repeatInterval, false, false, false, pluginInfo);
  }

  /**
   * Constructs a new {@link Task} with the given parameters.
   * 
   * @param name
   * @param description
   * @param username
   * @param startDate
   * @param repeatCount
   * @param repeatInterval
   * @param scheduled
   * @param paused
   * @param running
   * @param pluginInfo
   */
  public Task(String name, String description, String username, Date startDate, int repeatCount, long repeatInterval,
    boolean scheduled, boolean paused, boolean running, PluginInfo pluginInfo) {
    setName(name);
    setDescription(description);
    setUsername(username);
    setStartDate(startDate);
    setRepeatCount(repeatCount);
    setRepeatInterval(repeatInterval);
    setScheduled(scheduled);
    setPaused(paused);
    setRunning(running);
    setPluginInfo(pluginInfo);
  }

  /**
   * Constructs a new {@link Task} cloning an existing {@link Task}.
   * 
   * @param task
   *          the {@link Task} to clone.
   */
  public Task(Task task) {
    this(task.getName(), task.getDescription(), task.getUsername(), task.getStartDate(), task.getRepeatCount(), task
      .getRepeatInterval(), task.isScheduled(), task.isRunning(), task.isPaused(), task.getPluginInfo());
  }

  /**
   * @see Object#toString()
   */
  public String toString() {

    return "Task(name=" + getName() + ", description=" + getDescription() + ", username=" + getUsername()
      + ", startDate=" + getStartDate() + ", repeatCount=" + getRepeatCount() + ", repeatInterval="
      + getRepeatInterval() + ", scheduled=" + isScheduled() + ", running=" + isRunning() + ", paused=" + isPaused()
      + ", pluginInfo=" + getPluginInfo() + ")";
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = false;

    if (obj != null && obj instanceof Task) {

      Task other = (Task) obj;

      equal = getName() == other.getName() || getName().equals(other.getName());

    } else {
      equal = false;
    }

    return equal;
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
   * @return the repeatCount
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * @param repeatCount
   *          the repeatCount to set
   */
  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }

  /**
   * @return the repeatInterval
   */
  public long getRepeatInterval() {
    return repeatInterval;
  }

  /**
   * @param repeatInterval
   *          the repeatInterval to set
   */
  public void setRepeatInterval(long repeatInterval) {
    this.repeatInterval = repeatInterval;
  }

  /**
   * @return the scheduled
   */
  public boolean isScheduled() {
    return scheduled;
  }

  /**
   * @param scheduled
   *          the scheduled to set
   */
  public void setScheduled(boolean scheduled) {
    this.scheduled = scheduled;
  }

  /**
   * @return the paused
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * @param paused
   *          the paused to set
   */
  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  /**
   * @return the running
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * @param running
   *          the running to set
   */
  public void setRunning(boolean running) {
    this.running = running;
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

}
