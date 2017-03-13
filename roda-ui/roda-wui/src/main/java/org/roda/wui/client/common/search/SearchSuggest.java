/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.io.Serializable;

import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class SearchSuggest implements Suggestion, Serializable, Comparable<SearchSuggest> {
  private static final long serialVersionUID = -4292378982124160066L;
  private String displayString;
  private String replacementString;
  private int order;

  public SearchSuggest() {
    super();
  }

  public SearchSuggest(String replacement, String display, int order) {
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

  @Override
  public int compareTo(SearchSuggest o) {
    return getReplacementString().compareTo(o.getReplacementString());
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o != null) {
      if (o instanceof SearchSuggest) {
        SearchSuggest suggest = (SearchSuggest) o;
        return getReplacementString().equals(suggest.getReplacementString());
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
