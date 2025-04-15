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
package org.roda.wui.client.ingest.appraisal;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class IngestAppraisal extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Filter BASE_FILTER = new Filter(
    new SimpleFilterParameter(RodaConstants.INDEX_STATE, AIPState.UNDER_APPRAISAL.toString()));

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
      return ListUtils.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "appraisal";
    }
  };

  private static IngestAppraisal instance = null;

  interface MyUiBinder extends UiBinder<Widget, IngestAppraisal> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel ingestAppraisalDescription;

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  private IngestAppraisal() {
    AipActions appraisalAipActions = AipActions.get(null, AIPState.UNDER_APPRAISAL, null);

    ActionableWidgetBuilder<IndexedAIP> sidebarActionsWidgetbuilder = new ActionableWidgetBuilder<>(appraisalAipActions)
      .withActionCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onFailure(Throwable caught) {
          super.onFailure(caught);
          searchWrapper.refreshAllLists();
        }

        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          searchWrapper.refreshAllLists();
        }
      });

    // prepare lists
    ListBuilder<IndexedAIP> aipListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "IngestAppraisal_searchAIPs").withJustActive(false)
        .withFilter(BASE_FILTER).bindOpener().withActionable(appraisalAipActions)
        .withActionWhitelist(List.of(AipActions.AipAction.APPRAISAL_ACCEPT, AipActions.AipAction.APPRAISAL_REJECT)));

    ListBuilder<IndexedRepresentation> representationListBuilder = new ListBuilder<>(
      () -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedRepresentation.class, "IngestAppraisal_searchRepresentations")
        .withJustActive(false).withFilter(BASE_FILTER).bindOpener());

    /*
     * ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new
     * ConfigurableAsyncTableCell<>(), new
     * AsyncTableCellOptions<>(IndexedFile.class,
     * "IngestAppraisal_searchFiles").withJustActive(false)
     * .withFilter(BASE_FILTER).bindOpener());
     */
    // add lists to search
    searchWrapper = new SearchWrapper(true, IndexedAIP.class.getSimpleName()).createListAndSearchPanel(aipListBuilder)
      .createListAndSearchPanel(representationListBuilder);// .createListAndSearchPanel(fileListBuilder);

    initWidget(uiBinder.createAndBindUi(this));
    ingestAppraisalDescription.add(new HTMLWidgetWrapper("IngestAppraisalDescription.html"));

  }

  public static IngestAppraisal getInstance() {
    if (instance == null) {
      instance = new IngestAppraisal();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (!historyTokens.isEmpty()) {
      setFilters(historyTokens);
    } else {
      searchWrapper.setFilter(IndexedAIP.class, BASE_FILTER);
      searchWrapper.setFilter(IndexedRepresentation.class, BASE_FILTER);
      searchWrapper.setFilter(IndexedFile.class, BASE_FILTER);
      searchWrapper.changeDropdownSelectedValue(IndexedAIP.class.getSimpleName());
    }
    callback.onSuccess(this);
  }

  private void setFilters(List<String> historyTokens) {
    if (!historyTokens.isEmpty()) {
      Filter filter = SearchFilters.createIncrementalFilterFromTokens(historyTokens, BASE_FILTER);

      searchWrapper.setFilter(IndexedRepresentation.class, filter);
      searchWrapper.setFilter(IndexedFile.class, filter);
      searchWrapper.setFilter(IndexedAIP.class, filter);
      searchWrapper.changeDropdownSelectedValue(IndexedAIP.class.getSimpleName());
    } else {
      GWT.log("setFilter can not handle tokens: " + historyTokens);
    }
  }
}
