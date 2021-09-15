/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class InstanceIdNotUpdated extends RODAException {
  private static final long serialVersionUID = -1656013708463576500L;

  public InstanceIdNotUpdated() {
    super();
  }

  public InstanceIdNotUpdated(String message) {
    super(message);
  }

  public InstanceIdNotUpdated(String message, Throwable cause) {
    super(message, cause);
  }

  public InstanceIdNotUpdated(Throwable cause) {
    super(cause);
  }

}
