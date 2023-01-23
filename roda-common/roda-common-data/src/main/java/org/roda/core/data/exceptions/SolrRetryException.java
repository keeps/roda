package org.roda.core.data.exceptions;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SolrRetryException extends RODAException {
  @Serial
  private static final long serialVersionUID = 3920079022473638105L;

  public SolrRetryException() {
    super();
  }

  public SolrRetryException(String message) {
    super(message);
  }

  public SolrRetryException(Throwable cause) {
    super(cause);
  }

  public SolrRetryException(String message, Throwable cause) {
    super(message, cause);
  }
}
