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
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.pagination.ListSelectionState;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.process.ShowJobReport;
import org.roda.wui.client.ingest.transfer.TransferUpload;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateJob;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseAIP extends Composite {

  private static final String BROWSE_TOP_CSS = "browse_top";
  private static final String BROWSE_AIP_CSS = "browse_aip";

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
    public String getHistoryToken() {
      return "browse";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseAIP> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static BrowseAIP instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static BrowseAIP getInstance() {
    if (instance == null) {
      instance = new BrowseAIP();
    }
    return instance;
  }

  private static final Filter COLLECTIONS_FILTER = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
  private static Facets FACETS = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
    new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private String aipId;
  private BrowseAIPBundle bundle;

  // Focus
  @UiField
  FocusPanel keyboardFocus;

  // HEADER

  @UiField
  Label browseTitle;

  @UiField
  FlowPanel browseDescription;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel itemIcon;

  // STATUS

  @UiField
  HTML aipState;

  // IDENTIFICATION

  @UiField
  Label browseItemHeader, itemTitle, itemId, sipId;

  @UiField
  FlowPanel ingestJobId;

  // DESCRIPTIVE METADATA

  @UiField
  TabPanel descriptiveMetadata;

  @UiField
  Button newDescriptiveMetadata;

  // REPRESENTATIONS

  @UiField
  Label representationsTitle;

  @UiField(provided = true)
  SearchPanel representationsSearch;

  @UiField(provided = true)
  RepresentationList representationsList;

  // DISSEMINATIONS

  @UiField
  Label disseminationsTitle;

  @UiField(provided = true)
  SearchPanel disseminationsSearch;

  @UiField(provided = true)
  DIPList disseminationsList;

  // AIP CHILDREN

  @UiField
  Label aipChildrenTitle;

  @UiField(provided = true)
  SearchPanel aipChildrenSearch;

  @UiField(provided = true)
  AIPList aipChildrenList;

  // SIDEBAR

  @UiField
  FlowPanel appraisalSidebar;

  @UiField
  FlowPanel preservationSidebar;

  @UiField
  FlowPanel actionsSidebar;

  @UiField
  Button preservationEvents, risks, logs, newProcess;

  @UiField
  Button createItem, moveItem, remove, newRepresentation;

  @UiField
  Button editPermissions;

  @UiField
  Button download;

  // @UiField
  // FlowPanel downloadSection;

  // @UiField
  // Button submission, documentation, schemas;

  @UiField
  FlowPanel searchSection;

  @UiField
  Button searchPrevious, searchNext, searchContext;

  @UiField
  Button searchAIP;

  @UiField
  FlowPanel itemsFacets;

  @UiField(provided = true)
  FlowPanel facetDescriptionLevels;

  @UiField(provided = true)
  FlowPanel facetHasRepresentations;

  private List<HandlerRegistration> handlers;

  boolean justActive = true;

  private BrowseAIP() {
    handlers = new ArrayList<HandlerRegistration>();

    boolean selectable = true;

    // REPRESENTATIONS
    representationsList = new RepresentationList(Filter.NULL, justActive, Facets.NONE, messages.listOfRepresentations(),
      true);

    ListSelectionState.bindBrowseOpener(representationsList);

    representationsSearch = new SearchPanel(Filter.NULL, RodaConstants.REPRESENTATION_SEARCH,
      messages.searchPlaceHolder(), false, false, true);
    representationsSearch.setDefaultFilterIncremental(true);
    representationsSearch.setList(representationsList);

    // DISSEMINATIONS

    disseminationsList = new DIPList(Filter.NULL, Facets.NONE, messages.listOfDisseminations(), true);
    ListSelectionState.bindBrowseOpener(disseminationsList);

    disseminationsSearch = new SearchPanel(Filter.NULL, RodaConstants.DIP_SEARCH, messages.searchPlaceHolder(), false,
      false, true);
    disseminationsSearch.setDefaultFilterIncremental(true);
    disseminationsSearch.setList(disseminationsList);

    // AIP CHILDREN

    aipChildrenList = new AIPList(Filter.NULL, justActive, FACETS, messages.listOfAIPs(), selectable);
    ListSelectionState.bindBrowseOpener(aipChildrenList);

    aipChildrenList.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedAIP>() {

      @Override
      public void onSelectionChange(SelectedItems<IndexedAIP> selected) {
        if (aipId == null) {
          boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
          moveItem.setEnabled(!empty);
          editPermissions.setEnabled(!empty);
        }
      }
    });

    aipChildrenSearch = new SearchPanel(COLLECTIONS_FILTER, RodaConstants.AIP_SEARCH, messages.searchPlaceHolder(),
      false, false, true);
    aipChildrenSearch.setDefaultFilterIncremental(true);
    aipChildrenSearch.setList(aipChildrenList);

    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.AIP_LEVEL, facetDescriptionLevels);
    facetPanels.put(RodaConstants.AIP_HAS_REPRESENTATIONS, facetHasRepresentations);

    FacetUtils.bindFacets(aipChildrenList, facetPanels);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // HEADER
    browseDescription.add(new HTMLWidgetWrapper("BrowseDescription.html"));

    // OTHER
    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(User user) {
        onPermissionsUpdate(user);
      }
    });

    ListSelectionState.bindLayout(IndexedAIP.class, searchPrevious, searchNext, keyboardFocus, true, false, false);

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  protected void onPermissionsUpdate(User user) {
    if (user != null) {
      logs.setVisible(user.hasRole(RodaConstants.REPOSITORY_PERMISSIONS_LOG_ENTRY_READ));
      createItem.setVisible(user.hasRole(RodaConstants.REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_UPDATE));
    } else {
      logs.setVisible(false);
      createItem.setVisible(false);
    }

  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    clear();
    if (historyTokens.size() == 0) {
      viewAction();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 1 && !historyTokens.get(0).equals(EditPermissions.RESOLVER.getHistoryToken())) {
      viewAction(historyTokens.get(0));
      callback.onSuccess(this);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(EditDescriptiveMetadata.RESOLVER.getHistoryToken())) {
      EditDescriptiveMetadata.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(CreateDescriptiveMetadata.RESOLVER.getHistoryToken())) {
      CreateDescriptiveMetadata.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(BrowseFile.RESOLVER.getHistoryToken())) {
      BrowseFile.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(BrowseDIP.RESOLVER.getHistoryToken())) {
      BrowseDIP.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(PreservationEvents.BROWSE_RESOLVER.getHistoryToken())) {
      PreservationEvents.BROWSE_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(DescriptiveMetadataHistory.RESOLVER.getHistoryToken())) {
      DescriptiveMetadataHistory.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() >= 1 && historyTokens.get(0).equals(EditPermissions.RESOLVER.getHistoryToken())) {
      EditPermissions.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(BrowseRepresentation.RESOLVER.getHistoryToken())) {
      BrowseRepresentation.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(BrowseFolder.RESOLVER.getHistoryToken())) {
      BrowseFolder.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 0
      && historyTokens.get(0).equals(TransferUpload.BROWSE_RESOLVER.getHistoryToken())) {
      TransferUpload.BROWSE_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  /**
   * Call the view action by the history token
   * 
   * @param id
   *          the pid of the object to view. if pid is null, then the base state
   *          will be called
   */
  public void view(final String id) {
    boolean historyUpdated = updateHistory(id);

    if (!historyUpdated) {
      viewAction(id);
    }
  }

  protected void viewAction(final String id) {
    if (id == null) {
      viewAction();
    } else {
      aipId = id;
      BrowserService.Util.getInstance().retrieveBrowseAIPBundle(id, LocaleInfo.getCurrentLocale().getLocaleName(),
        new AsyncCallback<BrowseAIPBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            if (!AsyncCallbackUtils.treatCommonFailures(caught, Welcome.RESOLVER.getHistoryPath())) {
              showError(id, caught);
            }
          }

          @Override
          public void onSuccess(BrowseAIPBundle bundle) {
            viewAction(bundle);
          }
        });
    }
  }

  private void clear() {

    removeStyleName(BROWSE_TOP_CSS);
    removeStyleName(BROWSE_AIP_CSS);

    justActive = true;
    browseItemHeader.setVisible(false);
    browseTitle.setVisible(false);
    browseDescription.setVisible(false);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");
    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(messages.browseLoading());
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");
    itemId.setText("");
    itemId.removeStyleName("browseItemId");
    sipId.setText("");
    sipId.removeStyleName("browseSipId");
    ingestJobId.clear();
    ingestJobId.removeStyleName("browseIngestJobId");

    breadcrumb.setVisible(false);

    descriptiveMetadata.setVisible(false);
    descriptiveMetadata.clear();
    removeHandlerRegistrations();

    newDescriptiveMetadata.setVisible(false);

    // Representations list
    representationsTitle.setVisible(false);
    representationsSearch.setVisible(false);
    representationsSearch.clearSearchInputBox();
    representationsList.setVisible(false);
    representationsList.getParent().setVisible(false);

    // Disseminations list
    disseminationsTitle.setVisible(false);
    disseminationsSearch.setVisible(false);
    disseminationsSearch.clearSearchInputBox();
    disseminationsList.setVisible(false);
    disseminationsList.getParent().setVisible(false);

    // AIP children list
    aipChildrenTitle.setVisible(false);
    aipChildrenSearch.setVisible(false);
    aipChildrenSearch.clearSearchInputBox();
    aipChildrenList.setVisible(false);
    aipChildrenList.getParent().setVisible(false);

    appraisalSidebar.setVisible(false);
    preservationSidebar.setVisible(false);
    actionsSidebar.setVisible(false);

    searchSection.setVisible(false);

    // Set button visibility
    createItem.setVisible(false);
    moveItem.setVisible(false);
    editPermissions.setVisible(false);
    remove.setVisible(false);
    newRepresentation.setVisible(false);

    for (AIPState state : AIPState.values()) {
      this.removeStyleName(state.toString().toLowerCase());
    }

    FACETS = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
  }

  protected void showError(String id, Throwable caught) {
    breadcrumb.updatePath(new ArrayList<BreadcrumbItem>());

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(null);
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");
    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(id);

    SafeHtml title;
    SafeHtml message;
    if (caught instanceof NotFoundException) {
      title = messages.notFoundErrorTitle();
      message = messages.notFoundErrorMessage(aipId);
      GWT.log(messages.notFoundError(), caught);
    } else {
      title = messages.genericErrorTitle();
      message = messages.genericErrorMessage(caught.getMessage());
    }

    HTML messageHTML = new HTML(message);
    messageHTML.addStyleName("error");
    descriptiveMetadata.add(messageHTML, title.asString(), true);
    descriptiveMetadata.selectTab(0);
    descriptiveMetadata.setVisible(true);
  }

  protected void viewAction(BrowseAIPBundle bundle) {
    if (bundle != null) {
      addStyleName(BROWSE_AIP_CSS);

      this.bundle = bundle;

      this.justActive = AIPState.ACTIVE.equals(bundle.getAip().getState());

      IndexedAIP aip = bundle.getAip();

      // STATUS
      for (AIPState state : AIPState.values()) {
        this.removeStyleName(state.toString().toLowerCase());
      }

      this.addStyleName(aip.getState().toString().toLowerCase());
      aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(aip.getState()));
      aipState.setVisible(AIPState.ACTIVE != aip.getState());

      // IDENTIFICATION
      updateSectionIdentification(bundle);

      // DESCRIPTIVE METADATA
      updateSectionDescriptiveMetadata(bundle);

      // REPRESENTATIONS
      if (bundle.getRepresentationCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId()));
        representationsSearch.setDefaultFilter(filter);
        representationsSearch.clearSearchInputBox();
        representationsList.set(filter, justActive, Facets.NONE);
      }

      representationsTitle.setVisible(bundle.getRepresentationCount() > 0);
      representationsSearch.setVisible(bundle.getRepresentationCount() > 0);
      representationsList.setVisible(bundle.getRepresentationCount() > 0);
      representationsList.getParent().setVisible(bundle.getRepresentationCount() > 0);

      // DISSEMINATIONS
      if (bundle.getDipCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId()));
        disseminationsSearch.setDefaultFilter(filter);
        disseminationsSearch.clearSearchInputBox();
        disseminationsList.set(filter, justActive, Facets.NONE);
      }

      disseminationsTitle.setVisible(bundle.getDipCount() > 0);
      disseminationsSearch.setVisible(bundle.getDipCount() > 0);
      disseminationsList.setVisible(bundle.getDipCount() > 0);
      disseminationsList.getParent().setVisible(bundle.getDipCount() > 0);

      // AIP CHILDREN
      if (bundle.getChildAIPCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId()));
        aipChildrenSearch.setDefaultFilter(filter);
        aipChildrenSearch.clearSearchInputBox();
        aipChildrenList.set(filter, justActive, FACETS);
      }

      aipChildrenTitle.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenSearch.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenList.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenList.getParent().setVisible(bundle.getChildAIPCount() > 0);

      // SIDEBAR
      appraisalSidebar.setVisible(aip.getState().equals(AIPState.UNDER_APPRAISAL));
      preservationSidebar.setVisible(true);
      actionsSidebar.setVisible(true);

      // Set button visibility

      createItem.setVisible(true);
      moveItem.setVisible(true);
      moveItem.setEnabled(true);
      editPermissions.setVisible(true);
      editPermissions.setEnabled(true);
      remove.setVisible(true);
      download.setVisible(true);
      searchSection.setVisible(true);

      keyboardFocus.setFocus(true);
      ListSelectionState.updateLayout(IndexedAIP.class, searchPrevious, searchNext);

    } else {
      viewAction();
    }
  }

  private void updateSectionDescriptiveMetadata(BrowseAIPBundle bundle) {
    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<Pair<String, HTML>>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();

    List<DescriptiveMetadataViewBundle> descMetadata = bundle.getDescriptiveMetadata();
    if (descMetadata != null) {
      for (DescriptiveMetadataViewBundle descMetadatum : descMetadata) {
        String title = descMetadatum.getLabel() != null ? descMetadatum.getLabel() : descMetadatum.getId();
        HTML container = new HTML();
        container.addStyleName("metadataContent");
        descriptiveMetadata.add(container, title);
        descriptiveMetadataContainers.add(Pair.create(descMetadatum.getId(), container));
        bundles.put(descMetadatum.getId(), descMetadatum);
      }
    }

    HandlerRegistration tabHandler = descriptiveMetadata.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
          Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
          String descId = pair.getFirst();
          final HTML html = pair.getSecond();
          final DescriptiveMetadataViewBundle bundle = bundles.get(descId);
          if (html.getText().length() == 0) {
            getDescriptiveMetadataHTML(aipId, descId, bundle, new AsyncCallback<SafeHtml>() {

              @Override
              public void onFailure(Throwable caught) {
                if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
                  Toast.showError(messages.errorLoadingDescriptiveMetadata(caught.getMessage()));
                }
              }

              @Override
              public void onSuccess(SafeHtml result) {
                html.setHTML(result);
              }
            });
          }
        }
      }
    });

    final int addTabIndex = descriptiveMetadata.getWidgetCount();
    FlowPanel addTab = new FlowPanel();
    addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
    descriptiveMetadata.add(new Label(), addTab);
    HandlerRegistration addTabHandler = descriptiveMetadata.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() == addTabIndex) {
          newDescriptiveMetadataRedirect();
        }
      }
    });
    addTab.addStyleName("addTab");
    addTab.getParent().addStyleName("addTabWrapper");

    handlers.add(tabHandler);
    handlers.add(addTabHandler);

    if (descMetadata != null && !descMetadata.isEmpty()) {
      descriptiveMetadata.setVisible(true);
      descriptiveMetadata.selectTab(0);
    } else {
      newDescriptiveMetadata.setVisible(true);
    }
  }

  private void updateSectionIdentification(BrowseAIPBundle bundle) {

    IndexedAIP aip = bundle.getAip();

    browseItemHeader.setVisible(true);

    breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(bundle.getAIPAncestors(), aip));
    breadcrumb.setVisible(true);
    newRepresentation.setVisible(true);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getElementLevelIconHTMLPanel(aip.getLevel());
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");
    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");
    itemId.setText(messages.itemIdMin(aip.getId()));
    itemId.addStyleName("browseItemId");

    if (!aip.getIngestSIPIds().isEmpty()) {
      sipId.setText(messages.sipIdMin(StringUtils.prettyPrint(aip.getIngestSIPIds())));
      sipId.addStyleName("browseSipId");
    }

    if (StringUtils.isNotBlank(aip.getIngestJobId())) {
      final IndexedAIP ingestedAIP = aip;

      InlineHTML html = new InlineHTML();
      html.setText(messages.processId());

      Anchor anchor = new Anchor();
      anchor.setText(aip.getIngestJobId());
      anchor.setHref(HistoryUtils.createHistoryHashLink(ShowJobReport.RESOLVER,
        ingestedAIP.getIngestJobId() + '-' + ingestedAIP.getId()));

      ingestJobId.add(html);
      ingestJobId.add(anchor);
      ingestJobId.addStyleName("browseIngestJobId");
    }
  }

  protected void viewAction() {
    aipId = null;

    browseTitle.setVisible(true);
    browseDescription.setVisible(true);
    addStyleName(BROWSE_TOP_CSS);

    breadcrumb.updatePath(
      Arrays.asList(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), "", RESOLVER.getHistoryPath())));

    HTMLPanel topIcon = DescriptionLevelUtils.getTopIconHTMLPanel();
    topIcon.addStyleName("browseItemIcon-all");
    itemIcon.setWidget(topIcon);
    itemTitle.setText(messages.allCollectionsTitle());
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    aipChildrenSearch.setDefaultFilter(COLLECTIONS_FILTER);
    aipChildrenList.set(COLLECTIONS_FILTER, justActive, FACETS);

    aipChildrenSearch.setVisible(true);
    aipChildrenList.setVisible(true);
    aipChildrenList.getParent().setVisible(true);

    actionsSidebar.setVisible(true);

    // Set button visibility
    createItem.setVisible(true);
    moveItem.setVisible(true);
    moveItem.setEnabled(false);
    editPermissions.setVisible(true);
    editPermissions.setEnabled(false);
    remove.setVisible(true);
    newRepresentation.setVisible(false);

    // downloadSection.setVisible(false);
    download.setVisible(false);
    newRepresentation.setVisible(false);

    searchSection.setVisible(false);

    this.removeStyleName("inactive");
    aipState.setVisible(false);
  }

  private void removeHandlerRegistrations() {
    for (HandlerRegistration handlerRegistration : handlers) {
      handlerRegistration.removeHandler();
    }
    handlers.clear();
  }

  private void getDescriptiveMetadataHTML(final String aipId, final String descId,
    final DescriptiveMetadataViewBundle bundle, final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            // Download link
            SafeUri downloadUri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, descId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = (RestErrorOverlayType) JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTranformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            callback.onSuccess(b.toSafeHtml());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          callback.onFailure(exception);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }

  @SuppressWarnings("unused")
  private String getDatesText(IndexedAIP aip) {
    return Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), true);
  }

  private boolean updateHistory(String id) {
    boolean historyUpdated;
    List<String> path;
    if (id == null) {
      path = RESOLVER.getHistoryPath();
    } else {
      path = ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), id);
    }

    if (path.equals(History.getToken())) {
      historyUpdated = false;
    } else {
      HistoryUtils.newHistory(path);
      historyUpdated = true;
    }
    return historyUpdated;
  }

  @SuppressWarnings("rawtypes")
  public SelectedItems getSelected() {
    return aipChildrenList.getSelected();
  }

  @UiHandler("preservationEvents")
  void buttonPreservationEventsHandler(ClickEvent e) {
    if (aipId != null) {
      HistoryUtils.newHistory(RESOLVER, PreservationEvents.BROWSE_RESOLVER.getHistoryToken(), aipId);
    }
  }

  @UiHandler("createItem")
  void buttonCreateItemHandler(ClickEvent e) {
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    BrowserService.Util.getInstance().createAIP(aipId, aipType, new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(String itemAIPId) {
        view(itemAIPId);
        HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, "aip", itemAIPId, CreateDescriptiveMetadata.NEW);
      }
    });
  }

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {

    final SelectedItems<IndexedAIP> selected = aipChildrenList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      // Remove the whole folder

      final SelectedItems<IndexedRepresentation> selectedRepresentations = representationsList.getSelected();
      if (ClientSelectedItemsUtils.isEmpty(selectedRepresentations)) {
        if (aipId != null) {
          Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(), messages.removeAllConfirmDialogMessage(),
            messages.dialogNo(), messages.dialogYes(), new AsyncCallback<Boolean>() {

              @Override
              public void onFailure(Throwable caught) {
                // nothing to do
              }

              @Override
              public void onSuccess(Boolean confirmed) {
                if (confirmed) {
                  Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
                    RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(),
                    new AsyncCallback<String>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        // do nothing
                      }

                      @Override
                      public void onSuccess(final String details) {
                        SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<IndexedAIP>(Arrays.asList(aipId),
                          IndexedAIP.class.getName());
                        BrowserService.Util.getInstance().deleteAIP(selected, details, new AsyncCallback<String>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            AsyncCallbackUtils.defaultFailureTreatment(caught);
                          }

                          @Override
                          public void onSuccess(String parentId) {
                            if (parentId != null) {
                              HistoryUtils.newHistory(BrowseAIP.RESOLVER, parentId);
                            } else {
                              HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                            }
                          }
                        });
                      }
                    });
                }
              }
            });
        } else {
          Dialogs.showInformationDialog(messages.selectAnItemTitle(), messages.selectAnItemToRemoveDescription(),
            messages.dialogOk());
        }
      } else {
        Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(), messages.removeAllSelectedConfirmDialogMessage(),
          messages.dialogNo(), messages.dialogYes(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              // nothing to do
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(String details) {
                      BrowserService.Util.getInstance().deleteRepresentation(selectedRepresentations, details,
                        new AsyncCallback<Void>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            AsyncCallbackUtils.defaultFailureTreatment(caught);
                          }

                          @Override
                          public void onSuccess(Void nothing) {
                            representationsList.refresh();
                          }
                        });
                    }
                  });
              }
            }
          });
      }
    } else {
      // Remove all selected

      ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final Long size) {
          // TODO update messages
          Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
            messages.removeSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
            new AsyncCallback<Boolean>() {

              @Override
              public void onSuccess(Boolean confirmed) {
                if (confirmed) {
                  Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
                    RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(),
                    new AsyncCallback<String>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        // do nothing
                      }

                      @Override
                      public void onSuccess(final String details) {
                        BrowserService.Util.getInstance().deleteAIP(selected, details,
                          new LoadingAsyncCallback<String>() {

                            @Override
                            public void onFailureImpl(Throwable caught) {
                              AsyncCallbackUtils.defaultFailureTreatment(caught);
                              aipChildrenList.refresh();
                            }

                            @Override
                            public void onSuccessImpl(String parentId) {
                              Toast.showInfo(messages.removeSuccessTitle(), messages.removeSuccessMessage(size));
                              aipChildrenList.refresh();
                            }
                          });
                      }
                    });
                }
              }

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }
            });
        }

      });

    }
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataHandler(ClickEvent e) {
    newDescriptiveMetadataRedirect();
  }

  private void newDescriptiveMetadataRedirect() {
    if (aipId != null) {
      HistoryUtils.newHistory(RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(),
        CreateDescriptiveMetadata.AIP, aipId);
    }
  }

  @UiHandler("moveItem")
  void buttonMoveItemHandler(ClickEvent e) {
    final SelectedItems<IndexedAIP> selected = aipChildrenList.getSelected();
    int counter = 0;

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      // Move this item

      if (aipId != null && bundle != null) {
        Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId),
          new NotSimpleFilterParameter(RodaConstants.AIP_ID, aipId));
        SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive, false);
        if (bundle.getAip().getParentID() != null) {
          selectAipDialog.setEmptyParentButtonVisible(true);
        }
        selectAipDialog.setSingleSelectionMode();
        selectAipDialog.showAndCenter();
        selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

          @Override
          public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
            final IndexedAIP parentAIP = event.getValue();
            final String parentId = (parentAIP != null) ? parentAIP.getId() : null;
            final SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<IndexedAIP>(Arrays.asList(aipId),
              IndexedAIP.class.getName());

            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                    new AsyncCallback<IndexedAIP>() {

                      @Override
                      public void onSuccess(IndexedAIP result) {
                        if (result != null) {
                          HistoryUtils.newHistory(BrowseAIP.RESOLVER, result.getId());
                        } else {
                          HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                        }
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        if (caught instanceof NotFoundException) {
                          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                        } else {
                          AsyncCallbackUtils.defaultFailureTreatment(caught);
                        }
                      }
                    });
                }
              });
          }
        });
      } else {
        Dialogs.showInformationDialog(messages.selectAnItemTitle(), messages.selectAnItemToMoveDescription(),
          messages.dialogOk());
      }
    } else {
      // Move all selected
      Filter filter = new Filter();
      boolean showEmptyParentButton;

      if (aipId != null) {
        filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId));
        filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ID, aipId));
        showEmptyParentButton = true;
      } else {
        if (selected instanceof SelectedItemsList) {
          SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selected;
          counter = list.getIds().size();
          if (counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
            for (String id : list.getIds()) {
              filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id));
              filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ID, id));
            }
          }
        } else {
          filter = Filter.ALL;
        }
        showEmptyParentButton = false;
      }

      SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive, true);
      selectAipDialog.setEmptyParentButtonVisible(showEmptyParentButton);
      selectAipDialog.showAndCenter();
      if (counter > 0 && counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
        selectAipDialog.addStyleName("object-dialog");
      }
      selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

        @Override
        public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
          final IndexedAIP parentAIP = event.getValue();
          final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                  new LoadingAsyncCallback<IndexedAIP>() {

                    @Override
                    public void onSuccessImpl(IndexedAIP result) {
                      if (result != null) {
                        HistoryUtils.newHistory(BrowseAIP.RESOLVER, result.getId());
                      } else {
                        HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                      }
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof NotFoundException) {
                        Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                      } else {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }
                    }
                  });
              }
            });
        }
      });

    }
  }

  @UiHandler("newProcess")
  void buttonNewProcessHandler(ClickEvent e) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    final SelectedItems<IndexedAIP> selectedSubs = aipChildrenList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selectedSubs)) {
      final SelectedItems<IndexedRepresentation> selectedReps = representationsList.getSelected();
      if (ClientSelectedItemsUtils.isEmpty(selectedReps)) {
        if (aipId != null) {
          selectedItems.setSelectedItems(SelectedItemsList.create(IndexedAIP.class, aipId));
        }
      } else {
        selectedItems.setSelectedItems(selectedReps);
      }
    } else {
      selectedItems.setSelectedItems(selectedSubs);
    }

    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateJob.RESOLVER, "action");
  }

  @UiHandler("editPermissions")
  void buttonEditPermissionsHandler(ClickEvent e) {
    final SelectedItems<IndexedAIP> selected = aipChildrenList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      if (aipId != null) {
        HistoryUtils.newHistory(RESOLVER, EditPermissions.RESOLVER.getHistoryToken(), aipId);
      }
    } else {
      LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
      selectedItems.setSelectedItems(selected);
      HistoryUtils.newHistory(RESOLVER, EditPermissions.RESOLVER.getHistoryToken());
    }
  }

  @UiHandler("risks")
  void buttonRisksHandler(ClickEvent e) {
    if (aipId != null) {
      HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, aipId);
    }
  }

  @UiHandler("logs")
  void buttonLogsHandler(ClickEvent e) {
    if (aipId != null) {
      HistoryUtils.newHistory(UserLog.RESOLVER, aipId);
    }
  }

  @UiHandler("download")
  void downloadButtonHandler(ClickEvent e) {
    SafeUri downloadUri = null;
    downloadUri = RestUtils.createAIPDownloadUri(aipId);
    Window.Location.assign(downloadUri.asString());
  }

  @UiHandler("appraisalAccept")
  void appraisalAcceptHandler(ClickEvent e) {
    final boolean accept = true;
    final SelectedItems<IndexedAIP> selected = SelectedItemsList.create(IndexedAIP.class, aipId);
    String rejectReason = null;
    BrowserService.Util.getInstance().appraisal(selected, accept, rejectReason,
      LocaleInfo.getCurrentLocale().getLocaleName(), new LoadingAsyncCallback<Void>() {

        @Override
        public void onSuccessImpl(Void result) {
          Toast.showInfo(messages.dialogDone(), messages.itemWasAccepted());
          // reload
          clear();
          viewAction(aipId);
        }
      });
  }

  @UiHandler("appraisalReject")
  void appraisalRejectHandler(ClickEvent e) {
    final boolean accept = false;
    final SelectedItems<IndexedAIP> selected = SelectedItemsList.create(IndexedAIP.class, aipId);
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @Override
        public void onSuccess(final String rejectReason) {
          BrowserService.Util.getInstance().appraisal(selected, accept, rejectReason,
            LocaleInfo.getCurrentLocale().getLocaleName(), new LoadingAsyncCallback<Void>() {

              @Override
              public void onSuccessImpl(Void result) {
                Toast.showInfo(messages.dialogDone(), messages.itemWasRejected());
                HistoryUtils.newHistory(IngestAppraisal.RESOLVER);
              }
            });
        }
      });
  }

  @UiHandler("searchContext")
  void searchContextHandler(ClickEvent e) {
    HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_ITEMS, RodaConstants.AIP_ANCESTORS, aipId);
  }

  @UiHandler("searchAIP")
  void searchAIPHandler(ClickEvent e) {
    HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_REPRESENTATIONS, RodaConstants.AIP_AIP_ID, aipId);
  }

  @UiHandler("newRepresentation")
  void createRepresentationHandler(ClickEvent e) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String details) {
          BrowserService.Util.getInstance().createRepresentation(aipId, details, new LoadingAsyncCallback<String>() {

            @Override
            public void onSuccessImpl(String representationId) {
              HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationId);
            }
          });
        }
      });
  }
}
