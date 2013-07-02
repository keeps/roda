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
import pt.gov.dgarq.roda.util.StringUtility;
import pt.gov.dgarq.roda.util.TempDir;
import pt.gov.dgarq.roda.util.XmlEncodeUtility;

/**
 * Sound convert abstract class Uses package soundconvert.<br>
 * <br>
 * <strong>Syntax:</strong> soundconverter -b -m audio/x-wav -s .wav F0<br>
 * <br>
 * Possible formats:
 * <ul>
 * <li>audio/ogg</li>
 * <li>audio/flac</li>
 * <li>audio/wav</li>
 * <li>audio/mpeg</li>
 * </ul>
 * 
 * @author Luis Faria
 * @author Rui Castro
 */
public abstract class SoundConverter extends AbstractSynchronousConverter {

	private static final Logger logger = Logger.getLogger(SoundConverter.class);

	private static final String VERSION = "1.0";

	protected String formatExtension;
	protected String soundconverterFormat;

	/**
	 * @throws RODAServiceException
	 */
	public SoundConverter() throws RODAServiceException {
		super();
	}

	/**
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

			try {

				File convertedFileWithExtension = new File(
						originalFile.getPath() + this.formatExtension);

				logger.warn("Using soundconverter-fix script as a workarround for a soundconverter bug that causes soundconverter to fail when running without X");
				String RODA_HOME = null;
				if (System.getProperty("roda.home") != null) {
					RODA_HOME = System.getProperty("roda.home");//$NON-NLS-1$
				} else if (System.getenv("RODA_HOME") != null) {
					RODA_HOME = System.getenv("RODA_HOME"); //$NON-NLS-1$
				} else {
					RODA_HOME = null;
				}

				if (StringUtils.isBlank(RODA_HOME)) {
					throw new ConverterException(
							"RODA_HOME enviroment variable and ${roda.home} system property are not set.");
				}

				File soundconverter = new File(new File(RODA_HOME, "bin"),
						"soundconverter-fix");

				List<String> commandArgs = new ArrayList<String>();
				commandArgs.add(soundconverter.getAbsolutePath());
				commandArgs.add("-b");
				commandArgs.add("-q");
				commandArgs.add("-m");
				commandArgs.add(soundconverterFormat);
				commandArgs.add("-s");
				commandArgs.add(formatExtension);
				commandArgs.add(originalFile.getAbsolutePath());

				String convertOutput = CommandUtility.execute(commandArgs);

				String commandline = StringUtility.join(commandArgs, " "); //$NON-NLS-1$

				String message;
				if (StringUtils.isBlank(convertOutput)) {
					message = String.format(
							"%s: %s => %s%n%s%n%n", localRepresentation //$NON-NLS-1$
									.getRootFile().getId(), localRepresentation
									.getRootFile().getOriginalName(),
							convertedFileWithExtension.getName(), commandline);
				} else {
					message = String
							.format("%s: %s => %s (%s)%n(Command: %s)%n%n", //$NON-NLS-1$
									localRepresentation.getRootFile().getId(),
									localRepresentation.getRootFile()
											.getOriginalName(),
									convertedFileWithExtension.getName(),
									convertOutput, commandline);
				}

				logger.trace(message);
				report.append(message);

				RepresentationFile convertedRootFile = convertedRepresentation
						.getRootFile();
				convertedRootFile.setMimetype(FormatUtility
						.getMimetype(convertedFileWithExtension.getName()));
				convertedRootFile.setOriginalName(convertedRootFile
						.getOriginalName() + this.formatExtension);
				convertedRootFile.setSize(convertedFileWithExtension.length());

				convertedRepresentation.setSubType(convertedRootFile
						.getMimetype());

				FileUtils.moveFile(convertedFileWithExtension, convertedFile);

				moveToFinalDirectory(convertedRepresentation, finalDirectory);

				EventPreservationObject eventPO = new EventPreservationObject();
				eventPO.setOutcome("success");
				eventPO.setOutcomeDetailNote("Converter details"); //$NON-NLS-1$
				String escapedText = XmlEncodeUtility
						.escapeInvalidXmlChars(report.toString());
				logger.debug("Escaped text:" + escapedText);
				eventPO.setOutcomeDetailExtension(escapedText);

				logger.info("Event is " + eventPO);

				return new ConversionResult(convertedRepresentation, eventPO,
						getAgent());

			} catch (CommandException e) {
				logger.debug(
						"Exception executing convert command - "
								+ e.getMessage(), e);
				throw new ConverterException(
						"Exception executing convert command - "
								+ e.getMessage(), e);
			}

		} catch (DownloaderException e) {
			logger.debug(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		} catch (IOException e) {
			logger.debug(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		}

	}

	protected String getVersion() throws ConverterException {
		try {
			String version = getClass().getName() + "/" + VERSION + " - ";
			String soundconverterHelp = CommandUtility.execute(
					"soundconverter", "-h");
			String soundconverterVersion = soundconverterHelp.substring(0,
					soundconverterHelp.indexOf('\n'));

			return version + soundconverterVersion;

		} catch (CommandException e) {
			logger.warn(
					"Exception getting OpenOffice version - " + e.getMessage(),
					e);
			throw new ConverterException(
					"Exception getting OpenOffice version - " + e.getMessage(),
					e);
		}

	}

}
