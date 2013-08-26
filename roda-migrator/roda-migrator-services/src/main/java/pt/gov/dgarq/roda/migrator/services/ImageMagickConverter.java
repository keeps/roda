package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
 * Converts images files using the Image Magick package
 *
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public abstract class ImageMagickConverter extends AbstractSynchronousConverter
        implements SynchronousConverter {

    private static final Logger logger = Logger
            .getLogger(ImageMagickConverter.class);
    private static final String VERSION = "1.0"; //$NON-NLS-1$
    protected String dstFileExtension;
    protected String dstFileFormat;
    protected String[] options = null;

    /**
     * @throws RODAServiceException
     */
    public ImageMagickConverter() throws RODAServiceException {
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

        StringBuffer report = new StringBuffer();

        LocalRepresentationObject localRepresentation;
        try {

            localRepresentation = downloadRepresentationToLocalDisk(representation);

            logger.trace("Representation downloaded " + localRepresentation); //$NON-NLS-1$

        } catch (DownloaderException e) {
            logger.debug("Exception downloading representation files - " //$NON-NLS-1$
                    + e.getMessage(), e);
            throw new ConverterException(
                    "Exception downloading representation files - " //$NON-NLS-1$
                    + e.getMessage(), e);
        } catch (IOException e) {
            logger.debug("Exception downloading representation files - " //$NON-NLS-1$
                    + e.getMessage(), e);
            throw new ConverterException(
                    "Exception downloading representation files - " //$NON-NLS-1$
                    + e.getMessage(), e);
        }

        File tempDirectory;
        try {

            tempDirectory = TempDir.createUniqueDirectory("convertedRep"); //$NON-NLS-1$

        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
            throw new ConverterException(e.getMessage(), e);
        }

        try {

            if (!representationNeedsConvertion(localRepresentation)) {
                throw new RepresentationAlreadyConvertedException(
                        "Representation is already in the destination format"); //$NON-NLS-1$
            }

        } catch (CommandException e) {
            logger.debug("Exception inspecting representation files - " //$NON-NLS-1$
                    + e.getMessage(), e);
            throw new ConverterException(
                    "Exception inspecting representation files - " //$NON-NLS-1$
                    + e.getMessage(), e);
        }

        // Create a new RepresentationObject that is a copy of source
        // RepresentationObject
        LocalRepresentationObject convertedRepresentation = new LocalRepresentationObject(
                tempDirectory, localRepresentation);

        logger.debug("Saving converted representation files to " //$NON-NLS-1$
                + tempDirectory);

        // Root File - Copy METS with structure
        File originalMetsFile = new File(URI.create(localRepresentation
                .getRootFile().getAccessURL()));
        File copiedMetsFile = new File(tempDirectory, originalMetsFile
                .getName());

        try {

            FileUtils.copyFile(originalMetsFile, copiedMetsFile);

        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
            throw new ConverterException(e.getMessage(), e);
        }

        try {

            convertedRepresentation.getRootFile().setAccessURL(
                    copiedMetsFile.toURI().toURL().toString());

        } catch (MalformedURLException e) {
            logger.debug(e.getMessage(), e);
            throw new ConverterException(e.getMessage(), e);
        }

        String message1 = String.format(
                "%s: %2$s => %2$s (Copied without change)", localRepresentation //$NON-NLS-1$
                .getRootFile().getId(), localRepresentation
                .getRootFile().getOriginalName());

        logger.trace(message1);
        report.append(message1 + "\n"); //$NON-NLS-1$

        convertAllImages(convertedRepresentation, tempDirectory, report);

        try {

            moveToFinalDirectory(convertedRepresentation, finalDirectory);

        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
            throw new ConverterException(e.getMessage(), e);
        }

        EventPreservationObject eventPO = new EventPreservationObject();
        eventPO.setOutcome("success"); //$NON-NLS-1$
        eventPO.setOutcomeDetailNote("Converter details"); //$NON-NLS-1$
        eventPO.setOutcomeDetailExtension(report.toString());

        logger.info("Event is " + eventPO); //$NON-NLS-1$

        return new ConversionResult(convertedRepresentation, eventPO,
                getAgent());

    }

    abstract protected boolean representationNeedsConvertion(
            LocalRepresentationObject localRepresentation)
            throws CommandException, WrongRepresentationTypeException,
            WrongRepresentationSubtypeException;

    protected void convertAllImages(
            LocalRepresentationObject convertedRepresentation,
            File tempDirectory, StringBuffer report) throws ConverterException {

        // Part Files - Convert all images
        for (RepresentationFile partFile : convertedRepresentation
                .getPartFiles()) {

            File originalFile = new File(URI.create(partFile.getAccessURL()));
            File convertedFile = new File(tempDirectory, originalFile.getName());

            try {

                String originalName = partFile.getOriginalName();
                String originalFileExtension = originalName.substring(
                        originalName.lastIndexOf('.') + 1, originalName
                        .length());

                File originalFileWithExtension = new File(originalFile + "."
                        + originalFileExtension);

                FileUtils.moveFile(originalFile, originalFileWithExtension);

                File convertedFileWithExtension = new File(tempDirectory,
                        originalFile.getName() + this.dstFileExtension);

                List<String> commandArgs = new ArrayList<String>();
                commandArgs.add("convert"); //$NON-NLS-1$
                commandArgs.add(originalFileWithExtension.getAbsolutePath());
                commandArgs.addAll(Arrays.asList(options));
                commandArgs.add(convertedFileWithExtension.getAbsolutePath());

                String convertOutput = CommandUtility.execute(commandArgs);

                String commandline = StringUtility.join(commandArgs, " "); //$NON-NLS-1$

                String message;
                if (StringUtils.isBlank(convertOutput)) {
                    message = String.format(
                            "%s: %s => %s%n(Command: %s)%n%n", //$NON-NLS-1$
                            partFile.getId(), partFile.getOriginalName(),
                            convertedFileWithExtension.getName(), commandline);
                } else {
                    message = String.format(
                            "%s: %s => %s (%s)%n(Command: %s)%n%n", partFile //$NON-NLS-1$
                            .getId(), partFile.getOriginalName(),
                            convertedFileWithExtension.getName(),
                            convertOutput, commandline);
                }

                logger.trace(message);
                report.append(message);

                FileUtils.moveFile(convertedFileWithExtension, convertedFile);

            } catch (CommandException e) {
                logger.debug("Exception executing convert command - " //$NON-NLS-1$
                        + e.getMessage(), e);
                throw new ConverterException(
                        "Exception executing convert command - " //$NON-NLS-1$
                        + e.getMessage(), e);
            } catch (IOException e) {
                logger.debug("Exception moving file - " + e.getMessage(), e); //$NON-NLS-1$
                throw new ConverterException("Exception moving file - " //$NON-NLS-1$
                        + e.getMessage(), e);
            }

            partFile.setAccessURL(convertedFile.getAbsolutePath());
            partFile.importFileFormat(FormatUtility.getFileFormat(convertedFile, convertedFile.getName()));
            partFile.setSize(convertedFile.length());
            partFile.setOriginalName(partFile.getOriginalName()
                    + this.dstFileExtension);

            String subtype = RepresentationBuilder
                    .getRepresentationSubtype(convertedRepresentation);

            convertedRepresentation.setSubType(subtype);
        }
    }

    /**
     * Convert file using current options
     *
     * @param original
     * @param targetWithExtension
     * @throws CommandException
     */
    protected String convert(File original, File targetWithExtension,
            String[] options) throws CommandException {

        List<String> commandArgs = new ArrayList<String>();
        commandArgs.add("convert"); //$NON-NLS-1$
        commandArgs.add(original.getAbsolutePath());
        commandArgs.addAll(Arrays.asList(options));
        commandArgs.add(targetWithExtension.getAbsolutePath());

        return CommandUtility.execute(commandArgs);
    }

    protected String getVersion() throws ConverterException {
        try {

            // $> "convert -version"
            // Version: ImageMagick 6.3.7 02/19/08 Q16
            // http://www.imagemagick.org
            // Copyright: Copyright (C) 1999-2008 ImageMagick Studio LLC
            //
            // We want the words "ImageMagick 6.3.7" from the first line.

            String imageMagickVersion = CommandUtility.execute("convert",
                    "-version");
            // Get the first line
            String[] versionLines = imageMagickVersion.split("\n", 1);

            if (versionLines.length > 0) {

                String[] versionLiveWords = versionLines[0].split(" ");

                // Get the second and third word
                if (versionLiveWords.length > 3) {
                    imageMagickVersion = versionLiveWords[1] + " " //$NON-NLS-1$
                            + versionLiveWords[2];
                }
            }

            return getClass().getName() + "/" + VERSION + " - " //$NON-NLS-1$//$NON-NLS-2$
                    + imageMagickVersion;

        } catch (CommandException e) {
            logger.warn("Exception getting ImageMagick version - " //$NON-NLS-1$
                    + e.getMessage(), e);
            throw new ConverterException(
                    "Exception getting ImageMagick version - " + e.getMessage(), //$NON-NLS-1$
                    e);
        }

    }

    /**
     * Get image width
     *
     * @param image the image file
     * @return the width
     * @throws CommandException
     */
    public static int getImageWidth(File image) throws CommandException {
        String width = CommandUtility.execute(new String[]{"identify", //$NON-NLS-1$
            "-format", "%w", image.getAbsolutePath()}); //$NON-NLS-1$//$NON-NLS-2$
        // remove trailing new line
        width = width.substring(0, width.length() - 1);
        return Integer.valueOf(width).intValue();
    }

    /**
     * Get image height
     *
     * @param image the image file
     * @return the height
     * @throws CommandException
     */
    public static int getImageHeight(File image) throws CommandException {
        String height = CommandUtility.execute(new String[]{"identify", //$NON-NLS-1$
            "-format", "%h", image.getAbsolutePath()}); //$NON-NLS-1$//$NON-NLS-2$
        // remove trailing new line
        height = height.substring(0, height.length() - 1);
        return Integer.valueOf(height).intValue();
    }

    /**
     * Get image compression or
     * <code>null</code> if the image is not compressed.
     *
     * @param image the image file
     *
     * @return the compression.
     *
     * @throws CommandException
     */
    public static String getImageCompression(File image)
            throws CommandException {
        String compression = CommandUtility.execute(new String[]{"identify", //$NON-NLS-1$
            "-format", "%C", image.getAbsolutePath()}); //$NON-NLS-1$ //$NON-NLS-2$
        // remove trailing new line
        compression = compression.substring(0, compression.length() - 1);

        if ("None".equals(compression)) { //$NON-NLS-1$
            compression = null;
        }

        return compression;
    }
}
