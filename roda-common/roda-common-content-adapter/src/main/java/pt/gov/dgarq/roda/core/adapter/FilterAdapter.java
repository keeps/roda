package pt.gov.dgarq.roda.core.adapter;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;

/**
 * @author Rui Castro
 * 
 * @param <EA>
 *            the type of the entity adapter for this {@link Adapter}. The
 *            entity adapter should provide methods to adapt the attributes of
 *            the entities to the needs of the {@link Adapter}.
 * @param <FPA>
 *            the {@link FilterParameter} adapter.
 */
public abstract class FilterAdapter<FPA, EA> extends Adapter<EA> {

	private Filter filter = null;

	private List<FPA> parameterAdapters = null;

	/**
	 * @param entityAdapter
	 * @param filter
	 */
	public FilterAdapter(EA entityAdapter, Filter filter) {
		super(entityAdapter);
		setEntityAdapter(entityAdapter);
	}

	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @param filter
	 *            the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * Returns whether this adaptor has a filter or not.
	 * 
	 * @return <code>true</code> if this {@link ContentAdapter} specifies one
	 *         or more filters and <code>false</code> otherwise.
	 */
	public boolean hasParameters() {
		return getFilter() != null && getFilter().getParameters() != null
				&& getFilter().getParameters().size() > 0;
	}

	/**
	 * Returns the {@link FilterParameterAdapter}s for the current
	 * {@link FilterParameter}s.
	 * 
	 * @return a {@link List} of {@link FilterParameterAdapter}s.
	 */
	public List<FPA> getParameterAdapters() {
		if (parameterAdapters == null && hasParameters()) {
			parameterAdapters = new ArrayList<FPA>();
			for (FilterParameter filterParameter : getFilter().getParameters()) {
				parameterAdapters
						.add(getFilterParameterAdapter(filterParameter));
			}
		}
		return parameterAdapters;
	}

	/**
	 * Returns the concrete {@link FilterParameterAdapter} for the given
	 * {@link FilterParameter}.
	 * 
	 * @param filterParameter
	 *            the {@link FilterParameter}.
	 * @return a {@link FilterParameterAdapter}
	 */
	abstract public FPA getFilterParameterAdapter(
			FilterParameter filterParameter);

}
