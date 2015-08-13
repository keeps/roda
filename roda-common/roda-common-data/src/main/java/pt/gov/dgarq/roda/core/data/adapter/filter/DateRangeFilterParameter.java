package pt.gov.dgarq.roda.core.data.adapter.filter;

import java.util.Date;

/**
 * @author Luis Faria <lfaria@keep.pt>
 */
public class DateRangeFilterParameter extends RangeFilterParameter<Date> {

	public enum DateGranularity {
		YEAR, MONTH, DAY //, HOUR, MINUTE, SECOND, MILISECOND
	}

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

}
