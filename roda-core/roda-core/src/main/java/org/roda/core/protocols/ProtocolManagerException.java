/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.protocols;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.plugins.PluginManager;

/**
 * Thrown to indicate that some went wrong inside a {@link ProtocolManager}.
 * 
 * @author Rui Castro
 */
public class ProtocolManagerException extends RODAException {
  private static final long serialVersionUID = -3251830859832250709L;

  /**
   * Constructs a new {@link ProtocolManagerException}.
   */
  public ProtocolManagerException() {
    // do nothing
  }

  /**
   * Constructs a new {@link ProtocolManagerException} with the given error message.
   *
   * @param message
   *          the error message.
   */
  public ProtocolManagerException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link ProtocolManagerException} with the given cause
   * exception.
   *
   * @param cause
   *          the cause exception.
   */
  public ProtocolManagerException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link ProtocolManagerException} with the given error message
   * and cause exception.
   *
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public ProtocolManagerException(String message, Throwable cause) {
    super(message, cause);
  }

}
