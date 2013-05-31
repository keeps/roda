package pt.gov.dgarq.roda.migrator.services;

import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * @author Luis Faria
 */
public class ST2Pdf extends OpenOfficeConverter {

	/**
	 * @throws RODAServiceException
	 */
	public ST2Pdf() throws RODAServiceException {
		super();
		format = "application/pdf";
		formatExtension = ".pdf";
	}

}
