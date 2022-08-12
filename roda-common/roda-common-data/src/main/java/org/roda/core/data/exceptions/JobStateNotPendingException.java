/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class JobStateNotPendingException extends RODAException {

  private static final long serialVersionUID = -829610996201922888L;

  public JobStateNotPendingException() {
    super();
  }

  public JobStateNotPendingException(String message) {
    super(message);
  }

  public JobStateNotPendingException(String message, Throwable cause) {
    super(message, cause);
  }

  public JobStateNotPendingException(Throwable cause) {
    super(cause);
  }

}
