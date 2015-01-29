package pt.gov.dgarq.roda.migrator.services;

import gov.loc.mets.DivType;
import gov.loc.mets.DivType.Fptr;
import gov.loc.mets.MetsDocument.Mets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;

import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.metadata.mets.DigitalizedWorkMetsHelper;
import pt.gov.dgarq.roda.core.metadata.mets.MetsMetadataException;
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
public class DW2SimpleViewer extends ImageMagickConverter {

    private static final Logger logger = Logger
            .getLogger(DW2SimpleViewer.class);
    private String[] imageOptions;
    private String[] thumbOptions;

    /**
     * @throws RODAServiceException
     */
    public DW2SimpleViewer() throws RODAServiceException {
        super();
        dstFileExtension = ".jpg";
        dstFileFormat = "JPG";
        imageOptions = new String[]{"-resize", "960x720", "-interlace",
            "none", "-background", "white", "-flatten"};
        thumbOptions = new String[]{"-interlace", "none", "-thumbnail",
            "65x65", "-background", "white", "-flatten"};
    }
    private int totalImageNumber;
    private int currentImageNumber;

    private void createGalleryXML(LocalRepresentationObject rep, File gallery)
            throws IOException, MetsMetadataException {

        // Root File - Copy METS with structure
        File originalMetsFile = new File(URI.create(rep.getRootFile()
                .getAccessURL()));

        DigitalizedWorkMetsHelper dwMetsHelper = DigitalizedWorkMetsHelper
                .newInstance(originalMetsFile);

        if (gallery.exists()) {
            gallery.delete();
        }
        gallery.createNewFile();

        String galleryTemplate = new String(StreamUtils
                .getBytes(DW2SimpleViewer.class.getClassLoader()
                .getResourceAsStream("/gallery.xml")));

        Mets mets = dwMetsHelper.getMets();

        DivType baseDiv = mets.getStructMapList().get(0).getDiv();

        Map<String, String> datastreamIdToCaption = new HashMap<String, String>();

        for (RepresentationFile repFile : rep.getPartFiles()) {
            datastreamIdToCaption.put(repFile.getId(), repFile
                    .getOriginalName());
        }

        totalImageNumber = dwMetsHelper.getFiles().size();
        currentImageNumber = 1;

        String imagesXML = createImageXML(new Vector<String>(), baseDiv,
                datastreamIdToCaption, dwMetsHelper);

        String title;
        try {
            SimpleDescriptionObject sdo = getRodaClient().getBrowserService()
                    .getSimpleDescriptionObject(rep.getDescriptionObjectPID());
            title = sdo.getTitle();
        } catch (Exception e) {
            title = "";
        }

        galleryTemplate = galleryTemplate.replaceAll("\\@TITLE", Matcher
				.quoteReplacement(XMLUtils.xmlEncodeString(title)));
        galleryTemplate = galleryTemplate.replaceAll("\\@IMAGES", Matcher
                .quoteReplacement(imagesXML));

        PrintWriter printer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(gallery), "UTF-8"));
        printer.write(galleryTemplate);
        printer.flush();
        printer.close();

    }
    private static final String IMAGE_PATH_SEPARATOR = " » ";

    protected String createImageXML(List<String> parentLabels, DivType div,
            Map<String, String> datastreamIdToCaption,
            DigitalizedWorkMetsHelper dwMetsHelper) {
        String xml = "";
        List<String> newParentLabels = new Vector<String>(parentLabels);
        newParentLabels.add(div.getLABEL());
        for (DivType divType : div.getDivList()) {
            xml += createImageXML(newParentLabels, divType,
                    datastreamIdToCaption, dwMetsHelper);
        }

        // Removing base div label
        newParentLabels.remove(0);

        for (Fptr fptr : div.getFptrList()) {
            String fileHref = dwMetsHelper.getFileHref(fptr.getFILEID());
            String image = fileHref + dstFileExtension;
            String caption = datastreamIdToCaption.get(fileHref);

            String parentsLabel = "";
            for (String parentLabel : newParentLabels) {
                parentsLabel += parentLabel + IMAGE_PATH_SEPARATOR;

            }

            caption = parentsLabel + caption;

            caption = "(" + currentImageNumber + "/" + totalImageNumber + ") "
                    + caption;

            if (image != null && caption != null) {
                xml += "<image><filename>" + image + "</filename><caption>"
                        + caption + "</caption></image>\n";
                currentImageNumber++;
            } else if (image != null) {
                xml += "<image><filename>" + image + "</filename></image>\n";
                currentImageNumber++;
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

            // Root File - Create SimpleViewer gallery
            RepresentationFile rootFile = convertedRepresentation.getRootFile();
            File gallery = new File(tempDirectory, rootFile.getId() + ".xml");

            createGalleryXML(convertedRepresentation, gallery);

            rootFile.setAccessURL(gallery.toURI().toURL().toString());
            rootFile.setId(rootFile.getId() + ".xml");

            logger.trace("Gallery created");

            convertAllImages(convertedRepresentation, tempDirectory, report);

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
            File imageFile = new File(tempDirectory, "image_"
                    + originalFile.getName() + dstFileExtension);
            File thumbFile = new File(tempDirectory, "thumb_"
                    + originalFile.getName() + dstFileExtension);

            try {
                convert(originalFile, imageFile, imageOptions);
                convert(originalFile, thumbFile, thumbOptions);

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

            RepresentationFile thumbPartFile = new RepresentationFile();
            thumbPartFile.setId(thumbFile.getName());
            thumbPartFile.setAccessURL(thumbFile.getAbsolutePath());
            thumbPartFile.importFileFormat(FormatUtility.getFileFormat(thumbFile, thumbFile.getName()));
            thumbPartFile.setSize(thumbFile.length());

            convertedRepresentation.addPartFile(thumbPartFile);

            convertedRepresentation.setSubType(partFile.getMimetype());
        }
    }

    @Override
    protected boolean representationNeedsConversion(
            LocalRepresentationObject localRepresentation)
            throws CommandException, WrongRepresentationTypeException,
            WrongRepresentationSubtypeException {
        return true;
    }
}
