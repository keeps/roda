/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.model;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXParseException;

public class ValidationException extends Exception {
  private static final long serialVersionUID = -7922205193060735117L;

  private List<SAXParseException> errors;

  public ValidationException(String message) {
    super(message);
    this.errors = new ArrayList<SAXParseException>();
  }

  public ValidationException(String message, List<SAXParseException> errors) {
    super(message);
    this.errors = errors;
  }

  public List<SAXParseException> getErrors() {
    return errors;
  }

  public void setErrors(List<SAXParseException> errors) {
    this.errors = errors;
  }

}
