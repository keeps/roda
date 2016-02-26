package org.roda.wui.client.search;

import org.roda.core.data.v2.index.IsIndexed;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SuggestBox;

public class SearchSuggestBox<T extends IsIndexed> extends Composite {

  private SuggestBox suggestBox;

  public SearchSuggestBox(Class<T> classToRequest, String facet) {
    suggestBox = new SuggestBox(new SearchSuggestOracle<T>(classToRequest, facet));
    initWidget(suggestBox);
    suggestBox.addStyleName("form-textbox");
  }

  public String getValue() {
    return suggestBox.getValue();
  }
}
