package pt.gov.dgarq.roda.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FITSUtility;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.EditorException;
import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RepresentationAlreadyPreservedException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.PreservationObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SearchParameter;
import pt.gov.dgarq.roda.core.data.SearchResult;
import pt.gov.dgarq.roda.core.data.SearchResultObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ClassificationSchemeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.preservation.Fixity;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.data.search.DefaultSearchParameter;
import pt.gov.dgarq.roda.core.data.search.EadcSearchFields;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.fedora.gsearch.FedoraGSearch;
import pt.gov.dgarq.roda.core.fedora.gsearch.FedoraGSearchException;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearchException;
import pt.gov.dgarq.roda.core.metadata.premis.PremisAgentHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisEventHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisFileObjectHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisMetadataException;
import pt.gov.dgarq.roda.core.metadata.premis.PremisRepresentationObjectHelper;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyHelper;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.util.FileUtility;

/**
 * @author Rui Castro
 */
public class IngestHelper extends EditorHelper {

  static final private Logger logger = Logger.getLogger(IngestHelper.class);

  /**
   * Constructs a new {@link IngestHelper}.
   * 
   * @param fedoraClientUtility
   * @param configuration
   * 
   * @throws EditorException
   */
  public IngestHelper(FedoraClientUtility fedoraClientUtility, Configuration configuration) throws EditorException {
    super(fedoraClientUtility, configuration);
  }

  /**
   * Creates a {@link DescriptionObject} without a parent. The specified parent
   * is used to access producers information.
   * 
   * @param dObject
   *          the {@link DescriptionObject} to create.
   * 
   * @return a {@link String} with the PID of the newly created object.
   * 
   * @throws NoSuchRODAObjectException
   *           if the specified <code>doParentPID</code> doesn't exist.
   * @throws InvalidDescriptionObjectException
   *           if the specified <code>dObject</code> is invalid.
   * @throws IngestException
   */
  public String createDetachedDescriptionObject(DescriptionObject dObject) throws NoSuchRODAObjectException,
    InvalidDescriptionObjectException, IngestException {

    String doParentPID = dObject.getParentPID();
    dObject.setParentPID(null);

    String doPID = null;
    try {

      Producers producers = getBrowserHelper().getProducers(doParentPID);

      RODAObjectPermissions permissions = getDefaultPermissions(null);

      permissions.addReadUsers(producers.getUsers());
      permissions.addReadGroups(producers.getGroups());

      dObject.setState(DescriptionObject.STATE_INACTIVE);

      doPID = createSingleDescriptionObject(dObject, permissions);

      if (descriptionObjectReplaceID) {
        // Set the ID automatically from the PID
        String[] namespaceID = doPID.split(":"); //$NON-NLS-1$
        dObject.setId(namespaceID[1]);
      }

      modifyDescriptionObject(dObject);

      return doPID;

    } catch (Exception e) {

      logger.error("Error creating detached DO - " + e.getMessage(), e);

      if (doPID != null) {

        logger.info("Removing DO " + doPID + " already created");

        try {

          removeDescriptionObject(doPID);

        } catch (EditorException e1) {
          logger.error("Error removing created DO " + doPID + " - " + e1.getMessage() + ". IGNORING");
        }
      }

      throw new IngestException("Error creating detached DO - " + e.getMessage(), e);
    }

  }

  /**
   * Creates a {@link DescriptionObject} with the specified parent.
   * 
   * @param dObject
   *          the {@link DescriptionObject} to create.
   * 
   * @return a {@link String} with the PID of the newly created object.
   * 
   * @throws NoSuchRODAObjectException
   *           if the specified <code>doParentPID</code> doesn't exist.
   * @throws InvalidDescriptionObjectException
   *           if the specified <code>dObject</code> is invalid.
   * @throws IngestException
   */
  public String createDescriptionObject(DescriptionObject dObject) throws NoSuchRODAObjectException,
    InvalidDescriptionObjectException, IngestException {

    String doPID = null;
    try {

      dObject.setState(DescriptionObject.STATE_INACTIVE);

      doPID = super.createDescriptionObject(dObject);

      if (descriptionObjectReplaceID) {
        // Set the ID automatically from the PID
        String[] namespaceID = doPID.split(":"); //$NON-NLS-1$
        dObject.setId(namespaceID[1]);
      }

      modifyDescriptionObject(dObject);

      return doPID;

    } catch (Exception e) {
      logger.error("Error creating detached DO - " + e.getMessage(), e);

      if (doPID != null) {

        logger.info("Removing DO " + doPID + " already created");

        try {

          removeDescriptionObject(doPID);

        } catch (EditorException e1) {
          logger.error("Error removing created DO " + doPID + " - " + e1.getMessage() + ". IGNORING");
        }
      }

      throw new IngestException("Error creating detached DO - " + e.getMessage(), e);
    }

  }

  /**
   * Creates a {@link DescriptionObject} without a parent. The specified parent
   * is used to access producers information.
   * 
   * @param dObject
   *          the {@link DescriptionObject} to create.
   * 
   * @return a {@link String} with the PID of the newly created object.
   * 
   * @throws NoSuchRODAObjectException
   *           if the specified <code>doParentPID</code> doesn't exist.
   * @throws InvalidDescriptionObjectException
   *           if the specified <code>dObject</code> is invalid.
   * @throws IngestException
   */
  // FIXME revise javadocs
  public String createDetachedDescriptionObject(DescriptionObject dObject, String originalMetadataFilePath)
    throws NoSuchRODAObjectException, InvalidDescriptionObjectException, IngestException {

    String doParentPID = dObject.getParentPID();
    dObject.setParentPID(null);

    String doPID = null;
    try {

      Producers producers = getBrowserHelper().getProducers(doParentPID);

      RODAObjectPermissions permissions = getDefaultPermissions(null);

      permissions.addReadUsers(producers.getUsers());
      permissions.addReadGroups(producers.getGroups());

      dObject.setState(DescriptionObject.STATE_INACTIVE);

      doPID = createSingleDescriptionObject(dObject, permissions, originalMetadataFilePath);

      if (descriptionObjectReplaceID) {
        // Set the ID automatically from the PID
        String[] namespaceID = doPID.split(":"); //$NON-NLS-1$
        dObject.setId(namespaceID[1]);
      }

      modifyDescriptionObject(dObject);

      return doPID;

    } catch (Exception e) {

      logger.error("Error creating detached DO - " + e.getMessage(), e);

      if (doPID != null) {

        logger.info("Removing DO " + doPID + " already created");

        try {

          removeDescriptionObject(doPID);

        } catch (EditorException e1) {
          logger.error("Error removing created DO " + doPID + " - " + e1.getMessage() + ". IGNORING");
        }
      }

      throw new IngestException("Error creating detached DO - " + e.getMessage(), e);
    }

  }

  /**
   * Creates a {@link DescriptionObject} with the specified parent.
   * 
   * @param dObject
   *          the {@link DescriptionObject} to create.
   * 
   * @return a {@link String} with the PID of the newly created object.
   * 
   * @throws NoSuchRODAObjectException
   *           if the specified <code>doParentPID</code> doesn't exist.
   * @throws InvalidDescriptionObjectException
   *           if the specified <code>dObject</code> is invalid.
   * @throws IngestException
   */
  // FIXME revise javadocs
  public String createDescriptionObject(DescriptionObject dObject, String originalMetadataFilePath)
    throws NoSuchRODAObjectException, InvalidDescriptionObjectException, IngestException {

    String doPID = null;
    try {

      dObject.setState(DescriptionObject.STATE_INACTIVE);

      doPID = super.createDescriptionObject(dObject, originalMetadataFilePath);

      if (descriptionObjectReplaceID) {
        // Set the ID automatically from the PID
        String[] namespaceID = doPID.split(":"); //$NON-NLS-1$
        dObject.setId(namespaceID[1]);
      }

      modifyDescriptionObject(dObject);

      return doPID;

    } catch (Exception e) {
      logger.error("Error creating detached DO - " + e.getMessage(), e);

      if (doPID != null) {

        logger.info("Removing DO " + doPID + " already created");

        try {

          removeDescriptionObject(doPID);

        } catch (EditorException e1) {
          logger.error("Error removing created DO " + doPID + " - " + e1.getMessage() + ". IGNORING");
        }
      }

      throw new IngestException("Error creating detached DO - " + e.getMessage(), e);
    }

  }

