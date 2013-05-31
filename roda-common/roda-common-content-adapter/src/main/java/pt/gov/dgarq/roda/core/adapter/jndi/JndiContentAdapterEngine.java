package pt.gov.dgarq.roda.core.adapter.jndi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.ContentAdapterEngine;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;

/**
 * This is the {@link ContentAdapterEngine} for JNDI data sources. (ex: LDAP)
 * 
 * @author Rui Castro
 * 
 * @param <EA>
 *            the entity adapter type
 * @param <E>
 *            the entity being adapted.
 */
public class JndiContentAdapterEngine<EA extends JndiEntityAdapter<E>, E>
		extends ContentAdapterEngine<EA, E> {
	static final private Logger logger = Logger
			.getLogger(JndiContentAdapterEngine.class);

	/**
	 * Constructs a new {@link JndiContentAdapterEngine}.
	 * 
	 * @param entityAdapter
	 * @param contentAdapter
	 */
	public JndiContentAdapterEngine(EA entityAdapter,
			ContentAdapter contentAdapter) {
		super(entityAdapter, contentAdapter);
	}

	/**
	 * @see ContentAdapterEngine#getFilterParameterAdapter(FilterParameter)
	 */
	public JndiFilterParameterAdapter<EA> getFilterParameterAdapter(
			FilterParameter filterParameter) {

		JndiFilterParameterAdapter<EA> jndiParameterAdapter = null;

		if (filterParameter == null) {
			// Ignore null parameter
			logger.warn("null FilterParameter found. Ignored.");
		} else if (filterParameter instanceof SimpleFilterParameter) {

			jndiParameterAdapter = new JndiSimpleFilterParameter<EA>(
					getEntityAdapter(), (SimpleFilterParameter) filterParameter);

		} else if (filterParameter instanceof OneOfManyFilterParameter) {

			jndiParameterAdapter = new JndiOneOfManyFilterParameter<EA>(
					getEntityAdapter(),
					(OneOfManyFilterParameter) filterParameter);

		} else if (filterParameter instanceof RangeFilterParameter) {

			// No range
			logger.warn("FilterParameters with type "
					+ filterParameter.getClass().getSimpleName()
					+ " are not supported");

		} else if (filterParameter instanceof RegexFilterParameter) {

			jndiParameterAdapter = new JndiRegexFilterParameter<EA>(
					getEntityAdapter(), (RegexFilterParameter) filterParameter);

		} else {

			logger.warn("FilterParameters with type "
					+ filterParameter.getClass().getSimpleName()
					+ " are not supported");
		}

		return jndiParameterAdapter;
	}

	/**
	 * @param keyAttribute
	 *            the attribute that must be present.
	 * 
	 * @return a {@link String} with JNDI filter for the current
	 *         {@link ContentAdapter}.
	 */
	public String getJndiFilter(String keyAttribute) {

		String filter = "";

		if (hasFilter()) {

			filter += "(& ";

			if (keyAttribute != null) {
				filter += "(" + keyAttribute + "=*)";
			}

			for (FilterParameter filterParameter : getFilter().getParameters()) {
				if (filterParameter != null
						&& getEntityAdapter().hasAttribute(
								filterParameter.getName())) {

					JndiFilterParameterAdapter<EA> ldapFilterParameterAdapter = getFilterParameterAdapter(filterParameter);

					if (ldapFilterParameterAdapter != null) {

						String jndiFilter = ldapFilterParameterAdapter
								.getJndiFilter();

						if (jndiFilter != null) {
							filter += " " + jndiFilter;
						}
					}
				}
			}

			filter += ")";

		} else {

			if (keyAttribute != null) {
				filter += "(" + keyAttribute + "=*)";
			}
		}

		return filter;
	}

	/**
	 * Returns a sublist of the given DNs list based on the
	 * {@link ContentAdapter} sublist.
	 * 
	 * @param DNs
	 *            the list of DNs.
	 * 
	 * @return a {@link List} that is a sublist of the given DNs list or the
	 *         given DNs list if the {@link ContentAdapter} doesn't have a
	 *         sublist.
	 */
	public List<String> getDNSublist(List<String> DNs) {

		if (hasSublist()) {

			int lastItemIndex = getSublist().getFirstElementIndex()
					+ getSublist().getMaximumElementCount();
			if (lastItemIndex > DNs.size()) {
				lastItemIndex = DNs.size();
			}

			return DNs.subList(getSublist().getFirstElementIndex(),
					lastItemIndex);

		} else {

			return DNs;

		}
	}

	/**
	 * @param attributesList
	 * @return a {@link List} of {@link Attributes} sorted according to the
	 *         current {@link Sorter}.
	 */
	public List<Attributes> sortAttributes(List<Attributes> attributesList) {
		List<Attributes> sortedAttributesList = null;

		if (hasSorter()) {

			AttributesComparator<EA> attrComparator = new AttributesComparator<EA>(
					getEntityAdapter(), getSorter().getParameters());

			Collections.sort(attributesList, attrComparator);
			sortedAttributesList = attributesList;

		} else {
			sortedAttributesList = attributesList;
		}

		return sortedAttributesList;
	}

	/**
	 * @param attributesList
	 * @return
	 */
	public List<Attributes> getSublist(List<Attributes> attributesList) {

		if (hasSublist()) {

			int lastItemIndex = getSublist().getFirstElementIndex()
					+ getSublist().getMaximumElementCount();
			if (lastItemIndex > attributesList.size()) {
				lastItemIndex = attributesList.size();
			}

			return attributesList.subList(getSublist().getFirstElementIndex(),
					lastItemIndex);

		} else {

			return attributesList;

		}
	}

	class AttributesComparator<EA extends JndiEntityAdapter> implements
			Comparator<Attributes> {

		private EA entityAdapter = null;
		private SortParameter[] parameters = null;

		/**
		 * @param entityAdapter
		 * @param parameters
		 */
		public AttributesComparator(EA entityAdapter, SortParameter[] parameters) {
			this.entityAdapter = entityAdapter;
			this.parameters = parameters;
		}

		/**
		 * 
		 * @param as1
		 * @param as2
		 * @return -1, 0, -1
		 * @see Comparator#compare(Object, Object)
		 */
		public int compare(Attributes as1, Attributes as2) {

			int result = 0;
			for (int i = 0; result == 0 && i < this.parameters.length; i++) {

				try {

					String jndiName = this.entityAdapter
							.getJndiAttributeNameForAttribute(parameters[i]
									.getName());

					Attribute a1 = as1.get(jndiName);
					Attribute a2 = as2.get(jndiName);

					if (a1 == null || a2 == null) {

						if (a1 == a2) {
							// a1 and a2 are null
							result = 0;
						} else if (a1 == null) {
							// a1 is null, a2 is not null
							result = 1;
						} else {
							// a1 is not null, a2 is null
							result = -1;
						}

					} else {

						Object value1 = a1.get();
						Object value2 = a2.get();

						if (value1 instanceof Comparable) {
							result = ((Comparable) value1).compareTo(value2);
						} else {
							result = value1.toString().compareTo(
									value2.toString());
						}

					}

					if (parameters[i].isDescending()) {
						result *= -1;
					}

				} catch (NamingException e) {
					logger.warn("Error comparing attributes - "
							+ e.getMessage(), e);
				}

			}

			return result;
		}
	}

}
