package pt.gov.dgarq.roda.core.scheduler.adapter;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;

/**
 * @author Rui Castro
 */
public class TaskSimpleFilterParameterAdapter extends
		TaskFilterParameterAdapter {

	/**
	 * @param filterParameter
	 */
	public TaskSimpleFilterParameterAdapter(
			SimpleFilterParameter filterParameter) {
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

			String parameterName = getSimpleFilterParameter().getName();
			String parameterValue = getSimpleFilterParameter().getValue();

			if ("name".equalsIgnoreCase(parameterName)) {

				passFilter = task.getName().equals(parameterValue);

			} else if ("description".equalsIgnoreCase(parameterName)) {

				passFilter = task.getDescription().equals(parameterValue);

			} else if ("username".equalsIgnoreCase(parameterName)) {

				passFilter = task.getUsername().equals(parameterValue);

			} else if ("startDate".equalsIgnoreCase(parameterName)) {

				passFilter = task.getStartDate().equals(parameterValue);

			} else if ("repeatCount".equalsIgnoreCase(parameterName)) {

				String repeatCount = Integer.toString(task.getRepeatCount());
				passFilter = repeatCount.equals(parameterValue);

			} else if ("repeatInterval".equalsIgnoreCase(parameterName)) {

				String repeatInterval = Long.toString(task.getRepeatInterval());
				passFilter = repeatInterval.equals(parameterValue);

			} else if ("scheduled".equalsIgnoreCase(parameterName)) {

				passFilter = task.isScheduled() == Boolean
						.parseBoolean(parameterValue);

			} else if ("paused".equalsIgnoreCase(parameterName)) {

				passFilter = task.isPaused() == Boolean
						.parseBoolean(parameterValue);

			} else if ("running".equalsIgnoreCase(parameterName)) {

				passFilter = task.isRunning() == Boolean
						.parseBoolean(parameterValue);

			} else {
				// parameterName is not a valid parameter. Ignore it!
				passFilter = true;
			}

		} else {
			passFilter = false;
		}

		return passFilter;
	}

	private SimpleFilterParameter getSimpleFilterParameter() {
		return (SimpleFilterParameter) getFilterParameter();
	}

}
