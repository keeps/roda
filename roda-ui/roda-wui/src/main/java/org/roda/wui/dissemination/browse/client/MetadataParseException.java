/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.dissemination.browse.client;

import java.util.ArrayList;
import java.util.List;

public class MetadataParseException extends Exception {
  private static final long serialVersionUID = -4784198919577336934L;

  private List<ParseError> errors;

  public MetadataParseException() {
    super();
    errors = new ArrayList<ParseError>();
  }

  public MetadataParseException(String message) {
    super(message);
    errors = new ArrayList<ParseError>();
  }

  public List<ParseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ParseError> errors) {
    this.errors = errors;
  }

}
