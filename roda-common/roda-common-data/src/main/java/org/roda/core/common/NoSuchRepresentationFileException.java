/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import org.roda.core.common.RODAException;
import org.roda.core.data.RepresentationFile;

/**
 * Thrown to indicate that the specified {@link RepresentationFile} doesn't
 * exist.
 * 
 * @author Rui Castro
 */
public class NoSuchRepresentationFileException extends RODAException {
  private static final long serialVersionUID = -5885736782330328511L;

  /**
   * Constructs a new {@link NoSuchRepresentationFileException}.
   */
  public NoSuchRepresentationFileException() {
  }

  /**
   * Constructs a new {@link NoSuchRepresentationFileException} with the given
   * error message.
   * 
   * @param message
   *          the error message.
   */
  public NoSuchRepresentationFileException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link NoSuchRepresentationFileException} with the given
   * cause.
   * 
   * @param cause
   *          the cause exception.
   */
  public NoSuchRepresentationFileException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link NoSuchRepresentationFileException} with the given
   * error message and cause.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public NoSuchRepresentationFileException(String message, Throwable cause) {
    super(message, cause);
  }

}
