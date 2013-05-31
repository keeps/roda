package pt.gov.dgarq.roda.core.scheduler.adapter;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.ContentAdapterEngine;
import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

/**
 * @author Rui Castro
 * 
 */
public class SchedulerContentAdapterEngine extends
		ContentAdapterEngine<TaskAdapter, Task> {

	static final private Logger logger = Logger
			.getLogger(SchedulerContentAdapterEngine.class);

	/**
	 * Constructs a new {@link SchedulerContentAdapterEngine}.
	 * 
	 * @param contentAdapter
	 */
	public SchedulerContentAdapterEngine(ContentAdapter contentAdapter) {
		super(new TaskAdapter(), contentAdapter);
	}

	/**
	 * @see ContentAdapterEngine#getFilterParameterAdapter(FilterParameter)
	 */
	@Override
	public FilterParameterAdapter<TaskAdapter> getFilterParameterAdapter(
			FilterParameter filterParameter) {

		TaskFilterParameterAdapter taskParameterAdapter = null;

		if (filterParameter == null) {
			// Ignore null parameter
			logger.warn("null FilterParameter found. Ignored.");
		} else if (filterParameter instanceof SimpleFilterParameter) {

			taskParameterAdapter = new TaskSimpleFilterParameterAdapter(
					(SimpleFilterParameter) filterParameter);

		} else if (filterParameter instanceof RangeFilterParameter) {

			taskParameterAdapter = new TaskRangeFilterParameterAdapter(
					(RangeFilterParameter) filterParameter);

		} else if (filterParameter instanceof OneOfManyFilterParameter) {

			taskParameterAdapter = new TaskOneOfManyFilterParameterAdapter(
					(OneOfManyFilterParameter) filterParameter);

		} else if (filterParameter instanceof RegexFilterParameter) {

			taskParameterAdapter = new TaskRegexFilterParameterAdapter(
					(RegexFilterParameter) filterParameter);

		} else {
			logger.warn("FilterParameters with type "
					+ filterParameter.getClass().getSimpleName()
					+ " are not supported");
		}

		return taskParameterAdapter;
	}

}
