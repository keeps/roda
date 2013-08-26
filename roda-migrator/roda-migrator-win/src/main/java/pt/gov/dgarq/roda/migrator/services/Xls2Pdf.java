package pt.gov.dgarq.roda.migrator.services;

import org.apache.log4j.Logger;
import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * @author Vladislav Korecky
 */
public class Xls2Pdf extends MicrosoftOfficeConverter {

    private static final Logger logger = Logger
            .getLogger(Ppt2Pdf.class);

    /**
     * @throws RODAServiceException
     */
    public Xls2Pdf() throws RODAServiceException {
        super();
        format = "application/pdf";
        formatExtension = ".pdf";
        try {
            this.office2pdfExecutable = getConfiguration().getString("xls2pdfExecutable");
            logger.info("Using converter " + this.office2pdfExecutable);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RODAServiceException(e.getMessage(), e);
        }
    }
}
