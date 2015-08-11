package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FacetFieldResult implements Serializable {

	private static final long serialVersionUID = 4007898233996477150L;

	private String field;
	private long totalCount;
	private List<FacetValue> values;

	public FacetFieldResult() {
		super();
	}

	public FacetFieldResult(String field, long totalCount) {
		super();
		this.field = field;
		this.totalCount = totalCount;
		this.values = new ArrayList<FacetValue>();
	}

	public FacetFieldResult(String field, long totalCount, List<FacetValue> values) {
		super();
		this.field = field;
		this.totalCount = totalCount;
		this.values = values;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public List<FacetValue> getValues() {
		return values;
	}

	public void setValues(List<FacetValue> values) {
		this.values = values;
	}

	public void addFacetValue(String value, long count) {
		values.add(new FacetValue(value, count));
	}

	@Override
	public String toString() {
		return "FacetFieldResult [field=" + field + ", totalCount=" + totalCount + ", values=" + values + "]";
	}

	

}
