/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class AuthorizationDeniedException extends RODAException {

  private static final long serialVersionUID = -6744205569453461540L;

  private List<String> missingRoles = new ArrayList<>();

  public AuthorizationDeniedException() {
    super();
  }

  public AuthorizationDeniedException(String message) {
    super(message);
  }

  public AuthorizationDeniedException(String message, List<String> missingRoles) {
    super(message);
    setMissingRoles(missingRoles);
  }

  public AuthorizationDeniedException(String message, Throwable cause) {
    super(message, cause);
  }

  public AuthorizationDeniedException(Throwable cause) {
    super(cause);
  }

  public List<String> getMissingRoles() {
    return missingRoles;
  }

  public void setMissingRoles(List<String> missingRoles) {
    this.missingRoles = missingRoles;
  }

}
