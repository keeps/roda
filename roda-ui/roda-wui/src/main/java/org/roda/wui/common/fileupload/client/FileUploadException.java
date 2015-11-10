/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.fileupload.client;

import org.roda.core.data.common.RODAException;
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
