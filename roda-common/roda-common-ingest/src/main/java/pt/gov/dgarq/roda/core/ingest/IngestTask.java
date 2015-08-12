package pt.gov.dgarq.roda.core.ingest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;

/**
 * This is an abstract ingest task. Specific tasks must extend this class.
 * 
 * @author Rui Castro
 */
public abstract class IngestTask {
	static final private Logger logger = Logger.getLogger(IngestTask.class);

	private IngestManager ingestManager = null;

	/**
	 * The {@link SIPState} initial state for this task.
	 */
	private String initialState = null;

	/**
	 * The {@link SIPState} final state for this task.
	 */
	private String finalState = null;

	protected IngestTask() throws InvalidIngestStateException,
			IngestRegistryException {

		this.ingestManager = IngestManager.getDefaultIngestManager();

		setInitialState(getIngestManager().getInitialState(getClass()));
		setFinalState(getIngestManager().getFinalState(getClass()));

		if (getInitialState() == null) {

			logger.trace("initialState is null. It's an initial task.");

			// If a task doesn't have an initial state, it means it's one of the
			// initial tasks. The initial tasks must put SIPs in one of the
			// initial states.

			if (getIngestManager().getInitialStates().contains(getFinalState())) {
				// ok
			} else {
				// not ok
				logger
						.error("finalState should be one of the initial states; throwing InvalidStateException");
				throw new InvalidIngestStateException(
						"finalState should be one of the initial states");
			}

		} else {
			// It's a normal task with initial and final states.
		}

		logger.info("init OK (" + getInitialState() + " => " + getFinalState()
				+ ")");
	}

	/**
	 * @return the ingestManager
	 */
	protected IngestManager getIngestManager() {
		return this.ingestManager;
	}

	/**
	 * @return the ingest manager configuration
	 */
	protected Configuration getIngestManagerConfiguration() {
		return getIngestManager().getConfiguration();
	}

	/**
	 * @return the initialState
	 */
	public String getInitialState() {
		return initialState;
	}

	/**
	 * @param initialState
	 *            the initialState to set
	 * @throws InvalidIngestStateException
	 */
	private void setInitialState(String initialState)
			throws InvalidIngestStateException {

		logger.trace("setInitialState(" + initialState + ")");

		if (initialState == null
				|| getIngestManager().getStates().contains(initialState)) {
			this.initialState = initialState;
		} else {
			throw new InvalidIngestStateException("'" + initialState
					+ "' is not a valid initial state");
		}
	}

	/**
	 * @return the finalState
	 */
	public String getFinalState() {
		return finalState;
	}

	/**
	 * @param finalState
	 *            the finalState to set
	 * 
	 * @throws InvalidIngestStateException
	 */
	private void setFinalState(String finalState)
			throws InvalidIngestStateException {

		logger.trace("setFinalState(" + finalState + ")");

		if (getIngestManager().getStates().contains(finalState)) {
			this.finalState = finalState;
		} else {
			throw new InvalidIngestStateException("'" + finalState
					+ "' is not a valid final state");
		}
	}

	/**
	 * Returns an ID for this task.
	 * 
	 * @return a {@link String} with and identifier for this task.
	 */
	public String getID() {
		return getClass().getSimpleName();
	}

	/**
	 * Performs the task on {@link SIPState}. This method should return a
	 * {@link IngestTaskResult}. If everything goes ok
	 * {@link IngestTaskResult#isPassed()} should return <code>true</code>, if
	 * not it should return <code>false</code>. Either way
	 * {@link IngestTaskResult#getOutcomeMessage()} should always contain a
	 * message with the details of the operation.
	 * 
	 * If the task couldn't be performed on the {@link SIPState} for some reason
	 * that doesn't mean the {@link SIPState} is invalid,
	 * {@link IngestTaskException} should be thrown with a message saying what
	 * when wrong.
	 * 
	 * This method is abstract and must be implemented by every specific task.
	 * 
	 * @param sipState
	 *            the SIP on which to perform the task.
	 * 
	 * @return An {@link IngestTaskResult}.
	 * 
	 * @throws IngestTaskException
	 *             if the task could not be performed. A message with details
	 *             about the error is mandatory inside the this exception.
	 */
	protected abstract IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException;

	/**
	 * Undo the effects of this task. This method is called when the task
	 * outcome could not be registered in ingest registry.
	 * 
	 * @param sipState
	 *            the {@link SIPState} to undo.
	 * @param the
	 *            {@link IngestTaskResult} produced by
	 *            {@link IngestTask#doTask(SIPState)}.
	 */
	protected abstract void undoTask(SIPState sipState,
			IngestTaskResult taskResult);

	/**
	 * Performs clean up actions after a successfull task. This method is called
	 * when the task was successfull and the registry too.
	 * 
	 * @param sipState
	 *            the {@link SIPState} to clean up.
	 * @param the
	 *            {@link IngestTaskResult} produced by
	 *            {@link IngestTask#doTask(SIPState)}.
	 */
	protected abstract void doCleanup(SIPState sipState,
			IngestTaskResult taskResult);

