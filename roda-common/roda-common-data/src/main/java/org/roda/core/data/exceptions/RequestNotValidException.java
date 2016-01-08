/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class RequestNotValidException extends RODAException {
  private static final long serialVersionUID = -2066738446256937178L;

  public RequestNotValidException() {
    super();
  }

  public RequestNotValidException(String msg) {
    super(msg);
  }

  public RequestNotValidException(String message, Throwable cause) {
    super(message, cause);
  }

  public RequestNotValidException(Throwable cause) {
    super(cause);
  }

}