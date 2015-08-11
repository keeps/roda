package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.util.ArrayList;
import java.util.List;

public class SimpleFacetParameter extends FacetParameter {

	private static final long serialVersionUID = -5377147008170114648L;

	private List<String> values;

	public SimpleFacetParameter() {
		super();
		values = new ArrayList<String>();
	}

	public SimpleFacetParameter(String name) {
		super(name);
		values = new ArrayList<String>();
	}

	public SimpleFacetParameter(String name, List<String> values) {
		super(name);
		this.setValues(values);
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
