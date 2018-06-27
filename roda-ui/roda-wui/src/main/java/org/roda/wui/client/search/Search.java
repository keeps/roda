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

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.search.MainSearch;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
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
public class Search extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
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

  private static Search instance = null;

  interface MyUiBinder extends UiBinder<Widget, Search> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel searchDescription;

  @UiField(provided = true)
  MainSearch mainSearch;

  boolean justActive = true;
  boolean itemsSelectable = true;
  boolean representationsSelectable = true;
  boolean filesSelectable = true;

  private Search() {
    // Create main search
    String parentAipId = null;
    mainSearch = new MainSearch(justActive, itemsSelectable, representationsSelectable, filesSelectable, "Search_AIPs",
      "Search_representations", "Search_files", parentAipId, AIPState.ACTIVE);

    initWidget(uiBinder.createAndBindUi(this));
    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));
  }

  public static Search getInstance() {
    if (instance == null) {
      instance = new Search();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    mainSearch.defaultFilters();
    if (historyTokens.isEmpty()) {
      mainSearch.search(false);
      callback.onSuccess(this);
    } else {
      // #search/TYPE/key/value/key/value
      boolean successful = mainSearch.setSearch(historyTokens);
      if (successful) {
        mainSearch.search(true);
        callback.onSuccess(this);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }
  }

  public void clearSelected() {
    mainSearch.clearSelected();
  }

  @Deprecated
  public SelectedItems<? extends IsIndexed> getSelected() {
    return mainSearch.getSelected();
  }

}
