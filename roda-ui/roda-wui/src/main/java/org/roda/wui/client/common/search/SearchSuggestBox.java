/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import org.roda.core.data.v2.index.IsIndexed;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestBox;

public class SearchSuggestBox<T extends IsIndexed> extends Composite {

  private SuggestBox suggestBox;
  private boolean allowPartial;

  public SearchSuggestBox(Class<T> classToRequest, String facet, boolean allowPartial) {
    this.allowPartial = allowPartial;
    suggestBox = new SuggestBox(new SearchSuggestOracle<T>(classToRequest, facet, allowPartial));
    initWidget(suggestBox);
    suggestBox.addStyleName("form-textbox");
    suggestBox.setAutoSelectEnabled(true);
  }

  public String getValue() {
    return suggestBox.getValue();
  }

  public void setValue(String value) {
    suggestBox.setValue(value);
  }

  public boolean isAllowPartial() {
    return allowPartial;
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return suggestBox.addValueChangeHandler(handler);
  }

  public void setFocus(boolean focused) {
    suggestBox.setFocus(focused);
  }
}
