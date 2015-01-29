package pt.gov.dgarq.roda.core.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import pt.gov.dgarq.roda.core.RodaWebApplication;
import pt.gov.dgarq.roda.core.common.NoSuchTaskException;
import pt.gov.dgarq.roda.core.common.NoSuchTaskInstanceException;
import pt.gov.dgarq.roda.core.data.PluginInfo;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.plugins.PluginManager;
import pt.gov.dgarq.roda.core.plugins.PluginManagerException;
import pt.gov.dgarq.roda.core.scheduler.adapter.SchedulerContentAdapterEngine;

/**
 * This class implements the RODA Core Scheduler.
 * 
 * @author Rui Castro
 */
public class SchedulerManager {

	static final private Logger logger = Logger
			.getLogger(SchedulerManager.class);

	private static SchedulerManager rodaScheduler = null;

	/**
	 * Returns the default {@link SchedulerManager}. If it doesn't exist, a new
	 * {@link SchedulerManager} it will be created and returned.
	 * 
	 * @return a {@link SchedulerManager}.
	 * 
	 * @throws RODASchedulerException
	 *             if the {@link SchedulerManager} couldn't be created.
	 */
	public synchronized static SchedulerManager getDefaultSchedulerManager()
			throws RODASchedulerException {
		if (rodaScheduler == null) {
			// Grab the Scheduler instance from the Factory
			rodaScheduler = new SchedulerManager();

			// Starts the scheduler.
			rodaScheduler.start();
		}
		return rodaScheduler;
	}

	private TaskInstanceDatabaseUtility databaseUtility = null;

	private Scheduler scheduler = null;
	private QuartzSchedulerListener quartzSchedulerListener = null;

	private PluginManager pluginManager = null;

	/**
	 * Creates a new {@link SchedulerManager}.
	 * 
	 * @throws RODASchedulerException
	 */
	private SchedulerManager() throws RODASchedulerException {

		try {
			Configuration configuration = RodaWebApplication.getConfiguration(
					getClass(), "scheduler.properties");

			String jdbcDriver = configuration.getString("jdbcDriver");
			String jdbcURL = configuration.getString("jdbcURL");
			String jdbcUsername = configuration.getString("jdbcUsername");
			String jdbcPassword = configuration.getString("jdbcPassword");

			if (databaseUtility == null) {
				databaseUtility = new TaskInstanceDatabaseUtility(jdbcDriver,
						jdbcURL, jdbcUsername, jdbcPassword);
			}

			this.pluginManager = PluginManager.getDefaultPluginManager();

			// Start quartz with custom configuration file
			String quartzPropertiesFile = configuration.getString(
					"quartzPropertiesFile", "quartz.properties");
                        String quartzPropertiesFilePath = new File(RodaWebApplication.RODA_CORE_CONFIG_DIRECTORY, quartzPropertiesFile)
                          .getAbsolutePath();

			logger.info("Using quartz properties "
					+ quartzPropertiesFilePath);

			// Grab the Scheduler instance from the Factory
                        this.scheduler = new StdSchedulerFactory(quartzPropertiesFilePath).getScheduler();

			this.quartzSchedulerListener = new QuartzSchedulerListener();
			this.scheduler.addSchedulerListener(quartzSchedulerListener);

		} catch (SchedulerException e) {
			logger.debug("Error creating Quartz scheduler - " + e.getMessage(),
					e);
			throw new RODASchedulerException(
					"Error creating Quartz scheduler - " + e.getMessage(), e);
		} catch (ConfigurationException e) {
			logger.error("Rrror reading configuration file", e);
			throw new RODASchedulerException(
					"Error reading configuration file", e);
		} catch (PluginManagerException e) {
			logger.debug("Error creating Plugin Manager - " + e.getMessage(), e);
			throw new RODASchedulerException("Error creating Plugin Manager - "
					+ e.getMessage(), e);
		}

		logger.info(getClass().getSimpleName() + " init OK");
	}

	/**
	 * Starts the scheduler.
	 * 
	 * @throws RODASchedulerException
	 */
	public void start() throws RODASchedulerException {

		try {

			if (this.scheduler != null) {

				if (this.scheduler.isStarted()) {
					logger.info("Quartz scheduler is already started");
				} else {
					logger.info("Quartz scheduler is not started. Starting");

					// start scheduler
					this.scheduler.start();
				}

			}

		} catch (SchedulerException e) {
			logger.debug(e.getMessage(), e);
			throw new RODASchedulerException(e.getMessage(), e);
		}
	}

