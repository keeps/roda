package pt.gov.dgarq.roda.core.scheduler;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import pt.gov.dgarq.roda.core.common.InvalidParameterException;
import pt.gov.dgarq.roda.core.common.NoSuchTaskException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.plugins.PluginManager;
import pt.gov.dgarq.roda.core.plugins.PluginManagerException;
import pt.gov.dgarq.roda.core.reports.ReportManager;
import pt.gov.dgarq.roda.core.reports.ReportManagerException;
import pt.gov.dgarq.roda.core.reports.ReportRegistryException;

/**
 * @author Rui Castro
 */
public class PluginJob implements StatefulJob {
	private static final long serialVersionUID = 5824317013537695666L;

	private static Logger logger = Logger.getLogger(PluginJob.class);

	protected static final String DATAMAP_USERNAME_KEY = PluginJob.class
			.getName()
			+ "#username";
	protected static final String DATAMAP_PLUGIN_ID_KEY = PluginJob.class
			.getName()
			+ "#pluginID";

	/**
	 * @see Job#execute(JobExecutionContext)
	 */
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		logger.debug("Executing task " + context.getJobDetail().getName());

		String pluginClassname = context.getMergedJobDataMap().getString(
				DATAMAP_PLUGIN_ID_KEY);

		PluginManager pluginManager = null;
		ReportManager reportManager = null;
		try {

			pluginManager = PluginManager.getDefaultPluginManager();
			reportManager = ReportManager.getDefaultReportManager();

		} catch (PluginManagerException e) {
			logger.debug("Error getting Plugin Manager - " + e.getMessage(), e);
			throw new JobExecutionException("Error getting Plugin Manager - "
					+ e.getMessage(), e);
		} catch (ReportManagerException e) {
			logger.debug("Error getting Report Manager - " + e.getMessage(), e);
			throw new JobExecutionException("Error getting Report Manager - "
					+ e.getMessage(), e);
		}

		try {

			Plugin plugin = pluginManager.getPlugin(pluginClassname);

			if (plugin == null) {
				throw new JobExecutionException(pluginClassname
						+ " is not a valid plugin class name");
			} else {

				logger.debug("Setting plugin " + plugin.getName()
						+ " parameters "
						+ context.getMergedJobDataMap().getWrappedMap());

				plugin.setParameterValues(context.getMergedJobDataMap()
						.getWrappedMap());

				logger.debug("Starting execution of plugin " + plugin.getName()
						+ " with parameters "
						+ context.getMergedJobDataMap().getWrappedMap());

				SchedulerManager scheduler = SchedulerManager
						.getDefaultSchedulerManager();

				// Gets the current Task
				Task task = scheduler.getTask(context.getJobDetail().getName());

				// Creates a new TaskInstante for the Task
				TaskInstance taskInstance = scheduler.createTaskInstance(task);

				Report report = null;
				try {

					// Execute the plugin and collect the repor
					report = plugin.execute();

				} catch (PluginException e) {

					logger.debug("Error executing plugin - " + e.getMessage(),
							e);

					report = e.getReport();

					if (report != null) {
						logger.info("Using report contained in exception");
					}

				} catch (Throwable t) {

					logger.warn("Unexpected exception executing plugin - "
							+ t.getMessage(), t);

					report = new Report();
					report.setType(Report.TYPE_PLUGIN_REPORT);
					report.setTitle("Plugin error report");
					report.addAttribute(new Attribute("Plugin error", t
							.getMessage()));
				}

				// Update TaskInstance information
				taskInstance.setCompletePercentage(100f);
				taskInstance.setFinishDate(new Date());
				taskInstance.setState(TaskInstance.STATE_STOPPED);

				if (report != null) {
					try {

						// Insert the report in the ReportManager
						Report insertedReport = reportManager
								.insertReport(report);

						taskInstance.setReportID(insertedReport.getId());

					} catch (ReportRegistryException e) {
						logger.error("Error inserting Report - "
								+ e.getMessage(), e);
						logger.error("Task instance report is:\n"
								+ report.toString());
					}
				}

				// Update the TaskInstance in RODA Scheduler
				scheduler.updateTaskInstance(taskInstance);
			}

		} catch (InvalidParameterException e) {
			logger.debug("Error setting plugin parameters - " + e.getMessage(),
					e);
			throw new JobExecutionException(
					"Error setting plugin parameters - " + e.getMessage(), e);
		} catch (RODASchedulerException e) {
			logger.debug(e.getMessage(), e);
			throw new JobExecutionException(e.getMessage(), e);
		} catch (NoSuchTaskException e) {
			logger.debug(e.getMessage(), e);
			throw new JobExecutionException(e.getMessage(), e);
		}

	}
}
