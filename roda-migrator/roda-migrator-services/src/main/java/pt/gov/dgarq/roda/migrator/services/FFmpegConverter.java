package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
import pt.gov.dgarq.roda.util.StringUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Luis Faria
 * @author Rui Castro
 */
public abstract class FFmpegConverter extends AbstractSynchronousConverter {

	private static final Logger logger = Logger
			.getLogger(FFmpegConverter.class);

	private static final String VERSION = "1.0";

	protected String targetExtension;

	/**
	 * @throws RODAServiceException
	 */
	public FFmpegConverter() throws RODAServiceException {
		super();
	}

	/**
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

			// Create a new RepresentationObject that is a copy of source
			// RepresentationObject
			LocalRepresentationObject convertedRepresentation = new LocalRepresentationObject(
					tempDirectory, localRepresentation);

			logger.debug("Saving converted representation files to "
					+ tempDirectory);

			// Root File
			File originalFile = new File(URI.create(localRepresentation
					.getRootFile().getAccessURL()));

			File convertedFile = new File(tempDirectory, originalFile.getName());
			File convertedFileWithExtension = new File(tempDirectory,
					originalFile.getName() + this.targetExtension);

			try {

				List<String> commandArgs = new ArrayList<String>();
				commandArgs.add("ffmpeg"); //$NON-NLS-1$
				commandArgs.add("-i"); //$NON-NLS-1$
				commandArgs.add(originalFile.getAbsolutePath());
				commandArgs.addAll(getOptions(representation));
				commandArgs.add(convertedFileWithExtension.getAbsolutePath());

				String convertOutput = CommandUtility.execute(commandArgs);

				String commandline = StringUtility.join(commandArgs, " "); //$NON-NLS-1$

				String message;
				if (StringUtils.isBlank(convertOutput)) {
					message = String
							.format(
									"%s: %s => %s%n(Command: %s)%n%n", localRepresentation //$NON-NLS-1$
											.getRootFile().getId(),
									localRepresentation.getRootFile()
											.getOriginalName(),
									convertedFileWithExtension.getName(),
									commandline);
				} else {
					message = String
							.format(
									"%s: %s => %s (%s)%n(Command: %s)%n%n", //$NON-NLS-1$
									localRepresentation.getRootFile().getId(),
									localRepresentation.getRootFile()
											.getOriginalName(),
									convertedFileWithExtension.getName(),
									convertOutput, commandline);
				}

				// logger.trace(message);

				// logger.trace("Encoding output...");
				// String encodedMessage = XmlEncodeUtility.encode(message);
				// logger.trace("Encoding done");

				// report.append(encodedMessage);
				report.append(message);

				// Sets the new size and original name
				RepresentationFile rootFile = convertedRepresentation
						.getRootFile();
				rootFile.setSize(convertedFileWithExtension.length());
				rootFile.setOriginalName(rootFile.getOriginalName()
						+ this.targetExtension);
				rootFile.setMimetype(getMimeType());

				convertedRepresentation.setSubType(rootFile.getMimetype());

				FileUtils.moveFile(convertedFileWithExtension, convertedFile);

				moveToFinalDirectory(convertedRepresentation, finalDirectory);

				EventPreservationObject eventPO = new EventPreservationObject();
				eventPO.setOutcome("success");
				eventPO.setOutcomeDetailNote("Converter details"); //$NON-NLS-1$
				eventPO.setOutcomeDetailExtension(report.toString());

				logger.info("Event is " + eventPO);

				return new ConversionResult(convertedRepresentation, eventPO,
						getAgent());

			} catch (CommandException e) {
				logger.debug("Exception executing convert command - "
						+ e.getMessage() + " - exitCode: " + e.getExitCode()
						+ " - output: " + e.getOutput(), e);
				throw new ConverterException(
						"Exception executing convert command - "
								+ e.getMessage() + " - exitCode: "
								+ e.getExitCode() + " - output: "
								+ e.getOutput(), e);
			}

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

	protected abstract String getMimeType();

	protected abstract List<String> getOptions(
			RepresentationObject representation);

	protected String getVersion() throws ConverterException {
		try {
			String version = getClass().getName() + "/" + VERSION + " - ";
			String ffmpeg = CommandUtility.execute("bash", "-c",
					"echo `ffmpeg -version`");
			String ffmpegVersion = ffmpeg.substring(0, ffmpeg.indexOf(','))
					.split(" ")[2];
			return version + ffmpegVersion;

		} catch (CommandException e) {
			logger.warn("Exception getting OpenOffice version - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception getting OpenOffice version - " + e.getMessage(),
					e);
		}
	}
}
