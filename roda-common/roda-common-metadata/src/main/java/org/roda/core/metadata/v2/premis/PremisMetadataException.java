/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.metadata.v2.premis;

import org.roda.core.metadata.MetadataException;

/**
 * Throw to indicate that an error related to PREMIS metadata.
 * 
 * @author Rui Castro
 */
public class PremisMetadataException extends MetadataException {
  private static final long serialVersionUID = -4948710694311868715L;

  /**
   * Constructs an empty {@link PremisMetadataException}.
   */
  public PremisMetadataException() {
  }

  /**
   * Constructs a new {@link PremisMetadataException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public PremisMetadataException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link PremisMetadataException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception
   */
  public PremisMetadataException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link PremisMetadataException} with the given message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public PremisMetadataException(String message, Throwable cause) {
    super(message, cause);
  }

}
