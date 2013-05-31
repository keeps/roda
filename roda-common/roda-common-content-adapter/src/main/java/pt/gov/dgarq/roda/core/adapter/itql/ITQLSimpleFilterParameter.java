package pt.gov.dgarq.roda.core.adapter.itql;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 */
public class ITQLSimpleFilterParameter<EA extends ITQLEntityAdapter<E>, E>
		extends ITQLFilterParameterAdapter<EA> {

	/**
	 * Constructs a new {@link ITQLSimpleFilterParameter}.
	 * 
	 * @param entityAdapter
	 * @param simpleFilterParameter
	 */
	public ITQLSimpleFilterParameter(EA entityAdapter,
			SimpleFilterParameter simpleFilterParameter) {
		super(entityAdapter, simpleFilterParameter);
	}

	/**
	 * @see ITQLFilterParameterAdapter#getITQLCondition(String)
	 */
	@Override
	public String getITQLCondition(String itqlSubject) {

		String itqlObject = getEntityAdapter().getITQLObjectForAttribute(
				getFilterParameter().getName(),
				getSimpleFilterParameter().getValue());

		return getEntityAdapter().getITQLCondition(itqlSubject,
				getFilterParameter().getName(), itqlObject);
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
