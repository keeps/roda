/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import java.io.Serializable;

public class SearchField implements Serializable {
  private static final long serialVersionUID = -2809811191632936028L;
  private String field;
  private String label;
  private String type;
  private boolean fixed;

  public boolean isFixed() {
    return fixed;
  }

  public void setFixed(boolean fixed) {
    this.fixed = fixed;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "SearchField [field=" + field + ", label=" + label + ", type=" + type + ", fixed=" + fixed + "]";
  }

}
