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
package org.roda.wui.client.search;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.search.CatalogueSearch;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class SearchWithPreFilters extends Composite {

  // Used by Search.RESOLVER

  private static SearchWithPreFilters instance = null;

  interface MyUiBinder extends UiBinder<Widget, SearchWithPreFilters> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel searchDescription;

  @UiField(provided = true)
  CatalogueSearch catalogueSearch;

  private SearchWithPreFilters() {
    // Create main search
    catalogueSearch = new CatalogueSearch(true, "Search_AIPs", "Search_representations", "Search_files", null,
      AIPState.ACTIVE, null, false);

    initWidget(uiBinder.createAndBindUi(this));
    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));
  }

  public static SearchWithPreFilters getInstance() {
    if (instance == null) {
      instance = new SearchWithPreFilters();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    catalogueSearch.setFilters(historyTokens);
    callback.onSuccess(this);
  }
}
