package pt.gov.dgarq.roda.common;

import pt.gov.dgarq.roda.core.data.FileFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.gov.nationalarchives.droid.command.action.CommandExecutionException;
import uk.gov.nationalarchives.droid.command.archive.GZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.TarArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.archive.ZipArchiveContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.Ole2ContainerContentIdentifier;
import uk.gov.nationalarchives.droid.command.container.ZipContainerContentIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureDefinitions;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.container.TriggerPuid;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.RodaBinarySignatureIdentifie;
import uk.gov.nationalarchives.droid.core.SignatureParseException;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.archive.IdentificationRequestFactory;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

/**
 * Class for detecting formats by DROID libraries
 *
 * @author Ing. Vladislav Korecký [vladislav_korecky@gordic.cz] - GORDIC spol.
 * s.r.o.
 *
 */
public class DroidFactory {

    private static Logger logger = Logger.getLogger(DroidFactory.class);
    private static final int maxBytesToScan = -1;
    private static final String FORWARD_SLASH = "/";
    private static final String BACKWARD_SLASH = "\\";
    private static final String R_SLASH = "/";
    private static final String L_BRACKET = "(";
    private static final String R_BRACKET = ")";
    private static final String SPACE = " ";
    private static final String OLE2_CONTAINER = "OLE2";
    private static final String ZIP_CONTAINER = "ZIP";
    private static final String ZIP_ARCHIVE = "x-fmt/263";
    private static final String JIP_ARCHIVE = "x-fmt/412";
    private static final String TAR_ARCHIVE = "x-fmt/265";
    private static final String GZIP_ARCHIVE = "x-fmt/266";
    private static FFSignatureFile signatureFile;
    private static RodaBinarySignatureIdentifie binarySignatureIdentifier;
    private static ContainerSignatureDefinitions containerSignatureDefinitions;
    private static String slash;
    private static String slash1;
    private static String wrongSlash;
    private boolean archives;
    private File sourceFile;
    private List<TriggerPuid> triggerPuids;
    private IdentificationRequestFactory requestFactory;

