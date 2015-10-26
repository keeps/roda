/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.model;

import org.roda.common.ServiceException;

public class ModelServiceException extends ServiceException {

  private static final long serialVersionUID = -3970536792438366410L;

  public ModelServiceException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public ModelServiceException(String message, int code) {
    super(message, code);
  }

}