	/**
	 * Stops the scheduler.
	 * 
	 * @throws RODASchedulerException
	 */
	public void stop() throws RODASchedulerException {

		try {

			if (this.scheduler != null) {

				// and shut it down
				// this.scheduler.shutdown();

				if (this.scheduler.isShutdown()) {
					logger.info("Scheduler is already shutdown");

				} else {
					// logger
					// .info("Scheduler is not shutdown. Interrupting Jobs and
					// shutting down");
					//
					// List currentlyExecutingJobs = this.scheduler
					// .getCurrentlyExecutingJobs();
					// Iterator iterator = currentlyExecutingJobs.iterator();
					// while (iterator.hasNext()) {
					//
					// JobExecutionContext context = (JobExecutionContext)
					// iterator
					// .next();
					// logger.info("Interrupting Job "
					// + context.getJobDetail().getName() + "."
					// + context.getJobDetail().getGroup());
					//
					// this.scheduler.interrupt(context.getJobDetail()
					// .getName(), context.getJobDetail().getGroup());
					// }

					logger.info("Shutting down Quartz scheduler (waiting for jobs to finish...)");
					this.scheduler.shutdown(true);
					logger.info("Quartz scheduler terminated.");
				}

			}

		} catch (SchedulerException e) {
			logger.debug(e.getMessage(), e);
			throw new RODASchedulerException(e.getMessage(), e);
		} catch (Throwable t) {
			logger.debug(t.getMessage(), t);
			throw new RODASchedulerException(t.getMessage(), t);
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
	 * @throws RODASchedulerException
	 *             if something error occurs.
	 */
	@SuppressWarnings("unchecked")
	public Task getTask(String taskName) throws NoSuchTaskException,
			RODASchedulerException {
		try {
			// Get the Job details
			JobDetail jobDetail = this.scheduler.getJobDetail(taskName,
					Scheduler.DEFAULT_GROUP);

			if (jobDetail != null) {

				// Get the Job trigger
				SimpleTrigger trigger = (SimpleTrigger) this.scheduler
						.getTrigger(taskName, Scheduler.DEFAULT_GROUP);

				// Get the Job DataMap
				Map<String, String> parametersMap = jobDetail.getJobDataMap()
						.getWrappedMap();

				String username = parametersMap
						.get(PluginJob.DATAMAP_USERNAME_KEY);

				String pluginID = parametersMap
						.get(PluginJob.DATAMAP_PLUGIN_ID_KEY);

				PluginInfo pluginInfo = this.pluginManager
						.getPluginInfo(pluginID);
				if (pluginInfo != null) {
					copyJobDataMapToPluginInfo(parametersMap, pluginInfo);
				} else {
					logger.warn("PluginInfo for plugin " + pluginID
							+ " is null");
				}

				boolean paused = isJobPaused(jobDetail);
				boolean running = isJobRunning(jobDetail);

				Task task = null;
				if (trigger != null) {

					// Quartz repeat interval is in milliseconds
					long repeatInterval = trigger.getRepeatInterval();
					if (repeatInterval != 0) {
						repeatInterval = repeatInterval / 1000;
					}

					boolean scheduled = trigger.mayFireAgain();

					task = new Task(jobDetail.getName(),
							jobDetail.getDescription(), username,
							trigger.getStartTime(), trigger.getRepeatCount(),
							repeatInterval, scheduled, paused, running,
							pluginInfo);
				} else {

					boolean scheduled = false;

					task = new Task(jobDetail.getName(),
							jobDetail.getDescription(), username, null, 0, 0,
							scheduled, paused, running, pluginInfo);
				}

				return task;

			} else {
				logger.debug("Task " + taskName + " doesn't exist");
				throw new NoSuchTaskException("Task " + taskName
						+ " doesn't exist");
			}

		} catch (SchedulerException e) {
			logger.debug(
					"Error getting task " + taskName + " - " + e.getMessage(),
					e);
			throw new RODASchedulerException("Error getting task " + taskName
					+ " - " + e.getMessage(), e);
		}

	}

	/**
	 * Returns the number of {@link Task}s in the scheduler.
	 * 
	 * @param filter
	 * 
	 * @return an <code>int</code> with the number of {@link Task}s in the
	 *         scheduler.
	 * @throws RODASchedulerException
	 *             if something goes wrong.
	 */
	public int getTaskCount(Filter filter) throws RODASchedulerException {

		try {
			SchedulerContentAdapterEngine contentAdapterEngine = new SchedulerContentAdapterEngine(
					new ContentAdapter(filter, null, null));

			List<Task> adaptedTasks = contentAdapterEngine
					.filter(getTasks(new ContentAdapter(filter, null, null)));

			return adaptedTasks.size();

		} catch (Throwable t) {
			logger.debug(t.getMessage(), t);
			throw new RODASchedulerException(t.getMessage(), t);
		}
	}

	/**
	 * Returns the {@link List} of {@link Task}s currently in the scheduler.
	 * 
	 * @param contentAdapter
	 * 
	 * @return a {@link List} of {@link Task}.
	 * 
	 * @throws RODASchedulerException
	 *             if something goes wrong.
	 */
	public List<Task> getTasks(ContentAdapter contentAdapter)
			throws RODASchedulerException {

		try {

			// Get the jobs in the scheduler
			String[] jobNames = this.scheduler
					.getJobNames(Scheduler.DEFAULT_GROUP);

			List<Task> tasks = new ArrayList<Task>();

			for (String jobName : jobNames) {
				tasks.add(getTask(jobName));
			}

			SchedulerContentAdapterEngine contentAdapterEngine = new SchedulerContentAdapterEngine(
					contentAdapter);
			List<Task> adaptedTasks = contentAdapterEngine.adaptEntities(tasks);

			return adaptedTasks;

		} catch (SchedulerException e) {
			logger.debug("Error getting tasks - " + e.getMessage(), e);
			throw new RODASchedulerException("Error getting tasks - "
					+ e.getMessage(), e);
		} catch (NoSuchTaskException e) {
			logger.debug("Error getting tasks - " + e.getMessage(), e);
			throw new RODASchedulerException("Error getting tasks - "
					+ e.getMessage(), e);
		} catch (Throwable t) {
			logger.debug(t.getMessage(), t);
			throw new RODASchedulerException(t.getMessage(), t);
		}
	}

	/**
	 * Adds a new {@link Task} to {@link SchedulerManager}.
	 * 
	 * @param task
	 *            the {@link Task} to add.
	 * 
	 * @return the new {@link Task}.
	 * 
	 * @throws RODASchedulerException
	 *             if something goes wrong.
	 */
	public Task addTask(Task task) throws RODASchedulerException {

		JobDetail taskJobDetail = new JobDetail(task.getName(),
				Scheduler.DEFAULT_GROUP, PluginJob.class);
		taskJobDetail.setDescription(task.getDescription());

		JobDataMap jobDataMap = taskJobDetail.getJobDataMap();
		copyPluginInfoToJobDataMap(task.getPluginInfo(), jobDataMap);
		jobDataMap.put(PluginJob.DATAMAP_USERNAME_KEY, task.getUsername());
		jobDataMap.put(PluginJob.DATAMAP_PLUGIN_ID_KEY, task.getPluginInfo()
				.getId());
		logger.trace("addTask() taskParameters=" + jobDataMap.getWrappedMap());

		SimpleTrigger taskTrigger = new SimpleTrigger(task.getName(),
				Scheduler.DEFAULT_GROUP);
		taskTrigger.setStartTime(task.getStartDate());
		taskTrigger.setRepeatCount(task.getRepeatCount());
		taskTrigger.setRepeatInterval(task.getRepeatInterval() * 1000);

		try {

			this.scheduler.scheduleJob(taskJobDetail, taskTrigger);

			return getTask(task.getName());

		} catch (SchedulerException e) {
			logger.debug("Error adding task - " + e.getMessage(), e);
			throw new RODASchedulerException("Error adding task - "
					+ e.getMessage(), e);
		} catch (NoSuchTaskException e) {
			logger.debug("Could not get added task - " + e.getMessage(), e);
			throw new RODASchedulerException("Could not get added task - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Modifies a {@link Task} already {@link SchedulerManager}.
	 * 
	 * @param task
	 *            the {@link Task} to modify.
	 * 
	 * @return the modified {@link Task}.
	 * 
	 * @throws NoSuchTaskException
	 *             if the there's no {@link Task} with the given name.
	 * @throws RODASchedulerException
	 *             if something error occurs.
	 */
	public Task modifyTask(Task task) throws NoSuchTaskException,
			RODASchedulerException {

		removeTask(task.getName());

		return addTask(task);
	}

	/**
	 * Removes a {@link Task} from the scheduler.
	 * 
	 * @param taskName
	 *            the name of the {@link Task} to remove.
	 * 
	 * @throws NoSuchTaskException
	 *             if the there's no {@link Task} with the given name.
	 * @throws RODASchedulerException
	 *             if something error occurs.
	 */
	public void removeTask(String taskName) throws NoSuchTaskException,
			RODASchedulerException {

		JobDetail jobDetail = null;

		try {

			jobDetail = this.scheduler.getJobDetail(taskName,
					Scheduler.DEFAULT_GROUP);

		} catch (SchedulerException e) {
			logger.debug(
					"Task " + taskName + " doesn't exist - " + e.getMessage(),
					e);
			throw new NoSuchTaskException("Task " + taskName
					+ " doesn't exist - " + e.getMessage(), e);
		}

		if (jobDetail != null) {

			try {

				this.scheduler.deleteJob(jobDetail.getName(),
						jobDetail.getGroup());

			} catch (SchedulerException e) {
				logger.debug(
						"Error removing task " + taskName + " - "
								+ e.getMessage(), e);
				throw new RODASchedulerException("Error removing task "
						+ taskName + " - " + e.getMessage(), e);
			}

		} else {
			logger.error("jobDetail is null. THIS IS PROBABLY A BUG");
			throw new RODASchedulerException(
					"jobDetail is null. Please inform developers");
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
	 * @throws RODASchedulerException
	 */
	public Task pauseTask(String taskName) throws NoSuchTaskException,
			RODASchedulerException {

		try {

			this.scheduler.pauseJob(taskName, Scheduler.DEFAULT_GROUP);

		} catch (SchedulerException e) {
			logger.debug(
					"Error pausing task " + taskName + " - " + e.getMessage(),
					e);
			throw new RODASchedulerException("Error pausing task " + taskName
					+ " - " + e.getMessage(), e);
		}

		return getTask(taskName);
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
	 * @throws RODASchedulerException
	 */
	public Task resumeTask(String taskName) throws NoSuchTaskException,
			RODASchedulerException {

		try {

			this.scheduler.resumeJob(taskName, Scheduler.DEFAULT_GROUP);

		} catch (SchedulerException e) {
			logger.debug(
					"Error pausing task " + taskName + " - " + e.getMessage(),
					e);
			throw new RODASchedulerException("Error pausing task " + taskName
					+ " - " + e.getMessage(), e);
		}

		return getTask(taskName);
	}

	/**
	 * Creates a new {@link TaskInstance}.
	 * 
	 * @param task
	 *            the {@link Task} for which to create the {@link TaskInstance}.
	 * 
	 * @return the new {@link TaskInstance}.
	 * 
	 * @throws RODASchedulerException
	 */
	public TaskInstance createTaskInstance(Task task)
			throws RODASchedulerException {

		TaskInstance taskInstance = new TaskInstance(null, task.getName(),
				task.getDescription(), task.getUsername(),
				task.getPluginInfo(), TaskInstance.STATE_RUNNING, 0,
				new Date(), null, null);

		try {

			return this.databaseUtility.insertTaskInstance(taskInstance);

		} catch (TaskInstanceRegistryException e) {
			logger.debug("Error creating TaskInstance - " + e.getMessage(), e);
			throw new RODASchedulerException("Error creating TaskInstance - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Updates information about a {@link TaskInstance}.
	 * 
	 * @param taskInstance
	 *            the {@link TaskInstance} to update.
	 * 
	 * @return the updated {@link TaskInstance}
	 * 
	 * @throws RODASchedulerException
	 */
	public TaskInstance updateTaskInstance(TaskInstance taskInstance)
			throws RODASchedulerException {
		try {

			return this.databaseUtility.updateTaskInstance(taskInstance);

		} catch (TaskInstanceRegistryException e) {
			logger.debug("Error creating TaskInstance - " + e.getMessage(), e);
			throw new RODASchedulerException("Error creating TaskInstance - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Returns the {@link TaskInstance} with the given ID.
	 * 
	 * @param taskInstanceID
	 *            the ID of the {@link TaskInstance}.
	 * 
	 * @return a {@link TaskInstance}.
	 * 
	 * @throws NoSuchTaskInstanceException
	 *             if a {@link TaskInstance} with specified ID doesn't exist.
	 * @throws RODASchedulerException
	 *             if some error occurs.
	 */
	public TaskInstance getTaskInstance(String taskInstanceID)
			throws RODASchedulerException, NoSuchTaskInstanceException {
		return this.databaseUtility.getTaskInstance(taskInstanceID);
	}

	/**
	 * Returns the number of {@link TaskInstance}s in the scheduler that respect
	 * the given {@link Filter}.
	 * 
	 * @param filter
	 *            the {@link ContentAdapter}'s {@link Filter}.
	 * 
	 * @return an <code>int</code> with the number of {@link TaskInstance}s.
	 * 
	 * @throws RODASchedulerException
	 *             if some error occurs.
	 */
	public int getTaskInstanceCount(Filter filter)
			throws RODASchedulerException {
		return this.databaseUtility.getTaskInstancesCount(filter);
	}

	/**
	 * Returns a {@link List} of {@link TaskInstance}s that respect the given
	 * {@link ContentAdapter}.
	 * 
	 * @param contentAdapter
	 *            the {@link ContentAdapter}.
	 * 
	 * @return a {@link List} of {@link TaskInstance}s.
	 * 
	 * @throws RODASchedulerException
	 *             if some error occurs.
	 */
	public List<TaskInstance> getTaskInstances(ContentAdapter contentAdapter)
			throws RODASchedulerException {
		return this.databaseUtility.getTaskInstances(contentAdapter);
	}

	private void copyPluginInfoToJobDataMap(PluginInfo pluginInfo,
			JobDataMap jobDataMap) {
		if (pluginInfo.getParameters() != null) {
			for (PluginParameter parameter : pluginInfo.getParameters()) {
				jobDataMap.put(parameter.getName(), parameter.getValue());
			}
		}
	}

	private void copyJobDataMapToPluginInfo(Map<String, String> parametersMap,
			PluginInfo pluginInfo) {
		if (pluginInfo.getParameters() != null) {
			for (PluginParameter parameter : pluginInfo.getParameters()) {
				parameter.setValue(parametersMap.get(parameter.getName()));
			}
		}
	}

	/**
	 * Verifies if a given job is paused. A job is considered to be paused when
	 * all it's {@link Trigger}s are paused.
	 * 
	 * @param jobDetail
	 *            the {@link JobDetail} of the job.
	 * 
	 * @return <code>true</code> if the job is paused, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws SchedulerException
	 */
	private boolean isJobPaused(JobDetail jobDetail) throws SchedulerException {

		Trigger[] triggers = this.scheduler.getTriggersOfJob(
				jobDetail.getName(), jobDetail.getGroup());

		boolean paused = true;

		if (triggers != null) {
			for (Trigger trigger : triggers) {
				int triggerState = this.scheduler.getTriggerState(
						trigger.getName(), trigger.getGroup());
				paused = paused && (triggerState == Trigger.STATE_PAUSED);
			}
		}

		return paused;
	}

	/**
	 * Verifies if a given job is running.
	 * 
	 * @param jobDetail
	 *            the {@link JobDetail} of the job.
	 * 
	 * @return <code>true</code> if the job is running, <code>false</code>
	 *         otherwise.
	 * 
	 * @throws SchedulerException
	 */
	@SuppressWarnings("unchecked")
	private boolean isJobRunning(JobDetail jobDetail) throws SchedulerException {

		List<JobExecutionContext> executingJobs = this.scheduler
				.getCurrentlyExecutingJobs();

		boolean running = false;

		if (executingJobs != null) {

			Iterator<JobExecutionContext> iterator = executingJobs.iterator();

			while (iterator.hasNext() && !running) {

				JobExecutionContext context = iterator.next();

				JobDetail runningJobDetail = context.getJobDetail();

				// JobDetail.equals() appears to have a bug. It always returns
				// false.
				// running = context.getJobDetail().equals(jobDetail);

				running = runningJobDetail.getName()
						.equals(jobDetail.getName())
						&& runningJobDetail.getGroup().equals(
								jobDetail.getGroup());
			}
		}

		return running;
	}

	/**
	 * {@link SchedulerManager} {@link SchedulerListener}
	 */
	class QuartzSchedulerListener implements SchedulerListener {

		/**
		 * @see SchedulerListener#jobScheduled(Trigger)
		 */
		public void jobScheduled(Trigger trigger) {
			logger.debug("jobScheduled(" + trigger + ")");
		}

		public void jobUnscheduled(String triggerName, String triggerGroup) {
			logger.debug("jobUnscheduled(" + triggerName + "," + triggerGroup
					+ ")");
		}

		public void triggerFinalized(Trigger trigger) {
			logger.debug("triggerFinalized(...)");
		}

		public void triggersPaused(String triggerName, String triggerGroup) {
			logger.debug("triggersPaused(" + triggerName + "," + triggerGroup
					+ ")");
		}

		public void triggersResumed(String triggerName, String triggerGroup) {
			logger.debug("triggersResumed(" + triggerName + "," + triggerGroup
					+ ")");
		}

		public void jobsPaused(String jobName, String jobGroup) {
			logger.debug("jobsPaused(" + jobName + "," + jobGroup + ")");
		}

		public void jobsResumed(String jobName, String jobGroup) {
			logger.debug("jobsResumed(" + jobName + "," + jobGroup + ")");
		}

		public void schedulerError(String msg, SchedulerException cause) {
			logger.debug("schedulerError(" + msg + "," + cause + ")");
		}

		public void schedulerShutdown() {
			logger.debug("schedulerShutdown()");
		}
	}

}
