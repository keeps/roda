/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.validation;

import java.io.Serializable;

public class ValidationIssue implements Serializable {

  private static final long serialVersionUID = -7269527212240870004L;

  private String message;
  private int lineNumber;
  private int columnNumber;

  public ValidationIssue() {
    super();
  }

  public ValidationIssue(String message) {
    super();
    setMessage(message);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

  public void setColumnNumber(int columnNumber) {
    this.columnNumber = columnNumber;
  }

  @Override
  public String toString() {
    return "ValidationIssue [message=" + message + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber
      + "]";
  }

}
