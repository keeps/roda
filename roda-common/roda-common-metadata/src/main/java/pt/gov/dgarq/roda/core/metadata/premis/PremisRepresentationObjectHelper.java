package pt.gov.dgarq.roda.core.metadata.premis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lc.xmlns.premisV2.LinkingEventIdentifierComplexType;
import lc.xmlns.premisV2.LinkingIntellectualEntityIdentifierComplexType;
import lc.xmlns.premisV2.ObjectDocument;
import lc.xmlns.premisV2.ObjectIdentifierComplexType;
import lc.xmlns.premisV2.PreservationLevelComplexType;
import lc.xmlns.premisV2.RelatedEventIdentificationComplexType;
import lc.xmlns.premisV2.RelatedObjectIdentificationComplexType;
import lc.xmlns.premisV2.RelationshipComplexType;
import lc.xmlns.premisV2.Representation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;

/**
 * @author Rui Castro
 */
public class PremisRepresentationObjectHelper extends PremisObjectHelper {

	private static final Logger logger = Logger
			.getLogger(PremisRepresentationObjectHelper.class);

	/**
	 * Creates a new instance of a {@link PremisRepresentationObjectHelper} for
	 * the PREMIS file inside the given file.
	 * 
	 * @param premisFile
	 *            the PREMIS XML file.
	 * 
	 * @return a {@link PremisRepresentationObjectHelper} for the given PREMIS
	 *         XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisRepresentationObjectHelper newInstance(File premisFile)
			throws PremisMetadataException, FileNotFoundException, IOException {
		FileInputStream premisInputStream = new FileInputStream(premisFile);
		PremisRepresentationObjectHelper instance = newInstance(premisInputStream);
		premisInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link PremisRepresentationObjectHelper} for
	 * the PREMIS XML inside the given {@link InputStream}.
	 * 
	 * @param premisInputStream
	 *            the PREMIS XML {@link InputStream}.
	 * 
	 * @return a {@link PremisRepresentationObjectHelper} for the given PREMIS
	 *         XML {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisRepresentationObjectHelper newInstance(
			InputStream premisInputStream) throws PremisMetadataException,
			IOException {

		try {

			ObjectDocument document = ObjectDocument.Factory
					.parse(premisInputStream);
			if (document.validate()) {
				return new PremisRepresentationObjectHelper(document);
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
	 * Constructs a new {@link PremisRepresentationObjectHelper} with a new
	 * {@link Representation}.
	 * 
	 * @throws PremisMetadataException
	 */
	public PremisRepresentationObjectHelper() throws PremisMetadataException {
		this(ObjectDocument.Factory.newInstance());
	}

	/**
	 * Constructs a new {@link PremisRepresentationObjectHelper} with an empty
	 * PREMIS document and sets the information inside the given
	 * {@link RepresentationPreservationObject}.
	 * 
	 * @param rpObject
	 *            the {@link RepresentationPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public PremisRepresentationObjectHelper(
			RepresentationPreservationObject rpObject)
			throws PremisMetadataException {
		this();
		setRepresentationPreservationObject(rpObject);
	}

	/**
	 * Create a new {@link PremisRepresentationObjectHelper}.
	 * 
	 * @param objectDocument
	 *            the PREMIS {@link ObjectDocument}.
	 * 
	 * @throws PremisMetadataException
	 *             if the {@link ObjectDocument} is not a {@link Representation}
	 *             .
	 */
	private PremisRepresentationObjectHelper(ObjectDocument objectDocument)
			throws PremisMetadataException {
		super(objectDocument);

		if (getRepresentation() == null) {

			setRepresentation(Representation.Factory.newInstance());

		} else if (!(getRepresentation() instanceof Representation)) {

			throw new PremisMetadataException(
					"Object inside document is not a Representation");
		}
	}

	/**
	 * @return the object
	 */
	public Representation getRepresentation() {
		return (Representation) getObjectDocument().getObject();
	}

