package pt.gov.dgarq.roda.core.services;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.NoSuchTaskException;
import pt.gov.dgarq.roda.core.common.NoSuchTaskInstanceException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.SchedulerException;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.scheduler.SchedulerManager;
import pt.gov.dgarq.roda.core.scheduler.RODASchedulerException;

/**
 * This is the implementation of the Scheduler service.
 * 
 * @author Rui Castro
 */
public class Scheduler extends RODAWebService {
	static final private Logger logger = Logger.getLogger(Scheduler.class);

	private SchedulerManager scheduler = null;

	/**
	 * Construct a new {@link Scheduler}.
	 * 
	 * @throws RODAServiceException
	 * @throws SchedulerException
	 */
	public Scheduler() throws RODAServiceException, SchedulerException {
		super();
		try {

			scheduler = SchedulerManager.getDefaultSchedulerManager();

		} catch (RODASchedulerException e) {
			logger.error("Error getting default RODA Scheduler - "
					+ e.getMessage(), e);
			throw new SchedulerException(
					"Error getting default RODA Scheduler - " + e.getMessage(),
					e);
		}
	}

	/**
	 * Returns the {@link Task} with the given name.
	 * 
	 * @param taskName
	 *            the name of the {@link Task}.
	 * 
	 * @return the {@link Task} with the specified name.
	 * 
	 * @throws NoSuchTaskException
	 *             if the there's no {@link Task} with the given name.
	 * @throws SchedulerException
	 *             if something error occurs.
	 */
	public Task getTask(String taskName) throws NoSuchTaskException,
			SchedulerException {

		try {

			Date start = new Date();
			Task task = this.scheduler.getTask(taskName);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.getTask", new String[] { "taskName",
					"" + taskName },
					"User %username% called method Scheduler.getTask("
							+ taskName + ")", duration);

