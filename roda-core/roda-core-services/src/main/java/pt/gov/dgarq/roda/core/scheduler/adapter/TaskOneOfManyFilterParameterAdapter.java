package pt.gov.dgarq.roda.core.scheduler.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;

/**
 * @author Rui Castro
 * 
 */
public class TaskOneOfManyFilterParameterAdapter extends
		TaskFilterParameterAdapter {

	/**
	 * @param filterParameter
	 */
	public TaskOneOfManyFilterParameterAdapter(
			OneOfManyFilterParameter filterParameter) {
		super(filterParameter);
	}

	/**
	 * @see FilterParameterAdapter#filterValue(Object)
	 */
	@Override
	public boolean filterValue(Object value) {
		boolean passFilter;

		if (value instanceof Task) {
			Task task = (Task) value;

			String parameterName = getOneOfManyFilterParameter().getName();

			List<String> values;
			if (getOneOfManyFilterParameter().getValues() != null) {
				values = getOneOfManyFilterParameter()
						.getValues();
			} else {
				// Every task will fail to pass an empty set of possibilities
				values = new ArrayList<String>();
			}

			if ("name".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(task.getName());

			} else if ("description".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(task.getDescription());

			} else if ("username".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(task.getUsername());

			} else if ("startDate".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(DateParser.getIsoDate(task
						.getStartDate()));

			} else if ("repeatCount".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(Integer.toString(task
						.getRepeatCount()));

			} else if ("repeatInterval".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(Long.toString(task
						.getRepeatInterval()));

			} else if ("scheduled".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(Boolean.toString(task
						.isScheduled()));

			} else if ("paused".equalsIgnoreCase(parameterName)) {

				passFilter = values.contains(Boolean.toString(task.isPaused()));

			} else if ("running".equalsIgnoreCase(parameterName)) {

				passFilter = values
						.contains(Boolean.toString(task.isRunning()));

			} else {
				// parameterName is not a valid parameter. Ignore it!
				passFilter = true;
			}

		} else {
			passFilter = false;
		}

		return passFilter;
	}

	private OneOfManyFilterParameter getOneOfManyFilterParameter() {
		return (OneOfManyFilterParameter) getFilterParameter();
	}

}
