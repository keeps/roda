/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

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
