package pt.gov.dgarq.roda.core.adapter.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter for SQL entities.
 * @param <E>
 *            the entity being adapted.
 */
public class SQLRegexFilterParameter<EA extends SQLEntityAdapter<E>, E> extends
		SQLFilterParameterAdapter<EA, E> {

	private Pattern regexPattern = null;

	/**
	 * @param entityAdapter
	 * @param regexFilterParameter
	 */
	public SQLRegexFilterParameter(EA entityAdapter,
			RegexFilterParameter regexFilterParameter) {
		super(entityAdapter, regexFilterParameter);

		this.regexPattern = Pattern.compile(regexFilterParameter.getRegex());
	}

	/**
	 * @see FilterParameterAdapter#setFilterParameter (FilterParameter)
	 */
	@Override
	public void setFilterParameter(FilterParameter filterParameter) {
		super.setFilterParameter(filterParameter);

		this.regexPattern = Pattern.compile(getRegexFilterParameter()
				.getRegex());
	}

	/**
	 * @see SQLFilterParameterAdapter#getSQLCondition()
	 */
	@Override
	public String getSQLCondition() {
		return null;
	}

	/**
	 * @see FilterParameterAdapter#canFilterValues()
	 */
	@Override
	public boolean canFilterValues() {
		return true;
	}

	/**
	 * @see FilterParameterAdapter#filterValue(Object)
	 */
	@Override
	public boolean filterValue(Object value) {
		ResultSet resultSet = (ResultSet) value;

		boolean match = false;

		String sqlColumnName = getEntityAdapter().getSQLColumnNameForAttribute(
				getFilterParameter().getName());

		try {

			String valueAsString = resultSet.getString(sqlColumnName);

			if (value != null) {

				Matcher matcher = this.regexPattern.matcher(valueAsString);
				match = matcher.find();

			} else {
				match = false;
			}

		} catch (SQLException e) {
			match = false;
		}

		return match;
	}

	private RegexFilterParameter getRegexFilterParameter() {
		return (RegexFilterParameter) getFilterParameter();
	}

}
