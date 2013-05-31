/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.fileupload.client;

import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.wui.common.client.GenericException;

/**
 * @author Luis Faria
 * 
 */
public class FileUploadException extends RODAException {

	public FileUploadException() {
		super();
	}

	public FileUploadException(String message, GenericException cause) {
		super(message, cause);
	}

	public FileUploadException(String message) {
		super(message);
	}

}
