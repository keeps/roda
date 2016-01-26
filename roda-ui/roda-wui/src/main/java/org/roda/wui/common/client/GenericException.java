/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client;

import org.roda.core.data.exceptions.RODAException;

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
