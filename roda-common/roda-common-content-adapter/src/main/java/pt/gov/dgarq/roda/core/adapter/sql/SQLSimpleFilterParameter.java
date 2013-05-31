package pt.gov.dgarq.roda.core.adapter.sql;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter for SQL entities.
 * @param <E>
 *            the entity being adapted.
 */
public class SQLSimpleFilterParameter<EA extends SQLEntityAdapter<E>, E>
		extends SQLFilterParameterAdapter<EA, E> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public SQLSimpleFilterParameter(EA entityAdapter,
			SimpleFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Gets the SQL condition text for this filter parameter.
	 */
	@Override
	public String getSQLCondition() {

		String sqlColumnName = getEntityAdapter().getSQLColumnNameForAttribute(
				getFilterParameter().getName());

		String sqlValue = getEntityAdapter().getSQLValueForAttribute(
				getFilterParameter().getName(),
				(Object) getSimpleFilterParameter().getValue());

		return sqlColumnName + "=" + sqlValue;
	}

	/**
	 * @see FilterParameterAdapter#canFilterValues()
	 */
	@Override
	public boolean canFilterValues() {
		return false;
	}

	/**
	 * @see FilterParameterAdapter#filterValue(Object)
	 */
	@Override
	public boolean filterValue(Object value) {
		return false;
	}

	private SimpleFilterParameter getSimpleFilterParameter() {
		return (SimpleFilterParameter) getFilterParameter();
	}
}
