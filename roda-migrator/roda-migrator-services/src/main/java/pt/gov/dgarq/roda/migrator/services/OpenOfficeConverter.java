package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.InvalidRepresentationException;
import pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;
import pt.gov.dgarq.roda.migrator.common.data.LocalRepresentationObject;
import pt.gov.dgarq.roda.util.CommandException;
import pt.gov.dgarq.roda.util.CommandUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Luis Faria
 * @author Rui Castro
 */
public abstract class OpenOfficeConverter extends AbstractSynchronousConverter {

	private static final Logger logger = Logger
			.getLogger(OpenOfficeConverter.class);

	private static final String VERSION = "1.0";

	protected String format;
	protected String formatExtension;

	protected JodConverter converter;

	/**
	 * @throws RODAServiceException
	 */
	public OpenOfficeConverter() throws RODAServiceException {
		super();

		try {

			converter = new JodConverter();

		} catch (ConnectException e) {
			logger.error("Error creating JodConverter - " + e.getMessage(), e);
			throw new RODAServiceException("Error creating JodConverter - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * 
	 * 
	 * @throws ConverterException
	 * 
	 * @see SynchronousConverter#convert(RepresentationObject)
	 */
	public ConversionResult convert(RepresentationObject representation)
			throws RepresentationAlreadyConvertedException,
			InvalidRepresentationException, WrongRepresentationTypeException,
			WrongRepresentationSubtypeException, ConverterException {

		UUID uuid = UUID.randomUUID();
		File finalDirectory = new File(getCacheDirectory(), uuid.toString());

		StringBuffer report = new StringBuffer();

		try {

			LocalRepresentationObject localRepresentation = downloadRepresentationToLocalDisk(representation);

			logger.trace("Representation downloaded " + localRepresentation);

			File tempDirectory = TempDir.createUniqueDirectory("convertedRep");

			logger.debug("Saving converted representation files to "
					+ tempDirectory);

			// Create a new RepresentationObject that is a copy of source
			// RepresentationObject
			LocalRepresentationObject convertedRepresentation = new LocalRepresentationObject(
					tempDirectory, localRepresentation);

			// Convert Root File
			RepresentationFile convertedRootFile = convertRootFile(
					localRepresentation.getRootFile(), tempDirectory, report);

			convertedRepresentation.setRootFile(convertedRootFile);

			String subtype = RepresentationBuilder
					.getRepresentationSubtype(convertedRepresentation);
			convertedRepresentation.setSubType(subtype);

			moveToFinalDirectory(convertedRepresentation, finalDirectory);

			EventPreservationObject eventPO = new EventPreservationObject();
			eventPO.setOutcome("success");
			eventPO.setOutcomeDetailNote("Converter details"); //$NON-NLS-1$
			eventPO.setOutcomeDetailExtension(report.toString());

			logger.info("Event is " + eventPO);

			return new ConversionResult(convertedRepresentation, eventPO,
					getAgent());

		} catch (DownloaderException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		} catch (IOException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		}

	}

	protected String getVersion() throws ConverterException {
		try {
			String version = getClass().getName() + "/" + VERSION + " - ";
			String sofficeHelp = CommandUtility.execute("soffice", "-h");
			String sofficeVersion = "";
			if (sofficeHelp.indexOf('\n') > 0) {
				sofficeVersion = sofficeHelp.substring(0, sofficeHelp
						.indexOf('\n'));
			} else {
				sofficeVersion = sofficeHelp;
			}
			return version + sofficeVersion;

		} catch (CommandException e) {
			logger.warn("Exception getting OpenOffice version - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception getting OpenOffice version - " + e.getMessage(),
					e);
		}

	}

	protected RepresentationFile convertRootFile(
			RepresentationFile originalRootFile, File tempDirectory,
			StringBuffer report) throws IOException {

		File originalFile = new File(URI
				.create(originalRootFile.getAccessURL()));
		File convertedFile = new File(tempDirectory, originalFile.getName());

		FileInputStream originalFileInputStream = new FileInputStream(
				originalFile);
		converter.convert(originalFileInputStream, originalRootFile
				.getMimetype(), new FileOutputStream(convertedFile), format);
		originalFileInputStream.close();

		RepresentationFile convertedRootFile = new RepresentationFile(
				originalRootFile);

		// convertedRootFile.setMimetype(format);
		convertedRootFile.setMimetype(FormatUtility.getMimetype(convertedFile
				.getName()
				+ this.formatExtension));
		convertedRootFile.setSize(convertedFile.length());
		convertedRootFile.setAccessURL(convertedFile.getAbsolutePath());
		convertedRootFile.setOriginalName(convertedRootFile.getOriginalName()
				+ this.formatExtension);

		String message = String.format("%s: %s => %s%n%n", originalRootFile
				.getId(), originalRootFile.getOriginalName(), convertedRootFile
				.getOriginalName());

		logger.trace(message);
		report.append(message);

		return convertedRootFile;
	}

}
