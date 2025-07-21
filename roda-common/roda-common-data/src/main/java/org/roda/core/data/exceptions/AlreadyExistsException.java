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
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class AlreadyExistsException extends RODAException {

  @Serial
  private static final long serialVersionUID = -6744205569453461540L;

  public AlreadyExistsException() {
    super();
  }

  public AlreadyExistsException(String message) {
    super(message);
  }

  public AlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public AlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
