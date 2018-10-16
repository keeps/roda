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
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
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
public class RepresentationInformationNetwork extends Composite {

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
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation_information_register";
    }
  };

  private static RepresentationInformationNetwork instance = null;

  interface MyUiBinder extends UiBinder<Widget, RepresentationInformationNetwork> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel registerDescription;

  @UiField(provided = true)
  SearchWrapper searchPanel;

  @UiField
  FlowPanel content;

  /**
   * Create a representation information page
   */
  public RepresentationInformationNetwork() {
    ListBuilder<RepresentationInformation> representationInformationListBuilder = new ListBuilder<>(
      () -> new RepresentationInformationList(),
      new AsyncTableCellOptions<>(RepresentationInformation.class, "RepresentationInformationNetwork_RI")
        .withSummary(messages.representationInformationTitle()).bindOpener()
        .withSearchPlaceholder(messages.representationInformationRegisterSearchPlaceHolder())
        .withActionable(RepresentationInformationActions.get()));

    searchPanel = new SearchWrapper(false).createListAndSearchPanel(representationInformationListBuilder);

    initWidget(uiBinder.createAndBindUi(this));

    registerDescription.add(new HTMLWidgetWrapper("RepresentationInformationNetworkDescription.html"));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RepresentationInformationNetwork getInstance() {
    if (instance == null) {
      instance = new RepresentationInformationNetwork();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      searchPanel.setFilter(RepresentationInformation.class, SearchFilters.allFilter());
      callback.onSuccess(this);
    } else {
      String basePage = historyTokens.remove(0);
      if (ShowRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
        ShowRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
      } else if (CreateRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
        CreateRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
      } else if (EditRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
        EditRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
      } else if (RepresentationInformationAssociations.RESOLVER.getHistoryToken().equals(basePage)) {
        RepresentationInformationAssociations.RESOLVER.resolve(historyTokens, callback);
      } else if (Search.RESOLVER.getHistoryToken().equals(basePage)) {
        searchPanel.setFilter(RepresentationInformation.class,
          SearchFilters.createFilterFromHistoryTokens(historyTokens));
        callback.onSuccess(this);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }
  }
}
