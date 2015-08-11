package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Facets implements Serializable {
	private static final long serialVersionUID = -1897012583120840693L;

	private List<FacetParameter> parameters = new ArrayList<FacetParameter>();
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
		this.parameters = new ArrayList<FacetParameter>();
		for (FacetParameter parameter : parameters) {
			this.parameters.add(parameter);
		}
	}

	public Facets(List<FacetParameter> parameters) {
		super();
		this.parameters = parameters;
	}

	public Facets(List<FacetParameter> parameters, String query) {
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

	public List<FacetParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<FacetParameter> parameters) {
		this.parameters = parameters;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
