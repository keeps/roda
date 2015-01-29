package pt.gov.dgarq.roda.core.fedora;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerException;

import noNamespace.PidListDocument;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.common.Pair;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Producers;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.fedora.gsearch.FedoraGSearch;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearch;
import pt.gov.dgarq.roda.core.fedora.risearch.FedoraRISearchException;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.util.StreamUtility;
import pt.gov.dgarq.roda.util.XsltUtility;
import fedora.client.Downloader;
import fedora.client.FedoraClient;
import fedora.client.HttpInputStream;
import fedora.client.Uploader;
import fedora.server.access.FedoraAPIA;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import fedora.server.types.gen.MIMETypedStream;
import fedora.server.types.gen.ObjectProfile;

/**
 * @author Rui Castro
 */
public class FedoraClientUtility extends FedoraClient {
	static final private Logger logger = Logger
			.getLogger(FedoraClientUtility.class);

	private static final String resourceXsltCreateRodaObject = "/create-roda-object.xslt";
	private static final String resourceXsltCreateRodaDescriptionObject = "/create-roda-description-object.xslt";
	private static final String resourceEmptyFoxml = "/empty.foxml";
	private static String relsExtDatastreamID;

	/**
	 * Enumeration of the possible states of a Fedora datastream.
	 * 
	 * @author Rui Castro
	 */
	public enum DatastreamState {
		Active, Inactive, Deleted
	};

	private URL fedoraURL = null;
	private URL fedoraUploadURL = null;

	private CASUserPrincipal cup = null;
	private CASUtility casUtility = null;

	private FedoraAPIA fedoraAPIA = null;
	private FedoraAPIM fedoraAPIM = null;

	private FedoraRISearch fedoraRISearch = null;
	private FedoraGSearch fedoraGSearch = null;

	private Uploader uploader = null;
	private Downloader downloader = null;

	/**
	 * Constructs a new instance of {@link FedoraClientUtility}.
	 * 
	 * @param fedoraURL
	 * @param fedoraGSearchURL
	 * @param user
	 * @param password
	 * 
	 * @throws FedoraClientException
	 * @throws MalformedURLException
	 */
	public FedoraClientUtility(String fedoraURL, String fedoraGSearchURL,
			CASUserPrincipal cup, CASUtility casUtility)
			throws FedoraClientException, MalformedURLException {

		super(fedoraURL, cup.getName(), cup.getProxyGrantingTicket());
		try {
			this.fedoraURL = new URL(fedoraURL);
			this.cup = cup;
			this.casUtility = casUtility;
			this.fedoraAPIA = super.getAPIA();
			this.fedoraAPIM = super.getAPIM();

			this.fedoraUploadURL = new URL(getUploadURL());

			this.fedoraRISearch = new FedoraRISearch(this, cup);
			this.fedoraGSearch = new FedoraGSearch(new URL(fedoraGSearchURL),
					cup);

			this.uploader = new Uploader(this.fedoraURL.getProtocol(),
					this.fedoraURL.getHost(), this.fedoraURL.getPort(),
					cup.getName(), cup.getProxyGrantingTicket());

			this.downloader = new Downloader(this.fedoraURL.getHost(),
					this.fedoraURL.getPort(), cup.getName(),
					cup.getProxyGrantingTicket());

			logger.trace(String.format("%4$s (%1$s ; %2$s ; %3$s) init OK",
					fedoraURL, cup.getName(), "*******", getClass()
							.getSimpleName()));

			if (relsExtDatastreamID == null) {

				PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
				propertiesConfiguration.setDelimiterParsingDisabled(true);

				propertiesConfiguration.load(getClass().getResource(
						"/roda-fedorarisearch.properties"));

				relsExtDatastreamID = propertiesConfiguration
						.getString("relsExtDatastreamID");
			}

		} catch (Exception e) {
			logger.debug(
					"Error initializing Fedora client - " + e.getMessage(), e);
			throw new FedoraClientException(
					"Error initializing Fedora client - " + e.getMessage(), e);
		}
	}

	/**
	 * @return the fedoraRISearch
	 * 
	 * @throws FedoraRISearchException
	 *             if an error occurs creating the new {@link FedoraRISearch}
	 *             instance.
	 */
	public FedoraRISearch getFedoraRISearchNewInstance()
			throws FedoraRISearchException {
		return new FedoraRISearch(this, cup);
	}

	/**
	 * @see FedoraClient#getAPIA()
	 */
	@Override
	public FedoraAPIA getAPIA() {
		return this.fedoraAPIA;
	}

	/**
	 * @see FedoraClient#getAPIM()
	 */
	@Override
	public FedoraAPIM getAPIM() {
		return this.fedoraAPIM;
	}

	/**
	 * @return the fedoraRISearch
	 */
	public FedoraRISearch getFedoraRISearch() {
		return this.fedoraRISearch;
	}

	/**
	 * @return the fedoraGSearch
	 */
	public FedoraGSearch getFedoraGSearch() {
		return fedoraGSearch;
	}

	/**
	 * @return the Fedora Uploader
	 */
	public Uploader getUploader() {
		return uploader;
	}

	/**
	 * @return the Fedora Downloader
	 */
	public Downloader getDownloader() {
		return downloader;
	}

	/**
	 * Returns the Fedora URI for the given PID.
	 * <p>
	 * Ex: for PID roda:123 it returns info:fedora/roda:123
	 * </p>
	 * 
	 * @param pid
	 *            the PID.
	 * 
	 * @return a {@link String} with the Fedora URI.
	 */
	public String getFedoraObjectURIFromPID(String pid) {
		if (pid == null) {
			return FedoraClient.FEDORA_URI_PREFIX;
		} else {
			return String.format("%1$s%2$s", FedoraClient.FEDORA_URI_PREFIX,
					pid);
		}
	}

	/**
	 * Returns the Fedora URI for a given PID datastream.
	 * <p>
	 * Ex: for PID roda:123 and datastream ID "EAD-C" it returns
	 * info:fedora/roda:123/EAD-C
	 * </p>
	 * 
	 * @param doPID
	 *            the PID.
	 * @param dsID
	 *            the datastream ID.
	 * @return a {@link String} with the URI.
	 */
	public String getDatastreamURI(String doPID, String dsID) {
		return String.format("%1$s/%2$s", getFedoraObjectURIFromPID(doPID),
				dsID);
	}

