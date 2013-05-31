package pt.gov.dgarq.roda.core.adapter;

/**
 * @author Rui Castro
 * 
 * @param <E>
 *            the entity being compared.
 */
public interface SortParameterComparator<E> {

	/**
	 * Returns <code>true</code> or <code>false</code> if the implementor will
	 * implement the {@link #compare(Object, Object, String)} method or not.
	 * 
	 * @return <code>true</code> or <code>false</code> if the implementor will
	 *         implement the {@link #compare(Object, Object, String)} method or
	 *         not.
	 */
	public boolean canSortEntities();

	/**
	 * Compares two entities by the given attribute name.
	 * 
	 * @param e1
	 *            the first entity
	 * @param e2
	 *            the second entity
	 * @param attributeName
	 *            the name of the attribute to compare.
	 * @return &lt;0, 0 or &gt;0 if the first entity is less than, equal or
	 *         greater then the second entity.
	 */
	public int compare(E e1, E e2, String attributeName);

}