  /**
   * Removes the {@link DescriptionObject} with PID <code>pid</code> and all
   * it's descendants as long as the {@link DescriptionObject} is inactive.
   * 
   * @param doPID
   *          the PID of the {@link DescriptionObject} to remove.
   * 
   * @throws NoSuchRODAObjectException
   * @throws IngestException
   */
  public void removeDescriptionObject(String doPID) throws NoSuchRODAObjectException, IngestException {

    SimpleDescriptionObject sdo = null;
    try {

      sdo = getBrowserHelper().getSimpleDescriptionObject(doPID);

    } catch (BrowserException e) {
      logger.debug("Exception getting DO " + doPID + " - " + e.getMessage(), e);
      throw new IngestException("Exception getting DO " + doPID + " - " + e.getMessage(), e);
    }

    if (sdo.getState().equals(RODAObject.STATE_INACTIVE)) {
      // DO is not inactive, continue.
    } else {
      logger.warn("DO " + doPID + " cannot be removed because it is active.");
      throw new IngestException("DO " + doPID + " cannot be removed because it is active.");
    }

    List<String> descendantPIDs;
    try {

      descendantPIDs = getBrowserHelper().getDODescendantPIDs(doPID);

    } catch (BrowserException e) {
      logger.debug("Exception getting descendants of DO " + doPID + " - " + e.getMessage(), e);
      throw new IngestException("Exception getting descendants of DO " + doPID + " - " + e.getMessage(), e);
    }

    // Remove the object 'doPID'
    try {

      logger.info("Removing DO " + doPID);
      getFedoraClientUtility().purgeObject(doPID);
      logger.info("DO " + doPID + " removed.");

    } catch (FedoraClientException e) {
      logger.debug("Error removing DO " + doPID + " - " + e.getMessage(), e);
      throw new IngestException("Error removing DO " + doPID + " - " + e.getMessage(), e);
    }

    // When the top DO is removed, the descendants become orphan and
    // invisible in the fonds trees.
    // So, the DO can be considered removed at this point.
    // No exception should be thrown from now on.
    if (descendantPIDs.size() > 0) {
      logger.info("DO " + doPID + " has " + descendantPIDs.size() + " descendants. Removing all descendants");

      logger.debug("DO descendants: " + descendantPIDs);

      removeObjects(descendantPIDs);
    }

  }

  /**
   * @param roObject
   *          the {@link RepresentationObject} to create.
   * 
   * @return a {@link String} with the PID of the new
   *         {@link RepresentationObject}.
   * 
   * @throws NoSuchRODAObjectException
   * @throws EditorException
   */
  public String createRepresentationObject(RepresentationObject roObject) throws NoSuchRODAObjectException,
    EditorException {

    if (roObject == null) {
      throw new EditorException("representation object argument cannot be null");
    }

    // Throws NoSuchRODAObjectException it the descriptionObject doesn't
    // exist
    SimpleDescriptionObject simpleDO;
    try {
      simpleDO = getBrowserHelper().getSimpleDescriptionObject(roObject.getDescriptionObjectPID());

    } catch (BrowserException e) {
      throw new EditorException(e.getMessage(), e);
    }

    String pid = null;
    try {

      // Create the representation object
      pid = getFedoraClientUtility().createObject(roObject);

      logger.info("Successfully created RO " + pid);

      /*
       * RELS-EXT datastream
       */
      // Create an empty RDF stream for DO
      InputStream rdfInputStream = getFedoraClientUtility().getEmptyRDF(pid);

      // Add SimpleDO properties
      rdfInputStream = getFedoraClientUtility().addRDFMultivalueProperties(rdfInputStream,
        getSimpleRORelsExtProperties(roObject));
      logger.info("Added Simple RO properties to RELS-EXT");

      // Set permissions
      RODAObjectPermissions permissions;
      try {

        permissions = getBrowserHelper().getRODAObjectPermissions(roObject.getDescriptionObjectPID());

      } catch (Exception e) {
        throw new EditorException("Exception getting permissions of RO " + roObject.getDescriptionObjectPID() + " - "
          + e.getMessage(), e);
      }

      permissions.setObjectPID(pid);
      permissions = makeConsistentPermissions(permissions);

      rdfInputStream = getFedoraClientUtility().addRDFMultivalueProperties(rdfInputStream,
        getPermissionRelsExtProperties(permissions));
      logger.info("Added permissions properties to RELS-EXT");

      ByteArrayOutputStream rdfOutputStream = new ByteArrayOutputStream();
      IOUtils.copy(rdfInputStream, rdfOutputStream);

      String temporaryURL = getFedoraClientUtility().temporaryUpload(rdfOutputStream.toByteArray());

      logger.info("RELS-EXT datastream uploaded to " + temporaryURL);

      // Add it to object pid
      getFedoraClientUtility().getAPIM().addDatastream(pid, this.relsExtDatastreamID, new String[0],
        "Relationship Metadata", true, "text/xml", null, temporaryURL, "X", "A", null, null,
        "Added by RODA Core Services");

      logger.info("RELS-EXT datastream added to RO " + pid);

      /*
       * POLICY datastream
       */
      // Create a PolicyDocument from the RODAObjectPermissions and save
      // it to a byte array
      byte[] policyByteArray = new PolicyHelper(permissions).saveToByteArray();

      // Upload the file to the server to a temporary location
      temporaryURL = getFedoraClientUtility().temporaryUpload(policyByteArray);

      logger.info("POLICY datastream uploaded to " + temporaryURL);

      getFedoraClientUtility().getAPIM().addDatastream(permissions.getObjectPID(),
        getBrowserHelper().policyDatastreamID, new String[0], "XACML Policy", true, "text/xml", null, temporaryURL,
        "X", "A", null, null, "Added by RODA Core Services");

      logger.info("POLICY datastream added to DO " + pid);

      /*
       * TODO: this needs to be changed to be done together with the above
       * operations
       * 
       * RELS-EXT datastream - "represented-by" relationship
       */
      // Creates a "represented-by" relationship between DescriptionObject
      // and the new RepresentationObject.
      getFedoraClientUtility().addRepresentedByRelationship(roObject.getDescriptionObjectPID(), pid);

      return pid;

    } catch (Exception e) {

      logger.debug("Exception creating representation object - " + e.getMessage(), e);

      if (pid != null) {

        logger.debug("Deleting created object " + pid);

        removeObjects(new String[] {pid});
      }

      throw new EditorException("Exception creating representation object - " + e.getMessage(), e);
    }

  }

  /**
   * Creates a {@link RepresentationPreservationObject}.
   * 
   * @param rpo
   *          the {@link RepresentationPreservationObject} to create.
   * @param doPID
   *          the PID of the {@link DescriptionObject}
   * 
   * @return a {@link String} with the PID of the created
   *         {@link RepresentationPreservationObject}.
   * 
   * @throws NoSuchRODAObjectException
   * @throws RepresentationAlreadyPreservedException
   * @throws EditorException
   */
  public String createRepresentationPreservationObject(RepresentationPreservationObject rpo, String doPID)
    throws RepresentationAlreadyPreservedException, EditorException, NoSuchRODAObjectException {
    return insertRepresentationPreservationObject(rpo, doPID);
  }

