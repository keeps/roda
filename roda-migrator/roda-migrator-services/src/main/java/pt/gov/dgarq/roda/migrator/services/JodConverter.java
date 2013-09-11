package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

/**
 * Utility class to execute unoconv without concurrency problems.
 *
 * Needs packages: openoffice.org, unoconv and X up and running
 *
 * @author Luis Faria
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 */
public class JodConverter {

    private static final Logger logger = Logger.getLogger(JodConverter.class);
    private OfficeDocumentConverter converter = null;
    private OfficeManager officeManager = null;
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
        OfficeManager officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
        officeManager.start();
        converter = new OfficeDocumentConverter(officeManager);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
        // Stops OfficeManager
        officeManager.stop();
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
     * @param inputFile
     * @param outputFile
     * @param outputFormat
     */
    protected void convert(File inputFile, File outputFile, DocumentFormat outputFormat) {
        converter.convert(inputFile, outputFile, outputFormat);
    }

    /**
     * Make a convertion
     *
     * @param inputFile
     * @param outputFile
     * @param outputMimeType
     */
    public void convert(File inputFile, File outputFile, String outputMimeType) {
        if (mimetypeMap.containsKey(outputMimeType)) {
            outputMimeType = mimetypeMap.get(outputMimeType);
        }
        convert(inputFile, outputFile, getRegistry().getFormatByMediaType(outputMimeType));
    }
}
