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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.JavascriptUtils;
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
import com.google.gwt.user.client.ui.SimplePanel;
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

  @UiField
  SimplePanel actionsSidebar;

  private IngestAppraisal() {
    AipActions appraisalAipActions = AipActions.get(null, AIPState.UNDER_APPRAISAL, null);

    ActionableWidgetBuilder<IndexedAIP> sidebarActionsWidgetbuilder = new ActionableWidgetBuilder<>(appraisalAipActions)
      .withCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
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
    ListBuilder<IndexedAIP> aipListBuilder = new ListBuilder<>(() -> new AIPList(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "IngestAppraisal_searchAIPs").withJustActive(false)
        .withFilter(BASE_FILTER).bindOpener().addCheckboxSelectionListener(selected -> {
          if (selected instanceof SelectedItemsList) {
            SelectedItemsList selectedItemsList = (SelectedItemsList) selected;
            if (!selectedItemsList.getIds().isEmpty()) {
              actionsSidebar
                .setWidget(sidebarActionsWidgetbuilder.buildListWithObjects(new ActionableObject<>(selected),
                  Arrays.asList(AipActions.AipAction.APPRAISAL_ACCEPT, AipActions.AipAction.APPRAISAL_REJECT)));
            } else {
              actionsSidebar
                .setWidget(sidebarActionsWidgetbuilder.buildListWithObjects(new ActionableObject<>(IndexedAIP.class),
                  Arrays.asList(AipActions.AipAction.APPRAISAL_ACCEPT, AipActions.AipAction.APPRAISAL_REJECT)));
            }
          } else {
            actionsSidebar.setWidget(sidebarActionsWidgetbuilder.buildListWithObjects(new ActionableObject<>(selected),
              Arrays.asList(AipActions.AipAction.APPRAISAL_ACCEPT, AipActions.AipAction.APPRAISAL_REJECT)));
          }
        }));

    ListBuilder<IndexedRepresentation> representationListBuilder = new ListBuilder<>(() -> new RepresentationList(),
      new AsyncTableCellOptions<>(IndexedRepresentation.class, "IngestAppraisal_searchRepresentations")
        .withJustActive(false).withFilter(BASE_FILTER).bindOpener());

    ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new SearchFileList(true),
      new AsyncTableCellOptions<>(IndexedFile.class, "IngestAppraisal_searchFiles").withJustActive(false)
        .withFilter(BASE_FILTER).bindOpener());

    // add lists to search
    searchWrapper = new SearchWrapper(true, IndexedAIP.class.getSimpleName())
      .createListAndSearchPanel(aipListBuilder, appraisalAipActions).createListAndSearchPanel(representationListBuilder)
      .createListAndSearchPanel(fileListBuilder);

    initWidget(uiBinder.createAndBindUi(this));
    ingestAppraisalDescription.add(new HTMLWidgetWrapper("IngestAppraisalDescription.html"));
  }

  public static IngestAppraisal getInstance() {
    if (instance == null) {
      instance = new IngestAppraisal();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    setFilters(historyTokens);
    callback.onSuccess(this);
  }

  private void setFilters(List<String> historyTokens) {
    Filter filter = SearchFilters.createIncrementalFilterFromTokens(ListUtils.tail(historyTokens), BASE_FILTER);
    searchWrapper.setFilter(IndexedRepresentation.class, filter);
    searchWrapper.setFilter(IndexedFile.class, filter);

    // handle aipId VS parentAipId
    if (historyTokens.contains(RodaConstants.REPRESENTATION_AIP_ID)) {
      List<String> tokensForAip = new ArrayList<>(historyTokens);
      tokensForAip.set(historyTokens.indexOf(RodaConstants.REPRESENTATION_AIP_ID), RodaConstants.AIP_PARENT_ID);
      searchWrapper.setFilter(IndexedAIP.class,
        SearchFilters.createFilterFromHistoryTokens(ListUtils.tail(historyTokens)));
    } else {
      searchWrapper.setFilter(IndexedAIP.class, filter);
    }
  }
}
