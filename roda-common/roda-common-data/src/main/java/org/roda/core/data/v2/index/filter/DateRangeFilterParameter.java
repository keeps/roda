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

  public DateRangeFilterParameter() {
    super();
  }

  public DateRangeFilterParameter(RangeFilterParameter<Date> rangeFilterParameter) {
    super(rangeFilterParameter);
  }

  public DateRangeFilterParameter(String name, Date fromValue, Date toValue) {
    super(name, fromValue, toValue);
  }

  public DateRangeFilterParameter(String name, Date fromValue, Date toValue, DateGranularity granularity) {
    super(name, fromValue, toValue);
    this.setGranularity(granularity);
  }

  public DateGranularity getGranularity() {
    return granularity;
  }

  public void setGranularity(DateGranularity granularity) {
    this.granularity = granularity;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

}
