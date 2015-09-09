package pt.gov.dgarq.roda.core.data;

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

  /**
   * Check for equality
   * 
   * @param o
   * @return true if o is an instance of {@link StatisticData} and timestamp,
   *         type and value are equal to the ones of this instance
   */
  public boolean equals(Object o) {
    boolean equal;
    if (o != null && o instanceof StatisticData) {
      StatisticData other = (StatisticData) o;
      equal = other.getTimestamp().equals(getTimestamp()) && other.getType().equals(getType())
        && other.getValue().equals(getValue());
    } else {
      equal = false;
    }
    return equal;
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
