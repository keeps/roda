package pt.gov.dgarq.roda.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;

import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.EditorException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevelManager;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearch;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearchException;
import pt.gov.dgarq.roda.core.metadata.DescriptionObjectValidator;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCHelper;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyHelper;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyMetadataException;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.util.StreamUtility;
import pt.gov.dgarq.roda.x2014.eadcSchema.EadCDocument;

/**
 * @author Rui Castro
 */
public class EditorHelper {
	static final private Logger logger = Logger.getLogger(EditorHelper.class);

	protected final String[] publicReadGroups;

	protected final String[] minimumReadUsers;
	protected final String[] minimumReadGroups;

	protected final String[] minimumModifyUsers;
	protected final String[] minimumModifyGroups;

	protected final String[] minimumRemoveUsers;
	protected final String[] minimumRemoveGroups;

	protected final String[] minimumGrantUsers;
	protected final String[] minimumGrantGroups;

	protected final String descriptionObjectDatastreamID;
	protected final String preservationObjectDatastreamID;
	protected final String relsExtDatastreamID;
	protected final boolean descriptionObjectReplaceID;

	protected String casURL;
	protected String coreURL;

	private FedoraClientUtility adminFedoraClientUtility = null;
	private FedoraClientUtility fedoraClientUtility = null;

	private CASUtility casUtility = null;

	protected BrowserHelper browserHelper = null;

	/**
	 * Constructs a new EditorHelper instance.
	 * 
	 * @param fedoraClientUtility
	 *            an instance of {@link FedoraClientUtility}.
	 * @param configuration
	 * 
	 * @throws EditorException
	 */
	public EditorHelper(FedoraClientUtility fedoraClientUtility,
			Configuration configuration) throws EditorException {

		if (fedoraClientUtility == null) {
			throw new NullPointerException("fedoraClientUtility cannot be null");
		}

		this.fedoraClientUtility = fedoraClientUtility;
		this.browserHelper = new BrowserHelper(fedoraClientUtility,
				configuration);

		this.publicReadGroups = configuration
				.getStringArray("publicReadGroups");

		this.minimumReadUsers = configuration
				.getStringArray("minimumReadUsers");
		this.minimumReadGroups = configuration
				.getStringArray("minimumReadGroups");

		this.minimumModifyUsers = configuration
				.getStringArray("minimumModifyUsers");
		this.minimumModifyGroups = configuration
				.getStringArray("minimumModifyGroups");

		this.minimumRemoveUsers = configuration
				.getStringArray("minimumRemoveUsers");
		this.minimumRemoveGroups = configuration
				.getStringArray("minimumRemoveGroups");

		this.minimumGrantUsers = configuration
				.getStringArray("minimumGrantUsers");
		this.minimumGrantGroups = configuration
				.getStringArray("minimumGrantGroups");

		this.preservationObjectDatastreamID = configuration
				.getString("preservationObjectDatastreamID");
		this.descriptionObjectDatastreamID = configuration
				.getString("descriptionObjectDatastreamID");
		this.relsExtDatastreamID = configuration
				.getString("relsExtDatastreamID");
		this.descriptionObjectReplaceID = configuration
				.getBoolean("descriptionObjectReplaceID");
		this.casURL = configuration.getString("roda.cas.url");
		this.coreURL = configuration.getString("roda.core.url");

		try {
			this.casUtility = new CASUtility(new URL(casURL), new URL(coreURL));

			String fedoraURL = configuration.getString("fedoraURL");
			String fedoraGSearchURL = configuration
					.getString("fedoraGSearchURL");
			String adminUsername = configuration.getString("adminUsername");
			String adminPassword = configuration.getString("adminPassword");

			CASUserPrincipal cup = casUtility.getCASUserPrincipal(
					adminUsername, adminPassword);

			this.adminFedoraClientUtility = new FedoraClientUtility(fedoraURL,
					fedoraGSearchURL, cup, casUtility);

		} catch (MalformedURLException mfue) {
			logger.error(
					"Error creating CASUtility for admin - "
							+ mfue.getMessage(), mfue);
			throw new EditorException("Error creating CASUtility for admin - "
					+ mfue.getMessage(), mfue);
		} catch (Exception e) {
			logger.error(
					"Error creating FedoraClientUtility for admin - "
							+ e.getMessage(), e);
			throw new EditorException(
					"Error creating FedoraClientUtility for admin - "
							+ e.getMessage(), e);
		}

	}

	/**
	 * Returns the current instance of the {@link BrowserHelper}.
	 * 
	 * @return a {@link BrowserHelper}.
	 */
	public BrowserHelper getBrowserHelper() {
		return this.browserHelper;
	}

	/**
	 * Creates a {@link DescriptionObject} without a parent. The
	 * {@link DescriptionObject} created this way is temporary if is not a fonds
	 * or while is not connected to a parent {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to create.
	 * @param permissions
	 * 
	 * @return a {@link String} with the PID of the newly created object.
	 * 
	 * @throws InvalidDescriptionObjectException
	 * @throws EditorException
	 */
	public String createSingleDescriptionObject(DescriptionObject dObject,
			RODAObjectPermissions permissions) throws EditorException,
			InvalidDescriptionObjectException {

		return createSingleDescriptionObject(dObject, permissions, null);

	}

