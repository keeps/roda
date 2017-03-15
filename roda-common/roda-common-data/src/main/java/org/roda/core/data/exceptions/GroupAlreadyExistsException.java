/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import org.roda.core.data.v2.user.Group;

/**
 * Thrown to indicate that a {@link Group} with the same name already exists
 * when a new one is trying to be created.
 * 
 * @author Rui Castro
 */
public class GroupAlreadyExistsException extends AlreadyExistsException {

  private static final long serialVersionUID = 6493339963861919270L;

  /**
   * Constructs a new GroupAlreadyExistsException.
   */
  public GroupAlreadyExistsException() {
    // do nothing
  }

  /**
   * Constructs a new GroupAlreadyExistsException with the given error message.
   * 
   * @param message
   *          the error message
   */
  public GroupAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Constructs a new GroupAlreadyExistsException with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception
   */
  public GroupAlreadyExistsException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new GroupAlreadyExistsException with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message
   * @param cause
   *          the cause exception
   */
  public GroupAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

}
