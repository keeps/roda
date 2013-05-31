package pt.gov.dgarq.roda.core.adapter.jndi;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter for JNDI entities.
 */
public abstract class JndiFilterParameterAdapter<EA extends JndiEntityAdapter>
		extends FilterParameterAdapter<EA> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public JndiFilterParameterAdapter(EA entityAdapter,
			FilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Gets the JNDI filter for this filter parameter.
	 * 
	 * @return with the JNDI filter for this filter parameter.
	 */
	abstract public String getJndiFilter();

}
