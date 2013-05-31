package pt.gov.dgarq.roda.core.scheduler.adapter;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.adapter.SortParameterComparator;
import pt.gov.dgarq.roda.core.data.Task;

/**
 * @author Rui Castro
 */
public class TaskAdapter implements SortParameterComparator<Task> {
	static final private Logger logger = Logger.getLogger(TaskAdapter.class);

	/*
	 * private String name = null; private String description = null;
	 * 
	 * private String username = null;
	 * 
	 * private Date startDate = null; private int repeatCount = 0; private long
	 * repeatInterval = 0;
	 * 
	 * private boolean scheduled = false; private boolean paused = false;
	 * private boolean running = false;
	 * 
	 * private PluginInfo pluginInfo = null;
	 */

	/**
	 * @see SortParameterComparator#canSortEntities()
	 */
	public boolean canSortEntities() {
		return true;
	}

	/**
	 * @param task1
	 *            the first task
	 * @param task2
	 *            the second task
	 * @param attributeName
	 *            the name of the attribute to compare.
	 * @return &lt;0, 0 or &gt;0 if the first entity is less than, equal or
	 *         greater then the second entity.
	 * 
	 * @see SortParameterComparator#compare(Object, Object, String)
	 */
	public int compare(Task task1, Task task2, String attributeName) {

		int result = 0;

		if ("name".equalsIgnoreCase(attributeName)) {

			result = task1.getName().compareToIgnoreCase(task2.getName());

		} else if ("description".equalsIgnoreCase(attributeName)) {

			result = task1.getDescription().compareToIgnoreCase(
					task2.getDescription());

		} else if ("username".equalsIgnoreCase(attributeName)) {

			result = task1.getUsername().compareToIgnoreCase(
					task2.getUsername());

		} else if ("startDate".equalsIgnoreCase(attributeName)) {

			result = task1.getStartDate().compareTo(task2.getStartDate());

		} else if ("repeatCount".equalsIgnoreCase(attributeName)) {

			result = new Integer(task1.getRepeatCount()).compareTo(task2
					.getRepeatCount());

		} else if ("repeatInterval".equalsIgnoreCase(attributeName)) {

			result = new Long(task1.getRepeatInterval()).compareTo(task2
					.getRepeatInterval());

		} else if ("scheduled".equalsIgnoreCase(attributeName)) {

			result = new Boolean(task1.isScheduled()).compareTo(task2
					.isScheduled());

		} else if ("paused".equalsIgnoreCase(attributeName)) {

			result = new Boolean(task1.isPaused()).compareTo(task2.isPaused());

		} else if ("running".equalsIgnoreCase(attributeName)) {

			result = new Boolean(task1.isRunning())
					.compareTo(task2.isRunning());

		} else {
			// don't know the attribute name.
			logger.warn("Unknon attribute name '" + attributeName + "'");
		}

		return result;
	}
}
