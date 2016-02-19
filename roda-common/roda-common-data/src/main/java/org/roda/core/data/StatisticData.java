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
 * An entry of the statistics
 * 
 * @author Luís Faria
 */
public class StatisticData implements Serializable {
  private static final long serialVersionUID = -4595612512736206637L;

  private Date timestamp;
  private String type;
  private String value;

  /**
   * Create new empty statistics entry
   */
  public StatisticData() {
  }

  /**
   * Create a new statistics entry
   * 
   * @param timestamp
   *          the date of the entry
   * @param type
   *          the statistics type of the entry
   * @param value
   *          the statistics value of the entry
   */
  public StatisticData(Date timestamp, String type, String value) {
    setTimestamp(timestamp);
    setType(type);
    setValue(value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    if (!(obj instanceof StatisticData)) {
      return false;
    }
    StatisticData other = (StatisticData) obj;
    if (timestamp == null) {
      if (other.timestamp != null) {
        return false;
      }
    } else if (!timestamp.equals(other.timestamp)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
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
  public String toString() {
    return "StatisticData (timestamp=" + getTimestamp() + ", type=" + getType() + ", value=" + getValue() + ")";
  }

  /**
   * Get the date of the entry
   * 
   * @return the {@link Date} of the entry
   */
  public Date getTimestamp() {
    return timestamp;
  }

  /**
   * Set the date of the entry
   * 
   * @param timestamp
   * 
   */
  public void setTimestamp(Date timestamp) {
    if (timestamp == null) {
      throw new NullPointerException("timestamp cannot be null");
    }
    this.timestamp = timestamp;
  }

  /**
   * Get the statistics type of the entry
   * 
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Set the statistics type of the entry
   * 
   * @param type
   */
  public void setType(String type) {
    if (type == null) {
      throw new NullPointerException("type cannot be null");
    }
    this.type = type;
  }

  /**
   * Get the value of the entry
   * 
   * @return a string representation of the value, often an integer
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the value of the statistics
   * 
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

}