	/**
	 * Creates a {@link DescriptionObject} without a parent. The
	 * {@link DescriptionObject} created this way is temporary if is not a fonds
	 * or while is not connected to a parent {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to create.
	 * 
	 * @return a {@link String} with the PID of the newly created object.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws InvalidDescriptionObjectException
	 * @throws EditorException
	 */
	public String createDescriptionObject(DescriptionObject dObject)
			throws NoSuchRODAObjectException,
			InvalidDescriptionObjectException, EditorException {

		return createDescriptionObject(dObject, null);
	}

	/**
	 * Creates a {@link DescriptionObject} without a parent. The
	 * {@link DescriptionObject} created this way is temporary if is not a fonds
	 * or while is not connected to a parent {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to create.
	 * @param permissions
	 * 
	 * @return a {@link String} with the PID of the newly created object.
	 * 
	 * @throws InvalidDescriptionObjectException
	 * @throws EditorException
	 */
	// FIXME revise javadocs
	public String createSingleDescriptionObject(DescriptionObject dObject,
			RODAObjectPermissions permissions, String otherMetadataFilePath)
			throws EditorException, InvalidDescriptionObjectException {

		if (StringUtils.isBlank(dObject.getCountryCode())) {
			throw new InvalidDescriptionObjectException(
					"countryCode cannot be empty");
		}
		if (StringUtils.isBlank(dObject.getRepositoryCode())) {
			throw new InvalidDescriptionObjectException(
					"repositoryCode cannot be empty");
		}

		String createdPID = null;
		try {

			String pid = getFedoraClientUtility().getNextPID();
			logger.info("Next PID is " + pid);

			// Set the PID of the description object
			dObject.setPid(pid);

			/*
			 * RELS-EXT datastream
			 */

			// Create an empty RDF stream for DO
			InputStream rdfInputStream = getFedoraClientUtility().getEmptyRDF(
					pid);

			// Add SimpleDO properties
			rdfInputStream = getFedoraClientUtility()
					.modifyRDFSingleProperties(rdfInputStream,
							getSimpleDORelsExtProperties(dObject));
			logger.info("Added Simple DO properties to RELS-EXT");

			rdfInputStream = getFedoraClientUtility()
					.modifyRDFResourceProperties(rdfInputStream,
							getSimpleDOChildOfProperty(dObject));
			logger.info("Added Simple DO 'child-of' property to RELS-EXT");

			// Set permissions
			permissions.setObjectPID(pid);
			permissions = makeConsistentPermissions(permissions);

			rdfInputStream = getFedoraClientUtility()
					.addRDFMultivalueProperties(rdfInputStream,
							getPermissionRelsExtProperties(permissions));
			logger.info("Added permissions properties to RELS-EXT");

			String rdfAsString = StreamUtility
					.inputStreamToString(rdfInputStream);

			logger.debug("RELS-EXT datastream is " + rdfAsString);

			/*
			 * POLICY datastream
			 */

			// Create a PolicyDocument from the RODAObjectPermissions and save
			// it to a byte array
			byte[] policyByteArray = new PolicyHelper(permissions)
					.saveToByteArray(false);

			String policyAsString = StreamUtility
					.inputStreamToString(new ByteArrayInputStream(
							policyByteArray));

			logger.debug("POLICY datastream is " + policyAsString);

			/*
			 * EAD-C datastream
			 */

			// Transforms the Description Object into a byte array to send to
			// Fedora
			byte[] eadcByteArray = new EadCHelper(dObject)
					.saveToByteArray(false);

			String eadcAsString = StreamUtility
					.inputStreamToString(new ByteArrayInputStream(eadcByteArray));

			logger.debug("EAD-C datastream is " + eadcAsString);

			/*
			 * Other Metadata datastream (other than EAD-C, if it exists)
			 */
			String otherMetadataAsString = null;
			logger.debug("otherMetadataFilePath: "
					+ otherMetadataFilePath);
			if (StringUtils.isNotBlank(otherMetadataFilePath)) {
				otherMetadataAsString = StreamUtility
						.inputStreamToString(new FileInputStream(
								otherMetadataFilePath));
				logger.debug("Other Metadata datastream is "
						+ otherMetadataAsString);
			}

			createdPID = getFedoraClientUtility().createDescriptionObject(
					dObject, rdfAsString, policyAsString, eadcAsString,
					otherMetadataAsString);

			if (createdPID.equals(pid)) {
				logger.info("Created PID " + createdPID
						+ " is equal to the generated PID " + pid);
			} else {
				logger.error("Created PID " + createdPID
						+ " is diferent to the generated PID " + pid);

				throw new EditorException("Created PID " + createdPID
						+ " is diferent to the generated PID " + pid);
			}

			return pid;

		} catch (Exception e) {

			if (createdPID != null) {

				logger.debug("Purging created DO " + createdPID);

				// If object was created, delete it
				try {

					getFedoraClientUtility().purgeObject(createdPID);

				} catch (Exception e1) {
					logger.warn("Exception purging object " + createdPID
							+ ". IGNORING", e1);
				}
			}

			throw new EditorException(e.getMessage(), e);
		}

	}

