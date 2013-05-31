package pt.gov.dgarq.roda.core.adapter.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter for SQL entities.
 * @param <E>
 *            the entity being adapted.
 */
public class SQLLikeFilterParameter<EA extends SQLEntityAdapter<E>, E> extends
		SQLFilterParameterAdapter<EA, E> {

	/**
	 * @param entityAdapter
	 * @param likeFilterParameter
	 */
	public SQLLikeFilterParameter(EA entityAdapter,
			LikeFilterParameter likeFilterParameter) {
		super(entityAdapter, likeFilterParameter);
	}

	/**
	 * @see FilterParameterAdapter#setFilterParameter (FilterParameter)
	 */
	@Override
	public void setFilterParameter(FilterParameter filterParameter) {
		super.setFilterParameter(filterParameter);
	}

	/**
	 * @see SQLFilterParameterAdapter#getSQLCondition()
	 */
	@Override
	public String getSQLCondition() {
		String sqlColumnName = getEntityAdapter().getSQLColumnNameForAttribute(
				getFilterParameter().getName());

		String sqlValue = getEntityAdapter().getSQLValueForAttribute(
				getFilterParameter().getName(),
				(Object) getLikeFilterParameter().getExpression());

		return sqlColumnName + " LIKE " + sqlValue; //$NON-NLS-1$
	}

	/**
	 * @see FilterParameterAdapter#filterValue(Object)
	 */
	@Override
	public boolean filterValue(Object value) {
		return false;
	}

	/**
	 * @see FilterParameterAdapter#canFilterValues()
	 */
	@Override
	public boolean canFilterValues() {
		return false;
	}

	private LikeFilterParameter getLikeFilterParameter() {
		return (LikeFilterParameter) getFilterParameter();
	}

}
