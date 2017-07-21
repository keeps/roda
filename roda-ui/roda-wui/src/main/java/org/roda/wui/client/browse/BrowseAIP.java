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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.process.ShowJobReport;
import org.roda.wui.client.ingest.transfer.TransferUpload;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
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
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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

  private static final Filter COLLECTIONS_FILTER = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
  private static Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
    new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private static final List<String> aipFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_STATE, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL,
      RodaConstants.INGEST_SIP_IDS, RodaConstants.INGEST_JOB_ID, RodaConstants.INGEST_UPDATE_JOB_IDS));

  private String aipId;

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
  FlowPanel ingestJobId, ingestUpdateJobIds;

  @UiField
  Label dateCreated, dateUpdated;

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
  SimplePanel actionsSidebar;

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
    handlers = new ArrayList<>();
    boolean selectable = true;

    // REPRESENTATIONS
    representationsList = new RepresentationList(Filter.NULL, justActive, Facets.NONE, messages.listOfRepresentations(),
      true);
    ListSelectionUtils.bindBrowseOpener(representationsList);

    representationsSearch = new SearchPanel(Filter.NULL, RodaConstants.REPRESENTATION_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);
    representationsSearch.setList(representationsList);

    // DISSEMINATIONS
    disseminationsList = new DIPList(Filter.NULL, Facets.NONE, messages.listOfDisseminations(), true);
    disseminationsList.setActionable(DisseminationActions.get());
    ListSelectionUtils.bindBrowseOpener(disseminationsList);

    disseminationsSearch = new SearchPanel(Filter.NULL, RodaConstants.DIP_SEARCH, true, messages.searchPlaceHolder(),
      false, false, true);
    disseminationsSearch.setList(disseminationsList);

    // AIP CHILDREN
    aipChildrenList = new AIPList(Filter.NULL, justActive, facets, messages.listOfAIPs(), selectable);
    ListSelectionUtils.bindBrowseOpener(aipChildrenList);

    aipChildrenSearch = new SearchPanel(COLLECTIONS_FILTER, RodaConstants.AIP_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);
    aipChildrenSearch.setList(aipChildrenList);

    facetDescriptionLevels = new FlowPanel();
    facetHasRepresentations = new FlowPanel();

    Map<String, FlowPanel> facetPanels = new HashMap<>();
    facetPanels.put(RodaConstants.AIP_LEVEL, facetDescriptionLevels);
    facetPanels.put(RodaConstants.AIP_HAS_REPRESENTATIONS, facetHasRepresentations);

    FacetUtils.bindFacets(aipChildrenList, facetPanels);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // HEADER
    browseDescription.add(new HTMLWidgetWrapper("BrowseDescription.html"));

    // CSS
    this.addStyleName("browse");
  }

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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    clear(false);
    if (historyTokens.isEmpty()) {
      viewAction();
      callback.onSuccess(this);
    } else if (historyTokens.size() == 1
      && !historyTokens.get(0).equals(EditPermissions.AIP_RESOLVER.getHistoryToken())) {
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
    } else if (!historyTokens.isEmpty()
      && historyTokens.get(0).equals(EditPermissions.AIP_RESOLVER.getHistoryToken())) {
      EditPermissions.AIP_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (!historyTokens.isEmpty()
      && historyTokens.get(0).equals(EditPermissions.DIP_RESOLVER.getHistoryToken())) {
      EditPermissions.DIP_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(BrowseRepresentation.RESOLVER.getHistoryToken())) {
      BrowseRepresentation.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (!historyTokens.isEmpty()
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
        aipFieldsToReturn, new AsyncCallback<BrowseAIPBundle>() {

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

  private void clear(boolean clearFacets) {
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
    ingestUpdateJobIds.clear();
    ingestUpdateJobIds.removeStyleName("browseIngestJobId");

    dateCreated.setText("");
    dateCreated.removeStyleName("browseItemId");
    dateUpdated.setText("");
    dateUpdated.removeStyleName("browseItemId");

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

    actionsSidebar.setVisible(false);

    searchSection.setVisible(false);

    // Set button visibility
    for (AIPState state : AIPState.values()) {
      this.removeStyleName(state.toString().toLowerCase());
    }

    if (clearFacets) {
      facets = new Facets(new SimpleFacetParameter(RodaConstants.AIP_LEVEL),
        new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    }
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
    } else {
      title = messages.genericErrorTitle();
      message = messages.genericErrorMessage(caught.getMessage());
    }

    HTML messageHTML = new HTML(message);
    messageHTML.addStyleName("error");
    descriptiveMetadata.add(messageHTML, title.asString(), true);
    descriptiveMetadata.selectTab(0);
    descriptiveMetadata.setVisible(true);
    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
  }

  protected void viewAction(BrowseAIPBundle bundle) {
    if (bundle != null) {
      addStyleName(BROWSE_AIP_CSS);

      Element firstElement = this.getElement().getFirstChildElement();
      if ("input".equalsIgnoreCase(firstElement.getTagName())) {
        firstElement.setAttribute("title", "browse input");
      }

      IndexedAIP aip = bundle.getAip();
      AIPState state = aip.getState();
      this.justActive = AIPState.ACTIVE.equals(state);

      // STATUS
      for (AIPState s : AIPState.values()) {
        this.removeStyleName(s.toString().toLowerCase());
      }

      this.addStyleName(state.toString().toLowerCase());
      aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(state));
      aipState.setVisible(!justActive);

      // IDENTIFICATION
      updateSectionIdentification(bundle);

      // DESCRIPTIVE METADATA
      updateSectionDescriptiveMetadata(bundle);

      // REPRESENTATIONS
      if (bundle.getRepresentationCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId()));
        representationsSearch.setDefaultFilter(filter, true);
        representationsSearch.clearSearchInputBox();
        representationsList.set(filter, justActive, Facets.NONE);
        representationsList.setActionable(RepresentationActions.get(aipId));
      }

      representationsTitle.setVisible(bundle.getRepresentationCount() > 0);
      representationsSearch.setVisible(bundle.getRepresentationCount() > 0);
      representationsList.setVisible(bundle.getRepresentationCount() > 0);
      representationsList.getParent().setVisible(bundle.getRepresentationCount() > 0);

      // DISSEMINATIONS
      if (bundle.getDipCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId()));
        disseminationsSearch.setDefaultFilter(filter, true);
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
        aipChildrenSearch.setDefaultFilter(filter, true);
        aipChildrenSearch.clearSearchInputBox();
        aipChildrenList.set(filter, justActive, facets);
        LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
        aipChildrenList.setActionable(AipActions.get(aip.getId(), aip.getState()));
      }

      aipChildrenTitle.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenSearch.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenList.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenList.getParent().setVisible(bundle.getChildAIPCount() > 0);

      // SIDEBAR
      itemsFacets.setVisible(false);
      actionsSidebar.setVisible(true);
      actionsSidebar.setWidget(AipActions.get().createActionsLayout(aip, new AsyncCallback<Actionable.ActionImpact>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Actionable.ActionImpact impact) {
          if (Actionable.ActionImpact.UPDATED.equals(impact)) {
            // reload
            clear(true);
            viewAction(aipId);
          }
        }
      }));

      // Set button visibility
      keyboardFocus.setFocus(true);
      ListSelectionUtils.bindLayout(aip, searchPrevious, searchNext, keyboardFocus, true, false, false, searchSection);

    } else {
      viewAction();
    }
  }

  private void updateSectionDescriptiveMetadata(BrowseAIPBundle bundle) {
    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();

    List<DescriptiveMetadataViewBundle> descMetadata = bundle.getDescriptiveMetadata();
    if (descMetadata != null) {
      for (DescriptiveMetadataViewBundle descMetadatum : descMetadata) {
        String title = descMetadatum.getLabel() != null ? descMetadatum.getLabel() : descMetadatum.getId();
        HTML container = new HTML();
        container.addStyleName("metadataContent");
        descriptiveMetadata.add(container, title);
        descriptiveMetadataContainers.add(Pair.of(descMetadatum.getId(), container));
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
          final DescriptiveMetadataViewBundle descBundle = bundles.get(descId);
          if (html.getText().length() == 0) {
            getDescriptiveMetadataHTML(aipId, descId, descBundle, new AsyncCallback<SafeHtml>() {

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

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
  }

  private void updateSectionIdentification(BrowseAIPBundle bundle) {
    IndexedAIP aip = bundle.getAip();

    browseItemHeader.setVisible(true);

    breadcrumb.updatePath(BreadcrumbUtils.getAipBreadcrumbs(bundle.getAIPAncestors(), aip));
    breadcrumb.setVisible(true);

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

    if (!aip.getIngestUpdateJobIds().isEmpty()) {
      final String id = aip.getId();

      InlineHTML html = new InlineHTML();
      html.setText(messages.updateProcessId());
      ingestUpdateJobIds.add(html);
      ingestUpdateJobIds.addStyleName("browseIngestJobId");
      Iterator<String> jobIterator = aip.getIngestUpdateJobIds().iterator();

      while (jobIterator.hasNext()) {
        String updateJobId = jobIterator.next();

        Anchor anchor = new Anchor();
        anchor.setText(updateJobId);
        anchor.setHref(HistoryUtils.createHistoryHashLink(ShowJobReport.RESOLVER, updateJobId + '-' + id));
        ingestUpdateJobIds.add(anchor);

        if (jobIterator.hasNext()) {
          ingestUpdateJobIds.add(new InlineHTML(", "));
        }
      }
    }

    if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy()));
      dateCreated.addStyleName("browseItemId");
    }

    if (aip.getUpdatedOn() != null && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy()));
      dateUpdated.addStyleName("browseItemId");
    }

  }

  protected void viewAction() {
    aipId = null;

    browseTitle.setVisible(true);
    browseDescription.setVisible(true);
    addStyleName(BROWSE_TOP_CSS);

    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    breadcrumb.updatePath(
      Arrays.asList(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), "", RESOLVER.getHistoryPath())));

    HTMLPanel topIcon = DescriptionLevelUtils.getTopIconHTMLPanel();
    topIcon.addStyleName("browseItemIcon-all");
    itemIcon.setWidget(topIcon);
    itemTitle.setText(messages.allCollectionsTitle());
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    aipChildrenSearch.setDefaultFilter(COLLECTIONS_FILTER, true);
    aipChildrenList.set(COLLECTIONS_FILTER, justActive, facets);
    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
    aipChildrenList.setActionable(AipActions.get());

    aipChildrenSearch.setVisible(true);
    aipChildrenList.setVisible(true);
    aipChildrenList.getParent().setVisible(true);

    actionsSidebar.setVisible(true);
    actionsSidebar.setWidget(
      AipActions.get().createActionsLayout(AipActions.NO_AIP_OBJECT, new AsyncCallback<Actionable.ActionImpact>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(Actionable.ActionImpact impact) {
          if (Actionable.ActionImpact.UPDATED.equals(impact)) {
            // reload
            clear(true);
            viewAction(aipId);
          }
        }
      }));

    // Set button visibility
    searchSection.setVisible(false);
    itemsFacets.setVisible(true);

    this.removeStyleName("inactive");
    aipState.setVisible(false);

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
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
          String escapedDescId = SafeHtmlUtils.htmlEscape(descId);

          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                escapedDescId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId,
              escapedDescId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            // Download link
            SafeUri downloadUri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, escapedDescId);
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
                escapedDescId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId,
              escapedDescId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTransformToHTMLError());
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

  private boolean updateHistory(String id) {
    boolean historyUpdated;
    List<String> path;
    if (id == null) {
      path = RESOLVER.getHistoryPath();
    } else {
      path = ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), id);
    }

    if (path.equals(HistoryUtils.getCurrentHistoryPath())) {
      historyUpdated = false;
    } else {
      HistoryUtils.newHistory(path);
      historyUpdated = true;
    }
    return historyUpdated;
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataHandler(ClickEvent e) {
    newDescriptiveMetadataRedirect();
  }

  private void newDescriptiveMetadataRedirect() {
    if (aipId != null) {
      HistoryUtils.newHistory(RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(),
        RodaConstants.RODA_OBJECT_AIP, aipId);
    }
  }

  @UiHandler("searchContext")
  void searchContextHandler(ClickEvent e) {
    HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_ITEMS, RodaConstants.AIP_ANCESTORS, aipId);
  }

  @UiHandler("searchAIP")
  void searchAIPHandler(ClickEvent e) {
    HistoryUtils.newHistory(Search.RESOLVER, RodaConstants.SEARCH_REPRESENTATIONS, RodaConstants.REPRESENTATION_AIP_ID,
      aipId);
  }

}
