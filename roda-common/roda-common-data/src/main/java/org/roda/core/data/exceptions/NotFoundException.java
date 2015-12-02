/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import org.roda.core.data.common.RODAException;

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


}
