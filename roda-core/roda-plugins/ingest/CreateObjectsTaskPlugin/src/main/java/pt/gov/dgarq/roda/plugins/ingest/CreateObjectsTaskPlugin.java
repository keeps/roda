package pt.gov.dgarq.roda.plugins.ingest;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.Uploader;
import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Ingest;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.SIPUtility;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPEventPreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationPreservationObject;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;

/**
 * @author Rui Castro
 */
public class CreateObjectsTaskPlugin extends IngestTaskPlugin {
	static final private Logger logger = Logger
			.getLogger(CreateObjectsTaskPlugin.class);

	private final String name = "Ingest/Create AIP"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Creates RODA Objects for objects inside the SIP."; //$NON-NLS-1$

	private RODAClient rodaClient = null;
	private Uploader rodaUploader = null;

	private Ingest ingestService = null;
	private Browser browserService = null;

	/**
	 * 
	 * @throws IngestRegistryException
	 * @throws InvalidIngestStateException
	 */
	public CreateObjectsTaskPlugin() throws InvalidIngestStateException,
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
		return Arrays.asList(AbstractPlugin.PARAMETER_RODA_CORE_URL(),
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
						.PARAMETER_RODA_CORE_PASSWORD());
	}

	@Override
	protected IngestTaskResult doTask(SIPState sipState)
			throws IngestTaskException {

		initRODAServices();

		StringBuffer report = new StringBuffer();

		SIP sip;
		try {

			sip = SIPUtility.readSIP(getInitialStateLocation(sipState), true);

		} catch (SIPException e) {

			logger.trace("SIPException - " + e.getMessage() //$NON-NLS-1$
					+ " - Copying SIP to quarantine..."); //$NON-NLS-1$

			copySIPToQuarantine(sipState);

			report.append(String.format(Messages
					.getString("CreateObjectsTaskPlugin.ERROR_READING_SIP"), e //$NON-NLS-1$
					.getMessage()));

			return new IngestTaskResult(false, e.getMessage());

		}

		List<String> createdPIDs = new ArrayList<String>();

		try {

			// Mark the acquisition date
			String acqinfoDate = DateParser.getIsoDate(new Date()).split("T")[0]; //$NON-NLS-1$

			// Create the DOs and it's ROs recursively
			SIPDescriptionObject parentDO = createDescriptionObject(sip
					.getParentPID(), sip.getDescriptionObject(), true,
					acqinfoDate);

			// Set the PID of the ingested DO in SIPState
			sipState.setIngestedPID(parentDO.getPid());

			// Create a new SIP with the ingested DOs and ROs
			SIP sipWithIngestedObjects = new SIP(sip.getParentPID(), parentDO);
			// List of ingested DOs
			List<SIPDescriptionObject> ingestedDOs = sipWithIngestedObjects
					.getDescriptionObjects();
			// List of ingested ROs
			List<SIPRepresentationObject> ingestedROs = sipWithIngestedObjects
					.getRepresentations();

			// Remove repeated PIDs from ingestedDOs
			Set<String> ingestedDOPIDs = new HashSet<String>();
			for (SIPDescriptionObject sipDO : ingestedDOs) {
				ingestedDOPIDs.add(sipDO.getPid());
			}
			createdPIDs.addAll(ingestedDOPIDs);

			// Remove repeated PIDs from ingestedROs
			Set<String> ingestedROPIDs = new HashSet<String>();
			for (SIPRepresentationObject sipRO : ingestedROs) {
				ingestedROPIDs.add(sipRO.getPid());
			}
			createdPIDs.addAll(ingestedROPIDs);

			// Remove repeated PIDs from ingestedPOs
			Set<String> ingestedPOPIDs = new HashSet<String>();
			for (SIPRepresentationObject sipRO : ingestedROs) {

				if (sipRO.getPreservationObject() != null) {

					ingestedPOPIDs.add(sipRO.getPreservationObject().getPid());

					if (sipRO.getPreservationObject().getPreservationEvents() != null) {
						for (SIPEventPreservationObject eventPO : sipRO
								.getPreservationObject()
								.getPreservationEvents()) {

							ingestedPOPIDs.add(eventPO.getPid());
						}
					}

				}
			}
			createdPIDs.addAll(ingestedPOPIDs);

			report
					.append(String
							.format(
									Messages
											.getString("CreateObjectsTaskPlugin.SIP_INGESTED_SUCCESSFULLY_UNDER_DO_X"), //$NON-NLS-1$
									sipState.getParentPID()));
			report
					.append(String
							.format(
									Messages
											.getString("CreateObjectsTaskPlugin.INGESTED_DESCRIPTION_OBJECTS"), //$NON-NLS-1$
									ingestedDOs.size()));
			report
					.append(String
							.format(
									Messages
											.getString("CreateObjectsTaskPlugin.INGESTED_REPRESENTATION_OBJECTS"), //$NON-NLS-1$
									ingestedROs.size()));

			String ingestEventPID = this.ingestService.registerIngestEvent(
					ingestedDOPIDs.toArray(new String[ingestedDOPIDs.size()]),
					ingestedROPIDs.toArray(new String[ingestedROPIDs.size()]),
					ingestedPOPIDs.toArray(new String[ingestedPOPIDs.size()]),
					getName() + "/" + getVersion(), report.toString()); //$NON-NLS-1$

			createdPIDs.add(ingestEventPID);

			report.append(String.format(Messages
					.getString("CreateObjectsTaskPlugin.CREATED_INGEST_EVENT"), //$NON-NLS-1$
					ingestEventPID)
					+ "\n"); //$NON-NLS-1$

			return new IngestTaskResult(true, report.toString());

		} catch (Exception e) {

			logger.info("Exception ingesting SIP objects - " + e.getMessage() //$NON-NLS-1$
					+ " - Copying SIP to quarantine...", e); //$NON-NLS-1$

			if (createdPIDs.size() > 0) {

				logger.info("Removing created objects with PIDs: " //$NON-NLS-1$
						+ createdPIDs);
				try {

					this.ingestService.removeObjects(createdPIDs
							.toArray(new String[createdPIDs.size()]));

				} catch (IngestException e1) {
					logger.info("Exception removing objects - " //$NON-NLS-1$
							+ e1.getMessage() + " - Ignoring."); //$NON-NLS-1$
				} catch (RemoteException e1) {
					logger.info("Exception removing objects - " //$NON-NLS-1$
							+ e1.getMessage() + " - Ignoring."); //$NON-NLS-1$
				}
			}

			copySIPToQuarantine(sipState);

			report
					.append(String
							.format(
									Messages
											.getString("CreateObjectsTaskPlugin.ERROR_CREATING_OBJECTS"), e //$NON-NLS-1$
											.getMessage()));

			return new IngestTaskResult(false, report.toString());
		}
	}

	@Override
	protected void undoTask(SIPState sip, IngestTaskResult taskResult) {
		logger.warn("undoTask for SIP " + sip.getId() + ". Nothing to undo"); //$NON-NLS-1$ //$NON-NLS-2$
		// TODO remove objects from Fedora???
	}

	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {
		// Deleting SIP from the initial state
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

	private SIPDescriptionObject createDescriptionObject(String parentPID,
			SIPDescriptionObject sipDO, boolean detached, String acqinfoDate)
			throws IngestException {

		String doPID;
		try {

			// Set the producer original ID (<acqinfo>/<num>)
			sipDO.setAcqinfoNum(sipDO.getId());
			// Set the acquisition date (<acqinfo>/<date>)
			sipDO.setAcqinfoDate(acqinfoDate);

			sipDO.setParentPID(parentPID);

			if (detached) {
				doPID = this.ingestService
						.createDetachedDescriptionObject(sipDO);
			} else {
				doPID = this.ingestService.createDescriptionObject(sipDO);
			}

			logger.info("DescriptionObject ingested successfully with PID " //$NON-NLS-1$
					+ doPID);

		} catch (Exception e) {
			logger.debug("Exception ingesting Description Object " //$NON-NLS-1$
					+ sipDO.getId() + " - " + e.getMessage()); //$NON-NLS-1$
			throw new IngestException("Exception ingesting Description Object " //$NON-NLS-1$
					+ sipDO.getId() + " - " + e.getMessage(), e); //$NON-NLS-1$
		}

		SIPDescriptionObject descriptionObject;
		try {

			descriptionObject = new SIPDescriptionObject(this.browserService
					.getDescriptionObject(doPID));
			logger.debug("DescriptionObject ingested is " + descriptionObject); //$NON-NLS-1$

		} catch (Exception e) {
			logger.debug("Exception getting ingested Description Object " //$NON-NLS-1$
					+ doPID + " - " + e.getMessage()); //$NON-NLS-1$
			throw new IngestException(
					"Exception getting ingested Description Object " + doPID //$NON-NLS-1$
							+ " - " + e.getMessage(), e); //$NON-NLS-1$
		}

		// Ingest Representation Objects
		try {

			for (SIPRepresentationObject sipChildRO : sipDO
					.getRepresentations()) {

				descriptionObject.addRepresentation(createRepresentationObject(
						descriptionObject.getPid(), sipChildRO));

			}

			createDerivationRelationshipsAndOrphanPOs(descriptionObject, sipDO);

		} catch (IngestException e) {

			logger
					.debug("Exception ingesting Representation Objects children of " //$NON-NLS-1$
							+ descriptionObject.getPid() + " - " //$NON-NLS-1$
							+ e.getMessage());

			logger.info("Removing " //$NON-NLS-1$
					+ descriptionObject.getRepresentations().size()
					+ " representations of " + descriptionObject.getPid() //$NON-NLS-1$
					+ " already ingested"); //$NON-NLS-1$

			// find out all the PIDs of already ingested objects
			Set<String> ingestedPIDs = new HashSet<String>();
			for (SIPRepresentationObject ingestedChildRO : descriptionObject
					.getRepresentations()) {

				ingestedPIDs.add(ingestedChildRO.getPid());

				SIPRepresentationPreservationObject ingestedRPO = ingestedChildRO
						.getPreservationObject();

				if (ingestedRPO != null) {

					if (ingestedRPO.getPid() != null) {
						ingestedPIDs.add(ingestedChildRO.getPid());
					}

					if (ingestedRPO.getPreservationEvents() != null) {

						for (SIPEventPreservationObject ingestedEPO : ingestedRPO
								.getPreservationEvents()) {

							if (ingestedEPO.getPid() != null) {
								ingestedPIDs.add(ingestedEPO.getPid());
							}
						}

					}

				}

			}

			deleteTemporaryObject(new ArrayList<String>(ingestedPIDs));

			logger.info("Objects " + ingestedPIDs + " removed successfully");

			logger.info("Removing Description Object " //$NON-NLS-1$
					+ descriptionObject.getPid() + " already ingested"); //$NON-NLS-1$

			if (deleteTemporaryObject(descriptionObject.getPid())) {

				logger.info("Description Object " + descriptionObject.getPid() //$NON-NLS-1$
						+ " removed successfully"); //$NON-NLS-1$
			}

			throw new IngestException(
					"Exception ingesting Representation Objects children of " //$NON-NLS-1$
							+ descriptionObject.getPid() + " - " //$NON-NLS-1$
							+ e.getMessage(), e);

		}

		// Ingest child Description Objects
		try {

			for (SIPDescriptionObject sipChildDO : sipDO.getChildren()) {

				descriptionObject.addChild(createDescriptionObject(
						descriptionObject.getPid(), sipChildDO, false,
						acqinfoDate));

			}

		} catch (IngestException e) {

			logger.debug("Exception ingesting Description Objects children of " //$NON-NLS-1$
					+ descriptionObject.getPid() + " - " + e.getMessage()); //$NON-NLS-1$

			logger.info("Removing " + descriptionObject.getChildren().size() //$NON-NLS-1$
					+ " objects already ingested"); //$NON-NLS-1$

			for (SIPDescriptionObject ingestedChildDO : descriptionObject
					.getChildren()) {

				if (deleteTemporaryObject(ingestedChildDO.getPid())) {

					logger.info("Description Object " //$NON-NLS-1$
							+ ingestedChildDO.getPid()
							+ " removed successfully"); //$NON-NLS-1$
				}
			}

			logger.info("Removing Description Object " //$NON-NLS-1$
					+ descriptionObject.getPid() + " already ingested"); //$NON-NLS-1$

			if (deleteTemporaryObject(descriptionObject.getPid())) {

				logger.info("Description Object " + descriptionObject.getPid() //$NON-NLS-1$
						+ " removed successfully"); //$NON-NLS-1$
			}

			throw new IngestException(
					"Exception ingesting Description Objects children of " //$NON-NLS-1$
							+ descriptionObject.getPid() + " - " //$NON-NLS-1$
							+ e.getMessage(), e);

		}

		return descriptionObject;
	}

	private SIPRepresentationObject createRepresentationObject(String doPID,
			SIPRepresentationObject sipRO) throws IngestTaskException {

		String roPID;
		try {

			logger.debug("Ingesting " + sipRO); //$NON-NLS-1$

			sipRO.setDescriptionObjectPID(doPID);

			roPID = this.ingestService.createRepresentationObject(sipRO);

		} catch (Exception e) {
			logger.debug("Exception ingesting Representation Object " //$NON-NLS-1$
					+ sipRO.getId() + " - " + e.getMessage()); //$NON-NLS-1$
			throw new IngestTaskException(
					"Exception ingesting Representation Object " //$NON-NLS-1$
							+ sipRO.getId() + " - " + e.getMessage(), e); //$NON-NLS-1$
		}

		try {

			// Upload root file
			this.rodaUploader.uploadRepresentationFile(roPID, sipRO
					.getRootFile());

			// Upload part files
			if (sipRO.getPartFiles() != null) {

				for (RepresentationFile rFile : sipRO.getPartFiles()) {
					this.rodaUploader.uploadRepresentationFile(roPID, rFile);
				}

			}

		} catch (Exception e) {
			logger.debug("Exception uploading Representation File - " //$NON-NLS-1$
					+ e.getMessage());
			throw new IngestTaskException(
					"Exception uploading Representation File - " //$NON-NLS-1$
							+ e.getMessage(), e);
		}

		SIPRepresentationObject createdRO = null;
		try {

			createdRO = new SIPRepresentationObject(this.browserService
					.getRepresentationObject(roPID));

		} catch (Exception e) {
			logger.debug("Exception getting ingested Representation Object " //$NON-NLS-1$
					+ roPID + " - " + e.getMessage()); //$NON-NLS-1$
			throw new IngestTaskException(
					"Exception getting ingested Representation Object " + roPID //$NON-NLS-1$
							+ " - " + e.getMessage(), e); //$NON-NLS-1$
		}

		// Create representation preservation object
		if (sipRO.getPreservationObject() != null) {

			SIPRepresentationPreservationObject createdRPO = createPreservationObject(
					sipRO.getPreservationObject(), createdRO, doPID);

			createdRO.setPreservationObject(createdRPO);
		}

		return createdRO;
	}

	private SIPRepresentationPreservationObject createPreservationObject(
			SIPRepresentationPreservationObject sipRPO,
			SIPRepresentationObject createdRO, String doPID)
			throws IngestTaskException {

		SIPRepresentationPreservationObject createdRPO = null;

		List<String> createdPIDs = new ArrayList<String>();

		try {

			// Create the representation preservation object (RPO)

			if (createdRO == null) {
				// sipRPO.setID();
				// sipRPO.setRepresentationObjectPID();
			} else {
				sipRPO.setID(createdRO.getPid());
				sipRPO.setRepresentationObjectPID(createdRO.getPid());
			}

			logger.debug("Creating RPO " + sipRPO);

			String createdRpoPID = this.ingestService
					.createRepresentationPreservationObject(sipRPO, doPID);
			createdPIDs.add(createdRpoPID);

			logger.debug("Created RPO PID " + createdRpoPID);

			createdRPO = new SIPRepresentationPreservationObject(
					this.browserService
							.getRepresentationPreservationObject(createdRpoPID));

			logger.debug("Created RPO " + createdRPO);

			// Create preservation events

			List<SIPEventPreservationObject> createdPreservationEvents = new ArrayList<SIPEventPreservationObject>();

			for (SIPEventPreservationObject eventPO : sipRPO
					.getPreservationEvents()) {

				eventPO.setState(RODAObject.STATE_INACTIVE);

				logger.debug("Creating EPO " + eventPO);

				String epoPID = this.ingestService.registerEvent(createdRPO
						.getPid(), eventPO, eventPO.getAgent());
				createdPIDs.add(epoPID);

				logger.debug("Created EPO PID " + epoPID);

				SIPEventPreservationObject createdEPO = new SIPEventPreservationObject(
						this.browserService.getEventPreservationObject(epoPID));

				logger.debug("Created EPO " + createdEPO);

				// SIPAgentPreservationObject createdAPO = new
				// SIPAgentPreservationObject(
				// this.browserService
				// .getAgentPreservationObject(createdEPO
				// .getAgentPID()));
				// createdEPO.setAgent(createdAPO);

				createdPreservationEvents.add(createdEPO);

			}

			createdRPO.setPreservationEvents(createdPreservationEvents);

			return createdRPO;

		} catch (Exception e) {

			logger.debug("Error creating preservation objects for RPO " //$NON-NLS-1$
					+ sipRPO.getID() + " - " + e.getMessage(), e); //$NON-NLS-1$

			if (createdPIDs.size() > 0) {

				logger.debug("Created objects (" + createdPIDs //$NON-NLS-1$
						+ ") are going to be removed"); //$NON-NLS-1$

				if (deleteTemporaryObject(createdPIDs)) {
					// Everything OK
				} else {
					logger
							.debug("An error occurred deleting temporary objects. IGNORING"); //$NON-NLS-1$
				}
			}

			throw new IngestTaskException(
					"Error creating preservation objects for RPO " //$NON-NLS-1$
							+ sipRPO.getID() + " - " + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	private void createDerivationRelationshipsAndOrphanPOs(
			SIPDescriptionObject createdDO, SIPDescriptionObject sipDO)
			throws IngestTaskException {

		List<SIPRepresentationObject> sipRepresentations = sipDO
				.getRepresentations();
		List<SIPRepresentationObject> createdRepresentations = createdDO
				.getRepresentations();

		for (int index = 0; index < sipRepresentations.size(); index++) {

			SIPRepresentationObject sipRO = sipRepresentations.get(index);
			SIPRepresentationObject createdRO = createdRepresentations
					.get(index);

			SIPRepresentationPreservationObject sipRPO = sipRO
					.getPreservationObject();

			if (sipRPO != null) {

				SIPRepresentationPreservationObject createdRPO = createdRO
						.getPreservationObject();

				createOrphanPreservationObjectsChain(createdRepresentations,
						sipRPO, createdRPO, createdDO.getPid());

			}
		}

	}

	private void createOrphanPreservationObjectsChain(
			List<SIPRepresentationObject> createdRepresentations,
			SIPRepresentationPreservationObject sipRPO,
			SIPRepresentationPreservationObject createdRPO, String doPID)
			throws IngestTaskException {

		SIPEventPreservationObject sipDerivationEPO = sipRPO
				.getDerivationEvent();

		if (sipDerivationEPO == null) {
			// No derivation event
		} else {
			// A derivation event exists, let's see if it is already
			// created

			logger.debug("Searching for a created derivation event equal to "
					+ sipDerivationEPO + " in created representations "
					+ createdRepresentations);

			SIPEventPreservationObject createdDerivationEPO = getROCreatedEventPreservationObject(
					createdRepresentations, sipDerivationEPO);

			logger.debug("Found createdDerivationEPO is "
					+ createdDerivationEPO);

			SIPRepresentationPreservationObject sipDerivedFromRPO = sipRPO
					.getDerivedFromRepresentationObject();
			SIPRepresentationPreservationObject createdDerivedFromRPO = null;

			if (createdDerivationEPO == null) {
				// It wasn't created. This means that the RPO is not
				// associated with any representation.
				// We need to create the RPO (and the associated EPOs)

				logger.debug("RPO " + sipDerivedFromRPO.getID() //$NON-NLS-1$
						+ " is not associated with any representation. " //$NON-NLS-1$
						+ "Creating the RPO and it's preservation events"); //$NON-NLS-1$

				createdDerivedFromRPO = createPreservationObject(
						sipDerivedFromRPO, null, doPID);

				logger
						.debug("Searching for a created derivation event equal to " //$NON-NLS-1$
								+ sipDerivationEPO + " in created events " //$NON-NLS-1$
								+ createdDerivedFromRPO.getPreservationEvents());

				createdDerivationEPO = getCreatedEventPreservationObject(
						createdDerivedFromRPO.getPreservationEvents(),
						sipDerivationEPO);

				logger.debug("Found createdDerivationEPO is " //$NON-NLS-1$
						+ createdDerivationEPO);

			} else {
				// It was created already.
			}

			createdRPO.setDerivationEvent(createdDerivationEPO);
			createdRPO.setDerivedFromRepresentationObject(createdDerivationEPO
					.getTarget());

			logger.debug("Associating RPO " + sipRPO.getID() + " (PID "
					+ createdRPO.getPid() + ")"
					+ " with it's derivation event "
					+ createdDerivationEPO.getLabel() + " ("
					+ createdDerivationEPO.getPid() + ")"
					+ " and with the original RPO "
					+ createdDerivedFromRPO.getID());

			// Create relationship between RPO and its derivation event (EPO)
			try {

				this.ingestService.createDerivationRelationship(createdRPO
						.getPid(), createdDerivationEPO.getPid());

			} catch (Exception e) {
				logger.debug("Error creating derivation relationship - " //$NON-NLS-1$
						+ e.getMessage(), e);
				throw new IngestTaskException(
						"Error creating derivation relationship - " //$NON-NLS-1$
								+ e.getMessage(), e);
			}

			// Recursively, create derivation events and derivedFrom RPOs, if
			// they exist.
			createOrphanPreservationObjectsChain(createdRepresentations,
					sipDerivedFromRPO, createdDerivedFromRPO, doPID);
		}

	}

	private SIPEventPreservationObject getROCreatedEventPreservationObject(
			List<SIPRepresentationObject> createdROs,
			SIPEventPreservationObject sipDerivationEPO) {

		SIPEventPreservationObject createdDerivationEPO = null;

		for (SIPRepresentationObject createdRO : createdROs) {

			if (createdRO.getPreservationObject() == null) {
				// no preservation object
			} else {

				List<SIPEventPreservationObject> createdEPOs = createdRO
						.getPreservationObject().getPreservationEvents();

				if (createdEPOs != null) {
					createdDerivationEPO = getCreatedEventPreservationObject(
							createdEPOs, sipDerivationEPO);
				}
			}
		}

		return createdDerivationEPO;
	}

	private SIPEventPreservationObject getCreatedEventPreservationObject(
			List<SIPEventPreservationObject> createdEPOs,
			SIPEventPreservationObject sipDerivationEPO) {

		SIPEventPreservationObject createdDerivationEPO = null;

		for (SIPEventPreservationObject createdEPO : createdEPOs) {

			SIPRepresentationPreservationObject sipTargetRPO = sipDerivationEPO
					.getTarget();
			SIPRepresentationPreservationObject createdTargetRPO = createdEPO
					.getTarget();

			// We can't compare the events by PID or ID because the
			// createdEPO has PID and ID, but the sipDerivationEPO
			// doesn't. So, we compare them by comparing the type,
			// datetime and target.
			if (createdEPO.getEventType().equals(
					sipDerivationEPO.getEventType())
					&& createdEPO.getDatetime().equals(
							sipDerivationEPO.getDatetime())
					&& sipTargetRPO.getID().equals(createdTargetRPO.getID())) {

				// Here it is!
				createdDerivationEPO = createdEPO;
			}

		}

		return createdDerivationEPO;
	}

	private boolean deleteTemporaryObject(String pid) {
		try {

			String[] removedObjects = this.ingestService
					.removeObjects(new String[] { pid });

			return (removedObjects != null && removedObjects.length == 1);

		} catch (IngestException e) {
			logger.error(
					"Error removing temporary objects - " + e.getMessage(), e); //$NON-NLS-1$
			return false;
		} catch (RemoteException e) {
			logger.error(
					"Error removing temporary objects - " + e.getMessage(), e); //$NON-NLS-1$
			return false;
		}
	}

	private boolean deleteTemporaryObject(List<String> pids) {
		try {

			String[] removedObjects = this.ingestService.removeObjects(pids
					.toArray(new String[pids.size()]));

			return (removedObjects != null && removedObjects.length == 1);

		} catch (IngestException e) {
			logger.error(
					"Error removing temporary objects - " + e.getMessage(), e); //$NON-NLS-1$
			return false;
		} catch (RemoteException e) {
			logger.error(
					"Error removing temporary objects - " + e.getMessage(), e); //$NON-NLS-1$
			return false;
		}
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
			this.rodaUploader = new Uploader(new URL(rodaClientServiceUrl),
					rodaClientUsername, rodaClientPassword);

			this.ingestService = this.rodaClient.getIngestService();
			this.browserService = this.rodaClient.getBrowserService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new IngestTaskException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}

}
