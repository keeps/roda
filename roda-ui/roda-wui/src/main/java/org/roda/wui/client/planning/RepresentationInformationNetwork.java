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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
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

  @UiField(provided = true)
  FlowPanel facetCategories;

  @UiField(provided = true)
  FlowPanel facetSupport;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  @UiField
  Button buttonCancel;

  @UiField
  FlowPanel sidebar;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel createPanel;

  @UiField
  Button buttonAddWithAssociation;

  private static final String CONTENT_STYLE_WITH_SIDEBAR = "col_10";
  private static final String CONTENT_STYLE_WITHOUT_SIDEBAR = "col_12";

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(RepresentationInformation.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(RepresentationInformation.class.getName());

  private Filter filter = DEFAULT_FILTER;

  private boolean creatingMode = false;

  /**
   * Create a format register page
   *
   * @param user
   */
  public RepresentationInformationNetwork() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES),
      new SimpleFacetParameter(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT));

    representationInformationList = new RepresentationInformationList(filter, facets,
      messages.representationInformationTitle(), true);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true,
      messages.representationInformationRegisterSearchPlaceHolder(), false, false, false);
    searchPanel.setList(representationInformationList);

    facetCategories = new FlowPanel();
    facetSupport = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<>();
    facetPanels.put(RodaConstants.REPRESENTATION_INFORMATION_CATEGORIES, facetCategories);
    facetPanels.put(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT, facetSupport);
    FacetUtils.bindFacets(representationInformationList, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    sidebar.setVisible(false);
    representationInformationList.setVisible(false);
    searchPanel.setVisible(false);

    representationInformationList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        RepresentationInformation selected = representationInformationList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
          HistoryUtils.newHistory(RESOLVER, ShowRepresentationInformation.RESOLVER.getHistoryToken(), selected.getId());
        }
      }
    });

    representationInformationList
      .addCheckboxSelectionListener(new CheckboxSelectionListener<RepresentationInformation>() {
        @Override
        public void onSelectionChange(SelectedItems<RepresentationInformation> selected) {
          boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
          buttonRemove.setEnabled(!empty);
          startProcess.setEnabled(!empty);
        }
      });

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        creatingMode = false;
      }
    });

    representationInformationList
      .addValueChangeHandler(new ValueChangeHandler<IndexResult<RepresentationInformation>>() {
        @Override
        public void onValueChange(ValueChangeEvent<IndexResult<RepresentationInformation>> event) {
          boolean empty = event.getValue().getTotalCount() == 0 && creatingMode;
          searchPanel.setVisible(!empty);
          representationInformationList.setVisible(!empty);

          sidebar.setVisible(!empty);
          content.setStyleName(CONTENT_STYLE_WITH_SIDEBAR, !empty);
          content.setStyleName(CONTENT_STYLE_WITHOUT_SIDEBAR, empty);

          createPanel.setVisible(empty);
        }
      });

    Label titleLabel = new Label(messages.representationInformationRegisterTitle());
    titleLabel.addStyleName("h1 browseItemText");
    title.add(titleLabel);

    InlineHTML badge = new InlineHTML("<span class='label-warning browseRepresentationOriginalIcon'>Beta</span>");
    title.add(badge);

    registerDescription.add(new HTMLWidgetWrapper("FormatRegisterDescription.html"));

    buttonAddWithAssociation.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RepresentationInformationDialogs.showPromptAddRepresentationInformationwithAssociation("title", "Cancel",
          "Associate with selected", "Create and associate",
          new NoAsyncCallback<SelectedItemsList<RepresentationInformation>>() {
            @Override
            public void onSuccess(SelectedItemsList<RepresentationInformation> selectedItemsList) {
              if (selectedItemsList != null) {
                String ids = "";
                for (String id : selectedItemsList.getIds()) {
                  ids += id + ", ";
                }
                GWT.log("received: " + ids);

                String filtertoAdd = HistoryUtils.getCurrentHistoryPath()
                  .get(HistoryUtils.getCurrentHistoryPath().size() - 1);

                BrowserService.Util.getInstance().updateRepresentationInformationListWithFilter(selectedItemsList,
                  filtertoAdd, new NoAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                      List<String> reloadHistory = HistoryUtils.getCurrentHistoryPath().subList(2,
                        HistoryUtils.getCurrentHistoryPath().size());


                      String path = "";
                      for (String token : reloadHistory) {
                        path += token + ", ";
                      }
                      GWT.log("going to " + path);

                      RepresentationInformationNetwork.this.resolve(reloadHistory, new NoAsyncCallback<Widget>());
                    }
                  });
              } else {
                LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
                selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
                HistoryUtils.newHistory(RESOLVER, CreateRepresentationInformation.RESOLVER.getHistoryToken());
              }
            }
          });
      }
    });
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
    sidebar.setVisible(false);
    representationInformationList.setVisible(false);
    searchPanel.setVisible(false);

    if (historyTokens.isEmpty()) {
      filter = DEFAULT_FILTER;
      creatingMode = false;
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
      } else if (Search.RESOLVER.getHistoryToken().equals(basePage) && !historyTokens.isEmpty()) {
        creatingMode = true;
        filter = createFilterFromHistoryTokens(historyTokens, false);

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

  private Filter createFilterFromHistoryTokens(List<String> historyTokens, boolean includingSearchToken) {
    int offset = 0;
    if (includingSearchToken && historyTokens.size() > 2
      && Search.RESOLVER.getHistoryToken().equals(historyTokens.get(0))) {
      offset = 1;
    }

    List<FilterParameter> params = new ArrayList<>();
    if (historyTokens.size() == (2 + offset)) {
      params.add(new SimpleFilterParameter(historyTokens.get(offset), historyTokens.get(1 + offset)));
    }
    return new Filter(new OrFiltersParameters(params));
  }

  @UiHandler("buttonAdd")
  void buttonAddHandler(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(RESOLVER, CreateRepresentationInformation.RESOLVER.getHistoryToken());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    final SelectedItems<RepresentationInformation> selected = representationInformationList.getSelected();

    ClientSelectedItemsUtils.size(RepresentationInformation.class, selected, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.representationInformationRemoveFolderConfirmDialogTitle(),
          messages.representationInformationRemoveSelectedConfirmDialogMessage(size),
          messages.representationInformationRemoveFolderConfirmDialogCancel(),
          messages.representationInformationRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().deleteRepresentationInformation(selected, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                    representationInformationList.refresh();
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Timer timer = new Timer() {
                      @Override
                      public void run() {
                        Toast.showInfo(messages.representationInformationRemoveSuccessTitle(),
                          messages.representationInformationRemoveSuccessMessage(size));
                        representationInformationList.refresh();
                      }
                    };

                    timer.schedule(RodaConstants.ACTION_TIMEOUT);
                  }
                });
              }
            }
          });
      }
    });
  }

  @UiHandler("startProcess")
  void handleButtonProcess(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(representationInformationList.getSelected());
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    HistoryUtils.newHistory(selectedItems.getLastHistory());
  }
}
