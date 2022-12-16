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

import java.util.List;
import java.util.Optional;

import com.google.gwt.user.client.Window;
import config.i18n.client.ClientMessages;
import org.roda.wui.client.common.SubTitlePanel;
import org.roda.wui.client.common.search.CatalogueSearch;
import org.roda.wui.common.client.tools.StringUtils;
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
    public static void resolveToNewInstance(List<String> historyTokens, AsyncCallback<Widget> callback) {
        callback.onSuccess(getInstance(historyTokens));
        Window.setTitle(messages.windowTitle(historyTokens.get(2).toUpperCase()));
    }

    interface MyUiBinder extends UiBinder<Widget, SearchWithPreFilters> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private static final ClientMessages messages = GWT.create(ClientMessages.class);
    private static SearchWithPreFilters instance = null;

    private static SearchWithPreFilters getInstance(List<String> historyTokens) {
        // also create a new instance if the historyTokens have changed
        if (instance == null || !historyTokens.equals(instance.historyTokens)) {
            instance = new SearchWithPreFilters(historyTokens);
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

    private SearchWithPreFilters(List<String> historyTokens) {
        this.historyTokens = historyTokens;

        Optional<String> title = getTitleFromHistoryTokens(historyTokens);

        // prepare historyToken for the SearchWrapper
        List<String> preparedHistoryToken;
        if (title.isPresent()) {
            preparedHistoryToken = historyTokens.subList(3, historyTokens.size());
        } else {
            preparedHistoryToken = historyTokens.subList(1, historyTokens.size());
        }

        // Create main search
        catalogueSearch = new CatalogueSearch(preparedHistoryToken, true, "Search_AIPs", "Search_representations", "Search_files",
                null, false, true);

        initWidget(uiBinder.createAndBindUi(this));

        subTitlePanel.setText(title.orElse(messages.genericSearchWithPrefilterTitle()));
        searchDescription.add(new HTMLWidgetWrapper("SearchDescription.html"));
        savedSearchStaticDescription.add(new HTMLWidgetWrapper("SavedSearchDescription.html"));
    }

    private Optional<String> getTitleFromHistoryTokens(List<String> historyTokens) {
        // #search/$prefilter/(title/<title>)?/<filter>
        // OR
        // #search/$prefilter/<filter>
        if ("title".equals(historyTokens.get(1))) {
            if (StringUtils.isNotBlank(historyTokens.get(2))) {
                return Optional.of(historyTokens.get(2));
            }

        }
        return Optional.empty();
    }
}
