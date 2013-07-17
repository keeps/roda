package pt.gov.dgarq.roda.core.metadata.premis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lc.xmlns.premisV2.ContentLocationComplexType;
import lc.xmlns.premisV2.CreatingApplicationComplexType;
import lc.xmlns.premisV2.EventDocument;
import lc.xmlns.premisV2.ExtensionComplexType;
import lc.xmlns.premisV2.File;
import lc.xmlns.premisV2.FixityComplexType;
import lc.xmlns.premisV2.FormatComplexType;
import lc.xmlns.premisV2.FormatDesignationComplexType;
import lc.xmlns.premisV2.FormatRegistryComplexType;
import lc.xmlns.premisV2.ObjectCharacteristicsComplexType;
import lc.xmlns.premisV2.ObjectDocument;
import lc.xmlns.premisV2.ObjectIdentifierComplexType;
import lc.xmlns.premisV2.OriginalNameComplexType;
import lc.xmlns.premisV2.PreservationLevelComplexType;
import lc.xmlns.premisV2.StorageComplexType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.data.preservation.Fixity;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;

/**
 * @author Rui Castro
 */
public class PremisFileObjectHelper extends PremisObjectHelper {

	private static final Logger logger = Logger
			.getLogger(PremisFileObjectHelper.class);

	/**
	 * Creates a new instance of a {@link PremisFileObjectHelper} for the PREMIS
	 * file inside the given file.
	 * 
	 * @param premisFile
	 *            the PREMIS XML file.
	 * 
	 * @return a {@link PremisFileObjectHelper} for the given PREMIS XML file.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws FileNotFoundException
	 *             if the specified File cannot be found.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisFileObjectHelper newInstance(java.io.File premisFile)
			throws PremisMetadataException, FileNotFoundException, IOException {
		FileInputStream premisInputStream = new FileInputStream(premisFile);
		PremisFileObjectHelper instance = newInstance(premisInputStream);
		premisInputStream.close();
		return instance;
	}

	/**
	 * Creates a new instance of a {@link PremisFileObjectHelper} for the PREMIS
	 * XML inside the given {@link InputStream}.
	 * 
	 * @param premisInputStream
	 *            the PREMIS XML {@link InputStream}.
	 * 
	 * @return a {@link PremisFileObjectHelper} for the given PREMIS XML
	 *         {@link InputStream}.
	 * 
	 * @throws IOException
	 *             if some I/O error occurs.
	 * @throws PremisMetadataException
	 *             if the PREMIS XML document is invalid.
	 */
	public static PremisFileObjectHelper newInstance(
			InputStream premisInputStream) throws PremisMetadataException,
			IOException {

		try {

			ObjectDocument document = ObjectDocument.Factory
					.parse(premisInputStream);
			if (document.validate()) {
				return new PremisFileObjectHelper(document);
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
	 * Constructs a new {@link PremisFileObjectHelper} with a new {@link File}.
	 * 
	 * @throws PremisMetadataException
	 */
	public PremisFileObjectHelper() throws PremisMetadataException {
		this(ObjectDocument.Factory.newInstance());
	}

	/**
	 * Constructs a new {@link PremisFileObjectHelper} with a new {@link File}
	 * and set the contents of {@link RepresentationFilePreservationObject}.
	 * 
	 * @param filePObject
	 *            the {@link RepresentationFilePreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 */
	public PremisFileObjectHelper(
			RepresentationFilePreservationObject filePObject)
			throws PremisMetadataException {
		this();
		setFilePreservationObject(filePObject);
	}

	/**
	 * Create a new PREMIS helper
	 * 
	 * @param objectDocument
	 *            the PREMIS {@link ObjectDocument}.
	 * 
	 * @throws PremisMetadataException
	 *             if the {@link ObjectDocument} is not a {@link File}.
	 */
	private PremisFileObjectHelper(ObjectDocument objectDocument)
			throws PremisMetadataException {

		super(objectDocument);

		if (getFile() == null) {

			setFile(File.Factory.newInstance());

		} else if (!(getFile() instanceof File)) {

			throw new PremisMetadataException(
					"Object inside document is not a File");
		}
	}

	/**
	 * @return the object
	 */
	public File getFile() {
		return (File) getObjectDocument().getObject();
	}

	/**
	 * Returns the PREMIS information inside a
	 * {@link RepresentationFilePreservationObject}.
	 * 
	 * @return a {@link RepresentationFilePreservationObject}.
	 * 
	 * @throws PremisMetadataException
	 *             if the PREMIS is not a representation.
	 */
	public RepresentationFilePreservationObject getRepresentationFilePreservationObject()
			throws PremisMetadataException {

		RepresentationFilePreservationObject pObject = new RepresentationFilePreservationObject();

		// <objectIdentifier>
		List<ObjectIdentifierComplexType> objectIdentifierList = getFile()
				.getObjectIdentifierList();
		if (objectIdentifierList != null && objectIdentifierList.size() > 0) {
			pObject.setID(getFile().getObjectIdentifierArray(0)
					.getObjectIdentifierValue());
		} else {
			logger.warn("PREMIS File doesn't have an ID");
		}

		// <preservationLevel>
		if (getFile().getPreservationLevelList().size() > 0) {
			pObject.setPreservationLevel(getFile().getPreservationLevelArray(0)
					.getPreservationLevelValue());
		}

		// <objectCharacteristics>
		if (getFile().getObjectCharacteristicsList() != null
				&& getFile().getObjectCharacteristicsList().size() > 0) {
			readObjectCharacteristics(pObject, getFile()
					.getObjectCharacteristicsArray(0));
		}

		if (getFile().getOriginalName() != null) {
			pObject.setOriginalName(getFile().getOriginalName()
					.getStringValue());
		}

		// <storage>
		// We're not writing this element
		if (getFile().getStorageList() != null
				&& getFile().getStorageList().size() > 0) {

			StorageComplexType storage = getFile().getStorageArray(0);

			pObject.setContentLocationType(storage.getContentLocation()
					.getContentLocationType());
			pObject.setContentLocationValue(storage.getContentLocation()
					.getContentLocationValue());
		}

		return pObject;

	}

	/**
	 * Replaces the current PREMIS XML data for the data inside the given
	 * {@link RepresentationFilePreservationObject}.
	 * 
	 * @param filePObject
	 * 
	 * @throws PremisMetadataException
	 */
	public void setFilePreservationObject(
			RepresentationFilePreservationObject filePObject)
			throws PremisMetadataException {

		// Replaces the current object with a new empty <object
		// xsi:type="file">
		setFile(File.Factory.newInstance());

		// <objectIdentifier>
		ObjectIdentifierComplexType objectIdentifier = getFile()
				.addNewObjectIdentifier();
		objectIdentifier
				.setObjectIdentifierType(PremisHelper.premisIdentifierTypeDatastreamID);
		objectIdentifier.setObjectIdentifierValue(filePObject.getID());

		// <preservationLevel>
		PreservationLevelComplexType preservationLevel = getFile()
				.addNewPreservationLevel();
		preservationLevel.setPreservationLevelValue(filePObject
				.getPreservationLevel());
		// preservationLevel.setPreservationLevelRole();
		// preservationLevel.addPreservationLevelRationale();
		preservationLevel.setPreservationLevelDateAssigned(DateParser
				.getIsoDateNoMillis(new Date()));

		// <objectCharacteristics>
		ObjectCharacteristicsComplexType objectCharacteristics = getFile()
				.addNewObjectCharacteristics();
		writeObjectCharacteristics(filePObject, objectCharacteristics);

		// <originalName>
		if (filePObject.getOriginalName() != null) {
			OriginalNameComplexType originalName = getFile()
					.addNewOriginalName();
			originalName.setStringValue(filePObject.getOriginalName());
		}

		// <storage>
		if (filePObject.getContentLocationValue() != null) {

			StorageComplexType storage = getFile().addNewStorage();
			ContentLocationComplexType contentLocation = storage
					.addNewContentLocation();

			contentLocation.setContentLocationType(filePObject
					.getContentLocationType());
			contentLocation.setContentLocationValue(filePObject
					.getContentLocationValue());
		}

	}

	private void readObjectCharacteristics(
			RepresentationFilePreservationObject pObject,
			ObjectCharacteristicsComplexType characteristics) {

		// <compositionLevel>
		pObject.setCompositionLevel(characteristics.getCompositionLevel()
				.intValue());

		// <fixity>
		if (characteristics.getFixityList() != null) {

			List<Fixity> fixities = new ArrayList<Fixity>();

			for (FixityComplexType fixity : characteristics.getFixityList()) {
				fixities.add(new Fixity(fixity.getMessageDigestAlgorithm(),
						fixity.getMessageDigest(), fixity
								.getMessageDigestOriginator()));
			}

			pObject.setFixities(fixities.toArray(new Fixity[fixities.size()]));
		}

		// <size>
		pObject.setSize(characteristics.getSize());

		// <format><formatDesignation>
		if (characteristics.getFormat().getFormatDesignation() != null) {

			FormatDesignationComplexType formatDesignation = characteristics
					.getFormat().getFormatDesignation();

			pObject.setFormatDesignationName(formatDesignation.getFormatName());
			pObject.setFormatDesignationVersion(formatDesignation
					.getFormatVersion());
		}

		// <format><formatRegistry>
		if (characteristics.getFormat().getFormatRegistry() != null) {

			FormatRegistryComplexType formatRegistry = characteristics
					.getFormat().getFormatRegistry();

			pObject.setFormatRegistryName(formatRegistry
					.getFormatRegistryName());
			pObject.setFormatRegistryKey(formatRegistry.getFormatRegistryKey());
			pObject.setFormatRegistryRole(formatRegistry
					.getFormatRegistryRole());
		}

		// <creatingApplication>
		if (characteristics.getCreatingApplicationList() != null
				&& characteristics.getCreatingApplicationList().size() > 0) {

			CreatingApplicationComplexType creatingApplication = characteristics
					.getCreatingApplicationArray(0);

			pObject.setCreatingApplicationName(creatingApplication
					.getCreatingApplicationName());
			pObject.setCreatingApplicationVersion(creatingApplication
					.getCreatingApplicationVersion());

			if (creatingApplication.getDateCreatedByApplication() != null) {
				pObject.setDateCreatedByApplication(creatingApplication
						.getDateCreatedByApplication().toString());
			}

		}

		// <objectCharacteristicsExtension>
		if (characteristics.getObjectCharacteristicsExtension() != null) {
			pObject.setObjectCharacteristicsExtension(characteristics
					.getObjectCharacteristicsExtension().xmlText());
		}

	}

	private void writeObjectCharacteristics(
			RepresentationFilePreservationObject filePObject,
			ObjectCharacteristicsComplexType objectCharacteristics)
			throws PremisMetadataException {

		// <compositionLevel>
		objectCharacteristics.setCompositionLevel(BigInteger
				.valueOf(filePObject.getCompositionLevel()));

		// <fixity>
		if (filePObject.getFixities() != null) {

			for (Fixity fixity : filePObject.getFixities()) {

				FixityComplexType fixityComplexType = objectCharacteristics
						.addNewFixity();

				fixityComplexType.setMessageDigestAlgorithm(fixity
						.getMessageDigestAlgorithm());
				fixityComplexType.setMessageDigest(fixity.getMessageDigest());

				if (!StringUtils.isBlank(fixity.getMessageDigestOriginator())) {
					fixityComplexType.setMessageDigestOriginator(fixity
							.getMessageDigestOriginator());
				}
			}
		}

		// <size>
		objectCharacteristics.setSize(filePObject.getSize());

		// <format>
		FormatComplexType format = objectCharacteristics.addNewFormat();

		// <format><formatDesignation>
		FormatDesignationComplexType formatDesignation = format
				.addNewFormatDesignation();

		formatDesignation.setFormatName(filePObject.getFormatDesignationName());
		if (!StringUtils.isBlank(filePObject.getFormatDesignationVersion())) {
			formatDesignation.setFormatVersion(filePObject
					.getFormatDesignationVersion());
		}

		// <format><formatRegistry>
		if (!StringUtils.isBlank(filePObject.getFormatRegistryName())) {

			FormatRegistryComplexType formatRegistry = format
					.addNewFormatRegistry();

			formatRegistry.setFormatRegistryName(filePObject
					.getFormatRegistryName());

			if (!StringUtils.isBlank(filePObject.getFormatRegistryKey())) {
				formatRegistry.setFormatRegistryKey(filePObject
						.getFormatRegistryKey());
			}

			if (!StringUtils.isBlank(filePObject.getFormatRegistryRole())) {
				formatRegistry.setFormatRegistryRole(filePObject
						.getFormatRegistryRole());
			}
		}

		// <creatingApplication>
		if (!StringUtils.isBlank(filePObject.getCreatingApplicationName())) {

			CreatingApplicationComplexType creatingApplication = objectCharacteristics
					.addNewCreatingApplication();

			creatingApplication.setCreatingApplicationName(filePObject
					.getCreatingApplicationName());

			if (!StringUtils.isBlank(filePObject
					.getCreatingApplicationVersion())) {
				creatingApplication.setCreatingApplicationVersion(filePObject
						.getCreatingApplicationVersion());
			}
		}

		// <objectCharacteristicsExtension>
		if (!StringUtils.isBlank(filePObject
				.getObjectCharacteristicsExtension())) {

			try {
				XmlObject extensionXmlObject = XmlObject.Factory
						.parse(filePObject.getObjectCharacteristicsExtension());

				ExtensionComplexType objectCharacteristicsExtension = objectCharacteristics
						.addNewObjectCharacteristicsExtension();
				objectCharacteristicsExtension.set(extensionXmlObject);

			} catch (XmlException e) {
				throw new PremisMetadataException(
						"objectCharacteristicsExtension is not a valid XML - "
								+ e.getMessage(), e);
			}

		}
	}

	private void setFile(File file) {
		getObjectDocument().setObject(file);
	}

}
