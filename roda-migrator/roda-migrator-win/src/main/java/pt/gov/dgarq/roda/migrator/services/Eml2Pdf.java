package pt.gov.dgarq.roda.migrator.services;

import org.apache.log4j.Logger;
import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 */
public class Eml2Pdf extends EmailConverter {

    private static final Logger logger = Logger.getLogger(Eml2Pdf.class);

    /**
     * @throws RODAServiceException
     */
    public Eml2Pdf() throws RODAServiceException {
        super();
        format = "application/pdf";
        formatExtension = ".pdf";
        try {
            this.email2pdfExecutable = getConfiguration().getString("eml2pdfExecutable");
            logger.info("Using converter " + this.email2pdfExecutable);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RODAServiceException(e.getMessage(), e);
        }
    }
}
