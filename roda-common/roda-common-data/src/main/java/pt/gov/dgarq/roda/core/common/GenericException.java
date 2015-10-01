package pt.gov.dgarq.roda.core.common;

/**
 * 
 */

/**
 * @author Luis Faria
 * 
 */
public class GenericException extends RODAException {

  private static final long serialVersionUID = -2093798996190248576L;

  public GenericException() {
    super();
  }

  public GenericException(String message) {
    super(message);
  }

  public GenericException(String message, GenericException e) {
    super(message, e);
  }

}
