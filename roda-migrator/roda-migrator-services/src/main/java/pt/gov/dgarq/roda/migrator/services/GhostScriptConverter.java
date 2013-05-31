package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
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
 * 
 */
public abstract class GhostScriptConverter extends AbstractSynchronousConverter
		implements SynchronousConverter {
	private static final Logger logger = Logger
			.getLogger(ImageMagickConverter.class);

	private static final String VERSION = "1.0";

	protected String formatExtension;
	protected String device;
	protected String[] options = null;

	/**
	 * @throws RODAServiceException
	 */
	public GhostScriptConverter() throws RODAServiceException {
		super();
	}

	/**
	 * Convert all images to the defined format and options
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
		String report = "";

		try {

			LocalRepresentationObject localRepresentation = downloadRepresentationToLocalDisk(representation);

			logger.trace("Representation downloaded " + localRepresentation);

			File tempDirectory = TempDir.createUniqueDirectory("convertedRep");

			// Create a new RepresentationObject that is a copy of source
			// RepresentationObject
			LocalRepresentationObject convertedRepresentation = new LocalRepresentationObject(
					tempDirectory, localRepresentation);

			logger.debug("Saving converted representation files to "
					+ tempDirectory);

			// Root File
			File originalPdfFile = new File(URI.create(localRepresentation
					.getRootFile().getAccessURL()));

			// Convert root file to images
			convert(originalPdfFile, tempDirectory, "F%d" + formatExtension,
					options);

			// Add new images to representation
			for (File image : tempDirectory.listFiles()) {
				RepresentationFile imagePartFile = new RepresentationFile();
				imagePartFile.setId(image.getName());
				imagePartFile.setAccessURL(image.getAbsolutePath());
				imagePartFile.setMimetype(FormatUtility.getMimetype(image
						.getName()));
				imagePartFile.setSize(image.length());
				convertedRepresentation.addPartFile(imagePartFile);
			}

			File root = new File(tempDirectory, "F0.xml");
			createRootFile(root, tempDirectory);

			RepresentationFile rootFile = new RepresentationFile();
			rootFile.setId(root.getName());
			rootFile.setAccessURL(root.getAbsolutePath());
			rootFile.setMimetype(FormatUtility.getMimetype(root.getName()));
			rootFile.setSize(root.length());
			rootFile.setOriginalName(rootFile.getOriginalName()
					+ formatExtension);
			convertedRepresentation.setRootFile(rootFile);

			moveToFinalDirectory(convertedRepresentation, finalDirectory);

			EventPreservationObject eventPO = new EventPreservationObject();
			eventPO.setOutcome("success");
			eventPO.setOutcomeDetailNote("converter outcome details");
			eventPO.setOutcomeDetailExtension(report);

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
		} catch (CommandException e) {
			logger.debug("Exception converting representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception converting representation files - "
							+ e.getMessage(), e);
		}

	}

	protected abstract void createRootFile(File root, File baseDir)
			throws ConverterException;

	/**
	 * Convert file using current options
	 * 
	 * @param original
	 * @param targetWithExtension
	 * @throws CommandException
	 */
	protected void convert(File original, File targetDir, String format,
			String[] options) throws CommandException {
		List<String> commandArgs = new ArrayList<String>();
		commandArgs.add("gs");
		commandArgs.add("-sDEVICE=" + device);
		commandArgs.addAll(Arrays.asList(options));
		commandArgs.add("-sOutputFile=" + targetDir.getAbsolutePath()
				+ File.separator + format);
		commandArgs.add(original.getAbsolutePath());

		CommandUtility.execute(commandArgs);
	}

	protected String getVersion() throws ConverterException {
		try {
			String version = getClass().getName() + "/" + VERSION + " - ";
			String gsVersion = CommandUtility.execute("gs", "-version");
			if (gsVersion.indexOf('\n') > 0) {
				gsVersion = gsVersion.substring(0, gsVersion.indexOf('\n'));
			}
			return version + gsVersion;

		} catch (CommandException e) {
			logger.warn("Exception getting ImageMagick version - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception getting ImageMagick version - " + e.getMessage(),
					e);
		}

	}
}
