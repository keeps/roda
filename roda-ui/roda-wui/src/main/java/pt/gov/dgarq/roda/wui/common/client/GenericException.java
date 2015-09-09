/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * @author Luis Faria
 * 
 */
public class GenericException extends RODAException {

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
