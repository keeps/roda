package pt.gov.dgarq.roda.core.metadata.premis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.EventDocument;
import lc.xmlns.premisV2.EventIdentifierComplexType;
import lc.xmlns.premisV2.EventOutcomeDetailComplexType;
import lc.xmlns.premisV2.EventOutcomeInformationComplexType;
import lc.xmlns.premisV2.ExtensionComplexType;
import lc.xmlns.premisV2.LinkingAgentIdentifierComplexType;
import lc.xmlns.premisV2.LinkingObjectIdentifierComplexType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject;
import pt.gov.dgarq.roda.core.metadata.MetadataException;
import pt.gov.dgarq.roda.core.metadata.MetadataHelperUtility;
import pt.gov.dgarq.roda.util.XmlEncodeUtility;

/**
 * @author Rui Castro
 */
public class PremisEventHelper {

	private static final Logger logger = Logger
			.getLogger(PremisEventHelper.class);

	private final EventDocument eventDocument;

	/**
	 * Creates a new instance of a {@link PremisEventHelper} for the PREMIS file
	 * inside the given file.
	 * 
	 * @param premisFile
	 *            the PREMIS XML file.
	 * 
	 * @return a {@link PremisEventHelper} for the given PREMIS XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisEventHelper newInstance(File premisFile)
			throws PremisMetadataException, FileNotFoundException, IOException {
		FileInputStream premisInputStream = new FileInputStream(premisFile);
		PremisEventHelper instance = newInstance(premisInputStream);
		premisInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link PremisEventHelper} for the PREMIS XML
	 * inside the given {@link InputStream}.
	 * 
	 * @param premisInputStream
	 *            the PREMIS XML {@link InputStream}.
	 * 
	 * @return a {@link PremisEventHelper} for the given PREMIS XML
	 *         {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisEventHelper newInstance(InputStream premisInputStream)
			throws PremisMetadataException, IOException {

		try {

			EventDocument document = EventDocument.Factory
					.parse(premisInputStream);
			if (document.validate()) {
				return new PremisEventHelper(document);
			} else {
				throw new PremisMetadataException(
						"Error validating XML document");
			}

		} catch (XmlException e) {
			logger.debug("Error parsing PREMIS - " + e.getMessage(), e);
			throw new PremisMetadataException("Error parsing PREMIS - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Constructs a new {@link PremisEventHelper} with a new
	 * {@link EventDocument}.
	 */
	public PremisEventHelper() {
		this(EventDocument.Factory.newInstance());
	}

	/**
	 * Constructs a new {@link PremisEventHelper} with an empty PREMIS document
	 * and sets the information inside the given {@link EventPreservationObject}
	 * .
	 * 
	 * @param eventPObject
	 *            the {@link EventPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public PremisEventHelper(EventPreservationObject eventPObject)
			throws PremisMetadataException {
		this();
		setEventPreservationObject(eventPObject);
	}

	/**
	 * Create a new PREMIS helper
	 * 
	 * @param eventDocument
	 *            the PREMIS {@link EventDocument}.
	 */
	private PremisEventHelper(EventDocument eventDocument) {

		this.eventDocument = eventDocument;

		if (getEvent() == null) {
			setEvent(EventComplexType.Factory.newInstance());
		}

	}

	/**
	 * @return the eventDocument
	 */
	public EventDocument getEventDocument() {
		return eventDocument;
	}

	/**
	 * @return the eventDocument
	 */
	public EventComplexType getEvent() {
		return getEventDocument().getEvent();
	}