	/**
	 * Creates a {@link DescriptionObject} without a parent. The
	 * {@link DescriptionObject} created this way is temporary if is not a fonds
	 * or while is not connected to a parent {@link DescriptionObject}.
	 * 
	 * @param dObject
	 *            the {@link DescriptionObject} to create.
	 * 
	 * @return a {@link String} with the PID of the newly created object.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws InvalidDescriptionObjectException
	 * @throws EditorException
	 */
	// FIXME revise javadocs
	public String createDescriptionObject(DescriptionObject dObject,
			String otherMetadataFilePath) throws NoSuchRODAObjectException,
			InvalidDescriptionObjectException, EditorException {

		RODAObjectPermissions permissions;

		if (dObject.getParentPID() == null) {

			if (!DescriptionLevelManager.getRootDescriptionLevels().contains(
					dObject.getLevel())) {

				throw new InvalidDescriptionLevel(
						"A description object without a parent must have level fonds. Description object has level "
								+ dObject.getLevel());

			} else {
				// It's a fonds. OK
				permissions = getDefaultPermissions(null);
			}

		} else {

			SimpleDescriptionObject parentSDO;

			try {

				parentSDO = getBrowserHelper().getSimpleDescriptionObject(
						dObject.getParentPID());

			} catch (BrowserException e) {
				logger.debug(
						"Exception getting parent SDO - " + e.getMessage(), e);
				throw new EditorException("Exception getting parent SDO - "
						+ e.getMessage(), e);
			}

			if (!DescriptionLevelManager.getChildLevels(parentSDO.getLevel())
					.contains(dObject.getLevel())) {

				throw new InvalidDescriptionLevel("child level ("
						+ dObject.getLevel()
						+ ") is higher than its parent's level ("
						+ parentSDO.getLevel() + ")");
			} else {
				// Continue...

				try {

					permissions = getBrowserHelper().getRODAObjectPermissions(
							dObject.getParentPID());

				} catch (Exception e) {
					throw new EditorException(
							"Exception getting permissions of parent DO "
									+ dObject.getParentPID() + " - "
									+ e.getMessage(), e);
				}
			}

		}

		return createSingleDescriptionObject(dObject, permissions,
				otherMetadataFilePath);
	}

	/**
	 * 
	 * @param dObject
	 * 
	 * @return the modified {@link DescriptionObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws InvalidDescriptionObjectException
	 * @throws EditorException
	 * @throws BrowserException
	 * @throws RemoteException
	 */
	public DescriptionObject modifyDescriptionObject(DescriptionObject dObject,
			byte[] originalMetadata, String streamID,
			LinkedList<DescriptionObject> parents) throws EditorException,
			NoSuchRODAObjectException, BrowserException,
			InvalidDescriptionObjectException, RemoteException {

		// Get the SimpleDescriptionObject we want to edit - throws
		// NoSuchRODAObjectException
		SimpleDescriptionObject currentSDO = getBrowserHelper()
				.getSimpleDescriptionObject(dObject.getPid());

		// Throws InvalidDescriptionObjectException
		DescriptionObjectValidator.validateDescriptionObject(dObject);

		// Create an EAD-C from the DescriptionObject
		EadCHelper eadcHelper = new EadCHelper(dObject);
		EadCDocument eadcDocument = eadcHelper.getEadcDocument();

		// Create an XmlOptions instance and set the error listener.
		XmlOptions validateOptions = new XmlOptions();
		List<XmlError> errorList = new ArrayList<XmlError>();
		validateOptions.setErrorListener(errorList);

		// Validate the XML.
		boolean isValid = eadcDocument.validate(validateOptions);

		if (!isValid) {

			// If the XML isn't valid, loop through the listener's contents,
			// printing contained messages.
			for (XmlError xmlError : errorList) {
				logger.error("XmlError: " + xmlError);
			}

			throw new EditorException("Description object is not valid. "
					+ errorList);

		} else {

			try {

				if (!currentSDO.getLabel().equals(dObject.getId())) {

					// Update the RODAObject#label to match the
					// DescriptionObject#id

					getFedoraClientUtility().getAPIM().modifyObject(
							dObject.getPid(),
							getFedoraClientUtility().getStateCode(dObject),
							dObject.getId(),
							getFedoraClientUtility().getCASUserPrincipal()
									.getName(),
							"Modified by RODA Core Services");
				}

				setSimpleDOProperties(dObject, null);

				getFedoraClientUtility().getAPIM().modifyDatastreamByValue(
						dObject.getPid(), this.descriptionObjectDatastreamID,
						new String[0],
						"Encoded Archival Description Component", "text/xml",
						null, eadcHelper.saveToByteArray(), null, null,
						"Modified by RODA Core Services", false);

			} catch (Exception e) {

				logger.error(
						"Exception storing description object - "
								+ e.getMessage(), e);

				throw new EditorException(
						"Exception storing description object - "
								+ e.getMessage(), e);
			}

			// If the DO has a different ID the complete reference of all it's
			// children it's going to change.
			// The children's completeReferences need to be updated
			String oldID = currentSDO.getId();
			String newID = dObject.getId();

			boolean idChanged = !oldID.equals(newID);

			// If the DO has a different parent, it means it was moved
			// The children's completeReferences need to be updated
			String oldParentPID = currentSDO.getParentPID();
			String newParentPID = dObject.getParentPID();

			boolean parentChanged = (oldParentPID == null && newParentPID != null)
					|| (oldParentPID != null && !oldParentPID
							.equals(newParentPID));

			if (idChanged || parentChanged) {

				// This thread will get a list of all the descendants of
				// 'pid' and update the GSearch indexes for them.
				new GSearchUpdater(getFedoraClientUtility(), dObject.getPid())
						.start();
			}
		}

		return this.browserHelper.getDescriptionObject(dObject.getPid());
	}

	

