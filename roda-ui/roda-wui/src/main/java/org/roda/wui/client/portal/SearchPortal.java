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

import java.util.Arrays;
import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.search.CatalogueSearch;
import org.roda.wui.common.client.HistoryResolver;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SearchPortal extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 1) {
        SearchWithPreFiltersPortal.resolveToNewInstance(historyTokens, callback);
      } else {
        getInstance().resolve(callback);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLoginPortal.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "search";
    }
  };

  private static SearchPortal instance = null;

  interface MyUiBinder extends UiBinder<Widget, SearchPortal> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  CatalogueSearch catalogueSearch;

  private SearchPortal() {
    catalogueSearch = new CatalogueSearch(true, "Search_AIPs", "Search_representations", "Search_files", null, true,
      false);
    initWidget(uiBinder.createAndBindUi(this));
  }

  public static SearchPortal getInstance() {
    if (instance == null) {
      instance = new SearchPortal();
    }
    return instance;
  }

  public void resolve(AsyncCallback<Widget> callback) {
    callback.onSuccess(this);
  }
}
