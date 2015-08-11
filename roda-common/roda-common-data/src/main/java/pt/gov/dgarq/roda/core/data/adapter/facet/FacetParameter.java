package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class FacetParameter implements Serializable {
	private static final long serialVersionUID = 4927529408810091855L;

	private String name;
	private List<String> values;

	public FacetParameter() {

	}

	public FacetParameter(String name) {
		super();
		this.name = name;
		this.values = new ArrayList<String>();
	}

	public FacetParameter(String name, List<String> values) {
		super();
		this.name = name;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}