	/**
	 * 
	 * @param dObject
	 * 
	 * @return the modified {@link DescriptionObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws InvalidDescriptionObjectException
	 * @throws EditorException
	 * @throws BrowserException
	 * @throws EadCMetadataException
	 * @throws RemoteException
	 * @throws MetadataException
	 */
	public DescriptionObject modifyDescriptionObject(DescriptionObject dObject)
			throws EditorException, BrowserException,
			NoSuchRODAObjectException, InvalidDescriptionObjectException,
			RemoteException, EadCMetadataException, MetadataException {
		LinkedList<DescriptionObject> parents = new LinkedList<DescriptionObject>();

		boolean exit = false;

		String pPID = null;
		while (!exit) {
			try {
				if (pPID == null) {
					pPID = dObject.getParentPID();
				}

				logger.error("ParentPID: " + pPID);
				DescriptionObject parentDO = getBrowserHelper()
						.getDescriptionObject(pPID);

				parents.add(parentDO);
				pPID = parentDO.getParentPID();
				if (pPID == null) {
					exit = true;
				}
			} catch (Exception e) {
				exit = true;
			}
		}
		return modifyDescriptionObject(dObject, null, null, parents);
	}

	/**
	 * Removes the {@link DescriptionObject} with PID <code>pid</code> and all
	 * it's descendants.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject} to remove.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void removeDescriptionObject(String doPID)
			throws NoSuchRODAObjectException, EditorException {

		SimpleDescriptionObject sdo = null;
		try {

			sdo = getBrowserHelper().getSimpleDescriptionObject(doPID);

		} catch (BrowserException e) {
			logger.debug(
					"Exception getting DO " + doPID + " - " + e.getMessage(), e);
			throw new EditorException("Exception getting DO " + doPID + " - "
					+ e.getMessage(), e);
		}

		if (sdo.getState().equals(RODAObject.STATE_INACTIVE)) {

			logger.warn("DO "
							+ doPID
							+ " cannot be removed because is inactive. Maybe it belongs to a SIP being ingested.");
			throw new EditorException(
					"DO "
							+ doPID
							+ " cannot be removed because is inactive. Maybe it belongs to a SIP being ingested.");
		} else {
			// DO is not inactive, continue.
		}

		logger.info("Removing DO " + doPID);

		// When the relationship with the parent is removed, the DO is not
		// longer visible.
		// So, the DO can be considered removed at this point.
		// No exception should be thrown from now on.

		try {

			List<String> descendantPIDs = getBrowserHelper()
					.getDODescendantPIDs(doPID);

			// Remove the object 'doPID'
			removeObjects(doPID);
			logger.info("DO " + doPID + " removed.");

			if (descendantPIDs.size() > 0) {
				logger.info("DO " + doPID + " has " + descendantPIDs.size()
						+ " descendants. Removing all descendants");

				logger.debug("DO descendants: " + descendantPIDs);

				removeObjects(descendantPIDs);
			}

		} catch (BrowserException e) {
			logger.debug(
					"Exception removing DO " + doPID + " - " + e.getMessage()
							+ " - IGNORED", e);
		}
	}

	/**
	 * Gets all the possible {@link DescriptionLevel}s a
	 * {@link DescriptionObject} can have, based on it's current position in the
	 * description tree.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * 
	 * @return a {@link List} of {@link DescriptionLevel}s.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public List<DescriptionLevel> getDOPossibleLevels(String doPID)
			throws NoSuchRODAObjectException, EditorException {

		try {

			// Get SimpleDescriptionObject with PID doPID
			SimpleDescriptionObject sdo = getBrowserHelper()
					.getSimpleDescriptionObject(doPID);

			// Get it's parent's level
			String parentLevel = getFedoraClientUtility().getFedoraRISearch()
					.getDOParentLevel(doPID);

			List<DescriptionLevel> possibleLevels;

			if (parentLevel == null) {
				logger.info(doPID + " doesn't have a parent.");
				possibleLevels = new ArrayList<DescriptionLevel>(
						DescriptionLevelManager.getRootDescriptionLevels());
			} else {
				logger.info(doPID + " has a parent with level " + parentLevel);
				// if it has a parent, then the possible levels are the ones
				// that can be
				// child of it
				possibleLevels = new ArrayList<DescriptionLevel>(
						DescriptionLevelManager
								.getChildLevels(new DescriptionLevel(
										parentLevel)));
			}

			logger.debug("possible levels: " + possibleLevels);

			// Get it's children's levels
			List<String> subElementsLevels = getFedoraClientUtility()
					.getFedoraRISearch().getDOChildrenLevels(doPID);

			if (subElementsLevels.size() > 0) {
				for (String childLevel : subElementsLevels) {
					possibleLevels.retainAll(DescriptionLevelManager
							.getParentLevels(childLevel));
					logger.debug("possible levels (after \"" + childLevel
							+ "\"): " + possibleLevels);
				}
			}

			logger.info("possible description levels: " + possibleLevels);

			return possibleLevels;

		} catch (FedoraRISearchException e) {
			logger.debug(
					"Exception getting possible levels - " + e.getMessage(), e);
			throw new EditorException("Exception getting possible levels - "
					+ e.getMessage(), e);
		} catch (BrowserException e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}

	}

	public String getDONextChildID(String doPID) throws EditorException {

		List<String> childIDs;
		try {

			childIDs = this.adminFedoraClientUtility.getFedoraRISearch()
					.getDOChildrenIDs(doPID);

		} catch (FedoraRISearchException e) {
			logger.debug(e.getMessage(), e);
			throw new EditorException(e.getMessage(), e);
		}

		List<String> childNumberIDs = new ArrayList<String>();

		for (String childID : childIDs) {

			try {
				Integer.parseInt(childID);
				// It's a number
				childNumberIDs.add(childID);

			} catch (NumberFormatException e) {
				// It's not a number
			}
		}

		Collections.sort(childNumberIDs);

		String nextID;

		if (childNumberIDs.size() == 0) {
			nextID = "1";
		} else {
			nextID = childNumberIDs.get(childNumberIDs.size() - 1);
		}

		return nextID;
	}

	/**
	 * Sets the {@link Producers} for a Fonds.
	 * 
	 * @param doPID
	 *            the PID of the Fonds {@link DescriptionObject} or any of it's
	 *            descendants.
	 * @param producers
	 *            the {@link Producers} to set.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void setDOProducers(String doPID, Producers producers)
			throws NoSuchRODAObjectException, EditorException {

		try {

			List<String> ancestorPIDs = this.fedoraClientUtility
					.getFedoraRISearch().getDOAncestorPIDs(doPID);

			String fondsPID = ancestorPIDs.get(0);

			getFedoraClientUtility().setDOProducerProperties(fondsPID,
					producers);

		} catch (FedoraClientException e) {
			logger.debug("Exception setting producers - " + e.getMessage(), e);
			throw new EditorException("Exception setting producers - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * @param pids
	 * @return a {@link List} of {@link String}s with the PIDs of the removed
	 *         objects.
	 */
	public List<String> removeObjects(String... pids) {
		return removeObjects(Arrays.asList(pids));
	}

