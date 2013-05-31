package pt.gov.dgarq.roda.core.adapter;

import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;

/**
 * @author Rui Castro
 * 
 * @param <EA>
 *            the type of the entity adapter for this {@link Adapter}. The
 *            entity adapter should provide methods to adapt the attributes of
 *            the entities to the needs of the {@link Adapter}.
 */
public abstract class FilterParameterAdapter<EA> extends Adapter<EA> {

	private FilterParameter filterParameter = null;

	/**
	 * Constructs a new {@link FilterParameterAdapter} with the given entity
	 * adapter and {@link FilterParameter}.
	 * 
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public FilterParameterAdapter(EA entityAdapter,
			FilterParameter filterParameter) {
		super(entityAdapter);
		setFilterParameter(filterParameter);
	}

	/**
	 * @return the filterParameter
	 */
	public FilterParameter getFilterParameter() {
		return filterParameter;
	}

	/**
	 * @param filterParameter
	 *            the filterParameter to set
	 */
	public void setFilterParameter(FilterParameter filterParameter) {
		if (filterParameter == null) {
			throw new NullPointerException("filterParameter cannot be null");
		}
		this.filterParameter = filterParameter;
	}

	/**
	 * Returns <code>true</code> if the method {@link #filterValue(Object)} is
	 * implemented and <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the method {@link #filterValue(Object)} is
	 *         implemented and <code>false</code> otherwise.
	 */
	abstract public boolean canFilterValues();

	/**
	 * Filter a value according to the current {@link FilterParameter}.
	 * 
	 * @param value
	 *            the value of the entity to filter.
	 * 
	 * @return <code>true</code> if the attributes match the filter and
	 *         <code>false</code> otherwise.
	 */
	abstract public boolean filterValue(Object value);

}
