/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 * 
 */
public class JobInErrorException extends JobException {
  private static final long serialVersionUID = 668519344542236907L;

  public JobInErrorException() {
    super();
  }

  public JobInErrorException(String message) {
    super(message);
  }

  public JobInErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public JobInErrorException(Throwable cause) {
    super(cause);
  }

}
