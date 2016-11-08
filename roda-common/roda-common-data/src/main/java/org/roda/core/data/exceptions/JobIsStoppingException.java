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
public class JobIsStoppingException extends JobException {
  private static final long serialVersionUID = 668519344542236907L;

  public JobIsStoppingException() {
    super();
  }

  public JobIsStoppingException(String message) {
    super(message);
  }

  public JobIsStoppingException(String message, Throwable cause) {
    super(message, cause);
  }

  public JobIsStoppingException(Throwable cause) {
    super(cause);
  }

}
