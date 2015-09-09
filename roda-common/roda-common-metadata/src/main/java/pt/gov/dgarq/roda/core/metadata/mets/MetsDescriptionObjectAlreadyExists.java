package pt.gov.dgarq.roda.core.metadata.mets;

/**
 * Thrown to indicate that a description object, that was being inserted,
 * already existed.
 * 
 * @author Rui Castro
 */
public class MetsDescriptionObjectAlreadyExists extends MetsMetadataException {
  private static final long serialVersionUID = -5637146041457254831L;

  /**
   * Constructs an empty {@link MetsDescriptionObjectAlreadyExists}.
   */
  public MetsDescriptionObjectAlreadyExists() {
  }

  /**
   * Constructs a new {@link MetsDescriptionObjectAlreadyExists} with the given
   * error message.
   * 
   * @param message
   *          the error message.
   */
  public MetsDescriptionObjectAlreadyExists(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link MetsDescriptionObjectAlreadyExists} with the given
   * cause exception.
   * 
   * @param cause
   *          the cause exception
   */
  public MetsDescriptionObjectAlreadyExists(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link MetsDescriptionObjectAlreadyExists} with the given
   * message and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public MetsDescriptionObjectAlreadyExists(String message, Throwable cause) {
    super(message, cause);
  }

}
