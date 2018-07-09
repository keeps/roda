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

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.ActionableObject;
import org.roda.wui.client.common.actions.ActionableWidgetBuilder;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
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
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

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
  FlowPanel title;

  @UiField
  FlowPanel registerDescription;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  RepresentationInformationList representationInformationList;

  @UiField
  FlowPanel sidebar;

  @UiField
  FlowPanel content;

  @UiField
  SimplePanel actionsSidebar;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(RepresentationInformation.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(RepresentationInformation.class.getName());

  private final Filter filter = DEFAULT_FILTER;

  /**
   * Create a representation information page
   */
  public RepresentationInformationNetwork() {
    representationInformationList = new RepresentationInformationList("RepresentationInformationNetwork_RI", filter,
      messages.representationInformationTitle(), true);
    representationInformationList.setActionable(RepresentationInformationActions.get());

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true,
      messages.representationInformationRegisterSearchPlaceHolder(), false, false, false);
    searchPanel.setList(representationInformationList);

    initWidget(uiBinder.createAndBindUi(this));

    actionsSidebar.setWidget(new ActionableWidgetBuilder<>(RepresentationInformationActions.get())
      .buildListWithObjects(new ActionableObject<>(RepresentationInformation.class)));

    representationInformationList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        RepresentationInformation selected = representationInformationList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
          HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER, selected.getId());
        }
      }
    });

    Label titleLabel = new Label(messages.representationInformationRegisterTitle());
    titleLabel.addStyleName("h1 browseItemText");
    title.add(titleLabel);

    InlineHTML badge = new InlineHTML("<span class='label-warning browseRepresentationOriginalIcon'>Beta</span>");
    title.add(badge);

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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      filter.setParameters(new ArrayList<>());
      searchPanel.setDefaultFilter(filter, true);
      representationInformationList.setFilter(filter);
      searchPanel.clearSearchInputBox();

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
        setFilterFromHistoryTokens(historyTokens);
        searchPanel.setDefaultFilter(filter, true);
        representationInformationList.setFilter(filter);
        searchPanel.clearSearchInputBox();

        callback.onSuccess(this);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }
  }

  private void setFilterFromHistoryTokens(List<String> historyTokens) {
    List<FilterParameter> params = new ArrayList<>();
    if (historyTokens.size() == (2)) {
      params.add(new SimpleFilterParameter(historyTokens.get(0), historyTokens.get(1)));
    }

    filter.setParameters(params);
  }
}
