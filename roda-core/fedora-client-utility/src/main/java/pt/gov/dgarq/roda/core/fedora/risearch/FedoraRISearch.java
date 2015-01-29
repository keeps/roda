package pt.gov.dgarq.roda.core.fedora.risearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.trippi.TrippiException;
import org.trippi.TupleIterator;

import pt.gov.dgarq.roda.core.adapter.itql.ITQLContentAdapterEngine;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

/**
 * Wrapper functions to connect to Fedora Resource Index Query Service.
 * 
 * @author Rui Castro
 * @author Luís Faria
 */
public class FedoraRISearch {
	private static final Logger logger = Logger.getLogger(FedoraRISearch.class);

	private static Configuration configuration = null;

	private static String RDF_RODA_NAMESPACE;
	private static String RDF_RODA_NAMESPACE_ALIAS;

	private static String RDF_RODA_CHILD_OF;
	private static String RDF_RODA_REPRESENTED_BY;
	private static String RDF_RODA_PRESERVED_BY;
	private static String RDF_RODA_PRESERVATION_OF;
	private static String RDF_RODA_PERFORMED_ON;
	private static String RDF_RODA_PERFORMED_BY;
	private static String RDF_RODA_DERIVED_FROM;

	private static String RDF_RODA_DESCRIPTION_LEVEL;
	private static String RDF_RODA_DESCRIPTION_COUNTRYCODE;
	private static String RDF_RODA_DESCRIPTION_REPOSITORYCODE;
	private static String RDF_RODA_DESCRIPTION_ID;
	private static String RDF_RODA_DESCRIPTION_TITLE;
	private static String RDF_RODA_DESCRIPTION_DATEINITIAL;
	private static String RDF_RODA_DESCRIPTION_DATEFINAL;

	private static String RDF_RODA_PERMISSION_READ_USER;
	private static String RDF_RODA_PERMISSION_READ_GROUP;

	private static String RDF_RODA_PRODUCER_USER;
	private static String RDF_RODA_PRODUCER_GROUP;

	private static String RDF_RODA_REPRESENTATION_STATUS;
	private static String RDF_RODA_REPRESENTATION_TYPE;
	private static String RDF_RODA_REPRESENTATION_SUBTYPE;

	public static String RDF_TAG_CHILD_OF;
	public static String RDF_TAG_REPRESENTED_BY;
	public static String RDF_TAG_PRESERVED_BY;
	public static String RDF_TAG_PRESERVATION_OF;
	public static String RDF_TAG_PERFORMED_ON;
	public static String RDF_TAG_PERFORMED_BY;
	public static String RDF_TAG_DERIVED_FROM;

	public static String RDF_TAG_DESCRIPTION_LEVEL;
	public static String RDF_TAG_DESCRIPTION_COUNTRYCODE;
	public static String RDF_TAG_DESCRIPTION_REPOSITORYCODE;
	public static String RDF_TAG_DESCRIPTION_ID;
	public static String RDF_TAG_DESCRIPTION_TITLE;
	public static String RDF_TAG_DESCRIPTION_DATEINITIAL;
	public static String RDF_TAG_DESCRIPTION_DATEFINAL;

	public static String RDF_TAG_PERMISSION_READ_USER;
	public static String RDF_TAG_PERMISSION_READ_GROUP;

	public static String RDF_TAG_PRODUCER_USER;
	public static String RDF_TAG_PRODUCER_GROUP;

	public static String RDF_TAG_REPRESENTATION_STATUS;
	public static String RDF_TAG_REPRESENTATION_TYPE;
	public static String RDF_TAG_REPRESENTATION_SUBTYPE;

	public static String ITQL_PREDICATE_FEDORA_MODEL_LABEL;
	public static String ITQL_PREDICATE_FEDORA_CONTENT_MODEL;
	public static String ITQL_PREDICATE_FEDORA_LAST_MODIFIED_DATE;
	public static String ITQL_PREDICATE_FEDORA_CREATED_DATE;
	public static String ITQL_PREDICATE_FEDORA_STATE;
	public static String ITQL_PREDICATE_FEDORA_HAS_DATASTREAM;

	public static String ITQL_PREDICATE_RODA_CHILD_OF;
	public static String ITQL_PREDICATE_RODA_REPRESENTED_BY;
	public static String ITQL_PREDICATE_RODA_PRESERVED_BY;
	public static String ITQL_PREDICATE_RODA_PRESERVATION_OF;
	public static String ITQL_PREDICATE_RODA_PERFORMED_ON;
	public static String ITQL_PREDICATE_RODA_PERFORMED_BY;
	public static String ITQL_PREDICATE_RODA_DERIVED_FROM;

	public static String ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL;
	public static String ITQL_PREDICATE_RODA_DESCRIPTION_COUNTRYCODE;
	public static String ITQL_PREDICATE_RODA_DESCRIPTION_REPOSITORYCODE;
	public static String ITQL_PREDICATE_RODA_DESCRIPTION_ID;
	public static String ITQL_PREDICATE_RODA_DESCRIPTION_TITLE;
	public static String ITQL_PREDICATE_RODA_DESCRIPTION_DATEINITIAL;
	public static String ITQL_PREDICATE_RODA_DESCRIPTION_DATEFINAL;

	public static String ITQL_PREDICATE_RODA_PERMISSION_READ_USER;
	public static String ITQL_PREDICATE_RODA_PERMISSION_READ_GROUP;

	public static String ITQL_PREDICATE_RODA_PRODUCER_USER;
	public static String ITQL_PREDICATE_RODA_PRODUCER_GROUP;

	public static String ITQL_PREDICATE_RODA_REPRESENTATION_STATUS;
	public static String ITQL_PREDICATE_RODA_REPRESENTATION_TYPE;
	public static String ITQL_PREDICATE_RODA_REPRESENTATION_SUBTYPE;

	public static String ITQL_OBJECT_FEDORA_STATE_ACTIVE;
	public static String ITQL_OBJECT_FEDORA_STATE_INACTIVE;
	public static String ITQL_OBJECT_FEDORA_STATE_DELETED;

	private FedoraClientUtility fedoraClientUtility = null;
	private User user = null;

	/**
	 * Create a new client class for Fedora RI Search service.
	 * 
	 * @param fedoraClientUtility
	 *            the {@link FedoraClientUtility} related with this
	 *            {@link FedoraRISearch}.
	 * @param user
	 *            user to use in connection
	 * @param password
	 *            password to use in connection
	 * 
	 * @throws FedoraRISearchException
	 */
	public FedoraRISearch(FedoraClientUtility fedoraClientUtility,
			CASUserPrincipal user) throws FedoraRISearchException {

		this.fedoraClientUtility = fedoraClientUtility;

		setUser(user);

		if (configuration == null) {
			try {

				configuration = readConfiguration();

			} catch (ConfigurationException e) {
				throw new FedoraRISearchException(
						"Error reading roda-fedorarisearch.properties - "
								+ e.getMessage(), e);
			}
		}
	}

