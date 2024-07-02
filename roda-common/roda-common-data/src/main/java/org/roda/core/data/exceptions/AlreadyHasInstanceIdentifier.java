/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.io.Serial;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class AlreadyHasInstanceIdentifier extends RODAException {

  @Serial
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
