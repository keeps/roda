/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.validation;

import org.roda.core.data.exceptions.RODAException;

public class ValidationException extends RODAException {
  private static final long serialVersionUID = -7922205193060735117L;

  private ValidationReport report;

  public ValidationException() {
    super();
    report = new ValidationReport();
    report.setValid(false);
  }

  public ValidationException(String message) {
    super(message);
    report = new ValidationReport();
    report.setValid(false);
    report.setMessage(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
    report = new ValidationReport();
    report.setValid(false);
    report.setMessage(message);
  }

  public ValidationException(Throwable cause) {
    super(cause);
    report = new ValidationReport();
    report.setValid(false);
  }

  public ValidationException(ValidationReport report) {
    super(report.getMessage() != null ? report.getMessage() : report.getIssues().toString());
    this.report = report;
  }

  public ValidationReport getReport() {
    return report;
  }

  public void setReport(ValidationReport report) {
    this.report = report;
  }

}