	/**
	 * @param pids
	 * @return a {@link List} of {@link String}s with the PIDs of the removed
	 *         objects.
	 */
	public List<String> removeObjects(List<String> pids) {

		try {

			return getFedoraClientUtility().purgeObjects(pids, false);

		} catch (FedoraClientException e) {

			// This should never happen because stopAtError parameter is false.
			logger.error(
					"Unexpected exception removing objects - " + e.getMessage()
							+ ". PLEASE INFORM DEVELOPERS!!!", e);

			return new ArrayList<String>();
		}

	}

	/**
	 * Sets the permissions specified in the {@link RODAObjectPermissions}
	 * given.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions} to set.
	 * 
	 * @return the new permissions for the target {@link RODAObject}.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public RODAObjectPermissions setRODAObjectPermissions(
			RODAObjectPermissions permissions)
			throws NoSuchRODAObjectException, EditorException {

		try {

			// Get the RODAObject we want to modify
			@SuppressWarnings("unused")
			RODAObject rodaObject = this.browserHelper
					.getRODAObject(permissions.getObjectPID());

		} catch (BrowserException e) {

			logger.debug(
					"Exception accessing object " + permissions.getObjectPID()
							+ " - " + e.getMessage(), e);
			throw new EditorException("Exception accessing object "
					+ permissions.getObjectPID() + " - " + e.getMessage(), e);
		}

		setPermissions(permissions);

		try {

			return this.browserHelper.getRODAObjectPermissions(permissions
					.getObjectPID());

		} catch (BrowserException e) {

			logger.debug("Exception reading POLICY file - " + e.getMessage(), e);
			throw new EditorException("Exception reading POLICY file - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Sets the permissions specified in the {@link RODAObjectPermissions}
	 * given.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions} to set.
	 * @param recursive
	 *            the permissions should be applied recursively?
	 * @param stopAtFirstError
	 *            the recursion should stop if an error is encountered?
	 * 
	 * @return the new permissions for the target {@link RODAObject}s.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public RODAObjectPermissions setRODAObjectPermissions(
			RODAObjectPermissions permissions, boolean recursive,
			boolean stopAtFirstError) throws NoSuchRODAObjectException,
			EditorException {

		String parentDOPID = permissions.getObjectPID();

		try {

			// Get the RODAObject we want to modify
			@SuppressWarnings("unused")
			RODAObject rodaObject = this.browserHelper
					.getRODAObject(permissions.getObjectPID());

		} catch (BrowserException e) {

			logger.debug("Exception accessing object " + parentDOPID + " - "
					+ e.getMessage(), e);
			throw new EditorException("Exception accessing object "
					+ parentDOPID + " - " + e.getMessage(), e);
		}

		setPermissions(permissions, recursive, stopAtFirstError);

		try {

			return this.browserHelper.getRODAObjectPermissions(permissions
					.getObjectPID());

		} catch (BrowserException e) {

			logger.debug("Exception reading POLICY file - " + e.getMessage(), e);
			throw new EditorException("Exception reading POLICY file - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Gets the default permissions for objects.
	 * 
	 * @param PID
	 *            the PID of the object that is the target of the permissions.
	 * 
	 * @return a {@link RODAObjectPermissions}.
	 */
	public RODAObjectPermissions getDefaultPermissions(String PID) {

		RODAObjectPermissions permissions = new RODAObjectPermissions();

		permissions.setObjectPID(PID);
		permissions.addReadUsers(new String[] { getClientUsername() });
		permissions.addModifyUsers(new String[] { getClientUsername() });
		permissions.addRemoveUsers(new String[] { getClientUsername() });
		permissions.addGrantUsers(new String[] { getClientUsername() });

		return permissions;
	}

