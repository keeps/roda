package pt.gov.dgarq.roda.core.common;

/**
 * 
 */

/**
 * @author Luis Faria
 * 
 */
public class NotFoundException extends RODAException {

  private static final long serialVersionUID = -6744205569453461540L;

  public NotFoundException() {
    super();
  }

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(String message, NotFoundException e) {
    super(message, e);
  }

}
