package pt.gov.dgarq.roda.ingest.siputility;

import gov.loc.mets.FileType;
import gov.loc.mets.MdSecType;
import gov.loc.mets.MdSecType.MdRef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.metadata.DescriptionObjectValidator;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCHelper;
import pt.gov.dgarq.roda.core.metadata.eadc.EadCMetadataException;
import pt.gov.dgarq.roda.core.metadata.mets.MetsMetadataException;
import pt.gov.dgarq.roda.core.metadata.mets.SIPMetsHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisAgentHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisEventHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisFileObjectHelper;
import pt.gov.dgarq.roda.core.metadata.premis.PremisMetadataException;
import pt.gov.dgarq.roda.core.metadata.premis.PremisRepresentationObjectHelper;
import pt.gov.dgarq.roda.ingest.siputility.data.SIP;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPAgentPreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPDescriptionObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPEventPreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationFilePreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationPreservationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;
import pt.gov.dgarq.roda.util.FileUtility;
import pt.gov.dgarq.roda.util.TempDir;
import pt.gov.dgarq.roda.util.ZipUtility;
import pt.gov.dgarq.roda.x2014.eadcSchema.EadCDocument;

/**
 * Utility to read, write SIPs.
 * 
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class SIPUtility {

	private static final Logger logger = Logger.getLogger(SIPUtility.class);

	/**
	 * Creates a {@link SIP} from a {@link DescriptionObject} and a {@link List}
	 * of {@link StreamRepresentationObject}s.
	 * 
	 * @param parentPID
	 *            the PID of the parent {@link DescriptionObject}.
	 * @param dObject
	 *            the {@link DescriptionObject}.
	 * @param rObjects
	 *            the {@link List} of {@link StreamRepresentationObject}s.
	 * 
	 * @return a {@link SIP}.
	 * 
	 * @throws SIPException
	 */
	public static SIP createSIP(String parentPID, DescriptionObject dObject,
			List<StreamRepresentationObject> rObjects) throws SIPException {

		SIP sip = new SIP();
		sip.setParentPID(parentPID);

		sip.setDescriptionObject(new SIPDescriptionObject(dObject));

		try {

			File sipTemporaryDirectory = TempDir
					.createUniqueTemporaryDirectory("newsip");

			List<SIPRepresentationObject> doROs = saveStreamRepresentationsToDirectory(
					rObjects, sipTemporaryDirectory);

			sip.getDescriptionObject().setRepresentations(doROs);

			if (doROs.size() == 0) {
				logger.debug("No representations present");
				throw new SIPException("No representations present");
			}

			return sip;

		} catch (IOException e) {
			logger.debug("Error creating SIP package - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP package - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Reads a {@link SIP} from a specified directory.
	 * 
	 * @param directory
	 *            the directory with the {@link SIP} contents. This parameter
	 *            cannot be <code>null</code>.
	 * @return the {@link SIP}.
	 * 
	 * @throws SIPException
	 */
	public static SIP readSIP(File directory) throws SIPException {
		return readSIP(directory, false);
	}

	/**
	 * Reads a {@link SIP} from a specified directory and verifies the checksums
	 * of the representation files.
	 * 
	 * @param directory
	 *            the directory with the {@link SIP} contents. This parameter
	 *            cannot be <code>null</code>.
	 * 
	 * @param verifyChecksums
	 *            <code>true</code> to verify checksums.
	 * 
	 * @return the {@link SIP}.
	 * 
	 * @throws SIPException
	 */
	public static SIP readSIP(File directory, boolean verifyChecksums)
			throws SIPException {

		if (!directory.canRead()) {
			throw new SIPException("Can't read the specified directory "
					+ directory);
		} else {

			try {
				SIP sip = new SIP();

				// Read METS.xml
				File fileMets = new File(directory, "METS.xml");

				SIPMetsHelper metsHelper = SIPMetsHelper.newInstance(fileMets);

				logger.trace("SIP METS parsed successfully - " + fileMets);

				SIPDescriptionObject parentDO = readSIPDescriptionObject(
						metsHelper, directory, metsHelper.getParentDmdSec(),
						verifyChecksums);

				logger.trace("Parent DO parentPID/ID is " + parentDO.getId());

				String[] parentPIDAndID = splitParentPIDAndID(parentDO.getId());

				sip.setParentPID(parentPIDAndID[0]);
				logger.trace("SIP parentPID is " + sip.getParentPID());

				parentDO.setId(parentPIDAndID[1]);
				logger.trace("Parent DO ID is " + parentDO.getId());

				sip.setDescriptionObject(parentDO);

				sip.setDirectory(directory);
				logger.trace("SIP directory set to " + directory);

				return sip;

			} catch (IOException e) {
				logger.debug("METS.xml parser error - " + e.getMessage(), e);
				throw new InvalidSIPLocationException(
						"METS.xml parser error - " + e.getMessage(), e);
			} catch (EadCMetadataException e) {
				logger.debug(
						"Error reading descriptive metadata - "
								+ e.getMessage(), e);
				throw new SIPException("Error reading descriptive metadata - "
						+ e.getMessage(), e);
			} catch (MetsMetadataException e) {
				logger.debug(
						"Error reading SIP METS metadata - " + e.getMessage(),
						e);
				throw new SIPException("Error reading SIP METS metadata - "
						+ e.getMessage(), e);
			} catch (PremisMetadataException e) {
				logger.debug(
						"Error reading preservation metadata - "
								+ e.getMessage(), e);
				throw new SIPException("Error reading preservation metadata - "
						+ e.getMessage(), e);
			}
		}
	}

	/**
	 * Writes a {@link SIP} to a specified directory.
	 * 
	 * @param sip
	 *            the {@link SIP} to write.
	 * @param directory
	 *            the directory where to write the {@link SIP}.
	 * 
	 * @return the {@link SIP}
	 * 
	 * @throws SIPException
	 */
	public static SIP writeSIP(SIP sip, File directory) throws SIPException {

		try {

			SIP newSIP = writeSIP(sip);

			if (directory.exists()) {
				try {

					FileUtils.deleteDirectory(directory);

				} catch (IOException e) {
					logger.debug(
							"Destination directory already exists and cannot be deleted - "
									+ e.getMessage(), e);
					throw new SIPException(
							"Destination directory already exists and cannot be deleted - "
									+ e.getMessage(), e);
				}
			}

			moveSIPToDirectory(newSIP, directory);

			logger.debug("Created SIP in " + directory);

			return newSIP;

		} catch (IOException e) {
			logger.debug(
					"Error writting SIP to the specified directory - "
							+ e.getMessage(), e);
			throw new SIPException(
					"Error writting SIP to the specified directory - "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Writes a {@link SIP} package (a ZIP file) to the specified {@link File}.
	 * 
	 * @param sip
	 *            the {@link SIP} to package.
	 * @param sipFile
	 *            the {@link File} to write the {@link SIP} package.
	 * 
	 * @throws SIPException
	 */
	public static void writeSIPPackage(SIP sip, File sipFile)
			throws SIPException {

		SIP newSIP = writeSIP(sip);

		try {

			File zipFile = ZipUtility.createZIPFile(sipFile,
					newSIP.getDirectory());
			logger.debug("Created ZIP file in " + zipFile);

		} catch (IOException e) {
			logger.debug("Error creating SIP package - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP package - "
					+ e.getMessage(), e);
		}

	}

	/**
	 * Writes a {@link SIP} package (a ZIP file) and writes the contents to the
	 * given {@link OutputStream}.
	 * 
	 * @param sip
	 *            the {@link SIP} to package.
	 * @param sipOutputStream
	 *            the {@link OutputStream} to write the {@link SIP} package.
	 * 
	 * @throws SIPException
	 */
	public static void writeSIPPackage(SIP sip, OutputStream sipOutputStream)
			throws SIPException {
		try {

			File tempFile = File.createTempFile("temp", "sip");

			writeSIPPackage(sip, tempFile);

			FileInputStream inputStream = new FileInputStream(tempFile);

			IOUtils.copy(inputStream, sipOutputStream);

			inputStream.close();
			tempFile.delete();

		} catch (IOException e) {
			logger.debug("Error creating SIP - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP - " + e.getMessage(), e);
		}
	}

	/**
	 * Validates the given {@link SIP}.
	 * 
	 * @param sip
	 *            the {@link SIP} to validate.
	 * 
	 * @throws SIPException
	 * @throws InvalidDescriptionObjectException
	 */
	public static void validateForSaving(SIP sip) throws SIPException,
			InvalidDescriptionObjectException {
		validateSIP(sip, false);
	}

	/**
	 * Validates the given {@link SIP}.
	 * 
	 * @param sip
	 *            the {@link SIP} to validate.
	 * 
	 * @throws SIPException
	 * @throws InvalidDescriptionObjectException
	 */
	public static void validateForIngest(SIP sip) throws SIPException,
			InvalidDescriptionObjectException {
		validateSIP(sip, true);
	}

	/**
	 * Validates the given {@link SIP}.
	 * 
	 * @param sip
	 *            the {@link SIP} to validate.
	 * @param verifyRepresentations
	 * 
	 * @throws SIPException
	 * @throws InvalidDescriptionObjectException
	 */
	public static void validateSIP(SIP sip, boolean verifyRepresentations)
			throws SIPException, InvalidDescriptionObjectException {

		if (sip.getDescriptionObject() == null) {

			throw new SIPException("No description object");

		} else {

			validateDescriptionObjectTree2(sip.getDescriptionObject());

		}

		if (verifyRepresentations) {

			List<SIPRepresentationObject> sipROs = getRepresentationObjects(sip);
			if (sipROs.size() == 0) {
				throw new SIPException("No representation objects");
			}

			for (SIPRepresentationObject sipRO : sipROs) {
				if (sipRO.getRootFile() == null) {
					throw new SIPException("Representation " + sipRO.getId()
							+ " doesn't have a root file");
				}
			}

		}

	}

	/**
	 * Get the {@link SIPDescriptionObject} with the given ID or
	 * <code>null</code> if it doesn't exist.
	 * 
	 * @param sip
	 *            the {@link SIP} where to look for the
	 *            {@link SIPDescriptionObject}.
	 * @param doID
	 *            the ID of the {@link SIPDescriptionObject}.
	 * 
	 * @return a {@link SIPDescriptionObject} or <code>null</code> if it doesn't
	 *         exist
	 */
	public static SIPDescriptionObject getDescriptionObjectWithID(SIP sip,
			String doID) {
		if (sip == null) {
			return null;
		} else {
			return getDescriptionObjectWithID(sip.getDescriptionObject(), doID);
		}
	}

	/**
	 * Get the {@link SIPDescriptionObject} with the given ID or
	 * <code>null</code> if it doesn't exist.
	 * 
	 * @param sipDO
	 *            the {@link SIPDescriptionObject} where to look for the
	 *            {@link SIPDescriptionObject}.
	 * @param doID
	 *            the ID of the {@link SIPDescriptionObject}.
	 * 
	 * @return a {@link SIPDescriptionObject} or <code>null</code> if it doesn't
	 *         exist
	 */
	public static SIPDescriptionObject getDescriptionObjectWithID(
			SIPDescriptionObject sipDO, String doID) {

		SIPDescriptionObject matchSipDO = null;

		for (SIPDescriptionObject possibleSipDO : getDescriptionObjects(sipDO)) {
			if (possibleSipDO.getId().equals(doID)) {
				matchSipDO = possibleSipDO;
				break;
			}
		}

		return matchSipDO;
	}

	/**
	 * Returns the given {@link SIPDescriptionObject} and all it's descendants
	 * in a {@link List}.
	 * 
	 * @param sipDO
	 *            the parent {@link SIPDescriptionObject}.
	 * 
	 * @return {@link List} of {@link SIPDescriptionObject}.
	 */
	public static List<SIPDescriptionObject> getDescriptionObjects(
			SIPDescriptionObject sipDO) {

		List<SIPDescriptionObject> sipDOs = new ArrayList<SIPDescriptionObject>();

		if (sipDO != null) {

			sipDOs.add(sipDO);

			for (SIPDescriptionObject childDO : sipDO.getChildren()) {
				sipDOs.addAll(getDescriptionObjects(childDO));
			}
		}

		return sipDOs;
	}

	/**
	 * Returns all {@link SIPRepresentationObject}s inside a {@link SIP}.
	 * 
	 * @param sip
	 *            the {@link SIP}.
	 * 
	 * @return {@link List} of {@link SIPRepresentationObject}.
	 */
	public static List<SIPRepresentationObject> getRepresentationObjects(SIP sip) {

		List<SIPRepresentationObject> sipROs = new ArrayList<SIPRepresentationObject>();

		for (SIPDescriptionObject sipDO : getDescriptionObjects(sip
				.getDescriptionObject())) {
			sipROs.addAll(sipDO.getRepresentations());
		}

		return sipROs;
	}

	/**
	 * Saves a {@link List} of {@link StreamRepresentationObject}s to a
	 * directory.
	 * 
	 * @param streamROs
	 *            the {@link List} of {@link StreamRepresentationObject}s.
	 * @param directory
	 *            the directory.
	 * 
	 * @return a {@link List} of {@link SIPRepresentationObject}s written to the
	 *         directory.
	 * 
	 * @throws IOException
	 */
	public static List<SIPRepresentationObject> saveStreamRepresentationsToDirectory(
			List<StreamRepresentationObject> streamROs, File directory)
			throws IOException {

		List<SIPRepresentationObject> repFiles = new ArrayList<SIPRepresentationObject>();

		for (StreamRepresentationObject rObject : streamROs) {

			File rObjectDirectory = TempDir.createTemporaryDirectory(
					rObject.getId(), directory);
			repFiles.add(saveStreamRepresentationToDirectory(rObject,
					rObjectDirectory));

		}

		return repFiles;
	}

	/**
	 * Saves a {@link StreamRepresentationObject} to a directory.
	 * 
	 * @param streamRO
	 *            the {@link StreamRepresentationObject} to save.
	 * @param directory
	 *            the directory.
	 * 
	 * @return the {@link SIPRepresentationObject} written to the directory.
	 * 
	 * @throws IOException
	 */
	public static SIPRepresentationObject saveStreamRepresentationToDirectory(
			StreamRepresentationObject streamRO, File directory)
			throws IOException {

		SIPRepresentationObject rObject = new SIPRepresentationObject();
		rObject.setId(streamRO.getId());
		rObject.setType(streamRO.getType());
		rObject.setSubType(streamRO.getSubType());
		rObject.setStatuses(streamRO.getStatuses());
		rObject.setDescriptionObjectPID(streamRO.getDescriptionObjectPID());

		StreamRepresentationFile rootStream = streamRO.getRootStream();

		// Write root file and create the root RepresentationFile.
		File tempFile = new File(directory, streamRO.getRootFile().getId());
		int numOfBytes = IOUtils.copy(rootStream.getInputStream(),
				new FileOutputStream(tempFile));
		rObject.setRootFile(new RepresentationFile(rootStream.getId(),
				rootStream.getOriginalName(), numOfBytes,
				fileToURLString(tempFile), rootStream.exportFileFormat()));

		// Write part files and create the part RepresentationFiles.
		List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>();
		for (StreamRepresentationFile rStream : streamRO.getPartStreams()) {
			tempFile = new File(directory, rStream.getId());
			numOfBytes = IOUtils.copy(rStream.getInputStream(),
					new FileOutputStream(tempFile));
			partFiles.add(new RepresentationFile(rStream.getId(), rStream
					.getOriginalName(), numOfBytes, fileToURLString(tempFile),
					rStream.exportFileFormat()));
		}
		rObject.setPartFiles(partFiles.toArray(new RepresentationFile[partFiles
				.size()]));

		return rObject;
	}

	private static void validateDescriptionObjectTree(SIPDescriptionObject sipDO)
			throws SIPException, EadCMetadataException {

		EadCDocument eadCDocument = new EadCHelper(sipDO).getEadcDocument();

		XmlOptions validateOptions = new XmlOptions();
		List<XmlError> errorList = new ArrayList<XmlError>();
		validateOptions.setErrorListener(errorList);

		if (!eadCDocument.validate(validateOptions)) {
			throw new EadCMetadataException(errorList.toString());
		}

		// Verify representations have at root file
		for (SIPRepresentationObject sipRO : sipDO.getRepresentations()) {
			if (StringUtils.isBlank(sipRO.getId())) {
				throw new SIPException("Representation doesn't have an ID");
			}
			if (sipRO.getRootFile() == null) {
				throw new SIPException("Representation with ID "
						+ sipRO.getId() + " of DO " + sipDO.getId()
						+ " doesn't have a RootFile");
			}
		}

		// Validate child DOs
		for (SIPDescriptionObject childDO : sipDO.getChildren()) {
			validateDescriptionObjectTree(childDO);
		}

	}

	private static void validateDescriptionObjectTree2(
			SIPDescriptionObject sipDO)
			throws InvalidDescriptionObjectException {

		DescriptionObjectValidator.validateDescriptionObject(sipDO);

		// Validate child DOs
		for (SIPDescriptionObject childDO : sipDO.getChildren()) {
			validateDescriptionObjectTree2(childDO);
		}

	}

	private static SIPDescriptionObject readSIPDescriptionObject(
			SIPMetsHelper metsHelper, File directory, MdSecType dmdSec,
			boolean verifyChecksums) throws EadCMetadataException,
			FileNotFoundException, IOException, MetsMetadataException,
			ChecksumVerificationFailedException, PremisMetadataException {

		String doHref = metsHelper.getDmdSecHref(dmdSec);
		File doFile = new File(directory, doHref);

		if (verifyChecksums) {
			checkMdRefChecksum(directory, dmdSec.getMdRef());
		}

		SIPDescriptionObject sipDO = new SIPDescriptionObject(EadCHelper
				.newInstance(doFile).getDescriptionObject());
		sipDO.setFile(doFile);

		logger.debug("Description object " + sipDO.getId()
				+ " read successfully from " + doFile);

		// Read representations for this dmdSec
		List<RepresentationObject> rObjects = metsHelper
				.getRepresentations(dmdSec.getID());

		if (logger.isDebugEnabled()) {
			for (RepresentationObject ro : rObjects) {

				logger.debug("RO " + ro.getId() + " files:");

				logger.debug("RO " + ro.getId() + " rootFile "
						+ ro.getRootFile().getId());

				if (ro.getPartFiles() != null) {
					for (RepresentationFile rPartFile : ro.getPartFiles()) {
						logger.debug("RO " + ro.getId() + " partFile "
								+ rPartFile.getId());
					}
				}
			}
		}

		if (rObjects.size() == 0) {

			logger.debug("Description object " + sipDO.getId() + "(dmdSec/@ID="
					+ dmdSec.getID() + ")" + " doesn't have representations.");

		} else {

			logger.debug("Description object " + sipDO.getId() + " has "
					+ rObjects.size() + " representation(s).");

			for (RepresentationObject rObject : rObjects) {
				logger.trace("Checking files of representation " + rObject);

				// Check representation file absolute paths and sizes

				// Root file
				checkRepresentationFile(rObject.getRootFile(), directory,
						verifyChecksums, rObject, metsHelper);
				logger.trace("Checked root file " + rObject.getRootFile());

				// Part files
				for (RepresentationFile rFile : rObject.getPartFiles()) {
					checkRepresentationFile(rFile, directory, verifyChecksums,
							rObject, metsHelper);
					logger.trace("Checked part file " + rFile);
				}

				SIPRepresentationObject sipRO = new SIPRepresentationObject(
						rObject);
				File representationDirectory = new File(URI.create(rObject
						.getRootFile().getAccessURL())).getParentFile();
				sipRO.setDirectory(representationDirectory);

				sipDO.addRepresentation(sipRO);

				logger.debug("Added representation " + sipRO.getId()
						+ " to description object " + sipDO.getId());
			}

		}

		loadRepresentationPreservationObjects(metsHelper, directory,
				sipDO.getRepresentations(), verifyChecksums);

		// Recursively read the children DOs
		for (MdSecType child : metsHelper.getChildrenDmdSecs(dmdSec)) {
			sipDO.addChild(readSIPDescriptionObject(metsHelper, directory,
					child, verifyChecksums));
		}

		return sipDO;
	}

	/**
	 * Writes a {@link SIP} to a temporary directory in the system default
	 * temporary directory.
	 * 
	 * @param sip
	 *            the {@link SIP} to write.
	 * 
	 * @return the new {@link SIP}
	 * 
	 * @throws SIPException
	 */
	private static SIP writeSIP(SIP sip) throws SIPException {

		try {
			File sipTemporaryDirectory = TempDir
					.createUniqueTemporaryDirectory("newsip");

			logger.debug("Creating SIP into temp dir "
					+ sipTemporaryDirectory.getAbsolutePath());

			SIPDescriptionObject parentDO = sip.getDescriptionObject();
			String parentDOID = parentDO.getId();

			SIPDescriptionObject parentDOWithPID = new SIPDescriptionObject(
					parentDO);

			// Sets the id of parent DO to PID/ID (for saving only)
			parentDOWithPID.setId(sip.getParentPID() + "/" + parentDOID);

			// Write the Description objects to disk
			writeDescriptionObjectToDirectory(parentDOWithPID,
					sipTemporaryDirectory);

			// Restore the representations
			parentDO.setRepresentations(parentDOWithPID.getRepresentations());
			// Restore the original ID (without the parent PID).
			// parentDO.setId(parentDOID);
			// Set file of the saved parentDO to the original parentDO
			parentDO.setFile(parentDOWithPID.getFile());

			sip.setDirectory(sipTemporaryDirectory);

			File metsFile = new File(sipTemporaryDirectory, "METS.xml");
			writeSIPMets(sip, metsFile);

			logger.debug("Wrote SIP METS to " + metsFile);

			return sip;

		} catch (IOException e) {
			logger.debug("Error creating SIP - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP - " + e.getMessage(), e);
		} catch (EadCMetadataException e) {
			logger.debug("Error creating SIP - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP - " + e.getMessage(), e);
		} catch (PremisMetadataException e) {
			logger.debug("Error creating SIP - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP - " + e.getMessage(), e);
		} catch (MetsMetadataException e) {
			logger.debug("Error creating SIP - " + e.getMessage(), e);
			throw new SIPException("Error creating SIP - " + e.getMessage(), e);
		}
	}

	private static SIPDescriptionObject writeDescriptionObjectToDirectory(
			SIPDescriptionObject sipDO, File directory) throws IOException,
			EadCMetadataException, PremisMetadataException {

		// Write this DO to a file
		File eadcFile = File.createTempFile("eadc", null, directory);
		new EadCHelper(sipDO).saveToFile(eadcFile);

		sipDO.setFile(eadcFile);

		logger.debug("EAD-C " + sipDO.getId() + " saved to " + eadcFile);

		// Write all it's representations
		writeRepresentationsToDirectory(sipDO.getRepresentations(), directory);

		// Write RPOs
		List<SIPRepresentationPreservationObject> sipRPOs = getRepresentationPreservationObjects(sipDO);
		if (sipRPOs != null) {
			for (SIPRepresentationPreservationObject sipRPO : sipRPOs) {

				String roID = null;
				if (sipRPO.getRepresentationObject() != null) {
					roID = sipRPO.getRepresentationObject().getId();
				} else {
					roID = sipRPO.getID();
				}

				File roDirectory = new File(directory, roID);
				if (!roDirectory.exists()) {

					FileUtils.forceMkdir(roDirectory);
					logger.debug("Created directory " + roDirectory
							+ " for representation " + roID);
				}

				SIPRepresentationPreservationObject rpo = writeRepresentationPreservationObjectToDirectory(
						sipRPO, roDirectory);
			}
		}

		// Recursively write it's children
		for (SIPDescriptionObject childDO : sipDO.getChildren()) {
			writeDescriptionObjectToDirectory(childDO, directory);
		}

		return sipDO;
	}

	private static List<SIPRepresentationObject> writeRepresentationsToDirectory(
			List<SIPRepresentationObject> sipROs, File sipTemporaryDirectory)
			throws IOException, PremisMetadataException {

		for (SIPRepresentationObject sipRO : sipROs) {

			File roDirectory = new File(sipTemporaryDirectory, sipRO.getId());
			FileUtils.forceMkdir(roDirectory);

			logger.debug("Created directory " + roDirectory
					+ " for representation " + sipRO.getId());

			writeRepresentationToDirectory(sipRO, roDirectory);
		}

		return sipROs;
	}

	private static SIPRepresentationObject writeRepresentationToDirectory(
			SIPRepresentationObject rObject, File repDirectory)
			throws FileNotFoundException, IOException, PremisMetadataException {

		// Write root file and create the root RepresentationFile.
		RepresentationFile rootFile = rObject.getRootFile();
		File tempFile = new File(repDirectory, rootFile.getId());

		FileUtils.copyFile(urlStringToFile(rootFile.getAccessURL()), tempFile);

		logger.debug("RO file " + rootFile.getId() + " copied to " + tempFile);

		rootFile.setSize(tempFile.length());
		rootFile.setAccessURL(fileToURLString(tempFile));

		// Write part files and create the part RepresentationFiles.
		if (rObject.getPartFiles() != null) {
			for (RepresentationFile partFile : rObject.getPartFiles()) {

				tempFile = new File(repDirectory, partFile.getId());

				FileUtils.copyFile(urlStringToFile(partFile.getAccessURL()),
						tempFile);

				logger.debug("RO file " + partFile.getId() + " copied to "
						+ tempFile);

				partFile.setSize(tempFile.length());
				partFile.setAccessURL(fileToURLString(tempFile));
			}
		}

		rObject.setDirectory(repDirectory);

		return rObject;
	}

	private static List<SIPRepresentationPreservationObject> getRepresentationPreservationObjects(
			SIPDescriptionObject sipDO) {

		List<SIPRepresentationPreservationObject> sipRPOs = new ArrayList<SIPRepresentationPreservationObject>();

		if (sipDO.getRepresentations() != null) {

			for (SIPRepresentationObject sipRO : sipDO.getRepresentations()) {

				SIPRepresentationPreservationObject sipRPO = sipRO
						.getPreservationObject();

				while (sipRPO != null) {

					if (!listContainsRPO(sipRPOs, sipRPO)) {
						sipRPOs.add(sipRPO);
					}

					sipRPO = sipRPO.getDerivedFromRepresentationObject();
				}

			}
		}

		return sipRPOs;
	}

	private static boolean listContainsRPO(
			List<SIPRepresentationPreservationObject> sipRPOs,
			SIPRepresentationPreservationObject sipRPO) {

		boolean contains = false;

		for (SIPRepresentationPreservationObject rpo : sipRPOs) {
			if (rpo.getID().equals(sipRPO.getID())) {
				contains = true;
				break;
			}
		}

		return contains;
	}

	private static SIPRepresentationPreservationObject writeRepresentationPreservationObjectToDirectory(
			SIPRepresentationPreservationObject sipRPO, File repDirectory)
			throws PremisMetadataException, IOException {

		// Write this RPO to a file
		File rpoPremisFile = new File(repDirectory, sipRPO.getID()
				+ ".premis.xml");
		new PremisRepresentationObjectHelper(sipRPO).saveToFile(rpoPremisFile);

		sipRPO.setPremisFile(rpoPremisFile);

		logger.debug("PREMIS representation " + sipRPO.getID() + " written to "
				+ rpoPremisFile);

		// Write root file
		if (sipRPO.getRootFile() != null) {

			SIPRepresentationFilePreservationObject sipRPORootFile = (SIPRepresentationFilePreservationObject) sipRPO
					.getRootFile();

			// Write this RFPO to a file
			File rfpoPremisRootFile = new File(repDirectory,
					sipRPORootFile.getID() + ".premis.xml");
			new PremisFileObjectHelper(sipRPORootFile)
					.saveToFile(rfpoPremisRootFile);

			sipRPORootFile.setPremisFile(rfpoPremisRootFile);

			logger.debug("PREMIS file " + sipRPORootFile.getID()
					+ " written to " + rfpoPremisRootFile);
		}

		// Write part files
		if (sipRPO.getPartFiles() != null) {
			for (SIPRepresentationFilePreservationObject sipRFPO : (SIPRepresentationFilePreservationObject[]) sipRPO
					.getPartFiles()) {

				// Write this RFPO to a file
				File rfpoPremisPartFile = new File(repDirectory,
						sipRFPO.getID() + ".premis.xml");
				new PremisFileObjectHelper(sipRFPO)
						.saveToFile(rfpoPremisPartFile);

				sipRFPO.setPremisFile(rfpoPremisPartFile);

				logger.debug("PREMIS file " + sipRFPO.getID() + " written to "
						+ rfpoPremisPartFile);
			}
		}

		if (sipRPO.getPreservationEvents() != null) {

			for (SIPEventPreservationObject sipEPO : sipRPO
					.getPreservationEvents()) {

				// Write this EPO to a file
				File epoPremisFile = new File(repDirectory, sipEPO.getID()
						+ ".premis.xml");
				new PremisEventHelper(sipEPO).saveToFile(epoPremisFile);

				sipEPO.setPremisFile(epoPremisFile);

				logger.debug("PREMIS event " + sipEPO.getID() + " written to "
						+ epoPremisFile);

				if (sipEPO.getAgent() != null) {
					SIPAgentPreservationObject sipAPO = sipEPO.getAgent();

					// Write this APO to a file
					File apoPremisFile = new File(repDirectory.getParentFile(),
							sipAPO.getID() + ".premis.xml");
					new PremisAgentHelper(sipAPO).saveToFile(apoPremisFile);

					sipAPO.setPremisFile(apoPremisFile);

					logger.debug("PREMIS agent " + sipAPO.getID()
							+ " written to " + apoPremisFile);
				}

			}

		}

		return sipRPO;
	}

	private static SIP moveSIPToDirectory(SIP sip, File directory)
			throws IOException {

		logger.debug("SIP before move " + sip);

		try {

			FileUtils.moveDirectory(sip.getDirectory(), directory);

		} catch (IOException e) {

			logger.warn("Exception moving SIP to directory " + directory);
			logger.warn("Copying and leaving temporary directopy behind");

			FileUtils.copyDirectory(sip.getDirectory(), directory,
					TrueFileFilter.TRUE);
		}

		sip.setDirectory(directory);

		logger.debug("SIP after move " + sip);

		return sip;
	}

	private static void writeSIPMets(SIP sip, File metsFile)
			throws IOException, MetsMetadataException {

		SIPMetsHelper sipMetsHelper = new SIPMetsHelper();

		sipMetsHelper.setCreator("RODA Common SIP Utility");

		writeSIPMetsDescriptionObjects(sipMetsHelper, null,
				sip.getDescriptionObject());

		sipMetsHelper.saveToFile(metsFile);
	}

	private static void writeSIPMetsDescriptionObjects(
			SIPMetsHelper sipMetsHelper, MdSecType parentDmdSec,
			SIPDescriptionObject sipDO) throws MetsMetadataException {

		MdSecType dmdSec = sipMetsHelper.addDescriptionObjectFile(parentDmdSec,
				sipDO.getId(), sipDO.getContentModel(), sipDO.getFile());

		sipMetsHelper
				.addRepresentations(
						dmdSec,
						new ArrayList<RepresentationObject>(sipDO
								.getRepresentations()));

		writeSIPMetsPreservationObjects(sipMetsHelper,
				getRepresentationPreservationObjects(sipDO));

		for (SIPDescriptionObject childDO : sipDO.getChildren()) {
			writeSIPMetsDescriptionObjects(sipMetsHelper, dmdSec, childDO);
		}

	}

	private static void writeSIPMetsPreservationObjects(
			SIPMetsHelper sipMetsHelper,
			List<SIPRepresentationPreservationObject> sipRPOs)
			throws MetsMetadataException {

		for (SIPRepresentationPreservationObject sipRPO : sipRPOs) {

			sipMetsHelper.addRepresentationPreservationObject(sipRPO,
					sipRPO.getPremisFile(), sipRPO.getRepresentationObject());

			if (sipRPO.getRootFile() != null) {

				SIPRepresentationFilePreservationObject rootFile = (SIPRepresentationFilePreservationObject) sipRPO
						.getRootFile();

				sipMetsHelper.addRepresentationFilePreservationObject(sipRPO,
						rootFile, rootFile.getPremisFile());
			}

			if (sipRPO.getPartFiles() != null) {

				for (RepresentationFilePreservationObject rfpo : sipRPO
						.getPartFiles()) {

					SIPRepresentationFilePreservationObject partFile = (SIPRepresentationFilePreservationObject) rfpo;

					sipMetsHelper.addRepresentationFilePreservationObject(
							sipRPO, partFile, partFile.getPremisFile());
				}

			}

			if (sipRPO.getPreservationEvents() != null) {

				for (SIPEventPreservationObject sipEPO : sipRPO
						.getPreservationEvents()) {

					sipMetsHelper.addEventPreservationObject(sipRPO, sipEPO,
							sipEPO.getPremisFile());

					try {

						sipMetsHelper.addAgentPreservationObject(sipEPO
								.getAgent(), sipEPO.getAgent().getPremisFile());

					} catch (PremisMetadataException e) {
						logger.warn(
								"Error adding AgentPO to SIP METS "
										+ e.getMessage(), e);
					}

				}
			}
		}

	}

	/**
	 * Verifies if the provided file has the correct checksum (compared with
	 * metadata value). If permissive mode is on, it will only depend on the
	 * checksum value (if checksumtype isn't available (if it is it will be
	 * used) MD5 will be used instead). If permissive mode is off, it will
	 * depend on both checksum and checksumtype or an exception will be thrown.
	 * 
	 * @param fileType
	 *            METS file object
	 * @param file
	 *            file to be checked
	 * @param permissive
	 *            if true, only checksum value is required (if checksumtype is
	 *            available it will be used, otherwise MD5 will be instead). if
	 *            false, both checksum and checksumtype should be provided or an
	 *            exception will be thrown.
	 * 
	 * @exception ChecksumVerificationFailedException
	 * */
	public static void verifyFileChecksum(FileType fileType, File file,
			boolean permissive) throws ChecksumVerificationFailedException {
		String checksumtype = "MD5";

		if (permissive) {
			if (StringUtils.isBlank(fileType.getCHECKSUM())) {
				throw new ChecksumVerificationFailedException(
						"File element must have valid, non-empty, @CHECKSUM attribute.");
			}
		} else {
			if (StringUtils.isBlank(fileType.getCHECKSUM())
					&& fileType.getCHECKSUMTYPE() == null) {
				throw new ChecksumVerificationFailedException(
						"File element must have valid, non-empty, @CHECKSUM, @CHECKSUMTYPE attributes.");
			}
		}
		if (fileType.getCHECKSUMTYPE() != null) {
			checksumtype = fileType.getCHECKSUMTYPE().toString();
		}

		try {
			String hash = FileUtility
					.calculateChecksumInHex(file, checksumtype);
			if (hash.equalsIgnoreCase(fileType.getCHECKSUM())) {
				// OK
				logger.debug(checksumtype + " checksum of file "
						+ fileType.getID() + ": OK");
			} else {
				throw new ChecksumVerificationFailedException(
						"File "
								+ fileType.getID()
								+ " checksum doesn't match METS checksum value (digestAlgorithm: "
								+ checksumtype + ").");
			}
		} catch (NoSuchAlgorithmException e) {
			throw new ChecksumVerificationFailedException("Checksum type "
					+ checksumtype + " is not supported - " + e.getMessage(), e);
		} catch (IOException e) {
			throw new ChecksumVerificationFailedException(
					"Input/Output error performing verification - "
							+ e.getMessage(), e);
		}
	}

	public static void checkRepresentationFile(RepresentationFile rFile,
			File directory, boolean verifyChecksum,
			RepresentationObject rObject, SIPMetsHelper metsHelper)
			throws MalformedURLException, ChecksumVerificationFailedException {

		File file = new File(directory, rFile.getAccessURL());
		rFile.setAccessURL(fileToURLString(file));
		rFile.setSize(file.length());

		if (verifyChecksum) {

			FileType fileType = metsHelper.getFile(rObject.getId(),
					rFile.getId());

			verifyFileChecksum(fileType, file, true);

		}

	}

	private static void checkMdRefChecksum(File directory, MdRef mdRef)
			throws ChecksumVerificationFailedException {

		String href = mdRef.getHref();
		File file = new File(directory, href);

		if (mdRef.getCHECKSUMTYPE() == null
				|| StringUtils.isBlank(mdRef.getCHECKSUM())) {

			// throw new ChecksumVerificationFailedException(
			// "mdRef element must have valid, non-empty, @CHECKSUM, @CHECKSUMTYPE attributes.");
			logger.warn("mdRef for '"
					+ mdRef.getHref()
					+ "' element should have valid, non-empty, @CHECKSUM, @CHECKSUMTYPE attributes.");

		} else {

			try {
				String hash = FileUtility.calculateChecksumInHex(file, mdRef
						.getCHECKSUMTYPE().toString());

				if (hash.equalsIgnoreCase(mdRef.getCHECKSUM())) {
					// OK
					logger.debug(mdRef.getCHECKSUMTYPE().toString()
							+ " checksum of file " + mdRef.getHref() + ": OK");
				} else {
					throw new ChecksumVerificationFailedException("File "
							+ mdRef.getHref()
							+ " checksum doesn't match METS checksum value.");
				}

			} catch (NoSuchAlgorithmException e) {
				throw new ChecksumVerificationFailedException("Checksum type "
						+ mdRef.getCHECKSUMTYPE().toString()
						+ " is not supported - " + e.getMessage(), e);
			} catch (IOException e) {
				throw new ChecksumVerificationFailedException(
						"Input/Output error performing verification - "
								+ e.getMessage(), e);
			}

		}

	}

	private static void loadRepresentationPreservationObjects(
			SIPMetsHelper metsHelper, File directory,
			List<SIPRepresentationObject> representations,
			boolean verifyChecksums)
			throws ChecksumVerificationFailedException,
			PremisMetadataException, FileNotFoundException, IOException {

		Map<File, SIPRepresentationPreservationObject> rpos = new HashMap<File, SIPRepresentationPreservationObject>();
		Map<File, SIPEventPreservationObject> epos = new HashMap<File, SIPEventPreservationObject>();
		Map<File, SIPAgentPreservationObject> apos = new HashMap<File, SIPAgentPreservationObject>();

		// for each representation, get the preservation object
		for (SIPRepresentationObject representation : representations) {

			String rpoMdSecID = metsHelper
					.getRepresentationPreservationObjectMdSecID(representation
							.getId());

			if (rpoMdSecID != null) {

				SIPRepresentationPreservationObject sipRPO = getRepresentationPreservationObject(
						metsHelper, directory, rpoMdSecID, verifyChecksums,
						rpos, epos, apos);

				representation.setPreservationObject(sipRPO);

			} else {
				// Representation doesn't have preservation metadata
			}

		}

		// Get derivation events & representations
		for (SIPRepresentationPreservationObject rpo : rpos.values()) {

			SIPEventPreservationObject derivationEPO = getRepresentationDerivationEvent(
					metsHelper, directory, representations, rpo,
					verifyChecksums, rpos, epos, apos);

			if (derivationEPO != null) {
				rpo.setDerivationEvent(derivationEPO);
				rpo.setDerivedFromRepresentationObject(derivationEPO
						.getTarget());
			}

		}

	}

	protected static SIPEventPreservationObject getRepresentationDerivationEvent(
			SIPMetsHelper metsHelper, File directory,
			List<SIPRepresentationObject> representations,
			SIPRepresentationPreservationObject rpo, boolean verifyChecksums,
			Map<File, SIPRepresentationPreservationObject> rpos,
			Map<File, SIPEventPreservationObject> epos,
			Map<File, SIPAgentPreservationObject> apos)
			throws ChecksumVerificationFailedException,
			PremisMetadataException, FileNotFoundException, IOException {

		SIPEventPreservationObject derivationEPO = null;

		String rpoMdSecID = null;
		for (SIPRepresentationObject ro : representations) {
			if (ro.getPreservationObject() == rpo) {
				rpoMdSecID = metsHelper
						.getRepresentationPreservationObjectMdSecID(ro.getId());
			}
		}

		MdSecType eventMdSec = metsHelper
				.getRepresentationDerivationEventMdSec(rpoMdSecID);

		derivationEPO = getEventPreservationObject(metsHelper, directory,
				eventMdSec.getID(), verifyChecksums, epos, apos);

		// Get the target RPO
		List<String> admIDList = eventMdSec.getADMID();
		if (admIDList != null) {

			if (admIDList.size() == 2) {

				// The second ID is the target representation
				String targetRpoMdSecID = admIDList.get(1);

				SIPRepresentationPreservationObject targetRPO = getRepresentationPreservationObject(
						metsHelper, directory, targetRpoMdSecID,
						verifyChecksums, rpos, epos, apos);

				derivationEPO.setTarget(targetRPO);

			} else {
				logger.warn("digiprovMD '"
						+ eventMdSec.getID()
						+ "' has attribute @AMDID with "
						+ admIDList.size()
						+ " values."
						+ "Event digiprovMD must have 2 values in @AMDID, the agent and the representation");
			}
		} else {
			logger.warn("digiprovMD '" + eventMdSec.getID()
					+ "' doesn't have attribute @AMDID!!!");
		}

		return derivationEPO;
	}

	protected static SIPRepresentationPreservationObject getRepresentationPreservationObject(
			SIPMetsHelper metsHelper, File directory, String rpoMdSecID,
			boolean verifyChecksums,
			Map<File, SIPRepresentationPreservationObject> rpos,
			Map<File, SIPEventPreservationObject> epos,
			Map<File, SIPAgentPreservationObject> apos)
			throws ChecksumVerificationFailedException,
			PremisMetadataException, FileNotFoundException, IOException {

		SIPRepresentationPreservationObject sipRPO = null;

		MdSecType rpoMdSec = metsHelper
				.getRepresentationPreservationObjectMdSec(rpoMdSecID);

		if (rpoMdSec == null) {
			logger.warn("Representation digiprovMD '" + rpoMdSecID
					+ "' doesn't exist");
		} else {
			if (rpoMdSec.getMdRef() == null) {

				logger.warn("Representation digiprovMD '" + rpoMdSec.getID()
						+ "' doesn't have an mdRef element");

			} else {

				MdRef rpoMdRef = rpoMdSec.getMdRef();

				if (verifyChecksums) {
					checkMdRefChecksum(directory, rpoMdRef);
				}

				// Get the File, joining the directory with the METS href
				File rpoFile = new File(directory, rpoMdRef.getHref());

				if (!rpos.containsKey(rpoFile)) {

					// Parse the PREMIS file
					PremisRepresentationObjectHelper premisROHelper = PremisRepresentationObjectHelper
							.newInstance(rpoFile);

					// Use the PremisRepresentationObjectHelper to get the
					// RepresentationPreservationObject
					RepresentationPreservationObject rpo = premisROHelper
							.getRepresentationPreservationObject();

					RepresentationFilePreservationObject rootFile = rpo
							.getRootFile();
					RepresentationFilePreservationObject[] partFiles = rpo
							.getPartFiles();

					// If the root file is null, take the first part file and
					// make it root
					// if (rootFile == null) {
					// if (partFiles == null || partFiles.length == 0) {
					// // This is a real problem! A representation without
					// // files
					// throw new PremisMetadataException(
					// "Representation preservation object doesn't have files");
					// } else {
					// rootFile = partFiles[0];
					// // partFiles = Arrays.copyOfRange(partFiles, 1,
					// // partFiles.length);
					// RepresentationFilePreservationObject[] newPartFiles = new
					// RepresentationFilePreservationObject[partFiles.length -
					// 1];
					// for (int i = 1; i < partFiles.length; i++) {
					// newPartFiles[i - 1] = partFiles[i];
					// }
					// partFiles = newPartFiles;
					//
					// logger
					// .warn("RPO didn't had a root file. First part file '"
					// + rootFile.getID()
					// + "' was made root file");
					// }
					// }

					SIPRepresentationFilePreservationObject[] newPartFiles = new SIPRepresentationFilePreservationObject[partFiles.length];

					// Get RFPO's hrefs from SIP METS
					List<String> fpoHrefs = metsHelper
							.getRepresentationFilePreservationObjectHrefs(rpoMdSecID);

					logger.debug("RPO " + rpo.getID() + " has "
							+ fpoHrefs.size() + " RFPOs");

					for (String fpoHref : fpoHrefs) {

						// Get the File, joining the directory with the METS
						// href
						File fpoFile = new File(directory, fpoHref);

						// Parse the PREMIS file
						PremisFileObjectHelper premisFOHelper = PremisFileObjectHelper
								.newInstance(fpoFile);

						// Use the PremisFileObjectHelper to get the
						// RepresentationFilePreservationObject
						RepresentationFilePreservationObject fpo = premisFOHelper
								.getRepresentationFilePreservationObject();

						if (rootFile != null) {

							if (fpo.getID().equals(rootFile.getID())) {

								SIPRepresentationFilePreservationObject sipFPO = new SIPRepresentationFilePreservationObject(
										fpo);
								sipFPO.setPremisFile(fpoFile);

								rpo.setRootFile(sipFPO);

							}
						}

						if (partFiles != null) {

							for (int position = 0; position < partFiles.length; position++) {
								if (partFiles[position].getID().equals(
										fpo.getID())) {

									SIPRepresentationFilePreservationObject sipFPO = new SIPRepresentationFilePreservationObject(
											fpo);
									sipFPO.setPremisFile(fpoFile);

									newPartFiles[position] = sipFPO;
								}
							}

							rpo.setPartFiles(newPartFiles);
						}

					}

					// Constructs a SIPRepresentationPreservationObject from the
					// RepresentationPreservationObject
					sipRPO = new SIPRepresentationPreservationObject(rpo);
					sipRPO.setPremisFile(rpoFile);

					// Get preservation events
					List<SIPEventPreservationObject> preservationEvents = getPreservationEvents(
							metsHelper, directory, rpoMdSecID, verifyChecksums,
							epos, apos);

					sipRPO.setPreservationEvents(preservationEvents);

					for (SIPEventPreservationObject preservationEvent : preservationEvents) {
						preservationEvent.setTarget(sipRPO);
					}

					rpos.put(rpoFile, sipRPO);
				}

				sipRPO = rpos.get(rpoFile);
			}
		}

		return sipRPO;
	}

	private static List<SIPEventPreservationObject> getPreservationEvents(
			SIPMetsHelper metsHelper, File directory, String rpoMdSecID,
			boolean verifyChecksums,
			Map<File, SIPEventPreservationObject> epos,
			Map<File, SIPAgentPreservationObject> apos)
			throws ChecksumVerificationFailedException,
			PremisMetadataException, FileNotFoundException, IOException {

		List<SIPEventPreservationObject> events = new ArrayList<SIPEventPreservationObject>();

		List<MdSecType> eventMdSecs = metsHelper.getEventMdSecs(rpoMdSecID);

		for (MdSecType eventMdSec : eventMdSecs) {

			SIPEventPreservationObject sipEventPO = getEventPreservationObject(
					metsHelper, directory, eventMdSec.getID(), verifyChecksums,
					epos, apos);

			events.add(sipEventPO);
		}

		return events;
	}

	private static SIPEventPreservationObject getEventPreservationObject(
			SIPMetsHelper metsHelper, File directory, String eventMdSecID,
			boolean verifyChecksums,
			Map<File, SIPEventPreservationObject> epos,
			Map<File, SIPAgentPreservationObject> apos)
			throws ChecksumVerificationFailedException,
			PremisMetadataException, FileNotFoundException, IOException {

		SIPEventPreservationObject sipEventPO = null;

		MdSecType eventMdSec = metsHelper.getEventMdSec(eventMdSecID);

		if (eventMdSec.getMdRef() == null) {

			logger.warn("digiprovMD '" + eventMdSec.getID()
					+ "' doesn't have an mdRef element");

		} else {

			MdRef eventMdRef = eventMdSec.getMdRef();

			// Get the File, joining the directory with the METS href
			File eventFile = new File(directory, eventMdRef.getHref());

			if (!epos.containsKey(eventFile)) {

				if (verifyChecksums) {
					checkMdRefChecksum(directory, eventMdRef);
				}

				// Parse the PREMIS file
				PremisEventHelper premisEventHelper = PremisEventHelper
						.newInstance(eventFile);

				// Use the PremisEventHelper to get the
				// RepresentationFilePreservationObject
				EventPreservationObject eventPO = premisEventHelper
						.getEventPreservationObject();

				logger.debug("eventPO " + eventPO);

				sipEventPO = new SIPEventPreservationObject(eventPO);
				sipEventPO.setPremisFile(eventFile);
				epos.put(eventFile, sipEventPO);

				logger.debug("sipEventPO " + sipEventPO);

				// Get the agent
				List<String> admIDList = eventMdSec.getADMID();
				if (admIDList != null) {

					if (admIDList.size() == 2) {

						// The first ID is the agent
						String agentMdSecID = admIDList.get(0);

						SIPAgentPreservationObject agent = getAgentPreservationObject(
								metsHelper, directory, agentMdSecID,
								verifyChecksums, apos);
						sipEventPO.setAgent(agent);

					} else {
						logger.warn("digiprovMD '"
								+ eventMdSec.getID()
								+ "' has attribute @AMDID with "
								+ admIDList.size()
								+ " values."
								+ "Event digiprovMD must have 2 values in @AMDID, the representation and the agent");
					}
				} else {
					logger.warn("digiprovMD '" + eventMdSec.getID()
							+ "' doesn't have attribute @AMDID!!!");
				}
			}

			sipEventPO = epos.get(eventFile);
		}

		return sipEventPO;
	}

	private static SIPAgentPreservationObject getAgentPreservationObject(
			SIPMetsHelper metsHelper, File directory, String agentMdSecID,
			boolean verifyChecksums, Map<File, SIPAgentPreservationObject> apos)
			throws ChecksumVerificationFailedException,
			PremisMetadataException, FileNotFoundException, IOException {

		SIPAgentPreservationObject sipAgentPO = null;

		MdSecType agentMdSec = metsHelper.getAgentMdSec(agentMdSecID);

		if (agentMdSec == null) {
			logger.warn("Agent digiprovMD '" + agentMdSecID + "' doesn't exist");
		} else {
			if (agentMdSec.getMdRef() == null) {

				logger.warn("Agent digiprovMD '" + agentMdSec.getID()
						+ "' doesn't have an mdRef element");

			} else {

				MdRef agentMdRef = agentMdSec.getMdRef();

				if (verifyChecksums) {
					checkMdRefChecksum(directory, agentMdRef);
				}

				// Get the File, joining the directory with the METS href
				File agentFile = new File(directory, agentMdRef.getHref());

				if (!apos.containsKey(agentFile)) {

					// Parse the PREMIS file
					PremisAgentHelper premisAgentHelper = PremisAgentHelper
							.newInstance(agentFile);

					// Use the PremisAgentHelper to get the
					// AgentPreservationObject
					AgentPreservationObject agentPO = premisAgentHelper
							.getAgentPreservationObject();

					sipAgentPO = new SIPAgentPreservationObject(agentPO);
					sipAgentPO.setPremisFile(agentFile);

					apos.put(agentFile, sipAgentPO);
				}

				sipAgentPO = apos.get(agentFile);
			}
		}

		return sipAgentPO;
	}

	private static String[] splitParentPIDAndID(String id) {

		String[] parentPIDAndID = new String[2];
		String[] parentPIDAndIDTemp = id.split("/");

		if (parentPIDAndIDTemp.length == 2) {
			parentPIDAndID = parentPIDAndIDTemp;
		} else if (parentPIDAndIDTemp.length > 2) {
			logger.warn("<unitid> inside SIP should be in the form of parentPID/id. Found "
					+ id);
			parentPIDAndID[0] = parentPIDAndIDTemp[0];
			parentPIDAndID[1] = parentPIDAndIDTemp[1];
		} else {
			logger.warn("<unitid> inside SIP should be in the form of parentPID/id. Found "
					+ id);
			parentPIDAndID[0] = null;
			parentPIDAndID[1] = id;
		}

		return parentPIDAndID;
	}

	private static String fileToURLString(File file)
			throws MalformedURLException {
		return file.toURI().toURL().toString();
	}

	private static File urlStringToFile(String url) {
		return new File(URI.create(url));
	}
}
