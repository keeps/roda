package pt.gov.dgarq.roda.core.adapter;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;

/**
 * This is the base class for all Content Adapter engines.
 * 
 * @author Rui Castro
 * 
 * @param <EA>
 *            the type of the entity adapter for this {@link Adapter}. The
 *            entity adapter should provide methods to adapt the attributes of
 *            the entities to the needs of the {@link Adapter}.
 * @param <E>
 *            the entity being adapted.
 */
public abstract class ContentAdapterEngine<EA extends SortParameterComparator<E>, E>
		extends Adapter<EA> {

	private ContentAdapter contentAdapter = null;

	private List<FilterParameterAdapter<EA>> mValueFilters = null;

	private SorterAdapter<EA, E> sorterAdapter = null;

	/**
	 * Constructs a {@link ContentAdapterEngine} for a given
	 * {@link ContentAdapter}.
	 * 
	 * @param entityAdapter
	 *            the entity adapter to use.
	 * @param contentAdapter
	 *            the {@link ContentAdapter} to clone.
	 */
	public ContentAdapterEngine(EA entityAdapter, ContentAdapter contentAdapter) {
		super(entityAdapter);
		setContentAdapter(contentAdapter);
	}

	/**
	 * @return the contentAdapter
	 */
	public ContentAdapter getContentAdapter() {
		return contentAdapter;
	}

	/**
	 * @param contentAdapter
	 *            the contentAdapter to set
	 */
	public void setContentAdapter(ContentAdapter contentAdapter) {
		if (contentAdapter == null) {
			this.contentAdapter = new ContentAdapter();
		} else {
			this.contentAdapter = contentAdapter;
		}

		// value filters will be determined next time they are needed.
		this.mValueFilters = null;
		this.sorterAdapter = new SorterAdapter<EA, E>(getEntityAdapter(),
				getSorter());
	}

	/**
	 * Returns whether this adaptor has a filter or not.
	 * 
	 * @return <code>true</code> if this {@link ContentAdapter} specifies one or
	 *         more filters and <code>false</code> otherwise.
	 */
	public boolean hasFilter() {
		return getFilter() != null && getFilter().getParameters() != null
				&& getFilter().getParameters().length > 0;
	}

	/**
	 * Returns whether this adaptor has a sorter or not.
	 * 
	 * @return <code>true</code> if this {@link ContentAdapter} specifies a one
	 *         or more sort parameters and <code>false</code> otherwise.
	 */
	public boolean hasSorter() {
		return getSorter() != null && getSorter().getParameters() != null
				&& getSorter().getParameters().length > 0;
	}

	/**
	 * Returns whether this adaptor has a sublist or not.
	 * 
	 * @return <code>true</code> if this {@link ContentAdapter} specifies a
	 *         sublist and <code>false</code> otherwise.
	 */
	public boolean hasSublist() {
		return getContentAdapter().getSublist() != null;
	}

	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return getContentAdapter().getFilter();
	}

	/**
	 * @return the sorter
	 */
	public Sorter getSorter() {
		return getContentAdapter().getSorter();
	}

	/**
	 * @return the sublist
	 */
	public Sublist getSublist() {
		return getContentAdapter().getSublist();
	}

	/**
	 * Gets the current {@link SorterAdapter}.
	 * 
	 * @return a {@link SorterAdapter} for the current {@link Sorter}.
	 */
	public SorterAdapter<EA, E> getSorterAdapter() {
		return this.sorterAdapter;
	}

	/**
	 * Verifies if this engine has filters that can filter values.
	 * 
	 * @return <code>true</code> if there's value filters and <code>false</code>
	 *         otherwise.
	 */
	public boolean hasValueFilters() {
		return getValueFilters().size() > 0;
	}

	/**
	 * Returns a {@link List} of {@link FilterParameterAdapter}s that can filter
	 * values.
	 * 
	 * @return a {@link List} of {@link FilterParameterAdapter}.
	 */
	public List<FilterParameterAdapter<EA>> getValueFilters() {

		if (mValueFilters == null) {

			List<FilterParameterAdapter<EA>> valueFilters = new ArrayList<FilterParameterAdapter<EA>>();

			if (hasFilter()) {
				for (FilterParameter filter : getFilter().getParameters()) {

					FilterParameterAdapter<EA> parameterAdapter = getFilterParameterAdapter(filter);

					if (parameterAdapter != null
							&& parameterAdapter.canFilterValues()) {
						valueFilters.add(parameterAdapter);
					}
				}
			}

			mValueFilters = valueFilters;
		}

		return mValueFilters;
	}

	/**
	 * Filter a list of values with the current value filters.
	 * 
	 * @param values
	 *            a {@link List} of values to filter.
	 * 
	 * @return a {@link List} with the filtered values. Only values that pass by
	 *         all filters will be part of this {@link List}.
	 */
	public List<? extends Object> filterValues(List<? extends Object> values) {

		List<FilterParameterAdapter<EA>> filters = getValueFilters();

		List<Object> filteredValues = new ArrayList<Object>();

		for (Object value : values) {
			if (filterValue(filters, value)) {
				filteredValues.add(value);
			}
		}

		return filteredValues;
	}

	/**
	 * Filter a value with the current value filters.
	 * 
	 * @param value
	 *            the value to filter.
	 * 
	 * @return <code>true</code> if the value passed the value filters and
	 *         <code>false</code> otherwise.
	 */
	public boolean filterValue(Object value) {
		return filterValue(getValueFilters(), value);
	}

	/**
	 * Returns a {@link FilterParameterAdapter} for the specified
	 * {@link FilterParameter}.
	 * 
	 * @param filterParameter
	 *            the FilterParameter.
	 * 
	 * @return a {@link FilterParameterAdapter} for the specified
	 *         {@link FilterParameter}.
	 */
	abstract public FilterParameterAdapter<EA> getFilterParameterAdapter(
			FilterParameter filterParameter);

	private boolean filterValue(List<FilterParameterAdapter<EA>> filters,
			Object value) {

		boolean match = true;

		for (FilterParameterAdapter<EA> filter : filters) {
			match &= filter.filterValue(value);
		}

		return match;
	}

	/**
	 * Filter a list of entities with the current entities filters.
	 * 
	 * @param entities
	 *            a {@link List} of entities to filter.
	 * 
	 * @return a {@link List} with the filtered entities. Only entities that
	 *         pass by all filters will be part of this {@link List}.
	 */
	public List<E> filter(List<E> entities) {

		List<FilterParameterAdapter<EA>> filters = getValueFilters();

		List<E> filteredEntities = new ArrayList<E>();

		for (E entity : entities) {
			if (filterValue(filters, entity)) {
				filteredEntities.add(entity);
			}
		}

		return filteredEntities;
	}

	/**
	 * @return <code>true</code> or <code>false</code> accordingly to
	 *         {@link SorterAdapter#canSortEntities()}.
	 */
	public boolean canSortEntities() {
		return getSorterAdapter().canSortEntities();
	}

	/**
	 * Sorts a list of entities using the current sorters.
	 * 
	 * @param entities
	 *            a {@link List} of entities to sort.
	 * 
	 * @return a {@link List} with the sorted entities.
	 */
	public List<E> sort(List<E> entities) {
		return getSorterAdapter().sort(entities);
	}

	/**
	 * Gets a sublist of the entities list acordingly to the current
	 * {@link ContentAdapter}'s {@link Sublist}.
	 * 
	 * @param entities
	 *            the list of entities.
	 * 
	 * @return a {@link List} of entities.
	 */
	public List<E> subList(List<E> entities) {

		int fromIndex = contentAdapter.getSublist().getFirstElementIndex();
		int toIndex = fromIndex
				+ contentAdapter.getSublist().getMaximumElementCount();
		if (toIndex > entities.size()) {
			toIndex = entities.size();
		}
		if (fromIndex > toIndex) {
			// TODO throw IndexOutOfBoundException() or
			// ContentAdapterException
			return new ArrayList<E>();
		} else {
			entities = entities.subList(fromIndex, toIndex);
		}

		return entities;
	}

	/**
	 * Adapt the given {@link List} of entities using the current
	 * {@link ContentAdapter}.
	 * 
	 * @param entities
	 *            the {@link List} of entities to adapt.
	 * 
	 * @return the adapted {@link List} of entities.
	 */
	public List<E> adaptEntities(List<E> entities) {
		if (hasFilter()) {
			entities = filter(entities);
		}
		if (hasSorter()) {
			entities = sort(entities);
		}
		if (hasSublist()) {
			entities = subList(entities);
		}
		return entities;
	}

}
