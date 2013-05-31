package pt.gov.dgarq.roda.core.adapter.sql;

import org.apache.commons.lang.StringUtils;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 * @param <E>
 *            the entity being adapted.
 */
public class SQLRangeFilterParameter<EA extends SQLEntityAdapter<E>, E> extends
		SQLFilterParameterAdapter<EA, E> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public SQLRangeFilterParameter(EA entityAdapter,
			RangeFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Gets the SQL condition text for this filter parameter.
	 */
	@Override
	public String getSQLCondition() {

		String condition = null;

		String sqlColumnName = getEntityAdapter().getSQLColumnNameForAttribute(
				getFilterParameter().getName());

		if (!StringUtils.isBlank(getRangeFilterParameter().getFromValue())) {

			String sqlFromValue = getEntityAdapter().getSQLValueForAttribute(
					getFilterParameter().getName(),
					(Object) getRangeFilterParameter().getFromValue());

			condition = sqlColumnName + ">=" + sqlFromValue;
		}

		if (!StringUtils.isBlank(getRangeFilterParameter().getToValue())) {

			if (condition != null) {
				condition += " AND ";
			}

			String sqlToValue = getEntityAdapter().getSQLValueForAttribute(
					getFilterParameter().getName(),
					(Object) getRangeFilterParameter().getToValue());

			condition += sqlColumnName + "<=" + sqlToValue;
		}

		return condition;
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

	private RangeFilterParameter getRangeFilterParameter() {
		return (RangeFilterParameter) getFilterParameter();
	}

}
