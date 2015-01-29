package pt.gov.dgarq.roda.core.adapter.itql;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;

/**
 * This is an abstract adapter for ITQL entities.
 * 
 * @author Rui Castro
 * 
 * @param <E>
 *            the entity being adapted.
 */
public abstract class ITQLEntityAdapter<E> implements
		SortParameterComparator<E> {
	static final private Logger logger = Logger
			.getLogger(ITQLEntityAdapter.class);

	/**
	 * Returns the list of attributes for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the names of the attributes
	 *         supported.
	 */
	public abstract List<String> getAttributeNames();

	/**
	 * Verifies if entity has the given attribute.
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
	 * Returns the attribute for ITQL subject if the attribute exists.
	 * 
	 * @param itqlSubject
	 *            the ITQL subject.
	 * 
	 * @return a {@link String} with the attribute name for the given ITQL
	 *         subject or <code>null</code> if the ITQL subject doesn't exist.
	 */
	public String getAttributeForITQLSubject(String itqlSubject) {

		String attribute = null;

		int index = getITQLSubjects().indexOf(itqlSubject);

		if (index >= 0) {
			attribute = getAttributeNames().get(index);
		} else {
			attribute = null;
		}

		return attribute;
	}

	/**
	 * Returns the list of ITQL subjects for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the ITQL subjects.
	 */
	public abstract List<String> getITQLSubjects();

	/**
	 * Returns the ITQL subject name for the given attribute if the attribute
	 * exists.
	 * 
	 * @param itqlQuerySubject
	 *            the ITQL query subject (eg: $pid)
	 * @param attributeName
	 *            the name of the attribute.
	 * 
	 * @return a {@link String} with the ITQL subject for the given attribute
	 *         name or <code>null</code> if the attribute doesn't exist.
	 */
	public String getITQLSubjectForAttribute(String itqlQuerySubject,
			String attributeName) {
		return getITQLSubjectForAttribute(attributeName);
	}

	/**
	 * Returns the list of ITQL predicates for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the ITQL predicates.
	 */
	public abstract List<String> getITQLPredicates();

	/**
	 * Returns the ITQL predicate name for the given attribute if the attribute
	 * exists.
	 * 
	 * @param attribute
	 *            the name of the attribute.
	 * @return a {@link String} with the ITQL predicate for the given attribute
	 *         name or <code>null</code> if the attribute doesn't exist.
	 */
	public String getITQLPredicateForAttribute(String attribute) {
		String predicate = null;

		int index = getAttributeNames().indexOf(attribute.toLowerCase());

		if (index >= 0) {
			predicate = getITQLPredicates().get(index);
		} else {
			predicate = null;
		}

		return predicate;
	}

	/**
	 * Returns the ITQL subject name for the given attribute if the attribute
	 * exists.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @return a {@link String} with the ITQL subject for the given attribute
	 *         name or <code>null</code> if the attribute doesn't exist.
	 */
	public String getITQLSubjectForAttribute(String attributeName) {

		String subject = null;

		int index = getAttributeNames().indexOf(attributeName.toLowerCase());

		if (index >= 0) {
			subject = getITQLSubjects().get(index);
		} else {
			subject = null;
		}

		return subject;
	}

	/**
	 * Returns the ITQL value for this given attribute.
	 * 
	 * @param name
	 *            the name of the attribute.
	 * @param value
	 *            the value of the attribute.
	 * @return the SQL value for the given attribute.
	 */
	public abstract String getITQLObjectForAttribute(String name, String value);

	/**
	 * Returns the ITQL condition to bind the ITQL variable (subject) of the
	 * given attribute.
	 * 
	 * @param itqlQuerySubject
	 * @param attributeName
	 * 
	 * @return a {@link String} with the ITQL binding condition.
	 */
	public String getITQLBindingCondition(String itqlQuerySubject,
			String attributeName) {

		String itqlBindingCondition = "";

		String itqlSubject = getITQLSubjectForAttribute(attributeName);

		if (isValidBindingPredicate(itqlQuerySubject, itqlSubject)
				&& itqlSubject != null) {

			itqlBindingCondition = getITQLCondition(itqlQuerySubject,
					attributeName, itqlSubject);

		}

		return itqlBindingCondition;
	}

	/**
	 * Returns the ITQL query condition for the given attribute name and value.
	 * 
	 * @param itqlQuerySubject
	 * @param attributeName
	 * @param attributeValue
	 * 
	 * @return a {@link String} with the ITQL condition.
	 */
	public abstract String getITQLCondition(String itqlQuerySubject,
			String attributeName, String attributeValue);

	/**
	 * Returns the ITQL query condition for producer.
	 * 
	 * @param itqlQuerySubject
	 * @param username
	 *            the producer username.
	 * @param groups
	 *            the producer groups.
	 * 
	 * @return a {@link String} with the producer condition.
	 */
	public abstract String getITQLProducerCondition(String itqlQuerySubject,
			String username, List<String> groups);

	public abstract String getITQLClassificationSchemeCondition(
			String itqlQuerySubject, String classificationSchemeId,
			String[] possibleParentsPids);

	public abstract String getEntityCountITQLQuerySubject();

	/**
	 * Returns an entity from the given {@link Map} of tuples.
	 * 
	 * @param tuple
	 *            the {@link Map} of tuples returned by a ITQL query.
	 * 
	 * @return the entity being adapted by this adapter.
	 */
	public abstract E getEntity(Map<String, Node> tuple);

	protected String getFedoraURIFromPID(String value) {
		if (value == null) {
			return "<info:fedora/>";
		} else {
			value.replaceAll(">", "");
			return String.format("<info:fedora/%1$s>", value);
		}
	}

	protected String getPIDFromFedoraURI(String fedoraURI) {
		String[] uri_pid = fedoraURI.split("/");

		String pid;
		if (uri_pid.length > 1 && !StringUtils.isBlank(uri_pid[1])) {
			pid = uri_pid[1];
		} else {
			pid = null;
		}

		return pid;
	}

	protected String getLiteral(String value) {
		value = value.replaceAll("'", "\\'");
		return "'" + value + "'";
	}

	protected String getISODate(String value) {
		try {
			String toIsoDate = DateParser.getIsoDate(DateParser.parse(value))
					.replace("Z", "");

			toIsoDate = toIsoDate.replaceAll("'", "\\'");
			return "'" + toIsoDate + "'";

		} catch (InvalidDateException e) {
			throw new IllegalArgumentException(
					"date is not a valid ISO datetime");
		}
	}

	protected boolean isValidBindingPredicate(String itqlQuerySubject,
			String itqlSubject) {
		return !itqlQuerySubject.equalsIgnoreCase(itqlSubject);
	}

}
