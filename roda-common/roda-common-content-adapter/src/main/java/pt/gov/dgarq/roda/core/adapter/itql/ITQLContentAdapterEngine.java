package pt.gov.dgarq.roda.core.adapter.itql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jrdf.graph.Node;
import org.trippi.TrippiException;
import org.trippi.TupleIterator;
import org.trippi.RDFUtil.FreeLiteral;

import pt.gov.dgarq.roda.core.adapter.ContentAdapterEngine;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ClassificationSchemeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;

/**
 * This is the {@link ContentAdapterEngine} for ITQL databases.
 * 
 * @author Rui Castro
 * 
 * @param <EA>
 *            the entity adapter type.
 * @param <E>
 *            the entity being adapted.
 */
public class ITQLContentAdapterEngine<EA extends ITQLEntityAdapter<E>, E>
		extends ContentAdapterEngine<EA, E> {
	private static final long serialVersionUID = 9199982131653638548L;

	static final private Logger logger = Logger
			.getLogger(ITQLContentAdapterEngine.class);

	/**
	 * Constructs a {@link ITQLContentAdapterEngine} cloning an existing
	 * {@link ContentAdapter}.
	 * 
	 * @param entityAdapter
	 * @param contentAdaptor
	 *            the {@link ContentAdapter} to clone.
	 */
	public ITQLContentAdapterEngine(EA entityAdapter,
			ContentAdapter contentAdaptor) {
		super(entityAdapter, contentAdaptor);
	}

	/**
	 * @see ContentAdapterEngine#getFilterParameterAdapter(FilterParameter)
	 */
	public ITQLFilterParameterAdapter<EA> getFilterParameterAdapter(
			FilterParameter filterParameter) {

		ITQLFilterParameterAdapter<EA> itqlParameterAdapter = null;

		if (filterParameter == null) {
			// Ignore null parameter
			logger.warn("null FilterParameter found. Ignored.");
		} else if (filterParameter instanceof SimpleFilterParameter) {
			itqlParameterAdapter = new ITQLSimpleFilterParameter<EA, E>(
					getEntityAdapter(), (SimpleFilterParameter) filterParameter);
		} else if (filterParameter instanceof OneOfManyFilterParameter) {
			itqlParameterAdapter = new ITQLOneOfManyFilterParameter<EA, E>(
					getEntityAdapter(),
					(OneOfManyFilterParameter) filterParameter);
		} else if (filterParameter instanceof RegexFilterParameter) {
			logger.warn("Regex filter not supported by "+this.getClass().getSimpleName()+". Ignoring "+filterParameter);
			// No regex
		} else if (filterParameter instanceof RangeFilterParameter) {
			logger.warn("Range filter not supported by "+this.getClass().getSimpleName()+". Ignoring "+filterParameter);
			// 
			// No range, yet!
		} else if (filterParameter instanceof ProducerFilterParameter) {
			itqlParameterAdapter = new ITQLProducerFilterParameter<EA, E>(
					getEntityAdapter(),
					(ProducerFilterParameter) filterParameter);
		} else if (filterParameter instanceof ClassificationSchemeFilterParameter) {
			itqlParameterAdapter = new ITQLClassificationSchemeFilterParameter<EA, E>(
					getEntityAdapter(),
					(ClassificationSchemeFilterParameter) filterParameter);
		} else {
		}

		return itqlParameterAdapter;
	}

	/**
	 * @param itqlBaseQuery
	 * @param itqlBaseConditions
	 * @param itqlFilterSubject
	 * 
	 * @return a {@link String} with the ITQL query
	 */
	public String getITQLQuery(String itqlBaseQuery, String itqlBaseConditions,
			String itqlFilterSubject) {
		return getITQLQuery(itqlBaseQuery, itqlBaseConditions,
				itqlFilterSubject, itqlFilterSubject);
	}

	/**
	 * @param itqlBaseQuery
	 * @param itqlBaseConditions
	 * @param itqlFilterSubject
	 * @param itqlProducerFilterSubject
	 * 
	 * @return a {@link String} with the ITQL query
	 */
	public String getITQLQuery(String itqlBaseQuery, String itqlBaseConditions,
			String itqlFilterSubject, String itqlProducerFilterSubject) {

		String itqlFilterText = getITQLQueryFilterConditions(itqlFilterSubject,
				itqlProducerFilterSubject);
		String itqlSorterText = getSorterITQLText(itqlFilterSubject);
		String itqlSublistText = getSublistITQLText();

		String itqlQuery = itqlBaseQuery + " where " + itqlBaseConditions;

		if (itqlFilterText != null) {
			itqlQuery += " and " + itqlFilterText;
		}
		if (itqlSorterText != null) {
			itqlQuery += " order by " + itqlSorterText;
		}
		if (itqlSublistText != null) {
			itqlQuery += " " + itqlSublistText;
		}

		return itqlQuery;
	}

	/**
	 * @param itqlExtraConditions
	 * 
	 * @return a {@link String} with the ITQL count query.
	 */
	public String getEntityCountITQLQuery(String itqlExtraConditions) {

		String itqlQuerySubject = getEntityAdapter()
				.getEntityCountITQLQuerySubject();

		String itqlBindingConditions = "";

		for (String attributeName : getEntityAdapter().getAttributeNames()) {

			// Get the binding condition
			String bindingCondition = getEntityAdapter()
					.getITQLBindingCondition(itqlQuerySubject, attributeName);

			if (!StringUtils.isBlank(bindingCondition)) {

				if (StringUtils.isBlank(itqlBindingConditions)) {
					itqlBindingConditions = bindingCondition;
				} else {
					itqlBindingConditions = itqlBindingConditions + " and "
							+ bindingCondition;
				}
			}
		}

		String itqlConditions = itqlBindingConditions + " and "
				+ itqlExtraConditions;

		String queryBase = String.format("select %1$s from <#ri>",
				itqlQuerySubject);

		String itqlInnerQuery = getITQLQuery(queryBase, itqlConditions,
				itqlQuerySubject);

		String itqlQuery = String.format(
				"select count(%1$s) from <#ri> where $s $p $o", itqlInnerQuery);

		return itqlQuery;
	}

	/**
	 * @param itqlExtraConditions
	 * 
	 * @return a {@link String} with the ITQL query.
	 */
	public String getEntitiesITQLQuery(String itqlExtraConditions) {

		String itqlQuerySubject = getEntityAdapter()
				.getEntityCountITQLQuerySubject();

		String itqlSelectSubjects = "";
		String itqlBindingConditions = "";

		for (String attributeName : getEntityAdapter().getAttributeNames()) {

			// Get the select subject
			String selectSubject = getEntityAdapter()
					.getITQLSubjectForAttribute(itqlQuerySubject, attributeName);
			if (!StringUtils.isBlank(selectSubject)) {
				if (StringUtils.isBlank(itqlSelectSubjects)) {
					itqlSelectSubjects = selectSubject;
				} else {
					itqlSelectSubjects = itqlSelectSubjects + " "
							+ selectSubject;
				}
			}

			// Get the binding condition
			String bindingCondition = getEntityAdapter()
					.getITQLBindingCondition(itqlQuerySubject, attributeName);
			if (!StringUtils.isBlank(bindingCondition)) {
				if (StringUtils.isBlank(itqlBindingConditions)) {
					itqlBindingConditions = bindingCondition;
				} else {
					itqlBindingConditions = itqlBindingConditions + " and "
							+ bindingCondition;
				}
			}
		}

		String itqlConditions = itqlBindingConditions + " and "
				+ itqlExtraConditions;

		String queryBase = String.format("select %1$s from <#ri>",
				itqlSelectSubjects);

		String itqlQuery = getITQLQuery(queryBase, itqlConditions,
				itqlQuerySubject);

		return itqlQuery;
	}

	/**
	 * Returns 1 entity from the given {@link TupleIterator}.
	 * 
	 * @param tuples
	 *            the {@link TupleIterator} for tuples returned by a ITQL query.
	 * 
	 * @return the entity being adapted or <code>null</code> it the
	 *         {@link TupleIterator} has no entities.
	 * 
	 * @throws TrippiException
	 */
	public E getEntity(TupleIterator tuples) throws TrippiException {

		E entity = null;

		if (tuples.hasNext()) {

			Map<String, Node> tuple = tuples.next();

			entity = getEntityAdapter().getEntity(tuple);
		}

		if (tuples.hasNext()) {
			logger.warn("getEntity(tuples) : tuples has more that 1 value!!!");
		}

		return entity;
	}

	/**
	 * Returns the number of entities in the given {@link TupleIterator}.
	 * 
	 * @param tuples
	 *            the {@link TupleIterator} for tuples returned by a ITQL query.
	 * 
	 * @return an <code>int</code> with the number of entities.
	 * 
	 * @throws TrippiException
	 */
	public int getEntityCount(TupleIterator tuples) throws TrippiException {

		int count = 0;
		if (tuples.hasNext()) {

			Map<String, Node> tuple = tuples.next();

			FreeLiteral countValue = (FreeLiteral) tuple.values().toArray()[0];
			count = (int) countValue.doubleValue();
		}

		if (tuples.hasNext()) {
			logger
					.warn("ITQL Query returned more than one object for count query!. Inform developers.");
		}

		return count;
	}

	/**
	 * Returns an entity from the given {@link Map} of tuples.
	 * 
	 * @param tuples
	 *            the {@link TupleIterator} for tuples returned by a ITQL query.
	 * 
	 * @return the entity being adapted by this adapter.
	 * 
	 * @throws TrippiException
	 */
	public List<E> getEntities(TupleIterator tuples) throws TrippiException {

		List<E> entities = new ArrayList<E>();

		while (tuples.hasNext()) {
			entities.add(getEntityAdapter().getEntity(tuples.next()));
		}

		return entities;
	}

	private String getITQLQueryFilterConditions(String itqlQuerySubject,
			String itqlProducerFilterSubject) {
		String itqlText = null;

		List<String> conditions = new ArrayList<String>();

		if (hasFilter()) {
			for (FilterParameter filterParameter : getFilter().getParameters()) {

				if (filterParameter != null
						&& getEntityAdapter().hasAttribute(
								filterParameter.getName())) {

					ITQLFilterParameterAdapter<EA> itqlParamAdapter = getFilterParameterAdapter(filterParameter);

					if (itqlParamAdapter != null) {

						conditions.add(itqlParamAdapter
								.getITQLCondition(itqlQuerySubject));

					}

				} else {
					// The entity doesn't have the attribute mentioned in the
					// filter.
					// Filter ignored.
				}
			}
		}

		if (conditions.size() > 0) {
			itqlText = conditions.get(0);
			for (int i = 1; i < conditions.size(); i++) {
				if (!StringUtils.isBlank(conditions.get(i))) {
					itqlText += " and " + conditions.get(i);
				}
			}
		}

		return itqlText;
	}

	private String getSorterITQLText(String itqlFilterSubject) {
		String itqlText = null;

		List<String> sortFields = new ArrayList<String>();
		if (hasSorter()) {

			for (SortParameter sortParameter : getSorter().getParameters()) {

				if (getEntityAdapter().hasAttribute(sortParameter.getName())) {

					String sortField = getEntityAdapter()
							.getITQLSubjectForAttribute(sortParameter.getName());

					if (sortParameter.isDescending()) {
						sortField += " desc ";
					} else {
						sortField += " asc ";
					}

					sortFields.add(sortField);
				}
			}

		}

		if (sortFields.size() > 0) {
			itqlText = sortFields.get(0);
			for (int i = 1; i < sortFields.size(); i++) {
				itqlText += " " + sortFields.get(i);
			}
		} else {
			itqlText = itqlFilterSubject + " desc ";
		}

		return itqlText;
	}

	private String getSublistITQLText() {
		String itqlText = null;

		if (hasSublist()) {

			if (getSublist().getMaximumElementCount() > 0) {
				itqlText = String.format(" limit %1$d ", getSublist()
						.getMaximumElementCount());
			} else {
				itqlText = "";
			}

			itqlText += String.format(" offset %1$d ", getSublist()
					.getFirstElementIndex());

		} else {
			itqlText = null;
		}

		return itqlText;
	}

}