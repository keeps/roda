package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.net.URI;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
import pt.gov.dgarq.roda.migrator.common.data.LocalRepresentationObject;
import pt.gov.dgarq.roda.util.CommandException;

/**
 * @author Rui Castro
 */
public class DW2Tiff extends ImageMagickConverter {

	/**
	 * @throws RODAServiceException
	 */
	public DW2Tiff() throws RODAServiceException {
		super();
		dstFileExtension = ".tiff";
		dstFileFormat = "TIFF";
		options = new String[] { "+compress" };
	}

	@Override
	protected boolean representationNeedsConversion(
			LocalRepresentationObject localRepresentation)
			throws CommandException, WrongRepresentationSubtypeException,
			WrongRepresentationTypeException {

		boolean needsConversion = false;

		if (localRepresentation.getType().equals(
				RepresentationObject.DIGITALIZED_WORK)) {

			if (localRepresentation.getSubType().startsWith("image/mets+")) { //$NON-NLS-1$

				if (localRepresentation.getSubType().equals("image/mets+tiff")) { //$NON-NLS-1$

					// It's METS+TIFF.
					// Only needs conversion if the TIFF is compressed.
					if (localRepresentation.getPartFiles() != null) {

						for (RepresentationFile rFile : localRepresentation
								.getPartFiles()) {

							if (getImageCompression(new File(URI.create(rFile
									.getAccessURL()))) != null) {
								needsConversion = true;
								break;
							}
						}

					} else {
						needsConversion = false;
					}

				} else {

					// It's not METS+TIFF. It needs conversion
					needsConversion = true;

				}

				return needsConversion;

			} else {
				// ERROR. It's not a supported mimetype
				throw new WrongRepresentationSubtypeException(
						localRepresentation.getType()
								+ " is not a supported subtype"); //$NON-NLS-1$
			}

		} else {
			// ERROR. It's not DigitalizedWork
			throw new WrongRepresentationTypeException(localRepresentation
					.getType()
					+ " is not a supported type."); //$NON-NLS-1$
		}

	}

}
