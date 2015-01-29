package pt.gov.dgarq.roda.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.harvard.hul.ois.fits.exceptions.FitsConfigurationException;
import edu.harvard.hul.ois.fits.exceptions.FitsException;
import pt.gov.dgarq.roda.core.data.FileFormat;

/**
 * This is an utility class with methods to find out the mimetype of a file or
 * stream.
 *
 * @author Rui Castro
 * @author Luís Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class FormatUtility {

    private static Logger logger = Logger.getLogger(FormatUtility.class);
    private static MimetypesFileTypeMap mimetypesFileTypeMap = null;

    /**
     * Returns the mimetype of the given {@link File}.
     *
     * @param file the {@link File}
     *
     * @return a {@link String} with the mimetype of the given {@link File}.
     */
    public static String getMimetype(File file, String originalFileName) {
        FileFormat fileFormat = getFileFormat(file, originalFileName);
        if (fileFormat != null) {
            return fileFormat.getMimetype();
        }
        return null;
    }

    /**
     * Get file format
     *
     * @param file
     * @return the file format
     */
    public static FileFormat getFileFormat(File file, String originalFileName) {
        FileFormat detectedFileFormat = null;

        if ((file != null) && (file.exists()) && (file.length() > 0)) {
            try {
                detectedFileFormat = FITSUtility.execute(file);
            } catch (FitsConfigurationException e) {
            	logger.error("Error while creating FITS Utility", e);
			} catch (FitsException e) {
				logger.error("Error while trying to detect file format using FITS", e);
			}
        } else {
            if (file == null) {
                logger.error("Error while trying to detect file format using FITS", new NullArgumentException("attribute file is NULL"));
            } else if (!file.exists()) {
                logger.error("Error while trying to detect file format using FITS", new IOException("File \"" + file.getAbsolutePath() + "\" doesn't exist"));
            } else if (file.length() > 1) {
                logger.error("Error while trying to detect file format using FITS", new IOException("File \"" + file.getAbsolutePath() + "\" has 0 size."));
            }
        }
        // Sometimes are not information from PRONOM completed or PRONOM couldn't detect file format. 
        // In this case method try fill missing information if needed
        if (detectedFileFormat == null) {
            detectedFileFormat = new FileFormat();
        } else {
            detectedFileFormat.setFormatRegistryName("PRONOM http://www.nationalarchives.gov.uk/PRONOM");
        }
        if (StringUtils.isBlank(detectedFileFormat.getMimetype())) {
            String mimetype = null;
            if (detectedFileFormat.getExtensions() != null) {
                for (String extension : detectedFileFormat.getExtensions()) {
                    mimetype = getMimetypesFileTypeMap().getContentType("file." + extension);
                    if (StringUtils.isBlank(mimetype)) {
                        break;
                    }
                }
            }
            if (StringUtils.isBlank(mimetype)) {
                mimetype = getMimetypesFileTypeMap().getContentType(originalFileName);
            }
            detectedFileFormat.setMimetype(mimetype);
        }
        if (StringUtils.isBlank(detectedFileFormat.getName())) {
            detectedFileFormat.setName(detectedFileFormat.getMimetype());
        }
        if (StringUtils.isBlank(detectedFileFormat.getFormatRegistryName())) {
            detectedFileFormat.setFormatRegistryName("none");
        }
        if (StringUtils.isBlank(detectedFileFormat.getPuid())) {
            detectedFileFormat.setPuid("none");
        }
        return detectedFileFormat;
    }

    /**
     * Get file format
     *
     * @param file
     * @return the file format
     */
    public static FileFormat getFileFormat(InputStream inputStream, String originalFileName) {
        FileFormat detectedFileFormat = null;
        File tempFile = null;
        try {
			tempFile = saveStreamAsTempFile(inputStream, originalFileName);
			
            // Run file format detection
            detectedFileFormat = getFileFormat(tempFile, originalFileName);
        } catch (IOException e) {
            logger.error(
                    "Cannot create temp file for getMimetype FileInputStream. FITS can't detect mime-type.",
                    e);
        } finally {
            if ((tempFile != null) && (tempFile.exists())) {
                tempFile.delete();
            }
        }
        return detectedFileFormat;
    }

    /**
     * Returns the mimetype of the given {@link InputStream}.
     *
     * @param inputStream
     *
     * @return a {@link String} with the mimetype of the given
     * {@link FileInputStream}.
     */
    public static String getMimetype(InputStream inputStream, String originalFileName) {
        if (inputStream == null) {
            logger.error(new NullArgumentException("Input stream cannot be NULL."));
            return null;
        }
        String mimeType = "application/octet-stream";
        File tempFile = null;
        try {
            tempFile = saveStreamAsTempFile(inputStream, originalFileName);
            // Run file format detection
            mimeType = getMimetype(tempFile, originalFileName);
        } catch (IOException e) {
            logger.error(
                    "Cannot create temp file for getMimetype FileInputStream. FITS can't detect mime-type.",
                    e);
        } finally {
            if ((tempFile != null) && (tempFile.exists())) {
                tempFile.delete();
            }
        }
        return mimeType;
    }

    protected static MimetypesFileTypeMap getMimetypesFileTypeMap() {
        if (mimetypesFileTypeMap == null) {
            mimetypesFileTypeMap = new MimetypesFileTypeMap();
            PropertiesConfiguration mimetypeConfiguration = new PropertiesConfiguration();
            mimetypeConfiguration.setDelimiterParsingDisabled(false);
            // PropertiesConfiguration.setDefaultListDelimiter(',');








            try {

                mimetypeConfiguration
                        .load(FormatUtility.class
                        .getResourceAsStream("mime.types-extensions.properties"));
                Iterator<String> keysIterator = mimetypeConfiguration.getKeys();

                while (keysIterator.hasNext()) {
                    String mimeType = keysIterator.next();
                    String mimetypevalue = mimeType + " "
                            + mimetypeConfiguration.getString(mimeType);
                    mimetypesFileTypeMap.addMimeTypes(mimetypevalue);
                }
            } catch (ConfigurationException e) {

                logger.warn("Error reading mime.types-extensions.properties file - " + e.getMessage(), e);
                logger.warn("Using default predefined values for mimetype/extensions");

                mimetypesFileTypeMap.addMimeTypes("application/pdf pdf PDF");
                mimetypesFileTypeMap.addMimeTypes("application/msword doc DOC");
                mimetypesFileTypeMap.addMimeTypes("application/vnd.openxmlformats-officedocument.wordprocessingml.document docx DOCX");
                mimetypesFileTypeMap.addMimeTypes("application/vnd.oasis.opendocument.text odt ODT");

                mimetypesFileTypeMap.addMimeTypes("text/xml xml XML");
                mimetypesFileTypeMap.addMimeTypes("text/dbml dbml DBML");

                mimetypesFileTypeMap.addMimeTypes("video/mpeg mpg MPG");
                mimetypesFileTypeMap.addMimeTypes("video/mpeg2 m2p M2P m2v M2V mpv2 MPV2 mp2v MP2V vob VOB");
                mimetypesFileTypeMap.addMimeTypes("video/avi avi AVI");
                mimetypesFileTypeMap.addMimeTypes("video/x-ms-wmv wmv WMV");
                mimetypesFileTypeMap.addMimeTypes("video/quicktime mov MOV");

                mimetypesFileTypeMap.addMimeTypes("audio/wav wav WAV");
                mimetypesFileTypeMap.addMimeTypes("audio/mpeg mp1 MP1 mp2 MP2 mp3 MP3 mpa MPA");
                mimetypesFileTypeMap.addMimeTypes("audio/aiff aif AIF aiff AIFF");
                mimetypesFileTypeMap.addMimeTypes("audio/ogg ogg OGG");

                mimetypesFileTypeMap.addMimeTypes("application/vnd.ms-powerpoint ppt PPT");
                mimetypesFileTypeMap.addMimeTypes("application/vnd.openxmlformats-officedocument.presentationml.presentation pptx PPTX");
                mimetypesFileTypeMap.addMimeTypes("application/vnd.oasis.opendocument.presentation odp ODP");

                mimetypesFileTypeMap.addMimeTypes("application/vnd.ms-excel xls XLS");
                mimetypesFileTypeMap.addMimeTypes("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet xlsx XLSX");
                mimetypesFileTypeMap.addMimeTypes("application/vnd.oasis.opendocument.spreadsheet ods ODS");

                mimetypesFileTypeMap.addMimeTypes("application/cdr cdr CDR");
                mimetypesFileTypeMap.addMimeTypes("application/illustrator ai AI");
                mimetypesFileTypeMap.addMimeTypes("application/x-qgis shp SHP");
                mimetypesFileTypeMap.addMimeTypes("application/acad dwg DWG");

                mimetypesFileTypeMap.addMimeTypes("message/rfc822 eml EML");
                mimetypesFileTypeMap.addMimeTypes("application/msoutlook msg MSG");
            }
        }

        return mimetypesFileTypeMap;
    }

    public static File saveStreamAsTempFile(InputStream inputStream, String originalFileName) throws IOException {
    	String suffix = (originalFileName != null)?originalFileName:".fits_tmp";
    	
    	// Create temp file                
        File tempFile = File.createTempFile(String.valueOf(UUID.randomUUID()), suffix);
        if (inputStream == null) {
            return null;
        }
        if ((tempFile != null) && (tempFile.exists())) {
            OutputStream outputStream = new FileOutputStream(tempFile);
            // write the inputStream to a OutputStream                        
            try {

                if (inputStream instanceof FileInputStream) {
                    // *************************************************************************
                    // *************************    FileInputStream    *************************
                    // *************************************************************************
                    FileInputStream fileInputStream = (FileInputStream) inputStream;
                    // Get file channel
                    FileChannel fileChannel = fileInputStream.getChannel();
                    // Set file input stream to begining
                    fileChannel.position(0);

                    // Save inputStream to temp file
                    ByteBuffer buffer = ByteBuffer.allocate(64);
                    int bytesRead = fileChannel.read(buffer);
                    while (bytesRead != -1) {
                        buffer.flip();
                        // rite to file
                        while (buffer.hasRemaining()) {
                            outputStream.write(buffer.get());
                        }
                        buffer.clear();
                        bytesRead = fileChannel.read(buffer);
                    }
                    // Set file input stream to begining
                    fileChannel.position(0);
                } else {
                    // *************************************************************************
                    // *************************    Unknown InputStream    **********************
                    // **************************************************************************                    

                    if (inputStream.markSupported()) {
                        inputStream.mark(1 << 24); // magic constant: BEWARE
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    while ((read = reader.read()) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    if (inputStream.markSupported()) {
                        inputStream.reset();
                    }
                }
            } catch (IOException e) {
                logger.error("Error when trying to save InputStream to File.", e);
            } finally {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    logger.error("Error when trying to close InputStream.", e);
//                }
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    logger.error("Error when trying to close OutputStream.", e);
                }
            }
        }
        return tempFile;
    }
    
	public static File saveStreamAsTempFile(InputStream inputStream)
			throws IOException {
		return saveStreamAsTempFile(inputStream, null);
	}
}
