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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.lists.RepresentationInformationList;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira
 *
 */
public class RepresentationInformationAssociations extends Composite {

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
      return ListUtils.concat(RepresentationInformationNetwork.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation_information_associations";
    }
  };

  private static RepresentationInformationAssociations instance = null;

  interface MyUiBinder extends UiBinder<Widget, RepresentationInformationAssociations> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel title;

  @UiField
  HTML description;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  RepresentationInformationList representationInformationList;

  @UiField
  FlowPanel createPanel;

  @UiField
  Button buttonAddToExistingRI;

  @UiField
  Button buttonAddToNewRI;

  private SafeHtml addWithAssociationDialogTitle;

  @UiField
  FlowPanel resultsPanel;

  @UiField
  HTML createPanelTitle;

  @UiField
  HTML resultsPanelTitle;

  private boolean gettingFilterResults = true;

  private static final Filter DEFAULT_FILTER = SearchFilters.defaultFilter(RepresentationInformation.class.getName());
  private static final String ALL_FILTER = SearchFilters.allFilter(RepresentationInformation.class.getName());

  private Filter filter = DEFAULT_FILTER;

  /**
   * Create a representation information page
   */
  public RepresentationInformationAssociations() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.REPRESENTATION_INFORMATION_TAGS),
      new SimpleFacetParameter(RodaConstants.REPRESENTATION_INFORMATION_SUPPORT));

    representationInformationList = new RepresentationInformationList(filter, facets,
      messages.representationInformationTitle(), false);

    searchPanel = new SearchPanel(DEFAULT_FILTER, ALL_FILTER, true,
      messages.representationInformationRegisterSearchPlaceHolder(), false, false, true);
    searchPanel.setList(representationInformationList);

    initWidget(uiBinder.createAndBindUi(this));

    resultsPanel.setVisible(false);
    createPanel.setVisible(false);

    searchPanel.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> valueChangeEvent) {
        // the user is searching. use this flag to avoid showing the options to
        // associate the filter with RI (if this was not present, those options
        // would
        // show up if the search had no results)
        gettingFilterResults = false;
      }
    });

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

    representationInformationList
      .addValueChangeHandler(new ValueChangeHandler<IndexResult<RepresentationInformation>>() {
        @Override
        public void onValueChange(ValueChangeEvent<IndexResult<RepresentationInformation>> event) {
          boolean associating = gettingFilterResults && event.getValue().getTotalCount() == 0;
          resultsPanel.setVisible(!associating);
          createPanel.setVisible(associating);
        }
      });

    Label titleLabel = new Label(messages.representationInformationAssociationsTitle());
    titleLabel.addStyleName("h1 browseItemText");
    title.add(titleLabel);

    InlineHTML badge = new InlineHTML("<span class='label-warning browseRepresentationOriginalIcon'>Beta</span>");
    title.add(badge);

    buttonAddToExistingRI.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        RepresentationInformationDialogs.showPromptAddRepresentationInformationWithAssociation(
          addWithAssociationDialogTitle, "Cancel", "Add to existing", "Add to new",
          new NoAsyncCallback<SelectedItemsList<RepresentationInformation>>() {
            @Override
            public void onSuccess(final SelectedItemsList<RepresentationInformation> selectedItemsList) {
              if (selectedItemsList != null) {
                String filtertoAdd = HistoryUtils.getCurrentHistoryPath()
                  .get(HistoryUtils.getCurrentHistoryPath().size() - 1);

                BrowserService.Util.getInstance().updateRepresentationInformationListWithFilter(selectedItemsList,
                  filtertoAdd, new NoAsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                      if (selectedItemsList.getIds().size() == 1) {
                        HistoryUtils.newHistory(ShowRepresentationInformation.RESOLVER,
                          selectedItemsList.getIds().get(0));
                      } else {
                        gettingFilterResults = false;
                        representationInformationList.refresh();
                        createPanel.setVisible(false);
                        resultsPanel.setVisible(true);
                      }
                    }
                  });
              } else {
                addToNewClickHandler();
              }
            }
          });
      }
    });

    buttonAddToNewRI.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        addToNewClickHandler();
      }
    });
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static RepresentationInformationAssociations getInstance() {
    if (instance == null) {
      instance = new RepresentationInformationAssociations();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    gettingFilterResults = true;

    createPanel.setVisible(false);
    resultsPanel.setVisible(false);

    if (historyTokens.size() == 2) {
      filter = createFilterAndSubtitleFromHistoryTokens(historyTokens);

      String[] parts = RepresentationInformationDialogs.breakFilterIntoParts(historyTokens.get(1));
      createPanelTitle.setHTML(messages.representationInformationNoAssociations(parts[0], parts[1], parts[2]));
      resultsPanelTitle.setHTML(messages.representationInformationAssociatedWith(parts[0], parts[1], parts[2]));
      description.setHTML(messages.representationInformationAssociatedWithDescription(parts[0], parts[1], parts[2]));
      addWithAssociationDialogTitle = messages.representationInformationAssociateWith(parts[0], parts[1], parts[2]);

      searchPanel.setDefaultFilter(filter, true);
      representationInformationList.setFilter(filter);
      searchPanel.clearSearchInputBox();

      callback.onSuccess(this);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  private Filter createFilterAndSubtitleFromHistoryTokens(List<String> historyTokens) {
    List<FilterParameter> params = new ArrayList<>();
    if (historyTokens.size() == (2)) {
      params.add(new SimpleFilterParameter(historyTokens.get(0), historyTokens.get(1)));
    }

    return new Filter(new OrFiltersParameters(params));
  }

  private void addToNewClickHandler() {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateRepresentationInformation.RESOLVER);
  }
}
