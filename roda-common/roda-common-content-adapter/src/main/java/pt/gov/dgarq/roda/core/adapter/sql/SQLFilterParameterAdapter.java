package pt.gov.dgarq.roda.core.adapter.sql;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 * @param <E>
 *            the entity being adapted.
 */
public abstract class SQLFilterParameterAdapter<EA extends SQLEntityAdapter<E>, E>
		extends FilterParameterAdapter<EA> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public SQLFilterParameterAdapter(EA entityAdapter,
			FilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Gets the SQL condition text for this filter parameter.
	 * 
	 * @return with the SQL condition text for this filter parameter.
	 */
	abstract public String getSQLCondition();

}
