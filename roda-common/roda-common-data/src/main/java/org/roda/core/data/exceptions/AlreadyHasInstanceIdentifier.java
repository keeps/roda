package org.roda.core.data.exceptions;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class AlreadyHasInstanceIdentifier extends RODAException {

  private static final long serialVersionUID = -6744205569453461540L;

  public AlreadyHasInstanceIdentifier() {
    super();
  }

  public AlreadyHasInstanceIdentifier(String message) {
    super(message);
  }

  public AlreadyHasInstanceIdentifier(String message, Throwable cause) {
    super(message, cause);
  }

  public AlreadyHasInstanceIdentifier(Throwable cause) {
    super(cause);
  }

}
