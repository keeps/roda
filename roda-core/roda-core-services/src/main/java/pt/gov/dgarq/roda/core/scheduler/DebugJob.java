package pt.gov.dgarq.roda.core.scheduler;

import org.apache.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;

/**
 * @author Rui Castro
 * 
 */
public class DebugJob implements StatefulJob, InterruptableJob {

	static final private Logger logger = Logger.getLogger(DebugJob.class);

	private boolean interrupt = false;

	/**
	 * @see Job#execute(JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		// logger.info(getClass().getSimpleName() + " starting");

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		int count = 0;
		if (dataMap.containsKey("count")) {
			count = Integer.parseInt(dataMap.getString("count"));
		}

		logger.info(getClass().getSimpleName() + " isRecovering? "
				+ context.isRecovering());
		logger.info(getClass().getSimpleName() + " running " + count);

		while (!this.interrupt) {
			try {
				Thread.sleep(5 * 1000);
				logger.info(getClass().getSimpleName() + " running " + count
						+ " - 5 seconds tick");
			} catch (InterruptedException e) {
				logger.info("Sleep interrupted - " + e.getMessage(), e);
			}
		}
		logger.info(getClass().getSimpleName() + " interrupted");

		count++;
		context.getJobDetail().getJobDataMap().putAsString("count", count);
		// context.getJobDetail().setRequestsRecovery(true);

		logger.info(getClass().getSimpleName() + " updating DataMap "
				+ dataMap.getWrappedMap());
	}

	/**
	 * @see InterruptableJob#interrupt()
	 */
	public void interrupt() throws UnableToInterruptJobException {
		this.interrupt = true;
	}
}
