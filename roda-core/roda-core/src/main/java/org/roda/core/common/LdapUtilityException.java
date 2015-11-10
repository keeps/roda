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
package org.roda.core.common;

import org.roda.core.data.common.RODAException;

/**
 * @author Rui Castro
 */
public class LdapUtilityException extends RODAException {
  private static final long serialVersionUID = -8003555284069536972L;

  /**
   * Constructs a new LDAPUtilityException.
   */
  public LdapUtilityException() {
  }

  /**
   * Constructs a new {@link LdapUtilityException} with the given message.
   * 
   * @param message
   */
  public LdapUtilityException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link LdapUtilityException} with the given cause
   * Exception.
   * 
   * @param cause
   */
  public LdapUtilityException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link LdapUtilityException} with the given message and
   * cause Exception.
   * 
   * @param message
   * @param cause
   */
  public LdapUtilityException(String message, Throwable cause) {
    super(message, cause);
  }

}
