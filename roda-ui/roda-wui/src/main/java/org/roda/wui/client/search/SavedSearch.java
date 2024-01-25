/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import java.util.List;

import org.roda.wui.client.common.SubTitlePanel;
import org.roda.wui.client.common.search.CatalogueSearch;
import org.roda.wui.client.common.search.RODASavedSearch;
import org.roda.wui.client.common.search.SavedSearchMapper;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarãese <mguimaraes@keep.pt>
 */
public class SavedSearch extends Composite {
  // Used by Search.RESOLVER
  public static void resolveToNewInstance(List<String> historyTokens, AsyncCallback<Widget> callback) {
    callback.onSuccess(getInstance(historyTokens));
    String titleFromHistoryTokens = getTitleFromHistoryTokens(historyTokens);
    Window.setTitle(messages.windowTitle(titleFromHistoryTokens.toUpperCase()));
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, SavedSearch> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static SavedSearch instance = null;

  private static SavedSearch getInstance(List<String> historyTokens) {
    // also create a new instance if the historyTokens have changed
    if (instance == null || !historyTokens.equals(instance.historyTokens)) {
      instance = new SavedSearch(historyTokens);
    }
    return instance;
  }

  private final List<String> historyTokens;

  @UiField
  FlowPanel searchDescription;

  @UiField
  FlowPanel savedSearchStaticDescription;

  @UiField
  SubTitlePanel subTitlePanel;

  @UiField(provided = true)
  CatalogueSearch catalogueSearch;

  private SavedSearch(List<String> historyTokens) {
    this.historyTokens = historyTokens;
    String titleFromHistoryTokens = getTitleFromHistoryTokens(historyTokens);

    // Create main search
    catalogueSearch = new CatalogueSearch(historyTokens, true, "Search_AIPs", "Search_representations", "Search_files",
      null, false, true);

    initWidget(uiBinder.createAndBindUi(this));

    subTitlePanel.setText(titleFromHistoryTokens);
    searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));
    savedSearchStaticDescription.add(new HTMLWidgetWrapper("SavedSearchDescription.html"));
  }

  private static String getTitleFromHistoryTokens(List<String> historyTokens) {
    String jsonValue = JavascriptUtils.decodeBase64(historyTokens.get(1));
    SavedSearchMapper mapper = GWT.create(SavedSearchMapper.class);
    RODASavedSearch savedSearch = mapper.read(jsonValue);

    return savedSearch.getTitle();
  }
}
