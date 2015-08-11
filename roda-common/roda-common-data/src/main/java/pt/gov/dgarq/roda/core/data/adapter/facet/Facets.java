package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Facets implements Serializable {
	private static final long serialVersionUID = -1897012583120840693L;

	private Map<String, FacetParameter> parameters = new HashMap<String, FacetParameter>();
	private String query = "";
	// TODO facet.sort (count or index)? NOTE: can be defined per
	// field/parameter
	// TODO facet.limit? NOTE: can be defined per field/parameter
	// TODO facet.mincount? or always set this to 1? NOTE: can be defined per
	// field/parameter

	public Facets() {
		super();
	}

	public Facets(FacetParameter... parameters) {
		super();
		for (FacetParameter parameter : parameters) {
			this.parameters.put(parameter.getName(), parameter);
		}
	}

	public Facets(Map<String, FacetParameter> parameters) {
		super();
		this.parameters = parameters;
	}

	public Facets(Map<String, FacetParameter> parameters, String query) {
		super();
		this.parameters = parameters;
		this.query = query;
	}

	public Facets(Facets facet) {
		super();
		parameters.clear();
		parameters = facet.getParameters();
		query = facet.getQuery();
	}

	public Map<String, FacetParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, FacetParameter> parameters) {
		this.parameters = parameters;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
