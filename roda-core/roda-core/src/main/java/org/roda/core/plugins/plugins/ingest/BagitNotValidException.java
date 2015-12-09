/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

public class BagitNotValidException extends Exception {
  private static final long serialVersionUID = -6425654163548447394L;

  public BagitNotValidException() {
    super();
  }

  public BagitNotValidException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public BagitNotValidException(String message, Throwable cause) {
    super(message, cause);
  }

  public BagitNotValidException(String message) {
    super(message);
  }

  public BagitNotValidException(Throwable cause) {
    super(cause);
  }

  
}
