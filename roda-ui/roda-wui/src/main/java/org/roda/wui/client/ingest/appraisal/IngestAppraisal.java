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
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.AipActions.AipAction;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.MainSearch;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class IngestAppraisal extends Composite {

  private static final ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private static final Filter BASE_FILTER = new Filter(
    new SimpleFilterParameter(RodaConstants.STATE, AIPState.UNDER_APPRAISAL.toString()));

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
  MainSearch mainSearch;

  // FILTERS
  @UiField(provided = true)
  FlowPanel itemsFacets, representationsFacets, filesFacets;

  @UiField(provided = true)
  Button acceptButton, rejectButton;

  // cannot let representations and files to be selectable for now
  boolean itemsSelectable = true;
  boolean representationsSelectable = false;
  boolean filesSelectable = false;
  boolean justActive = false;

  private IngestAppraisal() {
    // Variables
    itemsFacets = new FlowPanel();
    representationsFacets = new FlowPanel();
    filesFacets = new FlowPanel();

    acceptButton = new Button();
    rejectButton = new Button();

    // Create main search
    mainSearch = new MainSearch(justActive, itemsSelectable, representationsSelectable, filesSelectable, itemsFacets,
      "IngestAppraisal_searchAIPs", representationsFacets, "IngestAppraisal_searchRepresentations", filesFacets,
      "IngestAppraisal_searchFiles", null, AIPState.UNDER_APPRAISAL);

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
    mainSearch.setDefaultFilters(BASE_FILTER);
    if (historyTokens.isEmpty()) {
      mainSearch.search(true);
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

  @SuppressWarnings("unchecked")
  private void appraise(boolean accept) {
    SelectedItems<?> selected = mainSearch.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      Toast.showInfo(messages.appraisalNoItemsSelectedTitle(), messages.appraisalNoItemsSelectedMessage());
    } else if (selected.getSelectedClass().equals(IndexedAIP.class.getName())) {
      AipAction action = accept ? AipAction.APPRAISAL_ACCEPT : AipAction.APPRAISAL_REJECT;

      LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
      AipActions.get().act(action, (SelectedItems<IndexedAIP>) selected, new AsyncCallback<Actionable.ActionImpact>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Actionable.ActionImpact result) {
          mainSearch.refresh();
        }
      });
    }
  }

  @UiHandler("acceptButton")
  void buttonAcceptHandler(ClickEvent e) {
    appraise(true);
  }

  @UiHandler("rejectButton")
  void buttonRejectHandler(ClickEvent e) {
    appraise(false);
  }
}
