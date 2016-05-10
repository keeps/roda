package org.roda.core.plugins.orchestrate;

import org.roda.core.data.exceptions.RODAException;

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
