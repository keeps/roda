package org.roda.index;

import org.roda.common.ServiceException;

public class IndexServiceException extends ServiceException {
  private static final long serialVersionUID = -8493613264790437755L;

  public IndexServiceException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public IndexServiceException(String message, int code) {
    super(message, code);
  }

}
