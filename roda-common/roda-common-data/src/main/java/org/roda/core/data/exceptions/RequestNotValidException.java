/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import org.roda.core.data.common.RODAException;

public class RequestNotValidException extends RODAException {
  private static final long serialVersionUID = -8801039779198172428L;

  public RequestNotValidException() {
    super();
  }

  public RequestNotValidException(String msg) {
    super(msg);
  }

}