package pt.gov.dgarq.roda.core.services;

import java.util.Queue;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.logger.LoggerManager;

/**
 * @author Rui Castro
 */
public class RegisterLogEntriesThread extends Thread {
	private static final Logger logger = Logger
			.getLogger(RegisterLogEntriesThread.class);

	private LoggerManager loggerManager = null;
	private boolean terminate = false;

	private Queue<LogEntry> logEntryQueue;

	/**
	 * @param loggerManager
	 * @param logEntryQueue
	 */
	public RegisterLogEntriesThread(LoggerManager loggerManager,
			Queue<LogEntry> logEntryQueue) {
		this.loggerManager = loggerManager;
		this.logEntryQueue = logEntryQueue;
	}

	/**
	 * @see Thread#run()
	 */
	@Override
	public void run() {

		while (!terminate) {

			if (logEntryQueue.isEmpty()) {
				synchronized (logEntryQueue) {
					try {
						logEntryQueue.wait();
					} catch (InterruptedException e) {
						logger.warn("Interrupted - " + e.getMessage(), e);
					}
				}
			}

			while (!logEntryQueue.isEmpty()) {

				LogEntry logEntry = logEntryQueue.poll();

				try {

					this.loggerManager.addLogEntry(logEntry);

				} catch (LoggerException e) {
					logger.error("registerAction(" + logEntry.getAction()
							+ ",...) failed because of a LoggerException - "
							+ e.getMessage(), e);
				}
			}

		}
	}

	/**
	 * 
	 */
	public void finish() {

		this.terminate = true;

		synchronized (logEntryQueue) {
			logEntryQueue.notify();
		}
	}

}