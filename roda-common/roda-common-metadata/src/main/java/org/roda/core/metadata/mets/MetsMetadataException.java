package org.roda.core.metadata.mets;

import org.roda.core.metadata.MetadataException;

/**
 * Throw to indicate that an error related to METS metadata.
 * 
 * @author Rui Castro
 */
public class MetsMetadataException extends MetadataException {
  private static final long serialVersionUID = -4948710694311868715L;

  /**
   * Constructs an empty {@link MetsMetadataException}.
   */
  public MetsMetadataException() {
  }

  /**
   * Constructs a new {@link MetsMetadataException} with the given error
   * message.
   * 
   * @param message
   *          the error message.
   */
  public MetsMetadataException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link MetsMetadataException} with the given cause
   * exception.
   * 
   * @param cause
   *          the cause exception
   */
  public MetsMetadataException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link MetsMetadataException} with the given message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public MetsMetadataException(String message, Throwable cause) {
    super(message, cause);
  }

}
