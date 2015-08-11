package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.io.Serializable;

public class FacetParameter implements Serializable {
	private static final long serialVersionUID = 4927529408810091855L;

	private String name;

	public FacetParameter() {

	}

	public FacetParameter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
