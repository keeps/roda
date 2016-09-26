/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.index.filter;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class LongRangeFilterParameter extends RangeFilterParameter<Long> {
  private static final long serialVersionUID = -5658723022959992610L;

  public LongRangeFilterParameter() {
    super();
  }

  public LongRangeFilterParameter(RangeFilterParameter<Long> rangeFilterParameter) {
    super(rangeFilterParameter);
  }

  public LongRangeFilterParameter(String name, Long fromValue, Long toValue) {
    super(name, fromValue, toValue);
  }

}
