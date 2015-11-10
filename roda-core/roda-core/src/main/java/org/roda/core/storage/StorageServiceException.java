/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import org.roda.core.common.ServiceException;

public class StorageServiceException extends ServiceException {

  private static final long serialVersionUID = 473749530367806276L;

  public StorageServiceException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public StorageServiceException(String message, int code) {
    super(message, code);
  }

}
