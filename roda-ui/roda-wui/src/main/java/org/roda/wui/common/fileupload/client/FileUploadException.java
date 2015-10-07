/**
 * 
 */
package org.roda.wui.common.fileupload.client;

import org.roda.core.common.RODAException;
import org.roda.wui.common.client.GenericException;

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
