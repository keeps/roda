package pt.gov.dgarq.roda.core.adapter.jndi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter for JNDI entities.
 */
public class JndiRegexFilterParameter<EA extends JndiEntityAdapter> extends
		JndiFilterParameterAdapter<EA> {

	private Pattern regexPattern = null;

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public JndiRegexFilterParameter(EA entityAdapter,
			RegexFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);

		this.regexPattern = Pattern.compile(filterParameter.getRegex());
	}

	/**
	 * @see JndiFilterParameterAdapter#getJndiFilter()
	 */
	@Override
	public String getJndiFilter() {
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

		Attributes attributes = (Attributes) value;

		boolean match = false;

		String jndiName = getEntityAdapter().getJndiAttributeNameForAttribute(
				getFilterParameter().getName());

		Attribute attr = attributes.get(jndiName);

		try {

			if (attr != null) {

				Matcher matcher = this.regexPattern.matcher(attr.get()
						.toString());
				match = matcher.find();

			} else {
				match = false;
			}

		} catch (NamingException e) {
			match = false;
		}

		return match;

	}

	@SuppressWarnings("unused")
	private RegexFilterParameter getRegexFilterParameter() {
		return (RegexFilterParameter) getFilterParameter();
	}

}
