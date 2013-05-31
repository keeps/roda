package pt.gov.dgarq.roda.core.services;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.IngestHelper;
import pt.gov.dgarq.roda.core.common.EditorException;
import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.RepresentationAlreadyPreservedException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.PreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;

/**
 * This class implements the Ingest service.
 * 
 * @author Rui Castro
 */
public class Ingest extends RODAWebService {

	static final private Logger logger = Logger.getLogger(Ingest.class);

	private Map<String, IngestHelper> ingestHelperCache = new HashMap<String, IngestHelper>();

	/**
	 * Constructs a new instance of the {@link Ingest} service.
	 * 
	 * @throws RODAServiceException
	 */
	public Ingest() throws RODAServiceException {

		super();

		logger.info(getClass().getSimpleName() + " init OK");
	}

	/**
	 * Creates a {@link DescriptionObject} without a parent. The specified
	 * parent is used to access producers information.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to create.
	 * 
	 * @return a {@link String} with the PID of the newly created object.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified <code>doParentPID</code> doesn't exist.
	 * @throws InvalidDescriptionObjectException
	 *             if the specified <code>dObject</code> is invalid.
	 * @throws IngestException
	 */
	public String createDetachedDescriptionObject(DescriptionObject dObject)
			throws NoSuchRODAObjectException,
			InvalidDescriptionObjectException, IngestException {
		Date start = new Date();

		String pid = getIngestHelper().createDetachedDescriptionObject(dObject);
		long duration = new Date().getTime() - start.getTime();

		registerAction("Ingest.createDetachedDescriptionObject", new String[] {
				"descriptionObject", dObject.toString() },
				"User %username% called method Ingest.createDetachedDescriptionObject("
						+ dObject + ")", duration);

		return pid;
	}

	/**
	 * Creates a {@link DescriptionObject} with the specified parent.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to create.
	 * 
	 * @return a {@link String} with the PID of the newly created object.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if the specified <code>doParentPID</code> doesn't exist.
	 * @throws InvalidDescriptionObjectException
	 *             if the specified <code>dObject</code> is invalid.
	 * @throws IngestException
	 */
	public String createDescriptionObject(DescriptionObject dObject)
			throws NoSuchRODAObjectException,
			InvalidDescriptionObjectException, IngestException {
		Date start = new Date();

		String pid = getIngestHelper().createDescriptionObject(dObject);
		long duration = new Date().getTime() - start.getTime();

		registerAction("Ingest.createDescriptionObject", new String[] {
				"descriptionObject", dObject.toString() },
				"User %username% called method Ingest.createDescriptionObject("
						+ dObject + ")", duration);

		return pid;

	}

