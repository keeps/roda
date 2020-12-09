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
