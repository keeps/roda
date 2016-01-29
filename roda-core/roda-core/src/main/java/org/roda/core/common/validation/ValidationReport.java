/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationReport<T extends Serializable> {

  private List<T> issues;
  private boolean valid;
  private String message;

  public ValidationReport() {
    this.valid = true;
    this.issues = new ArrayList<T>();
  }

  public List<T> getIssues() {
    return issues;
  }

  public void setIssues(List<T> issues) {
    this.issues = issues;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void addIssue(T issue) {
    if (issues == null) {
      issues = new ArrayList<T>();
    }
    issues.add(issue);
  }
}