	/**
	 * Returns the &lt;linkingIntellectualEntityIdentifierValue&gt; if exists,
	 * otherwise <code>null</code>.
	 * 
	 * @return a {@link String} with the
	 *         &lt;linkingIntellectualEntityIdentifierValue&gt; or
	 *         <code>null</code>.
	 */
	public String getLinkingIntellectualEntityIdentifierValue() {

		String entityIdentifierValue = null;

		List<LinkingIntellectualEntityIdentifierComplexType> entityIdentifierList = getRepresentation()
				.getLinkingIntellectualEntityIdentifierList();

		if (entityIdentifierList != null && entityIdentifierList.size() > 0) {
			entityIdentifierValue = entityIdentifierList.get(0)
					.getLinkingIntellectualEntityIdentifierValue();
		}

		return entityIdentifierValue;
	}

	/**
	 * Returns the information about a {@link RepresentationPreservationObject}
	 * from a PREMIS XML file and copy the values from the given
	 * {@link RODAObject}.
	 * 
	 * @param simpleRPO
	 *            the {@link RODAObject} of the
	 *            {@link RepresentationPreservationObject}.
	 * 
	 * @return a {@link RepresentationPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public RepresentationPreservationObject getRepresentationPreservationObject(
			SimpleRepresentationPreservationObject simpleRPO)
			throws PremisMetadataException {

		RepresentationPreservationObject representationPO = getRepresentationPreservationObject();

		// Copy the values from the SimpleRepresentationPreservationObject
		representationPO.setPid(simpleRPO.getPid());
		representationPO.setLabel(simpleRPO.getLabel());
		representationPO.setContentModel(simpleRPO.getContentModel());
		representationPO.setLastModifiedDate(simpleRPO.getLastModifiedDate());
		representationPO.setCreatedDate(simpleRPO.getCreatedDate());
		representationPO.setState(simpleRPO.getState());
		representationPO.setRepresentationObjectPID(simpleRPO
				.getRepresentationObjectPID());

		return representationPO;
	}

	/**
	 * Returns the information about a {@link RepresentationPreservationObject}
	 * from a PREMIS XML file.
	 * 
	 * @return a {@link RepresentationPreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 *             if the PREMIS is not a representation.
	 */
	public RepresentationPreservationObject getRepresentationPreservationObject()
			throws PremisMetadataException {

		RepresentationPreservationObject pObject = new RepresentationPreservationObject();

		// <objectIdentifier>
		List<ObjectIdentifierComplexType> objectIdentifierList = getRepresentation()
				.getObjectIdentifierList();
		if (objectIdentifierList != null && objectIdentifierList.size() > 0) {

			for (ObjectIdentifierComplexType objectIdentifier : objectIdentifierList) {

				if (PremisHelper.premisIdentifierTypePID
						.equals(objectIdentifier.getObjectIdentifierType())) {
					pObject.setID(objectIdentifier.getObjectIdentifierValue());
				}

				if (PremisHelper.premisIdentifierTypeContentModel
						.equals(objectIdentifier.getObjectIdentifierType())) {
					pObject.setRepresentationContentModel(objectIdentifier
							.getObjectIdentifierValue());
				}

			}

			// If we don't have an ID, it's because the <ObjectIdentifierType>
			// is unknown. Let's use the first value as ID.
			if (pObject.getID() == null) {
				pObject.setID(objectIdentifierList.get(0)
						.getObjectIdentifierValue());
			}

		} else {
			logger.warn("PREMIS Representation doesn't have an ID");
		}

		// <preservationLevel>
		if (getRepresentation().getPreservationLevelList().size() > 0) {
			pObject.setPreservationLevel(getRepresentation()
					.getPreservationLevelArray(0).getPreservationLevelValue());
		}

		List<RepresentationFilePreservationObject> partFiles = new ArrayList<RepresentationFilePreservationObject>();

		// <relationship>
		if (getRepresentation().getRelationshipList().size() > 0) {

			for (RelationshipComplexType relationship : getRepresentation()
					.getRelationshipList()) {

				if (premisRelationshipTypeStructural.equals(relationship
						.getRelationshipType())) {

					if (premisRelationshipSubTypeHasRoot.equals(relationship
							.getRelationshipSubType())) {

						RelatedObjectIdentificationComplexType relatedObjectIdentification = relationship
								.getRelatedObjectIdentificationArray(0);

						RepresentationFilePreservationObject rootFile = new RepresentationFilePreservationObject();
						rootFile.setID(relatedObjectIdentification
								.getRelatedObjectIdentifierValue());

						pObject.setRootFile(rootFile);

					} else if (premisRelationshipSubTypeHasPart
							.equals(relationship.getRelationshipSubType())) {

						for (RelatedObjectIdentificationComplexType partObjectIdentification : relationship
								.getRelatedObjectIdentificationList()) {

							RepresentationFilePreservationObject partFile = new RepresentationFilePreservationObject();

							partFile.setID(partObjectIdentification
									.getRelatedObjectIdentifierValue());

							partFiles.add(partObjectIdentification
									.getRelatedObjectSequence().intValue() - 1,
									partFile);
						}

					} else {

						logger.warn("Found unknown structural relationship '"
								+ relationship.getRelationshipSubType() + "'");

					}

				} else if (premisRelationshipTypeDerivation.equals(relationship
						.getRelationshipType())) {

					if (premisRelationshipSubTypeDerivedFrom
							.equals(relationship.getRelationshipSubType())) {

						if (relationship.getRelatedObjectIdentificationList() != null
								&& relationship
										.getRelatedObjectIdentificationList()
										.size() > 0) {

							RelatedObjectIdentificationComplexType relatedObjectIdentification = relationship
									.getRelatedObjectIdentificationArray(0);
							pObject.setDerivedFromRepresentationObjectID(relatedObjectIdentification
									.getRelatedObjectIdentifierValue());
						}

						if (relationship.getRelatedEventIdentificationList() != null
								&& relationship
										.getRelatedEventIdentificationList()
										.size() > 0) {

							RelatedEventIdentificationComplexType relatedEventIdentification = relationship
									.getRelatedEventIdentificationArray(0);
							pObject.setDerivationEventID(relatedEventIdentification
									.getRelatedEventIdentifierValue());
						}

					} else {
						logger.warn("Found unknown derivation relationship '"
								+ relationship.getRelationshipSubType() + "'");
					}

				} else {
					logger.warn("Found unknown relationship '"
							+ relationship.getRelationshipType() + "'");
				}
			}

			pObject.setPartFiles(partFiles
					.toArray(new RepresentationFilePreservationObject[partFiles
							.size()]));

		} else {
			// No relationships
			logger.warn("Representation doesn't have relationships!");
		}

		// <linkingEventIdentifier>
		List<String> eventIDs = new ArrayList<String>();

		for (LinkingEventIdentifierComplexType eventIdentifier : getRepresentation()
				.getLinkingEventIdentifierList()) {
			eventIDs.add(eventIdentifier.getLinkingEventIdentifierValue());
		}

		pObject.setPreservationEventIDs(eventIDs.toArray(new String[eventIDs
				.size()]));

		return pObject;
	}