  /**
   * Sets the specified {@link RepresentationObject} as the the normalized
   * representation of the specified {@link DescriptionObject}.
   * 
   * @param doPID
   *          the PID of the {@link DescriptionObject}.
   * @param roPID
   *          the PID of the {@link RepresentationObject}.
   * 
   * @return a {@link String} with the PID of the {@link RepresentationObject}
   *         that is the current normalized representation.
   * 
   * @throws NoSuchRODAObjectException
   *           if one of the given PIDs (doPID or roPID) doesn't exist.
   * @throws EditorException
   */
  public String setDONormalizedRepresentation(String doPID, String roPID) throws NoSuchRODAObjectException,
    EditorException {

    // Perform sanity check!!!
    // DO exists?
    try {

      SimpleDescriptionObject simpleDO = getBrowserHelper().getSimpleDescriptionObject(doPID);

      logger.debug("setDONormalizedRepresentation(" + doPID + "," + roPID + ") DO is " + simpleDO);

    } catch (BrowserException e) {
      logger.debug("Error getting DescriptionObject " + doPID + " - " + e.getMessage(), e);
      throw new EditorException("Error getting DescriptionObject " + doPID + " - " + e.getMessage(), e);
    }

    // RO exists?
    SimpleRepresentationObject simpleRO;
    try {

      simpleRO = getBrowserHelper().getSimpleRepresentationObject(roPID);

      logger.debug("setDONormalizedRepresentation(" + doPID + "," + roPID + ") RO is " + simpleRO);

      // RO is a representation of DO?
      if (!simpleRO.getDescriptionObjectPID().equals(doPID)) {
        throw new EditorException(roPID + " is not a representation of " + doPID);
      }

    } catch (BrowserException e) {
      logger.debug("Error getting DescriptionObject " + doPID + " - " + e.getMessage(), e);
      throw new EditorException("Error getting DescriptionObject " + doPID + " - " + e.getMessage(), e);
    }

    try {

    	List<FilterParameter> parameters = new ArrayList<FilterParameter>();
    	parameters.add(new SimpleFilterParameter("descriptionObjectPID", doPID));
    	parameters.add(new SimpleFilterParameter("statuses", RepresentationObject.STATUS_NORMALIZED));
      Filter normROfilter = new Filter(parameters);
      List<SimpleRepresentationObject> normalizedSROs = getBrowserHelper().getSimpleRepresentationObjects(
        new ContentAdapter(normROfilter, null, null));

      if (normalizedSROs.size() > 0) {

        if (normalizedSROs.size() > 1) {
          logger.warn("DO " + doPID + " has " + normalizedSROs.size() + " normalized representations!!!");
        }

        for (SimpleRepresentationObject normalizedSRO : normalizedSROs) {

          if (normalizedSRO.equals(roPID)) {
            // roPID is already normalized. Nothing to do.
            logger.info("Representation " + roPID + " already has normalized status");
          } else {
            // This object is not the normalized representation.
            logger
              .info("Representation " + roPID + " is not the normalized representation. Removing status normalized");

            normalizedSRO.removeStatus(RepresentationObject.STATUS_NORMALIZED);

            try {

              getFedoraClientUtility().setRepresentationProperties(normalizedSRO.getPid(), normalizedSRO.getStatuses(),
                normalizedSRO.getType(), normalizedSRO.getSubType());

            } catch (FedoraClientException e) {
              logger.error(
                "Error removing normalized status from RO " + normalizedSRO.getPid() + " - " + e.getMessage(), e);
            }

          }
        }

      } else {
        // No normalized representation. Let's mark roPID as normalized
        logger.info("Marking " + roPID + " as the normalized representation of DO " + doPID);

        simpleRO.addStatus(RepresentationObject.STATUS_NORMALIZED);

        try {

          getFedoraClientUtility().setRepresentationProperties(simpleRO.getPid(), simpleRO.getStatuses(),
            simpleRO.getType(), simpleRO.getSubType());

        } catch (FedoraClientException e) {
          logger.debug("Error setting normalized status on RO " + simpleRO.getPid() + " - " + e.getMessage(), e);
          throw new EditorException("Error setting normalized status on RO " + simpleRO.getPid() + " - "
            + e.getMessage(), e);
        }
      }

      logger.info("Normalized representation of DO " + doPID + " is RO " + roPID);

      return roPID;

    } catch (BrowserException e) {
      logger.debug("Error getting DescriptionObject's representations " + " - " + e.getMessage(), e);
      throw new EditorException("Error getting DescriptionObject's representations " + " - " + e.getMessage(), e);
    }

  }

  public void createDerivationRelationship(String rpoPID, String derivationEventPID) throws IngestException {

    // Create relationship with derivation event
    try {

      getFedoraClientUtility().addDerivedFromRelationship(rpoPID, derivationEventPID);

    } catch (FedoraClientException e) {
      logger.debug("Error creating derivation relationship between " //$NON-NLS-1$
        + rpoPID + " and " + derivationEventPID + " - " //$NON-NLS-1$//$NON-NLS-2$
        + e.getMessage());
      throw new IngestException("Error creating derivation relationship between " + rpoPID //$NON-NLS-1$
        + " and " + derivationEventPID + " - " //$NON-NLS-1$//$NON-NLS-2$
        + e.getMessage(), e);
    }

  }

  /**
   * Create preservation metadata relative to the ingestion of the given
   * objects. Creates {@link RepresentationPreservationObject}s for
   * {@link RepresentationObject}s and register the ingest
   * {@link EventPreservationObject}.
   * 
   * @param doPIDs
   *          the PIDs of the {@link DescriptionObject}s.
   * @param roPIDs
   *          the PIDs of the {@link RepresentationObject}s.
   * @param poPIDs
   *          the PIDs of the {@link PreservationObject}s.
   * @param agentName
   *          the name of the agent responsible for the ingestion.
   * @param details
   *          the details of the ingest operation.
   * 
   * 
   * @return the PID of the ingest {@link EventPreservationObject}.
   * 
   * @throws RepresentationAlreadyPreservedException
   * @throws EditorException
   */
  public String registerIngestEvent(String[] doPIDs, String[] roPIDs, String[] poPIDs, String agentName, String details)
    throws RepresentationAlreadyPreservedException, EditorException {

    List<String> createdObjectPIDs = new ArrayList<String>();

    String parentDescriptionObjectPID = doPIDs[0];

    AgentPreservationObject agent = null;
    try {

      // Get or create Agent information
      agent = getBrowserHelper().getAgentPreservationObjectWithName(agentName);

      if (agent == null) {
        // Create a new Agent
        agent = new AgentPreservationObject();
        agent.setAgentName(agentName);
        agent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_INGEST_TASK);

        String agentPID = insertAgentPreservationObject(agent);
        createdObjectPIDs.add(agentPID);

        // Set public permissions for agents
        // setDefaultPermissions(agentPID);
        setPermissions(getPublicPermissions(agentPID));

        // This is already done inside the
        // insertAgentPreservationObject(Agent) method
        // agent.setID(agentPID);
      }

      logger.trace("Ingest Event Agent is " + agent);

    } catch (BrowserException e) {
      logger.debug("Exception getting preservation agent -" + e.getMessage(), e);
      throw new EditorException("Exception getting preservation agent -" + e.getMessage(), e);
    }