    // Inicialize DROID signature files
    static {
        // Set DORID log level
        LogManager.getRootLogger().setLevel((Level) Level.WARN);

        // Binary signature
        try {
            // Convert resource stream to File
            InputStream inputStream = DroidFactory.class.getResourceAsStream("DROID_SignatureFile_V68.xml");
            File binarySignaturesFile = File.createTempFile("binary-signature", ".xml");
            if ((binarySignaturesFile != null) && (binarySignaturesFile.exists())) {
                // write the inputStream to a FileOutputStream
                OutputStream out = new FileOutputStream(binarySignaturesFile);
                // Save inputStream to temp file
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                inputStream.close();
                out.flush();
                out.close();
            }
            if (binarySignaturesFile != null) {
                // Slashs
                String path = binarySignaturesFile.getAbsolutePath();
                slash = path.contains(FORWARD_SLASH) ? FORWARD_SLASH
                        : BACKWARD_SLASH;
                binarySignatureIdentifier = new RodaBinarySignatureIdentifie();

                if (binarySignaturesFile.exists()) {
                    binarySignatureIdentifier.setSignatureFile(path);
                    try {
                        binarySignatureIdentifier.init();
                    } catch (SignatureParseException e) {
                        logger.error("DROID - Can't parse signature file: \"" + path + "\"");
                    }
                    binarySignatureIdentifier.setMaxBytesToScan(maxBytesToScan);
                    signatureFile = binarySignatureIdentifier.getSigFile();
                }
            }
        } catch (IOException ex) {
            logger.error("Cannot create temp file for binary-signature.xml. DROID can't detect file formats.", ex);
        }

        // ContainerSignatureDefinitions
        containerSignatureDefinitions = null;
        InputStream in = null;
        try {
            in = DroidFactory.class.getResourceAsStream("container-signature.xml");
            final ContainerSignatureSaxParser parser = new ContainerSignatureSaxParser();
            containerSignatureDefinitions = parser.parse(in);
        } catch (SignatureParseException e) {
            logger.error("DROID - Can't parse container signature file.");
        } catch (JAXBException jaxbe) {
            logger.error("DROID - Can't bind data from container signature file.", jaxbe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("DROID - Error closing InputStream on signature file.", e);
                }
            }
        }
    }

    /**
     * Constructor
     *
     * @throws URISyntaxException
     */
    public DroidFactory(File sourceFile) throws FormatDetectionException {
        this.sourceFile = sourceFile;
        // Set whether this examines Archives.
        this.archives = false;
        // TODO: DROID signature file is now in resources, auto update from URL
        // will be good option. URL:
        // http://www.nationalarchives.gov.uk/aboutapps/pronom/droid-signature-files.htm

        this.slash1 = this.slash;
        this.wrongSlash = this.slash.equals(R_SLASH) ? "\\" : R_SLASH;

        if (signatureFile == null) {
            throw new FormatDetectionException("Signature file is not initialized");
        }

        if (containerSignatureDefinitions != null) {
            triggerPuids = containerSignatureDefinitions.getTiggerPuids();
        }
    }

    public FileFormat execute() throws FormatDetectionException {
        FileFormat fileFormat = null;
        if (!sourceFile.exists()) {
            throw new FormatDetectionException("DROID - Resources file not found");
        }

        String fileNameCanonicalCanonicalPath = null;
        try {
            fileNameCanonicalCanonicalPath = sourceFile.getCanonicalPath();
        } catch (IOException e) {
            throw new FormatDetectionException("DROID - Can't canonical resources file path.", e);
        }
        URI uri = sourceFile.toURI();
        RequestMetaData metaData = new RequestMetaData(sourceFile.length(),
                sourceFile.lastModified(), fileNameCanonicalCanonicalPath);
        RequestIdentifier identifier = new RequestIdentifier(uri);
        identifier.setParentId(1L);

        InputStream in = null;
        IdentificationRequest request = new FileSystemIdentificationRequest(
                metaData, identifier);
        try {
            in = new FileInputStream(sourceFile);
            request.open(in);
            IdentificationResultCollection results = binarySignatureIdentifier
                    .matchBinarySignatures(request);

            String path = "";
            String fileName = (path + request.getFileName()).replace(
                    wrongSlash, slash);
            IdentificationResultCollection containerResults = null;
            try {
                containerResults = getContainerResults(results, request, fileName);
            } catch (CommandExecutionException e) {
                throw new FormatDetectionException("DROID - Can't get container results.", e);
            }
            IdentificationResultCollection finalResults = new IdentificationResultCollection(
                    request);
            boolean container = false;
            if (containerResults.getResults().size() > 0) {
                container = true;
                finalResults = containerResults;
            } else if (results.getResults().size() > 0) {
                finalResults = results;
            }
            if (finalResults.getResults().size() > 0) {
                binarySignatureIdentifier.removeLowerPriorityHits(finalResults);
            }
            if (finalResults.getResults().size() > 0) {
                for (IdentificationResult identResult : finalResults.getResults()) {
                    String puid = identResult.getPuid();
                    if (!container && JIP_ARCHIVE.equals(puid)) {
                        puid = ZIP_ARCHIVE;
                    }

                    // Get file format information form DROID signature file
                    uk.gov.nationalarchives.droid.core.signature.FileFormat droidFileFormat = signatureFile.getFileFormat(puid);
                    // Convert DROID fileformat to RODA file format
                    fileFormat = new FileFormat();
                    if (droidFileFormat.getExtensions() != null) {
                        fileFormat.setExtensions(droidFileFormat.getExtensions().toArray(new String[droidFileFormat.getExtensions().size()]));
                    }
                    // Get first mimetype (if more then one is detected)
                    String[] mimeTypes = droidFileFormat.getMimeType().split(",");
                    if ((mimeTypes.length > 0) && (mimeTypes[0] != null)) {
                        fileFormat.setMimetype(mimeTypes[0].trim());
                    }
                    fileFormat.setName(droidFileFormat.getName());
                    fileFormat.setPuid(droidFileFormat.getPUID());
                    fileFormat.setVersion(droidFileFormat.getVersion());

                    if (archives && !container) {
                        if (GZIP_ARCHIVE.equals(puid)) {
                            GZipArchiveContentIdentifier gzipArchiveIdentifier = new GZipArchiveContentIdentifier(
                                    binarySignatureIdentifier,
                                    containerSignatureDefinitions, path, slash,
                                    slash1);
                            try {
                                gzipArchiveIdentifier.identify(results.getUri(),
                                        request);
                            } catch (CommandExecutionException e) {
                                throw new FormatDetectionException("DROID - GZipArchiveContentIdentifier error.", e);
                            }
                        } else if (TAR_ARCHIVE.equals(puid)) {
                            TarArchiveContentIdentifier tarArchiveIdentifier = new TarArchiveContentIdentifier(
                                    binarySignatureIdentifier,
                                    containerSignatureDefinitions, path, slash,
                                    slash1);
                            try {
                                tarArchiveIdentifier.identify(results.getUri(),
                                        request);
                            } catch (CommandExecutionException e) {
                                throw new FormatDetectionException("DROID - TarArchiveContentIdentifier error.", e);
                            }
                        } else if (ZIP_ARCHIVE.equals(puid)
                                || JIP_ARCHIVE.equals(puid)) {
                            ZipArchiveContentIdentifier zipArchiveIdentifier = new ZipArchiveContentIdentifier(
                                    binarySignatureIdentifier,
                                    containerSignatureDefinitions, path, slash,
                                    slash1);
                            try {
                                zipArchiveIdentifier.identify(results.getUri(),
                                        request);
                            } catch (CommandExecutionException e) {
                                throw new FormatDetectionException("DROID - ZipArchiveContentIdentifier error.", e);
                            }
                        }
                    }
                }
            } else {
                System.out.println(fileName + ",Unknown");
            }
        } catch (FileNotFoundException fnfe) {
            throw new FormatDetectionException("DROID - Error processing files, can`t find file.", fnfe);
        } catch (IOException e) {
            throw new FormatDetectionException("DROID - Error processing files, can't access file.", e);
        } finally {
            if (in != null) {
                try {
                    request.close();
                    sourceFile = null;
                    in.close();

                } catch (IOException e) {
                    throw new FormatDetectionException("DROID - Can't close stream with resource file.", e);
                }
            }
        }
        return fileFormat;
    }

    private IdentificationResultCollection getContainerResults(
            final IdentificationResultCollection results,
            final IdentificationRequest request, final String fileName)
            throws CommandExecutionException {

        IdentificationResultCollection containerResults = new IdentificationResultCollection(
                request);

        if (results.getResults().size() > 0
                && containerSignatureDefinitions != null) {
            for (IdentificationResult identResult : results.getResults()) {
                String filePuid = identResult.getPuid();
                if (filePuid != null) {
                    TriggerPuid containerPuid = getTriggerPuidByPuid(filePuid);
                    if (containerPuid != null) {

                        requestFactory = new ContainerFileIdentificationRequestFactory();
                        String containerType = containerPuid.getContainerType();

                        if (OLE2_CONTAINER.equals(containerType)) {
                            try {
                                Ole2ContainerContentIdentifier ole2Identifier = new Ole2ContainerContentIdentifier();
                                ole2Identifier.init(
                                        containerSignatureDefinitions,
                                        containerType);
                                Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
                                ole2IdentifierEngine
                                        .setRequestFactory(requestFactory);
                                ole2Identifier
                                        .setIdentifierEngine(ole2IdentifierEngine);
                                containerResults = ole2Identifier.process(
                                        request.getSourceInputStream(),
                                        containerResults);
                            } catch (IOException e) { // carry on after
                                // container i/o
                                // problems
                                System.err.println(e + SPACE + L_BRACKET
                                        + fileName + R_BRACKET);
                            }
                        } else if (ZIP_CONTAINER.equals(containerType)) {
                            try {
                                ZipContainerContentIdentifier zipIdentifier = new ZipContainerContentIdentifier();
                                zipIdentifier.init(
                                        containerSignatureDefinitions,
                                        containerType);
                                ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
                                zipIdentifierEngine
                                        .setRequestFactory(requestFactory);
                                zipIdentifier
                                        .setIdentifierEngine(zipIdentifierEngine);
                                containerResults = zipIdentifier.process(
                                        request.getSourceInputStream(),
                                        containerResults);
                            } catch (IOException e) { // carry on after
                                // container i/o
                                // problems
                                System.err.println(e + SPACE + L_BRACKET
                                        + fileName + R_BRACKET);
                            }
                        } else {
                            throw new CommandExecutionException(
                                    "Unknown container type: " + containerPuid);
                        }
                    }
                }
            }
        }
        return containerResults;
    }

    private TriggerPuid getTriggerPuidByPuid(final String puid) {
        for (final TriggerPuid tp : triggerPuids) {
            if (tp.getPuid().equals(puid)) {
                return tp;
            }
        }
        return null;
    }
}
