package pt.gov.dgarq.roda.core.data.adapter.filter;

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
