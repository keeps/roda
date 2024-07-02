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
public class NotSupportedException extends RODAException {
  @Serial
  private static final long serialVersionUID = -1656013708463576500L;

  public NotSupportedException() {
    super();
  }

  public NotSupportedException(String message) {
    super(message);
  }

  public NotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotSupportedException(Throwable cause) {
    super(cause);
  }

}
