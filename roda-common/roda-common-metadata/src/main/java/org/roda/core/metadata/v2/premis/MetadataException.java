package org.roda.core.metadata.v2.premis;

/**
 * Throw to indicate that an error related to PREMIS metadata.
 * 
 * @author Rui Castro
 */
public class MetadataException extends Exception {
  private static final long serialVersionUID = -4948710694311868715L;

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
