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
public class BadHistoryTokenException extends RODAException {

  /**
   * Create a new bad history token exception
   */
  public BadHistoryTokenException() {
    super();
  }

  /**
   * Create a new bad history token exception
   * 
   * @param message
   */
  public BadHistoryTokenException(String message) {
    super(message);
  }

}
