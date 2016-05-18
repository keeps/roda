/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

public class TimeoutJobException extends JobException {
  private static final long serialVersionUID = 1893131595923947285L;

  public TimeoutJobException() {
    super();
  }

  public TimeoutJobException(String message) {
    super(message);
  }

  public TimeoutJobException(String message, Throwable cause) {
    super(message, cause);
  }

  public TimeoutJobException(Throwable cause) {
    super(cause);
  }
}
