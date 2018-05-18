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
public class DateRangeFilterParameter extends RangeFilterParameter<Date> {

  private static final long serialVersionUID = -8039972534809175118L;

  private DateGranularity granularity = DateGranularity.DAY;
  @SuppressWarnings("deprecation")
  private int timeZoneOffset = new Date().getTimezoneOffset();

  public DateRangeFilterParameter() {
    super();
  }

  public DateRangeFilterParameter(RangeFilterParameter<Date> rangeFilterParameter) {
    super(rangeFilterParameter);
  }

  public DateRangeFilterParameter(String name, Date fromValue, Date toValue) {
    super(name, fromValue, toValue);

    if (toValue != null) {
      this.setTimeZoneOffset(toValue.getTimezoneOffset());
    } else if (fromValue != null) {
      this.setTimeZoneOffset(fromValue.getTimezoneOffset());
    }
  }

  public DateRangeFilterParameter(String name, Date fromValue, Date toValue, DateGranularity granularity) {
    this(name, fromValue, toValue);
    this.setGranularity(granularity);
  }

  public DateRangeFilterParameter(String name, Date fromValue, Date toValue, DateGranularity granularity,
    int timeZoneOffset) {
    super(name, fromValue, toValue);
    this.setGranularity(granularity);
    this.setTimeZoneOffset(timeZoneOffset);
  }

  public DateGranularity getGranularity() {
    return granularity;
  }

  public void setGranularity(DateGranularity granularity) {
    this.granularity = granularity;
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
    DateRangeFilterParameter other = (DateRangeFilterParameter) obj;
    if (granularity != other.granularity)
      return false;
    if (timeZoneOffset != other.timeZoneOffset)
      return false;
    return true;
  }

}
