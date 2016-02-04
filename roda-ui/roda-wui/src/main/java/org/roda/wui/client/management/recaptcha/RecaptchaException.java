package org.roda.wui.client.management.recaptcha;

public class RecaptchaException extends Exception {
  private static final long serialVersionUID = 3392813159441368655L;

  /**
   * Constructs a new {@link RecaptchaException}.
   */
  public RecaptchaException() {
  }

  /**
   * Construct a new {@link RecaptchaException} with the error message.
   * 
   * @param message
   *          the error message.
   */
  public RecaptchaException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link RecaptchaException} with the given cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public RecaptchaException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link RecaptchaException} with the given error message
   * and cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public RecaptchaException(String message, Throwable cause) {
    super(message, cause);
  }
}
