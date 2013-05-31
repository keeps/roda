package pt.gov.dgarq.roda.migrator.services;

import pt.gov.dgarq.roda.core.common.RODAServiceException;

/**
 * @author Luis Faria
 * @author Rui Castro
 */
public class Doc2Pdf extends MicrosoftWordConverter {

	/**
	 * @throws RODAServiceException
	 */
	public Doc2Pdf() throws RODAServiceException {
		super();
		format = "application/pdf";
		formatExtension = ".pdf";
	}

}
