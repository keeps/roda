package pt.gov.dgarq.roda.ingest.siputility.builders;

import gov.loc.mets.FileType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;
import pt.gov.dgarq.roda.core.metadata.mets.MetsFileAlreadyExistsException;
import pt.gov.dgarq.roda.core.metadata.mets.MetsMetadataException;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationFile;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;

/**
 * Representation builder for digitalized works.
 * 
 * @author Rui Castro
 */
public class DigitalizedWorkRepresentationBuilder extends RepresentationBuilder {
	private static final Logger logger = Logger
			.getLogger(DigitalizedWorkRepresentationBuilder.class);

	/**
	 * Returns the subtype of the given {@link RepresentationObject}.
	 * 
	 * @param rObject
	 *            the {@link RepresentationObject}.
	 * 
	 * @return a {@link String} with the subtype of the given
	 *         {@link RepresentationObject} or <code>null</code> if the subtype
	 *         couldn't be determined.
	 */
	public static String getRepresentationSubtype(RepresentationObject rObject) {
		return getRepresentationSubtype(Arrays.asList(rObject.getPartFiles()));
	}

	/**
	 * Returns the subtype of the given {@link RepresentationObject}.
	 * 
	 * @param partFiles
	 * 
	 * @return a {@link String} with the subtype of the given
	 *         {@link RepresentationObject} or <code>null</code> if the subtype
	 *         couldn't be determined.
	 */
	public static String getRepresentationSubtype(
			List<RepresentationFile> partFiles) {

		String subType = null;

		Set<String> mimetypes = new HashSet<String>();
		if (partFiles != null) {
			for (RepresentationFile partFile : partFiles) {
				mimetypes.add(partFile.getMimetype());
			}
		}

		logger.debug("mimetypes of images are " + mimetypes);

		if (mimetypes.size() > 1) {
			// If we have more than 1 mimetype
			subType = "image/mets+misc";
		} else if (mimetypes.size() == 1) {
			// If we have only 1 mimetype

			// Get the mimetype (image/jpeg)
			String mimetype = new ArrayList<String>(mimetypes).get(0);
			// Get only the second part of the mimetype (jpeg)
			subType = "image/mets+" + mimetype.split("/")[1];
		}

		return subType;
	}

	/**
	 * @see RepresentationBuilder#createRepresentation(List, List)
	 */
	@Override
	public StreamRepresentationObject createRepresentation(
			List<String> imageFilenames, List<InputStream> imageStreams)
			throws SIPException {

		if (imageFilenames == null || imageStreams == null
				|| imageFilenames.size() != imageStreams.size()
				|| imageFilenames.isEmpty()) {
			throw new IllegalArgumentException(
					"filenames and streams cannot be null, empty or in diferent number");
		}

		try {

			// the representation ID
			String newRepID = createNewRepresentationID();

			// Find out the subtype from the mimetypes of the images
			Set<String> mimetypes = new HashSet<String>();
			for (String imageFilename : imageFilenames) {
				mimetypes.add(FormatUtility.getMimetype(imageFilename));
			}
			logger.debug("mimetypes of images are " + mimetypes);

			String subType = null;

			if (mimetypes.size() > 1) {
				// If we have more than 1 mimetype
				subType = "image/mets+misc";
			} else {
				// If we have only 1 mimetype

				// Get the mimetype (image/jpeg)
				String mimetype = new ArrayList<String>(mimetypes).get(0);
				// Get only the second part of the mimetype (jpeg)
				subType = "image/mets+" + mimetype.split("/")[1];
			}

			// The name of the representation root file (METS file)
			String metsFilename = "METS.xml";

			// Create a METS only with the representation ID
			DigitalizedWorkMetsHelper dwMetsHelper = new DigitalizedWorkMetsHelper();
			dwMetsHelper.createRepresentation(newRepID);
			InputStream emptyMetsStream = new ByteArrayInputStream(dwMetsHelper
					.saveToByteArray());

			// Create the representation
			StreamRepresentationObject representation = createRepresentation(
					SIPRepresentationObject.DIGITALIZED_WORK, subType,
					newRepID, metsFilename, emptyMetsStream, imageFilenames,
					imageStreams);

			logger.debug("Represenation created " + representation);

			// Add information about the part files to METS (the representation
			// root file)
			// By default the image files are structured in a flat structure.
			addRepresentationFilesWithFlatStructure(dwMetsHelper,
					representation.getPartStreams());

			// Change the stream and size of the root file (the METS file)
			StreamRepresentationFile metsRootStream = representation
					.getRootStream();
			byte[] metsByteArray = dwMetsHelper.saveToByteArray();
			metsRootStream.setSize(metsByteArray.length);
			metsRootStream.setInputStream(new ByteArrayInputStream(
					metsByteArray));

			// This is not needed. It's the same reference
			// representation.setRootStream(metsRootStream);

			return representation;

		} catch (MetsMetadataException e) {
			logger.warn("Error creating METS with the file structure - "
					+ e.getMessage(), e);
			throw new SIPException(
					"Error creating METS with the file structure - "
							+ e.getMessage(), e);
		} catch (MetsFileAlreadyExistsException e) {
			logger.warn("Error creating METS with the file structure - "
					+ e.getMessage(), e);
			throw new SIPException(
					"Error creating METS with the file structure - "
							+ e.getMessage(), e);
		}

	}

	private void addRepresentationFilesWithFlatStructure(
			DigitalizedWorkMetsHelper dwMetsHelper,
			StreamRepresentationFile[] partStreams)
			throws MetsFileAlreadyExistsException {

		if (partStreams != null) {
			for (StreamRepresentationFile partFile : partStreams) {

				// Create the file in the fileSec
				FileType fileType = dwMetsHelper.createFile(partFile.getId(),
						partFile.getId());

				// Add a reference in the structure div
				dwMetsHelper.createFptr(dwMetsHelper
						.getRepresentationFileStructureDiv(), fileType);
			}

		}

	}

}
