package org.roda.wui.client.disposal;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
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
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalDestroyedRecords extends Composite {
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
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "destroyed";
    }
  };

  private static DisposalDestroyedRecords instance = null;

  interface MyUiBinder extends UiBinder<Widget, DisposalDestroyedRecords> {
  }

  private static DisposalDestroyedRecords.MyUiBinder uiBinder = GWT.create(DisposalDestroyedRecords.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel disposalDestroyedRecordsDescription;

  @UiField(provided = true)
  SearchWrapper disposalDestroyedRecordsSearch;

  @UiField
  FlowPanel content;

  /**
   * Create a representation information page
   */
  public DisposalDestroyedRecords() {

    ListBuilder<IndexedAIP> aipChildrenListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "DisposalDestroyedRecords_aip")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.RESIDUAL.name())))
        .withSummary(messages.listOfAIPs()).bindOpener());

    disposalDestroyedRecordsSearch = new SearchWrapper(false).createListAndSearchPanel(aipChildrenListBuilder);

    initWidget(uiBinder.createAndBindUi(this));
    disposalDestroyedRecordsDescription.add(new HTMLWidgetWrapper("DisposalDestroyedRecordsDescription.html"));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static DisposalDestroyedRecords getInstance() {
    if (instance == null) {
      instance = new DisposalDestroyedRecords();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } /*
       * else { String basePage = historyTokens.remove(0); if
       * (ShowRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
       * ShowRepresentationInformation.RESOLVER.resolve(historyTokens, callback); }
       * else if
       * (CreateRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage))
       * { CreateRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
       * } else if
       * (EditRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
       * EditRepresentationInformation.RESOLVER.resolve(historyTokens, callback); }
       * else if
       * (RepresentationInformationAssociations.RESOLVER.getHistoryToken().equals(
       * basePage)) {
       * RepresentationInformationAssociations.RESOLVER.resolve(historyTokens,
       * callback); } else if (Search.RESOLVER.getHistoryToken().equals(basePage)) {
       * searchPanel.setFilter(RepresentationInformation.class,
       * SearchFilters.createFilterFromHistoryTokens(historyTokens));
       * callback.onSuccess(this); } else { HistoryUtils.newHistory(RESOLVER);
       * callback.onSuccess(null); } }
       */
  }
}
