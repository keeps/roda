package pt.gov.dgarq.roda.plugins.maintenance.ingest;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTask;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Ingest;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;

/**
 * @author Rui Castro
 * 
 */
public abstract class AbstractRejectSIPTaskPlugin extends IngestTaskPlugin {
	private static final Logger logger = Logger
			.getLogger(AbstractRejectSIPTaskPlugin.class);

	/**
	 * Producer user name plugin parameter
	 */
	protected static PluginParameter PARAMETER_USERNAME() {
		return new PluginParameter(
				"producerUsername", //$NON-NLS-1$
				PluginParameter.TYPE_STRING, null, false, false,
				"Producer username"); //$NON-NLS-1$
	}

	/**
	 * Original filename plugin parameter
	 */
	protected static PluginParameter PARAMETER_ORIGINAL_FILENAME() {
		return new PluginParameter("originalFilename", //$NON-NLS-1$
				PluginParameter.TYPE_STRING, null, false, false, "SIP filename"); //$NON-NLS-1$
	}

	/**
	 * 'From date' plugin parameter
	 */
	// protected static PluginParameter PARAMETER_FROM_DATETIME() {
	// return new PluginParameter("fromDatetime",
	// PluginParameter.TYPE_DATETIME, null, true, false,
	// "SIPs after this date");
	// }
	/**
	 * 'To date' plugin parameter
	 */
	// protected static PluginParameter PARAMETER_TO_DATETIME() {
	// return new PluginParameter("toDatetime", PluginParameter.TYPE_DATETIME,
	// null, true, false, "SIPs until this date");
	// }
	private RODAClient rodaClient = null;

	private Ingest ingestService = null;

	private String ingestedPID = null;

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public AbstractRejectSIPTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
		super();
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
		logger.debug("init() OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		logger.debug("shutdown() OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(AbstractPlugin.PARAMETER_RODA_CORE_URL(),
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
						.PARAMETER_RODA_CORE_PASSWORD(), PARAMETER_USERNAME(),
				PARAMETER_ORIGINAL_FILENAME());
	}

	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {

		initRODAServices();

		String username = getParameterValues().get(
				PARAMETER_USERNAME().getName());
		String originalFilename = getParameterValues().get(
				PARAMETER_ORIGINAL_FILENAME().getName());

		Date fromDatetime = null;
		Date toDatetime = null;

		// if (!StringUtils.isBlank(getParameterValues().get(
		// PARAMETER_FROM_DATETIME().getName()))) {
		//
		// try {
		//
		// fromDatetime = DateParser.parse(getParameterValues().get(
		// PARAMETER_FROM_DATETIME().getName()));
		//
		// } catch (InvalidDateException e) {
		// logger.debug("Error parsing date 'fromDatetime' - "
		// + e.getMessage(), e);
		// throw new IngestTaskException(
		// "Error parsing date 'fromDatetime' - " + e.getMessage(),
		// e);
		// }
		//
		// }
		//
		// if (!StringUtils.isBlank(getParameterValues().get(
		// PARAMETER_TO_DATETIME().getName()))) {
		//
		// try {
		//
		// toDatetime = DateParser.parse(getParameterValues().get(
		// PARAMETER_TO_DATETIME().getName()));
		//
		// } catch (InvalidDateException e) {
		// logger.debug("Error parsing date 'fromDatetime' - "
		// + e.getMessage(), e);
		// throw new IngestTaskException(
		// "Error parsing date 'fromDatetime' - " + e.getMessage(),
		// e);
		// }
		//
		// }

		boolean match = true;

		if (!StringUtils.isBlank(username)) {
			match &= sipState.getUsername().equals(username);
		}
		if (!StringUtils.isBlank(originalFilename)) {
			match &= sipState.getOriginalFilename().equals(originalFilename);
		}
		if (fromDatetime != null) {
			match &= sipState.getDatetime().compareTo(fromDatetime) >= 0;
		}
		if (toDatetime != null) {
			match &= sipState.getDatetime().compareTo(toDatetime) <= 0;
		}

		if (match) {

			this.ingestedPID = sipState.getIngestedPID();
			sipState.setIngestedPID(null);

			return new IngestTaskResult(false, "Rejected by plugin '" //$NON-NLS-1$
					+ getName() + "'"); //$NON-NLS-1$
		} else {
			throw new IngestTaskException(
					"SIP doesn't match conditions - IGNORED"); //$NON-NLS-1$
		}
	}

	/**
	 * @see IngestTask#doCleanup(SIPState, IngestTaskResult)
	 */
	@Override
	protected void doCleanup(SIPState sipState, IngestTaskResult taskResult) {

		if (this.ingestedPID == null) {

			// Deleting SIP files from the initial state
			try {
				deleteSIPFilesFromState(sipState, getInitialState());
			} catch (IngestTaskException e) {
				logger
						.warn(
								"Exception deleting SIP " //$NON-NLS-1$
										+ sipState.getId()
										+ " contents from initial state. Ignoring and leaving the garbage behind.", //$NON-NLS-1$
								e);
			}

		} else {

			// Remove objects from Fedora
			try {

				this.ingestService.removeDescriptionObject(this.ingestedPID);

			} catch (Exception e) {
				logger.warn("Error removing DO " + sipState.getIngestedPID() //$NON-NLS-1$
						+ ". Leaving it behind - " + e.getMessage(), e); //$NON-NLS-1$
			}

		}
	}

	/**
	 * @see IngestTask#undoTask(SIPState, IngestTaskResult)
	 */
	@Override
	protected void undoTask(SIPState sipState, IngestTaskResult taskResult) {
		// Nothing to undo
	}

	private void initRODAServices() throws IngestTaskException {

		String rodaClientServiceUrl = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		String rodaClientUsername = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
		String rodaClientPassword = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());

		try {

			this.rodaClient = new RODAClient(new URL(rodaClientServiceUrl),
					rodaClientUsername, rodaClientPassword);

			this.ingestService = this.rodaClient.getIngestService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new IngestTaskException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}
}
