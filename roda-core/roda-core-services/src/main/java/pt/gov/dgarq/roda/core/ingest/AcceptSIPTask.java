package pt.gov.dgarq.roda.core.ingest;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearchException;
import pt.gov.dgarq.roda.core.services.Browser;
import pt.gov.dgarq.roda.core.services.Editor;

/**
 * This task accepts a {@link SIPState}, marking it's objects active, or rejects
 * it purging it's objects from the repository. Either way, the action is
 * registered with an optional message explaining the reason for the action.
 * 
 * @author Rui Castro
 */
public class AcceptSIPTask extends IngestTask {
	private static final Logger logger = Logger.getLogger(AcceptSIPTask.class);

	private FedoraClientUtility fedoraClientUtility = null;

	private boolean accept;
	private String reason;
	private String username;

	private String parentDOPID;

	private List<String> allIngestedPIDs;

	private boolean needsUndo = true;

	/**
	 * Construct a new {@link AcceptSIPTask}.
	 * 
	 * @param fedoraClientUtility
	 *            the Fedora client utility.
	 * 
	 * @throws IngestRegistryException
	 * @throws InvalidIngestStateException
	 */
	public AcceptSIPTask(FedoraClientUtility fedoraClientUtility)
			throws InvalidIngestStateException, IngestRegistryException {
		super();

		this.fedoraClientUtility = fedoraClientUtility;
	}

	/**
	 * @return the fedoraClientUtility
	 */
	private FedoraClientUtility getFedoraClientUtility() {
		return fedoraClientUtility;
	}

	@Override
	protected IngestTaskResult doTask(SIPState sip) throws IngestTaskException {

		StringBuffer buffer = new StringBuffer();

		if (accept) {
			buffer.append(String.format(Messages
					.getString("AcceptSIPTask.SIP_ACCEPTED"), this.username)); //$NON-NLS-1$
		} else {
			buffer.append(String.format(Messages
					.getString("AcceptSIPTask.SIP_REJECTED"), this.username)); //$NON-NLS-1$
		}

		if (!StringUtils.isBlank(this.reason)) {
			buffer.append(String.format(Messages
					.getString("AcceptSIPTask.REASON"), this.reason)); //$NON-NLS-1$
		}

		// Get the parent and descendants PIDs
		try {

			this.parentDOPID = sip.getParentPID();
			this.allIngestedPIDs = getFedoraClientUtility().getFedoraRISearch()
					.getDODescendantPIDs(sip.getIngestedPID(), true);
			this.allIngestedPIDs.add(0, sip.getIngestedPID());

			logger.debug("SIP objects are " + this.allIngestedPIDs); //$NON-NLS-1$

		} catch (NoSuchRODAObjectException e) {
			this.needsUndo = false;
			logger.debug(e.getMessage(), e);
			throw new IngestTaskException(
					"Error getting SIP ingested object's parent or descendents - " //$NON-NLS-1$
							+ e.getMessage(), e);
		} catch (FedoraRISearchException e) {
			this.needsUndo = false;
			logger.debug(e.getMessage(), e);
			throw new IngestTaskException(
					"Error getting SIP ingested object's parent or descendents - " //$NON-NLS-1$
							+ e.getMessage(), e);
		}

		if (accept) {
			// If SIP was accepted, make objects active
			try {

				getFedoraClientUtility().markObjectsActive(
						this.allIngestedPIDs, true);

				buffer.append(String.format(Messages
						.getString("AcceptSIPTask.OBJECTS_MARKED_ACTIVE"), //$NON-NLS-1$
						this.allIngestedPIDs));

				DescriptionObject ingestedDO = new Browser()
						.getDescriptionObject(sip.getIngestedPID());

				ingestedDO.setParentPID(parentDOPID);

				Date acceptDate = new Date();
				String acqinfoDate = DateParser.getIsoDate(acceptDate).split(
						"T")[0]; //$NON-NLS-1$
				ingestedDO.setAcqinfoDate(acqinfoDate);

				if (StringUtils.isBlank(ingestedDO.getNote())) {
					// If previous note is blank
					ingestedDO
							.setNote("Pacote de submissão validado semanticamente por "
									+ username);
				} else {
					// If previous note already contains something
					ingestedDO
							.setNote("Pacote de submissão validado semanticamente por "
									+ username + "\n" + ingestedDO.getNote());
				}

				Editor editorService = new Editor();
				Browser browserService = new Browser();

				RODAObjectPermissions doPermissions = browserService
						.getRODAObjectPermissions(this.parentDOPID);
				doPermissions.setObjectPID(sip.getIngestedPID());

				editorService.setRODAObjectPermissions(doPermissions, true);

				logger.info("Copied permissions from parent DO "
						+ this.parentDOPID + " to ingested DO "
						+ sip.getIngestedPID() + " and it's descendants");

				editorService.modifyDescriptionObject(ingestedDO);

				logger.info("Modified DO " + ingestedDO.getPid()
						+ " to be a child of " + parentDOPID);

				editorService = null;
				browserService = null;

			} catch (Exception e) {
				this.needsUndo = true;
				throw new IngestTaskException("Task did not complete - " //$NON-NLS-1$
						+ e.getMessage(), e);
			}

		} else {
			// If SIP was rejected, remove object relation to parent.
			try {

				// SimpleDescriptionObject sdo = getFedoraClientUtility()
				// .getFedoraRISearch().getSimpleDescriptionObject(
				// sip.getIngestedPID());
				//
				// sdo.setParentPID(null);
				//
				// getFedoraClientUtility().setSimpleDOProperties(sdo);

				sip.setIngestedPID(null);

			} catch (Exception e) {
				this.needsUndo = false;
				throw new IngestTaskException("Task not done - " //$NON-NLS-1$
						+ e.getMessage(), e);
			}
		}

		return new IngestTaskResult(accept, buffer.toString());
	}

