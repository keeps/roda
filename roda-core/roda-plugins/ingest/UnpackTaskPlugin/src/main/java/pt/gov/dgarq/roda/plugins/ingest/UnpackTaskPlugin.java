package pt.gov.dgarq.roda.plugins.ingest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;
import pt.gov.dgarq.roda.util.ZipUtility;

/**
 * This class performs the task of unpacking (UnZipping) a SIP in ZIP format.
 * 
 * @author Rui Castro
 */
public class UnpackTaskPlugin extends IngestTaskPlugin {
	static final private Logger logger = Logger
			.getLogger(UnpackTaskPlugin.class);

	private final String name = "Ingest/Unpack SIPs"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Unpacks a SIP. Extract all the files inside a SIP (ZIP file)."; //$NON-NLS-1$

	/**
	 * Constructs a new {@link UnpackTaskPlugin}.
	 * 
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public UnpackTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
		super();
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
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

	@Override
	protected IngestTaskResult doTask(SIPState sip) throws IngestTaskException {

		StringBuilder report = new StringBuilder();

		File zipFilename = getInitialStateLocation(sip);
		File directoryForNewState = getFinalStateLocation(sip);

		try {

			List<File> extractedFiles = ZipUtility.extractFilesFromZIP(
					zipFilename, directoryForNewState);

			report.append(Messages
					.getString("UnpackTaskPlugin.EXTRACTED_FILES") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			for (File file : extractedFiles) {
				report.append("\t" + file + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			return new IngestTaskResult(true, report.toString());

		} catch (IOException e) {
			return new IngestTaskResult(false, Messages
					.getString("UnpackTaskPlugin.ERROR_UNPACKING") //$NON-NLS-1$
					+ " - " + e.getMessage()); //$NON-NLS-1$
		}
	}

	@Override
	protected void undoTask(SIPState sip, IngestTaskResult taskResult) {
		// Remove SIP contents already unpacked to new final state directory.
		try {
			deleteSIPFilesFromState(sip, getFinalState());
		} catch (IngestTaskException e) {
			logger.warn("Exception performing undo on SIP " + sip.getId() //$NON-NLS-1$
					+ ". Ignoring and leaving the garbage behind.", e); //$NON-NLS-1$
		}
	}

	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {
		// Deleting SIP files from the initial state
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
