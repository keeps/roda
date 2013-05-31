package pt.gov.dgarq.roda.core.adapter;

import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;

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
public class SortParameterAdapter<EA extends SortParameterComparator<E>, E>
		extends Adapter<EA> {

	private SortParameter sortParameter = null;

	/**
	 * Constructs a {@link SortParameterAdapter} for the given
	 * {@link SortParameter}.
	 * 
	 * @param entityAdapter
	 * @param sortParameter
	 *            the {@link SortParameter} to adapt.
	 */
	public SortParameterAdapter(EA entityAdapter, SortParameter sortParameter) {
		super(entityAdapter);
		setSortParameter(sortParameter);
	}

	/**
	 * @return the sortParameter
	 */
	public SortParameter getSortParameter() {
		return sortParameter;
	}

	/**
	 * @param sortParameter
	 *            the sortParameter to set
	 */
	public void setSortParameter(SortParameter sortParameter) {
		this.sortParameter = sortParameter;
	}

	/**
	 * @return <code>true</code> or <code>false</code> accordingly to
	 *         {@link SortParameterComparator#canSortEntities()}.
	 */
	public boolean canSortEntities() {
		return getEntityAdapter().canSortEntities();
	}

	/**
	 * Compares the two arguments according to the {@link SortParameter}.
	 * 
	 * @param e1
	 *            the first entity
	 * @param e2
	 *            the second entity
	 * 
	 * @return if {@link SortParameter#isDescending()} is <code>false</code>
	 *         then returns &lt;0, 0 or &gt;0 if the first argument is less
	 *         than, equal or greater than the second argument. If
	 *         {@link SortParameter#isDescending()} is <code>true</code> returns
	 *         &lt;0, 0 or &gt;0 if the second argument is less than, equal or
	 *         greater than the first argument
	 */
	public int compare(E e1, E e2) {

		int result = 0;

		if (!getSortParameter().isDescending()) {
			result = getEntityAdapter().compare(e1, e2,
					getSortParameter().getName());
		} else {
			result = getEntityAdapter().compare(e2, e1,
					getSortParameter().getName());
		}

		return result;
	}
}
