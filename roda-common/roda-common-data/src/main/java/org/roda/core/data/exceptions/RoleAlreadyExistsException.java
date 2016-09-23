/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * Thrown to indicate that a role with the same name already exists when a new
 * one is trying to be created.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class RoleAlreadyExistsException extends AlreadyExistsException {

  private static final long serialVersionUID = -6214944667650454428L;

  /**
   * Constructs a new {@link RoleAlreadyExistsException}.
   */
  public RoleAlreadyExistsException() {
  }

  /**
   * Constructs a new {@link RoleAlreadyExistsException} with the given error
   * message.
   *
   * @param message
   *          the error message
   */
  public RoleAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link RoleAlreadyExistsException} with the given cause
   * exception.
   *
   * @param cause
   *          the cause exception
   */
  public RoleAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link RoleAlreadyExistsException} with the given error
   * message and cause exception.
   *
   * @param message
   *          the error message
   * @param cause
   *          the cause exception
   */
  public RoleAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

}
