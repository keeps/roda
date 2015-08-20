package pt.gov.dgarq.roda.core.fedora.risearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jrdf.graph.Node;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.adapter.itql.ITQLEntityAdapter;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * @author Rui Castro
 */
public class SimpleRepresentationPreservationObjectAdapter extends
		ITQLEntityAdapter<SimpleRepresentationPreservationObject> {
	static final private Logger logger = Logger
			.getLogger(SimpleRepresentationPreservationObjectAdapter.class);

	private static final String[] attributeNames = new String[] { "representationobjectpid" };

	private static final String[] itqlSubjects = new String[] { "$representationObjectPID" };

	// private static final String[] itqlPredicates = new String[] {
	// FedoraRISearch.ITQL_PREDICATE_RODA_PRESERVED_BY };
	private static final String[] itqlPredicates = new String[] { FedoraRISearch.ITQL_PREDICATE_RODA_PRESERVATION_OF };

	private RODAObjectAdapter rodaObjectAdapter = null;

	/**
	 * Constructs a new {@link SimpleRepresentationPreservationObjectAdapter}.
	 * 
	 * @param user
	 *            the {@link User}.
	 */
	public SimpleRepresentationPreservationObjectAdapter(User user) {
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
	public int compare(SimpleRepresentationPreservationObject e1,
			SimpleRepresentationPreservationObject e2, String attributeName) {
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
		List<String> list = new ArrayList<String>(
				rodaObjectAdapter.getAttributeNames());
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
		List<String> list = new ArrayList<String>(
				rodaObjectAdapter.getITQLSubjects());
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
		List<String> list = new ArrayList<String>(
				rodaObjectAdapter.getITQLPredicates());
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

			if ("representationObjectPID".equalsIgnoreCase(name)) {
				object = getFedoraURIFromPID(value);
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

			if ("representationObjectPID".equalsIgnoreCase(attributeName)) {

				itqlCondition = String.format("%1$s %2$s %3$s",
						itqlQuerySubject,
						getITQLPredicateForAttribute(attributeName),
						attributeValue);

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
		return null;
	}

	/**
	 * @see ITQLEntityAdapter#getEntityCountITQLQuerySubject()
	 */
	@Override
	public String getEntityCountITQLQuerySubject() {
		return rodaObjectAdapter.getEntityCountITQLQuerySubject();
	}

	/**
	 * @see ITQLEntityAdapter#getEntity(Map)
	 */
	@Override
	public SimpleRepresentationPreservationObject getEntity(
			Map<String, Node> tuple) {

		RODAObject rodaObject = rodaObjectAdapter.getEntity(tuple);

		SimpleRepresentationPreservationObject sro = new SimpleRepresentationPreservationObject(
				rodaObject, rodaObject.getLabel());

		sro.setRepresentationObjectPID(getPIDFromFedoraURI(tuple.get(
				"representationObjectPID").stringValue()));

		return sro;

	}

	@Override
	public String getITQLClassificationSchemeCondition(String itqlQuerySubject,
			String classificationSchemeId, String[] possibleParentsPids) {
		return null;
	}
}
