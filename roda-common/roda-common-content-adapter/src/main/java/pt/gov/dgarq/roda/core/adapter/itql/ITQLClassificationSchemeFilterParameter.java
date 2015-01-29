package pt.gov.dgarq.roda.core.adapter.itql;

import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ClassificationSchemeFilterParameter;

public class ITQLClassificationSchemeFilterParameter<EA extends ITQLEntityAdapter<E>, E>
		extends ITQLFilterParameterAdapter<EA> {

	public ITQLClassificationSchemeFilterParameter(EA entityAdapter,
			FilterParameter filterParameter) {
		super(entityAdapter, filterParameter);
	}

	@Override
	public String getITQLCondition(String itqlSubject) {
		return getEntityAdapter()
				.getITQLClassificationSchemeCondition(
						itqlSubject,
						getProducerClassesFilterParameter()
								.getClassificationSchemeId(),
						getProducerClassesFilterParameter()
								.getPossibleClassesPids());
	}

	@Override
	public boolean canFilterValues() {
		return false;
	}

	@Override
	public boolean filterValue(Object value) {
		return false;
	}

	private ClassificationSchemeFilterParameter getProducerClassesFilterParameter() {
		return (ClassificationSchemeFilterParameter) getFilterParameter();
	}

}
