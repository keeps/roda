package pt.gov.dgarq.roda.core.adapter.itql;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;

/**
 * @author Rui Castro
 * 
 * @param <EA>
 *            the entity adapter type.
 */
public class ITQLOneOfManyFilterParameter<EA extends ITQLEntityAdapter<E>, E> extends
		ITQLFilterParameterAdapter<EA> {

	/**
	 * @param entityAdapter
	 * @param filterParameter
	 */
	public ITQLOneOfManyFilterParameter(EA entityAdapter,
			OneOfManyFilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	/**
	 * 
	 * @see ITQLFilterParameterAdapter#getITQLCondition(String)
	 */
	@Override
	public String getITQLCondition(String itqlSubject) {

		String[] values = getOneOfManyFilterParameter().getValues();

		String itqlCondition = "";
		if (values != null && values.length > 0) {

			itqlCondition += "(";

			String firstItqlObject = getEntityAdapter()
					.getITQLObjectForAttribute(getFilterParameter().getName(),
							values[0]);

			itqlCondition += getEntityAdapter().getITQLCondition(itqlSubject,
					getFilterParameter().getName(), firstItqlObject);

			for (int i = 1; i < values.length; i++) {

				String value = values[i];

				String itqlObject = getEntityAdapter()
						.getITQLObjectForAttribute(
								getFilterParameter().getName(), value);

				itqlCondition += " or "
						+ getEntityAdapter().getITQLCondition(itqlSubject,
								getFilterParameter().getName(), itqlObject);
			}

		}

		itqlCondition += ")";

		return itqlCondition;
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