	/**
	 * Replaces the current PREMIS XML data for the data inside the given
	 * {@link RepresentationPreservationObject}.
	 * 
	 * @param rpo
	 */
	public void setRepresentationPreservationObject(
			RepresentationPreservationObject rpo) {

		// Replaces the current object with a new empty <object
		// xsi:type="representation">
		setRepresentation(Representation.Factory.newInstance());

		// <objectIdentifier>
		ObjectIdentifierComplexType objectIdentifier = getRepresentation()
				.addNewObjectIdentifier();
		objectIdentifier
				.setObjectIdentifierType(PremisHelper.premisIdentifierTypePID);
		objectIdentifier.setObjectIdentifierValue(rpo.getID());

		// <objectIdentifier>
		if (rpo.getRepresentationContentModel() != null) {
			ObjectIdentifierComplexType objectCModelIdentifier = getRepresentation()
					.addNewObjectIdentifier();
			objectCModelIdentifier
					.setObjectIdentifierType(PremisHelper.premisIdentifierTypeContentModel);
			objectCModelIdentifier.setObjectIdentifierValue(rpo
					.getRepresentationContentModel());
		}

		// <preservationLevel>
		PreservationLevelComplexType preservationLevel = getRepresentation()
				.addNewPreservationLevel();
		preservationLevel.setPreservationLevelValue(rpo.getPreservationLevel());
		// preservationLevel.setPreservationLevelRole();
		// preservationLevel.addPreservationLevelRationale();
		preservationLevel.setPreservationLevelDateAssigned(DateParser
				.getIsoDateNoMillis(new Date()));

		if (rpo.getRootFile() != null) {

			// Add "has root" structural relationship
			RelationshipComplexType relationshipHasRoot = createRelationship(
					getRepresentation(), premisRelationshipTypeStructural,
					premisRelationshipSubTypeHasRoot);

			// <relationship><relatedObjectIdentification>
			addNewRelatedObject(relationshipHasRoot,
					PremisHelper.premisIdentifierTypeDatastreamID, rpo
							.getRootFile().getID(), 0);
		}

		if (rpo.getPartFiles() != null && rpo.getPartFiles().length > 0) {

			// Add "has part" structural relationship
			RelationshipComplexType relationshipHasPart = createRelationship(
					getRepresentation(), premisRelationshipTypeStructural,
					premisRelationshipSubTypeHasPart);

			for (int index = 0; index < rpo.getPartFiles().length; index++) {

				RepresentationFilePreservationObject rFilePObject = rpo
						.getPartFiles()[index];

				// <relationship><relatedObjectIdentification>
				addNewRelatedObject(relationshipHasPart,
						PremisHelper.premisIdentifierTypeDatastreamID,
						rFilePObject.getID(), index + 1);
			}

		}

		if (!StringUtils.isBlank(rpo.getDerivedFromRepresentationObjectID())) {

			// Add "derived from" derivation relationship

			RelationshipComplexType relationshipDerivedFrom = createRelationship(
					getRepresentation(), premisRelationshipTypeDerivation,
					premisRelationshipSubTypeDerivedFrom);

			addNewRelatedObject(relationshipDerivedFrom,
					PremisHelper.premisIdentifierTypePID,
					rpo.getDerivedFromRepresentationObjectID());

			// <relatedEventIdentification>
			if (!StringUtils.isBlank(rpo.getDerivationEventID())) {

				RelatedEventIdentificationComplexType relatedEventIdentification = relationshipDerivedFrom
						.addNewRelatedEventIdentification();
				relatedEventIdentification
						.setRelatedEventIdentifierType(PremisHelper.premisIdentifierTypePID);
				relatedEventIdentification.setRelatedEventIdentifierValue(rpo
						.getDerivationEventID());

			}
		}

		// <linkingEventIdentifier>
		if (rpo.getPreservationEventIDs() != null) {

			for (String eventID : rpo.getPreservationEventIDs()) {

				LinkingEventIdentifierComplexType linkingEventIdentifier = getRepresentation()
						.addNewLinkingEventIdentifier();

				linkingEventIdentifier
						.setLinkingEventIdentifierType(PremisHelper.premisIdentifierTypePID);
				linkingEventIdentifier.setLinkingEventIdentifierValue(eventID);
			}

		}

	}