	/**
	 * Returns the information about a {@link EventPreservationObject} from a
	 * PREMIS XML file and copy the values from the given {@link RODAObject}.
	 * 
	 * @param simpleEPO
	 *            the {@link SimpleEventPreservationObject} of the
	 *            {@link EventPreservationObject}.
	 * 
	 * @return a {@link EventPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public EventPreservationObject getEventPreservationObject(
			SimpleEventPreservationObject simpleEPO)
			throws PremisMetadataException {

		EventPreservationObject event = getEventPreservationObject();

		// Copy the values from the RODAObject
		event.setPid(simpleEPO.getPid());
		event.setLabel(simpleEPO.getLabel());
		event.setContentModel(simpleEPO.getContentModel());
		event.setLastModifiedDate(simpleEPO.getLastModifiedDate());
		event.setCreatedDate(simpleEPO.getCreatedDate());
		event.setState(simpleEPO.getState());
		event.setAgentPID(simpleEPO.getAgentPID());
		event.setTargetPID(simpleEPO.getTargetPID());

		return event;
	}

	/**
	 * Returns the information about a {@link EventPreservationObject} from a
	 * PREMIS XML file.
	 * 
	 * @return a {@link EventPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public EventPreservationObject getEventPreservationObject()
			throws PremisMetadataException {

		EventPreservationObject pObject = new EventPreservationObject();

		// <eventIdentifier>
		EventIdentifierComplexType eventIdentifier = getEvent()
				.getEventIdentifier();
		if (eventIdentifier != null) {
			pObject.setID(eventIdentifier.getEventIdentifierValue());
		} else {
			logger.warn("PREMIS Event doesn't have an ID");
		}

		// <eventType>
		pObject.setEventType(getEvent().getEventType());

		// <eventDateTime>
		if (getEvent().getEventDateTime() != null
				&& !StringUtils.isBlank(getEvent().getEventDateTime()
						.toString())) {
			try {

				pObject.setDatetime(DateParser.parse(getEvent()
						.getEventDateTime().toString()));

			} catch (InvalidDateException e) {
				throw new PremisMetadataException(
						"PREMIS eventDateTime is not valid - " + e.getMessage(),
						e);
			}
		}

		// <eventDetail>
		if (!StringUtils.isBlank(getEvent().getEventDetail())) {
			pObject.setEventDetail(getEvent().getEventDetail());
		}

		// <eventOutcomeInformation>
		if (getEvent().getEventOutcomeInformationList() != null
				&& getEvent().getEventOutcomeInformationList().size() > 0) {

			EventOutcomeInformationComplexType eventOutcomeInformation = getEvent()
					.getEventOutcomeInformationArray(0);

			pObject.setOutcome(eventOutcomeInformation.getEventOutcome());

			// <eventOutcomeDetail>
			if (eventOutcomeInformation.getEventOutcomeDetailList() != null
					&& eventOutcomeInformation.getEventOutcomeDetailList()
							.size() > 0) {

				EventOutcomeDetailComplexType eventOutcomeDetail = eventOutcomeInformation
						.getEventOutcomeDetailArray(0);

				pObject.setOutcomeDetailNote(eventOutcomeDetail
						.getEventOutcomeDetailNote());

				XmlObject[] textList = eventOutcomeDetail
						.getEventOutcomeDetailExtension().selectPath(
								"p/pre/text()"); //$NON-NLS-1$
				if (textList != null && textList.length > 0) {
					pObject.setOutcomeDetailExtension(textList[0].xmlText());
				} else {
					pObject.setOutcomeDetailExtension(eventOutcomeDetail
							.getEventOutcomeDetailExtension().xmlText());
				}

			}

		}

		// <linkingAgentIdentifier>
		if (getEvent().getLinkingAgentIdentifierList() != null
				&& getEvent().getLinkingAgentIdentifierList().size() > 0) {

			LinkingAgentIdentifierComplexType linkingAgentIdentifier = getEvent()
					.getLinkingAgentIdentifierArray(0);

			pObject.setAgentID(linkingAgentIdentifier
					.getLinkingAgentIdentifierValue());

			if (linkingAgentIdentifier.getLinkingAgentRoleList() != null
					&& linkingAgentIdentifier.getLinkingAgentRoleList().size() > 0) {

				pObject.setAgentRole(linkingAgentIdentifier
						.getLinkingAgentRoleArray(0));

			}
		}

		// <linkingObjectIdentifier>
		if (getEvent().getLinkingObjectIdentifierList() != null) {

			List<String> objectIDs = new ArrayList<String>();

			for (LinkingObjectIdentifierComplexType linkingObjectIdentifier : getEvent()
					.getLinkingObjectIdentifierList()) {

				objectIDs.add(linkingObjectIdentifier
						.getLinkingObjectIdentifierValue());

			}

			pObject.setObjectIDs(objectIDs.toArray(new String[objectIDs.size()]));

		}

		return pObject;
	}

	/**
	 * Replaces the current PREMIS XML data for the data inside the given
	 * {@link EventPreservationObject}.
	 * 
	 * @param eventPObject
	 * 
	 * @throws PremisMetadataException
	 */
	public void setEventPreservationObject(EventPreservationObject eventPObject)
			throws PremisMetadataException {

		// Replaces the current event with a new empty <event>
		setEvent(EventComplexType.Factory.newInstance());

		// <eventIdentifier>
		EventIdentifierComplexType eventIdentifier = getEvent()
				.addNewEventIdentifier();
		eventIdentifier
				.setEventIdentifierType(PremisHelper.premisIdentifierTypePID);
		eventIdentifier.setEventIdentifierValue(eventPObject.getID());

		// <eventType>
		getEvent().setEventType(eventPObject.getEventType());

		// <eventDateTime>
		getEvent().setEventDateTime(
				DateParser.getIsoDate(eventPObject.getDatetime()));

		// <eventDetail>
		getEvent().setEventDetail(eventPObject.getEventDetail());

		// <eventOutcomeInformation>
		EventOutcomeInformationComplexType eventOutcomeInformation = getEvent()
				.addNewEventOutcomeInformation();
		eventOutcomeInformation.setEventOutcome(eventPObject.getOutcome());

		// <eventOutcomeInformation><eventOutcomeDetail>
		if (!StringUtils.isBlank(eventPObject.getOutcomeDetailExtension())) {

			EventOutcomeDetailComplexType eventOutcomeDetail = eventOutcomeInformation
					.addNewEventOutcomeDetail();

			if (!StringUtils.isBlank(eventPObject.getOutcomeDetailNote())) {

				eventOutcomeDetail.setEventOutcomeDetailNote(eventPObject
						.getOutcomeDetailNote());

			}

			ExtensionComplexType eventOutcomeDetailExtension = eventOutcomeDetail
					.addNewEventOutcomeDetailExtension();

			try {

				XmlObject xmlPObject = XmlObject.Factory
						.parse("<p xmlns=\"http://www.w3.org/1999/xhtml\">"
								+ XmlEncodeUtility.encode(eventPObject
										.getOutcomeDetailExtension()) + "</p>");

				eventOutcomeDetailExtension.set(xmlPObject);

			} catch (XmlException e) {
				logger.debug(
						"Error parsing eventOutcomeDetailExtension XML data - "
								+ e.getMessage(), e);
				throw new PremisMetadataException(
						"Error parsing eventOutcomeDetailExtension XML data - "
								+ e.getMessage(), e);
			}

		}

		// <linkingAgentIdentifier>
		LinkingAgentIdentifierComplexType linkingAgentIdentifier = getEvent()
				.addNewLinkingAgentIdentifier();
		linkingAgentIdentifier
				.setLinkingAgentIdentifierType(PremisHelper.premisIdentifierTypePID);
		linkingAgentIdentifier.setLinkingAgentIdentifierValue(eventPObject
				.getAgentID());
		if (!StringUtils.isBlank(eventPObject.getAgentRole())) {
			linkingAgentIdentifier.addLinkingAgentRole(eventPObject
					.getAgentRole());
		}

		// <linkingObjectIdentifier>
		if (eventPObject.getObjectIDs() != null) {
			for (String objectID : eventPObject.getObjectIDs()) {

				LinkingObjectIdentifierComplexType linkingObjectIdentifier = getEvent()
						.addNewLinkingObjectIdentifier();

				linkingObjectIdentifier
						.setLinkingObjectIdentifierType(PremisHelper.premisIdentifierTypePID);
				linkingObjectIdentifier
						.setLinkingObjectIdentifierValue(objectID);
				linkingObjectIdentifier
						.addLinkingObjectRole(EventPreservationObject.PRESERVATION_EVENT_OBJECT_ROLE_TARGET);
			}
		}

	}

