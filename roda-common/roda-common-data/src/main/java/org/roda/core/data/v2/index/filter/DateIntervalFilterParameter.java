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

  public DateIntervalFilterParameter() {
    super();
  }

  public DateIntervalFilterParameter(String fromName, String toName, Date fromValue, Date toValue) {
    setFromName(fromName);
    setToName(toName);
    setFromValue(fromValue);
    setToValue(toValue);
  }

  public DateIntervalFilterParameter(String fromName, String toName, Date fromValue, Date toValue,
    DateGranularity granularity) {
    this(fromName, toName, fromValue, toValue);
    setGranularity(granularity);
  }

  public DateIntervalFilterParameter(DateIntervalFilterParameter dateIntervalFilterParameter) {
    this(dateIntervalFilterParameter.getFromName(), dateIntervalFilterParameter.getToName(),
      dateIntervalFilterParameter.getFromValue(), dateIntervalFilterParameter.getToValue(),
      dateIntervalFilterParameter.getGranularity());
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

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return "DateIntervalFilterParameter [getGranularity()=" + getGranularity() + ", getFromName()=" + getFromName()
      + ", getToName()=" + getToName() + ", getFromValue()=" + getFromValue() + ", getToValue()=" + getToValue() + "]";
  }

}