	/**
	 * Returns the Fedora URL for a given PID datastream.
	 * <p>
	 * Ex: for PID roda:123 and datastream ID "EAD-C" and supposing fedoraURL is
	 * http://localhost:8080/fedora it returns
	 * http://localhost:8080/fedora/get/roda:123/EAD-C
	 * </p>
	 * 
	 * @param doPID
	 *            the PID.
	 * @param dsID
	 *            the datastream ID.
	 * 
	 * @return a {@link String} with the URL.
	 */
	public String getDatastreamURL(String doPID, String dsID) {
		return String.format("%1$s/get/%2$s/%3$s", this.fedoraURL, doPID, dsID);
	}

	/**
	 * Returns an {@link InputStream} for a given datastream ID.
	 * 
	 * @param pid
	 *            the PID.
	 * @param dsID
	 *            the datastream ID.
	 * 
	 * @return an {@link InputStream}.
	 * 
	 * @throws IOException
	 */
	public InputStream getDatastream(String pid, String dsID)
			throws IOException {
		return get(getDatastreamURI(pid, dsID), true);
	}

	/**
	 * Creates the given {@link RODAObject} inside Fedora.
	 * 
	 * @param rObject
	 * 
	 * @return the PID of the newly created object.
	 * 
	 * @throws FedoraClientException
	 */
	public String createObject(RODAObject rObject) throws FedoraClientException {

		InputStream xsltStream = getClass().getResourceAsStream(
				resourceXsltCreateRodaObject);

		InputStream foxmlStream = getClass().getResourceAsStream(
				resourceEmptyFoxml);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			Map<String, Object> parameters = XsltUtility.createParameters(
					"contentModel", rObject.getContentModel());
			parameters.put("label", rObject.getLabel());

			if (RODAObject.STATE_ACTIVE.equalsIgnoreCase(rObject.getState())) {

				parameters.put("state", "Active");

			} else if (RODAObject.STATE_INACTIVE.equalsIgnoreCase(rObject
					.getState())) {

				parameters.put("state", "Inactive");

			} else if (RODAObject.STATE_DELETED.equalsIgnoreCase(rObject
					.getState())) {

				parameters.put("state", "Deleted");

			}

			XsltUtility.applyTransformation(xsltStream, parameters,
					foxmlStream, baos);

			byte[] template_foxml_data = baos.toByteArray();

			String pid = getAPIM().ingest(template_foxml_data, "foxml1.0",
					"Created by RODA Core");

			logger.info("Created object " + pid + ", contentModel="
					+ rObject.getContentModel() + ", label="
					+ rObject.getLabel() + ", state=" + rObject.getState());

			return pid;

		} catch (Exception e) {
			logger.debug("Error creating object - " + e.getMessage(), e);
			throw new FedoraClientException("Error creating object - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Creates the given {@link RODAObject} inside Fedora.
	 * 
	 * @param rObject
	 * 
	 * @return the PID of the newly created object.
	 * 
	 * @throws FedoraClientException
	 */
	public String createDescriptionObject(RODAObject rObject, String rdf,
			String policy, String eadc) throws FedoraClientException {

		return createDescriptionObject(rObject, rdf, policy, eadc, null);

	}

	/**
	 * Creates the given {@link RODAObject} inside Fedora.
	 * 
	 * @param rObject
	 * 
	 * @return the PID of the newly created object.
	 * 
	 * @throws FedoraClientException
	 */
	// FIXME revise javadocs
	public String createDescriptionObject(RODAObject rObject, String rdf,
			String policy, String eadc, String otherMetadata) throws FedoraClientException {

		InputStream xsltStream = getClass().getResourceAsStream(
				resourceXsltCreateRodaDescriptionObject);

		InputStream foxmlStream = getClass().getResourceAsStream(
				resourceEmptyFoxml);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {

			Map<String, Object> parameters = XsltUtility.createParameters(
					"PID", rObject.getPid());
			parameters.put("contentModel", rObject.getContentModel());
			parameters.put("label", rObject.getLabel());
			parameters.put("insertOtherMetadata", (otherMetadata != null
					&& otherMetadata.length() > 0 ? "true" : ""));

			if (RODAObject.STATE_ACTIVE.equalsIgnoreCase(rObject.getState())) {

				parameters.put("state", "Active");

			} else if (RODAObject.STATE_INACTIVE.equalsIgnoreCase(rObject
					.getState())) {

				parameters.put("state", "Inactive");

			} else if (RODAObject.STATE_DELETED.equalsIgnoreCase(rObject
					.getState())) {

				parameters.put("state", "Deleted");

			}

			XsltUtility.applyTransformation(xsltStream, parameters,
					foxmlStream, baos);

			String foxmlAsString = StreamUtility
					.inputStreamToString(new ByteArrayInputStream(baos
							.toByteArray()));

			foxmlAsString = foxmlAsString.replaceFirst("RDF_XML_PLACEHOLDER",
					rdf);
			foxmlAsString = foxmlAsString.replaceFirst(
					"POLICY_XML_PLACEHOLDER", policy);
			foxmlAsString = foxmlAsString.replaceFirst("EADC_XML_PLACEHOLDER",
					eadc);
			if (otherMetadata != null && otherMetadata.length() > 0) {
				foxmlAsString = foxmlAsString.replaceFirst(
						"OTHER_METADATA_XML_PLACEHOLDER", otherMetadata);
			}

			logger.debug("FOXML is\n" + foxmlAsString);

			byte[] template_foxml_data = foxmlAsString.getBytes();

			String pid = getAPIM().ingest(template_foxml_data, "foxml1.0",
					"Created by RODA Core");

			logger.info("Created object " + pid + ", contentModel="
					+ rObject.getContentModel() + ", label="
					+ rObject.getLabel() + ", state=" + rObject.getState());

			return pid;

		} catch (Exception e) {
			logger.debug("Error creating object - " + e.getMessage(), e);
			throw new FedoraClientException("Error creating object - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Adds a datastream to a Fedora object.
	 * 
	 * @param pid
	 * @param dsID
	 * @param altIDs
	 * @param dsLabel
	 * @param versionable
	 * @param MIMEType
	 * @param formatURI
	 * @param dsLocation
	 * @param controlGroup
	 * @param dsState
	 * @param checksumType
	 * @param checksum
	 * @param logMessage
	 * 
	 * @return the datastreamID of the newly added datastream.
	 * 
	 * @throws FedoraClientException
	 */
	public String addDatastream(String pid, String dsID, String[] altIDs,
			String dsLabel, boolean versionable, String MIMEType,
			String formatURI, String dsLocation, String controlGroup,
			DatastreamState dsState, String checksumType, String checksum,
			String logMessage) throws FedoraClientException {

		try {

			return getAPIM().addDatastream(pid, dsID, altIDs, dsLabel,
					versionable, MIMEType, formatURI, dsLocation, controlGroup,
					getStateCode(dsState), checksumType, checksum, logMessage);

		} catch (RemoteException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * Adds a datastream to a Fedora object.
	 * 
	 * @param pid
	 * @param dsID
	 * @param dsLabel
	 * @param versionable
	 * @param MIMEType
	 * @param dsLocation
	 * @param dsState
	 * @param logMessage
	 * 
	 * @return the datastreamID of the newly added datastream.
	 * 
	 * @throws FedoraClientException
	 */
	public String addDatastream(String pid, String dsID, String dsLabel,
			boolean versionable, String MIMEType, String dsLocation,
			DatastreamState dsState, String logMessage)
			throws FedoraClientException {

		try {

			return getAPIM().addDatastream(pid, dsID, new String[0], dsLabel,
					versionable, MIMEType, null, dsLocation, "M",
					getStateCode(dsState), null, null, logMessage);

		} catch (RemoteException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * Purges an object from the repository.
	 * 
	 * @param pid
	 *            the pid of the object to purge.
	 * 
	 * @throws FedoraClientException
	 */
	public void purgeObject(String pid) throws FedoraClientException {
		try {

			getAPIM().purgeObject(pid, "Purged by RODA Services", false);

		} catch (RemoteException e) {
			logger.debug("Exception creating object - " + e.getMessage(), e);
			throw new FedoraClientException(
					"Exception purging object from Fedora - " + e.getMessage(),
					e);
		}
	}

	/**
	 * Purges objects from the repository.
	 * 
	 * @param PIDs
	 *            the PIDs of the objects to purge.
	 * @param stopAtError
	 *            if is <code>true</code> this operation will stop at first
	 *            error encountered, if it is <code>false</code> it will
	 *            continue purging even if an error occurs.
	 * 
	 * @return a {@link List} with the successfully purged PIDs.
	 * 
	 * @throws FedoraClientException
	 *             if <code>stopAtError</code> is <code>true</code> and an
	 *             {@link Exception} occurs.
	 */
	public List<String> purgeObjects(List<String> PIDs, boolean stopAtError)
			throws FedoraClientException {

		List<String> purgedPIDs = new ArrayList<String>();

		for (String pid : PIDs) {

			try {

				purgeObject(pid);
				purgedPIDs.add(pid);

			} catch (FedoraClientException e) {
				if (stopAtError) {
					throw e;
				}
			}
		}

		return purgedPIDs;
	}

	/**
	 * Returns the next PID for a new Fedora object.
	 * 
	 * @return the next PID for a new Fedora object.
	 * 
	 * @throws FedoraClientException
	 */
	public String getNextPID() throws FedoraClientException {

		String url = this.fedoraURL
				+ "/management/getNextPID?xml=true&namespace=roda&numPIDs=1";

		try {

			HttpInputStream inputStream = get(url, true);

			PidListDocument pidListDocument = PidListDocument.Factory
					.parse(inputStream);
			List<String> pidList = pidListDocument.getPidList().getPidList();
			inputStream.close();

			if (pidList != null && pidList.size() > 0) {

				return pidList.get(0);

			} else {
				throw new FedoraClientException("PidList is empty");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new FedoraClientException("Error getting Fedora next PID - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * @param parentPID
	 * @param childPID
	 * @throws FedoraClientException
	 */
	public void addRepresentedByRelationship(String parentPID, String childPID)
			throws FedoraClientException {
		try {

			addRDFResourceProperty(parentPID,
					FedoraRISearch.RDF_TAG_REPRESENTED_BY,
					getFedoraObjectURIFromPID(childPID));

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * @param roPID
	 * @param poPID
	 * @throws FedoraClientException
	 */
	// public void addPreservedByRelationship(String roPID, String poPID)
	// throws FedoraClientException {
	// try {
	//
	// addRDFResourceProperty(roPID, FedoraRISearch.RDF_TAG_PRESERVED_BY,
	// getFedoraObjectURIFromPID(poPID));
	//
	// } catch (Exception e) {
	// logger.debug(e.getMessage(), e);
	// throw new FedoraClientException(e.getMessage(), e);
	// }
	// }
	/**
	 * @param poPID
	 *            the PID of {@link RepresentationPreservationObject}.
	 * @param roPID
	 *            the PID of {@link RepresentationObject}.
	 * 
	 * @throws FedoraClientException
	 */
	public void addPreservationOfRelationship(String poPID, String roPID)
			throws FedoraClientException {
		try {

			addRDFResourceProperty(poPID,
					FedoraRISearch.RDF_TAG_PRESERVATION_OF,
					getFedoraObjectURIFromPID(roPID));

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * @param poPID
	 * @param objectPIDs
	 * @throws FedoraClientException
	 */
	public void addPerformedOnRelationships(String poPID, String[] objectPIDs)
			throws FedoraClientException {

		String[] resources = new String[objectPIDs.length];
		for (int i = 0; i < objectPIDs.length; i++) {
			resources[i] = getFedoraObjectURIFromPID(objectPIDs[i]);
		}

		try {

			addRDFResourceProperties(poPID,
					FedoraRISearch.RDF_TAG_PERFORMED_ON, resources);

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * @param poPID
	 * @param agentPID
	 * @throws FedoraClientException
	 */
	public void addPerformedByRelationship(String poPID, String agentPID)
			throws FedoraClientException {
		try {

			addRDFResourceProperty(poPID, FedoraRISearch.RDF_TAG_PERFORMED_BY,
					getFedoraObjectURIFromPID(agentPID));

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * @param rpoPID
	 * @param eventPID
	 * @throws FedoraClientException
	 */
	public void addDerivedFromRelationship(String rpoPID, String eventPID)
			throws FedoraClientException {
		try {

			addRDFResourceProperty(rpoPID, FedoraRISearch.RDF_TAG_DERIVED_FROM,
					getFedoraObjectURIFromPID(eventPID));

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * @param roPID
	 * @param statuses
	 * @param type
	 * @param subType
	 * @throws FedoraClientException
	 */
	public void setRepresentationProperties(String roPID, String[] statuses,
			String type, String subType) throws FedoraClientException {

		// setRepresentationStatuses(roPID, statuses);

		logger.trace("setRepresentationProperties(statuses="
				+ Arrays.toString(statuses) + ", type=" + type + ", subType="
				+ subType + ")");

		Map<String, String[]> properties = new HashMap<String, String[]>();
		// predicate -> object
		properties.put(FedoraRISearch.RDF_TAG_REPRESENTATION_STATUS, statuses);
		properties.put(FedoraRISearch.RDF_TAG_REPRESENTATION_TYPE,
				new String[] { type });
		properties.put(FedoraRISearch.RDF_TAG_REPRESENTATION_SUBTYPE,
				new String[] { subType });

		try {

			replaceRDFValueProperties(roPID, properties);

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * Sets the {@link DescriptionLevel} property in a {@link DescriptionObject}
	 * .
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * @param dLevel
	 *            the {@link DescriptionLevel}.
	 * 
	 * @throws FedoraClientException
	 */
	public void setDODescriptionLevel(String doPID, DescriptionLevel dLevel)
			throws FedoraClientException {
		try {

			modifyRDFValueSingleProperty(doPID,
					FedoraRISearch.RDF_TAG_DESCRIPTION_LEVEL, dLevel.getLevel());

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * @param subjectPID
	 * @param predicate
	 * @param objectPID
	 * @throws FedoraClientException
	 */
	public void addRelationship(String subjectPID, String predicate,
			String objectPID) throws FedoraClientException {
		try {

			addRDFResourceProperty(subjectPID, predicate,
					getFedoraObjectURIFromPID(objectPID));

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param file
	 *            the {@link File} to upload.
	 * @return a {@link String} with the temporary id which can then be passed
	 *         to API-M requests as a URL. It will look like uploaded://123
	 * @throws FedoraClientException
	 */
	public String temporaryUpload(File file) throws FedoraClientException {

		try {

			return uploadFile(file);

		} catch (IOException e) {
			throw new FedoraClientException("Exception uploading file - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param data
	 * @return a {@link String} with the temporary id which can then be passed
	 *         to API-M requests as a URL. It will look like uploaded://123
	 * 
	 * @throws FedoraClientException
	 */
	public String temporaryUpload(byte[] data) throws FedoraClientException {
		return temporaryUpload(new ByteArrayPartSource("file", data));
	}

	/**
	 * 
	 * @param file
	 * @return a {@link String} with the temporary id which can then be passed
	 *         to API-M requests as a URL. It will look like uploaded://123
	 * 
	 * @throws FedoraClientException
	 */
	public String temporaryUpload(PartSource file) throws FedoraClientException {

		logger.debug("Uploading file '" + file.getFileName() + "' ("
				+ file.getLength() + " bytes)" + " to "
				+ getFedoraUploadURL().toString());

		PostMethod postMethod = new PostMethod(getFedoraUploadURL().toString());
		// postMethod.setFollowRedirects(true);

		Part[] parts = { new FilePart("file", file) };

		postMethod.setRequestEntity(new MultipartRequestEntity(parts,
				postMethod.getParams()));

		HttpClient client = new HttpClient();

		String username = this.cup.getName();
		String password = this.cup.getProxyGrantingTicket();
		Credentials credentials = new UsernamePasswordCredentials(username,
				password);

		client.getState().setCredentials(
				new AuthScope(getFedoraUploadURL().getHost(),
						getFedoraUploadURL().getPort()), credentials);

		try {

			int status = client.executeMethod(postMethod);

			if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {

				String uploadedFileURL = postMethod.getResponseBodyAsString()
						.trim();

				postMethod.releaseConnection();

				logger.info("Upload successful: " + status + " - "
						+ postMethod.getStatusText() + " - Response: "
						+ uploadedFileURL);

				return uploadedFileURL;

			} else {
				postMethod.releaseConnection();

				logger.info("Upload failed: " + status + " - "
						+ postMethod.getStatusText());

				throw new FedoraClientException("Error uploading file: "
						+ postMethod.getStatusText());
			}

		} catch (HttpException e) {
			logger.debug(
					"Exception uploading file to Fedora - " + e.getMessage(), e);
			throw new FedoraClientException(
					"Exception uploading file to Fedora - " + e.getMessage(), e);
		} catch (IOException e) {
			logger.debug(
					"Exception uploading file to Fedora - " + e.getMessage(), e);
			throw new FedoraClientException(
					"Exception uploading file to Fedora - " + e.getMessage(), e);
		}
	}

	/**
	 * Upload the contents of an {@link InputStream} to Fedora.
	 * 
	 * @param inputStream
	 * 
	 * @return a {@link String} with the temporary id which can then be passed
	 *         to API-M requests as a URL. It will look like uploaded://123
	 * 
	 * @throws FedoraClientException
	 *             if something goes wrong with the upload.
	 */
	public String temporaryUpload(InputStream inputStream)
			throws FedoraClientException {
		try {

			return this.uploader.upload(inputStream);

		} catch (IOException e) {
			throw new FedoraClientException(
					"Exception uploading inputStream contents - "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Sets the state of a list of Fedora objects to <strong>"Active"</strong>.
	 * 
	 * @param pids
	 *            the PIDs of the objects.
	 * @param stopAtError
	 *            stops at the first error.
	 * 
	 * @return the number of successfully marked objects.
	 * 
	 * @throws FedoraClientException
	 */
	public int markObjectsActive(List<String> pids, boolean stopAtError)
			throws FedoraClientException {
		return setObjectsState(pids, RODAObject.STATE_ACTIVE, stopAtError);
	}

	/**
	 * Sets the state of a list of Fedora objects to
	 * <strong>"Inactive"</strong>.
	 * 
	 * @param pids
	 *            the PIDs of the objects.
	 * @param stopAtError
	 *            stops at the first error.
	 * 
	 * @return the number of successfully marked objects.
	 * 
	 * @throws FedoraClientException
	 */
	public int markObjectsInactive(List<String> pids, boolean stopAtError)
			throws FedoraClientException {
		return setObjectsState(pids, RODAObject.STATE_INACTIVE, stopAtError);
	}

	/**
	 * Sets the state of a list of Fedora objects to <strong>"Deleted"</strong>.
	 * 
	 * @param pids
	 *            the PIDs of the objects.
	 * @param stopAtError
	 *            stops at the first error.
	 * 
	 * @return the number of successfully marked objects.
	 * 
	 * @throws FedoraClientException
	 */
	public int markObjectsDeleted(List<String> pids, boolean stopAtError)
			throws FedoraClientException {
		return setObjectsState(pids, RODAObject.STATE_DELETED, stopAtError);
	}

	/**
	 * @param pid
	 * @param readUsers
	 * @param readGroups
	 * @throws FedoraClientException
	 */
	public void setReadPermissionsProperties(String pid, String[] readUsers,
			String[] readGroups) throws FedoraClientException {

		Map<String, String[]> properties = new HashMap<String, String[]>();
		// predicate -> object
		properties.put(FedoraRISearch.RDF_TAG_PERMISSION_READ_USER, readUsers);
		properties
				.put(FedoraRISearch.RDF_TAG_PERMISSION_READ_GROUP, readGroups);

		try {

			replaceRDFValueProperties(pid, properties);

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}

	}

	/**
	 * Sets the {@link Producers} of a specified {@link DescriptionObject}.
	 * 
	 * @param doPID
	 *            the PID of the {@link DescriptionObject}.
	 * @param producers
	 *            the {@link Producers} to set.
	 * 
	 * @throws FedoraClientException
	 */
	public void setDOProducerProperties(String doPID, Producers producers)
			throws FedoraClientException {

		Map<String, String[]> properties = new HashMap<String, String[]>();
		// predicate -> object
		properties.put(FedoraRISearch.RDF_TAG_PRODUCER_USER,
				producers.getUsers());
		properties.put(FedoraRISearch.RDF_TAG_PRODUCER_GROUP,
				producers.getGroups());

		try {

			replaceRDFValueProperties(doPID, properties);

		} catch (Exception e) {
			logger.debug(
					"Exception setting producer properties - " + e.getMessage(),
					e);
			throw new FedoraClientException(
					"Exception setting producer properties - " + e.getMessage(),
					e);
		}

	}

	/**
	 * Sets the state of a list of Fedora objects.
	 * 
	 * @param pids
	 *            the PIDs of the objects.
	 * @param state
	 *            the state to set.
	 * @param stopAtError
	 *            stops at the first error.
	 * 
	 * @return the number of successfully marked objects.
	 * 
	 * @throws FedoraClientException
	 */
	private int setObjectsState(List<String> pids, String state,
			boolean stopAtError) throws FedoraClientException {

		int count = 0;

		for (String pid : pids) {

			try {

				ObjectProfile objectProfile = getAPIA().getObjectProfile(pid,
						null);

				getAPIM().modifyObject(pid, getStateCode(state),
						objectProfile.getObjLabel(), this.cup.getName(),
						"Marked " + state + " by RODA FedoraClientUtility.");
				count++;

			} catch (RemoteException e) {
				if (stopAtError) {
					throw new FedoraClientException(e.getMessage(), e);
				} else {
					logger.warn("Error marking object " + pid + " " + state
							+ " - " + e.getMessage());
				}
			}
		}

		return count;
	}

	/**
	 * Get the Fedora code for the given state.
	 * 
	 * @param state
	 *            one of {@link RODAObject#STATE_ACTIVE},
	 *            {@link RODAObject#STATE_INACTIVE} or
	 *            {@link RODAObject#STATE_DELETED}.
	 * 
	 * @return a {@link String} with the Fedora code or <code>null</code> if the
	 *         state is not valid.
	 */
	private String getStateCode(String state) {

		String stateCode;

		if (RODAObject.STATE_ACTIVE.equalsIgnoreCase(state)) {
			stateCode = "A";
		} else if (RODAObject.STATE_INACTIVE.equalsIgnoreCase(state)) {
			stateCode = "I";
		} else if (RODAObject.STATE_DELETED.equalsIgnoreCase(state)) {
			stateCode = "D";
		} else {
			logger.warn("getStateCode(" + state + ") => null. State is unkown.");
			stateCode = null;
		}
		return stateCode;
	}

	/**
	 * Gets the Fedora state code for the given {@link RODAObject}.
	 * 
	 * @param rObject
	 *            the {@link RODAObject}.
	 * 
	 * @return a {@link String} with the Fedora code.
	 */
	public String getStateCode(RODAObject rObject) {
		return getStateCode(rObject.getState());
	}

	public String getStateCode(DatastreamState state) {

		if (state == DatastreamState.Active) {
			return "A";
		} else if (state == DatastreamState.Inactive) {
			return "I";
		} else {
			// (state == FedoraDatastreamState.Deleted)
			return "D";
		}
	}

	private void addRDFProperties(String pid, List<Pair<String, String>> properties,
			InputStream xsltAddProperty) throws Exception {

		Map<String, Object> parameters = new HashMap<String, Object>();
		InputStream rdfInputStream = null;
		ByteArrayOutputStream rdfOutputStream = null;

		boolean datastreamExists = false;

		try {

			MIMETypedStream dsDissemination = getAPIA()
					.getDatastreamDissemination(pid, "RELS-EXT", null);

			datastreamExists = dsDissemination != null;

			logger.trace("RELS-EXT exists? " + datastreamExists);

			// If datastream already exists, get a inputStream for it
			rdfInputStream = new ByteArrayInputStream(
					dsDissemination.getStream());

		} catch (Exception e) {

			logger.trace("Exception from Fedora getting RELS-EXT ("
					+ e.getMessage() + "). RELS-EXT exists? NO");

			datastreamExists = false;

			// If it doesn't exist, create an empty RDF for object pid
			// and get an inputStream for it
			rdfInputStream = getClass().getResourceAsStream("/empty.rdf");

			InputStream xsltCreateRDF = getClass().getResourceAsStream(
					"/createRDF.xslt");

			rdfOutputStream = new ByteArrayOutputStream();

			parameters.put("subjectPID", pid);

			XsltUtility.applyTransformation(xsltCreateRDF, parameters,
					rdfInputStream, rdfOutputStream);

			rdfInputStream = new ByteArrayInputStream(
					rdfOutputStream.toByteArray());
		}

		String xsltAddPropertyString = StreamUtility
				.inputStreamToString(xsltAddProperty);

		// Add all the properties in the properties Map
		for (Pair<String, String> entry : properties) {
			rdfOutputStream = new ByteArrayOutputStream();

			parameters.clear();
			parameters.put("predicate", entry.getFirst());
			parameters.put("object", entry.getSecond());

			// Add the property
			XsltUtility.applyTransformation(xsltAddPropertyString, parameters,
					rdfInputStream, rdfOutputStream);
			// }

			rdfInputStream = new ByteArrayInputStream(
					rdfOutputStream.toByteArray());
		}

		logger.trace("New RELS-EXT:\n"
				+ StreamUtility.inputStreamToString(rdfInputStream));

		// Check if the datastream already exists...
		if (datastreamExists) {

			// If the datastream already exists on object, modify it.
			getAPIM().modifyDatastreamByValue(pid, "RELS-EXT", new String[0],
					"Relationship Metadata", "text/xml", null,
					rdfOutputStream.toByteArray(), null, null,
					"Modified by RODA Core", false);

		} else {

			// If doesn't exist, upload a new one and ...
			String tempURL = temporaryUpload(rdfOutputStream.toByteArray());

			logger.trace("Datastream file uploaded to " + tempURL);

			// Add it to object pid
			getAPIM().addDatastream(pid, "RELS-EXT", new String[0],
					"Relationship Metadata", true, "text/xml", null, tempURL,
					"X", "A", null, null, "Added by RODA Core");

		}

	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param resource
	 * 
	 * @throws Exception
	 */
	private void addRDFResourceProperty(String pid, String propertyName,
			String resource) throws Exception {

		List<Pair<String,String>> properties = new ArrayList<Pair<String,String>>();
		properties.add(new Pair<String, String>(propertyName, resource));
		
		addRDFProperties(pid, properties,
				getClass().getResourceAsStream("/addRDFResourceProperty.xslt"));
	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param resource
	 * 
	 * @throws Exception
	 */
	// FIXME
	private void addRDFResourceProperties(String pid, String propertyName,
			String[] resources) throws Exception {

		List<Pair<String,String>> properties = new ArrayList<Pair<String,String>>();
		for (String resource : resources) {
			properties.add(new Pair<String, String>(propertyName, resource));
		}

		addRDFProperties(pid, properties,
				getClass().getResourceAsStream("/addRDFResourceProperty.xslt"));
	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param value
	 * 
	 * @throws Exception
	 */
	private void addRDFValueProperty(String pid, String propertyName,
			String value) throws Exception {

		List<Pair<String,String>> properties = new ArrayList<Pair<String,String>>();
		properties.add(new Pair<String, String>(propertyName, value));
	

		addRDFProperties(pid, properties,
				getClass().getResourceAsStream("/addRDFValueProperty.xslt"));
	}

	/**
	 * Removes a RDF property from object <code>subjectPID</code> named
	 * <code>predicate</code> with value/resource <code>object</code>.
	 * 
	 * @param subjectPID
	 *            the PID of the subject object
	 * @param predicate
	 *            the name of the property
	 * @param object
	 *            the value/resource of the property
	 * @throws Exception
	 *             if property couldn't be removed.
	 */
	private void removeRDFProperty(String subjectPID, String predicate,
			String object, InputStream xsltRemoveProperty) throws Exception {

		Map<String, Object> xsltParameters = new HashMap<String, Object>();
		xsltParameters.put("predicate", predicate);
		xsltParameters.put("object", object);

		Datastream datastream = getAPIM().getDatastream(subjectPID, "RELS-EXT",
				null);

		MIMETypedStream dissemination = getAPIA().getDatastreamDissemination(
				subjectPID, "RELS-EXT", null);

		if (dissemination != null) {

			InputStream inputStream = new ByteArrayInputStream(
					dissemination.getStream());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			XsltUtility.applyTransformation(xsltRemoveProperty, xsltParameters,
					inputStream, outputStream);

			getAPIM().modifyDatastreamByValue(subjectPID, "RELS-EXT", null,
					datastream.getLabel(), datastream.getMIMEType(), null,
					outputStream.toByteArray(), null, null,
					"Modified by RODA Core", false);

			logger.trace("removeRDFProperty(<" + subjectPID + ", " + predicate
					+ ", " + object + ">) => removed OK");

		} else {
			// No RELS-EXT datastream, nothing to remove
			logger.trace("removeRDFRelationship(<" + subjectPID + ", "
					+ predicate + ", " + object
					+ ">) => No RELS-EXT to remove from");
		}

	}

	/**
	 * Removes a RDF resource property, from object with PID
	 * <code>subjectPID</code>, named <code>predicate</code> with resource
	 * <code>object</code>.
	 * 
	 * @param subjectPID
	 *            the PID of the subject object
	 * @param predicate
	 *            the name of the property
	 * @param object
	 *            the resource
	 * 
	 * @throws Exception
	 */
	private void removeRDFResourceProperty(String subjectPID, String predicate,
			String object) throws Exception {

		removeRDFProperty(subjectPID, predicate, object, getClass()
				.getResourceAsStream("/removeRDFResourceProperty.xslt"));
	}

	/**
	 * Removes a RDF value property, from object with PID
	 * <code>subjectPID</code>, named <code>predicate</code> with value
	 * <code>object</code>.
	 * 
	 * @param subjectPID
	 *            the PID of the subject object
	 * @param predicate
	 *            the name of the property
	 * @param object
	 *            the value
	 * 
	 * @throws Exception
	 */
	private void removeRDFValueProperty(String subjectPID, String predicate,
			String object) throws Exception {

		removeRDFProperty(subjectPID, predicate, object, getClass()
				.getResourceAsStream("/removeRDFValueProperty.xslt"));
	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param resource
	 * 
	 * @throws Exception
	 */
	private void modifyRDFResourceSingleProperty(String pid,
			String propertyName, String resource) throws Exception {
		
		List<Pair<String,String>> properties = new ArrayList<Pair<String,String>>();
		properties.add(new Pair<String, String>(propertyName, resource));		

		addRDFProperties(
				pid,
				properties,
				getClass().getResourceAsStream(
						"/modifyRDFResourceSingleProperty.xslt"));

	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param value
	 * 
	 * @throws Exception
	 */
	private void modifyRDFValueSingleProperty(String pid, String propertyName,
			String value) throws Exception {

		// predicate -> object
		List<Pair<String,String>> properties = new ArrayList<Pair<String,String>>();
		properties.add(new Pair<String, String>(propertyName, value));

		modifyRDFValueSingleProperties(pid, properties);
	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param value
	 * @throws Exception
	 */
	private void modifyRDFValueSingleProperties(String pid,
			List<Pair<String,String>> properties) throws Exception {

		addRDFProperties(
				pid,
				properties,
				getClass().getResourceAsStream(
						"/modifyRDFValueSingleProperty.xslt"));
	}

	/**
	 * 
	 * @param pid
	 * @param propertyName
	 * @param value
	 * 
	 * @throws Exception
	 */
	private void replaceRDFValueProperties(String pid,
			Map<String, String[]> properties) throws Exception {

		logger.trace("replaceRDFValueProperties(pid=" + pid + ", properties="
				+ properties + ")");

		Map<String, Object> parameters = new HashMap<String, Object>();
		InputStream rdfInputStream = null;
		ByteArrayOutputStream rdfOutputStream = null;

		boolean datastreamExists = false;

		try {

			MIMETypedStream dsDissemination = getAPIA()
					.getDatastreamDissemination(pid, "RELS-EXT", null);

			datastreamExists = dsDissemination != null;

			logger.trace("RELS-EXT exists? " + datastreamExists);

			// If datastream already exists, get a inputStream for it
			rdfInputStream = new ByteArrayInputStream(
					dsDissemination.getStream());

		} catch (Exception e) {

			logger.trace("Exception from Fedora getting RELS-EXT ("
					+ e.getMessage() + "). RELS-EXT exists? NO");

			datastreamExists = false;

			// If it doesn't exist, create an empty RDF for object pid
			// and get an inputStream for it
			rdfInputStream = getClass().getResourceAsStream("/empty.rdf");

			InputStream xsltCreateRDF = getClass().getResourceAsStream(
					"/createRDF.xslt");

			rdfOutputStream = new ByteArrayOutputStream();

			parameters.put("subjectPID", pid);

			XsltUtility.applyTransformation(xsltCreateRDF, parameters,
					rdfInputStream, rdfOutputStream);

			rdfInputStream = new ByteArrayInputStream(
					rdfOutputStream.toByteArray());
		}

		String removeRDFValueSinglePropertyString = StreamUtility
				.inputStreamToString(getClass().getResourceAsStream(
						"/removeRDFValueSingleProperty.xslt"));

		// Remove all the properties in the properties Map
		for (String propertyName : properties.keySet()) {
			rdfOutputStream = new ByteArrayOutputStream();

			parameters.clear();
			parameters.put("predicate", propertyName);

			// Remove the property
			XsltUtility.applyTransformation(removeRDFValueSinglePropertyString,
					parameters, rdfInputStream, rdfOutputStream);

			rdfInputStream = new ByteArrayInputStream(
					rdfOutputStream.toByteArray());
		}

		String addRDFValuePropertyString = StreamUtility
				.inputStreamToString(getClass().getResourceAsStream(
						"/addRDFValueProperty.xslt"));

		// Add all the properties in the properties Map
		for (Map.Entry<String, String[]> entry : properties.entrySet()) {

			for (String value : entry.getValue()) {
				rdfOutputStream = new ByteArrayOutputStream();

				parameters.clear();
				parameters.put("predicate", entry.getKey());
				parameters.put("object", value);

				logger.trace("Adding property " + entry.getKey() + " => "
						+ value);

				// Add the property
				XsltUtility.applyTransformation(addRDFValuePropertyString,
						parameters, rdfInputStream, rdfOutputStream);

				rdfInputStream = new ByteArrayInputStream(
						rdfOutputStream.toByteArray());

				// logger.trace("New RELS-EXT:\n"
				// + StreamUtility
				// .inputStreamToString(new ByteArrayInputStream(
				// rdfOutputStream.toByteArray())));
			}

		}

		logger.trace("New RELS-EXT:\n"
				+ StreamUtility.inputStreamToString(rdfInputStream));

		// Check if the datastream already exists...
		if (datastreamExists) {

			// If the datastream already exists on object, modify it.
			getAPIM().modifyDatastreamByValue(pid, "RELS-EXT", new String[0],
					"Relationship Metadata", "text/xml", null,
					rdfOutputStream.toByteArray(), null, null,
					"Modified by RODA Core", false);

		} else {

			// If doesn't exist, upload a new one and ...
			String tempURL = temporaryUpload(rdfOutputStream.toByteArray());

			logger.trace("Datastream file uploaded to " + tempURL);

			// Add it to object pid
			getAPIM().addDatastream(pid, "RELS-EXT", new String[0],
					"Relationship Metadata", true, "text/xml", null, tempURL,
					"X", "A", null, null, "Added by RODA Core");

		}

	}

	public InputStream getRODAObjectRDF(String pid)
			throws FedoraClientException {

		try {

			MIMETypedStream dsDissemination = getAPIA()
					.getDatastreamDissemination(pid, this.relsExtDatastreamID,
							null);

			return new ByteArrayInputStream(dsDissemination.getStream());

		} catch (RemoteException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	public InputStream getEmptyRDF(String pid) throws FedoraClientException {

		try {
			InputStream rdfInputStream = getClass().getResourceAsStream(
					"/empty.rdf");

			InputStream xsltCreateRDF = getClass().getResourceAsStream(
					"/createRDF.xslt");

			ByteArrayOutputStream rdfOutputStream = new ByteArrayOutputStream();

			XsltUtility.applyTransformation(xsltCreateRDF,
					XsltUtility.createParameters("subjectPID", pid),
					rdfInputStream, rdfOutputStream);

			rdfInputStream = new ByteArrayInputStream(
					rdfOutputStream.toByteArray());

			return rdfInputStream;

		} catch (TransformerException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	public InputStream modifyRDFSingleProperties(InputStream rdfInputStream,
			Map<String, String> values) throws FedoraClientException {

		try {

			String xsltAddPropertyString = StreamUtility
					.inputStreamToString(getClass().getResourceAsStream(
							"/modifyRDFValueSingleProperty.xslt"));

			ByteArrayOutputStream rdfOutputStream = null;

			Map<String, Object> parameters = new HashMap<String, Object>();

			// Add all the properties in the properties Map
			for (Map.Entry<String, String> entry : values.entrySet()) {
				rdfOutputStream = new ByteArrayOutputStream();

				parameters.clear();
				parameters.put("predicate", entry.getKey());
				parameters.put("object", entry.getValue());

				// Add the property
				XsltUtility.applyTransformation(xsltAddPropertyString,
						parameters, rdfInputStream, rdfOutputStream);

				rdfInputStream = new ByteArrayInputStream(
						rdfOutputStream.toByteArray());
			}

			return rdfInputStream;

		} catch (TransformerException e) {
			throw new FedoraClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	public InputStream removeRDFProperties(InputStream rdfInputStream,
			Set<String> properties) throws FedoraClientException {

		try {

			String removeRDFValueSinglePropertyString = StreamUtility
					.inputStreamToString(getClass().getResourceAsStream(
							"/removeRDFValueSingleProperty.xslt"));

			Map<String, Object> parameters = new HashMap<String, Object>();
			ByteArrayOutputStream rdfOutputStream = null;

			// Remove all the properties in the properties Set
			for (String property : properties) {
				rdfOutputStream = new ByteArrayOutputStream();

				parameters.clear();
				parameters.put("predicate", property);

				// Remove the property
				XsltUtility.applyTransformation(
						removeRDFValueSinglePropertyString, parameters,
						rdfInputStream, rdfOutputStream);

				rdfInputStream = new ByteArrayInputStream(
						rdfOutputStream.toByteArray());
			}

			return rdfInputStream;

		} catch (TransformerException e) {
			throw new FedoraClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	public InputStream addRDFMultivalueProperties(InputStream rdfInputStream,
			Map<String, String[]> properties) throws FedoraClientException {

		try {
			String addRDFValuePropertyString = StreamUtility
					.inputStreamToString(getClass().getResourceAsStream(
							"/addRDFValueProperty.xslt"));

			Map<String, Object> parameters = new HashMap<String, Object>();
			ByteArrayOutputStream rdfOutputStream;

			// Add all the properties in the properties Map
			for (Map.Entry<String, String[]> entry : properties.entrySet()) {

				for (String value : entry.getValue()) {
					rdfOutputStream = new ByteArrayOutputStream();

					parameters.clear();
					parameters.put("predicate", entry.getKey());
					parameters.put("object", value);

					logger.trace("Adding property " + entry.getKey() + " => "
							+ value);

					// Add the property
					XsltUtility.applyTransformation(addRDFValuePropertyString,
							parameters, rdfInputStream, rdfOutputStream);

					rdfInputStream = new ByteArrayInputStream(
							rdfOutputStream.toByteArray());

				}

			}

			return rdfInputStream;

		} catch (TransformerException e) {
			throw new FedoraClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	public InputStream modifyRDFResourceProperties(InputStream rdfInputStream,
			Map<String, String> properties) throws FedoraClientException {

		try {

			String xsltAddPropertyString = StreamUtility
					.inputStreamToString(getClass().getResourceAsStream(
							"/modifyRDFResourceSingleProperty.xslt"));

			ByteArrayOutputStream rdfOutputStream = null;

			Map<String, Object> parameters = new HashMap<String, Object>();

			// Add all the properties in the properties Map
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				rdfOutputStream = new ByteArrayOutputStream();

				parameters.clear();
				parameters.put("predicate", entry.getKey());
				parameters.put("object", entry.getValue());

				// Add the property
				XsltUtility.applyTransformation(xsltAddPropertyString,
						parameters, rdfInputStream, rdfOutputStream);

				rdfInputStream = new ByteArrayInputStream(
						rdfOutputStream.toByteArray());
			}

			return rdfInputStream;

		} catch (TransformerException e) {
			throw new FedoraClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	private URL getFedoraUploadURL() {
		return this.fedoraUploadURL;
	}

	/*
	 * Maintenance methods
	 */

	public Map<String, String> getPreservedByRelationships()
			throws FedoraClientException {
		try {

			return getFedoraRISearch().getPreservedByRelationships();

		} catch (FedoraRISearchException e) {
			logger.debug(e.getMessage(), e);
			throw new FedoraClientException(e.getMessage(), e);
		}
	}

	public CASUserPrincipal getCASUserPrincipal() {
		return cup;
	}

}