	/**
	 * Saves the current PREMIS document to a byte array.
	 * 
	 * @return a <code>byte[]</code> with the contents of the PREMIS XML file.
	 * 
	 * @throws PremisMetadataException
	 *             if the PREMIS document is not valid or if something goes
	 *             wrong with the serialisation.
	 */
	public byte[] saveToByteArray() throws PremisMetadataException {

		try {

			return MetadataHelperUtility.saveToByteArray(getEventDocument());

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new PremisMetadataException(e.getMessage(), e);
		}
	}

	/**
	 * Saves the current PREMIS document to a {@link File}.
	 * 
	 * @param premisFile
	 *            the {@link File}.
	 * 
	 * @throws PremisMetadataException
	 *             if the EAD-C document is not valid or if something goes wrong
	 *             with the serialisation.
	 * 
	 * @throws IOException
	 *             if {@link FileOutputStream} associated with the {@link File}
	 *             couldn't be closed.
	 * @throws IOException
	 */
	public void saveToFile(File premisFile) throws PremisMetadataException,
			IOException {
		try {

			MetadataHelperUtility.saveToFile(getEventDocument(), premisFile);

		} catch (MetadataException e) {
			logger.debug(e.getMessage(), e);
			throw new PremisMetadataException(e.getMessage(), e);
		}
	}

	private void setEvent(EventComplexType event) {
		getEventDocument().setEvent(event);
	}

}
