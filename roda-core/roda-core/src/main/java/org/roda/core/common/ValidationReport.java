/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.ArrayList;
import java.util.List;

public class ValidationReport {

  private List<String> issues;
  private boolean valid;

  public ValidationReport() {
    this.valid = true;
    this.issues = new ArrayList<String>();
  }

  public List<String> getIssues() {
    return issues;
  }

  public void setIssues(List<String> issues) {
    this.issues = issues;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public void addIssue(String issue) {
    if (issues == null) {
      issues = new ArrayList<String>();
    }
    issues.add(issue);
  }
}