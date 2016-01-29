/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.validation;

import java.io.Serializable;

import org.roda.core.data.exceptions.RODAException;

public class ValidationException extends RODAException {
  private static final long serialVersionUID = -7922205193060735117L;

  private ValidationReport<? extends Serializable> report;

  public ValidationException(String message) {
    super(message);
    report = new ValidationReport<>();
    report.setValid(false);
    report.setMessage(message);
  }

  public ValidationException(ValidationReport<? extends Serializable> report) {
    super(report.getMessage() != null ? report.getMessage() : report.getIssues().toString());
    this.report = report;
  }

  public ValidationReport<? extends Serializable> getReport() {
    return report;
  }

  public void setReport(ValidationReport<? extends Serializable> report) {
    this.report = report;
  }

}
