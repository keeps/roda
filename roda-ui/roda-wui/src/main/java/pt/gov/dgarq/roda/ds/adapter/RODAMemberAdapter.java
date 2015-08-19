package pt.gov.dgarq.roda.ds.adapter;

import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.jndi.JndiEntityAdapter;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;

/**
 * @author Rui Castro
 * 
 */
public class RODAMemberAdapter<E extends RODAMember> extends
		JndiEntityAdapter<E> {

	private static final String[] attributeNames = new String[] { "active" };

	private static final String[] jndiAttributeNames = new String[] { "shadowInactive" };

	/**
	 * @see SortParameterComparator#canSortEntities()
	 */
	public boolean canSortEntities() {
		return false;
	}

	/**
	 * @param e1
	 * @param e2
	 * @param attributeName
	 * 
	 * @return always returns 0;
	 * 
	 * @see SortParameterComparator#compare(Object, Object, String)
	 */
	public int compare(RODAMember e1, RODAMember e2, String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for the entity.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of names of the JNDI attributes that match the
	 * attributes in {@link JndiEntityAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the JNDI attributes
	 *         supported.
	 */
	public List<String> getJndiAttributeNames() {
		return Arrays.asList(jndiAttributeNames);
	}

	/**
	 * Returns the JNDI value for a given attribute value.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the attribute value adapted for a JNDI search.
	 */
	@Override
	public String getJndiValueForAtribute(String attributeName, String value) {

		String jndiValue = null;

		if ("active".equalsIgnoreCase(attributeName)) {
			jndiValue = Boolean.parseBoolean(value) ? "0" : "1";
		} else {
			jndiValue = value;
		}

		return jndiValue;
	}

}
