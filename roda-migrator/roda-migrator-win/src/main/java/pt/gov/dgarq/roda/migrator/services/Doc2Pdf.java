package pt.gov.dgarq.roda.migrator.services;

import pt.gov.dgarq.roda.core.common.RODAServiceException;
import org.apache.log4j.Logger;

/**
 * @author Luis Faria
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class Doc2Pdf extends MicrosoftOfficeConverter {
    private static final Logger logger = Logger
			.getLogger(Doc2Pdf.class);
    /**
     * @throws RODAServiceException
     */
    public Doc2Pdf() throws RODAServiceException {
        super();
        format = "application/pdf";
        formatExtension = ".pdf";
        try {
            this.office2pdfExecutable = getConfiguration().getString("doc2pdfExecutable");
            logger.info("Using converter " + this.office2pdfExecutable);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RODAServiceException(e.getMessage(), e);
        }
    }
}
