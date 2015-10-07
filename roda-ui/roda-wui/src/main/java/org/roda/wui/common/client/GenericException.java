/**
 * 
 */
package org.roda.wui.common.client;

import org.roda.core.common.RODAException;

/**
 * @author Luis Faria
 * 
 */
public class GenericException extends RODAException {

  private static final long serialVersionUID = 1541272912846000235L;

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