	/**
	 * Returns the list of SIPs waiting for this task.
	 * 
	 * @return a {@link List} of {@link SIPState}s of the SIPs in state
	 *         <code>state</code>.
	 * 
	 * @throws IngestRegistryException
	 */
	public List<SIPState> getAvailableSIPs() throws IngestRegistryException {

		Filter availableSIPsFilter = new Filter();
		availableSIPsFilter.add(new SimpleFilterParameter("state", getInitialState()));
		availableSIPsFilter.add(new SimpleFilterParameter("processing", "false"));

		return getIngestManager().getSIPs(
				new ContentAdapter(availableSIPsFilter, null, null));
	}

	/**
	 * Returns the next {@link SIPState} waiting for this task.
	 * 
	 * @return a {@link SIPState} or <code>null</code> if no {@link SIPState} is
	 *         available.
	 * 
	 * @throws IngestRegistryException
	 */
	public SIPState getNextAvailableSIP() throws IngestRegistryException {

		Filter availableSIPsFilter = new Filter();
		availableSIPsFilter.add(new SimpleFilterParameter("state", getInitialState()));
		availableSIPsFilter.add(new SimpleFilterParameter("processing", "false"));
		Sorter sorterOlderFirst = new Sorter(
				new SortParameter[] { new SortParameter("datetime", false) });
		ContentAdapter firstAvailableSIP = new ContentAdapter(
				availableSIPsFilter, sorterOlderFirst, new Sublist(0, 1));

		List<SIPState> sips = getIngestManager().getSIPs(firstAvailableSIP);

		SIPState sip;
		if (sips.size() > 0) {
			sip = sips.get(0);
		} else {
			sip = null;
		}

		return sip;
	}

	/**
	 * Process a {@link SIPState}. This is the method that should be called to
	 * start the processing of a {@link SIPState}.
	 * 
	 * @param sipState
	 *            the {@link SIPState} to process.
	 * 
	 * @throws SIPAlreadyProcessingException
	 *             if the processing flag is already on.
	 * @throws IngestTaskException
	 *             if something went wrong with task processing.
	 */
	public void processSIP(SIPState sipState) throws IngestTaskException,
			SIPAlreadyProcessingException {

		try {

			getIngestManager().activateProcessingFlag(sipState.getId());

		} catch (NoSuchSIPException e) {

			logger.debug("Exception activating processing flag of SIP "
					+ sipState.getId() + " - " + e.getMessage(), e);

			throw new IngestTaskException(
					"Exception activating processing flag of SIP "
							+ sipState.getId() + " - " + e.getMessage(), e);

		} catch (IngestRegistryException e) {

			logger.debug("Exception activating processing flag of SIP "
					+ sipState.getId() + " - " + e.getMessage(), e);

			throw new IngestTaskException(
					"Exception activating processing flag of SIP "
							+ sipState.getId() + " - " + e.getMessage(), e);
		}

		try {

			IngestTaskResult taskResult = doTask(sipState);

			try {

				if (taskResult.isPassed()) {

					logger.debug("Task " + getClass().getSimpleName()
							+ " successfull. Registering state transition...");

					registerTaskSuccess(sipState.getId(), sipState.getIngestedPID(), sipState
							.getParentPID(), getID(), taskResult
							.getOutcomeMessage());

				} else {

					logger
							.debug("Task "
									+ getClass().getSimpleName()
									+ " failed. Registering transition to quarantine...");

					registerTaskFailure(sipState.getId(), sipState.getIngestedPID(), sipState
							.getParentPID(), getID(), taskResult
							.getOutcomeMessage());

				}

				logger.trace("calling doCleanup(" + sipState.getId() + ")");
				doCleanup(sipState, taskResult);

			} catch (IngestRegistryException e) {

				logger.warn(
						"Exception registering task outcome. Calling undoTask("
								+ sipState.getId() + ")", e);

				undoTask(sipState, taskResult);

			}

		} catch (IngestTaskException e) {

			throw e;

		} finally {

			try {

				getIngestManager().deactivateProcessingFlag(sipState.getId());

			} catch (IngestRegistryException e) {
				logger.warn("Exception removing processing flag from SIP "
						+ sipState.getId(), e);
			}

		}

	}

	/**
	 * Process the next available {@link SIPState}.
	 * 
	 * @return a {@link String} with the ID of the {@link SIPState} processed.
	 * 
	 * @throws IngestTaskException
	 *             if something went wrong with task processing.
	 */
	public String processNextAvailableSIP() throws IngestTaskException {

		try {

			SIPState sip = getNextAvailableSIP();

			if (sip != null) {
				processSIP(sip);
				return sip.getId();
			} else {
				return null;
			}

		} catch (IngestRegistryException e) {

			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestTaskException(
					"Exception getting next available SIP " + e.getMessage(), e);

		} catch (SIPAlreadyProcessingException e) {

			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestTaskException(
					"Exception getting next available SIP " + e.getMessage(), e);
		}

	}

