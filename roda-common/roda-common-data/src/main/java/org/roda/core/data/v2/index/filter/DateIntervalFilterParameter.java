/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

import java.util.Date;

import org.roda.core.data.common.RodaConstants.DateGranularity;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class DateIntervalFilterParameter extends RangeFilterParameter<Date> {

  private static final long serialVersionUID = -8039972534809175118L;

  private String toName;
  private DateGranularity granularity = DateGranularity.DAY;
  @SuppressWarnings("deprecation")
  private int timeZoneOffset = new Date().getTimezoneOffset();

  public DateIntervalFilterParameter() {
    super();
  }

  public DateIntervalFilterParameter(String fromName, String toName, Date fromValue, Date toValue) {
    setFromName(fromName);
    setToName(toName);
    setFromValue(fromValue);
    setToValue(toValue);
    setTimeZoneOffset(toValue.getTimezoneOffset());
  }

  public DateIntervalFilterParameter(String fromName, String toName, Date fromValue, Date toValue,
    DateGranularity granularity) {
    this(fromName, toName, fromValue, toValue);
    setGranularity(granularity);
  }

  public DateIntervalFilterParameter(String fromName, String toName, Date fromValue, Date toValue,
    DateGranularity granularity, int timeZoneOffset) {
    this(fromName, toName, fromValue, toValue);
    setGranularity(granularity);
    setTimeZoneOffset(timeZoneOffset);
  }

  public DateIntervalFilterParameter(DateIntervalFilterParameter dateIntervalFilterParameter) {
    this(dateIntervalFilterParameter.getFromName(), dateIntervalFilterParameter.getToName(),
      dateIntervalFilterParameter.getFromValue(), dateIntervalFilterParameter.getToValue(),
      dateIntervalFilterParameter.getGranularity(), dateIntervalFilterParameter.getTimeZoneOffset());
  }

  public DateGranularity getGranularity() {
    return granularity;
  }

  public void setGranularity(DateGranularity granularity) {
    this.granularity = granularity;
  }

  /**
   * @deprecated use {@link #getFromName()} instead
   */
  @Deprecated
  @Override
  public String getName() {
    return super.getName();
  }

  /**
   * @deprecated use {@link #setFromName(String)} instead
   * 
   */
  @Deprecated
  @Override
  public void setName(String name) {
    super.setName(name);
  }

  public String getFromName() {
    return super.getName();
  }

  public void setFromName(String fromName) {
    super.setName(fromName);
  }

  public String getToName() {
    return toName;
  }

  public void setToName(String toName) {
    this.toName = toName;
  }

  public int getTimeZoneOffset() {
    return timeZoneOffset;
  }

  public void setTimeZoneOffset(int timeZoneOffset) {
    this.timeZoneOffset = timeZoneOffset;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((granularity == null) ? 0 : granularity.hashCode());
    result = prime * result + timeZoneOffset;
    result = prime * result + ((toName == null) ? 0 : toName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    DateIntervalFilterParameter other = (DateIntervalFilterParameter) obj;
    if (granularity != other.granularity)
      return false;
    if (timeZoneOffset != other.timeZoneOffset)
      return false;
    if (toName == null) {
      if (other.toName != null)
        return false;
    } else if (!toName.equals(other.toName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DateIntervalFilterParameter [getGranularity()=" + getGranularity() + ", getFromName()=" + getFromName()
      + ", getToName()=" + getToName() + ", getFromValue()=" + getFromValue() + ", getToValue()=" + getToValue()
      + ", getTimeZoneOffset()=" + getTimeZoneOffset() + "]";
  }

}
