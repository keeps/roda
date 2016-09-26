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
public class StringRangeFilterParameter extends RangeFilterParameter<String> {
  private static final long serialVersionUID = 302363746955812349L;

  public StringRangeFilterParameter() {
    super();
  }

  public StringRangeFilterParameter(RangeFilterParameter<String> rangeFilterParameter) {
    super(rangeFilterParameter);
  }

  public StringRangeFilterParameter(String name, String fromValue, String toValue) {
    super(name, fromValue, toValue);
  }

}
