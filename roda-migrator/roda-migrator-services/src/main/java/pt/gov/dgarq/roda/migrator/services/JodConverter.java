/**
 * 
 */
package pt.gov.dgarq.roda.migrator.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

/**
 * Utility class to execute unoconv without concurrency problems.
 * 
 * Needs packages: openoffice.org, unoconv and X up and running
 * 
 * @author Luis Faria
 * 
 */
public class JodConverter {
	private static final Logger logger = Logger.getLogger(JodConverter.class);

	private OpenOfficeConnection connection = null;
	private DocumentConverter converter = null;
	private DocumentFormatRegistry registry = null;

	private static Map<String, String> mimetypeMap = new HashMap<String, String>();
	static {
		mimetypeMap.put("application/rtf", "text/rtf");
	}

	/**
	 * Construct a new {@link JodConverter}.
	 * 
	 * @throws ConnectException
	 */
	public JodConverter() throws ConnectException {
		connection = new SocketOpenOfficeConnection(8100);
		connection.connect();
		converter = new OpenOfficeDocumentConverter(connection);
	}

	/**
	 * Get document registry
	 * 
	 * @return the registry
	 */
	public DocumentFormatRegistry getRegistry() {
		if (registry == null) {
			registry = new DefaultDocumentFormatRegistry();
		}
		return registry;
	}

	/**
	 * Make a convertion
	 * 
	 * @param inputStream
	 * @param inputFormat
	 * @param outputStream
	 * @param outputFormat
	 */
	protected void convert(InputStream inputStream, DocumentFormat inputFormat,
			OutputStream outputStream, DocumentFormat outputFormat) {
		converter.convert(inputStream, inputFormat, outputStream, outputFormat);
	}

	/**
	 * Make a convertion
	 * 
	 * @param inputStream
	 * @param inputMimeType
	 * @param outputStream
	 * @param outputMimeType
	 */
	public void convert(InputStream inputStream, String inputMimeType,
			OutputStream outputStream, String outputMimeType) {

		if (mimetypeMap.containsKey(inputMimeType)) {
			inputMimeType = mimetypeMap.get(inputMimeType);
		}

		if (mimetypeMap.containsKey(outputMimeType)) {
			outputMimeType = mimetypeMap.get(outputMimeType);
		}

		convert(inputStream, getRegistry().getFormatByMimeType(inputMimeType),
				outputStream, getRegistry().getFormatByMimeType(outputMimeType));

	}

}
