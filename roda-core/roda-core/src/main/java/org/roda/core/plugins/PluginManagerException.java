/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import org.roda.core.data.exceptions.RODAException;

/**
 * Thrown to indicate that some went wrong inside a {@link PluginManager}.
 * 
 * @author Rui Castro
 */
public class PluginManagerException extends RODAException {
  private static final long serialVersionUID = 3329949098345675989L;

  /**
   * Constructs a new {@link PluginManagerException}.
   */
  public PluginManagerException() {
    // do nothing
  }

  /**
   * Constructs a new {@link PluginManagerException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public PluginManagerException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link PluginManagerException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public PluginManagerException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link PluginManagerException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public PluginManagerException(String message, Throwable cause) {
    super(message, cause);
  }

}