	/**
	 * Removes the {@link DescriptionObject} with PID <code>pid</code> and all
	 * it's descendants.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject} to remove.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws IngestException
	 */
	public void removeDescriptionObject(String doPID)
			throws NoSuchRODAObjectException, IngestException {

		try {

			Date start = new Date();

			getIngestHelper().removeDescriptionObject(doPID);

			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.removeDescriptionObject", new String[] {
					"doPID", doPID },
					"User %username% called method Ingest.removeDescriptionObject("
							+ doPID + ")", duration);

		} catch (Throwable e) {
			logger.debug(e.getMessage(), e);
			throw new IngestException(e.getMessage(), e);
		}
	}

	/**
	 * Creates a {@link RepresentationObject} for the given
	 * {@link DescriptionObject}.
	 * <p>
	 * <strong>NOTE:</strong> This method only creates the object and set it's
	 * properties, the {@link RepresentationFile}s are not created by this
	 * method. Use FileUpload servlet to add {@link RepresentationFile}s to the
	 * {@link RepresentationObject}.
	 * </p>
	 * 
	 * @param rObject
	 *            the {@link RepresentationObject} to create.
	 * 
	 * @return a {@link String} with the PID of the created
	 *         {@link RepresentationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws IngestException
	 */
	public String createRepresentationObject(RepresentationObject rObject)
			throws IngestException, NoSuchRODAObjectException {
		Date start = new Date();

		try {

			rObject.setState(RODAObject.STATE_INACTIVE);
			String pid = getIngestHelper().createRepresentationObject(rObject);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.createRepresentationObject", new String[] {
					"rObject", rObject.toString() },
					"User %username% called method Ingest.createRepresentationObject("
							+ rObject + ")", duration);

			return pid;

		} catch (EditorException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestException(e.getMessage(), e);
		}
	}

	/**
	 * Creates a {@link RepresentationPreservationObject}.
	 * 
	 * @param rpo
	 *            the {@link RepresentationPreservationObject} to create.
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}
	 * 
	 * @return a {@link String} with the PID of the created
	 *         {@link RepresentationPreservationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws RepresentationAlreadyPreservedException
	 * @throws IngestException
	 */
	public String createRepresentationPreservationObject(
			RepresentationPreservationObject rpo, String doPID)
			throws NoSuchRODAObjectException,
			RepresentationAlreadyPreservedException, IngestException {

		Date start = new Date();

		try {

			String pid = getIngestHelper()
					.createRepresentationPreservationObject(rpo, doPID);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.createRepresentationPreservationObject",
					new String[] { "rpo", rpo.toString(), "doPID", doPID },
					"User %username% called method Ingest.createRepresentationPreservationObject("
							+ rpo + ", " + doPID + ")", duration);

			return pid;

		} catch (EditorException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestException(e.getMessage(), e);
		}
	}

	/**
	 * Sets the specified {@link RepresentationObject} as the the normalized
	 * representation of the specified {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * @param roPID
	 *            the PID of the {@link RepresentationObject}.
	 * 
	 * @return a {@link String} with the PID of the {@link RepresentationObject}
	 *         that is the current normalized representation.
	 * 
	 * @throws NoSuchRODAObjectException
	 *             if one of the given PIDs (doPID or roPID) doesn't exist.
	 * @throws IngestException
	 */
	public String setDONormalizedRepresentation(String doPID, String roPID)
			throws NoSuchRODAObjectException, IngestException {
		Date start = new Date();

		try {

			String pid = getIngestHelper().setDONormalizedRepresentation(doPID,
					roPID);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.setDONormalizedRepresentation",
					new String[] { "doPID", doPID, "roPID", roPID },
					"User %username% called method Ingest.setDONormalizedRepresentation("
							+ doPID + ", " + roPID + ")", duration);

			return pid;

		} catch (EditorException e) {
			logger.debug(e.getMessage(), e);
			throw new IngestException(e.getMessage(), e);
		}
	}

	/**
	 * Creates a relationship (<em>derived-from</em>) between the
	 * {@link RepresentationPreservationObject} and the
	 * {@link EventPreservationObject} from which the
	 * {@link RepresentationPreservationObject} has derived.
	 * 
	 * @param rpoPID
	 *            the PID of the {@link RepresentationPreservationObject}.
	 * @param derivationEventPID
	 *            the PID of the {@link EventPreservationObject}.
	 * 
	 * @throws IngestException
	 */
	public void createDerivationRelationship(String rpoPID,
			String derivationEventPID) throws IngestException {

		Date start = new Date();

		try {

			getIngestHelper().createDerivationRelationship(rpoPID,
					derivationEventPID);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.createDerivationRelationship",
					new String[] { "rpoPID", rpoPID, "derivationEventPID",
							derivationEventPID },
					"User %username% called method Ingest.createDerivationRelationship("
							+ rpoPID + ", " + derivationEventPID + ")",
					duration);

		} catch (IngestException e) {
			logger.debug(e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * Create preservation metadata relative to the ingestion of the given
	 * objects. Creates {@link RepresentationPreservationObject}s for
	 * {@link RepresentationObject}s and register the ingest
	 * {@link EventPreservationObject}.
	 * 
	 * @param doPIDs
	 *            the PIDs of the {@link DescriptionObject}s.
	 * @param roPIDs
	 *            the PIDs of the {@link RepresentationObject}s.
	 * @param poPIDs
	 *            the PIDs of the {@link PreservationObject}s.
	 * @param agentName
	 *            the name of the agent responsible for the ingestion.
	 * @param details
	 *            the details of the ingest operation.
	 * 
	 * 
	 * @return the PID of the ingestion {@link EventPreservationObject}.
	 * 
	 * @throws IngestException
	 */
	public String registerIngestEvent(String[] doPIDs, String[] roPIDs,
			String[] poPIDs, String agentName, String details)
			throws IngestException {
		Date start = new Date();

		try {

			String pid = getIngestHelper().registerIngestEvent(doPIDs, roPIDs,
					poPIDs, agentName, details);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.registerIngestEvent", new String[] {
					"doPIDs", Arrays.toString(doPIDs), "roPIDs",
					Arrays.toString(roPIDs), "poPIDs", Arrays.toString(poPIDs),
					"agentName", agentName, "details", details },
					"User %username% called method Ingest.registerIngestEvent("
							+ Arrays.toString(doPIDs) + ", "
							+ Arrays.toString(roPIDs) + ", "
							+ Arrays.toString(poPIDs) + ", " + agentName + ", "
							+ details + ")", duration);

			return pid;

		} catch (EditorException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestException(e.getMessage(), e);
		} catch (RepresentationAlreadyPreservedException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestException(e.getMessage(), e);
		}

	}

	/**
	 * Create preservation metadata relative to a single event in a
	 * {@link RepresentationObject}.
	 * 
	 * @param rpoPID
	 *            the PIDs of the {@link RepresentationPreservationObject}.
	 * @param eventPO
	 *            the {@link EventPreservationObject}.
	 * @param agent
	 *            the {@link AgentPreservationObject}.
	 * 
	 * 
	 * @return the PID of the {@link EventPreservationObject}.
	 * 
	 * @throws IngestException
	 */
	public String registerEvent(String rpoPID, EventPreservationObject eventPO,
			AgentPreservationObject agent) throws IngestException {
		Date start = new Date();

		try {

			String pid = getIngestHelper()
					.registerEvent(rpoPID, eventPO, agent);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.registerEvent", new String[] { "rpoPID",
					rpoPID, "eventPO", "" + eventPO, "agent", "" + agent },
					"User %username% called method Ingest.registerIngestion("
							+ rpoPID + ", " + eventPO + ", " + agent + ")",
					duration);

			return pid;

		} catch (EditorException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestException(e.getMessage(), e);
		}
	}

	/**
	 * Create preservation metadata relative to a derivation event in a
	 * {@link RepresentationObject}.
	 * 
	 * @param originalRepresentationPID
	 *            the PID of the original {@link RepresentationObject}.
	 * @param derivedRepresentationPID
	 *            the PID of the derived {@link RepresentationObject}.
	 * @param eventPO
	 *            the {@link EventPreservationObject}.
	 * @param agentPO
	 *            the {@link AgentPreservationObject}.
	 * @param markObjectsActive
	 *            should the created objects be marked active?
	 * 
	 * @return the PID of the {@link EventPreservationObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws IngestException
	 */
	public String registerDerivationEvent(String originalRepresentationPID,
			String derivedRepresentationPID, EventPreservationObject eventPO,
			AgentPreservationObject agentPO, boolean markObjectsActive)
			throws IngestException, NoSuchRODAObjectException {
		Date start = new Date();

		try {

			String pid = getIngestHelper().registerDerivationEvent(
					originalRepresentationPID, derivedRepresentationPID,
					eventPO, agentPO, markObjectsActive);
			long duration = new Date().getTime() - start.getTime();

			registerAction("Ingest.registerDerivationEvent", new String[] {
					"originalRepresentationPID", originalRepresentationPID,
					"derivedRepresentationPID", derivedRepresentationPID,
					"eventPO", "" + eventPO, "agentPO", "" + agentPO },
					"User %username% called method Ingest.registerDerivationEvent("
							+ originalRepresentationPID + ", "
							+ derivedRepresentationPID + ", " + eventPO + ", "
							+ agentPO + ")", duration);

			return pid;

		} catch (EditorException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new IngestException(e.getMessage(), e);
		}
	}

	/**
	 * Remove the given objects.
	 * 
	 * @param pids
	 *            the PIDs of the objects to remove.
	 * 
	 * @return an array of {@link String} with the PIDs of the removed objects.
	 * 
	 * @throws IngestException
	 */
	public String[] removeObjects(String[] pids) throws IngestException {
		Date start = new Date();

		List<String> removedObjects = getIngestHelper().removeObjects(pids);
		String[] result = removedObjects.toArray(new String[removedObjects
				.size()]);
		long duration = new Date().getTime() - start.getTime();

		registerAction("Ingest.removeObjects", new String[] { "pids",
				Arrays.toString(pids) },
				"User %username% called method Ingest.removeObjects("
						+ Arrays.asList(pids) + ")", duration);

		return result;
	}

	private IngestHelper getIngestHelper() throws IngestException {

		User clientUser = getClientUser();

		if (clientUser != null) {

			String usernamePasswordKey = clientUser.getName()
					+ getClientUserPassword();

			if (!this.ingestHelperCache.containsKey(usernamePasswordKey)) {

				try {

					String fedoraURL = getConfiguration()
							.getString("fedoraURL");
					String fedoraGSearchURL = getConfiguration().getString(
							"fedoraGSearchURL");

					FedoraClientUtility fedoraClientUtility = new FedoraClientUtility(
							fedoraURL, fedoraGSearchURL, clientUser,
							getClientUserPassword());

					this.ingestHelperCache.put(usernamePasswordKey,
							new IngestHelper(fedoraClientUtility,
									getConfiguration()));

				} catch (FedoraClientException e) {
					logger.debug("Error creating Fedora client - "
							+ e.getMessage(), e);
					throw new IngestException("Error creating Fedora client - "
							+ e.getMessage(), e);
				} catch (EditorException e) {
					logger.debug("Error creating Fedora client - "
							+ e.getMessage(), e);
					throw new IngestException("Error creating Fedora client - "
							+ e.getMessage(), e);
				} catch (MalformedURLException e) {
					logger.debug("Bad URL for Fedora client - "
							+ e.getMessage(), e);
					throw new IngestException("Bad URL for Fedora client - "
							+ e.getMessage(), e);
				}

			}

			return this.ingestHelperCache.get(usernamePasswordKey);

		} else {

			throw new IngestException("User credentials are not available.");

		}
	}

}
