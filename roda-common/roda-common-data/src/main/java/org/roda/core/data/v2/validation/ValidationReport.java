/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ValidationReport implements Serializable {
  private static final long serialVersionUID = 4670965331325954161L;

  private List<ValidationIssue> issues;
  private boolean valid;
  private String message;

  public ValidationReport() {
    this.valid = true;
    this.issues = new ArrayList<>();
  }

  public List<ValidationIssue> getIssues() {
    return issues;
  }

  public void setIssues(List<ValidationIssue> issues) {
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

  public void addIssue(ValidationIssue issue) {
    if (issues == null) {
      issues = new ArrayList<>();
    }
    issues.add(issue);
  }

  @Override
  public String toString() {
    return "ValidationReport [valid=" + valid + ", message=" + message + ", issues=" + issues + "]";
  }

  public String toHtml() {
    return toHtml(true, true);
  }

  public String toHtml(boolean fullHtml, boolean addDefaultCss) {
    return toHtml(fullHtml, addDefaultCss, true, "");
  }

  public String toHtml(boolean fullHtml, boolean addDefaultCss, boolean addIssueValues, String title) {

    StringBuilder sb = new StringBuilder();
    if (fullHtml) {
      sb.append("<html>");
      sb.append("<head>");
      sb.append("<title>Validation report</title>");
      sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
      if (addDefaultCss) {
        sb.append(getDefaultCss());
      }
      sb.append("</head>");
      sb.append("<body>");
      sb.append("<h1>Validation report</h1>");
    }

    // open report
    sb.append(getDivBeginning("report"));

    // is it valid?
    if (title.isEmpty()) {
      getValidationEntryAttribute(sb, "valid", "Is valid?", isValid() ? "yes" : "no");
    } else {
      getValidationEntryAttribute(sb, "valid", title, "");
    }

    if (message != null && !message.trim().isEmpty()) {
      sb.append(getDivBeginning("message"));
      // FIXME lfaria 20161018: escape HTML
      sb.append(message);
      sb.append(getDivEnding());
    }

    // add validation entries
    sb.append(getDivBeginning("entries"));
    for (ValidationIssue validationEntry : issues) {
      sb.append(getValidationEntryDiv(validationEntry, addIssueValues));
    }
    sb.append(getDivEnding());

    // close report
    sb.append(getDivEnding());

    if (fullHtml) {
      // wrap up
      sb.append("</body>");
      sb.append("</html>");
    }
    return sb.toString();
  }

  private String getDivBeginning(String classString) {
    return "<div class=\"" + classString + "\">";
  }

  private String getDivEnding() {
    return "</div>";
  }

  private String getValidationEntryDiv(ValidationIssue validationEntry, boolean addIssueValues) {
    StringBuilder sb = new StringBuilder();
    sb.append(getDivBeginning("entry " + "level_error"));
    // message
    getValidationEntryAttribute(sb, "message", "Message", validationEntry.getMessage());

    if (addIssueValues) {
      // line number
      getValidationEntryAttribute(sb, "line_number", "Line number", Integer.toString(validationEntry.getLineNumber()));

      // column number
      getValidationEntryAttribute(sb, "column_number", "Column number",
        Integer.toString(validationEntry.getColumnNumber()));
    }

    sb.append(getDivEnding());
    return sb.toString();
  }

  private void getValidationEntryAttribute(StringBuilder sb, String attrClass, String label, String value) {
    sb.append(getDivBeginning("entry_attr " + attrClass));
    sb.append(getDivBeginning("label"));
    sb.append(label);
    sb.append(getDivEnding());
    sb.append(getDivBeginning("value"));
    sb.append(value);
    sb.append(getDivEnding());
    sb.append(getDivEnding());
  }

  private String getDefaultCss() {
    StringBuilder sb = new StringBuilder();
    sb.append("<style>");
    sb.append(
      ".valid {border-bottom: 1px solid black; border-left: 2px solid black;; margin-bottom: 10px; padding: 5px;} ");
    sb.append(".entry {display: block; margin-bottom: 10px;} ");
    sb.append(".entry div, .valid div {padding-left: 5px; padding-right: 5px;} ");
    sb.append(".level_info {border-bottom: 1px solid blue; border-left: 2px solid blue;} ");
    sb.append(".level_warning {border-bottom: 1px solid orange; border-left: 2px solid orange;} ");
    sb.append(".level_error {border-bottom: 1px solid red; border-left: 2px solid red;} ");
    sb.append(".entry .label, .valid .label {font-weight: bold;} ");
    sb.append(".entry .label, .entry .value, .valid .label, .valid .value {display: inline; } ");
    sb.append("</style>");
    return sb.toString();
  }

}