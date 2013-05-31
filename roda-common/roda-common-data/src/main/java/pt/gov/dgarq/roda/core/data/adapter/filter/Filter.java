package pt.gov.dgarq.roda.core.data.adapter.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is a filter of data. It's used by some service methods that deal with
 * sets or lists, to filter the elements in the set or list.
 * 
 * @author Rui Castro
 */
public class Filter implements Serializable {
	private static final long serialVersionUID = -5544859696646804386L;

	private List<FilterParameter> parameters = new ArrayList<FilterParameter>();

	/**
	 * Constructs an empty {@link Filter}.
	 */
	public Filter() {
	}

	/**
	 * Constructs a {@link Filter} cloning an existing {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link Filter} to clone.
	 */
	public Filter(Filter filter) {
		this(filter.getParameters());
	}

	/**
	 * Constructs a {@link Filter} with a single parameter.
	 * 
	 * @param parameter
	 */
	public Filter(FilterParameter parameter) {
		add(parameter);
	}

	/**
	 * Constructs a {@link Filter} with the given parameters.
	 * 
	 * @param parameters
	 */
	public Filter(FilterParameter[] parameters) {
		setParameters(parameters);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		FilterParameter[] params = getParameters();
		return "Filter ("
				+ (params != null ? Arrays.asList(params).toString() : "")
				+ ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean equal = true;

		if (obj != null && obj instanceof Filter) {
			Filter other = (Filter) obj;
			equal = parameters.equals(other.parameters);
		} else {
			equal = false;
		}

		return equal;
	}

	/**
	 * Gets the list of {@link FilterParameter}s.
	 * 
	 * @return an array of {@link FilterParameter} with this filter parameters.
	 */
	public FilterParameter[] getParameters() {
		return (FilterParameter[]) parameters
				.toArray(new FilterParameter[parameters.size()]);
	}

	/**
	 * Sets this filter's {@link FilterParameter}s.
	 * 
	 * @param parameters
	 *            an array of {@link FilterParameter} to set.
	 */
	public void setParameters(FilterParameter[] parameters) {
		this.parameters.clear();
		add(parameters);
	}

	/**
	 * Adds the given parameters.
	 * 
	 * @param parameters
	 */
	public void add(FilterParameter[] parameters) {
		if (parameters != null) {
			this.parameters.addAll(Arrays.asList(parameters));
		}
	}

	/**
	 * Adds the given parameter.
	 * 
	 * @param parameter
	 */
	public void add(FilterParameter parameter) {
		if (parameters != null) {
			this.parameters.add(parameter);
		}
	}

}
