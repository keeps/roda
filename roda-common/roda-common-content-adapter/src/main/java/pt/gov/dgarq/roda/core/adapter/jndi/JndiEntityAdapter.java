package pt.gov.dgarq.roda.core.adapter.jndi;

import java.util.List;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;

/**
 * @author Rui Castro
 * 
 * @param <E>
 *            the entity being adapted.
 */
public abstract class JndiEntityAdapter<E> implements
		SortParameterComparator<E> {

	/**
	 * Returns the list of attributes for the entity.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public abstract List<String> getAttributeNames();

	/**
	 * Returns the list of names of the JNDI attributes that match the
	 * attributes in {@link JndiEntityAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the JNDI attributes
	 *         supported.
	 */
	public abstract List<String> getJndiAttributeNames();

	/**
	 * Returns the JNDI value for a given attribute value.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the attribute value adapted for a JNDI search.
	 */
	public abstract String getJndiValueForAtribute(String attributeName,
			String value);

	/**
	 * Verifies if {@link JndiEntityAdapter} has the given attribute.
	 * 
	 * @param name
	 *            the name of the attribute.
	 * @return <code>true</code> the attribute exists, <code>false</code>
	 *         otherwise.
	 */
	public boolean hasAttribute(String name) {
		return getAttributeNames().contains(name.toLowerCase());
	}

	/**
	 * Returns the JNDI attribute name for a given attribute.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @return a {@link String} with the name of the attribute adapted for a
	 *         JNDI search of <code>null</code> if the attribute doesn't exist.
	 */
	public String getJndiAttributeNameForAttribute(String attributeName) {
		if (getAttributeNames().contains(attributeName.toLowerCase())) {
			return getJndiAttributeNames().get(
					getAttributeNames().indexOf(attributeName.toLowerCase()));
		} else {
			return null;
		}
	}

}
