package pt.gov.dgarq.roda.core.fedora.risearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.trippi.RDFUtil.FreeLiteral;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.itql.ITQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;

/**
 * @author Rui Castro
 */
public class SimpleDescriptionObjectAdapter extends
		ITQLEntityAdapter<SimpleDescriptionObject> {

	static final private Logger logger = Logger
			.getLogger(SimpleDescriptionObjectAdapter.class);

	private static final String[] attributeNames = new String[] { "id",
			"level", "countrycode", "repositorycode", "title", "dateinitial",
			"datefinal", "parentpid", "subelementscount", "produceruser",
			"producergroup" };

	private static final String[] itqlSubjects = new String[] { "$id",
			"$level", "$countryCode", "$repositoryCode", "$title",
			"$dateInitial", "$dateFinal", "$parentPID", "$k0", null, null };

	private static final String[] itqlPredicates = new String[] {
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_ID,
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL,
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_COUNTRYCODE,
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_REPOSITORYCODE,
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_TITLE,
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_DATEINITIAL,
			FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_DATEFINAL,
			FedoraRISearch.ITQL_PREDICATE_RODA_CHILD_OF, null,
			FedoraRISearch.ITQL_PREDICATE_RODA_PRODUCER_USER,
			FedoraRISearch.ITQL_PREDICATE_RODA_PRODUCER_GROUP, };

	private RODAObjectAdapter rodaObjectAdapter = null;

	/**
	 * Constructs a new {@link SimpleDescriptionObject}.
	 * 
	 * @param user
	 *            the {@link User}.
	 */
	public SimpleDescriptionObjectAdapter(User user) {
		rodaObjectAdapter = new RODAObjectAdapter(user);
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
	public int compare(SimpleDescriptionObject e1, SimpleDescriptionObject e2,
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
		List<String> list = new ArrayList<String>(rodaObjectAdapter
				.getAttributeNames());
		list.addAll(Arrays.asList(attributeNames));
		return list;
	}

	/**
	 * Returns the list of ITQL subjects for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the ITQL subjects.
	 */
	@Override
	public List<String> getITQLSubjects() {
		List<String> list = new ArrayList<String>(rodaObjectAdapter
				.getITQLSubjects());
		list.addAll(Arrays.asList(itqlSubjects));
		return list;
	}

	/**
	 * Returns the list of ITQL predicates for the entity.
	 * 
	 * @return a {@link List} of {@link String} with the ITQL predicates.
	 */
	@Override
	public List<String> getITQLPredicates() {
		List<String> list = new ArrayList<String>(rodaObjectAdapter
				.getITQLPredicates());
		list.addAll(Arrays.asList(itqlPredicates));
		return list;
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

		String object = rodaObjectAdapter
				.getITQLObjectForAttribute(name, value);

		if (object == null) {

			if ("id".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("level".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("countryCode".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("repositoryCode".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("title".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("dateInitial".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("dateFinal".equalsIgnoreCase(name)) {
				object = getLiteral(value);
			} else if ("parentPID".equalsIgnoreCase(name)) {
				object = getFedoraURIFromPID(value);
			} else {
				object = null;
			}

		} else {
			// Parent already set the object
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

		if (rodaObjectAdapter.hasAttribute(attributeName)) {

			itqlCondition = rodaObjectAdapter.getITQLCondition(
					itqlQuerySubject, attributeName, attributeValue);

		} else {

			if ("id".equalsIgnoreCase(attributeName)
					|| "level".equalsIgnoreCase(attributeName)
					|| "countryCode".equalsIgnoreCase(attributeName)
					|| "repositoryCode".equalsIgnoreCase(attributeName)
					|| "title".equalsIgnoreCase(attributeName)
					|| "dateInitial".equalsIgnoreCase(attributeName)
					|| "dateFinal".equalsIgnoreCase(attributeName)
					|| "parentPID".equalsIgnoreCase(attributeName)) {

				itqlCondition = String.format("%1$s %2$s %3$s",
						itqlQuerySubject,
						getITQLPredicateForAttribute(attributeName),
						attributeValue);

			} else if ("producerUser".equalsIgnoreCase(attributeName)) {
				itqlCondition = null;
			} else if ("producerGroup".equalsIgnoreCase(attributeName)) {
				itqlCondition = null;
			} else {
				itqlCondition = null;
			}
		}

		return itqlCondition;
	}

	/**
	 * @see ITQLEntityAdapter#getITQLProducerCondition(String, String, List)
	 */
	@Override
	public String getITQLProducerCondition(String itqlQuerySubject,
			String username, List<String> groups) {

		String itqlObjectFonds = getITQLObjectForAttribute("level",
				DescriptionLevel.FONDS.getLevel());

		String conditions1 = String.format("%1$s %2$s %3$s and %4$s",
				itqlQuerySubject,
				FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL,
				itqlObjectFonds, getProducerConditions(itqlQuerySubject,
						username, groups));

		String conditions2 = String
				.format(
						"$ascendent %2$s %3$s and (%1$s %4$s $ascendent or trans(%1$s %4$s $ascendent)) and %5$s",
						itqlQuerySubject,
						FedoraRISearch.ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL,
						itqlObjectFonds,
						FedoraRISearch.ITQL_PREDICATE_RODA_CHILD_OF,
						getProducerConditions("$ascendent", username, groups));

		return String.format("((%s) or (%s))", conditions1, conditions2);
	}

	/**
	 * @see ITQLEntityAdapter#getITQLSubjectForAttribute(String, String)
	 */
	@Override
	public String getITQLSubjectForAttribute(String itqlQuerySubject,
			String attributeName) {

		if ("subElementsCount".equalsIgnoreCase(attributeName)) {

			// the subject is a count subquery
			return String
					.format(
							"count(select $child from <#ri> where $child %1$s %2$s and %3$s)",
							FedoraRISearch.ITQL_PREDICATE_RODA_CHILD_OF,
							itqlQuerySubject, rodaObjectAdapter
									.getPermissionConditions("$child"));

		} else {
			return super.getITQLSubjectForAttribute(itqlQuerySubject,
					attributeName);
		}
	}

	/**
	 * @see ITQLEntityAdapter#getEntityCountITQLQuerySubject()
	 */
	@Override
	public String getEntityCountITQLQuerySubject() {
		return rodaObjectAdapter.getEntityCountITQLQuerySubject();
	}

	/**
	 * @see RODAObjectAdapter#getEntity(Map)
	 */
	@Override
	public SimpleDescriptionObject getEntity(Map<String, Node> tuple) {

		SimpleDescriptionObject sdo = new SimpleDescriptionObject(
				rodaObjectAdapter.getEntity(tuple));

		if (tuple.containsKey("level")) {
			sdo
					.setLevel(new DescriptionLevel(tuple.get("level")
							.stringValue()));
		}

		if (tuple.containsKey("countryCode")) {
			sdo.setCountryCode(tuple.get("countryCode").stringValue());
		}

		if (tuple.containsKey("repositoryCode")) {
			sdo.setRepositoryCode(tuple.get("repositoryCode").stringValue());
		}

		if (tuple.containsKey("id")) {
			sdo.setId(tuple.get("id").stringValue());
		}

		if (tuple.containsKey("title")) {
			sdo.setTitle(tuple.get("title").stringValue());
		}

		if (tuple.containsKey("dateInitial")) {
			String dateinitial = tuple.get("dateInitial").stringValue();
			if (!StringUtils.isBlank(dateinitial)) {
				sdo.setDateInitial(dateinitial);
			}
		}

		if (tuple.containsKey("dateFinal")) {
			String datefinal = tuple.get("dateFinal").stringValue();
			if (!StringUtils.isBlank(datefinal)) {
				sdo.setDateFinal(datefinal);
			}
		}

		if (tuple.containsKey("parentPID")) {
			String parentPIDURI = tuple.get("parentPID").stringValue();
			if (!StringUtils.isBlank(parentPIDURI)) {
				sdo.setParentPID(getPIDFromFedoraURI(parentPIDURI));
			}
		}

		if (tuple.containsKey("k0")) {
			FreeLiteral childCountValue = (FreeLiteral) tuple.get("k0");
			sdo.setSubElementsCount((int) childCountValue.doubleValue());
		}

		return sdo;
	}

	private String getProducerConditions(String itqlSubject, String username,
			List<String> groups) {

		String producerConditions = String.format("%1$s %2$s '%3$s'",
				itqlSubject, getITQLPredicateForAttribute("produceruser"),
				username);

		for (String group : groups) {
			producerConditions = String.format("%1$s or %2$s %3$s '%4$s'",
					producerConditions, itqlSubject,
					getITQLPredicateForAttribute("producergroup"), group);
		}

		return " (" + producerConditions + ") ";
	}

}
