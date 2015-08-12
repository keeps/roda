package pt.gov.dgarq.roda.core.data.adapter.filter;

import java.util.Date;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class DateRangeFilterParameter extends RangeFilterParameter<Date> {

	private static final long serialVersionUID = -8039972534809175118L;

	public DateRangeFilterParameter() {
		super();
	}

	public DateRangeFilterParameter(RangeFilterParameter<Date> rangeFilterParameter) {
		super(rangeFilterParameter);
	}

	public DateRangeFilterParameter(String name, Date fromValue, Date toValue) {
		super(name, fromValue, toValue);
	}

}
