package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.util.List;

public class SimpleFacetParameter extends FacetParameter {

	private static final long serialVersionUID = -5377147008170114648L;

	public SimpleFacetParameter() {
		super();
	}

	public SimpleFacetParameter(String name) {
		super(name);
	}

	public SimpleFacetParameter(String name, List<String> values) {
		super(name, values);
	}
}
