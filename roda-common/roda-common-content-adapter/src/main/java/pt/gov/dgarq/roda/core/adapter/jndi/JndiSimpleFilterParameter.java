package pt.gov.dgarq.roda.core.adapter.jndi;

import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter for JNDI entities.
 */
public class JndiSimpleFilterParameter<EA extends JndiEntityAdapter> extends
		JndiFilterParameterAdapter<EA> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public JndiSimpleFilterParameter(EA entityAdapter,
			SimpleFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Gets the JNDI filter for this filter parameter.
	 * 
	 * @return with the JNDI filter for this filter parameter.
	 */
	@Override
	public String getJndiFilter() {
		String jndiAttributeName = getEntityAdapter()
				.getJndiAttributeNameForAttribute(
						getFilterParameter().getName());

		String jndiValue = getEntityAdapter().getJndiValueForAtribute(
				getFilterParameter().getName(),
				getSimpleFilterParameter().getValue());

		return "(" + jndiAttributeName + "=" + jndiValue + ")";
	}

	private SimpleFilterParameter getSimpleFilterParameter() {
		return (SimpleFilterParameter) getFilterParameter();
	}

	@Override
	public boolean canFilterValues() {
		return false;
	}

	@Override
	public boolean filterValue(Object attributes) {
		return false;
	}

}
