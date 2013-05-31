package pt.gov.dgarq.roda.core.scheduler.adapter;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;

/**
 * @author Rui Castro
 */
public abstract class TaskFilterParameterAdapter extends
		FilterParameterAdapter<TaskAdapter> {

	/**
	 * @param filterParameter
	 */
	public TaskFilterParameterAdapter(FilterParameter filterParameter) {
		super(new TaskAdapter(), filterParameter);
	}

	/**
	 * @see FilterParameterAdapter#canFilterValues()
	 */
	@Override
	public boolean canFilterValues() {
		return true;
	}

}
