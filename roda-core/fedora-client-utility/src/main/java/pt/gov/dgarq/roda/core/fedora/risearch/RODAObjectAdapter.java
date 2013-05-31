package pt.gov.dgarq.roda.core.fedora.risearch;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.itql.ITQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.User;

/**
 * @author Rui Castro
 */
public class RODAObjectAdapter extends ITQLEntityAdapter<RODAObject> {
	private static final Logger logger = Logger
			.getLogger(RODAObjectAdapter.class);

	private static final String[] attributeNames = new String[] { "pid",
			"contentmodel", "label", "lastmodifieddate", "createddate", "state" };

	private static final String[] itqlSubjects = new String[] { "$pid",
			"$contentModel", "$label", "$lastModifiedDate", "$createdDate",
			"$state" };

	private static final String[] itqlPredicates = new String[] {
			"<http://mulgara.org/mulgara#is>",
			FedoraRISearch.ITQL_PREDICATE_FEDORA_CONTENT_MODEL,
			FedoraRISearch.ITQL_PREDICATE_FEDORA_MODEL_LABEL,
			FedoraRISearch.ITQL_PREDICATE_FEDORA_LAST_MODIFIED_DATE,
			FedoraRISearch.ITQL_PREDICATE_FEDORA_CREATED_DATE,
			FedoraRISearch.ITQL_PREDICATE_FEDORA_STATE };

	private User user = null;

	/**
	 * Constructs a new {@link RODAObject}.
	 * 
	 * @param user
	 *            the {@link User}.
	 */
	public RODAObjectAdapter(User user) {
		this.user = user;
	}

	/**
	 * @return the user
	 */
	protected User getUser() {
		return user;
	}

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
	public int compare(RODAObject e1, RODAObject e2,
			String attributeName) {
		return 0;
	}

	/**
	 * Returns the list of attributes for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the names of the attributes
	 *         supported.
	 */
	@Override
	public List<String> getAttributeNames() {
		return Arrays.asList(attributeNames);
	}

	/**
	 * Returns the list of ITQL subjects for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the ITQL subjects.
	 */
	@Override
	public List<String> getITQLSubjects() {
		return Arrays.asList(itqlSubjects);
	}

	/**
	 * Returns the list of ITQL predicates for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the ITQL predicates.
	 */
	@Override
	public List<String> getITQLPredicates() {
		return Arrays.asList(itqlPredicates);
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
	@Override
	public String getITQLObjectForAttribute(String name, String value) {
		String object = null;

		if ("pid".equalsIgnoreCase(name)) {
			object = getFedoraURIFromPID(value);
		}
		if ("label".equalsIgnoreCase(name)) {
			object = getLiteral(value);
		}
		if ("contentModel".equalsIgnoreCase(name)) {
			object = getLiteral(value);
		}
		if ("lastModifiedDate".equalsIgnoreCase(name)) {
			object = getISODate(value);
		}
		if ("createdDate".equalsIgnoreCase(name)) {
			object = getISODate(value);
		}
		if ("state".equalsIgnoreCase(name)) {
			object = getFedoraURIFromState(value);
		}

		return object;
	}

	/**
	 * @see ITQLEntityAdapter#getITQLCondition(String, String, String)
	 */
	@Override
	public String getITQLCondition(String itqlQuerySubject,
			String attributeName, String attributeValue) {

		String itqlCondition = null;

		if ("pid".equalsIgnoreCase(attributeName)
				|| "label".equalsIgnoreCase(attributeName)
				|| "contentModel".equalsIgnoreCase(attributeName)
				|| "lastModifiedDate".equalsIgnoreCase(attributeName)
				|| "lastModifiedDate".equalsIgnoreCase(attributeName)
				|| "createdDate".equalsIgnoreCase(attributeName)
				|| "state".equalsIgnoreCase(attributeName)) {

			itqlCondition = String
					.format("%1$s %2$s %3$s", itqlQuerySubject,
							getITQLPredicateForAttribute(attributeName),
							attributeValue);

		} else {
			itqlCondition = null;
		}

		return itqlCondition;
	}

	/**
	 * @see ITQLEntityAdapter#getITQLProducerCondition(String, String, List)
	 */
	@Override
	public String getITQLProducerCondition(String itqlQuerySubject,
			String username, List<String> groups) {
		return null;
	}

	/**
	 * @see ITQLEntityAdapter#getEntityCountITQLQuerySubject()
	 */
	@Override
	public String getEntityCountITQLQuerySubject() {
		return "$pid";
	}

	/**
	 * @see ITQLEntityAdapter#getEntity(Map)
	 */
	@Override
	public RODAObject getEntity(Map<String, Node> tuple) {

		String pid = getPIDFromFedoraURI(tuple.get("pid").stringValue());

		String label = tuple.get("label").stringValue();

		String contentModel = tuple.get("contentModel").stringValue();

		Date lastModifiedDate = null;
		try {
			lastModifiedDate = DateParser.parse(tuple.get("lastModifiedDate")
					.stringValue());
		} catch (InvalidDateException e) {
			logger.warn("Error parsing lastModifiedDate - " + e.getMessage()
					+ ". IGNORING", e);
		}

		Date createdDate = null;
		try {
			createdDate = DateParser.parse(tuple.get("createdDate")
					.stringValue());
		} catch (InvalidDateException e) {
			logger.warn("Error parsing createdDate - " + e.getMessage()
					+ ". IGNORING", e);
		}

		String state = getStateFromURI(tuple.get("state").stringValue());

		return new RODAObject(pid, label, contentModel, lastModifiedDate,
				createdDate, state);
	}

	/**
	 * @param itqlQuerySubject
	 * 
	 * @return a {@link String} with the ITQL conditions for user permissions
	 *         over the queried entity.
	 */
	public String getPermissionConditions(String itqlQuerySubject) {

		String conditions = String.format("%1$s %2$s '%3$s'", itqlQuerySubject,
				FedoraRISearch.ITQL_PREDICATE_RODA_PERMISSION_READ_USER,
				getUser().getName());

		for (String group : getUser().getAllGroups()) {
			conditions = String.format("%1$s or %2$s %3$s '%4$s'", conditions,
					itqlQuerySubject,
					FedoraRISearch.ITQL_PREDICATE_RODA_PERMISSION_READ_GROUP,
					group);
		}

		return " (" + conditions + ") ";
	}

	private String getStateFromURI(String stateURI) {
		return stateURI.replace("info:fedora/fedora-system:def/model#", "");
	}

	private String getFedoraURIFromState(String state) {

		String stateURI = null;

		if (RODAObject.STATE_ACTIVE.equalsIgnoreCase(state)) {
			stateURI = FedoraRISearch.ITQL_OBJECT_FEDORA_STATE_ACTIVE;
		} else if (RODAObject.STATE_INACTIVE.equalsIgnoreCase(state)) {
			stateURI = FedoraRISearch.ITQL_OBJECT_FEDORA_STATE_INACTIVE;
		} else if (RODAObject.STATE_DELETED.equalsIgnoreCase(state)) {
			stateURI = FedoraRISearch.ITQL_OBJECT_FEDORA_STATE_DELETED;
		}

		return stateURI;
	}
}