			return task;

		} catch (RODASchedulerException e) {
			logger.debug("Error getting task - " + e.getMessage(), e);
			throw new SchedulerException("Error getting task - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the information about the {@link Task}s in the scheduler.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link Task}.
	 * 
	 * @throws SchedulerException
	 */
	public Task[] getTasks(ContentAdapter contentAdapter)
			throws SchedulerException {

		try {

			Date start = new Date();
			List<Task> tasks = this.scheduler.getTasks(contentAdapter);
			Task[] result = tasks.toArray(new Task[tasks.size()]);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.getTasks", new String[] {
					"contentAdapter", "" + contentAdapter },
					"User %username% called method Scheduler.getTasks("
							+ contentAdapter + ")", duration);

			return result;

		} catch (RODASchedulerException e) {
			logger.debug("Error getting tasks - " + e.getMessage(), e);
			throw new SchedulerException("Error getting tasks - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the number of {@link Task}s in the scheduler..
	 * 
	 * @param filter
	 * 
	 * @return and <code>int</code> with the number of {@link Task}s.
	 * 
	 * @throws SchedulerException
	 */
	public int getTaskCount(Filter filter) throws SchedulerException {

		try {

			Date start = new Date();
			int count = this.scheduler.getTaskCount(filter);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.getTaskCount", new String[] { "filter",
					"" + filter },
					"User %username% called method Scheduler.getTaskCount("
							+ filter + ")", duration);

			return count;

		} catch (RODASchedulerException e) {
			logger.debug("Error getting task count - " + e.getMessage(), e);
			throw new SchedulerException("Error getting task count - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Adds a new {@link Task} to the scheduler.
	 * 
	 * @param task
	 *            the {@link Task} to add.
	 * 
	 * @return the added {@link Task}.
	 * 
	 * @throws SchedulerException
	 */
	public Task addTask(Task task) throws SchedulerException {

		try {

			Date start = new Date();
			task.setUsername(getClientUser().getName());
			Task result = this.scheduler.addTask(task);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.addTask", new String[] { "task",
					"" + task },
					"User %username% called method Scheduler.addTask(" + task
							+ ")", duration);

			return result;

		} catch (RODASchedulerException e) {
			logger.debug("Error adding task - " + e.getMessage(), e);
			throw new SchedulerException(e.getMessage(), e);
		}
	}

	/**
	 * Modifies a task in the scheduler.
	 * 
	 * @param task
	 *            the info about the task to modify.
	 * 
	 * @return the modified task.
	 * @throws NoSuchTaskException
	 *             if the there's no {@link Task} with the given name.
	 * @throws SchedulerException
	 */
	public Task modifyTask(Task task) throws NoSuchTaskException,
			SchedulerException {

		try {

			Date start = new Date();
			task.setUsername(getClientUser().getName());
			Task result = this.scheduler.modifyTask(task);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.modifyTask", new String[] { "task",
					"" + task },
					"User %username% called method Scheduler.modifyTask("
							+ task + ")", duration);

			return result;

		} catch (RODASchedulerException e) {
			logger.debug("Error modifying task - " + e.getMessage(), e);
			throw new SchedulerException("Error modifying task - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Removes a task from the scheduler.
	 * 
	 * @param taskName
	 *            the info about the task to remove.
	 * 
	 * @throws NoSuchTaskException
	 *             if the there's no {@link Task} with the given name.
	 * @throws SchedulerException
	 */
	public void removeTask(String taskName) throws NoSuchTaskException,
			SchedulerException {

		try {

			Date start = new Date();
			this.scheduler.removeTask(taskName);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.removeTask", new String[] { "taskName",
					"" + taskName },
					"User %username% called method Scheduler.removeTask("
							+ taskName + ")", duration);

		} catch (RODASchedulerException e) {
			logger.debug("Error modifying task - " + e.getMessage(), e);
			throw new SchedulerException("Error modifying task - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Pause the specified task.
	 * 
	 * @param taskName
	 *            the name of the task to pause.
	 * @return the paused {@link Task}.
	 * 
	 * @throws NoSuchTaskException
	 * @throws SchedulerException
	 */
	public Task pauseTask(String taskName) throws NoSuchTaskException,
			SchedulerException {

		try {

			Date start = new Date();
			Task result = this.scheduler.pauseTask(taskName);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.pauseTask", new String[] { "taskName",
					"" + taskName },
					"User %username% called method Scheduler.pauseTask("
							+ taskName + ")", duration);

			return result;

		} catch (RODASchedulerException e) {
			logger.debug("Error pausing task - " + e.getMessage(), e);
			throw new SchedulerException("Error pausing task - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Resume a previously paused task with the specified name.
	 * 
	 * @param taskName
	 *            the name of the task to resume.
	 * 
	 * @return the resumed {@link Task}.
	 * 
	 * @throws NoSuchTaskException
	 * @throws SchedulerException
	 */
	public Task resumeTask(String taskName) throws NoSuchTaskException,
			SchedulerException {

		try {

			Date start = new Date();
			Task result = this.scheduler.resumeTask(taskName);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.resumeTask", new String[] { "taskName",
					"" + taskName },
					"User %username% called method Scheduler.resumeTask("
							+ taskName + ")", duration);

			return result;

		} catch (RODASchedulerException e) {
			logger.debug("Error resuming task - " + e.getMessage(), e);
			throw new SchedulerException("Error resuming task - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the {@link TaskInstance} with the given name.
	 * 
	 * @param taskInstanceID
	 *            the name of the {@link TaskInstance}.
	 * 
	 * @return the {@link TaskInstance} with the specified name.
	 * 
	 * @throws NoSuchTaskInstanceException
	 *             if the there's no {@link TaskInstance} with the given name.
	 * @throws SchedulerException
	 *             if something error occurs.
	 */
	public TaskInstance getTaskInstance(String taskInstanceID)
			throws NoSuchTaskInstanceException, SchedulerException {

		try {

			Date start = new Date();
			TaskInstance taskInstance = this.scheduler
					.getTaskInstance(taskInstanceID);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.getTaskInstance", new String[] {
					"taskInstanceID", "" + taskInstanceID },
					"User %username% called method Scheduler.getTaskInstance("
							+ taskInstanceID + ")", duration);

			return taskInstance;

		} catch (RODASchedulerException e) {
			logger.debug("Error getting task instance - " + e.getMessage(), e);
			throw new SchedulerException("Error getting task instance - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the information about the {@link TaskInstance}s in the scheduler.
	 * 
	 * @param contentAdapter
	 * 
	 * @return an array of {@link TaskInstance}.
	 * 
	 * @throws SchedulerException
	 */
	public TaskInstance[] getTaskInstances(ContentAdapter contentAdapter)
			throws SchedulerException {

		try {

			Date start = new Date();
			List<TaskInstance> tasks = this.scheduler
					.getTaskInstances(contentAdapter);
			TaskInstance[] taskInstances = tasks.toArray(new TaskInstance[tasks
					.size()]);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.getTaskInstances", new String[] {
					"contentAdapter", "" + contentAdapter },
					"User %username% called method Scheduler.getTaskInstances("
							+ contentAdapter + ")", duration);

			return taskInstances;

		} catch (RODASchedulerException e) {
			logger.debug("Error getting task instances - " + e.getMessage(), e);
			throw new SchedulerException("Error getting task instances - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the number of {@link TaskInstance}s in the scheduler.
	 * 
	 * @param filter
	 * 
	 * @return and <code>int</code> with the number of {@link TaskInstance}s.
	 * 
	 * @throws SchedulerException
	 */
	public int getTaskInstanceCount(Filter filter) throws SchedulerException {

		try {

			Date start = new Date();
			int count = this.scheduler.getTaskInstanceCount(filter);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Scheduler.getTaskInstanceCount", new String[] {
					"filter", "" + filter },
					"User %username% called method Scheduler.getTaskInstanceCount("
							+ filter + ")", duration);

			return count;

		} catch (RODASchedulerException e) {
			logger.debug("Error getting task instance count - "
					+ e.getMessage(), e);
			throw new SchedulerException("Error getting task instance count - "
					+ e.getMessage(), e);
		} catch (Throwable t) {
			logger.debug(t.getMessage(), t);
			throw new SchedulerException(t.getMessage(), t);
		}
	}

}
