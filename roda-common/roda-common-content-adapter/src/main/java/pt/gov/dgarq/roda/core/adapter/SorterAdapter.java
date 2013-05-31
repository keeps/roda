package pt.gov.dgarq.roda.core.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;

/**
 * @author Rui Castro
 * 
 * @param <EA>
 *            the type of the entity adapter for this {@link Adapter}. The
 *            entity adapter should provide methods to adapt the attributes of
 *            the entities to the needs of the {@link Adapter}.
 * @param <E>
 *            the entity being adapted.
 */
public class SorterAdapter<EA extends SortParameterComparator<E>, E> extends
		Adapter<EA> implements Comparator<E> {
	static final private Logger logger = Logger.getLogger(SorterAdapter.class);

	private Sorter sorter = null;

	private List<SortParameterAdapter<EA, E>> parameterAdapters = null;

	/**
	 * Constructs a {@link SorterAdapter} cloning an existing {@link Sorter}.
	 * 
	 * @param entityAdapter
	 * @param sorter
	 *            the {@link Sorter} to clone.
	 */
	public SorterAdapter(EA entityAdapter, Sorter sorter) {
		super(entityAdapter);
		setSorter(sorter);
	}

	/**
	 * @return the sorter
	 */
	public Sorter getSorter() {
		return sorter;
	}

	/**
	 * @param sorter
	 *            the sorter to set
	 */
	public void setSorter(Sorter sorter) {
		this.sorter = sorter;
	}

	/**
	 * Returns whether this adaptor has a filter or not.
	 * 
	 * @return <code>true</code> if the {@link Sorter} specifies one or more
	 *         sort parameters and <code>false</code> otherwise.
	 */
	public boolean hasParameters() {
		return getSorter() != null && getSorter().getParameters() != null
				&& getSorter().getParameters().length > 0;
	}

	/**
	 * Returns a {@link List} of {@link SortParameterAdapter}s, one for each
	 * {@link SortParameter} in the current {@link Sorter}.
	 * 
	 * @return a {@link List} of {@link SortParameterAdapter}.
	 */
	public List<SortParameterAdapter<EA, E>> getParameterAdapters() {

		if (parameterAdapters == null) {

			parameterAdapters = new ArrayList<SortParameterAdapter<EA, E>>();

			if (hasParameters()) {
				for (SortParameter sortParameter : getSorter().getParameters()) {
					parameterAdapters.add(new SortParameterAdapter<EA, E>(
							getEntityAdapter(), sortParameter));
				}
			}
		}

		return parameterAdapters;
	}

	/**
	 * @return <code>true</code> or <code>false</code> accordingly to
	 *         {@link SortParameterComparator#canSortEntities()}.
	 */
	public boolean canSortEntities() {
		return getEntityAdapter().canSortEntities();
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

		List<E> sortedEntities = new ArrayList<E>(entities);

		if (canSortEntities()) {
			Collections.sort(sortedEntities, this);
		}

		return sortedEntities;
	}

	/**
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(E e1, E e2) {
		int result = 0;
		int sorterIndex = 0;

		while (result == 0 && sorterIndex < getParameterAdapters().size()) {
			result = getParameterAdapters().get(sorterIndex).compare(e1, e2);
			sorterIndex++;
		}

		return result;
	}
}
