/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RetentionPeriodCalculationException extends RODAException {
  private static final long serialVersionUID = -834809453076076692L;

  public RetentionPeriodCalculationException() {
    super();
  }

  public RetentionPeriodCalculationException(String message) {
    super(message);
  }

  public RetentionPeriodCalculationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RetentionPeriodCalculationException(Throwable cause) {
    super(cause);
  }
}
