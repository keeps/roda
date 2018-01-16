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
 */
public class NotImplementedException extends RODAException {
  private static final long serialVersionUID = -6744205569453461540L;

  public NotImplementedException() {
    super();
  }

  public NotImplementedException(String message) {
    super(message);
  }

  public NotImplementedException(String message, NotImplementedException e) {
    super(message, e);
  }

}
