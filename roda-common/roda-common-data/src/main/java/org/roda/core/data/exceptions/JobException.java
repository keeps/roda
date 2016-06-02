/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

public class JobException extends RODAException {
  private static final long serialVersionUID = 1893131595923947285L;

  public JobException() {
    super();
  }

  public JobException(String message) {
    super(message);
  }

  public JobException(String message, Throwable cause) {
    super(message, cause);
  }

  public JobException(Throwable cause) {
    super(cause);
  }
}
