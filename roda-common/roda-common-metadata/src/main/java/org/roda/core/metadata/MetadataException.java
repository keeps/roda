/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.metadata;

import org.roda.core.data.common.RODAException;

/**
 * Thrown to indicate that an error related with RODA Metadata as occurred.
 * 
 * @author Rui Castro
 */
public class MetadataException extends RODAException {

  private static final long serialVersionUID = 7511204911535262249L;

  /**
   * Constructs an empty {@link MetadataException}.
   */
  public MetadataException() {
  }

  /**
   * Constructs a new {@link MetadataException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public MetadataException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link MetadataException} with the given cause exception.
   * 
   * @param cause
   *          the cause exception
   */
  public MetadataException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link MetadataException} with the given message and cause
   * exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public MetadataException(String message, Throwable cause) {
    super(message, cause);
  }

}
