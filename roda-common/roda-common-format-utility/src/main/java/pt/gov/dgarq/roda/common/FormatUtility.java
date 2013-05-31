package pt.gov.dgarq.roda.common;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * This is an utility class with methods to find out the mimetype of a file or
 * stream.
 * 
 * @author Rui Castro
 * @author Luís Faria
 */
public class FormatUtility {

	private static Logger logger = Logger.getLogger(FormatUtility.class);

	private static MimetypesFileTypeMap mimetypesFileTypeMap = null;

	/**
	 * Returns the mimetype of the given {@link File}.
	 * 
	 * @param file
	 *            the {@link File}
	 * 
	 * @return a {@link String} with the mimetype of the given {@link File}.
	 */
	public static String getMimetype(File file) {
		return getMimetypesFileTypeMap().getContentType(file);
	}

	/**
	 * Returns the mimetype of the given {@link InputStream} and filename.
	 * 
	 * @param filename
	 * @param inputStream
	 * 
	 * @return a {@link String} with the mimetype of the given
	 *         {@link InputStream}.
	 */
	public static String getMimetype(String filename, InputStream inputStream) {
		return getMimetypesFileTypeMap().getContentType(filename);
	}

	/**
	 * Get file format
	 * 
	 * @param file
	 * @return the file format
	 */
	public static FileFormat getFileFormat(File file) {
		String mimetype = getMimetype(file);
		FileFormat fileFormat = new FileFormat();
		fileFormat.setName(mimetype);
		fileFormat.setMimetype(mimetype);
		return fileFormat;
	}

	/**
	 * Returns the mimetype of the given {@link InputStream}.
	 * 
	 * @param inputStream
	 * 
	 * @return a {@link String} with the mimetype of the given
	 *         {@link InputStream}.
	 */
	public static String getMimetype(InputStream inputStream) {
		// TODO use DROID to get MIME type of an input stream
		return "application/octet-stream";
	}

	/**
	 * Returns the mimetype for the given filename.
	 * 
	 * @param filename
	 * 
	 * @return a {@link String} with the mimetype of the given filename.
	 */
	public static String getMimetype(String filename) {
		return getMimetypesFileTypeMap().getContentType(filename);
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

				logger.warn(
						"Error reading mime.types-extensions.properties file - "
								+ e.getMessage(), e);
				logger
						.warn("Using default predefined values for mimetype/extensions");

				mimetypesFileTypeMap.addMimeTypes("application/pdf pdf PDF");
				mimetypesFileTypeMap.addMimeTypes("application/msword doc DOC");
				mimetypesFileTypeMap
						.addMimeTypes("application/vnd.openxmlformats-officedocument.wordprocessingml.document docx DOCX");
				mimetypesFileTypeMap
						.addMimeTypes("application/vnd.oasis.opendocument.text odt ODT");

				mimetypesFileTypeMap.addMimeTypes("text/xml xml XML");
				mimetypesFileTypeMap.addMimeTypes("text/dbml dbml DBML");

				mimetypesFileTypeMap.addMimeTypes("video/mpeg mpg MPG");
				mimetypesFileTypeMap
						.addMimeTypes("video/mpeg2 m2p M2P m2v M2V mpv2 MPV2 mp2v MP2V vob VOB");
				mimetypesFileTypeMap.addMimeTypes("video/avi avi AVI");
				mimetypesFileTypeMap.addMimeTypes("video/x-ms-wmv wmv WMV");
				mimetypesFileTypeMap.addMimeTypes("video/quicktime mov MOV");

				mimetypesFileTypeMap.addMimeTypes("audio/wav wav WAV");
				mimetypesFileTypeMap
						.addMimeTypes("audio/mpeg mp1 MP1 mp2 MP2 mp3 MP3 mpa MPA");
				mimetypesFileTypeMap
						.addMimeTypes("audio/aiff aif AIF aiff AIFF");
				mimetypesFileTypeMap.addMimeTypes("audio/ogg ogg OGG");

			}
		}

		return mimetypesFileTypeMap;
	}
}
