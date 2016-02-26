package org.roda.wui.client.search;

import java.io.Serializable;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class SearchSuggestion implements Suggestion, Serializable, Comparable<SearchSuggestion> {
  private static final long serialVersionUID = -4292378982124160066L;
  private String displayString;
  private String replacementString;
  private int order;

  public SearchSuggestion() {
    super();
  }

  public SearchSuggestion(String replacement, String display, int order) {
    super();
    this.displayString = display;
    this.replacementString = replacement;
    this.order = order;
  }

  public String getDisplayString() {
    return displayString;
  }

  public void setDisplayString(String displayString) {
    this.displayString = displayString;
  }

  public String getReplacementString() {
    return replacementString;
  }

  public void setReplacementString(String replacementString) {
    this.replacementString = replacementString;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int compareTo(SearchSuggestion o) {
    return getReplacementString().compareTo(o.getReplacementString());
  }

  public boolean equals(SearchSuggestion o) {
    return getReplacementString().equals(o.getReplacementString());
  }
}