	@Override
	protected void undoTask(SIPState sip, IngestTaskResult taskResult) {

		if (this.needsUndo) {

			if (accept) {
				// The objects were marked active, make objects inactive again.

				try {
					getFedoraClientUtility().markObjectsInactive(
							this.allIngestedPIDs, false);
				} catch (Exception e) {
					logger
							.warn("markObjectsInactive thrown exception even with stopAtError at false"); //$NON-NLS-1$
				}

			} else {
				// The relation to the parent object was removed, create
				// relation again.
				// try {
				//
				// SimpleDescriptionObject sdo = getFedoraClientUtility()
				// .getFedoraRISearch().getSimpleDescriptionObject(
				// sip.getIngestedPID());
				// sdo.setParentPID(parentDOPID);
				// getFedoraClientUtility().setSimpleDOProperties(sdo);
				//
				// } catch (Exception e) {
				//					logger.error("Error adding parent-of relation - " //$NON-NLS-1$
				// + e.getMessage(), e);
				// }
			}

		} else {
			// No undo needed
		}
	}

	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {
		// Action was successful...

		if (accept) {
			// The objects were marked active, nothing to do.
		} else {
			// The SIP was rejected and relation to parent removed, purge
			// the objects.
			try {
				getFedoraClientUtility().purgeObjects(this.allIngestedPIDs,
						false);
			} catch (Exception e) {
				logger
						.warn("purgeObjects thrown exception even with stopAtError at false"); //$NON-NLS-1$
			}
		}

	}

	/**
	 * Accepts or rejects a {@link SIPState}.
	 * 
	 * @param sipID
	 *            the ID of the {@link SIPState} to accept/reject.
	 * @param accept
	 *            <code>true</code> to accept, <code>false</code> to reject.
	 * @param reason
	 *            a message with the reason for accepting or rejecting the
	 *            {@link SIPState}.
	 * @param username
	 *            the user name of the user that accepted/rejected the SIP.
	 * 
	 * @return the accepted/rejected {@link SIPState}.
	 * 
	 * @throws NoSuchSIPException
	 * @throws SIPAlreadyProcessingException
	 * @throws IllegalOperationException
	 * @throws IngestRegistryException
	 * @throws IngestTaskException
	 */
	public SIPState acceptSIP(String sipID, boolean accept, String reason,
			String username) throws NoSuchSIPException,
			IngestRegistryException, IngestTaskException,
			IllegalOperationException, SIPAlreadyProcessingException {

		List<SIPState> availableSIPs = getAvailableSIPs();

		SIPState sip = getIngestManager().getSIP(sipID);

		if (availableSIPs.contains(sip)) {
			this.accept = accept;
			this.reason = reason;
			this.username = username;

			processSIP(sip);

			return getIngestManager().getSIP(sipID);

		} else {
			throw new IllegalOperationException("SIP with id " + sipID //$NON-NLS-1$
					+ " is not available for this task."); //$NON-NLS-1$
		}

	}

}