	/*
	 * RODAObject methods
	 */

	/**
	 * Gets the {@link RODAObject} with the given PID.
	 * 
	 * @param PID
	 *            the PID of the {@link RODAObject}.
	 * 
	 * @return the {@link RODAObject} for the specified PID.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public RODAObject getRODAObject(String PID)
			throws NoSuchRODAObjectException, FedoraRISearchException {

		Filter filter = new Filter(
				new FilterParameter[] { new SimpleFilterParameter("pid", PID) });

		ITQLContentAdapterEngine<RODAObjectAdapter, RODAObject> riSearchAdapter = new ITQLContentAdapterEngine<RODAObjectAdapter, RODAObject>(
				new RODAObjectAdapter(getUser()), new ContentAdapter(filter,
						null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getRODAObject(" + PID + ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			RODAObject rodaObject = riSearchAdapter.getEntity(tuples);

			if (rodaObject == null) {
				throw new NoSuchRODAObjectException("Object with PID " + PID
						+ " doesn't exist in Fedora RI.");
			}

			tuples.close();

			logger.trace("getRODAObject(" + PID + ") => " + rodaObject);

			return rodaObject;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Get the number of {@link RODAObject}s that respect the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link Filter} for the results.
	 * 
	 * @return an <code>int</code> with the number of {@link RODAObject}s.
	 * 
	 * @throws FedoraRISearchException
	 */
	public int getRODAObjectCount(Filter filter) throws FedoraRISearchException {

		ITQLContentAdapterEngine<RODAObjectAdapter, RODAObject> riSearchAdapter = new ITQLContentAdapterEngine<RODAObjectAdapter, RODAObject>(
				new RODAObjectAdapter(getUser()), new ContentAdapter(filter,
						null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntityCountITQLQuery(itqlPermissionConditions);

		logger.trace("getRODAObjectCount(" + filter + ") ITQL Query: "
				+ itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			int count = riSearchAdapter.getEntityCount(tuples);
			tuples.close();

			logger.trace("getRODAObjectCount(" + filter + ") => " + count);

			return count;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Get the {@link RODAObject}s that respect the given {@link ContentAdapter}
	 * .
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter} for the results.
	 * 
	 * @return A {@link List <RODAObject>}.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<RODAObject> getRODAObjects(ContentAdapter contentAdapter)
			throws FedoraRISearchException {

		ITQLContentAdapterEngine<RODAObjectAdapter, RODAObject> riSearchAdapter = new ITQLContentAdapterEngine<RODAObjectAdapter, RODAObject>(
				new RODAObjectAdapter(getUser()), contentAdapter);

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getRODAObjects(" + contentAdapter + ") ITQL Query: "
				+ itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			List<RODAObject> results = riSearchAdapter.getEntities(tuples);
			tuples.close();

			logger.trace("getRODAObjects(" + contentAdapter + ") => "
					+ results.size() + " results");

			return results;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/*
	 * SimpleDescriptionObject methods
	 */

	/**
	 * Gets a {@link SimpleDescriptionObject} with the specified PID from the
	 * properties in RELS-EXT.
	 * 
	 * @param sdoPID
	 *            the PID of the {@link SimpleDescriptionObject}.
	 * 
	 * @return a {@link SimpleDescriptionObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public SimpleDescriptionObject getSimpleDescriptionObject(String sdoPID)
			throws NoSuchRODAObjectException, FedoraRISearchException {

		Filter filter = new Filter(
				new FilterParameter[] { new SimpleFilterParameter("pid", sdoPID) });

		ITQLContentAdapterEngine<SimpleDescriptionObjectAdapter, SimpleDescriptionObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleDescriptionObjectAdapter, SimpleDescriptionObject>(
				new SimpleDescriptionObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleDescriptionObject(" + sdoPID + ") ITQL Query: "
				+ itqlQuery);

		SimpleDescriptionObject sdo = null;
		try {

			TupleIterator tuples = getTuples(itqlQuery);

			sdo = riSearchAdapter.getEntity(tuples);

			if (sdo == null) {
				throw new NoSuchRODAObjectException(
						"SimpleDescriptionObject with PID " + sdoPID
								+ " doesn't exist in Fedora RI.");
			}

			tuples.close();

			logger.trace("getSimpleDescriptionObject(" + sdoPID + ") => " + sdo);

			return sdo;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Get the number of {@link SimpleDescriptionObject}s that respect the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link Filter} for the results.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link SimpleDescriptionObject}s.
	 * 
	 * @throws FedoraRISearchException
	 */
	public int getSimpleDescriptionObjectCount(Filter filter)
			throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleDescriptionObjectAdapter, SimpleDescriptionObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleDescriptionObjectAdapter, SimpleDescriptionObject>(
				new SimpleDescriptionObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntityCountITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleDescriptionObjectCount(" + filter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			int count = riSearchAdapter.getEntityCount(tuples);
			tuples.close();

			logger.trace("getSimpleDescriptionObjectCount(" + filter + ") => "
					+ count);

			return count;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Get the {@link SimpleDescriptionObject}s that respect the given
	 * {@link ContentAdapter} .
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter} for the results.
	 * 
	 * @return A {@link List <SimpleDescriptionObject>}.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<SimpleDescriptionObject> getSimpleDescriptionObjects(
			ContentAdapter contentAdapter) throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleDescriptionObjectAdapter, SimpleDescriptionObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleDescriptionObjectAdapter, SimpleDescriptionObject>(
				new SimpleDescriptionObjectAdapter(getUser()), contentAdapter);

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleDescriptionObjects(" + contentAdapter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			List<SimpleDescriptionObject> results = riSearchAdapter
					.getEntities(tuples);

			tuples.close();

			logger.trace("getSimpleDescriptionObjects(" + contentAdapter
					+ ") => " + results.size() + " results");

			return results;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Returns a list of all Description Object PIDs.
	 * 
	 * @return a {@link List} of {@link String} with the PIDs of all DOs.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<String> getDOPIDs() throws FedoraRISearchException {

		List<String> subjects = getTripleSubjects(
				ITQL_PREDICATE_FEDORA_CONTENT_MODEL, "'roda:d'");

		List<String> doPIDs = new ArrayList<String>();

		for (String subject : subjects) {
			doPIDs.add(getPIDFromFedoraURI(subject));
		}

		logger.trace("getDOPIDs() => " + doPIDs.size());

		return doPIDs;
	}

	/**
	 * Gets the Descriptive Object's ancestors PIDs.
	 * 
	 * @param doPID
	 *            the PID of the DO.
	 * 
	 * @return a {@link List} of {@link String}'s with the PIDs of the ancestors
	 *         of this DO. The {@link List} is orderer such as the fonds is the
	 *         first element and the specified object PID is the last.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public List<String> getDOAncestorPIDs(String doPID)
			throws NoSuchRODAObjectException, FedoraRISearchException {

		// Try to get RODAObject with the specified doPID
		getRODAObject(doPID);

		String itqlQuery = String.format(
				"select $child $parent from <#ri> where "
						+ "walk (%1$s %2$s $parent and $child %2$s $parent)",
				getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_CHILD_OF);

		logger.trace("getDOAncestorPIDs(" + doPID + ") ITQL Query: "
				+ itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			Map<String, String> childOfMap = new HashMap<String, String>();

			while (tuples.hasNext()) {
				Map<String, Node> attributes = tuples.next();
				childOfMap.put(getPIDFromFedoraURI(attributes.get("child")
						.stringValue()),
						getPIDFromFedoraURI(attributes.get("parent")
								.stringValue()));
			}

			tuples.close();

			List<String> ancestors = new ArrayList<String>();
			// doPID is the last element in the list.
			ancestors.add(doPID);

			String parent = childOfMap.get(doPID);
			while (!StringUtils.isBlank(parent)) {
				ancestors.add(0, parent);
				parent = childOfMap.get(parent);
			}

			logger.trace("getDOAncestorPIDs(" + doPID + ") => " + ancestors);

			return ancestors;

		} catch (IOException e) {

			logger.error("Error getting ancestors - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting ancestors - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating ancestors - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating ancestors - "
					+ e.getMessage(), e);

		}

	}

	/**
	 * Gets the Description Levels of the children of a given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return a {@link List} of {@link String}s with the levels.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<String> getDOChildrenLevels(String doPID)
			throws FedoraRISearchException {

		String itqlQuery = String.format("select $level from <#ri>"
				+ " where $child %1$s %2$s and $child %3$s $level",
				ITQL_PREDICATE_RODA_CHILD_OF,
				getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL);

		logger.trace("getDOChildrenLevels(" + doPID + ") ITQL query: "
				+ itqlQuery);

		List<String> childrenLevels = new ArrayList<String>();
		try {

			TupleIterator tuples = getTuples(itqlQuery);

			while (tuples.hasNext()) {
				childrenLevels.add(tuples.next().get("level").stringValue());
			}

			tuples.close();

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

		logger.trace("getDOChildrenLevels(" + doPID + ") => " + childrenLevels);

		return childrenLevels;
	}

	/**
	 * Gets the description IDs of the children of a given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return a {@link List} of {@link String}s with the IDs.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<String> getDOChildrenIDs(String doPID)
			throws FedoraRISearchException {

		String itqlQuery = String.format("select $id from <#ri>"
				+ " where $child %1$s %2$s and $child %3$s $id",
				ITQL_PREDICATE_RODA_CHILD_OF,
				getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_DESCRIPTION_ID);

		logger.trace("getDOChildrenIDs(" + doPID + ") ITQL query: " + itqlQuery);

		List<String> childrenIDs = new ArrayList<String>();
		try {

			TupleIterator tuples = getTuples(itqlQuery);

			while (tuples.hasNext()) {
				childrenIDs.add(tuples.next().get("id").stringValue());
			}

			tuples.close();

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

		logger.trace("getDOChildrenIDs(" + doPID + ") => " + childrenIDs);

		return childrenIDs;
	}

	/**
	 * Returns the level of the parent DescriptionObject (DO) of the DO with the
	 * given pid.
	 * 
	 * @param doPID
	 *            the pid of the child DO
	 * 
	 * @return the level of the parent DO or <code>null</code> if given DO has
	 *         no parent.
	 * 
	 * @throws FedoraRISearchException
	 * 
	 */
	public String getDOParentLevel(String doPID) throws FedoraRISearchException {

		String itqlQuery = String.format("select $level from <#ri>"
				+ " where %1$s %2$s $parent and $parent %3$s $level",
				getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_CHILD_OF,
				ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL);

		logger.trace("getDOParentLevel(" + doPID + ") ITQL query: " + itqlQuery);

		String parentLevel;
		try {
			TupleIterator tuples = getTuples(itqlQuery);

			if (tuples.hasNext()) {

				Map<String, Node> tuple = tuples.next();

				parentLevel = tuple.get("level").stringValue();

			} else {

				// doPID doesn't have a parent.
				parentLevel = null;

			}

			if (tuples.hasNext()) {
				logger.warn("ITQL Query returned more than one parent level for PID "
						+ doPID + "!!!");
			}

			tuples.close();

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

		logger.trace("getDOParentLevel(" + doPID + ") => " + parentLevel);

		return parentLevel;
	}

	/**
	 * Gets the {@link Producers} of a given {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return the {@link Producers}.
	 * 
	 * @throws FedoraRISearchException
	 */
	public Producers getDOProducers(String doPID)
			throws FedoraRISearchException {

		List<String> users = getTripleObjects(
				getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_PRODUCER_USER);
		List<String> groups = getTripleObjects(
				getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_PRODUCER_GROUP);

		Producers producers = new Producers();
		producers.setDescriptionObjectPID(doPID);
		producers.setUsers(users.toArray(new String[users.size()]));
		producers.setGroups(groups.toArray(new String[groups.size()]));

		return producers;
	}

	/**
	 * Gets the PIDs of all {@link DescriptionObject}s descendant of the given
	 * {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the ancestor {@link DescriptionObject}.
	 * 
	 * @return a {@link List} of {@link String}s with the PIDs of the
	 *         descendants.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public List<String> getDescendantDescriptionObjectPIDs(String doPID)
			throws NoSuchRODAObjectException, FedoraRISearchException {

		// Try to get RODAObject with the specified doPID
		getRODAObject(doPID);

		String itqlQuery = String.format("select $descendant from <#ri> where "
				+ "walk ($object %2$s %1$s and $descendant %2$s $object)"
				+ " and %3$s", getRIFedoraObjectURIFromPID(doPID),
				ITQL_PREDICATE_RODA_CHILD_OF,
				getPermissionConditions("$descendant"));

		logger.trace("getDescendantDescriptionObjectPIDs(" + doPID
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			List<String> descendantPIDs = new ArrayList<String>();

			while (tuples.hasNext()) {
				Map<String, Node> attributes = tuples.next();
				descendantPIDs.add(getPIDFromFedoraURI(attributes.get(
						"descendant").toString()));
			}

			tuples.close();

			logger.trace("getDescendantDescriptionObjectPIDs(" + doPID
					+ ") => " + descendantPIDs.size() + " descendant(s) DO(s)");

			return descendantPIDs;

		} catch (IOException e) {

			logger.error("Error getting descendants - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting descendants - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating descendants - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating descendants - "
					+ e.getMessage(), e);

		}

	}

	/**
	 * Gets the PIDs of all {@link RODAObject}s, descendant of the given
	 * {@link RODAObject}. Descendants can be {@link DescriptionObject}s,
	 * {@link RepresentationObject}s, {@link RepresentationPreservationObject}
	 * and {@link EventPreservationObject}s.
	 * 
	 * @param PID
	 *            the PID of the {@link RODAObject}.
	 * @param childDOs
	 *            <code>true</code> to return child {@link DescriptionObject}s
	 *            and <code>false</code> to return only
	 *            {@link RepresentationObject}s and it's descendants.
	 * 
	 * @return a {@link List} of {@link String}s with the PIDs of the
	 *         descendants.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public List<String> getDODescendantPIDs(String doPID, boolean childDOs)
			throws NoSuchRODAObjectException, FedoraRISearchException {

		Set<String> descendantPIDs = new HashSet<String>();

		List<String> doPIDs = new ArrayList<String>();
		doPIDs.add(doPID);

		if (childDOs) {
			List<String> descendantDescriptionObjectPIDs = getDescendantDescriptionObjectPIDs(doPID);
			descendantPIDs.addAll(descendantDescriptionObjectPIDs);
			doPIDs.addAll(descendantDescriptionObjectPIDs);
		}

		for (String doPIDtoProcess : doPIDs) {
			// (DO "represented-by" RO)
			List<String> roPIDs = getPIDsFromURIs(getTripleObjects(
					getRIFedoraObjectURIFromPID(doPIDtoProcess),
					ITQL_PREDICATE_RODA_REPRESENTED_BY));
			descendantPIDs.addAll(roPIDs);

			for (String roPID : roPIDs) {
				List<String> rpoPIDs = getRORepresentationPreservationObjectPIDs(roPID);
				descendantPIDs.addAll(rpoPIDs);

				for (String rpoPID : rpoPIDs) {
					// (EPO "performed-on" RPO)
					List<String> epoPIDs = getPIDsFromURIs(getTripleSubjects(
							ITQL_PREDICATE_RODA_PERFORMED_ON,
							getRIFedoraObjectURIFromPID(rpoPID)));
					descendantPIDs.addAll(epoPIDs);
				}

			}
		}

		return new ArrayList<String>(descendantPIDs);
	}

	/**
	 * Gets a {@link SimpleRepresentationObject} with the specified PID from the
	 * properties in RELS-EXT.
	 * 
	 * @param sroPID
	 *            the PID of the {@link SimpleRepresentationObject}.
	 * 
	 * @return a {@link SimpleRepresentationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public SimpleRepresentationObject getSimpleRepresentationObject(
			String sroPID) throws NoSuchRODAObjectException,
			FedoraRISearchException {

		Filter filter = new Filter(
				new FilterParameter[] { new SimpleFilterParameter("pid", sroPID) });

		ITQLContentAdapterEngine<SimpleRepresentationObjectAdapter, SimpleRepresentationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleRepresentationObjectAdapter, SimpleRepresentationObject>(
				new SimpleRepresentationObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleRepresentationObject(" + sroPID
				+ ") ITQL Query: " + itqlQuery);

		SimpleRepresentationObject sro = null;
		try {

			TupleIterator tuples = getTuples(itqlQuery);

			List<SimpleRepresentationObject> results = riSearchAdapter
					.getEntities(tuples);

			tuples.close();

			results = joinRepresentationsByStatus(results);

			if (results.size() > 0) {
				sro = results.get(0);

				if (results.size() > 1) {
					logger.warn("More that 1 SRO with PID " + sroPID + "!!!");
				}
			} else {
				throw new NoSuchRODAObjectException(
						"SimpleRepresentationObject with PID " + sroPID
								+ " doesn't exist in Fedora RI.");
			}

			logger.trace("getSimpleRepresentationObject(" + sroPID + ") => "
					+ sro);

			return sro;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Returns the number of {@link RepresentationObject}s that match the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link RepresentationObject}s that match the given {@link Filter}
	 *         .
	 * @throws FedoraRISearchException
	 */
	public int getSimpleRepresentationObjectCount(Filter filter)
			throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleRepresentationObjectAdapter, SimpleRepresentationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleRepresentationObjectAdapter, SimpleRepresentationObject>(
				new SimpleRepresentationObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntityCountITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleRepresentationObjectCount(" + filter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			int count = riSearchAdapter.getEntityCount(tuples);
			tuples.close();

			logger.trace("getSimpleRepresentationObjectCount(" + filter
					+ ") => " + count);

			return count;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Gets the list of {@link SimpleRepresentationObject}s that match the given
	 * {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleRepresentationObject} that match the
	 *         given {@link ContentAdapter}.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<SimpleRepresentationObject> getSimpleRepresentationObjects(
			ContentAdapter contentAdapter) throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleRepresentationObjectAdapter, SimpleRepresentationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleRepresentationObjectAdapter, SimpleRepresentationObject>(
				new SimpleRepresentationObjectAdapter(getUser()),
				contentAdapter);

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleRepresentationObjects(" + contentAdapter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			List<SimpleRepresentationObject> results = riSearchAdapter
					.getEntities(tuples);

			results = joinRepresentationsByStatus(results);

			tuples.close();

			logger.trace("getSimpleRepresentationObjects(" + contentAdapter
					+ ") => " + results.size() + " results");

			return results;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Gets a {@link SimpleRepresentationPreservationObject} with the specified
	 * PID from the properties in RELS-EXT.
	 * 
	 * @param srpoPID
	 *            the PID of the {@link SimpleRepresentationPreservationObject}.
	 * 
	 * @return a {@link SimpleRepresentationPreservationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public SimpleRepresentationPreservationObject getSimpleRepresentationPreservationObject(
			String srpoPID) throws NoSuchRODAObjectException,
			FedoraRISearchException {

		Filter filter = new Filter(
				new FilterParameter[] { new SimpleFilterParameter("pid",
						srpoPID) });

		ITQLContentAdapterEngine<SimpleRepresentationPreservationObjectAdapter, SimpleRepresentationPreservationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleRepresentationPreservationObjectAdapter, SimpleRepresentationPreservationObject>(
				new SimpleRepresentationPreservationObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleRepresentationPreservationObject(" + srpoPID
				+ ") ITQL Query: " + itqlQuery);

		SimpleRepresentationPreservationObject simpleRPO = null;
		try {

			TupleIterator tuples = getTuples(itqlQuery);

			simpleRPO = riSearchAdapter.getEntity(tuples);

			if (simpleRPO == null) {
				throw new NoSuchRODAObjectException(
						"SimpleRepresentationPreservationObject with PID "
								+ srpoPID + " doesn't exist in Fedora RI.");
			}

			tuples.close();

			logger.trace("getSimpleRepresentationPreservationObject(" + srpoPID
					+ ") => " + simpleRPO);

			return simpleRPO;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Returns the number of {@link RepresentationObject}s that match the given
	 * {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link RepresentationObject}s that match the given {@link Filter}
	 *         .
	 * @throws FedoraRISearchException
	 */
	public int getSimpleRepresentationPreservationObjectCount(Filter filter)
			throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleRepresentationPreservationObjectAdapter, SimpleRepresentationPreservationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleRepresentationPreservationObjectAdapter, SimpleRepresentationPreservationObject>(
				new SimpleRepresentationPreservationObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntityCountITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleRepresentationPreservationObjectCount(" + filter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			int count = riSearchAdapter.getEntityCount(tuples);
			tuples.close();

			logger.trace("getSimpleRepresentationPreservationObjectCount("
					+ filter + ") => " + count);

			return count;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Gets the list of {@link SimpleRepresentationPreservationObject}s that
	 * match the given {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleRepresentationPreservationObject} that
	 *         match the given {@link ContentAdapter}.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<SimpleRepresentationPreservationObject> getSimpleRepresentationPreservationObjects(
			ContentAdapter contentAdapter) throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleRepresentationPreservationObjectAdapter, SimpleRepresentationPreservationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleRepresentationPreservationObjectAdapter, SimpleRepresentationPreservationObject>(
				new SimpleRepresentationPreservationObjectAdapter(getUser()),
				contentAdapter);

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleRepresentationPreservationObjects("
				+ contentAdapter + ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			List<SimpleRepresentationPreservationObject> results = riSearchAdapter
					.getEntities(tuples);

			tuples.close();

			logger.trace("getSimpleRepresentationPreservationObjects("
					+ contentAdapter + ") => " + results.size() + " results");

			return results;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the {@link SimpleRepresentationPreservationObject} of a given
	 * representation.
	 * 
	 * @param roPID
	 *            the PID of the {@link RepresentationObject}.
	 * 
	 * @return a {@link SimpleRepresentationPreservationObject} of a given
	 *         representation or <code>null</code> if it doesn't exist.
	 * 
	 * @throws FedoraRISearchException
	 */
	public SimpleRepresentationPreservationObject getROPreservationObject(
			String roPID) throws FedoraRISearchException {

		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter("representationObjectPID", roPID));

		List<SimpleRepresentationPreservationObject> simpleRPOs = getSimpleRepresentationPreservationObjects(new ContentAdapter(
				filter, null, null));

		SimpleRepresentationPreservationObject simpleRPO = null;

		if (simpleRPOs.size() > 0) {
			simpleRPO = simpleRPOs.get(0);

			if (simpleRPOs.size() > 1) {
				logger.warn("Representation object has " + simpleRPOs.size()
						+ " preservation objects.");
			}

		}

		return simpleRPO;
	}

	/**
	 * Gets a {@link SimpleEventPreservationObject} with the specified PID from
	 * the properties in RELS-EXT.
	 * 
	 * @param sepoPID
	 *            the PID of the {@link SimpleEventPreservationObject}.
	 * 
	 * @return a {@link SimpleEventPreservationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws FedoraRISearchException
	 */
	public SimpleEventPreservationObject getSimpleEventPreservationObject(
			String sepoPID) throws NoSuchRODAObjectException,
			FedoraRISearchException {

		Filter filter = new Filter(
				new FilterParameter[] { new SimpleFilterParameter("pid",
						sepoPID) });

		ITQLContentAdapterEngine<SimpleEventPreservationObjectAdapter, SimpleEventPreservationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleEventPreservationObjectAdapter, SimpleEventPreservationObject>(
				new SimpleEventPreservationObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleEventPreservationObject(" + sepoPID
				+ ") ITQL Query: " + itqlQuery);

		SimpleEventPreservationObject simpleEPO = null;
		try {

			TupleIterator tuples = getTuples(itqlQuery);

			simpleEPO = riSearchAdapter.getEntity(tuples);

			if (simpleEPO == null) {
				throw new NoSuchRODAObjectException(
						"SimpleEventPreservationObject with PID " + sepoPID
								+ " doesn't exist in Fedora RI.");
			}

			tuples.close();

			logger.trace("getSimpleEventPreservationObject(" + sepoPID
					+ ") => " + simpleEPO);

			return simpleEPO;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Returns the number of {@link SimpleEventPreservationObject}s that match
	 * the given {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of
	 *         {@link SimpleEventPreservationObject}s that match the given
	 *         {@link Filter} .
	 * @throws FedoraRISearchException
	 */
	public int getSimpleEventPreservationObjectCount(Filter filter)
			throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleEventPreservationObjectAdapter, SimpleEventPreservationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleEventPreservationObjectAdapter, SimpleEventPreservationObject>(
				new SimpleEventPreservationObjectAdapter(getUser()),
				new ContentAdapter(filter, null, null));

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntityCountITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleEventPreservationObjectCount(" + filter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			int count = riSearchAdapter.getEntityCount(tuples);
			tuples.close();

			logger.trace("getSimpleEventPreservationObjectCount(" + filter
					+ ") => " + count);

			return count;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		} catch (NumberFormatException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Gets the list of {@link SimpleEventPreservationObject}s that match the
	 * given {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return an array of {@link SimpleEventPreservationObject} that match the
	 *         given {@link ContentAdapter}.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<SimpleEventPreservationObject> getSimpleEventPreservationObjects(
			ContentAdapter contentAdapter) throws FedoraRISearchException {

		ITQLContentAdapterEngine<SimpleEventPreservationObjectAdapter, SimpleEventPreservationObject> riSearchAdapter = new ITQLContentAdapterEngine<SimpleEventPreservationObjectAdapter, SimpleEventPreservationObject>(
				new SimpleEventPreservationObjectAdapter(getUser()),
				contentAdapter);

		String itqlPermissionConditions = getPermissionConditions(riSearchAdapter
				.getEntityAdapter().getEntityCountITQLQuerySubject());

		String itqlQuery = riSearchAdapter
				.getEntitiesITQLQuery(itqlPermissionConditions);

		logger.trace("getSimpleEventPreservationObjects(" + contentAdapter
				+ ") ITQL Query: " + itqlQuery);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			List<SimpleEventPreservationObject> results = riSearchAdapter
					.getEntities(tuples);

			tuples.close();

			logger.trace("getSimpleEventPreservationObjects(" + contentAdapter
					+ ") => " + results.size() + " results");

			return results;

		} catch (IOException e) {
			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);
		} catch (TrippiException e) {
			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the list of datastream IDs for an object with the given PID.
	 * 
	 * @param pid
	 *            the PID of the object
	 * 
	 * @return a {@link List} with all the datastream IDs.
	 * 
	 * @throws FedoraRISearchException
	 */
	public List<String> getRODAObjectDatastreamIDs(String pid)
			throws FedoraRISearchException {

		List<String> dsIDs = new ArrayList<String>();

		List<String> dsObjects = getTripleObjects(
				getRIFedoraObjectURIFromPID(pid),
				ITQL_PREDICATE_FEDORA_HAS_DATASTREAM);

		for (String dsObject : dsObjects) {

			String dsID = dsObject.split("/")[2];

			dsIDs.add(dsID);
		}

		logger.trace("getRODAObjectDatastreamIDs(" + pid + ") => "
				+ dsIDs.size() + " datastream(s)");

		return dsIDs;
	}

	public List<String> getRORepresentationPreservationObjectPIDs(String roPID)
			throws FedoraRISearchException {

		List<String> rpoPIDs = new ArrayList<String>();

		// (RPO "preservation-of" RO)
		List<String> rpos = getPIDsFromURIs(getTripleSubjects(
				ITQL_PREDICATE_RODA_PRESERVATION_OF,
				getRIFedoraObjectURIFromPID(roPID)));

		if (rpos != null && rpos.size() > 0) {

			String rpoPID = rpos.get(0);

			if (rpos.size() > 1) {
				logger.warn("RO " + roPID + " has " + rpos.size()
						+ " RPOs!!! Using the first one.");
			}

			while (rpoPID != null) {
				rpoPIDs.add(0, rpoPID);
				rpoPID = getPreviousRPO(rpoPID);
			}
		}

		return rpoPIDs;
	}

	private String getPreviousRPO(String rpoPID) throws FedoraRISearchException {

		String previousRPOPID = null;

		List<String> epos = getPIDsFromURIs(getTripleObjects(
				getRIFedoraObjectURIFromPID(rpoPID),
				ITQL_PREDICATE_RODA_DERIVED_FROM));

		if (epos != null && epos.size() > 0) {

			String epoPID = epos.get(0);

			if (epos.size() > 1) {
				logger.warn("RPO " + rpoPID + " has 'derived-from' "
						+ epos.size() + " EPOs!!! Using the first one.");
			}

			List<String> rpos = getPIDsFromURIs(getTripleObjects(
					getRIFedoraObjectURIFromPID(epoPID),
					ITQL_PREDICATE_RODA_PERFORMED_ON));

			if (rpos != null && rpos.size() > 0) {

				previousRPOPID = rpos.get(0);

				if (rpos.size() > 1) {
					logger.warn("EPO " + epoPID + " was 'performed-on' "
							+ rpos.size() + " RPOs!!! Using the first one.");
				}
			}

		}

		return previousRPOPID;
	}

	private List<String> getPIDsFromURIs(List<String> URIs) {

		List<String> PIDs = new ArrayList<String>();

		for (String uri : URIs) {
			PIDs.add(getPIDFromFedoraURI(uri));
		}

		return PIDs;
	}

	/**
	 * Gets {@link RODAObject}s from a {@link TupleIterator}.
	 * 
	 * @param tuples
	 *            the {@link TupleIterator}.
	 * 
	 * @return a {@link List} of {@link RODAObject}s.
	 * 
	 * @throws TrippiException
	 */
	private List<RODAObject> getRODAObjectsFromTuples(TupleIterator tuples)
			throws TrippiException {

		List<RODAObject> rodaObjects = new ArrayList<RODAObject>();

		while (tuples.hasNext()) {
			rodaObjects.add(new RODAObjectAdapter(getUser()).getEntity(tuples
					.next()));
		}

		return rodaObjects;
	}

	private List<SimpleRepresentationObject> joinRepresentationsByStatus(
			List<SimpleRepresentationObject> simpleROs) {

		List<SimpleRepresentationObject> srObjects = new ArrayList<SimpleRepresentationObject>();
		for (SimpleRepresentationObject sro : simpleROs) {

			if (srObjects.contains(sro)) {

				SimpleRepresentationObject sameSRO = srObjects.get(srObjects
						.indexOf(sro));

				List<String> joinStatuses = new ArrayList<String>(
						Arrays.asList(sameSRO.getStatuses()));
				joinStatuses.addAll(Arrays.asList(sro.getStatuses()));

				sameSRO.setStatuses(joinStatuses
						.toArray(new String[joinStatuses.size()]));

			} else {

				srObjects.add(sro);

			}
		}

		return srObjects;
	}

	/**
	 * Gets the subjects of triples in the form of &lt;subject, predicate,
	 * object&gt;.
	 * 
	 * @param predicate
	 *            the triple's predicate.
	 * 
	 * @param object
	 *            the triple's object.
	 * 
	 * @return a {@link List}<{@link String}> with the subjects of triples with
	 *         the form of &lt;subject, predicate, object&gt;.
	 * 
	 * @throws FedoraRISearchException
	 */
	private List<String> getTripleSubjects(String predicate, String object)
			throws FedoraRISearchException {

		List<String> subjects = new ArrayList<String>();

		String itqlQuery = String.format(
				"select $subject from <#ri> where $subject %1$s %2$s",
				predicate, object);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			while (tuples.hasNext()) {
				Map<String, Node> attributes = tuples.next();
				subjects.add(attributes.get("subject").stringValue());
			}

			tuples.close();

		} catch (IOException e) {

			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);

		}

		return subjects;
	}

	/**
	 * Gets the objects of triples in the form of &lt;subject, predicate,
	 * object&gt;.
	 * 
	 * @param subject
	 *            the triple's subject.
	 * @param predicate
	 *            the triple's predicate.
	 * 
	 * @return a {@link List} of {@link String}s with the names of the objects
	 *         of triples with the form of &lt;subject, predicate, object&gt;.
	 * 
	 * @throws FedoraRISearchException
	 */
	private List<String> getTripleObjects(String subject, String predicate)
			throws FedoraRISearchException {

		String itqlQuery = String.format(
				"select $object from <#ri> where %1$s %2$s $object", subject,
				predicate);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			List<String> objects = new ArrayList<String>();

			while (tuples.hasNext()) {
				Map<String, Node> attributes = tuples.next();
				objects.add(attributes.get("object").stringValue());
			}

			tuples.close();

			return objects;

		} catch (IOException e) {

			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);

		}

	}

	/**
	 * Gets the number of objects of triples in the form of &lt;subject,
	 * predicate, object&gt;.
	 * 
	 * @param subject
	 *            the triple's subject.
	 * @param predicate
	 *            the triple's predicate.
	 * 
	 * @return an <code>int</code> with the number of the objects of triples
	 *         with the form of &lt;subject, predicate, object&gt;.
	 * 
	 * @throws FedoraRISearchException
	 */
	private int getTripleObjectCount(String subject, String predicate)
			throws FedoraRISearchException {

		int count = 0;

		String itqlQuery = String.format(
				"select $object from <#ri> where %1$s %2$s $object", subject,
				predicate);

		try {

			TupleIterator tuples = getTuples(itqlQuery);
			count = tuples.count();

			// tuples.count() closes the iterator automatically. No need to
			// close it manually.
			// tuples.close();

		} catch (IOException e) {

			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);

		}

		return count;
	}

	/**
	 * Gets the predicates and objects of triples in the form of &lt;subject,
	 * predicate, object&gt;.
	 * 
	 * @param subject
	 *            the triple's subject.
	 * 
	 * @return a {@link Map <String, List<String>>} with the predicates and
	 *         respective objects of triples with the form of &lt;subject,
	 *         predicate, object&gt;.
	 * 
	 * @throws FedoraRISearchException
	 */
	private Map<String, Set<String>> getPredicateAndObjects(String subject)
			throws FedoraRISearchException {

		String itqlQuery = String
				.format("select $predicate $object from <#ri> where %1$s $predicate $object",
						subject);

		logger.trace("getPredicateAndObjects( " + itqlQuery + " )");

		Map<String, Set<String>> predicateAndObjects = new HashMap<String, Set<String>>();

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			while (tuples.hasNext()) {

				Map<String, Node> tuple = tuples.next();

				String predicate = tuple.get("predicate").stringValue();
				String object = tuple.get("object").stringValue();

				Set<String> objects = predicateAndObjects.get(predicate);
				if (objects == null) {
					objects = new HashSet<String>();
				}
				objects.add(object);
				predicateAndObjects.put(predicate, objects);
			}

		} catch (IOException e) {

			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);
		}

		return predicateAndObjects;
	}

	private TupleIterator getTuples(String itqlQuery) throws IOException {

		Map<String, String> params = new HashMap<String, String>();
		params.put("lang", "ITQL");
		params.put("query", itqlQuery);

		return fedoraClientUtility.getTuples(params);
	}

	/**
	 * @return the username
	 */
	private String getUsername() {
		return getUser().getName();
	}

	/**
	 * @return the user
	 */
	private User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	private void setUser(User user) {
		this.user = user;
	}

	/**
	 * Returns the Fedora URI for RI Search for a given PID.
	 * 
	 * @param pid
	 *            the PID.
	 * @return a {@link String} with the Fedora URI.
	 */
	private String getRIFedoraObjectURIFromPID(String pid) {
		String pidUri = this.fedoraClientUtility.getFedoraObjectURIFromPID(pid);
		pidUri = pidUri.replaceAll(">", "");
		return String.format("<%1$s>", pidUri);
	}

	private String getPIDFromFedoraURI(String fedoraURI) {
		String[] uri_pid = fedoraURI.split("/");
		if (uri_pid.length > 1) {
			return uri_pid[1];
		} else {
			return null;
		}
	}

	private String getPermissionConditions(String tripleSubject) {

		String conditions = String.format("%1$s %2$s '%3$s'", tripleSubject,
				ITQL_PREDICATE_RODA_PERMISSION_READ_USER, getUsername());

		for (String group : getUser().getAllGroups()) {
			conditions = String.format("%1$s or %2$s %3$s '%4$s'", conditions,
					tripleSubject, ITQL_PREDICATE_RODA_PERMISSION_READ_GROUP,
					group);
		}

		return " (" + conditions + ") ";
	}

	private static Configuration readConfiguration()
			throws ConfigurationException {

		if (configuration == null) {

			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
			propertiesConfiguration.setDelimiterParsingDisabled(true);

			propertiesConfiguration.load(FedoraRISearch.class
					.getResource("/roda-fedorarisearch.properties"));

			configuration = propertiesConfiguration;

			ITQL_PREDICATE_FEDORA_MODEL_LABEL = configuration
					.getString("itqlPredicateFedoraLabel");
			ITQL_PREDICATE_FEDORA_CONTENT_MODEL = configuration
					.getString("itqlPredicateFedoraContentModel");
			ITQL_PREDICATE_FEDORA_LAST_MODIFIED_DATE = configuration
					.getString("itqlPredicateFedoraLastModifiedDate");
			ITQL_PREDICATE_FEDORA_CREATED_DATE = configuration
					.getString("itqlPredicateFedoraCreatedDate");
			ITQL_PREDICATE_FEDORA_STATE = configuration
					.getString("itqlPredicateFedoraState");
			ITQL_PREDICATE_FEDORA_HAS_DATASTREAM = configuration
					.getString("itqlPredicateFedoraHasDatastream");

			ITQL_OBJECT_FEDORA_STATE_ACTIVE = configuration
					.getString("itqlObjectFedoraStateActive");
			ITQL_OBJECT_FEDORA_STATE_INACTIVE = configuration
					.getString("itqlObjectFedoraStateInactive");
			ITQL_OBJECT_FEDORA_STATE_DELETED = configuration
					.getString("itqlObjectFedoraStateDeleted");

			RDF_RODA_NAMESPACE = configuration.getString("rdfRodaNamespace");
			RDF_RODA_NAMESPACE_ALIAS = configuration
					.getString("rdfRodaNamespaceAlias");

			RDF_RODA_CHILD_OF = configuration.getString("rdfRodaChildOf");
			RDF_RODA_REPRESENTED_BY = configuration
					.getString("rdfRodaRepresentedBy");
			RDF_RODA_PRESERVED_BY = configuration
					.getString("rdfRodaPreservedBy");
			RDF_RODA_PRESERVATION_OF = configuration
					.getString("rdfRodaPreservationOf");
			RDF_RODA_PERFORMED_ON = configuration
					.getString("rdfRodaPerformedOn");
			RDF_RODA_PERFORMED_BY = configuration
					.getString("rdfRodaPerformedBy");
			RDF_RODA_DERIVED_FROM = configuration
					.getString("rdfRodaDerivedFrom");

			RDF_RODA_DESCRIPTION_LEVEL = configuration
					.getString("rdfRodaDescriptionLevel");
			RDF_RODA_DESCRIPTION_COUNTRYCODE = configuration
					.getString("rdfRodaDescriptionCountrycode");
			RDF_RODA_DESCRIPTION_REPOSITORYCODE = configuration
					.getString("rdfRodaDescriptionRepositorycode");
			RDF_RODA_DESCRIPTION_ID = configuration
					.getString("rdfRodaDescriptionId");
			RDF_RODA_DESCRIPTION_TITLE = configuration
					.getString("rdfRodaDescriptionTitle");
			RDF_RODA_DESCRIPTION_DATEINITIAL = configuration
					.getString("rdfRodaDescriptionDateinitial");
			RDF_RODA_DESCRIPTION_DATEFINAL = configuration
					.getString("rdfRodaDescriptionDatefinal");

			RDF_RODA_PERMISSION_READ_USER = configuration
					.getString("rdfRodaPermissionReadUser");
			RDF_RODA_PERMISSION_READ_GROUP = configuration
					.getString("rdfRodaPermissionReadGroup");

			RDF_RODA_PRODUCER_USER = configuration
					.getString("rdfRodaProducerUser");
			RDF_RODA_PRODUCER_GROUP = configuration
					.getString("rdfRodaProducerGroup");

			RDF_RODA_REPRESENTATION_STATUS = configuration
					.getString("rdfRodaRepresentationStatus");
			RDF_RODA_REPRESENTATION_TYPE = configuration
					.getString("rdfRodaRepresentationType");
			RDF_RODA_REPRESENTATION_SUBTYPE = configuration
					.getString("rdfRodaRepresentationSubtype");

			ITQL_PREDICATE_RODA_CHILD_OF = getITQLPredicate(RDF_RODA_CHILD_OF);
			ITQL_PREDICATE_RODA_REPRESENTED_BY = getITQLPredicate(RDF_RODA_REPRESENTED_BY);
			ITQL_PREDICATE_RODA_PRESERVED_BY = getITQLPredicate(RDF_RODA_PRESERVED_BY);
			ITQL_PREDICATE_RODA_PRESERVATION_OF = getITQLPredicate(RDF_RODA_PRESERVATION_OF);
			ITQL_PREDICATE_RODA_PERFORMED_ON = getITQLPredicate(RDF_RODA_PERFORMED_ON);
			ITQL_PREDICATE_RODA_PERFORMED_BY = getITQLPredicate(RDF_RODA_PERFORMED_BY);
			ITQL_PREDICATE_RODA_DERIVED_FROM = getITQLPredicate(RDF_RODA_DERIVED_FROM);

			ITQL_PREDICATE_RODA_DESCRIPTION_LEVEL = getITQLPredicate(RDF_RODA_DESCRIPTION_LEVEL);
			ITQL_PREDICATE_RODA_DESCRIPTION_COUNTRYCODE = getITQLPredicate(RDF_RODA_DESCRIPTION_COUNTRYCODE);
			ITQL_PREDICATE_RODA_DESCRIPTION_REPOSITORYCODE = getITQLPredicate(RDF_RODA_DESCRIPTION_REPOSITORYCODE);
			ITQL_PREDICATE_RODA_DESCRIPTION_ID = getITQLPredicate(RDF_RODA_DESCRIPTION_ID);
			ITQL_PREDICATE_RODA_DESCRIPTION_TITLE = getITQLPredicate(RDF_RODA_DESCRIPTION_TITLE);
			ITQL_PREDICATE_RODA_DESCRIPTION_DATEINITIAL = getITQLPredicate(RDF_RODA_DESCRIPTION_DATEINITIAL);
			ITQL_PREDICATE_RODA_DESCRIPTION_DATEFINAL = getITQLPredicate(RDF_RODA_DESCRIPTION_DATEFINAL);

			ITQL_PREDICATE_RODA_PERMISSION_READ_USER = getITQLPredicate(RDF_RODA_PERMISSION_READ_USER);
			ITQL_PREDICATE_RODA_PERMISSION_READ_GROUP = getITQLPredicate(RDF_RODA_PERMISSION_READ_GROUP);

			ITQL_PREDICATE_RODA_PRODUCER_USER = getITQLPredicate(RDF_RODA_PRODUCER_USER);
			ITQL_PREDICATE_RODA_PRODUCER_GROUP = getITQLPredicate(RDF_RODA_PRODUCER_GROUP);

			ITQL_PREDICATE_RODA_REPRESENTATION_STATUS = getITQLPredicate(RDF_RODA_REPRESENTATION_STATUS);
			ITQL_PREDICATE_RODA_REPRESENTATION_TYPE = getITQLPredicate(RDF_RODA_REPRESENTATION_TYPE);
			ITQL_PREDICATE_RODA_REPRESENTATION_SUBTYPE = getITQLPredicate(RDF_RODA_REPRESENTATION_SUBTYPE);

			RDF_TAG_CHILD_OF = getRodaRDFTag(RDF_RODA_CHILD_OF);
			RDF_TAG_REPRESENTED_BY = getRodaRDFTag(RDF_RODA_REPRESENTED_BY);
			RDF_TAG_PRESERVED_BY = getRodaRDFTag(RDF_RODA_PRESERVED_BY);
			RDF_TAG_PRESERVATION_OF = getRodaRDFTag(RDF_RODA_PRESERVATION_OF);
			RDF_TAG_PERFORMED_ON = getRodaRDFTag(RDF_RODA_PERFORMED_ON);
			RDF_TAG_PERFORMED_BY = getRodaRDFTag(RDF_RODA_PERFORMED_BY);
			RDF_TAG_DERIVED_FROM = getRodaRDFTag(RDF_RODA_DERIVED_FROM);

			RDF_TAG_DESCRIPTION_LEVEL = getRodaRDFTag(RDF_RODA_DESCRIPTION_LEVEL);
			RDF_TAG_DESCRIPTION_COUNTRYCODE = getRodaRDFTag(RDF_RODA_DESCRIPTION_COUNTRYCODE);
			RDF_TAG_DESCRIPTION_REPOSITORYCODE = getRodaRDFTag(RDF_RODA_DESCRIPTION_REPOSITORYCODE);
			RDF_TAG_DESCRIPTION_ID = getRodaRDFTag(RDF_RODA_DESCRIPTION_ID);
			RDF_TAG_DESCRIPTION_TITLE = getRodaRDFTag(RDF_RODA_DESCRIPTION_TITLE);
			RDF_TAG_DESCRIPTION_DATEINITIAL = getRodaRDFTag(RDF_RODA_DESCRIPTION_DATEINITIAL);
			RDF_TAG_DESCRIPTION_DATEFINAL = getRodaRDFTag(RDF_RODA_DESCRIPTION_DATEFINAL);

			RDF_TAG_PERMISSION_READ_USER = getRodaRDFTag(RDF_RODA_PERMISSION_READ_USER);
			RDF_TAG_PERMISSION_READ_GROUP = getRodaRDFTag(RDF_RODA_PERMISSION_READ_GROUP);

			RDF_TAG_PRODUCER_USER = getRodaRDFTag(RDF_RODA_PRODUCER_USER);
			RDF_TAG_PRODUCER_GROUP = getRodaRDFTag(RDF_RODA_PRODUCER_GROUP);

			RDF_TAG_REPRESENTATION_STATUS = getRodaRDFTag(RDF_RODA_REPRESENTATION_STATUS);
			RDF_TAG_REPRESENTATION_TYPE = getRodaRDFTag(RDF_RODA_REPRESENTATION_TYPE);
			RDF_TAG_REPRESENTATION_SUBTYPE = getRodaRDFTag(RDF_RODA_REPRESENTATION_SUBTYPE);
		}

		return configuration;
	}

	private static String getITQLPredicate(String property) {
		return String.format("<%1$s%2$s>", RDF_RODA_NAMESPACE, property);
	}

	private static String getRodaRDFTag(String property) {
		return String.format("%1$s:%2$s", RDF_RODA_NAMESPACE_ALIAS, property);
	}

	/*
	 * Maintenance methods
	 */

	public Map<String, String> getPreservedByRelationships()
			throws FedoraRISearchException {

		String itqlQuery = String.format(
				"select $roPID $rpoPID from <#ri> where $roPID %1$s $rpoPID",
				ITQL_PREDICATE_RODA_PRESERVED_BY);

		try {

			TupleIterator tuples = getTuples(itqlQuery);

			Map<String, String> relationships = new HashMap<String, String>();

			while (tuples.hasNext()) {
				Map<String, Node> attributes = tuples.next();

				relationships.put(getPIDFromFedoraURI(attributes.get("roPID")
						.stringValue()),
						getPIDFromFedoraURI(attributes.get("rpoPID")
								.stringValue()));
			}

			tuples.close();

			return relationships;

		} catch (IOException e) {

			logger.error("Error getting tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error getting tuples - "
					+ e.getMessage(), e);

		} catch (TrippiException e) {

			logger.error("Error iterating tuples - " + e.getMessage(), e);
			throw new FedoraRISearchException("Error iterating tuples - "
					+ e.getMessage(), e);

		}
	}

}
