package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.FileFormat;
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
import pt.gov.dgarq.roda.util.StringUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public abstract class EmailConverter extends AbstractSynchronousConverter {

    private static final Logger logger = Logger.getLogger(EmailConverter.class);
    private static final String VERSION = "1.0";
    protected String email2pdfExecutable = null;
    protected String format;
    protected String formatExtension;

    /**
     * @throws RODAServiceException
     */
    public EmailConverter() throws RODAServiceException {
        super();
    }

    /**
     * @throws ConverterException
     * @see SynchronousConverter#convert(RepresentationObject)
     */
    public ConversionResult convert(RepresentationObject representation) throws RepresentationAlreadyConvertedException, InvalidRepresentationException, WrongRepresentationTypeException, WrongRepresentationSubtypeException, ConverterException {
        UUID uuid = UUID.randomUUID();
        File finalDirectory = new File(getCacheDirectory(), uuid.toString());
        StringBuffer report = new StringBuffer();
        LocalRepresentationObject localRepresentation = null;
        try {
            localRepresentation = downloadRepresentationToLocalDisk(representation);
            logger.trace("Representation downloaded " + localRepresentation);
        } catch (DownloaderException e) {
            logger.debug("Exception downloading representation files - " + e.getMessage(), e);
            throw new ConverterException("Exception downloading representation files - " + e.getMessage(), e);
        } catch (IOException e) {
            logger.debug("Exception downloading representation files - " + e.getMessage(), e);
            throw new ConverterException("Exception downloading representation files - " + e.getMessage(), e);
        }

        File tempDirectory = null;
        try {
            tempDirectory = TempDir.createUniqueDirectory("convertedRep");
            logger.debug("Saving converted representation files to " + tempDirectory);
        } catch (IOException e) {
            logger.debug("Error creating directory for converted representation - " + e.getMessage(), e);
            throw new ConverterException("Error creating directory for converted representation - " + e.getMessage(), e);
        }

        LocalRepresentationObject convertedRepresentation = null;
        try {
            // Create a new RepresentationObject that is a copy of source
            // RepresentationObject
            convertedRepresentation = new LocalRepresentationObject(tempDirectory, localRepresentation);

            // Convert Root File
            RepresentationFile convertedRootFile = convertRootFile(localRepresentation.getRootFile(), tempDirectory, report);
            convertedRepresentation.setRootFile(convertedRootFile);
            String subtype = RepresentationBuilder.getRepresentationSubtype(convertedRepresentation);
            convertedRepresentation.setSubType(subtype);
        } catch (IOException e) {
            logger.debug("Error creating directory for converted representation - " + e.getMessage(), e);
            throw new ConverterException("Error creating directory for converted representation - " + e.getMessage(), e);
        } catch (CommandException e) {
            logger.debug("Error converting representation - " + e.getMessage(), e);
            throw new ConverterException("Error converting representation - " + e.getMessage(), e);
        }

        try {
            moveToFinalDirectory(convertedRepresentation, finalDirectory);
        } catch (IOException e) {
            logger.debug("Error moving representation to cache - " + e.getMessage(), e);
            throw new ConverterException("Error moving representation to cache - " + e.getMessage(), e);
        }
        EventPreservationObject eventPO = new EventPreservationObject();
        eventPO.setOutcome("success");
        eventPO.setOutcomeDetailNote("converter outcome details");
        eventPO.setOutcomeDetailExtension(report.toString());
        logger.info("Event is " + eventPO);
        return new ConversionResult(convertedRepresentation, eventPO, getAgent());
    }

    protected String getVersion() throws ConverterException {
        String version = getClass().getName() + "/" + VERSION + " - ";
        return version;
    }

    protected RepresentationFile convertRootFile(RepresentationFile originalRootFile, File tempDirectory, StringBuffer report) throws IOException, CommandException {
        // /tmp/original.../F0
        File originalFile = new File(URI.create(originalRootFile.getAccessURL()));
        // Detect extension by DROID
        String origNameWithExt = originalRootFile.getOriginalName();
        FileFormat detectedFileFormat = FormatUtility.getFileFormat(originalFile, origNameWithExt);
        if ((detectedFileFormat != null) && (detectedFileFormat.getExtensions() != null) && (detectedFileFormat.getExtensions().length > 0)) {
            origNameWithExt = origNameWithExt + "." + detectedFileFormat.getExtensions()[0];
        }
        // /tmp/original.../originalName.eml
        File originalFileWithExtension = new File(originalFile.getParentFile(), origNameWithExt);
        // mv /tmp/original.../F0 /tmp/original.../originalName.eml
        FileUtils.moveFile(originalFile, originalFileWithExtension);
        // /tmp/converted.../F0
        File convertedFile = new File(tempDirectory, originalFile.getName());
        // /tmp/converted.../originalName.eml.pdf
        File convertedFileWithExtension = new File(tempDirectory, originalRootFile.getOriginalName() + this.formatExtension);
        // /tmp/converted.../originalName.eml.log
        File logFileWithExtension = new File(tempDirectory, originalRootFile.getOriginalName() + ".log");


        // Execute the command
        List<String> commandArgs = new ArrayList<String>();
        commandArgs.add(this.email2pdfExecutable);
        commandArgs.add(originalFileWithExtension.getAbsolutePath());
        commandArgs.add(convertedFileWithExtension.getAbsolutePath());
        commandArgs.addAll(Arrays.asList("-c", "pdf", "-sender", "-rcpt", "-cc", "-date", "-subject", "-attach", "-iattach", "-uattach", "-images", "-msg", "-docs", "-dpi", "300", "-pc", "Maximum", "-tiffpi", "wz", "-po", "Portrait", "-ps", "A4", "-verbosity", "error", "-logmode", "overwrite", "-fo", "-combine", "-sort", "name", "-Head:off", "-Foot:off", "-log"));
        commandArgs.add(logFileWithExtension.getAbsolutePath());
        String convertOutput = CommandUtility.execute(commandArgs);
        String commandline = StringUtility.join(commandArgs, " ");
        String message;
        if (StringUtils.isBlank(convertOutput)) {
            message = String.format("%s: %s => %s%n(Command: %s)%n%n", originalRootFile.getId(), originalRootFile.getOriginalName(), convertedFileWithExtension.getName(), commandline);
        } else {
            message = String.format("%s: %s => %s (%s)%n(Command: %s)%n%n", originalRootFile.getId(), originalRootFile.getOriginalName(), convertedFileWithExtension.getName(), convertOutput, commandline);
        }
        logger.trace(message);
        report.append(message);

        // mv /tmp/converted.../originalName.eml.pdf /tmp/converted.../F0
        FileUtils.moveFile(convertedFileWithExtension, convertedFile);

        RepresentationFile convertedRootFile = new RepresentationFile(originalRootFile);

        // convertedRootFile.setMimetype(format);
        convertedRootFile.importFileFormat(FormatUtility.getFileFormat(convertedFile, convertedFileWithExtension.getName()));
        convertedRootFile.setSize(convertedFile.length());
        convertedRootFile.setAccessURL(convertedFile.getAbsolutePath());
        convertedRootFile.setOriginalName(convertedRootFile.getOriginalName() + this.formatExtension);
        return convertedRootFile;
    }
}
