package pt.gov.dgarq.roda.plugins.ingest.virus;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTask;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;

/**
 * This task performs a virus check on the files of a {@link SIPState}.
 * 
 * The {@link AntiVirus} used depends on the value of
 * <code>.antiVirusClassname</code> property in the configuration. If this
 * property is not specified in the configuration, the default antivirus (
 * {@link AVGAntiVirus}) is used.
 * 
 * This task will report an error if the anti-virus reports anything about the
 * files.
 * 
 * @author Rui Castro
 */
public class CheckVirusTaskPlugin extends IngestTaskPlugin {
	static final private Logger logger = Logger
			.getLogger(CheckVirusTaskPlugin.class);

	private final String name = "Ingest/Virus check"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Verifies if a SIP is free of virus."; //$NON-NLS-1$

	private final static String ANTI_VIRUS_CLASSNAME_KEY = CheckVirusTaskPlugin.class
			.getName()
			+ ".antiVirusClassname"; //$NON-NLS-1$

	private AntiVirus antiVirus = null;

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public CheckVirusTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {

		Configuration configuration = getIngestManagerConfiguration();

		if (configuration.containsKey(ANTI_VIRUS_CLASSNAME_KEY)) {

			String antiVirusClassname = configuration
					.getString(ANTI_VIRUS_CLASSNAME_KEY);

			try {

				logger.info("Loading antivirus class " + antiVirusClassname); //$NON-NLS-1$
				setAntiVirus((AntiVirus) Class.forName(antiVirusClassname)
						.newInstance());
				logger.info("Using antivirus " //$NON-NLS-1$
						+ getAntiVirus().getClass().getName());

			} catch (ClassNotFoundException e) {
				// class doesn't exist. configuration is wrong
				logger.warn("Antivirus class " + antiVirusClassname //$NON-NLS-1$
						+ " not found - " + e.getMessage()); //$NON-NLS-1$
			} catch (InstantiationException e) {
				// not possible to create a new instance of the class
				logger.warn("Antivirus class " + antiVirusClassname //$NON-NLS-1$
						+ " instantiation exception - " + e.getMessage()); //$NON-NLS-1$
			} catch (IllegalAccessException e) {
				// not possible to create a new instance of the class
				logger.warn("Antivirus class " + antiVirusClassname //$NON-NLS-1$
						+ " illegal access exception - " + e.getMessage()); //$NON-NLS-1$
			}
		}

		if (getAntiVirus() == null) {
			setAntiVirus(new AVGAntiVirus());
			logger.info("Using default antivirus " //$NON-NLS-1$
					+ getAntiVirus().getClass().getName());
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
	 * @return the antiVirus
	 */
	protected AntiVirus getAntiVirus() {
		return antiVirus;
	}

	/**
	 * @param antiVirus
	 *            the antiVirus to set
	 */
	protected void setAntiVirus(AntiVirus antiVirus) {
		this.antiVirus = antiVirus;
	}

	/**
	 * Performs a virus check on the given {@link SIPState} contents.
	 * 
	 * @param sip
	 *            the {@link SIPState} to test.
	 * 
	 * @return a {@link IngestTaskResult} with the details about the operation
	 *         performed.
	 * 
	 * @throws IngestTaskException
	 *             if the virus check could not be performed.
	 * 
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sip) throws IngestTaskException {

		IngestTaskResult ingestTaskResult = null;

		File initialStateDirectory = getInitialStateLocation(sip);

		VirusCheckResult virusCheckResult = null;
		try {

			virusCheckResult = getAntiVirus().checkForVirus(
					initialStateDirectory);

			ingestTaskResult = new IngestTaskResult(virusCheckResult.isClean(),
					virusCheckResult.getReport());

		} catch (RuntimeException e) {
			logger.debug("Exception running virus check on SIP " + sip.getId() //$NON-NLS-1$
					+ " - " + e.getMessage(), e); //$NON-NLS-1$
			throw new IngestTaskException(
					"Exception running virus check on SIP " + sip.getId() //$NON-NLS-1$
							+ " - " + e.getMessage(), e); //$NON-NLS-1$
		}

		// Task performed successfully

		// Check antivirus result
		if (virusCheckResult.isClean()) {

			// Try to copy SIP files to new state
			try {

				copySIPToStateDirectory(sip, getFinalState());

			} catch (IngestTaskException e) {
				logger.debug("Exception copying files. Aborting task", e); //$NON-NLS-1$
				throw new IngestTaskException(
						"Exception copying SIP files. Leaving SIP unchanged.", //$NON-NLS-1$
						e);
			}

		} else {

			// If the SIP didn't pass the virus check, copy it to quarantine.
			try {

				copySIPToQuarantine(sip);

			} catch (IngestTaskException e) {
				logger.warn("Exception copying SIP " + sip.getId() //$NON-NLS-1$
						+ " contents to quarantine - " + e.getMessage(), e); //$NON-NLS-1$
			}
		}

		return ingestTaskResult;
	}

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

	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {
		// Deleting SIP files from the inital state
		try {

			deleteSIPFilesFromState(sip, getInitialState());

		} catch (IngestTaskException e) {
			logger
					.warn(
							"Exception deleting SIP " //$NON-NLS-1$
									+ sip.getId()
									+ " contents from initial state. Ignoring and leaving the garbage behind.", //$NON-NLS-1$
							e);
		}
	}

}
