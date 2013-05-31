package pt.gov.dgarq.roda.core.adapter.itql;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 */
public abstract class ITQLFilterParameterAdapter<EA extends ITQLEntityAdapter>
		extends FilterParameterAdapter<EA> {

	/**
	 * Constructs a new {@link ITQLFilterParameterAdapter} for specified
	 * {@link ITQLEntityAdapter} and {@link FilterParameter}.
	 * 
	 * @param entityAdapter
	 *            the {@link ITQLEntityAdapter}.
	 * @param filterParameter
	 *            the {@link FilterParameter}.
	 */
	public ITQLFilterParameterAdapter(EA entityAdapter,
			FilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * Returns a ITQL condition for the given subject.
	 * 
	 * @param itqlSubject
	 *            the condition subject.
	 * 
	 * @return a {@link String} with the ITQL condition.
	 */
	abstract public String getITQLCondition(String itqlSubject);

}
