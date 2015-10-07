package org.roda.wui.dissemination.search.client;

import java.io.Serializable;

public class SearchField implements Serializable {
  private static final long serialVersionUID = -2809811191632936028L;
  private String field;
  private String label;
  private String type;

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
    return "SearchField [field=" + field + ", label=" + label + ", type=" + type + "]";
  }

}