	/**
	 * Gets the permissions for public objects.
	 * 
	 * @param PID
	 *            the PID of the object that is the target of the permissions.
	 * 
	 * @return a {@link RODAObjectPermissions}.
	 */
	public RODAObjectPermissions getPublicPermissions(String PID) {

		RODAObjectPermissions permissions = new RODAObjectPermissions();

		permissions.setObjectPID(PID);
		permissions.addReadGroups(this.publicReadGroups);

		return permissions;
	}

	/**
	 * Copy the permissions of object <code>sourcePID</code> to object
	 * <code>sourcePID</code>.
	 * 
	 * @param sourcePID
	 *            the object to copy the permissions from.
	 * @param destPID
	 *            the object to copy the permissions to.
	 * @param recursive
	 *            the permissions should be applied recursively? (Errors in the
	 *            descendants will not throw exceptions).
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void copyPermissions(String sourcePID, String destPID,
			boolean recursive) throws EditorException,
			NoSuchRODAObjectException {

		copyPermissions(sourcePID, destPID, recursive, false);

	}

	/**
	 * Copy the permissions of object <code>sourcePID</code> to object
	 * <code>sourcePID</code>. Permissions will be applied recursively and any
	 * errors setting permissions to descendants will not throw exceptions.
	 * 
	 * @param sourcePID
	 *            the object to copy the permissions from.
	 * @param destPID
	 *            the object to copy the permissions to.
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void copyPermissions(String sourcePID, String destPID)
			throws EditorException, NoSuchRODAObjectException {

		copyPermissions(sourcePID, destPID, true, false);

	}

	/**
	 * Copy the permissions of object <code>sourcePID</code> to object
	 * <code>sourcePID</code> and all it's descendants.
	 * 
	 * @param sourcePID
	 *            the object to copy the permissions from.
	 * @param destPID
	 *            the object to copy the permissions to.
	 * @param recursive
	 *            the permissions should be applied recursively?
	 * @param stopAtFirstError
	 *            the recursion should stop if an error is encountered?
	 * 
	 * @throws NoSuchRODAObjectException
	 * @throws EditorException
	 */
	public void copyPermissions(String sourcePID, String destPID,
			boolean recursive, boolean stopAtFirstError)
			throws NoSuchRODAObjectException, EditorException {

		RODAObjectPermissions permissions;
		try {

			permissions = getBrowserHelper()
					.getRODAObjectPermissions(sourcePID);

		} catch (BrowserException e) {
			logger.debug("ReThrowing - " + e.getMessage());
			throw new EditorException(
					"Exception reading permissions of object " + sourcePID
							+ " - " + e.getMessage(), e);
		}

		// Change the object of the permissions to be destPID
		permissions.setObjectPID(destPID);

		// Set the permissions for destPID
		setPermissions(permissions, recursive, stopAtFirstError);

	}

	/**
	 * Gets the properties of a {@link SimpleDescriptionObject}.
	 * 
	 * @param simpleDO
	 * @param permissions
	 * 
	 */
	protected Map<String, String[]> getSimpleRORelsExtProperties(
			SimpleRepresentationObject simpleRO) {

		Map<String, String[]> properties = new HashMap<String, String[]>();

		// predicate -> object
		properties.put(FedoraRISearch.RDF_TAG_REPRESENTATION_STATUS,
				simpleRO.getStatuses());
		properties.put(FedoraRISearch.RDF_TAG_REPRESENTATION_TYPE,
				new String[] { simpleRO.getType() });
		properties.put(FedoraRISearch.RDF_TAG_REPRESENTATION_SUBTYPE,
				new String[] { simpleRO.getSubType() });

		return properties;
	}

