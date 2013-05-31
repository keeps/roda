package pt.gov.dgarq.roda.core.adapter.itql;

import java.util.Arrays;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;

/**
 * @author Rui Castro
 * @param <EA>
 *            the entity adapter type.
 */
public class ITQLProducerFilterParameter<EA extends ITQLEntityAdapter<E>, E>
		extends ITQLFilterParameterAdapter<EA> {
	static final private Logger logger = Logger
			.getLogger(ITQLProducerFilterParameter.class);

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public ITQLProducerFilterParameter(EA entityAdapter,
			ProducerFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * @see ITQLFilterParameterAdapter#getITQLCondition(String)
	 */
	@Override
	public String getITQLCondition(String itqlSubject) {

		return getEntityAdapter().getITQLProducerCondition(itqlSubject,
				getProducerFilterParameter().getUsername(),
				Arrays.asList(getProducerFilterParameter().getGroups()));

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

	private ProducerFilterParameter getProducerFilterParameter() {
		return (ProducerFilterParameter) getFilterParameter();
	}

}
