/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class ActionForbiddenException extends Exception {

  private static final long serialVersionUID = -6744205569453461540L;

  public ActionForbiddenException() {
    super();
  }

  public ActionForbiddenException(String message) {
    super(message);
  }

  public ActionForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  public ActionForbiddenException(Throwable cause) {
    super(cause);
  }
  
  


}
