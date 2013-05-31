package pt.gov.dgarq.roda.plugins.ingest;

import java.util.Arrays;
import java.util.List;

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
 * This task simply moves the SIP from one state to another without any test or
 * modification.
 * 
 * @author Rui Castro
 */
public class MoveTaskPlugin extends IngestTaskPlugin {
	static final private Logger logger = Logger.getLogger(MoveTaskPlugin.class);

	private final String name = "Ingest/Unpack FTP SIPs"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Moves the SIP (and it's contents) to a new state (and correspondent directory)."; //$NON-NLS-1$

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public MoveTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
		logger.info("init OK"); //$NON-NLS-1$
	}

	/**
	 * @see pt.gov.dgarq.roda.core.plugins.Plugin#shutdown()
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
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {
		try {

			copySIPToStateDirectory(sipState, getFinalState());

			return new IngestTaskResult(true, "SIP copied successfully"); //$NON-NLS-1$

		} catch (IngestTaskException e) {
			return new IngestTaskResult(false, "Error copying SIP " //$NON-NLS-1$
					+ e.getMessage());
		}

	}

	/**
	 * @see IngestTask#doCleanup(SIPState, IngestTaskResult)
	 */
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

}
