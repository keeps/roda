package org.roda.core.data.adapter.filter;

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
