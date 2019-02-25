/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.portal;

import java.util.List;

import org.roda.wui.client.common.search.CatalogueSearch;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SearchWithPreFiltersPortal extends Composite {
  public static void resolveToNewInstance(List<String> historyTokens, AsyncCallback<Widget> callback) {
    callback.onSuccess(getInstance(historyTokens));
  }

  interface MyUiBinder extends UiBinder<Widget, SearchWithPreFiltersPortal> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static SearchWithPreFiltersPortal instance = null;

  private static SearchWithPreFiltersPortal getInstance(List<String> historyTokens) {
    if (instance == null || !historyTokens.equals(instance.historyTokens)) {
      instance = new SearchWithPreFiltersPortal(historyTokens);
    }
    return instance;
  }

  private final List<String> historyTokens;

  @UiField(provided = true)
  CatalogueSearch catalogueSearch;

  private SearchWithPreFiltersPortal(List<String> historyTokens) {
    this.historyTokens = historyTokens;
    catalogueSearch = new CatalogueSearch(historyTokens, true, "Search_AIPs", "Search_representations", "Search_files",
      null, false, true);
    initWidget(uiBinder.createAndBindUi(this));
  }
}
