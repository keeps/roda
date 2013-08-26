package pt.gov.dgarq.roda.migrator.services;

import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.MetsDocument.Mets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;
import pt.gov.dgarq.roda.core.metadata.mets.MetsMetadataException;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;
import pt.gov.dgarq.roda.migrator.common.data.LocalRepresentationObject;
import pt.gov.dgarq.roda.util.CommandException;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class DW2FlashPageFlip extends ImageMagickConverter {

    private static final Logger logger = Logger
            .getLogger(DW2FlashPageFlip.class);
    private String[] imageOptions;

    /**
     * @throws RODAServiceException
     */
    public DW2FlashPageFlip() throws RODAServiceException {
        super();
        dstFileExtension = ".jpg";
        dstFileFormat = "JPG";
        imageOptions = new String[]{"-resize", "1280x960"};
    }
    private int width;
    private int height;

    private void createPagesXML(LocalRepresentationObject rep, File pages,
            File baseDir) throws IOException, MetsMetadataException,
            CommandException {

        // Root File - Copy METS with structure
        File originalMetsFile = new File(URI.create(rep.getRootFile()
                .getAccessURL()));

        DigitalizedWorkMetsHelper dwMetsHelper = DigitalizedWorkMetsHelper
                .newInstance(originalMetsFile);

        if (pages.exists()) {
            pages.delete();
        }
        pages.createNewFile();

        String pagesTemplate = new String(StreamUtils
                .getBytes(DW2FlashPageFlip.class.getClassLoader()
                .getResourceAsStream("/Pages.xml")));

        Mets mets = dwMetsHelper.getMets();

        DivType baseDiv = mets.getStructMapList().get(0).getDiv();

        width = 0;
        height = 0;

        String pagesXML = createPageXML(baseDiv, dwMetsHelper, baseDir);

        pagesTemplate = pagesTemplate.replaceAll("\\@WIDTH", "" + width);
        pagesTemplate = pagesTemplate.replaceAll("\\@HEIGHT", "" + height);
        pagesTemplate = pagesTemplate.replaceAll("\\@PAGES", Matcher
                .quoteReplacement(pagesXML));

        PrintWriter printer = new PrintWriter(new FileOutputStream(pages));
        printer.write(pagesTemplate);
        printer.flush();
        printer.close();

    }

    protected String createPageXML(DivType div,
            DigitalizedWorkMetsHelper dwMetsHelper, File baseDir)
            throws CommandException {
        String xml = "";

        // recursive call on sub-divs
        for (DivType divType : div.getDivList()) {
            xml += createPageXML(divType, dwMetsHelper, baseDir);
        }

        // create xml for file list
        for (Fptr fptr : div.getFptrList()) {
            String fileHref = dwMetsHelper.getFileHref(fptr.getFILEID());
            String image = fileHref + dstFileExtension;
            File imageFile = new File(baseDir, image);
            width = Math.max(width, ImageMagickConverter
                    .getImageWidth(imageFile));
            height = Math.max(height, ImageMagickConverter
                    .getImageHeight(imageFile));

            if (image != null) {
                xml += "\t<page src=\"" + image + "\" />\n";
            }
        }

        return xml;
    }

    public ConversionResult convert(RepresentationObject representation)
            throws ConverterException {

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

            // Convert all images
            convertAllImages(convertedRepresentation, tempDirectory, report);

            // Root File - Create SimpleViewer gallery
            RepresentationFile rootFile = convertedRepresentation.getRootFile();
            File pages = new File(tempDirectory, rootFile.getId() + ".xml");

            createPagesXML(convertedRepresentation, pages, tempDirectory);

            rootFile.setAccessURL(pages.toURI().toURL().toString());
            rootFile.setId(rootFile.getId() + ".xml");

            logger.trace("Pages created");

            moveToFinalDirectory(convertedRepresentation, finalDirectory);

            EventPreservationObject eventPO = new EventPreservationObject();
            eventPO.setOutcome("success");
            eventPO.setOutcomeDetailNote("converter outcome details");
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
        } catch (MetsMetadataException e) {
            logger.debug("Exception converting representation files - "
                    + e.getMessage(), e);
            throw new ConverterException(
                    "Exception converting representation files - "
                    + e.getMessage(), e);
        } catch (Throwable t) {
            logger.debug("Exception converting representation files - "
                    + t.getMessage(), t);
            throw new ConverterException(
                    "Exception converting representation files - "
                    + t.getMessage(), t);
        }

    }

    @Override
    protected void convertAllImages(
            LocalRepresentationObject convertedRepresentation,
            File tempDirectory, StringBuffer report) throws ConverterException {
        // Part Files - Convert all images
        for (RepresentationFile partFile : convertedRepresentation
                .getPartFiles()) {

            File originalFile = new File(URI.create(partFile.getAccessURL()));
            File imageFile = new File(tempDirectory, originalFile.getName()
                    + dstFileExtension);

            try {
                convert(originalFile, imageFile, imageOptions);

            } catch (CommandException e) {
                logger.debug("Exception executing convert command - "
                        + e.getMessage(), e);
                throw new ConverterException(
                        "Exception executing convert command - "
                        + e.getMessage(), e);
            }

            partFile.setId(imageFile.getName());
            partFile.setAccessURL(imageFile.getAbsolutePath());
            partFile.importFileFormat(FormatUtility.getFileFormat(imageFile, imageFile.getName()));
            partFile.setSize(imageFile.length());

            convertedRepresentation.setSubType(partFile.getMimetype());
        }
    }

    @Override
    protected boolean representationNeedsConvertion(
            LocalRepresentationObject localRepresentation)
            throws CommandException, WrongRepresentationTypeException,
            WrongRepresentationSubtypeException {
        return true;
    }
}