	/**
	 * Register a new SIP.
	 * 
	 * @param username
	 *            the username of the user that submitted this {@link SIPState}.
	 * @param originalFilename
	 *            the {@link SIPState} original filename.
	 * @param state
	 *            the initial state for the {@link SIPState}. This state must be
	 *            one of the possible initial states (
	 *            {@link IngestManager#getInitialStates()}).
	 * 
	 * @return the {@link SIPState} registered.
	 * 
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 * @throws IngestTaskException
	 */
	public SIPState registerSIP(String username, String originalFilename,
			String state) throws InvalidIngestStateException,
			IngestRegistryException, IngestTaskException {

		if (getIngestManager().getInitialStates().contains(state)) {

			SIPState newSIP = getIngestManager().insertSIP(username,
					originalFilename);

			try {

				processSIP(newSIP);

			} catch (IngestTaskException e) {

				logger.warn("Insertion of new SIP failed - " + e.getMessage(),
						e);
				logger.info("Removing SIP from list.");

				try {

					getIngestManager().removeSIP(newSIP.getId());

				} catch (NoSuchSIPException e1) {
					logger.warn("SIP removal failed. Ignoring.", e1);
				}

				throw new IngestTaskException("Insertion of new SIP failed - "
						+ e.getMessage(), e);

			} catch (SIPAlreadyProcessingException e) {

				logger.warn("Insertion of new SIP failed - " + e.getMessage(),
						e);
				logger.info("Removing SIP from list.");

				try {

					getIngestManager().removeSIP(newSIP.getId());

				} catch (NoSuchSIPException e1) {
					logger.warn("SIP removal failed. Ignoring.", e1);
				}

				throw new IngestTaskException("Insertion of new SIP failed - "
						+ e.getMessage(), e);

			}

			try {

				return getIngestManager().getSIP(newSIP.getId());

			} catch (NoSuchSIPException e) {
				throw new IngestTaskException(
						"Error accessing newly created SIP - " + e.getMessage(),
						e);
			}

		} else {
			throw new InvalidIngestStateException(state
					+ " is not one of the initial states "
					+ getIngestManager().getInitialStates());
		}

	}

	protected File getInitialStateLocation(SIPState sip) {
		return getIngestManager().getLocationForState(sip, getInitialState());
	}

	protected File getFinalStateLocation(SIPState sip) {
		return getIngestManager().getLocationForState(sip, getFinalState());
	}

	protected void moveSIPToStateDirectory(SIPState sip, String state)
			throws IngestTaskException {

		try {

			File sipFile = getIngestManager().getCurrentSIPLocation(sip);
			File destDir = getIngestManager().getLocationForState(sip, state);

			// Delete destination directory if exists
			if (destDir.exists()) {
				FileUtils.deleteDirectory(destDir);
			}

			// Move the SIP file/directory to 'state' directory
			FileUtils.moveToDirectory(sipFile, destDir, true);

		} catch (IOException e) {
			throw new IngestTaskException("Error moving SIP " + sip.getId()
					+ " - " + e.getMessage(), e);
		}
	}

	protected void copySIPToStateDirectory(SIPState sipState, String state)
			throws IngestTaskException {

		File sipFileOrDir = getIngestManager().getCurrentSIPLocation(sipState);
		File destDir = getIngestManager().getLocationForState(sipState, state);

		try {

			// Delete destination directory if exists
			if (destDir.exists()) {
				FileUtils.deleteDirectory(destDir);
			}

			// Copy the SIP file/directory to 'state' directory
			if (sipFileOrDir.isDirectory()) {
				FileUtils.copyDirectory(sipFileOrDir, destDir);
			} else {
				FileUtils.copyFileToDirectory(sipFileOrDir, destDir);
			}

		} catch (IOException e) {
			throw new IngestTaskException("Error copying SIP " + sipState.getId()
					+ " to directory " + destDir + " - " + e.getMessage(), e);
		}
	}

	protected void deleteSIPFilesFromState(SIPState sip, String state)
			throws IngestTaskException {
		try {

			File fileForState = getIngestManager().getLocationForState(sip,
					state);
			FileUtils.forceDelete(fileForState);

		} catch (IOException e) {
			throw new IngestTaskException("Error deleting SIP " + sip.getId()
					+ " - " + e.getMessage(), e);
		}
	}

	protected void moveSIPToQuarantine(SIPState sipState) throws IngestTaskException {
		moveSIPToStateDirectory(sipState, getIngestManager().getQuarantineState());
	}

	protected void copySIPToQuarantine(SIPState sipState) throws IngestTaskException {
		copySIPToStateDirectory(sipState, getIngestManager().getQuarantineState());
	}

	private void registerTaskSuccess(String sipID, String ingestedPID,
			String parentPID, String taskID, String successMessage)
			throws IngestRegistryException {

		getIngestManager().registerStateChange(sipID, getInitialState(),
				getFinalState(), ingestedPID, parentPID, taskID, true,
				successMessage);
	}

	private void registerTaskFailure(String sipID, String ingestedPID,
			String parentPID, String taskID, String errorMessage)
			throws IngestRegistryException {

		getIngestManager().registerStateChange(sipID, getInitialState(),
				getIngestManager().getQuarantineState(), ingestedPID,
				parentPID, taskID, false, errorMessage);
	}

}
