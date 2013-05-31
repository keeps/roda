package pt.gov.dgarq.roda.plugins.ingest;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;

/**
 * This task checks the coherence of a {@link SIP}.
 * 
 * @author Rui Castro
 */
public class CheckSIPCoherenceTaskPlugin extends IngestTaskPlugin {
	private static final Logger logger = Logger
			.getLogger(CheckSIPCoherenceTaskPlugin.class);

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(CheckSIPCoherenceTaskPlugin.class.getName()
					+ "_messages");

	private final String name = "Ingest/Check SIP syntax"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Check SIP coherence. Verifies the validity and completeness of a SIP."; //$NON-NLS-1$

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public CheckSIPCoherenceTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
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

	/**
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {

		StringBuffer report = new StringBuffer();

		try {

			SIP sip = SIPUtility.readSIP(getInitialStateLocation(sipState),
					true);
			logger.info("SIP read successfully"); //$NON-NLS-1$

			sipState.setParentPID(sip.getParentPID());

			report.append(getString("CheckSIPCoherenceTaskPlugin.SIP_READ_OK")
					+ "\n");
			report.append(getString("CheckSIPCoherenceTaskPlugin.CHECKSUMS_OK")
					+ "\n");
			report.append(String.format(
					getString("CheckSIPCoherenceTaskPlugin.SIP_PARENT_DO"),
					sipState.getParentPID())
					+ "\n");

			List<SIPDescriptionObject> sipDOs = sip.getDescriptionObjects();
			if (sipDOs.size() > 0) {
				// OK

				logger.info("SIP has " + sipDOs.size() //$NON-NLS-1$
						+ " description objects"); //$NON-NLS-1$

				report.append(String
						.format(getString("CheckSIPCoherenceTaskPlugin.SIP_HAS_X_DESCRIPTION_OBJECTS"),
								sipDOs.size())
						+ "\n");

			} else {
				throw new SIPException(
						getString("CheckSIPCoherenceTaskPlugin.SIP_DOESNT_HAVE_DESCRIPTIVE_METADATA"));
			}

			List<SIPRepresentationObject> representations = SIPUtility
					.getRepresentationObjects(sip);
			if (representations.size() > 0) {
				// OK

				logger.info("SIP has " + representations.size() //$NON-NLS-1$
						+ " representations"); //$NON-NLS-1$

				report.append(String
						.format(getString("CheckSIPCoherenceTaskPlugin.SIP_HAS_X_REPRESENTATIONS"),
								representations.size())
						+ "\n");

			} else {
				throw new SIPException(
						getString("CheckSIPCoherenceTaskPlugin.SIP_DOESNT_HAVE_REPRESENTATIONS"));
			}

			copySIPToStateDirectory(sipState, getFinalState());

			return new IngestTaskResult(true, report.toString());

		} catch (SIPException e) {

			logger.debug("SIPException - " + e.getMessage() //$NON-NLS-1$
					+ " - Copying files to quarantine..."); //$NON-NLS-1$

			copySIPToQuarantine(sipState);

			return new IngestTaskResult(false, e.getMessage());
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

	/**
	 * @see IngestTask#doCleanup(SIPState, IngestTaskResult)
	 */
	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {
		// Deleting SIP files from the initial state
		try {

			deleteSIPFilesFromState(sip, getInitialState());

		} catch (IngestTaskException e) {
			logger.warn(
					"Exception deleting SIP " //$NON-NLS-1$
							+ sip.getId()
							+ " contents from initial state. Ignoring and leaving the garbage behind.", //$NON-NLS-1$
					e);
		}
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
