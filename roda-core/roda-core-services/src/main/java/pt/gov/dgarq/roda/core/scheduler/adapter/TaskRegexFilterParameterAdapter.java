package pt.gov.dgarq.roda.core.scheduler.adapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;

/**
 * @author Rui Castro
 */
public class TaskRegexFilterParameterAdapter extends TaskFilterParameterAdapter {

	private Pattern regexPattern = null;

	/**
	 * @param filterParameter
	 */
	public TaskRegexFilterParameterAdapter(RegexFilterParameter filterParameter) {
		super(filterParameter);

		this.regexPattern = Pattern.compile(filterParameter.getRegex());
	}

	/**
	 * @see FilterParameterAdapter#filterValue(Object)
	 */
	@Override
	public boolean filterValue(Object value) {
		boolean passFilter;

		if (value instanceof Task) {
			Task task = (Task) value;

			String parameterName = getRegexFilterParameter().getName();

			String valueAsString;

			if ("name".equalsIgnoreCase(parameterName)) {

				valueAsString = task.getName();

			} else if ("description".equalsIgnoreCase(parameterName)) {

				valueAsString = task.getDescription();

			} else if ("username".equalsIgnoreCase(parameterName)) {

				valueAsString = task.getUsername();

			} else if ("startDate".equalsIgnoreCase(parameterName)) {

				valueAsString = DateParser.getIsoDate(task.getStartDate());

			} else if ("repeatCount".equalsIgnoreCase(parameterName)) {

				valueAsString = Integer.toString(task.getRepeatCount());

			} else if ("repeatInterval".equalsIgnoreCase(parameterName)) {

				valueAsString = Long.toString(task.getRepeatInterval());

			} else if ("scheduled".equalsIgnoreCase(parameterName)) {

				valueAsString = Boolean.toString(task.isScheduled());

			} else if ("paused".equalsIgnoreCase(parameterName)) {

				valueAsString = Boolean.toString(task.isPaused());

			} else if ("running".equalsIgnoreCase(parameterName)) {

				valueAsString = Boolean.toString(task.isRunning());

			} else {
				// parameterName is not a valid parameter. Ignore it!
				valueAsString = null;
			}

			if (value != null) {

				Matcher matcher = this.regexPattern.matcher(valueAsString);
				passFilter = matcher.find();

			} else {
				passFilter = false;
			}

		} else {
			passFilter = false;
		}

		return passFilter;
	}

	private RegexFilterParameter getRegexFilterParameter() {
		return (RegexFilterParameter) getFilterParameter();
	}

}
