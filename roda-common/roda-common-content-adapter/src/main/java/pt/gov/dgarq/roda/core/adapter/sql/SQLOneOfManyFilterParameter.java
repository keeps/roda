package pt.gov.dgarq.roda.core.adapter.sql;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 * @param <E>
 *            the entity being adapted.
 */
public class SQLOneOfManyFilterParameter<EA extends SQLEntityAdapter<E>, E>
		extends SQLFilterParameterAdapter<EA, E> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public SQLOneOfManyFilterParameter(EA entityAdapter,
			OneOfManyFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Gets the SQL condition text for this filter parameter.
	 */
	@Override
	public String getSQLCondition() {

		String sqlColumnName = getEntityAdapter().getSQLColumnNameForAttribute(
				getFilterParameter().getName());

		String[] values = getOneOfManyFilterParameter().getValues();

		String sqlCondition = "";
		if (values != null && values.length > 0) {

			sqlCondition += "(";

			String firstSQLValue = getEntityAdapter().getSQLValueForAttribute(
					getFilterParameter().getName(), (Object) values[0]);
			sqlCondition += sqlColumnName + "=" + firstSQLValue;

			for (int i = 1; i < values.length; i++) {

				String value = values[i];

				String sqlValue = getEntityAdapter().getSQLValueForAttribute(
						getFilterParameter().getName(), (Object) value);

				sqlCondition += " OR " + sqlColumnName + "=" + sqlValue;
			}

		}

		sqlCondition += ")";

		return sqlCondition;
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

	private OneOfManyFilterParameter getOneOfManyFilterParameter() {
		return (OneOfManyFilterParameter) getFilterParameter();
	}

}
