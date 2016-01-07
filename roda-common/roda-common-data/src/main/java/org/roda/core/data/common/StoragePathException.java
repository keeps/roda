/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

import org.roda.core.data.exceptions.GenericException;

public class StoragePathException extends RODAException {

  private static final long serialVersionUID = -3242810946238751526L;

  public StoragePathException() {
    super();
  }

  public StoragePathException(String message) {
    super(message);
  }

  public StoragePathException(String message, GenericException e) {
    super(message, e);
  }
}
