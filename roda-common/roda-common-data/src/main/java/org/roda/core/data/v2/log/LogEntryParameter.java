/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.log;

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
    // do nothing
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof LogEntryParameter)) {
      return false;
    }
    LogEntryParameter other = (LogEntryParameter) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  /**
   * @see Object#toString()
   */
  @Override
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