	private void setRepresentation(Representation representation) {
		getObjectDocument().setObject(representation);
	}

	private RelationshipComplexType createRelationship(
			Representation representation, String type, String subType) {

		RelationshipComplexType relationship = representation
				.addNewRelationship();

		relationship.setRelationshipType(type);
		relationship.setRelationshipSubType(subType);

		return relationship;
	}

	private RelatedObjectIdentificationComplexType addNewRelatedObject(
			RelationshipComplexType relationship, String identifierType,
			String identifierValue) {

		RelatedObjectIdentificationComplexType relatedObjectIdentification = relationship
				.addNewRelatedObjectIdentification();
		relatedObjectIdentification
				.setRelatedObjectIdentifierType(identifierType);
		relatedObjectIdentification
				.setRelatedObjectIdentifierValue(identifierValue);

		return relatedObjectIdentification;

	}

	private RelatedObjectIdentificationComplexType addNewRelatedObject(
			RelationshipComplexType relationship, String identifierType,
			String identifierValue, long sequenceNumber) {

		RelatedObjectIdentificationComplexType relatedObjectIdentification = addNewRelatedObject(
				relationship, identifierType, identifierValue);
		relatedObjectIdentification.setRelatedObjectSequence(BigInteger
				.valueOf(sequenceNumber));

		return relatedObjectIdentification;

	}

}
