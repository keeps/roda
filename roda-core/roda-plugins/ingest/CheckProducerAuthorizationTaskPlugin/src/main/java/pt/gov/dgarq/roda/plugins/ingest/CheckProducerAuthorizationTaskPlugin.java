package pt.gov.dgarq.roda.plugins.ingest;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTask;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class CheckProducerAuthorizationTaskPlugin extends IngestTaskPlugin {
	private static final Logger logger = Logger
			.getLogger(CheckProducerAuthorizationTaskPlugin.class);

	private final String name = "Ingest/Check producer authorization"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Check SIP producer authorization. " //$NON-NLS-1$
			+ "Verifies that the producer of the SIP has permissions to ingest to the specified Fonds."; //$NON-NLS-1$

	private RODAClient rodaClient = null;
	private UserBrowser rodaServiceUserBrowser = null;
	private Browser rodaServiceBrowser = null;

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public CheckProducerAuthorizationTaskPlugin()
			throws InvalidIngestStateException, IngestRegistryException {
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
		return Arrays.asList(AbstractPlugin.PARAMETER_RODA_CORE_URL(),
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
						.PARAMETER_RODA_CORE_PASSWORD(),AbstractPlugin.PARAMETER_RODA_CAS_URL());
	}

	/**
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {

		initRODAServices();

		StringBuffer report = new StringBuffer();
		try {

			User producerUser = this.rodaServiceUserBrowser.getUser(sipState
					.getUsername());
			Producers fondsProducers = this.rodaServiceBrowser
					.getProducers(sipState.getParentPID());

			List<String> producerUsers = Arrays.asList(fondsProducers
					.getUsers());
			List<String> producerGroups = new ArrayList<String>(Arrays
					.asList(fondsProducers.getGroups()));

			producerGroups.retainAll(producerGroups);

			boolean authorized = producerUsers.contains(producerUser.getName())
					|| producerGroups.size() > 0;

			if (authorized) {
				// OK

				report
						.append(String
								.format(
										Messages
												.getString("CheckProducerAuthorizationTaskPlugin.PRODUCER_IS_AUTHORIZED"), //$NON-NLS-1$
										producerUser.getName(), fondsProducers
												.getDescriptionObjectPID()));

				// Copy SIP files to next state directory
				copySIPToStateDirectory(sipState, getFinalState());

				return new IngestTaskResult(true, report.toString());

			} else {

				// ERROR

				// Copy SIP files to quarantine
				copySIPToQuarantine(sipState);

				report
						.append(String
								.format(
										Messages
												.getString("CheckProducerAuthorizationTaskPlugin.PRODUCER_IS_NOT_AUTHORIZED"), //$NON-NLS-1$
										producerUser.getName(), fondsProducers
												.getDescriptionObjectPID()));

				return new IngestTaskResult(false, report.toString());
			}

		} catch (NoSuchRODAObjectException e) {

			// ERROR

			// Copy SIP files to quarantine
			copySIPToQuarantine(sipState);

			report
					.append(String
							.format(
									Messages
											.getString("CheckProducerAuthorizationTaskPlugin.PARENT_D_DOESNT_EXIST"), //$NON-NLS-1$
									sipState.getParentPID()));

			return new IngestTaskResult(false, report.toString());

		} catch (UserManagementException e) {

			logger.debug("Error getting producer information - " //$NON-NLS-1$
					+ e.getMessage() + " - Task aborted.", e); //$NON-NLS-1$
			throw new IngestTaskException(
					"Error getting producer information - " + e.getMessage() //$NON-NLS-1$
							+ " - Task aborted.", e); //$NON-NLS-1$

		} catch (BrowserException e) {

			logger.debug("Error getting fonds producers information - " //$NON-NLS-1$
					+ e.getMessage() + " - Task aborted.", e); //$NON-NLS-1$
			throw new IngestTaskException(
					"Error getting fonds producers information - " //$NON-NLS-1$
							+ e.getMessage() + " - Task aborted.", e); //$NON-NLS-1$

		} catch (RemoteException e) {

			logger.debug("Unespected error - " + e.getMessage() //$NON-NLS-1$
					+ " - Task aborted.", e); //$NON-NLS-1$
			throw new IngestTaskException("Unespected error - " //$NON-NLS-1$
					+ e.getMessage() + " - Task aborted.", e); //$NON-NLS-1$
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
			logger
					.warn(
							"Exception deleting SIP " //$NON-NLS-1$
									+ sip.getId()
									+ " contents from initial state. Ignoring and leaving the garbage behind.", //$NON-NLS-1$
							e);
		}
	}

	private void initRODAServices() throws IngestTaskException {

		String rodaClientServiceUrl = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		String rodaClientUsername = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
		String rodaClientPassword = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());
		String casURL = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CAS_URL().getName());
		String coreURL = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL));
			this.rodaClient = new RODAClient(new URL(rodaClientServiceUrl),
					rodaClientUsername, rodaClientPassword,casUtility);

			this.rodaServiceUserBrowser = this.rodaClient
					.getUserBrowserService();
			this.rodaServiceBrowser = this.rodaClient.getBrowserService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new IngestTaskException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}

}
