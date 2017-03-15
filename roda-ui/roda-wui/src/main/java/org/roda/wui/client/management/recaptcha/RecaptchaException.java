/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.recaptcha;

public class RecaptchaException extends Exception {
  private static final long serialVersionUID = 3392813159441368655L;

  /**
   * Constructs a new {@link RecaptchaException}.
   */
  public RecaptchaException() {
    // do nothing
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