	/**
	 * Sets the properties of a {@link SimpleDescriptionObject}.
	 * 
	 * @param simpleDO
	 * @param permissions
	 * 
	 */
	protected Map<String, String> getSimpleDORelsExtProperties(
			SimpleDescriptionObject simpleDO) {

		Map<String, String> properties = new HashMap<String, String>();
		// predicate -> object
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_LEVEL, simpleDO
				.getLevel().getLevel());
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_COUNTRYCODE,
				simpleDO.getCountryCode());
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_REPOSITORYCODE,
				simpleDO.getRepositoryCode());
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_ID, simpleDO.getId());
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_TITLE,
				simpleDO.getTitle());
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_DATEINITIAL,
				simpleDO.getDateInitial());
		properties.put(FedoraRISearch.RDF_TAG_DESCRIPTION_DATEFINAL,
				simpleDO.getDateFinal());

		return properties;
	}

	/**
	 * Gets the 'child-of' property of a {@link SimpleDescriptionObject}.
	 */
	private Map<String, String> getSimpleDOChildOfProperty(
			SimpleDescriptionObject simpleDO) {

		Map<String, String> properties = new HashMap<String, String>();
		// predicate -> object
		properties.put(
				FedoraRISearch.RDF_TAG_CHILD_OF,
				getFedoraClientUtility().getFedoraObjectURIFromPID(
						simpleDO.getParentPID()));

		return properties;
	}

	protected Map<String, String[]> getPermissionRelsExtProperties(
			RODAObjectPermissions permissions) {

		Map<String, String[]> properties = new HashMap<String, String[]>();
		// predicate -> object
		properties.put(FedoraRISearch.RDF_TAG_PERMISSION_READ_USER,
				permissions.getReadUsers());
		properties.put(FedoraRISearch.RDF_TAG_PERMISSION_READ_GROUP,
				permissions.getReadGroups());

		return properties;
	}

	/**
	 * Sets the properties of a {@link SimpleDescriptionObject}.
	 * 
	 * @param simpleDO
	 * @param permissions
	 * 
	 * @throws FedoraClientException
	 */
	private void setSimpleDOProperties(SimpleDescriptionObject simpleDO,
			RODAObjectPermissions permissions) throws FedoraClientException {

		try {

			InputStream rdfInputStream = getFedoraClientUtility()
					.getRODAObjectRDF(simpleDO.getPid());

			rdfInputStream = getFedoraClientUtility()
					.modifyRDFSingleProperties(rdfInputStream,
							getSimpleDORelsExtProperties(simpleDO));
			logger.info("Added Simple DO properties to RELS-EXT");

			rdfInputStream = getFedoraClientUtility()
					.modifyRDFResourceProperties(rdfInputStream,
							getSimpleDOChildOfProperty(simpleDO));
			logger.info("Added Simple DO 'child-of' property to RELS-EXT");

			ByteArrayOutputStream rdfOutputStream = new ByteArrayOutputStream();
			IOUtils.copy(rdfInputStream, rdfOutputStream);
			logger.info("Added Simple DO properties to RELS-EXT");

			if (permissions != null) {
				// Set permissions
				permissions.setObjectPID(simpleDO.getPid());
				permissions = makeConsistentPermissions(permissions);

				rdfInputStream = getFedoraClientUtility()
						.addRDFMultivalueProperties(rdfInputStream,
								getPermissionRelsExtProperties(permissions));
				logger.info("Added permissions properties to RELS-EXT");
			}

			// If the datastream already exists on object, modify it.
			getFedoraClientUtility().getAPIM().modifyDatastreamByValue(
					simpleDO.getPid(), "RELS-EXT", new String[0],
					"Relationship Metadata", "text/xml", null,
					rdfOutputStream.toByteArray(), null, null,
					"Modified by RODA Core Services", false);

		} catch (Exception e) {
			throw new FedoraClientException(e.getMessage(), e);
		}

	}

	protected RODAObjectPermissions makeConsistentPermissions(
			RODAObjectPermissions permissions) {

		// The minimum users and groups are needed for every object

		permissions.addReadUsers(this.minimumReadUsers);
		permissions.addReadGroups(this.minimumReadGroups);

		permissions.addModifyUsers(this.minimumModifyUsers);
		permissions.addModifyGroups(this.minimumModifyGroups);

		permissions.addRemoveUsers(this.minimumRemoveUsers);
		permissions.addRemoveGroups(this.minimumRemoveGroups);

		permissions.addGrantUsers(this.minimumGrantUsers);
		permissions.addGrantGroups(this.minimumGrantGroups);

		// All users/groups that can grant can also modify.
		permissions.addModifyUsers(permissions.getGrantUsers());
		permissions.addModifyGroups(permissions.getGrantGroups());

		// All users/groups that can modify can also read.
		permissions.addReadUsers(permissions.getModifyUsers());
		permissions.addReadGroups(permissions.getModifyGroups());

		return permissions;
	}

	/**
	 * Sets the permissions specified in the {@link RODAObjectPermissions}
	 * given.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions} to set.
	 * @param recursive
	 *            the permissions should be applied recursively to child DOs?
	 * @param stopAtFirstError
	 *            the recursion should stop if an error is encountered?
	 * 
	 * @throws EditorException
	 */
	private void setPermissions(RODAObjectPermissions permissions,
			boolean recursive, boolean stopAtFirstError) throws EditorException {

		List<String> doDescendantPIDs = new ArrayList<String>();

		if (recursive) {

			try {

				doDescendantPIDs = this.fedoraClientUtility.getFedoraRISearch()
						.getDescendantDescriptionObjectPIDs(
								permissions.getObjectPID());

			} catch (NoSuchRODAObjectException e) {

				logger.warn("Exception finding descendant DOs of "
						+ permissions.getObjectPID() + " - " + e.getMessage()
						+ " - Assuming object " + permissions.getObjectPID()
						+ " doesn't have DO descendants.");

			} catch (FedoraRISearchException e) {

				logger.warn("Exception finding descendant DOs of "
						+ permissions.getObjectPID() + " - " + e.getMessage()
						+ " - Assuming object " + permissions.getObjectPID()
						+ " doesn't have DO descendants.");

			}

		}

		setPermissions(permissions);

		if (recursive && doDescendantPIDs.size() > 0) {

			logger.debug("setPermissions() recursive is " + recursive);
			logger.debug("Object " + permissions.getObjectPID() + " has "
					+ doDescendantPIDs.size() + " descendant DO(s)."
					+ " Copying permissions to them.");

			setPermissions(permissions, doDescendantPIDs, stopAtFirstError);

			logger.debug("Finished setting permissions on descendant DO(s) of object "
							+ permissions.getObjectPID());

		} else {

			logger.debug("setPermissions() recursive is " + recursive);
			logger.debug("Object " + permissions.getObjectPID() + " has "
					+ doDescendantPIDs.size() + " descendant DO(s)."
					+ " Not copying permissions.");
		}

	}

	/**
	 * Sets the permissions of the specified in the
	 * {@link RODAObjectPermissions} given.
	 * 
	 * @param permissions
	 *            the {@link RODAObjectPermissions} to set.
	 * 
	 * @throws EditorException
	 */
	protected void setPermissions(RODAObjectPermissions permissions)
			throws EditorException {

		permissions = makeConsistentPermissions(permissions);

		try {

			// Get the list of datastreams in the object
			List<String> datastreamIDs = this.fedoraClientUtility
					.getFedoraRISearch().getRODAObjectDatastreamIDs(
							permissions.getObjectPID());

			// Verify if the POLICY datastream already exists
			boolean datastreamExists = datastreamIDs
					.contains(this.browserHelper.policyDatastreamID);

			// Create a PolicyDocument from the RODAObjectPermissions and save
			// it to a byte array
			byte[] policyByteArray = new PolicyHelper(permissions)
					.saveToByteArray();

			// Add read permissions to RELS-EXT datastream
			getFedoraClientUtility().setReadPermissionsProperties(
					permissions.getObjectPID(), permissions.getReadUsers(),
					permissions.getReadGroups());

			if (datastreamExists) {

				getFedoraClientUtility().getAPIM().modifyDatastreamByValue(
						permissions.getObjectPID(),
						this.browserHelper.policyDatastreamID, new String[0],
						"XACML Policy", "text/xml", null, policyByteArray,
						null, null, "Modified by RODA Core Services", true);

			} else {

				// Upload the file to the server to a temporary location
				String temporaryURL = getFedoraClientUtility().temporaryUpload(
						policyByteArray);

				getFedoraClientUtility().getAPIM().addDatastream(
						permissions.getObjectPID(),
						this.browserHelper.policyDatastreamID, new String[0],
						"XACML Policy", true, "text/xml", null, temporaryURL,
						"X", "A", null, null, "Added by RODA Core Services");

			}

			List<String> descendantPIDs = this.fedoraClientUtility
					.getFedoraRISearch().getDODescendantPIDs(
							permissions.getObjectPID(), false);

			if (descendantPIDs.size() > 0) {

				logger.debug("Object " + permissions.getObjectPID() + " has "
						+ descendantPIDs.size() + " descendant RO/PO(s)."
						+ " Copying permissions to them.");

				setPermissions(permissions, descendantPIDs, false);

				logger.debug("Finished setting permissions on RO/PO descendants of object "
								+ permissions.getObjectPID());

			} else {
				logger.debug("Object " + permissions.getObjectPID() + " has "
						+ descendantPIDs.size() + " descendant RO/PO(s).");
			}

		} catch (PolicyMetadataException e) {

			logger.debug(
					"Exception creating XACML Policy file - " + e.getMessage(),
					e);
			throw new EditorException(
					"Exception creating XACML Policy file  - " + e.getMessage(),
					e);

		} catch (Exception e) {

			logger.debug(
					"Exception modifying POLICY datastream - " + e.getMessage(),
					e);
			throw new EditorException(
					"Exception modifying POLICY datastream - " + e.getMessage(),
					e);
		}

	}

	private void setPermissions(RODAObjectPermissions permissions,
			List<String> PIDs, boolean stopAtFirstError) throws EditorException {

		for (String pid : PIDs) {

			permissions.setObjectPID(pid);

			try {

				setPermissions(permissions);

			} catch (EditorException e) {

				if (stopAtFirstError) {
					logger.debug("Exception setting permissions on object "
							+ pid + " - " + e.getMessage(), e);
					throw new EditorException(
							"Exception setting permissions on object " + pid
									+ " - " + e.getMessage(), e);
				} else {
					logger.debug(
							"IGNORED Exception setting permissions on object "
									+ pid + " - " + e.getMessage(), e);
				}
			}
		}
	}

	private void setDefaultPermissions(String PID) throws EditorException {
		setPermissions(getDefaultPermissions(PID));
	}

	/**
	 * @return the fedoraClientUtility
	 */
	protected FedoraClientUtility getFedoraClientUtility() {
		return fedoraClientUtility;
	}

	private String getClientUsername() {
		return this.fedoraClientUtility.getCASUserPrincipal().getName();
	}

	/**
	 * This thread will get a list of all the descendants of 'pid' and update
	 * the GSearch indexes for them. Old and new parents are not updated by this
	 * thread, because they are automatically updated by Fedora.
	 */
	class GSearchUpdater extends Thread {

		private FedoraClientUtility fedoraClientUtility = null;

		private String doPID = null;

		/**
		 * @param fedoraClientUtility
		 * @param doPID
		 * @throws FedoraRISearchException
		 */
		public GSearchUpdater(FedoraClientUtility fedoraClientUtility,
				String doPID) {

			super(GSearchUpdater.class.getSimpleName() + " thread for DO "
					+ doPID);

			this.fedoraClientUtility = fedoraClientUtility;
			this.doPID = doPID;
		}

		/**
		 * Starts the thread.
		 */
		@Override
		public void run() {

			logger.debug(getName() + " started, to update renamed/moved DOs...");

			try {

				Set<String> pidsToUpdate = new HashSet<String>(
						this.fedoraClientUtility.getFedoraRISearch()
								.getDescendantDescriptionObjectPIDs(doPID));

				logger.trace("desdendants of " + doPID + " are " + pidsToUpdate);

				logger.debug("PIDs to update: " + pidsToUpdate);

				this.fedoraClientUtility.getFedoraGSearch()
						.updateDescriptionObjectsIndex(
								new ArrayList<String>(pidsToUpdate));

			} catch (NoSuchRODAObjectException e) {
				logger.error(
						"Exception updating moved objects - " + e.getMessage()
								+ ". Ignoring", e);
			} catch (FedoraRISearchException e) {
				logger.error(
						"Exception updating moved objects - " + e.getMessage()
								+ ". Ignoring", e);
			}

			logger.debug(getName() + " finished");
		}
	}

}
