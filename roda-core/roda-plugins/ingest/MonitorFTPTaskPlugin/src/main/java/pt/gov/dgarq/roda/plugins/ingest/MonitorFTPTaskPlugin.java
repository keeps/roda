package pt.gov.dgarq.roda.plugins.ingest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTask;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;
import pt.gov.dgarq.roda.util.TempDir;
import pt.gov.dgarq.roda.util.ZipUtility;

/**
 * This task monitors FTP directory for dropped new SIPs.
 * 
 * @author Rui Castro
 */
public class MonitorFTPTaskPlugin extends IngestTaskPlugin {
	static final private Logger logger = Logger
			.getLogger(MonitorFTPTaskPlugin.class);

	private final String name = "Ingest/Monitor FTP SIPs"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Monitor new SIPs uploaded by FTP and begins the ingest process."; //$NON-NLS-1$

	private File ftpDirectory = null;

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public MonitorFTPTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {

		Configuration configuration = getIngestManagerConfiguration();

		String ftpDirectoryKey = getClass().getName() + ".ftpDirectory"; //$NON-NLS-1$

		if (configuration.containsKey(ftpDirectoryKey)) {

			this.ftpDirectory = new File(configuration
					.getString(ftpDirectoryKey));

			logger.info("FTP directory is " + this.ftpDirectory); //$NON-NLS-1$

		} else {

			logger.error("Property ftpDirectory is not defined"); //$NON-NLS-1$
			throw new PluginException("Property ftpDirectory is not defined"); //$NON-NLS-1$

		}

		logger.info("init OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		logger.info("shutdown OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList();
	}

	/**
	 * @see IngestTaskPlugin#execute()
	 */
	@Override
	public Report execute() throws PluginException {

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("FTP Monitor plugin report"); //$NON-NLS-1$
		report.addAttribute(new Attribute("Start datetime", DateParser //$NON-NLS-1$
				.getIsoDate(new Date())));

		Map<String, List<File>> producerSIPs = getSIPFileOrDirectories(this.ftpDirectory);

		int userCount = producerSIPs.size();
		int sipCount = 0;
		int sipOkCount = 0;
		int sipFailCount = 0;

		for (String producer : producerSIPs.keySet()) {

			logger.debug("Inspecting SIPs dropped by producer " + producer); //$NON-NLS-1$

			for (File sipFile : producerSIPs.get(producer)) {

				sipCount++;

				logger.debug("Inspecting SIP " + sipFile.getName()); //$NON-NLS-1$

				ReportItem reportItem = new ReportItem("Producer " + producer //$NON-NLS-1$
						+ " - SIP " + sipFile.getName()); //$NON-NLS-1$

				if (sipFile.isDirectory()) {

					try {

						SIP sip = SIPUtility.readSIP(sipFile, true);

						logger.info("SIP read successfully from " + sipFile //$NON-NLS-1$
								+ " - registering SIP"); //$NON-NLS-1$

						try {

							SIPState registeredSIP = registerSIP(producer,
									sipFile.getName());

							reportItem.addAttribute(new Attribute("Outcome", //$NON-NLS-1$
									"success")); //$NON-NLS-1$
							reportItem.addAttribute(new Attribute(
									"Outcome details", //$NON-NLS-1$
									"SIP read sucessfully from file " //$NON-NLS-1$
											+ sipFile.getName()
											+ " and registered with ID " //$NON-NLS-1$
											+ registeredSIP.getId()));

							sipOkCount++;

						} catch (IngestException e) {
							logger.error("Error registering SIP " //$NON-NLS-1$
									+ sipFile.getName() + " - " //$NON-NLS-1$
									+ e.getMessage(), e);

							reportItem.addAttribute(new Attribute("Outcome", //$NON-NLS-1$
									"error")); //$NON-NLS-1$
							reportItem.addAttribute(new Attribute(
									"Outcome details", "Error registering SIP " //$NON-NLS-1$ //$NON-NLS-2$
											+ sipFile.getName()
											+ " - System details: " //$NON-NLS-1$
											+ e.getMessage()));
							sipFailCount++;
						}

					} catch (SIPException e) {

						logger.error("Error reading SIP from " + sipFile //$NON-NLS-1$
								+ " - " + e.getMessage() + "- SIP ignored"); //$NON-NLS-1$ //$NON-NLS-2$

						try {

							FileUtils.deleteDirectory(sipFile);

						} catch (IOException e1) {
							logger.error("Error deleting bad SIP directory " //$NON-NLS-1$
									+ sipFile + " - " + e.getMessage()); //$NON-NLS-1$
						}

						reportItem.addAttribute(new Attribute("Outcome", //$NON-NLS-1$
								"error")); //$NON-NLS-1$
						reportItem.addAttribute(new Attribute(
								"Outcome details", //$NON-NLS-1$
								"Error reading SIP from directory " //$NON-NLS-1$
										+ sipFile.getName()
										+ " - System details: " //$NON-NLS-1$
										+ e.getMessage()));

						sipFailCount++;
					}

				} else {

					try {

						File tempDir = TempDir
								.createUniqueTemporaryDirectory("sip"); //$NON-NLS-1$

						List<File> extractedFiles = ZipUtility
								.extractFilesFromZIP(sipFile, tempDir);

						logger.info("Extracted files successfully from " //$NON-NLS-1$
								+ sipFile + " - registering SIP"); //$NON-NLS-1$

						try {

							SIPState registeredSIP = registerSIP(producer,
									sipFile.getName());

							reportItem.addAttribute(new Attribute("Outcome", //$NON-NLS-1$
									"success")); //$NON-NLS-1$
							reportItem.addAttribute(new Attribute(
									"Outcome details", //$NON-NLS-1$
									"SIP read sucessfully from file " //$NON-NLS-1$
											+ sipFile.getName()
											+ " and registered with ID " //$NON-NLS-1$
											+ registeredSIP.getId()));

							sipOkCount++;

						} catch (IngestException e) {
							logger.error("Error registering SIP " //$NON-NLS-1$
									+ sipFile.getName() + " - " //$NON-NLS-1$
									+ e.getMessage(), e);

							reportItem.addAttribute(new Attribute("Outcome", //$NON-NLS-1$
									"error")); //$NON-NLS-1$
							reportItem.addAttribute(new Attribute(
									"Outcome details", "Error registering SIP " //$NON-NLS-1$ //$NON-NLS-2$
											+ sipFile.getName()
											+ " - System details: " //$NON-NLS-1$
											+ e.getMessage()));
							sipFailCount++;
						}

					} catch (IOException e) {
						logger.error("Error extracting file from SIP " //$NON-NLS-1$
								+ sipFile + " - " + e.getMessage() //$NON-NLS-1$
								+ "- Deleting file"); //$NON-NLS-1$

						if (!sipFile.delete()) {
							logger.error("Error deleting bad SIP file " //$NON-NLS-1$
									+ sipFile);
						}

						reportItem.addAttribute(new Attribute("Outcome", //$NON-NLS-1$
								"error")); //$NON-NLS-1$
						reportItem.addAttribute(new Attribute(
								"Outcome details", //$NON-NLS-1$
								"Error reading SIP from file " //$NON-NLS-1$
										+ sipFile.getName()
										+ " - System details: " //$NON-NLS-1$
										+ e.getMessage()));
						sipFailCount++;
					}

				}

				report.addItem(reportItem);
			}

		}

		report.addAttribute(new Attribute("User directories", Integer //$NON-NLS-1$
				.toString(userCount)));
		report.addAttribute(new Attribute("SIPs tested", Integer //$NON-NLS-1$
				.toString(sipCount)));
		report.addAttribute(new Attribute("SIPs accepted", Integer //$NON-NLS-1$
				.toString(sipOkCount)));
		report.addAttribute(new Attribute("SIPs rejected", Integer //$NON-NLS-1$
				.toString(sipFailCount)));

		report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
				.getIsoDate(new Date())));
		return report;
	}

	/**
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {

		String taskReport = ""; //$NON-NLS-1$

		File sipFile = new File(new File(this.ftpDirectory, sipState
				.getUsername()), sipState.getOriginalFilename());

		File directoryForNewState = getFinalStateLocation(sipState);

		logger.debug("SIP file is " + sipFile); //$NON-NLS-1$
		logger.debug("SIP final state directory is " + directoryForNewState); //$NON-NLS-1$

		if (sipFile.isDirectory()) {

			try {

				FileUtils.moveDirectory(sipFile, directoryForNewState);
				taskReport += String.format("SIP read from directory %s", //$NON-NLS-1$
						sipFile);

			} catch (IOException e) {
				return new IngestTaskResult(false, "Error moving SIP - " //$NON-NLS-1$
						+ e.getMessage());
			}

		} else {

			try {

				List<File> extractedFiles = ZipUtility.extractFilesFromZIP(
						sipFile, directoryForNewState);

				StringBuilder report = new StringBuilder(String.format(
						"Extracted SIP contents (%s):\n", sipFile.getName())); //$NON-NLS-1$

				for (File file : extractedFiles) {
					report.append("\t" + file + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				taskReport += report.toString();

			} catch (IOException e) {
				return new IngestTaskResult(false, "Error unpacking SIP - " //$NON-NLS-1$
						+ e.getMessage());
			}

		}

		return new IngestTaskResult(true, taskReport);
	}

	/**
	 * @see IngestTask#doCleanup(SIPState, IngestTaskResult)
	 */
	@Override
	protected void doCleanup(SIPState sipState, IngestTaskResult taskResult) {

		File sipFile = new File(new File(this.ftpDirectory, sipState
				.getUsername()), sipState.getOriginalFilename());

		if (sipFile.exists()) {

			if (sipFile.delete()) {
				logger.info("SIP deleted from FTP drop directory " + sipFile); //$NON-NLS-1$
			} else {
				logger.error("Erro deleting SIP " + sipState.getId() //$NON-NLS-1$
						+ " contents from FTP drop directory."); //$NON-NLS-1$
			}

		}

	}

	/**
	 * @see IngestTask#undoTask(SIPState, IngestTaskResult)
	 */
	@Override
	protected void undoTask(SIPState sip, IngestTaskResult taskResult) {
		// Remove SIP contents already copied to new final state directory.
		try {
			deleteSIPFilesFromState(sip, getFinalState());
		} catch (IngestTaskException e) {
			logger.warn("Exception performing undo on SIP " + sip.getId() //$NON-NLS-1$
					+ ". Ignoring and leaving the garbage behind.", e); //$NON-NLS-1$
		}
	}

	private Map<String, List<File>> getSIPFileOrDirectories(File directory) {

		Map<String, List<File>> producerSIPFiles = new HashMap<String, List<File>>();

		File[] producerDirectories = directory.listFiles();

		if (producerDirectories == null) {

			logger.trace("FTP drop directory has no producer directories"); //$NON-NLS-1$

		} else {

			logger.trace("FTP drop directory has " + producerDirectories.length //$NON-NLS-1$
					+ " producer directories"); //$NON-NLS-1$

			for (File producerDirectory : producerDirectories) {

				List<File> sips = null;

				if (producerDirectory.listFiles() != null) {
					sips = Arrays.asList(producerDirectory.listFiles());
				} else {
					sips = new ArrayList<File>();
				}

				logger.trace("Producer " + producerDirectory.getName() //$NON-NLS-1$
						+ " directory has " + sips.size() + " SIPs"); //$NON-NLS-1$ //$NON-NLS-2$

				producerSIPFiles.put(producerDirectory.getName(), sips);
			}
		}

		return producerSIPFiles;
	}

	private SIPState registerSIP(String producer, String originalFilename)
			throws IngestException {
		try {

			SIPState newSIP = registerSIP(producer, originalFilename,
					getFinalState());

			logger.info("SIP registered with id " + newSIP.getId()); //$NON-NLS-1$

			return newSIP;

		} catch (InvalidIngestStateException e) {
			logger.error("Error registering SIP " + originalFilename //$NON-NLS-1$
					+ " from user " + producer); //$NON-NLS-1$
			throw e;
		} catch (IngestRegistryException e) {
			logger.error("Error registering SIP " + originalFilename //$NON-NLS-1$
					+ " from user " + producer); //$NON-NLS-1$
			throw e;
		} catch (IngestTaskException e) {
			logger.error("Error registering SIP " + originalFilename //$NON-NLS-1$
					+ " from user " + producer); //$NON-NLS-1$
			throw e;
		}
	}

}