    try {
      // TODO deal with the poPIDs ingested

      // Generate RepresentationPreservationObjects for representations
      List<String> rpoPIDs = new ArrayList<String>();
      if (roPIDs != null) {

        for (String roPID : roPIDs) {

          RepresentationPreservationObject rpo = getBrowserHelper().getROPreservationObject(roPID);

          if (rpo == null) {

            String rpoPID = createAndInsertRepresentationPreservationObject(roPID, null, null);

            logger.trace("Created preservation object " + rpoPID + " for representation " + roPID);

            rpoPIDs.add(rpoPID);
            createdObjectPIDs.add(rpoPID);

          } else {

            logger.info("Representation " + roPID + " already has a preservation object (" + rpo.getPid() + ")");

            rpoPIDs.add(rpo.getPid());
            createdObjectPIDs.add(rpo.getPid());
          }
        }

      } else {
        logger.warn("No representations ingested? Inform developers.");
      }

      EventPreservationObject ingestEvent = createIngestEvent(doPIDs, roPIDs, poPIDs, details, agent.getID(),
        createdObjectPIDs);

      logger.trace("Ingestion Event is " + ingestEvent);

      String ingestEventPID = insertEventPreservationObject(ingestEvent, rpoPIDs.toArray(new String[rpoPIDs.size()]),
        agent.getPid());

      // Copy permissions from the parent description object
      copyPermissions(parentDescriptionObjectPID, ingestEventPID);

      return ingestEventPID;

    } catch (NoSuchRODAObjectException e) {

      logger.debug("Error registering ingestion - " + e.getMessage() + ". Removing created objects.");

      removeObjects(createdObjectPIDs.toArray(new String[createdObjectPIDs.size()]));

      logger.debug("Error registering ingestion - " + e.getMessage(), e);
      throw new EditorException("Error registering ingestion - " + e.getMessage(), e);

    } catch (FedoraRISearchException e) {

      logger.debug("Error registering ingestion - " + e.getMessage() + ". Removing created objects.");

      removeObjects(createdObjectPIDs.toArray(new String[createdObjectPIDs.size()]));

      throw new EditorException("Error registering ingestion - " + e.getMessage(), e);

    } catch (BrowserException e) {

      logger.debug("Error registering ingestion - " + e.getMessage() + ". Removing created objects.");

      removeObjects(createdObjectPIDs.toArray(new String[createdObjectPIDs.size()]));

      throw new EditorException("Error registering ingestion - " + e.getMessage(), e);
    }

  }

  /**
   * Create preservation metadata relative to a single event in a
   * {@link RepresentationObject}.
   * 
   * @param rpoPID
   *          the PIDs of the {@link RepresentationPreservationObject}.
   * @param eventPO
   *          the {@link EventPreservationObject}.
   * @param agent
   *          the {@link AgentPreservationObject}.
   * 
   * 
   * @return the PID of the {@link EventPreservationObject}.
   * 
   * @throws EditorException
   */
  public String registerEvent(String rpoPID, EventPreservationObject eventPO, AgentPreservationObject agent)
    throws EditorException {

    List<String> createdObjectPIDs = new ArrayList<String>();

    AgentPreservationObject existingAgent = null;
    try {

      // Get or create Agent information
      existingAgent = getBrowserHelper().getAgentPreservationObjectWithName(agent.getAgentName());

      if (existingAgent == null) {
        // Create a new Agent
        existingAgent = new AgentPreservationObject();
        existingAgent.setAgentName(agent.getAgentName());
        existingAgent.setAgentType(agent.getAgentType());

        String agentPID = insertAgentPreservationObject(existingAgent);

        createdObjectPIDs.add(agentPID);

        // Set public permissions for agents
        // setDefaultPermissions(agentPID);
        setPermissions(getPublicPermissions(agentPID));

        // This is already done inside the
        // insertAgentPreservationObject(Agent) method
        // agent.setID(agentPID);
      }

      logger.trace("Event Agent is " + agent);

    } catch (BrowserException e) {
      logger.debug("Exception getting preservation agent -" + e.getMessage(), e);
      throw new EditorException("Exception getting preservation agent -" + e.getMessage(), e);
    }

    try {

      eventPO.setAgentID(existingAgent.getID());
      // eventPO.setAgentRole("");
      if (eventPO.getDatetime() == null) {
        eventPO.setDatetime(new Date());
      }
      eventPO.setObjectIDs(new String[] {rpoPID});

      logger.trace("Event is " + eventPO);

      String eventPID = insertEventPreservationObject(eventPO, new String[] {rpoPID}, existingAgent.getPid());

      // Copy permissions from the parent description object
      copyPermissions(rpoPID, eventPID);

      return eventPID;

    } catch (NoSuchRODAObjectException e) {

      logger.debug("Error registering event - " + e.getMessage() + ". Removing created objects.");

      removeObjects(createdObjectPIDs.toArray(new String[createdObjectPIDs.size()]));

      logger.debug("Error registering event - " + e.getMessage(), e);
      throw new EditorException("Error registering event - " + e.getMessage(), e);

    }

  }

  /**
   * Create preservation metadata relative to a derivation event in a
   * {@link RepresentationObject}.
   * 
   * @param originalRepresentationPID
   *          the PID of the original {@link RepresentationObject}.
   * @param derivedRepresentationPID
   *          the PID of the derived {@link RepresentationObject}.
   * @param eventPO
   *          the {@link EventPreservationObject}.
   * @param agentPO
   *          the {@link AgentPreservationObject}.
   * @param markObjectsActive
   *          should the created objects be marked active?
   * 
   * 
   * @return the PID of the {@link EventPreservationObject}.
   * 
   * @throws NoSuchRODAObjectException
   * @throws EditorException
   * 
   */
  public String registerDerivationEvent(String originalRepresentationPID, String derivedRepresentationPID,
    EventPreservationObject eventPO, AgentPreservationObject agentPO, boolean markObjectsActive)
    throws NoSuchRODAObjectException, EditorException {

    SimpleRepresentationObject originalRO = null;
    RepresentationPreservationObject originalRPO = null;
    SimpleRepresentationObject derivedRO = null;
    AgentPreservationObject existingAgentPO = null;

    try {

      logger.info("registerMigrationEvent() performing sanity check, before registering event.");

      // Get original Representation
      originalRO = getBrowserHelper().getSimpleRepresentationObject(originalRepresentationPID);
      logger.info("Original RO is " + originalRO);

      originalRPO = getBrowserHelper().getROPreservationObject(originalRepresentationPID);
      logger.info("Original RPO is " + originalRPO);

      // Get derived Representation
      derivedRO = getBrowserHelper().getSimpleRepresentationObject(derivedRepresentationPID);
      logger.info("Derived RO is " + derivedRO);

      if (agentPO == null || eventPO == null) {
        throw new EditorException("Agent and Event cannot be null.");
      }

      // Get Agent information if already exists
      existingAgentPO = getBrowserHelper().getAgentPreservationObjectWithName(agentPO.getAgentName());
      if (existingAgentPO != null) {
        logger.info("Agent PO " + agentPO.getAgentName() + " already exist " + existingAgentPO);
      } else {
        logger.info("Agent PO " + agentPO.getAgentName() + " doesn't exist.");
      }

      // Check eventPO outcome fields
      if (StringUtils.isBlank(eventPO.getEventType()) || StringUtils.isBlank(eventPO.getOutcome())
        || StringUtils.isBlank(eventPO.getOutcomeDetailNote())
        || StringUtils.isBlank(eventPO.getOutcomeDetailExtension())) {

        logger.warn("Event PO is incomplete " + eventPO);

        logger.warn("Event PO type: " + eventPO.getEventType());
        logger.warn("Event PO outcome: " + eventPO.getOutcome());
        logger.warn("Event PO outcomeDetailNote: " + eventPO.getOutcomeDetailNote());
        logger.warn("Event PO outcomeDetailNoteExtension: " + eventPO.getOutcomeDetailExtension());

        throw new EditorException("Event outcome attributes must be filled.");
      }

    } catch (BrowserException e) {
      logger.debug("Error checking existing objects - " + e.getMessage(), e);
      throw new EditorException("Error checking existing objects - " + e.getMessage(), e);
    }

    List<String> createdObjectPIDs = new ArrayList<String>();

    // Add the derived representation object to the list of created objects
    createdObjectPIDs.add(derivedRepresentationPID);

    String agentPID = null;
    if (existingAgentPO == null) {
      // Create a new Agent

      // agentPO
      // .setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_MIGRATOR);
      agentPO.setState(RODAObject.STATE_ACTIVE);
      agentPID = insertAgentPreservationObject(agentPO);
      createdObjectPIDs.add(agentPID);

      logger.info("Agent PO " + agentPO.getAgentName() + " created with PID " + agentPID);
    } else {
      agentPID = existingAgentPO.getPid();
    }

    eventPO.setDatetime(new Date());
    // eventPO.setEventDetail(agentPO.getAgentName()
    // + " created representation " + derivedRepresentationPID
    // + " from representation " + originalRepresentationPID);
    eventPO.setEventDetail("The representation " + derivedRepresentationPID + " derived from representation "
      + originalRepresentationPID);

    // This values should come already filled.
    // eventPO
    // .setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_MIGRATION);
    // eventPO.setOutcome("");
    // eventPO.setOutcomeDetailNote("Agent output");
    // eventPO.setOutcomeDetailExtension("");
    // Agent ID is the PID
    eventPO.setAgentID(agentPID);
    eventPO.setAgentRole(EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK);
    eventPO.setObjectIDs(new String[] {originalRPO.getID()});

    eventPO.setState(RODAObject.STATE_INACTIVE);
    String eventPID = insertEventPreservationObject(eventPO, new String[] {originalRPO.getPid()}, agentPID);
    createdObjectPIDs.add(eventPID);

    // Copy permissions from the original representation object
    copyPermissions(originalRepresentationPID, eventPID);

    logger.info("Created event PO is " + eventPO);

    // Create representation preservation object
    try {

      String derivedRPOPID = createAndInsertRepresentationPreservationObject(derivedRepresentationPID, eventPID,
        originalRPO.getID());
      createdObjectPIDs.add(derivedRPOPID);

      // Create relationship with derivation event
      getFedoraClientUtility().addDerivedFromRelationship(derivedRPOPID, eventPID);

      logger.info("Created preservation object " + derivedRPOPID + " for representation " + derivedRepresentationPID);

    } catch (Exception e) {

      logger.debug("Error creating representation preservation object - " + e.getMessage()
        + ". Deleting created objects " + createdObjectPIDs, e);

      removeObjects(createdObjectPIDs);

      throw new EditorException("Error creating representation preservation object - " + e.getMessage(), e);
    }

    if (markObjectsActive) {
      try {

        getFedoraClientUtility().markObjectsActive(createdObjectPIDs, true);

        logger.info("Marked objects " + createdObjectPIDs + " as Active");

      } catch (FedoraClientException e) {

        logger.debug("Error marking objects as Active - " + e.getMessage() + ". Deleting created objects "
          + createdObjectPIDs, e);

        removeObjects(createdObjectPIDs);

        throw new EditorException("Error marking objects as Active - " + e.getMessage(), e);
      }
    }

    return eventPID;
  }

  private RepresentationPreservationObject createRepresentationPreservationObject(String roPID)
    throws NoSuchRODAObjectException, BrowserException, EditorException {

    RepresentationObject rObject = getBrowserHelper().getRepresentationObject(roPID);

    RepresentationPreservationObject pObject = new RepresentationPreservationObject();

    // The ID is the PID of the RepresentationObject
    pObject.setID(roPID);

    // preservation level
    if (RepresentationObject.UNKNOWN.equals(rObject.getType())) {
      pObject.setPreservationLevel(RepresentationPreservationObject.PRESERVATION_LEVEL_BITLEVEL);
    } else {
      pObject.setPreservationLevel(RepresentationPreservationObject.PRESERVATION_LEVEL_FULL);
    }

    RepresentationFilePreservationObject pObjectRootFile = createRepresentationFilePreservationObject(rObject.getPid(),
      rObject.getRootFile());

    pObject.setRootFile(pObjectRootFile);

    List<RepresentationFilePreservationObject> pObjectPartFiles = new ArrayList<RepresentationFilePreservationObject>();
    if (rObject.getPartFiles() != null) {

      for (RepresentationFile partFile : rObject.getPartFiles()) {
        RepresentationFilePreservationObject pObjectPartFile = createRepresentationFilePreservationObject(
          rObject.getPid(), partFile);
        pObjectPartFiles.add(pObjectPartFile);
      }
    }
    pObject.setPartFiles(pObjectPartFiles.toArray(new RepresentationFilePreservationObject[pObjectPartFiles.size()]));

    return pObject;
  }

  private RepresentationFilePreservationObject createRepresentationFilePreservationObject(String roPID,
    RepresentationFile rFile) throws EditorException {

    RepresentationFilePreservationObject pObjectFile = new RepresentationFilePreservationObject();

    // <objectIdentifier>
    pObjectFile.setID(rFile.getId());

    // <preservationLevel>
    pObjectFile.setPreservationLevel(RepresentationFilePreservationObject.PRESERVATION_LEVEL_FULL);

    /*
     * <objectCharacteristics>
     */
    // <compositionLevel>
    pObjectFile.setCompositionLevel(0);

    // <fixity>
    try {

      Fixity[] fixities = new Fixity[2];

      fixities[0] = calculateFixity(roPID, rFile, "MD5");
      fixities[1] = calculateFixity(roPID, rFile, "SHA-1");

      pObjectFile.setFixities(fixities);

    } catch (NoSuchAlgorithmException e) {
      logger.debug("Error calculating datastream checksum - " + e.getMessage(), e);
      throw new EditorException("Error calculating datastream checksum - " + e.getMessage(), e);
    } catch (IOException e) {
      logger.debug("Error calculating datastream checksum - " + e.getMessage(), e);
      throw new EditorException("Error calculating datastream checksum - " + e.getMessage(), e);
    }

    // <size>
    pObjectFile.setSize(rFile.getSize());

    // <format><formatDesignation>
    pObjectFile.setFormatDesignationName(rFile.getMimetype());
    pObjectFile.setFormatDesignationVersion(rFile.getVersion());
    pObjectFile.setFormatRegistryName(rFile.getFormatRegistryName());
    pObjectFile.setFormatRegistryKey(rFile.getPuid());

    // <creatingApplication>
    // pObjectFile.setCreatingApplicationName();
    // pObjectFile.setCreatingApplicationVersion();
    // pObjectFile.setDateCreatedByApplication();
    // <objectCharacteristicsExtension>
    String objectCharacteristicsExtension = getPreservationFileObjectCharacteristicsExtension(roPID, rFile);
    pObjectFile.setObjectCharacteristicsExtension(objectCharacteristicsExtension);

    // <originalName>
    pObjectFile.setOriginalName(rFile.getOriginalName());

    // <storage><contentLocation>
    pObjectFile.setContentLocationType("RODAObjectPID/RODAObjectDatastreamID");
    pObjectFile.setContentLocationValue(roPID + "/" + rFile.getId());

    return pObjectFile;
  }

  private String getPreservationFileObjectCharacteristicsExtension(String roPID, RepresentationFile rFile)
    throws EditorException {

    try {

      // Get datastream from Fedora
      InputStream inputStream = getFedoraClientUtility().getDatastream(roPID, rFile.getId());
      // Create a temporary file to save the datastream
      File dsTempFile = File.createTempFile("rFile", "temp");
      FileOutputStream dsTempFileOutputStream = new FileOutputStream(dsTempFile);

      // Copy the datastream to the file
      IOUtils.copyLarge(inputStream, dsTempFileOutputStream);

      // Close input and output streams
      inputStream.close();
      dsTempFileOutputStream.close();

      // Inspect the file with FITS utility
      String fitsOutput = FITSUtility.inspect(dsTempFile);

      // Delete the temporary file
      dsTempFile.delete();

      return fitsOutput;

    } catch (Exception e) {
      logger.debug("Error inspecting representation file " + roPID + "/" + rFile.getId() + " - " + e.getMessage(), e);
      throw new EditorException("Error inspecting representation file " + roPID + "/" + rFile.getId() + " - "
        + e.getMessage(), e);
    }
  }

  private EventPreservationObject createIngestEvent(String[] doPIDs, String[] roPIDs, String[] poPIDs, String details,
    String agentPID, List<String> createdRepPObjectPIDs) {

    List<String> ingestedPIDs = new ArrayList<String>();

    if (doPIDs != null) {
      ingestedPIDs.addAll(Arrays.asList(doPIDs));
    }
    if (roPIDs != null) {
      ingestedPIDs.addAll(Arrays.asList(roPIDs));
    }
    if (poPIDs != null) {
      ingestedPIDs.addAll(Arrays.asList(poPIDs));
    }

    EventPreservationObject event = new EventPreservationObject();
    event.setState(RODAObject.STATE_INACTIVE);
    event.setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_INGESTION);
    event.setDatetime(new Date());

    event.setEventDetail("The objects inside the SIP were added to the preservation repository");

    event.setOutcome("success");
    event.setOutcomeDetailNote("Ingest details");
    event.setOutcomeDetailExtension(details);

    event.setAgentID(agentPID);
    event.setAgentRole(EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK);

    // event.setObjectIDs(ingestedPIDs
    // .toArray(new String[ingestedPIDs.size()]));
    event.setObjectIDs(createdRepPObjectPIDs.toArray(new String[createdRepPObjectPIDs.size()]));

    return event;
  }

  /**
   * Create preservation metadata for the specified representation and inserts a
   * new preservation object.
   * 
   * @param roPID
   *          the PID of the {@link RepresentationObject}.
   * @param derivationEventID
   * @param derivedFromRepresentationID
   * 
   * @return the PID of the {@link RepresentationPreservationObject}.
   * 
   * @throws NoSuchRODAObjectException
   * @throws FedoraRISearchException
   * @throws RepresentationAlreadyPreservedException
   * @throws BrowserException
   * @throws EditorException
   */
  private String createAndInsertRepresentationPreservationObject(String roPID, String derivationEventID,
    String derivedFromRepresentationID) throws EditorException, FedoraRISearchException, NoSuchRODAObjectException,
    RepresentationAlreadyPreservedException, BrowserException {

    RepresentationPreservationObject repPreservationObject = createRepresentationPreservationObject(roPID);

    repPreservationObject.setDerivationEventID(derivationEventID);
    repPreservationObject.setDerivedFromRepresentationObjectID(derivedFromRepresentationID);

    repPreservationObject.setState(RODAObject.STATE_INACTIVE);
    repPreservationObject.setRepresentationObjectPID(roPID);

    return insertRepresentationPreservationObject(repPreservationObject, null);
  }

  private String insertRepresentationPreservationObject(RepresentationPreservationObject rpo, String doPID)
    throws NoSuchRODAObjectException, RepresentationAlreadyPreservedException, EditorException {

    String roPID = rpo.getRepresentationObjectPID();

    SimpleRepresentationPreservationObject simpleRPO = null;

    if (roPID == null) {
      // RPO is orphan
      logger.debug("RPO representation object PID is null. RPO is orphan.");
    } else {

      try {

        getBrowserHelper().getRepresentationObject(roPID);

      } catch (BrowserException e) {
        logger.debug("Error getting representation object " + roPID + " - " + e.getMessage(), e);
        throw new EditorException("Error getting representation object " + roPID + " - " + e.getMessage(), e);
      }

      try {

        simpleRPO = getFedoraClientUtility().getFedoraRISearch().getROPreservationObject(roPID);

      } catch (FedoraRISearchException e) {
        logger.debug("Error getting representation preservation object " + roPID + " - " + e.getMessage(), e);
        throw new EditorException("Error getting representation preservation object " + roPID + " - " + e.getMessage(),
          e);
      }

    }

    if (simpleRPO == null) {

      // There's no RPO yet. Good!
      // Let's create one.
      rpo.setState(RODAObject.STATE_INACTIVE);
      rpo.setRepresentationObjectPID(roPID);

      String poPID = null;
      try {

        // Create an empty description object
        // poPID = getFedoraClientUtility().createObject(
        // rPreservationObject.getContentModel(),
        // rPreservationObject.getID(), ObjectState.Active);
        poPID = getFedoraClientUtility().createObject(rpo);

        rpo.setPid(poPID);

        // Transforms the Preservation Object into a byte array to send
        // to Fedora
        byte[] serializedRepresentationPremis = new PremisRepresentationObjectHelper(rpo).saveToByteArray();

        // Add representation PREMIS metadata
        addPREMISDatastream(poPID, this.preservationObjectDatastreamID, "Representation Preservation Metadata",
          serializedRepresentationPremis);

        // Add representation files PREMIS metadata
        if (rpo.getRootFile() != null) {
          addFilePremisDatatream(poPID, rpo.getRootFile());
        }

        if (rpo.getPartFiles() != null) {

          for (RepresentationFilePreservationObject partFile : rpo.getPartFiles()) {

            addFilePremisDatatream(poPID, partFile);
          }
        }

      } catch (Exception e) {

        logger.error("Exception creating preservation object - " + e.getMessage() + ". Deleting created object "
          + poPID, e);

        removeObjects(new String[] {poPID});

        throw new EditorException("Exception creating preservation object - " + e.getMessage(), e);
      }

      try {

        if (roPID == null) {
          // RPO is orphan

          // Copy permissions from description object
          copyPermissions(doPID, poPID);

        } else {

          // Copy permissions from representation object
          copyPermissions(roPID, poPID);

        }

      } catch (Exception e) {

        logger.debug("Error copying permissions from " + roPID + " to " + poPID + ". Deleting created object " + poPID,
          e);

        removeObjects(new String[] {poPID});

        throw new EditorException("Error copying permissions from " + roPID + " to " + poPID + " - " + e.getMessage(),
          e);
      }

      try {

        // getFedoraClientUtility().addPreservedByRelationship(roPID,
        // poPID);
        getFedoraClientUtility().addPreservationOfRelationship(poPID, roPID);

      } catch (Exception e) {

        logger.debug("Exception creating relationship between " + roPID + " and " + poPID
          + ". Deleting created object " + poPID, e);

        removeObjects(new String[] {poPID});

        throw new EditorException("Exception creating relationship between " + roPID + " and " + poPID + " - "
          + e.getMessage(), e);
      }

      return poPID;

    } else {

      // Representation already has a RepresentationPreservationObject
      logger.warn("RepresentationObject " + roPID + " already has a PreservationObject (" + simpleRPO.getPid() + ")");
//      throw new RepresentationAlreadyPreservedException("RepresentationObject " + roPID
//        + " already has a PreservationObject (" + simpleRPO.getPid() + ")");
      
      // TODO update RPO
      try {
        RepresentationPreservationObject existingRPO = browserHelper.getRepresentationPreservationObject(simpleRPO);
        if(StringUtils.isNotBlank(rpo.getDerivationEventID())) {
          existingRPO.setDerivationEventID(rpo.getDerivationEventID());
        }
        
        if(StringUtils.isNotBlank(rpo.getDerivedFromRepresentationObjectID())) {
          existingRPO.setDerivedFromRepresentationObjectID(rpo.getDerivedFromRepresentationObjectID());
        }
        
        // Transforms the Preservation Object into a byte array to send
        // to Fedora
        byte[] serializedRepresentationPremis = new PremisRepresentationObjectHelper(existingRPO).saveToByteArray();

        // Add representation PREMIS metadata
        addPREMISDatastream(simpleRPO.getPid(), this.preservationObjectDatastreamID, "Representation Preservation Metadata",
          serializedRepresentationPremis);
        
        
      } catch (BrowserException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (FedoraClientException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (PremisMetadataException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      return simpleRPO.getPid();

    }

  }

  private String insertEventPreservationObject(EventPreservationObject event, String[] performedOnPIDs, String agentPID)
    throws EditorException {

    String poPID = null;
    try {

      // Create an empty preservation object
      poPID = getFedoraClientUtility().createObject(event);

      // Sets the ID of the Event to the object PID
      event.setID(poPID);

      // Transforms the Preservation Object into a byte array to send
      // to Fedora
      byte[] serializedPremisEvent = new PremisEventHelper(event).saveToByteArray();

      // Add representation PREMIS metadata
      addPREMISDatastream(poPID, this.preservationObjectDatastreamID, "Event Preservation Metadata",
        serializedPremisEvent);

      // Create relationships with objects and agent
      getFedoraClientUtility().addPerformedOnRelationships(poPID, performedOnPIDs);
      getFedoraClientUtility().addPerformedByRelationship(poPID, agentPID);

      return poPID;

    } catch (Exception e) {

      logger.error("Exception creating preservation object - " + e.getMessage() + ". Deleting object " + poPID, e);

      if (poPID != null) {
        // If object was created, delete it
        removeObjects(new String[] {poPID});
      }

      throw new EditorException("Exception creating preservation object - " + e.getMessage(), e);
    }

  }

  private String insertAgentPreservationObject(AgentPreservationObject agent) throws EditorException {

    if (agent == null) {
      throw new EditorException("agent is null");
    }

    if (!AgentPreservationObject.TYPE.equals(agent.getType())) {
      throw new EditorException("agent contentModel type is " + agent.getType() + " and should be "
        + AgentPreservationObject.TYPE);
    }

    if (StringUtils.isBlank(agent.getAgentName())) {
      throw new EditorException("agent name is empty");
    }

    if (StringUtils.isBlank(agent.getAgentType())) {
      throw new EditorException("agent type is empty");
    }

    if (!Arrays.asList(AgentPreservationObject.PRESERVATION_AGENT_TYPES).contains(agent.getAgentType())) {

      // throw new EditorException("agent type is unknown");
      logger.warn("Agent type '" + agent.getAgentType() + "' is unknown. Setting agent type to "
        + AgentPreservationObject.PRESERVATION_AGENT_TYPE_UNKNOWN_PREFIX + agent.getAgentType());
      agent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_UNKNOWN_PREFIX + agent.getAgentType());
    }

    String poPID = null;
    try {

      // Create an empty description object
      // poPID = getFedoraClientUtility().createObject(
      // agent.getContentModel(), agent.getAgentName(),
      // ObjectState.Active);
      poPID = getFedoraClientUtility().createObject(agent);
      agent.setPid(poPID);
      // Sets the ID of the Agent to the object PID
      agent.setID(poPID);

      // Transforms the Preservation Object into a byte array to send to
      // Fedora
      PremisAgentHelper premisHelper = new PremisAgentHelper();
      premisHelper.setAgentPreservationObject(agent);

      byte[] serializedRepresentationPremis = premisHelper.saveToByteArray();

      // Add representation PREMIS metadata
      addPREMISDatastream(poPID, this.preservationObjectDatastreamID, "Agent Preservation Metadata",
        serializedRepresentationPremis);

      // Set public permissions for agents
      setPermissions(getPublicPermissions(poPID));

      return poPID;

    } catch (Exception e) {

      logger.error("Exception creating preservation object - " + e.getMessage() + ". Deleting object " + poPID, e);

      if (poPID != null) {
        // If object was created, delete it
        removeObjects(new String[] {poPID});
      }

      throw new EditorException("Exception creating preservation object - " + e.getMessage(), e);
    }

  }

  private void addFilePremisDatatream(String poPID, RepresentationFilePreservationObject rFile)
    throws PremisMetadataException, FedoraClientException, RemoteException {

    logger.debug("Adding RepresentationFilePreservationObject " + rFile);

    PremisFileObjectHelper premisFileObjectHelper = new PremisFileObjectHelper(rFile);
    logger.debug("Adding PREMIS object " + premisFileObjectHelper.getObjectDocument());

    addPREMISDatastream(poPID, rFile.getID(), "File Preservation Metadata",
      new PremisFileObjectHelper(rFile).saveToByteArray());

  }

  private void addPREMISDatastream(String poPID, String dsID, String dsLabel, byte[] datastream)
    throws FedoraClientException, RemoteException {

    // Upload the datastream to Fedora
    String temporaryURL = getFedoraClientUtility().temporaryUpload(datastream);

    // Add the uploaded PREMIS as a datastream of the new object
    getFedoraClientUtility().getAPIM().addDatastream(poPID, dsID, new String[0], dsLabel, true, "text/xml", null,
      temporaryURL, "M", "A", null, null, "Added by RODA Core Services");

    logger.info("Added datastream " + dsID + " to object " + poPID);
  }

  private Fixity calculateFixity(String roPID, RepresentationFile rFile, String digestAlgorithm) throws IOException,
    NoSuchAlgorithmException {

    InputStream dsInputStream = getFedoraClientUtility().getDatastream(roPID, rFile.getId());

    Fixity fixity = new Fixity(digestAlgorithm, FileUtility.calculateChecksumInHex(dsInputStream, digestAlgorithm),
      "RODA Core Services");

    dsInputStream.close();

    return fixity;
  }

  // FIXME add javadocs
  public DescriptionObject findVolumeParent(CASUserPrincipal casUserPrincipal, String fedoraGSearchURL,
    String classPID, String caseFileFullId, String subfileFullId) throws IngestException {

    DescriptionObject lastDOFound = null;
    FedoraGSearch fedoraGSearch = getFedoraGSearch(casUserPrincipal, fedoraGSearchURL);

    lastDOFound = findCaseFileDescriptionObject(fedoraGSearch, classPID, caseFileFullId);

    if (lastDOFound != null) {

      lastDOFound = findSubFileDescriptionObject(fedoraGSearch, lastDOFound, subfileFullId);

    }

    return lastDOFound;
  }

  private DescriptionObject findCaseFileDescriptionObject(FedoraGSearch fedoraGSearch, String classPID,
    String caseFileFullId) throws IngestException {
    DescriptionObject lastDOFound = null;

    DefaultSearchParameter caseFileSearchParameter = new DefaultSearchParameter(new String[] {EadcSearchFields.LEVEL},
      "casefile", DefaultSearchParameter.MATCH_EXACT_PHRASE);
    DefaultSearchParameter caseFileFullIdSearchParameter = new DefaultSearchParameter(
      new String[] {EadcSearchFields.ACQINFO_NUM_FULL_ID}, caseFileFullId, DefaultSearchParameter.MATCH_EXACT_PHRASE);
    SearchParameter[] caseFilesearchParameters = new SearchParameter[] {caseFileSearchParameter,
      caseFileFullIdSearchParameter};

    try {
      SearchResult caseFileSearch = fedoraGSearch.advancedSearch(caseFilesearchParameters, 0, Integer.MAX_VALUE, 0,
        Integer.MAX_VALUE);
      SearchResultObject[] caseFileSearchResultObjects = caseFileSearch.getSearchResultObjects();
      for (int i = 0; i < caseFileSearchResultObjects.length; i++) {
        DescriptionObject caseFileDO = this.getBrowserHelper().getDescriptionObject(
          caseFileSearchResultObjects[i].getDescriptionObject().getPid());
        if (caseFileDO.getParentPID().equals(classPID)) {
          lastDOFound = caseFileSearchResultObjects[i].getDescriptionObject();
          logger.info("Found casefile with PID=" + lastDOFound.getPid());
          break;
        }
      }
    } catch (FedoraGSearchException e) {
      throw new IngestException("Error while searching for casefile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + caseFileFullId + "\"", e);
    } catch (InvalidDescriptionLevel e) {
      throw new IngestException("Error while searching for casefile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + caseFileFullId + "\"", e);
    } catch (BrowserException e) {
      throw new IngestException("Error while searching for casefile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + caseFileFullId + "\"", e);
    } catch (NoSuchRODAObjectException e) {
      throw new IngestException("Error while searching for casefile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + caseFileFullId + "\"", e);
    }

    return lastDOFound;
  }

  private DescriptionObject findSubFileDescriptionObject(FedoraGSearch fedoraGSearch,
    DescriptionObject descriptionObject, String subfileFullId) throws IngestException {
    DescriptionObject lastDOFound = descriptionObject;

    DefaultSearchParameter subFileSearchParameter = new DefaultSearchParameter(new String[] {EadcSearchFields.LEVEL},
      "subfile", DefaultSearchParameter.MATCH_EXACT_PHRASE);
    DefaultSearchParameter subFileFullIdSearchParameter = new DefaultSearchParameter(
      new String[] {EadcSearchFields.ACQINFO_NUM_FULL_ID}, subfileFullId, DefaultSearchParameter.MATCH_EXACT_PHRASE);
    SearchParameter[] subFileSearchParameters = new SearchParameter[] {subFileSearchParameter,
      subFileFullIdSearchParameter};

    try {
      SearchResult subFileSearch = fedoraGSearch.advancedSearch(subFileSearchParameters, 0, Integer.MAX_VALUE, 0,
        Integer.MAX_VALUE);

      SearchResultObject[] subFileSearchResultObjects = subFileSearch.getSearchResultObjects();
      for (int i = 0; i < subFileSearchResultObjects.length; i++) {
        DescriptionObject subfileDO = this.getBrowserHelper().getDescriptionObject(
          subFileSearchResultObjects[i].getDescriptionObject().getPid());
        if (subfileDO.getParentPID().equals(descriptionObject.getPid())) {
          lastDOFound = subFileSearchResultObjects[i].getDescriptionObject();
          logger.info("Found subfile with PID=" + lastDOFound.getPid());
          break;
        }
      }
    } catch (FedoraGSearchException e) {
      throw new IngestException("Error while searching for subfile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + subfileFullId + "\"", e);
    } catch (InvalidDescriptionLevel e) {
      throw new IngestException("Error while searching for subfile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + subfileFullId + "\"", e);
    } catch (BrowserException e) {
      throw new IngestException("Error while searching for subfile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + subfileFullId + "\"", e);
    } catch (NoSuchRODAObjectException e) {
      throw new IngestException("Error while searching for subfile with " + EadcSearchFields.ACQINFO_NUM_FULL_ID
        + "=\"" + subfileFullId + "\"", e);
    }

    return lastDOFound;
  }

  // FIXME add javadocs
  public String[] getPossibleParents(CASUserPrincipal casUserPrincipal, String fedoraGSearchURL, String classFullId)
    throws IngestException {

    List<String> pids = new ArrayList<String>();

    FedoraGSearch fedoraGSearch = getFedoraGSearch(casUserPrincipal, fedoraGSearchURL);

    DefaultSearchParameter classSearchParameter = new DefaultSearchParameter(new String[] {EadcSearchFields.LEVEL},
      "class", DefaultSearchParameter.MATCH_EXACT_PHRASE);
    DefaultSearchParameter classFullIdSearchParameter = new DefaultSearchParameter(
      new String[] {EadcSearchFields.ACQINFO_NUM_FULL_ID}, classFullId, DefaultSearchParameter.MATCH_EXACT_PHRASE);
    SearchParameter[] searchParameters = new SearchParameter[] {classSearchParameter, classFullIdSearchParameter};

    try {
      SearchResult advancedSearch = fedoraGSearch.advancedSearch(searchParameters, 0, Integer.MAX_VALUE, 0,
        Integer.MAX_VALUE);
      SearchResultObject[] searchResultObjects = advancedSearch.getSearchResultObjects();
      for (int i = 0; i < searchResultObjects.length; i++) {
        pids.add(searchResultObjects[i].getDescriptionObject().getPid());
      }

    } catch (FedoraGSearchException e) {
      new IngestException("Error while searching for classes with " + EadcSearchFields.ACQINFO_NUM_FULL_ID + "=\""
        + classFullId + "\"", e);
    }

    return pids.toArray(new String[pids.size()]);

  }

  // FIXME add javadocs
  public String[] getPossibleParentsByClassificationSchemeId(String producerUsername, String classificationSchemeId,
    String[] possibleParentsPids) throws IngestException {
    String[] possibleParentsPidsToReturn = new String[] {};

    if (possibleParentsPids != null && possibleParentsPids.length > 0) {
      Filter filter = new Filter();
      filter.add(new ProducerFilterParameter(producerUsername));
      // FIXME
      //filter.add(new ClassificationSchemeFilterParameter(classificationSchemeId, possibleParentsPids));

      logger.debug("Filter to be applied: " + filter);

      try {
        List<SimpleDescriptionObject> simpleDescriptionObjects = getBrowserHelper().getSimpleDescriptionObjects(
          new ContentAdapter(filter, null, null));
        logger.debug("Result list size: " + simpleDescriptionObjects.size());
        possibleParentsPidsToReturn = new String[simpleDescriptionObjects.size()];
        for (int i = 0; i < simpleDescriptionObjects.size(); i++) {
          logger.info("(" + i + ") SDO pid: " + simpleDescriptionObjects.get(i).getPid());
          possibleParentsPidsToReturn[i] = simpleDescriptionObjects.get(i).getPid();
        }
      } catch (BrowserException e) {
        throw new IngestException("Error while retrieving possible parents description objects", e);
      } catch (Throwable e) {
        logger.error("Unexpected error", e);
        throw new IngestException("Error while retrieving possible parents description objects", e);
      }
    }
    return possibleParentsPidsToReturn;
  }

  private FedoraGSearch getFedoraGSearch(User clientUser, String fedoraGSearchURL) throws IngestException {

    FedoraGSearch fgs = null;
    if (clientUser != null) {

      try {

        fgs = new FedoraGSearch(new URL(fedoraGSearchURL), clientUser);
      } catch (MalformedURLException e) {

        logger.error("MalformedURLException in FedoraGSearch service URL", e);

        throw new IngestException("Malformed URL Exception in FedoraGSearch service URL - " + e.getMessage(), e);
      } catch (FedoraGSearchException e) {
        throw new IngestException("Error creating FedoraGSearch service client - " + e.getMessage(), e);
      }
      return fgs;

    } else {

      throw new IngestException("User credentials are not available.");
    }

  }

}
