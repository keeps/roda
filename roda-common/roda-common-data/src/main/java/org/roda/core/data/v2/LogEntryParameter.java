/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;

/**
 * This is a parameter in a {@link LogEntry}.
 * 
 * @author Rui Castro
 */
public class LogEntryParameter implements Serializable {
  private static final long serialVersionUID = -557597047059109105L;

  private String name = null;
  private String value = null;

  /**
   * Constructs an empty {@link LogEntryParameter}.
   */
  public LogEntryParameter() {
  }

  /**
   * Constructs an empty {@link LogEntryParameter} cloning an existing
   * {@link LogEntryParameter}.
   * 
   * @param logEntryParameter
   *          the {@link LogEntryParameter} to clone.
   */
  public LogEntryParameter(LogEntryParameter logEntryParameter) {
    this(logEntryParameter.getName(), logEntryParameter.getValue());
  }

  /**
   * Constructs a new {@link LogEntryParameter} with the given arguments.
   * 
   * @param name
   *          the name of the parameter.
   * @param value
   *          the value of the parameter.
   */
  public LogEntryParameter(String name, String value) {
    setName(name);
    setValue(value);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = false;

    if (obj != null && obj instanceof LogEntryParameter) {
      LogEntryParameter other = (LogEntryParameter) obj;
      equal = getName().equals(other.getName())
        && (getValue() == other.getValue() || getValue().equals(other.getValue()));
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "LogEntryParameter (" + getName() + ", " + getValue() + ")";
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
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

}
