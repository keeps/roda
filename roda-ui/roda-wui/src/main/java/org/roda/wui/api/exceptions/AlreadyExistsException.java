/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.exceptions;

public class AlreadyExistsException extends ApiException {
  private static final long serialVersionUID = -8801039779198172428L;

  public AlreadyExistsException(int code, String msg) {
    super(code, msg);
  }
}
