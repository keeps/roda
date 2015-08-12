package pt.gov.dgarq.roda.core.scheduler.adapter;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.adapter.FilterParameterAdapter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;

/**
 * @author Rui Castro
 * 
 */
public class TaskRangeFilterParameterAdapter extends TaskFilterParameterAdapter {
	static final private Logger logger = Logger
			.getLogger(TaskRangeFilterParameterAdapter.class);

	/**
	 * @param filterParameter
	 */
	public TaskRangeFilterParameterAdapter(RangeFilterParameter filterParameter) {
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
			
			// FIXME
			passFilter = false;
//			String parameterName = getRangeFilterParameter().getName();
//			String fromValue = getRangeFilterParameter().getFromValue();
//			String toValue = getRangeFilterParameter().getToValue();
//
//			if ("name".equalsIgnoreCase(parameterName)) {
//
//				passFilter = task.getName().compareTo(fromValue) >= 0
//						&& task.getName().compareTo(toValue) <= 0;
//
//			} else if ("description".equalsIgnoreCase(parameterName)) {
//
//				passFilter = task.getDescription().compareTo(fromValue) >= 0
//						&& task.getDescription().compareTo(toValue) <= 0;
//
//			} else if ("username".equalsIgnoreCase(parameterName)) {
//
//				passFilter = task.getUsername().compareTo(fromValue) >= 0
//						&& task.getUsername().compareTo(toValue) <= 0;
//
//			} else if ("startDate".equalsIgnoreCase(parameterName)) {
//
//				try {
//
//					passFilter = task.getStartDate().compareTo(
//							DateParser.parse(fromValue)) >= 0
//							&& task.getStartDate().compareTo(
//									DateParser.parse(toValue)) <= 0;
//
//				} catch (InvalidDateException e) {
//					logger.warn(
//							"Error parsing date from RangeFilterParameter - "
//									+ e.getMessage() + ". Filter ignored.", e);
//					passFilter = true;
//				}
//
//			} else if ("repeatCount".equalsIgnoreCase(parameterName)) {
//
//				passFilter = task.getRepeatCount() >= Integer
//						.parseInt(fromValue)
//						&& task.getRepeatCount() <= Integer.parseInt(toValue);
//
//			} else if ("repeatInterval".equalsIgnoreCase(parameterName)) {
//
//				passFilter = task.getRepeatInterval() >= Integer
//						.parseInt(fromValue)
//						&& task.getRepeatInterval() <= Integer
//								.parseInt(toValue);
//
//			} else if ("scheduled".equalsIgnoreCase(parameterName)) {
//
//				// booleans are not in ranges!!!
//				passFilter = true;
//
//			} else if ("paused".equalsIgnoreCase(parameterName)) {
//
//				// booleans are not in ranges!!!
//				passFilter = true;
//
//			} else if ("running".equalsIgnoreCase(parameterName)) {
//
//				// booleans are not in ranges!!!
//				passFilter = true;
//
//			} else {
//				// parameterName is not a valid parameter. Ignore it!
//				passFilter = true;
//			}

		} else {
			passFilter = false;
		}

		return passFilter;
	}

	private RangeFilterParameter getRangeFilterParameter() {
		return (RangeFilterParameter) getFilterParameter();
	}

}
