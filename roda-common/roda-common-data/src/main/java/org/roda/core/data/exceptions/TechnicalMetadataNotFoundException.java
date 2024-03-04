/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.exceptions;

import java.io.Serial;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */

public class TechnicalMetadataNotFoundException extends RODAException{

  @Serial
  private static final long serialVersionUID = 3820597384560474275L;

  public TechnicalMetadataNotFoundException() {
    super();
  }

  public TechnicalMetadataNotFoundException(String message) {
    super(message);
  }

  public TechnicalMetadataNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public TechnicalMetadataNotFoundException(Throwable cause) {
    super(cause);
  }
}
