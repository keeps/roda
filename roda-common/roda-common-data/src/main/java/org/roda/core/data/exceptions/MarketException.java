package org.roda.core.data.exceptions;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MarketException extends RODAException {
  private static final long serialVersionUID = 5663037203596674543L;

  public MarketException() {
    super();
  }

  public MarketException(String message) {
    super(message);
  }

  public MarketException(String message, Throwable cause) {
    super(message, cause);
  }

  public MarketException(Throwable cause) {
    super(cause);
  }
}
