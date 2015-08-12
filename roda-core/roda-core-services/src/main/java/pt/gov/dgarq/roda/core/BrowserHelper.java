package pt.gov.dgarq.roda.core;

import fedora.client.Downloader;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.DatastreamDef;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import static pt.gov.dgarq.roda.common.FormatUtility.getMimetype;
import static pt.gov.dgarq.roda.common.FormatUtility.saveStreamAsTempFile;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.NoSuchRepresentationFileException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.RODAObjectUserPermissions;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility.DatastreamState;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearch;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearchException;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCHelper;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.core.metadata.premis.PremisAgentHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisEventHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisFileObjectHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisMetadataException;
import pt.gov.dgarq.roda.core.metadata.premis.PremisRepresentationObjectHelper;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyHelper;
import pt.gov.dgarq.roda.core.metadata.xacml.PolicyMetadataException;
import pt.gov.dgarq.roda.core.services.UserBrowser;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

/**
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class BrowserHelper {

    static final private Logger logger = Logger.getLogger(BrowserHelper.class);
    protected final String descriptionObjectDatastreamID;
    protected final List<String> representationObjectDefaultDatastreams;
    protected final String preservationObjectDatastreamID;
    protected final String policyDatastreamID;
    private final String fedoraAdminUsername;
    private final String rodaNAHandleURL;
    // private Configuration configuration = null;
    private FedoraClientUtility fedoraClientUtility = null;
    private FedoraRISearch fedoraRISearch = null;
    private CASUserPrincipal user = null;

    /**
     * Constructs a new {@link BrowserHelper}.
     *
     * @param fedoraClientUtility
     * @param configuration
     */
    public BrowserHelper(FedoraClientUtility fedoraClientUtility,
            Configuration configuration) {

        // this.configuration = configuration;
        this.fedoraClientUtility = fedoraClientUtility;
        this.fedoraRISearch = fedoraClientUtility.getFedoraRISearch();

        this.user = this.fedoraClientUtility.getCASUserPrincipal();

        this.descriptionObjectDatastreamID = configuration
                .getString("descriptionObjectDatastreamID");

        this.representationObjectDefaultDatastreams = Arrays
                .asList(configuration
                .getStringArray("representationObjectDefaultDatastreams"));

        this.preservationObjectDatastreamID = configuration
                .getString("preservationObjectDatastreamID");

        this.policyDatastreamID = configuration.getString("policyDatastreamID");

        this.fedoraAdminUsername = configuration
                .getString("fedoraAdminUsername");

        this.rodaNAHandleURL = configuration.getString("rodaNAHandleURL");
    }

    /**
     * Returns the {@link RODAObject} with the given PID.
     *
     * @param pid the PID of the object.
     * @return a {@link RODAObject} for the given PID.
     * @throws BrowserException
     * @throws NoSuchRODAObjectException if the object with the specified PID
     * does not exist.
     */
    public RODAObject getRODAObject(String pid) throws BrowserException,
            NoSuchRODAObjectException {
        try {

            return this.fedoraRISearch.getRODAObject(pid);

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the number of {@link RODAObject}s that match the given
     * {@link Filter}.
     *
     * @param filter the {@link ContentAdapter}'s {@link Filter}.
     *
     * @return an <code>int</code> with the number of {@link RODAObject}s that
     * match the given {@link Filter}.
     *
     * @throws BrowserException
     */
    public int getRODAObjectCount(Filter filter) throws BrowserException {

        try {

            return this.fedoraRISearch.getRODAObjectCount(filter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleDescriptionObject count from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleDescriptionObject count from RI Search - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Gets the list of {@link RODAObject}s that match the given
     * {@link ContentAdapter}'s {@link Filter}.
     *
     * @param contentAdapter the {@link ContentAdapter}.
     *
     * @return a {@link List} of {@link RODAObject} that match the given
     * {@link ContentAdapter}.
     *
     * @throws BrowserException
     */
    public List<RODAObject> getRODAObjects(ContentAdapter contentAdapter)
            throws BrowserException {

        try {

            return this.fedoraRISearch.getRODAObjects(contentAdapter);

        } catch (FedoraRISearchException e) {
            logger.debug("Exception getting RODAObjects from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting RODAObjects from RI Search - "
                    + e.getMessage(), e);
        }

    }

    /**
     * @param doPID the PID of the Description Object.
     *
     * @return the {@link SimpleDescriptionObject} for the given DO PID.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     * @throws BrowserException
     */
    public SimpleDescriptionObject getSimpleDescriptionObject(String doPID)
            throws BrowserException, NoSuchRODAObjectException {

        try {

            return this.fedoraRISearch.getSimpleDescriptionObject(doPID);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleDescriptionObject from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleDescriptionObject from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the number of {@link DescriptionObject}s that match the given
     * {@link Filter}.
     *
     * @param filter the {@link ContentAdapter}'s {@link Filter}.
     *
     * @return an <code>int</code> with the number of {@link DescriptionObject}s
     * that match the given {@link Filter}.
     *
     * @throws BrowserException
     */
    public int getSimpleDescriptionObjectCount(Filter filter)
            throws BrowserException {

        try {

            fillProducerFilterParameters(filter);

            return this.fedoraRISearch.getSimpleDescriptionObjectCount(filter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleDescriptionObject count from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleDescriptionObject count from RI Search - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Gets the list of {@link SimpleDescriptionObject}s that match the given
     * {@link ContentAdapter}'s {@link Filter}.
     *
     * @param contentAdapter the {@link ContentAdapter}.
     *
     * @return a {@link List} of {@link SimpleDescriptionObject} that match the
     * given {@link ContentAdapter}.
     *
     * @throws BrowserException
     */
    public List<SimpleDescriptionObject> getSimpleDescriptionObjects(
            ContentAdapter contentAdapter) throws BrowserException {

        try {

            if (contentAdapter != null) {
                fillProducerFilterParameters(contentAdapter.getFilter());
            }

            return this.fedoraRISearch
                    .getSimpleDescriptionObjects(contentAdapter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleDescriptionObjects from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleDescriptionObjects from RI Search - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Returns the index of given {@link DescriptionObject}'s pid within the
     * list of {@link DescriptionObject}s for the specified
     * {@link ContentAdapter}.
     *
     * @param pid the PID of the {@link DescriptionObject}.
     * @param contentAdapter the {@link ContentAdapter}.
     *
     * @return an <code>int</code> with the index of the given pid within the
     * list of results for the specified {@link ContentAdapter}.
     *
     * @throws BrowserException
     */
    public int getSimpleDescriptionObjectIndex(String pid,
            ContentAdapter contentAdapter) throws BrowserException {
        try {
            if (contentAdapter != null) {
                fillProducerFilterParameters(contentAdapter.getFilter());
            }

            List<SimpleDescriptionObject> subElements = this.fedoraRISearch
                    .getSimpleDescriptionObjects(contentAdapter);

            // the contentModel "roda:d" it's only to create a valid
            // RODAObject.
            return subElements.indexOf(new RODAObject(pid, null, "roda:d"));

        } catch (FedoraRISearchException e) {
            logger.debug("Exception getting object index - " + e.getMessage(),
                    e);
            throw new BrowserException("Exception getting object index - "
                    + e.getMessage(), e);
        }
    }

    /**
     * @param doPID the PID of the Description Object.
     *
     * @return the DescriptionObject for the given DO PID.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     * @throws InvalidDescriptionLevel if the level being set is not one of
     * <ul>
     * <li>{@link DescriptionLevel#FONDS},</li>
     * <li>{@link DescriptionLevel#SUBFONDS},</li>
     * <li>{@link DescriptionLevel#CLASS},</li>
     * <li>{@link DescriptionLevel#SUBCLASS},</li>
     * <li>{@link DescriptionLevel#SERIES},</li>
     * <li>{@link DescriptionLevel#SUBSERIES},</li>
     * <li>{@link DescriptionLevel#FILE},</li>
     * <li>{@link DescriptionLevel#ITEM}.</li>
     * </ul>
     * @throws BrowserException
     */
    public DescriptionObject getDescriptionObject(String doPID)
            throws NoSuchRODAObjectException, InvalidDescriptionLevel,
            BrowserException {

        SimpleDescriptionObject sdo = getSimpleDescriptionObject(doPID);

        try {

            InputStream eadcInputStream = this.fedoraClientUtility
                    .getDatastream(doPID, descriptionObjectDatastreamID);

            DescriptionObject dObject = EadCHelper.newInstance(eadcInputStream)
                    .getDescriptionObject(sdo, sdo.getParentPID(),
                    sdo.getSubElementsCount());

            setCompleteReference(dObject);

            dObject.setHandleURL(getHandleURLForPID(dObject.getPid()));

            return dObject;

        } catch (IOException e) {
            logger.debug("Exception getting EAD-C stream - " + e.getMessage(),
                    e);
            throw new BrowserException("Exception getting EAD-C stream - "
                    + e.getMessage(), e);
        } catch (EadCMetadataException e) {
            logger.debug("Exception parsing EAD-C - " + e.getMessage(), e);
            throw new BrowserException("Exception parsing EAD-C - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Returns a list of all Description Object PIDs.
     *
     * @return a {@link List} of {@link String} with the PIDs of all DOs.
     *
     * @throws BrowserException
     */
    public List<String> getDOPIDs() throws BrowserException {
        try {

            return this.fedoraRISearch.getDOPIDs();

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns a list of ancestor PIDs for this Description Object.
     *
     * @param doPID the PID of the Descriptive Object.
     *
     * @return an array of {@link String} with the ancestor PIDs, from this DO
     * to the fonds DO.
     *
     * @throws BrowserException
     * @throws NoSuchRODAObjectException
     */
    public List<String> getDOAncestorPIDs(String doPID)
            throws BrowserException, NoSuchRODAObjectException {
        try {

            return this.fedoraRISearch.getDOAncestorPIDs(doPID);

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the completeReference of a {@link DescriptionObject}.
     *
     * @param doPID the PID of the {@link DescriptionObject}
     *
     * @return a {@link String} with the complete reference.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public String getDOCompleteReference(String doPID)
            throws NoSuchRODAObjectException, BrowserException {

        SimpleDescriptionObject simpleDO = getSimpleDescriptionObject(doPID);

        List<String> ancestorPIDs = getDOAncestorPIDs(simpleDO.getPid());
        String completeReference = null;

        if (ancestorPIDs.size() == 1) {

            // this is a fonds
            completeReference = simpleDO.getCountryCode() + "/"
                    + simpleDO.getRepositoryCode() + "/" + simpleDO.getId();

        } else {

            // Get countryCode and repositoryCode for the fonds element
            SimpleDescriptionObject sdoFonds = getSimpleDescriptionObject(ancestorPIDs
                    .get(0));

            completeReference = sdoFonds.getCountryCode() + "/"
                    + sdoFonds.getRepositoryCode();

            for (String ancestorPID : ancestorPIDs) {
                SimpleDescriptionObject sdo = getSimpleDescriptionObject(ancestorPID);

                if (completeReference != null) {
                    completeReference = completeReference + "/" + sdo.getId();
                } else {
                    completeReference = sdo.getId();
                }
            }
        }

        return completeReference;
    }

    /**
     * Gets the original {@link RepresentationObject} of a given
     * {@link DescriptionObject}.
     *
     * @param doPID the PID of the {@link DescriptionObject}.
     *
     * @return the original {@link RepresentationObject} for the given DO PID or
     * <code>null</code> if the given {@link DescriptionObject} doesn't contain
     * an original {@link RepresentationObject}.
     *
     * @throws NoSuchRODAObjectException the the specified <code>doPID</code>
     * doesn't exist.
     * @throws BrowserException
     */
    public RepresentationObject getDOOriginalRepresentation(String doPID)
            throws BrowserException, NoSuchRODAObjectException {
        return getDORepresentation(doPID, RepresentationObject.STATUS_ORIGINAL);
    }

    /**
     * Gets the normalized {@link RepresentationObject} of a given
     * {@link DescriptionObject}.
     *
     * @param doPID the PID of the Description Object.
     *
     * @return the {@link RepresentationObject} for the given DO PID or
     * <code>null</code> if the given {@link DescriptionObject} doesn't contain
     * an normalized {@link RepresentationObject}.
     *
     * @throws NoSuchRODAObjectException if the specified
     * {@link DescriptionObject} PID doesn't exist.
     *
     * @throws BrowserException
     */
    public RepresentationObject getDONormalizedRepresentation(String doPID)
            throws BrowserException, NoSuchRODAObjectException {
        return getDORepresentation(doPID,
                RepresentationObject.STATUS_NORMALIZED);
    }

    /**
     * Gets all the {@link RepresentationObject}s of a given
     * {@link DescriptionObject}.
     *
     * @param doPID the PID of the {@link DescriptionObject}.
     *
     * @return an array of {@link RepresentationObject}s for the given DO PID.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     *
     * @throws BrowserException
     */
    public RepresentationObject[] getDORepresentations(String doPID)
            throws NoSuchRODAObjectException, BrowserException {
        try {

            Filter filter = new Filter(new SimpleFilterParameter(
                "descriptionObjectPID", doPID));

            List<SimpleRepresentationObject> simpleROs = this.fedoraRISearch
                    .getSimpleRepresentationObjects(new ContentAdapter(filter,
                    null, null));

            List<RepresentationObject> representations = new ArrayList<RepresentationObject>();

            for (SimpleRepresentationObject simpleRO : simpleROs) {
                representations.add(getRepresentationObject(simpleRO));
            }

            return representations
                    .toArray(new RepresentationObject[representations.size()]);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting representation - " + e.getMessage(), e);
            throw new BrowserException("Exception getting representation - "
                    + e.getMessage(), e);
        }
    }

    private List<String> getDORepresentationPreservationObjectPIDs(String doPID)
            throws BrowserException, NoSuchRODAObjectException {

        List<String> rpoPIDs = new ArrayList<String>();

        SimpleRepresentationObject simpleRO = null;

        simpleRO = getDOSimpleRepresentation(doPID,
                RepresentationObject.STATUS_NORMALIZED);

        if (simpleRO != null) {
            logger.debug("DO " + doPID + " has a normalized RO " + simpleRO);
        }

        if (simpleRO == null) {

            logger.debug("DO " + doPID + " doesn't have a normalized RO");

            simpleRO = getDOSimpleRepresentation(doPID,
                    RepresentationObject.STATUS_ALTERNATIVE);

            if (simpleRO != null) {
                logger.debug("DO " + doPID + " has an alternative RO "
                        + simpleRO);
            }

        }

        if (simpleRO == null) {

            logger.debug("DO " + doPID + " doesn't have an alternative RO");

            simpleRO = getDOSimpleRepresentation(doPID,
                    RepresentationObject.STATUS_ORIGINAL);

            if (simpleRO != null) {
                logger.debug("DO " + doPID + " has an original RO " + simpleRO);
            }

        }

        if (simpleRO == null) {

            logger.debug("DO " + doPID + " doesn't have ROs");

        } else {

            try {

                rpoPIDs.addAll(this.fedoraRISearch
                        .getRORepresentationPreservationObjectPIDs(simpleRO
                        .getPid()));

            } catch (FedoraRISearchException e) {
                logger.debug("Error getting RPOs for RO " + simpleRO.getPid()
                        + " - " + e.getMessage(), e);
                throw new BrowserException("Error getting RPOs for RO "
                        + simpleRO.getPid() + " - " + e.getMessage(), e);
            }
        }

        return rpoPIDs;
    }

    /**
     * Gets all the {@link RepresentationPreservationObject}s associated with
     * the given {@link DescriptionObject} PID.
     *
     * @param doPID the PID of the {@link DescriptionObject}.
     *
     * @return an array of {@link RepresentationPreservationObject}
     *
     * @throws BrowserException
     * @throws NoSuchRODAObjectException
     */
    public RepresentationPreservationObject[] getDORepresentationPreservationObjects(
            String doPID) throws BrowserException, NoSuchRODAObjectException {

        List<RepresentationPreservationObject> rpos = new ArrayList<RepresentationPreservationObject>();

        List<String> rpoPIDs = getDORepresentationPreservationObjectPIDs(doPID);

        for (String rpoPID : rpoPIDs) {
            rpos.add(getRepresentationPreservationObject(rpoPID));
        }

        return rpos.toArray(new RepresentationPreservationObject[rpos.size()]);
    }

    /**
     * Gets the PIDs of all {@link RODAObject}s, descendant of the given
     * {@link RODAObject}. Descendants can be {@link DescriptionObject}s,
     * {@link RepresentationObject}s, {@link RepresentationPreservationObject}
     * and {@link EventPreservationObject}s.
     *
     * @param PID the PID of the {@link RODAObject}.
     *
     * @return a {@link List} of {@link String}s with the PIDs of the
     * descendants.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public List<String> getDODescendantPIDs(String PID)
            throws NoSuchRODAObjectException, BrowserException {
        try {

            return this.fedoraRISearch.getDODescendantPIDs(PID, true);

        } catch (FedoraRISearchException e) {
            logger.debug(e.getMessage(), e);
            throw new BrowserException(e.getMessage(), e);
        }
    }

    /**
     * Returns the {@link SimpleRepresentationObject} with the given PID.
     *
     * @param roPID the PID of the {@link RepresentationObject}.
     *
     * @return a {@link SimpleRepresentationObject} for the given PID.
     *
     * @throws NoSuchRODAObjectException if the specified PID does not exist.
     * @throws BrowserException if something goes wrong with the operation.
     */
    public SimpleRepresentationObject getSimpleRepresentationObject(String roPID)
            throws NoSuchRODAObjectException, BrowserException {

        try {

            return this.fedoraRISearch.getSimpleRepresentationObject(roPID);

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the number of {@link RepresentationObject}s that match the given
     * {@link Filter}.
     *
     * @param filter the {@link ContentAdapter}'s {@link Filter}.
     *
     * @return an <code>int</code> with the number of
     * {@link RepresentationObject}s that match the given {@link Filter} .
     * @throws BrowserException
     */
    public int getSimpleRepresentationObjectCount(Filter filter)
            throws BrowserException {
        try {

            return this.fedoraRISearch
                    .getSimpleRepresentationObjectCount(filter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting RepresentationObject count from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting RepresentationObject count from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the list of {@link SimpleRepresentationObject}s that match the given
     * {@link ContentAdapter}'s {@link Filter}.
     *
     * @param contentAdapter the {@link ContentAdapter}.
     *
     * @return an {@link List} of {@link SimpleRepresentationObject} that match
     * the given {@link ContentAdapter}.
     *
     * @throws BrowserException
     */
    public List<SimpleRepresentationObject> getSimpleRepresentationObjects(
            ContentAdapter contentAdapter) throws BrowserException {
        try {

            return this.fedoraRISearch
                    .getSimpleRepresentationObjects(contentAdapter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleRepresentationObjects from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleRepresentationObjects from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the {@link RepresentationObject} with the given PID.
     *
     * @param roPID the PID of the {@link RepresentationObject}.
     *
     * @return a {@link RepresentationObject} for the given PID.
     *
     * @throws NoSuchRODAObjectException if the specified PID does not exist.
     * @throws BrowserException if something goes wrong with the operation.
     */
    public RepresentationObject getRepresentationObject(String roPID)
            throws NoSuchRODAObjectException, BrowserException {

        try {

            return getRepresentationObject(this.fedoraRISearch
                    .getSimpleRepresentationObject(roPID));

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the {@link RepresentationFile} with the specified ID from specified
     * the representation object.
     *
     * @param roPID the PID of the {@link RepresentationObject}.
     * @param fileID the {@link RepresentationFile} ID.
     *
     * @return a {@link RepresentationFile} for the given PID and fileID or
     * <code>null</code>.
     *
     * @throws NoSuchRODAObjectException if the specified object doesn't exist.
     * @throws NoSuchRepresentationFileException if the specified representation
     * file doesn't exist.
     * @throws BrowserException
     */
    public RepresentationFile getRepresentationFile(String roPID, String fileID)
            throws NoSuchRepresentationFileException, BrowserException,
            NoSuchRODAObjectException {

        try {

            // Throws NoSuchRODAObjectException if the object doesn't exist.
            getRODAObject(roPID);

            // Look for the requested datastream
              Datastream[] datastreams = this.fedoraClientUtility.getAPIM().getDatastreams(roPID, null,
                fedoraClientUtility.getStateCode(DatastreamState.Active));
           
            Datastream datastream = null;
            if (datastreams != null) {

                for (Datastream ds : datastreams) {

                    if (ds.getID().equals(fileID)) {

                        datastream = ds;
                        break;
                    }
                }
                
            }

            if (datastream == null) {
                throw new NoSuchRepresentationFileException(
                        "RepresentationFile " + fileID
                        + " from representation " + roPID
                        + " was not found.");
            } else {
                return getRepresentationFileFromDatastream(roPID,
                		datastream);
            }

        } catch (RemoteException e) {
            logger.debug("Error getting datastream " + fileID + " of object "
                    + roPID + " from Fedora - " + e.getMessage(), e);
            throw new BrowserException("Error getting datastream " + fileID
                    + " of object " + roPID + " from Fedora - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the {@link RepresentationPreservationObject} of a given
     * {@link RepresentationObject}.
     *
     * @param roPID the PID of the {@link RepresentationObject}.
     *
     * @return the {@link RepresentationPreservationObject} for the given RO PID
     * or <code>null</code> if the {@link RepresentationObject} doesn't have a
     * {@link RepresentationPreservationObject}.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     * @throws BrowserException
     */
    public RepresentationPreservationObject getROPreservationObject(String roPID)
            throws NoSuchRODAObjectException, BrowserException {

        try {

            SimpleRepresentationPreservationObject simpleRPO = this.fedoraRISearch
                    .getROPreservationObject(roPID);

            if (simpleRPO == null) {
                return null;
            } else {
                return getRepresentationPreservationObject(simpleRPO);
            }

        } catch (FedoraRISearchException e) {
            logger.debug("Error getting preservation object - "
                    + e.getMessage(), e);
            throw new BrowserException("Error getting preservation object - "
                    + e.getMessage(), e);
        } catch (Throwable e) {
            logger.debug("Error getting preservation object - "
                    + e.getMessage(), e);
            throw new BrowserException("Error getting preservation object - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the {@link SimpleRepresentationPreservationObject} with the given
     * PID.
     *
     * @param rpoPID the PID of the {@link RepresentationPreservationObject}.
     *
     * @return a {@link SimpleRepresentationPreservationObject} for the given
     * PID.
     *
     * @throws NoSuchRODAObjectException if the specified PID does not exist.
     * @throws BrowserException if something goes wrong with the operation.
     */
    public SimpleRepresentationPreservationObject getSimpleRepresentationPreservationObject(
            String rpoPID) throws NoSuchRODAObjectException, BrowserException {

        try {

            return this.fedoraRISearch
                    .getSimpleRepresentationPreservationObject(rpoPID);

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the number of {@link SimpleRepresentationPreservationObject}s
     * that match the given {@link Filter}.
     *
     * @param filter the {@link ContentAdapter}'s {@link Filter}.
     *
     * @return an <code>int</code> with the number of
     * {@link SimpleRepresentationPreservationObject}s that match the given
     * {@link Filter} .
     * @throws BrowserException
     */
    public int getSimpleRepresentationPreservationObjectCount(Filter filter)
            throws BrowserException {
        try {

            return this.fedoraRISearch
                    .getSimpleRepresentationPreservationObjectCount(filter);

        } catch (FedoraRISearchException e) {
            logger
                    .debug(
                    "Exception getting SimpleRepresentationPreservationObject count from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleRepresentationPreservationObject count from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the list of {@link SimpleRepresentationPreservationObject}s that
     * match the given {@link ContentAdapter}'s {@link Filter}.
     *
     * @param contentAdapter the {@link ContentAdapter}.
     *
     * @return a list of {@link SimpleRepresentationPreservationObject} that
     * match the given {@link ContentAdapter}.
     *
     * @throws BrowserException
     */
    public List<SimpleRepresentationPreservationObject> getSimpleRepresentationPreservationObjects(
            ContentAdapter contentAdapter) throws BrowserException {
        try {

            return this.fedoraRISearch
                    .getSimpleRepresentationPreservationObjects(contentAdapter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleRepresentationPreservationObjects from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleRepresentationPreservationObjects from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the specified {@link RepresentationPreservationObject}.
     *
     * @param poPID the PID of the {@link RepresentationPreservationObject}.
     *
     * @return the {@link RepresentationPreservationObject} with the given PID.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     *
     * @throws BrowserException
     */
    public RepresentationPreservationObject getRepresentationPreservationObject(
            String poPID) throws NoSuchRODAObjectException, BrowserException {

        try {

            return getRepresentationPreservationObject(this.fedoraRISearch
                    .getSimpleRepresentationPreservationObject(poPID));

        } catch (FedoraRISearchException e) {
            logger.error("Error getting preservation object - "
                    + e.getMessage(), e);
            throw new BrowserException("Error getting preservation object - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Returns the {@link SimpleRepresentationPreservationObject} with the given
     * PID.
     *
     * @param epoPID the PID of the {@link EventPreservationObject}.
     *
     * @return a {@link SimpleRepresentationPreservationObject} for the given
     * PID.
     *
     * @throws NoSuchRODAObjectException if the specified PID does not exist.
     * @throws BrowserException if something goes wrong with the operation.
     */
    public SimpleEventPreservationObject getSimpleEventPreservationObject(
            String epoPID) throws NoSuchRODAObjectException, BrowserException {

        try {

            return this.fedoraRISearch.getSimpleEventPreservationObject(epoPID);

        } catch (FedoraRISearchException e) {
            logger.error("Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception connecting to Fedora RI Search service - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the number of {@link SimpleEventPreservationObject}s that match
     * the given {@link Filter}.
     *
     * @param filter the {@link ContentAdapter}'s {@link Filter}.
     *
     * @return an <code>int</code> with the number of
     * {@link SimpleEventPreservationObject}s that match the given
     * {@link Filter} .
     * @throws BrowserException
     */
    public int getSimpleEventPreservationObjectCount(Filter filter)
            throws BrowserException {
        try {

            return this.fedoraRISearch
                    .getSimpleEventPreservationObjectCount(filter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleEventPreservationObject count from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleEventPreservationObject count from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the list of {@link SimpleEventPreservationObject}s that match the
     * given {@link ContentAdapter}'s {@link Filter}.
     *
     * @param contentAdapter the {@link ContentAdapter}.
     *
     * @return an array of {@link SimpleEventPreservationObject} that match the
     * given {@link ContentAdapter}.
     *
     * @throws BrowserException
     */
    public List<SimpleEventPreservationObject> getSimpleEventPreservationObjects(
            ContentAdapter contentAdapter) throws BrowserException {
        try {

            return this.fedoraRISearch
                    .getSimpleEventPreservationObjects(contentAdapter);

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting SimpleEventPreservationObjects from RI Search - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting SimpleEventPreservationObjects from RI Search - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the specified {@link EventPreservationObject}.
     *
     * @param epoPID the PID of the {@link EventPreservationObject}.
     *
     * @return the {@link EventPreservationObject} for the given RO PID.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     *
     * @throws BrowserException
     */
    public EventPreservationObject getEventPreservationObject(String epoPID)
            throws NoSuchRODAObjectException, BrowserException {

        try {

            return getEventPreservationObject(this.fedoraRISearch
                    .getSimpleEventPreservationObject(epoPID));

        } catch (FedoraRISearchException e) {
            logger.debug("Exception getting event preservation object - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting event preservation object - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets all the {@link EventPreservationObject}s for the the specified
     * {@link RepresentationPreservationObject}s PID.
     *
     * @param PID the PID of the {@link RODAObject}.
     *
     * @return a {@link List} of {@link EventPreservationObject}.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     * @throws BrowserException
     */
    public List<EventPreservationObject> getPreservationEventsPerformedOn(
            String PID) throws BrowserException, NoSuchRODAObjectException {

        List<EventPreservationObject> events = new ArrayList<EventPreservationObject>();

        Filter filter = new Filter();
        filter.add(new SimpleFilterParameter("targetPID", PID));

        List<SimpleEventPreservationObject> eventsPerformedOn = getSimpleEventPreservationObjects(new ContentAdapter(
                filter, null, null));

        for (SimpleEventPreservationObject simpleEPO : eventsPerformedOn) {
          
          // XXX this "if clause" was added because the ITQL query doesn't ensure that
          // only events associated to the provided targetPID are added to the result set
          // A re-implementation of how ITQL filters already existing parameters needs to be done
          // to solve this issue permanently.
          if(simpleEPO.getTargetPID().equals(PID)){
            events.add(getEventPreservationObject(simpleEPO));
          }
        }

        return events;
    }

    /**
     * Gets the specified {@link AgentPreservationObject}.
     *
     * @param apoPID the PID of the {@link AgentPreservationObject}.
     *
     * @return the {@link AgentPreservationObject} for the given RO PID.
     *
     * @throws NoSuchRODAObjectException if the specified object PID doesn't
     * exist.
     *
     * @throws BrowserException
     */
    public AgentPreservationObject getAgentPreservationObject(String apoPID)
            throws NoSuchRODAObjectException, BrowserException {

        try {

            return getAgentPreservationObject(this.fedoraRISearch
                    .getRODAObject(apoPID));

        } catch (FedoraRISearchException e) {
            logger.debug("Exception getting agent preservation object - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting agent preservation object - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the {@link AgentPreservationObject} with the given name.
     *
     * @param agentName the name of the agent.
     *
     * @return a {@link AgentPreservationObject} or <code>null</code> if an
     * agent couldn't be found with the given name.
     *
     * @throws BrowserException
     */
    public AgentPreservationObject getAgentPreservationObjectWithName(
            String agentName) throws BrowserException {

    	List<FilterParameter> parameters = new ArrayList<FilterParameter>();
    	parameters.add(new SimpleFilterParameter("label", agentName));
    	parameters.add(new SimpleFilterParameter("contentModel", "roda:p:"
                + AgentPreservationObject.TYPE));
        Filter filter = new Filter(parameters);

        try {
            List<RODAObject> agentObjects = this.fedoraRISearch
                    .getRODAObjects(new ContentAdapter(filter, null, null));

            AgentPreservationObject poAgent = null;

            if (agentObjects.size() == 0) {

                poAgent = null;

            } else {

                poAgent = getAgentPreservationObject(agentObjects.get(0));

                if (agentObjects.size() > 1) {
                    logger.warn("Found " + agentObjects.size()
                            + " agents with the name " + agentName
                            + ". Inform developers!!!");
                }

            }

            return poAgent;

        } catch (FedoraRISearchException e) {
            logger.debug(e.getMessage(), e);
            throw new BrowserException(e.getMessage(), e);
        }
    }

    /**
     * Gets the {@link RODAObjectPermissions} for the specified
     * {@link RODAObject}.
     *
     * @param pid the {@link RODAObject} PID.
     *
     * @return the {@link RODAObjectPermissions}.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public RODAObjectPermissions getRODAObjectPermissions(String pid)
            throws BrowserException, NoSuchRODAObjectException {

        @SuppressWarnings("unused")
        RODAObject rodaObject = getRODAObject(pid);

        try {

            InputStream policyInputStream = this.fedoraClientUtility
                    .getDatastream(pid, policyDatastreamID);

            RODAObjectPermissions objectPermissions = PolicyHelper.newInstance(
                    policyInputStream).getRODAObjectPermissions();

            policyInputStream.close();

            return removeFedoraAdminPermissions(objectPermissions);

        } catch (IOException e) {
            logger.debug("Exception getting POLICY stream - " + e.getMessage(),
                    e);
            throw new BrowserException("Exception getting POLICY stream - "
                    + e.getMessage(), e);
        } catch (PolicyMetadataException e) {
            logger.debug("Exception parsing POLICY - " + e.getMessage(), e);
            throw new BrowserException("Exception parsing POLICY - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Gets the {@link RODAObjectUserPermissions} for the current user over the
     * specified {@link RODAObject}.
     *
     * @param pid the {@link RODAObject} PID.
     * @param user
     *
     * @return the {@link RODAObjectUserPermissions}.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public RODAObjectUserPermissions getRODAObjectUserPermissions(String pid,
            User user) throws BrowserException, NoSuchRODAObjectException {

        RODAObjectPermissions permissions = getRODAObjectPermissions(pid);

        List<String> modifyUsers = Arrays.asList(permissions.getModifyUsers());
        List<String> modifyGroups = new ArrayList<String>(Arrays
                .asList(permissions.getModifyGroups()));
        // Intersect modifyGroups with all user groups
        modifyGroups.retainAll(Arrays.asList(user.getAllGroups()));

        List<String> removeUsers = Arrays.asList(permissions.getRemoveUsers());
        List<String> removeGroups = new ArrayList<String>(Arrays
                .asList(permissions.getRemoveGroups()));
        // Intersect removeGroups with all user groups
        removeGroups.retainAll(Arrays.asList(user.getAllGroups()));

        List<String> grantUsers = Arrays.asList(permissions.getGrantUsers());
        List<String> grantGroups = new ArrayList<String>(Arrays
                .asList(permissions.getGrantGroups()));
        // Intersect grantGroups with all user groups
        grantGroups.retainAll(Arrays.asList(user.getAllGroups()));

        RODAObjectUserPermissions userPermissions = new RODAObjectUserPermissions();
        userPermissions.setObjectPID(pid);
        userPermissions.setUsername(user.getName());
        userPermissions.setModify(modifyUsers.contains(user.getName())
                || modifyGroups.size() > 0);
        userPermissions.setRemove(removeUsers.contains(user.getName())
                || removeGroups.size() > 0);
        userPermissions.setGrant(grantUsers.contains(user.getName())
                || grantGroups.size() > 0);

        return userPermissions;
    }

    /**
     * Verifies if the specified user has modify permission over the specified
     * object.
     *
     * @param pid the PID of the {@link RODAObject}.
     * @param user
     *
     * @return <code>true</code> if the current user has modify permission and
     * <code>false</code> otherwise.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public boolean hasModifyPermission(String pid, User user)
            throws BrowserException, NoSuchRODAObjectException {

        RODAObjectPermissions permissions = getRODAObjectPermissions(pid);

        List<String> modifyUsers = Arrays.asList(permissions.getModifyUsers());
        List<String> modifyGroups = Arrays
                .asList(permissions.getModifyGroups());

        // Intersect modifyGroups with all user groups
        modifyGroups.retainAll(Arrays.asList(user.getAllGroups()));

        return modifyUsers.contains(user.getName()) || modifyGroups.size() > 0;
    }

    /**
     * Verifies if the specified user has remove permission over the specified
     * object.
     *
     * @param pid the PID of the {@link RODAObject}.
     * @param user
     *
     * @return <code>true</code> if the current user has remove permission and
     * <code>false</code> otherwise.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public boolean hasRemovePermission(String pid, User user)
            throws BrowserException, NoSuchRODAObjectException {

        RODAObjectPermissions permissions = getRODAObjectPermissions(pid);

        List<String> removeUsers = Arrays.asList(permissions.getRemoveUsers());
        List<String> removeGroups = Arrays
                .asList(permissions.getRemoveGroups());

        // Intersect removeGroups with all user groups
        removeGroups.retainAll(Arrays.asList(user.getAllGroups()));

        return removeUsers.contains(user.getName()) || removeGroups.size() > 0;
    }

    /**
     * Verifies of the specified user has grant permission over the specified
     * object.
     *
     * @param pid the PID of the {@link RODAObject}.
     * @param user
     *
     * @return <code>true</code> if the current user has grant permission and
     * <code>false</code> otherwise.
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public boolean hasGrantPermission(String pid, User user)
            throws BrowserException, NoSuchRODAObjectException {

        RODAObjectPermissions permissions = getRODAObjectPermissions(pid);

        List<String> grantUsers = Arrays.asList(permissions.getGrantUsers());
        List<String> grantGroups = Arrays.asList(permissions.getGrantGroups());

        // Intersect grantGroups with all user groups
        grantGroups.retainAll(Arrays.asList(user.getAllGroups()));

        return grantUsers.contains(user.getName()) || grantGroups.size() > 0;
    }

    /**
     * Gets the {@link Producers} for a Fonds.
     *
     * @param doPID the PID of the Fonds {@link DescriptionObject} or any of
     * it's descendants.
     *
     * @return the {@link Producers}.
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    public Producers getProducers(String doPID)
            throws NoSuchRODAObjectException, BrowserException {
        try {

            List<String> ancestorPIDs = this.fedoraClientUtility
                    .getFedoraRISearch().getDOAncestorPIDs(doPID);

            String fondsPID = ancestorPIDs.get(0);

            return this.fedoraRISearch.getDOProducers(fondsPID);

        } catch (FedoraRISearchException e) {
            logger.error("Exception getting producers properties from " + doPID
                    + " - " + e.getMessage(), e);
            throw new BrowserException(
                    "Exception getting producers properties from " + doPID
                    + " - " + e.getMessage(), e);
        }
    }

    /**
     * Sets the completeReference of a {@link DescriptionObject}.
     *
     * @param dObject the {@link DescriptionObject}
     *
     * @return the {@link DescriptionObject}
     *
     * @throws NoSuchRODAObjectException
     * @throws BrowserException
     */
    private DescriptionObject setCompleteReference(DescriptionObject dObject)
            throws NoSuchRODAObjectException, BrowserException {

        List<String> ancestorPIDs = getDOAncestorPIDs(dObject.getPid());
        String completeReference = null;

        if (ancestorPIDs.size() == 1) {

            // this is a fonds
            completeReference = dObject.getCountryCode() + "/"
                    + dObject.getRepositoryCode() + "/" + dObject.getId();

        } else {

            // Get countryCode and repositoryCode for the fonds element
            SimpleDescriptionObject sdoFonds = getSimpleDescriptionObject(ancestorPIDs
                    .get(0));

            completeReference = sdoFonds.getCountryCode() + "/"
                    + sdoFonds.getRepositoryCode();

            for (String ancestorPID : ancestorPIDs) {
                SimpleDescriptionObject sdo = getSimpleDescriptionObject(ancestorPID);

                if (completeReference != null) {
                    completeReference = completeReference + "/" + sdo.getId();
                } else {
                    completeReference = sdo.getId();
                }
            }
        }

        dObject.setCompleteReference(completeReference);

        return dObject;
    }

    private boolean isDefaultDatastream(String dsID) {
        return representationObjectDefaultDatastreams.contains(dsID);
    }

    /**
     * Gets the {@link RepresentationObject} of a given
     * {@link DescriptionObject} and with the given status.
     *
     * @param doPID the PID of the {@link DescriptionObject}.
     * @param status
     *
     * @return the {@link RepresentationObject} for the given DO PID with the
     * given status or <code>null</code> if the given {@link DescriptionObject}
     * doesn't contain a {@link RepresentationObject} with the given status.
     *
     * @throws NoSuchRODAObjectException the the specified <code>doPID</code>
     * doesn't exist.
     * @throws BrowserException
     */
    private RepresentationObject getDORepresentation(String doPID, String status)
            throws BrowserException, NoSuchRODAObjectException {

        SimpleRepresentationObject simpleRO = getDOSimpleRepresentation(doPID,
                status);

        if (simpleRO != null) {
            return getRepresentationObject(simpleRO);
        } else {
            return null;
        }

    }

    /**
     * Gets the {@link SimpleRepresentationObject} of a given
     * {@link DescriptionObject} and with the given status.
     *
     * @param doPID the PID of the {@link DescriptionObject}.
     * @param status
     *
     * @return the {@link SimpleRepresentationObject} for the given DO PID with
     * the given status or <code>null</code> if the given
     * {@link DescriptionObject} doesn't contain a {@link RepresentationObject}
     * with the given status.
     *
     * @throws NoSuchRODAObjectException the the specified <code>doPID</code>
     * doesn't exist.
     * @throws BrowserException
     */
    private SimpleRepresentationObject getDOSimpleRepresentation(String doPID,
            String status) throws BrowserException, NoSuchRODAObjectException {
        try {

            Filter filter = new Filter(new SimpleFilterParameter(
                "descriptionObjectPID", doPID));

            if (status != null) {
                filter.add(new SimpleFilterParameter("statuses", status));
            }

            List<SimpleRepresentationObject> simpleROs = this.fedoraRISearch
                    .getSimpleRepresentationObjects(new ContentAdapter(filter,
                    null, null));

            SimpleRepresentationObject simpleRO = null;

            if (simpleROs != null && simpleROs.size() > 0) {
                simpleRO = simpleROs.get(0);

                if (simpleROs.size() > 1) {
                    logger.warn("Description object " + doPID + " has "
                            + simpleROs.size()
                            + " representations with status '" + status + "'");
                }
            }

            return simpleRO;

        } catch (FedoraRISearchException e) {
            logger.debug(
                    "Exception getting representation - " + e.getMessage(), e);
            throw new BrowserException("Exception getting representation - "
                    + e.getMessage(), e);
        }
    }

    private RepresentationObject getRepresentationObject(
            SimpleRepresentationObject simpleRO) throws BrowserException {
        try {

            List<RepresentationFile> repFiles = new ArrayList<RepresentationFile>();

            Datastream[] datastreams = this.fedoraClientUtility.getAPIM()
                    .getDatastreams(simpleRO.getPid(), null, fedoraClientUtility.getStateCode(DatastreamState.Active));

            for (Datastream datastream : datastreams) {

                if (!isDefaultDatastream(datastream.getID())) {
                    RepresentationFile rFile = getRepresentationFileFromDatastream(
                            simpleRO.getPid(), datastream);
                    repFiles.add(rFile);

                } else {
                    // It's one of the default datastreams DC, RELS-EXT, POLICY,
                    // etc
                }

            }

            Collections.sort(repFiles, new Comparator<RepresentationFile>() {
                /**
                 * Compares {@link RepresentationFile}s comparing their IDs.
                 */
                public int compare(RepresentationFile r1, RepresentationFile r2) {
                    return r1.getId().compareTo(r2.getId());
                }
            });

            RepresentationFile rootFile = repFiles.get(0);
            repFiles.remove(0);
            RepresentationFile[] partFiles = repFiles
                    .toArray(new RepresentationFile[repFiles.size()]);

            return new RepresentationObject(simpleRO, rootFile, partFiles);

        } catch (RemoteException e) {
            logger.error("Error getting datastreams for " + simpleRO.getPid()
                    + " from Fedora - " + e.getMessage(), e);
            throw new BrowserException("Error getting datastreams for "
                    + simpleRO.getPid() + " from Fedora - " + e.getMessage(), e);
        }
    }

    private RepresentationFile getRepresentationFileFromDatastreamDef(
            String roPID, DatastreamDef datastreamDef) {
        // String accessURL = String.format("/Access?pid=%1$s&id=%2$s", roPID,
        // datastreamDef.getID());
        String accessURL = String.format("/get/%1$s/%2$s", roPID, datastreamDef
                .getID());

        FileFormat fileFormat = null;
        File tempFile = null;
        try {
            String fileUrl = this.fedoraClientUtility.getDatastreamURL(roPID, datastreamDef.getID());
            tempFile = File.createTempFile(String.valueOf(UUID.randomUUID()), ".fits_tmp");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            this.fedoraClientUtility.getDownloader().get(fileUrl, outputStream);
            outputStream.flush();
            outputStream.close();
            fileFormat = FormatUtility.getFileFormat(tempFile, datastreamDef.getLabel());
        } catch (IOException ex) {
            logger.error("Cannot get roda object from fedora repository. RODA PID=\"" + roPID + "\"", ex);
        } finally {
            if ((tempFile != null) && (tempFile.exists())) {
                tempFile.delete();
            }
        }
        if (fileFormat
                == null) {
            fileFormat = new FileFormat();
            fileFormat.setMimetype(datastreamDef.getMIMEType());
        }

        // TODO: find out the size of a datastream                
        return new RepresentationFile(datastreamDef.getID(), datastreamDef.getLabel(), 0, accessURL, fileFormat);
    }

    private RepresentationFile getRepresentationFileFromDatastream(
            String roPID, Datastream datastream) {

        // String accessURL = String.format("/Access?pid=%1$s&id=%2$s", roPID,
        // datastream.getID());
        String accessURL = String.format("/get/%1$s/%2$s", roPID, datastream
                .getID());

        FileFormat fileFormat = new FileFormat();
        fileFormat.setMimetype(datastream.getMIMEType());
        fileFormat.setPuid(datastream.getFormatURI());
        return new RepresentationFile(datastream.getID(), datastream.getLabel(), datastream.getSize(), accessURL, fileFormat);
    }

    protected RepresentationPreservationObject getRepresentationPreservationObject(
            SimpleRepresentationPreservationObject simleRPO)
            throws BrowserException {

        try {

            // Get PREMIS for the Representation

            logger.trace("Getting PREMIS datastream " + simleRPO.getPid() + "/"
                    + preservationObjectDatastreamID);

            InputStream dsPREMIS = this.fedoraClientUtility.getDatastream(
                    simleRPO.getPid(), preservationObjectDatastreamID);

            logger.trace("PREMIS datastream InputStream has "
                    + dsPREMIS.available() + " bytes available");

            PremisRepresentationObjectHelper premisObjectHelper = PremisRepresentationObjectHelper
                    .newInstance(dsPREMIS);
            RepresentationPreservationObject rpObject = premisObjectHelper
                    .getRepresentationPreservationObject(simleRPO);

            /*
             * The root and part files inside 'rpObject' only have the IDs. Now
             * we need to get the rest of the information from the other PREMIS
             * datastreams.
             */

            // Get PREMIS for the root file
            if (rpObject.getRootFile() != null) {
                rpObject.setRootFile(getFilePreservationObject(simleRPO
                        .getPid(), rpObject.getRootFile().getID()));
            }

            // Get PREMIS for the part files
            if (rpObject.getPartFiles() != null) {
                RepresentationFilePreservationObject[] partFiles = rpObject
                        .getPartFiles();

                RepresentationFilePreservationObject[] fullRepFiles = new RepresentationFilePreservationObject[partFiles.length];

                for (int i = 0; i < partFiles.length; i++) {
                    fullRepFiles[i] = getFilePreservationObject(simleRPO
                            .getPid(), partFiles[i].getID());
                }

                rpObject.setPartFiles(fullRepFiles);
            }

            // Get the SimpleEventPreservationObject of the events related with
            // this RepresentationPreservationObject
            Filter filter = new Filter();
            filter
                    .add(new SimpleFilterParameter("targetPID", simleRPO
                    .getPid()));
            List<SimpleEventPreservationObject> eventObjects = getSimpleEventPreservationObjects(new ContentAdapter(
                    filter, null, null));

            String[] eventPIDs = new String[eventObjects.size()];
            for (int i = 0; i < eventObjects.size(); i++) {
                eventPIDs[i] = eventObjects.get(i).getPid();
            }

            rpObject.setPreservationEventIDs(eventPIDs);

            return rpObject;

        } catch (IOException e) {
            logger.debug("Error getting preservation object info - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Error getting preservation object info - "
                    + e.getMessage(), e);
        } catch (PremisMetadataException e) {
            logger.debug("Error getting preservation object info - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Error getting preservation object info - "
                    + e.getMessage(), e);
        }

    }

    private RepresentationFilePreservationObject getFilePreservationObject(
            String PID, String fileID) throws IOException,
            PremisMetadataException {

        InputStream rootFilePREMIS = this.fedoraClientUtility.getDatastream(
                PID, fileID);

        PremisFileObjectHelper fileObjectHelper = PremisFileObjectHelper
                .newInstance(rootFilePREMIS);

        return fileObjectHelper.getRepresentationFilePreservationObject();
    }

    private EventPreservationObject getEventPreservationObject(
            SimpleEventPreservationObject simpleEPO) throws BrowserException {

        try {

            // Get PREMIS for the Event

            logger.trace("Getting PREMIS datastream " + simpleEPO.getPid()
                    + "/" + preservationObjectDatastreamID);

            InputStream dsPREMIS = this.fedoraClientUtility.getDatastream(
                    simpleEPO.getPid(), preservationObjectDatastreamID);

            PremisEventHelper premisObjectHelper = PremisEventHelper
                    .newInstance(dsPREMIS);

            return premisObjectHelper.getEventPreservationObject(simpleEPO);

        } catch (IOException e) {
            logger.debug("Error getting preservation object info - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Error getting preservation object info - "
                    + e.getMessage(), e);
        } catch (PremisMetadataException e) {
            logger.debug("Error getting preservation object info - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Error getting preservation object info - "
                    + e.getMessage(), e);
        }
    }

    private AgentPreservationObject getAgentPreservationObject(
            RODAObject pObject) throws BrowserException {

        try {

            // Get PREMIS for the Agent

            logger.trace("Getting PREMIS datastream " + pObject.getPid() + "/"
                    + preservationObjectDatastreamID);

            InputStream dsPREMIS = this.fedoraClientUtility.getDatastream(
                    pObject.getPid(), preservationObjectDatastreamID);

            PremisAgentHelper premisObjectHelper = PremisAgentHelper
                    .newInstance(dsPREMIS);

            return premisObjectHelper.getAgentPreservationObject(pObject);

        } catch (IOException e) {
            logger.debug("Error getting preservation object info - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Error getting preservation object info - "
                    + e.getMessage(), e);
        } catch (PremisMetadataException e) {
            logger.debug("Error getting preservation object info - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Error getting preservation object info - "
                    + e.getMessage(), e);
        }
    }

    private RODAObjectPermissions removeFedoraAdminPermissions(
            RODAObjectPermissions permissions) {

        permissions.removeReadUser(fedoraAdminUsername);
        permissions.removeModifyUser(fedoraAdminUsername);
        permissions.removeRemoveUser(fedoraAdminUsername);
        permissions.removeGrantUser(fedoraAdminUsername);

        return permissions;
    }

    private Filter fillProducerFilterParameters(Filter filter)
            throws BrowserException {
        try {

            if (filter != null) {

                for (FilterParameter parameter : filter.getParameters()) {

                    if (parameter instanceof ProducerFilterParameter) {
                        ProducerFilterParameter producerParameter = (ProducerFilterParameter) parameter;

                        User producerUser;
                        if (StringUtils
                                .isBlank(producerParameter.getUsername())) {
                            // No username supplied, assuming it's the current
                            // user
                            producerUser = this.user;
                        } else {
                            // Using username supplied
                            producerUser = new UserBrowser()
                                    .getUser(producerParameter.getUsername());
                        }

                        producerParameter.setUsername(producerUser.getName());
                        producerParameter
                                .setGroups(producerUser.getAllGroups());

                    }
                }

            }

            return filter;

        } catch (UserManagementException e) {
            logger.debug("Exception accessing producer user information - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception accessing producer user information - "
                    + e.getMessage(), e);
        } catch (RODAServiceException e) {
            logger.debug("Exception accessing producer user information - "
                    + e.getMessage(), e);
            throw new BrowserException(
                    "Exception accessing producer user information - "
                    + e.getMessage(), e);
        }
    }

    /**
     * Returns the handle URL for the given PID.
     *
     * @param doPID the Description Object PID.
     *
     * @return a {@link String} with the handle URL for the given PID.
     */
    private String getHandleURLForPID(String doPID) {
        String[] pidParts = doPID.split(":");
        return this.rodaNAHandleURL + "/" + pidParts[1];
    }
}
