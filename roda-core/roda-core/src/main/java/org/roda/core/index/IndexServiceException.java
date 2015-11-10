/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import org.roda.core.common.ServiceException;

public class IndexServiceException extends ServiceException {
  private static final long serialVersionUID = -8493613264790437755L;

  public IndexServiceException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public IndexServiceException(String message, int code) {
    super(message, code);
  }

}
