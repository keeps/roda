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
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.RepresentationList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.slider.SliderPanel;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.transfer.TransferUpload;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
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

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private static final List<String> aipFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_STATE, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL,
      RodaConstants.INGEST_SIP_IDS, RodaConstants.INGEST_JOB_ID, RodaConstants.INGEST_UPDATE_JOB_IDS));

  private String aipId;

  private ActionableWidgetBuilder<IndexedAIP> actionableWidgetBuilder;
  private SliderPanel aipDetailsSlider;

  // Focus
  @UiField
  FocusPanel keyboardFocus;

  // HEADER

  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;

  @UiField
  FlowPanel browseDescription;

  @UiField
  SimplePanel bigTitle;

  @UiField
  SimplePanel smallTitle;

  // STATUS

  @UiField
  HTML aipState;

  // IDENTIFICATION

  @UiField
  Label browseItemHeader;

  @UiField
  FlowPanel identificationPanel;

  // DESCRIPTIVE METADATA

  @UiField
  TabPanel descriptiveMetadata;

  @UiField
  Button newDescriptiveMetadata;

  // REPRESENTATIONS

  @UiField
  SimplePanel representationsTitle;

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
  SimplePanel aipChildrenTitle;

  @UiField(provided = true)
  SearchPanel aipChildrenSearch;

  @UiField(provided = true)
  AIPList aipChildrenList;

  @UiField
  FlowPanel risksEventsLogs;

  @UiField
  FlowPanel center;
  @UiField
  Label dateCreatedAndModified;

  private List<HandlerRegistration> handlers;

  boolean justActive = true;

  private BrowseAIP() {
    handlers = new ArrayList<>();
    boolean selectable = true;
    actionableWidgetBuilder = new ActionableWidgetBuilder<>(AipActions.get());

    // REPRESENTATIONS
    representationsList = new RepresentationList("BrowseAIP_representations", Filter.NULL, justActive,
      messages.listOfRepresentations(), true);
    ListSelectionUtils.bindBrowseOpener(representationsList);

    representationsSearch = new SearchPanel(Filter.NULL, RodaConstants.REPRESENTATION_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);
    representationsSearch.setList(representationsList);

    // DISSEMINATIONS
    disseminationsList = new DIPList("BrowseAIP_disseminations", Filter.NULL, messages.listOfDisseminations(), true);
    disseminationsList.setActionable(DisseminationActions.get());
    ListSelectionUtils.bindBrowseOpener(disseminationsList);

    disseminationsSearch = new SearchPanel(Filter.NULL, RodaConstants.DIP_SEARCH, true, messages.searchPlaceHolder(),
      false, false, true);
    disseminationsSearch.setList(disseminationsList);

    // AIP CHILDREN
    aipChildrenList = new AIPList("BrowseAIP_aipChildren", Filter.NULL, justActive, messages.listOfAIPs(), selectable);
    ListSelectionUtils.bindBrowseOpener(aipChildrenList);

    aipChildrenSearch = new SearchPanel(COLLECTIONS_FILTER, RodaConstants.AIP_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);
    aipChildrenSearch.setList(aipChildrenList);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // HEADER
    browseDescription.add(new HTMLWidgetWrapper("BrowseDescription.html"));

    // CSS
    newDescriptiveMetadata.getElement().setId("aipNewDescriptiveMetadata");
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
    clear(true);
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
    bigTitle.setVisible(false);
    browseDescription.setVisible(false);

    identificationPanel.removeStyleName("browseTitle-allCollections-wrapper");
    smallTitle.setWidget(actionableWidgetBuilder.withTitleLoading().buildTitleWithoutActions());

    dateCreatedAndModified.setText("");
    if (aipDetailsSlider != null) {
      aipDetailsSlider.dispose();
    }
    navigationToolbar.clearBreadcrumb();
    navigationToolbar.hide();

    descriptiveMetadata.setVisible(false);
    descriptiveMetadata.clear();
    removeHandlerRegistrations();

    newDescriptiveMetadata.setVisible(false);

    // Representations list
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
    aipChildrenSearch.setVisible(false);
    aipChildrenSearch.clearSearchInputBox();
    aipChildrenList.setVisible(false);
    aipChildrenList.getParent().setVisible(false);

    // Set button visibility
    for (AIPState state : AIPState.values()) {
      this.removeStyleName(state.toString().toLowerCase());
    }

    if (clearFacets) {
      aipChildrenList.setFacets(ConfigurationManager.FacetFactory.getFacets(aipChildrenList.getListId()));
    }
  }

  protected void showError(String id, Throwable caught) {
    navigationToolbar.clearBreadcrumb();
    navigationToolbar.hide();

    smallTitle.setWidget(actionableWidgetBuilder
      .withTitleSmall(id, DescriptionLevelUtils.getElementLevelIconCssClass(null)).buildTitleWithoutActions());

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

      // NAVIGATION TOOLBAR
      navigationToolbar.setObject(aip);
      navigationToolbar.show();

      // IDENTIFICATION
      updateSectionIdentification(bundle);

      // DESCRIPTIVE METADATA
      updateSectionDescriptiveMetadata(bundle);

      // REPRESENTATIONS
      RepresentationActions representationActions = RepresentationActions.get(bundle.getAip());
      if (bundle.getRepresentationCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId()));
        representationsSearch.setDefaultFilter(filter, true);
        representationsSearch.clearSearchInputBox();
        representationsList.set(filter, justActive);
        representationsList.setActionable(representationActions);
      }

      representationsTitle.setWidget(new ActionableWidgetBuilder<>(representationActions)
        .withTitleForCard(messages.representationsTitle(),
          DescriptionLevelUtils.getRepresentationTypeIconCssClass(null))
        .buildTitleWithObjects(new ActionableObject<>(IndexedRepresentation.class)));
      representationsSearch.setVisible(bundle.getRepresentationCount() > 0);
      representationsList.setVisible(bundle.getRepresentationCount() > 0);
      representationsList.getParent().setVisible(true);

      // DISSEMINATIONS
      if (bundle.getDipCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId()));
        disseminationsSearch.setDefaultFilter(filter, true);
        disseminationsSearch.clearSearchInputBox();
        disseminationsList.set(filter, justActive);
      }

      disseminationsTitle.setVisible(bundle.getDipCount() > 0);
      disseminationsSearch.setVisible(bundle.getDipCount() > 0);
      disseminationsList.setVisible(bundle.getDipCount() > 0);
      disseminationsList.getParent().setVisible(bundle.getDipCount() > 0);

      // AIP CHILDREN
      AipActions aipActions = AipActions.get(aip.getId(), aip.getState());
      if (bundle.getChildAIPCount() > 0) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId()));
        aipChildrenSearch.setDefaultFilter(filter, true);
        aipChildrenSearch.clearSearchInputBox();
        aipChildrenList.set(filter, justActive);
        LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
        aipChildrenList.setActionable(aipActions);
      }

      aipChildrenTitle.setWidget(new ActionableWidgetBuilder<>(aipActions)
        .withTitleForCard(messages.sublevels(),
          DescriptionLevelUtils.getElementLevelIconCssClass(RodaConstants.AIP_CHILDREN))
        .buildTitleWithObjects(new ActionableObject<>(IndexedAIP.class)));
      aipChildrenSearch.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenList.setVisible(bundle.getChildAIPCount() > 0);
      aipChildrenList.getParent().setVisible(true);

      // Set button visibility
      keyboardFocus.setFocus(true);

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
    addTab.getElement().setId("aipNewDescriptiveMetadata");
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

    actionableWidgetBuilder
      .withTitleSmall(aip.getTitle() != null ? aip.getTitle() : aip.getId(),
        DescriptionLevelUtils.getElementLevelIconCssClass(aip.getLevel()))
      .withCallback(new NoAsyncCallback<Actionable.ActionImpact>() {
        @Override
        public void onSuccess(Actionable.ActionImpact impact) {
          if (Actionable.ActionImpact.UPDATED.equals(impact)) {
            // reload
            clear(true);
            viewAction(aipId);
          }
        }
      });

    smallTitle.setWidget(actionableWidgetBuilder.buildTitleWithObjects(new ActionableObject<>(aip)));

    browseItemHeader.setVisible(true);

    if (aipDetailsSlider != null) {
      aipDetailsSlider.dispose();
    }
    aipDetailsSlider = Sliders.createAipInfoSlider(center, navigationToolbar.getSidebarButton(), bundle);

    Anchor risksLink = new Anchor(messages.aipRiskIncidences(bundle.getRiskIncidenceCount()),
      HistoryUtils.createHistoryHashLink(RiskIncidenceRegister.RESOLVER, aip.getId()));
    Anchor eventsLink = new Anchor(messages.aipEvents(bundle.getPreservationEventCount()),
      HistoryUtils.createHistoryHashLink(PreservationEvents.BROWSE_RESOLVER, aip.getId()));
    Anchor logsLink = new Anchor(messages.aipLogs(bundle.getLogCount()),
      HistoryUtils.createHistoryHashLink(UserLog.RESOLVER, aip.getId()));

    risksEventsLogs.clear();
    risksEventsLogs.add(risksLink);
    risksEventsLogs.add(new Label(", "));
    risksEventsLogs.add(eventsLink);
    risksEventsLogs.add(new Label(" " + messages.and() + " "));
    risksEventsLogs.add(logsLink);

    navigationToolbar.updateBreadcrumb(bundle);

    identificationPanel.removeStyleName("browseTitle-allCollections-wrapper");

    if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy()) && aip.getUpdatedOn() != null
      && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      dateCreatedAndModified.setText(messages.dateCreatedAndUpdated(Humanize.formatDate(aip.getCreatedOn()),
        aip.getCreatedBy(), Humanize.formatDate(aip.getUpdatedOn()), aip.getUpdatedBy()));
    } else if (aip.getCreatedOn() != null && StringUtils.isNotBlank(aip.getCreatedBy())) {
      dateCreatedAndModified
        .setText(messages.dateCreated(Humanize.formatDateTime(aip.getCreatedOn()), aip.getCreatedBy()));
    } else if (aip.getUpdatedOn() != null && StringUtils.isNotBlank(aip.getUpdatedBy())) {
      dateCreatedAndModified
        .setText(messages.dateUpdated(Humanize.formatDateTime(aip.getUpdatedOn()), aip.getUpdatedBy()));
    } else {
      dateCreatedAndModified.setText("");
    }
  }

  protected void viewAction() {
    aipId = null;

    actionableWidgetBuilder.withCallback(new NoAsyncCallback<>());

    bigTitle.setWidget(actionableWidgetBuilder.withTitle(messages.allCollectionsTitle())
      .buildTitleWithObjects(new ActionableObject<>(IndexedAIP.class)));
    bigTitle.setVisible(true);
    browseDescription.setVisible(true);
    addStyleName(BROWSE_TOP_CSS);

    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    if (aipDetailsSlider != null) {
      aipDetailsSlider.dispose();
    }
    navigationToolbar.updateBreadcrumbPath(
      new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), "", RESOLVER.getHistoryPath()));

    identificationPanel.addStyleName("browseTitle-allCollections-wrapper");

    aipChildrenSearch.setDefaultFilter(COLLECTIONS_FILTER, true);
    aipChildrenList.set(COLLECTIONS_FILTER, justActive);
    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
    aipChildrenList.setActionable(AipActions.get());

    aipChildrenSearch.setVisible(true);
    aipChildrenList.setVisible(true);
    aipChildrenList.getParent().setVisible(true);

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
            String editLinkHtml = "<a href='" + editLink
              + "' class='toolbarLink' id='aipEditDescriptiveMetadata'><i class='fa fa-edit'></i></a>";
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
}
