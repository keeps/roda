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
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.tabs.BrowseAIPTabs;
import org.roda.wui.client.common.BrowseAIPActionsToolbar;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.cards.AIPDisseminationCardList;
import org.roda.wui.client.common.cards.AIPRepresentationCardList;
import org.roda.wui.client.common.labels.Header;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
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

  private static final List<String> fieldsToReturn = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static SimplePanel container;
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  static {
    fieldsToReturn.addAll(
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_STATE, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL,
        RodaConstants.INGEST_SIP_IDS, RodaConstants.INGEST_JOB_ID, RodaConstants.INGEST_UPDATE_JOB_IDS));
  }

  // Focus
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;
  @UiField
  BrowseAIPActionsToolbar objectToolbar;
  @UiField
  HTML aipState;
  // IDENTIFICATION
  @UiField
  TitlePanel title;
  @UiField
  BrowseAIPTabs browseTab;

  // AIP CHILDREN
  @UiField
  FlowPanel lowerContent;
  @UiField
  Header aipChildrenTitle;
  @UiField
  SimplePanel aipChildrenCard;
  @UiField
  FlowPanel center;

  // SIDEBAR
  @UiField
  FlowPanel sidePanel;
  @UiField
  FlowPanel representationCards;
  @UiField
  FlowPanel disseminationCards;

  private String aipId;
  private IndexedAIP aip;
  private SimplePanel descriptiveMetadataButtons;
  private Map<Integer, HTMLPanel> descriptiveMetadataSavedButtons;
  private TabPanel descriptiveMetadataTabPanel;
  private Button newDescriptiveMetadata;

  private BrowseAIP(BrowseAIPResponse response) {
    aip = response.getIndexedAIP();
    aipId = aip.getId();
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    RepresentationActions representationActions = RepresentationActions.get(aip.getId(), aip.getPermissions());
    DisseminationActions disseminationActions = DisseminationActions.get();
    AipActions aipActions = AipActions.get(aip.getId(), aip.getState(), aip.getPermissions());

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    AsyncCallback<Actionable.ActionImpact> listActionableCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onSuccess(Actionable.ActionImpact impact) {
        if (!Actionable.ActionImpact.NONE.equals(impact)) {
          refresh(aipId, new NoAsyncCallback<>());
        }
      }
    };

    // DESCRIPTIVE METADATA
    FlowPanel descriptiveMetadataTab = new FlowPanel();
    FlowPanel descriptiveMetadataTabCard = new FlowPanel();
    descriptiveMetadataTabCard.addStyleName("card descriptiveMetadataCard");
    descriptiveMetadataTabPanel = new TabPanel();
    descriptiveMetadataTab.addStyleName("browseItemMetadata");
    descriptiveMetadataTabCard.add(descriptiveMetadataTabPanel);
    descriptiveMetadataTab.add(descriptiveMetadataTabCard);
    newDescriptiveMetadata = new Button(messages.newDescriptiveMetadataTitle());
    newDescriptiveMetadata.addStyleName("btn btn-block btn-plus browseNewDescriptiveMetadataButton");
    newDescriptiveMetadata.addClickHandler(e -> {
      newDescriptiveMetadataRedirect();
    });
    descriptiveMetadataTab.add(newDescriptiveMetadata);
    browseTab.init(response);
    updateSectionDescriptiveMetadata(response.getDescriptiveMetadataInfos());

    // AIP CHILDREN
    aipChildrenTitle.setHeaderText(messages.sublevels());
    aipChildrenTitle.setIcon("cmi cmi-accountTree");
    aipChildrenTitle.setLevel(5);
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      ListBuilder<IndexedAIP> aipChildrenListBuilder;
      if (aip.getState().equals(AIPState.DESTROYED) || aip.isOnHold() || aip.getDisposalConfirmationId() != null) {
        aipChildrenListBuilder = new ListBuilder<>(ConfigurableAsyncTableCell::new,
          new AsyncTableCellOptions<>(IndexedAIP.class, "BrowseAIP_aipChildren")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId())))
            .withJustActive(justActive).withSummary(messages.listOfAIPs()).bindOpener());

      } else {
        aipChildrenListBuilder = new ListBuilder<>(ConfigurableAsyncTableCell::new,
          new AsyncTableCellOptions<>(IndexedAIP.class, "BrowseAIP_aipChildren")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aip.getId())))
            .withJustActive(justActive).withSummary(messages.listOfAIPs()).bindOpener().withActionable(aipActions)
            .withActionableCallback(listActionableCallback));
      }

      SearchWrapper aipChildrenSearchWrapper = new SearchWrapper(false)
        .createListAndSearchPanel(aipChildrenListBuilder);
      aipChildrenCard.setWidget(aipChildrenSearchWrapper);
      aipChildrenCard.setVisible(response.getChildAipsCount().getResult() > 0);
    } else {
      aipChildrenCard.setVisible(false);
    }

    PermissionClientUtils.bindPermission(newDescriptiveMetadata, aip.getPermissions(),
      RodaConstants.PERMISSION_METHOD_CREATE_DESCRIPTIVE_METADATA_FILE);

    // CSS
    newDescriptiveMetadata.getElement().setId("aipNewDescriptiveMetadata");
    keyboardFocus.addStyleName("browse browse_aip browse_main_panel");

    // make FocusPanel comply with WCAG
    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    // STATE
    keyboardFocus.addStyleName(aip.getState().toString().toLowerCase());
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(aip.getState()));
    aipState.setVisible(!justActive);

    // NAVIGATION TOOLBAR
    if (justActive) {
      navigationToolbar.withObject(aip);
      navigationToolbar.withPermissions(aip.getPermissions());
      navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.DESTROYED, () -> {
        if (StringUtils.isNotBlank(aip.getParentID())) {
          HistoryUtils.newHistory(BrowseTop.RESOLVER, aip.getParentID());
        } else {
          HistoryUtils.newHistory(BrowseTop.RESOLVER);
        }
      });
      navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.UPDATED,
        () -> refresh(aipId, new NoAsyncCallback<>()));
      navigationToolbar.build();
    }

    // OBJECT TOOLBAR
    if (justActive) {
      objectToolbar.setObjectAndBuild(aip);
    }

    // IDENTIFICATION
    updateSectionIdentification(response);

    // AIP CHILDREN
    if (aip.getState().equals(AIPState.ACTIVE)) {
      if (response.getChildAipsCount().getResult() > 0) {
        LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
      }

      lowerContent.setVisible(response.getChildAipsCount().getResult() > 0);
    }

    // Side panel representations
    if (response.getRepresentationCount().getResult() > 0 || response.getDipCount().getResult() > 0) {
      if (response.getRepresentationCount().getResult() > 0) {
        this.representationCards.add(new AIPRepresentationCardList(aipId));
      }
      if (response.getDipCount().getResult() > 0) {
        this.disseminationCards.add(new AIPDisseminationCardList(aipId));
      }
    } else {
      this.sidePanel.setVisible(false);
    }

    keyboardFocus.setFocus(true);
  }

  public static void getAndRefresh(String id, AsyncCallback<Widget> callback) {
    container = new SimplePanel();
    refresh(id, new AsyncCallback<IndexedAIP>() {
      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(IndexedAIP result) {
        callback.onSuccess(container);
      }
    });
  }

  private static void refresh(String id, AsyncCallback<IndexedAIP> callback) {

    Services service = new Services("Retrieve AIP", "get");
    service
      .rodaEntityRestService(s -> s.findByUuid(id, LocaleInfo.getCurrentLocale().getLocaleName()), IndexedAIP.class)
      .whenComplete((aip, error) -> {
        if (error != null) {
          if (error instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
            HistoryUtils.newHistory(ListUtils.concat(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(error);
          }
        } else {
          CompletableFuture<List<IndexedAIP>> futureAncestors = service.aipResource(s -> s.getAncestors(id));

          CompletableFuture<List<String>> futureRepFields = service
            .aipResource(AIPRestService::retrieveAIPRuleProperties);

          CompletableFuture<DescriptiveMetadataInfos> futureDescriptiveMetadataInfos = service
            .aipResource(s -> s.getDescriptiveMetadata(id, LocaleInfo.getCurrentLocale().getLocaleName()));

          CompletableFuture<LongResponse> futureChildAipCount = service
            .rodaEntityRestService(
              s -> s.count(new FindRequest.FindRequestBuilder(
                new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, id)), false).build()),
              IndexedAIP.class);

          CompletableFuture<LongResponse> futureRepCount = service.rodaEntityRestService(
            s -> s.count(
              new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, id)), false)),
            IndexedRepresentation.class);

          CompletableFuture<LongResponse> futureDipCount = service.rodaEntityRestService(
            s -> s.count(new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_IDS, id)), false)),
            IndexedDIP.class);

          CompletableFuture<LongResponse> futureIncidenceCount = service.rodaEntityRestService(
            s -> s.count(
              new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, id)), false)),
            RiskIncidence.class);

          CompletableFuture<LongResponse> futureEventCount = service.rodaEntityRestService(
            s -> s.count(new CountRequest(
              new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, id)), false)),
            IndexedPreservationEvent.class);

          CompletableFuture<LongResponse> futureLogCount = service.rodaEntityRestService(
            s -> s.count(
              new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, id)), false)),
            LogEntry.class);

          CompletableFuture
            .allOf(futureChildAipCount, futureRepCount, futureDipCount, futureAncestors, futureAncestors,
              futureRepFields, futureDescriptiveMetadataInfos, futureIncidenceCount, futureEventCount, futureLogCount)
            .thenApply(v -> {
              BrowseAIPResponse rp = new BrowseAIPResponse();
              rp.setIndexedAIP(aip);
              rp.setAncestors(futureAncestors.join());
              rp.setRepresentationInformationFields(futureRepFields.join());
              rp.setDescriptiveMetadataInfos(futureDescriptiveMetadataInfos.join());
              rp.setChildAipsCount(futureChildAipCount.join());
              rp.setRepresentationCount(futureRepCount.join());
              rp.setDipCount(futureDipCount.join());
              rp.setIncidenceCount(futureIncidenceCount.join());
              rp.setEventCount(futureEventCount.join());
              rp.setLogCount(futureLogCount.join());
              return rp;
            }).whenComplete((value, throwable) -> {

              if (throwable == null) {
                container.setWidget(new BrowseAIP(value));
                callback.onSuccess(aip);
              }
            });
        }
      });

  }

  private void updateSectionDescriptiveMetadata(DescriptiveMetadataInfos descriptiveMetadataInfos) {
    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<>();
    final Map<String, DescriptiveMetadataInfo> metadataInfos = new HashMap<>();

    List<DescriptiveMetadataInfo> descMetadata = descriptiveMetadataInfos.getDescriptiveMetadataInfoList();
    if (descMetadata != null) {
      for (DescriptiveMetadataInfo descMetadatum : descMetadata) {
        String title = descMetadatum.getLabel() != null ? descMetadatum.getLabel() : descMetadatum.getId();
        HTML container = new HTML();
        container.addStyleName("metadataContent");
        descriptiveMetadataTabPanel.add(container, title);
        descriptiveMetadataContainers.add(Pair.of(descMetadatum.getId(), container));
        metadataInfos.put(descMetadatum.getId(), descMetadatum);
      }
    }

    descriptiveMetadataTabPanel.addSelectionHandler(event -> {
      if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
        Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
        String descId = pair.getFirst();
        final HTML html = pair.getSecond();
        final DescriptiveMetadataInfo descBundle = metadataInfos.get(descId);
        if (html.getText().isEmpty()) {
          getDescriptiveMetadataHTML(aipId, descId, descBundle, event.getSelectedItem(), new AsyncCallback<SafeHtml>() {

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
    });

    if (PermissionClientUtils.hasPermissions(aip.getPermissions(),
      RodaConstants.PERMISSION_METHOD_CREATE_DESCRIPTIVE_METADATA_FILE) && aip.getState().equals(AIPState.ACTIVE)
      && !aip.isOnHold() && aip.getDisposalConfirmationId() == null) {
      final int addTabIndex = descriptiveMetadataTabPanel.getWidgetCount();
      FlowPanel addTab = new FlowPanel();
      addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
      descriptiveMetadataTabPanel.add(new Label(), addTab);
      descriptiveMetadataTabPanel.addSelectionHandler(event -> {
        if (event.getSelectedItem() == addTabIndex) {
          newDescriptiveMetadataRedirect();
        }
      });

      addTab.addStyleName("addTab");
      addTab.getElement().setId("aipNewDescriptiveMetadata");
      addTab.getParent().addStyleName("addTabWrapper");
    }

    descriptiveMetadataSavedButtons = new HashMap<>();
    descriptiveMetadataButtons = new SimplePanel();
    descriptiveMetadataButtons.addStyleName("descriptiveMetadataTabButtons");
    descriptiveMetadataTabPanel.getTabBar().getElement().getStyle().clearProperty("width");
    descriptiveMetadataTabPanel.getTabBar().getElement().getParentElement()
      .insertFirst(descriptiveMetadataButtons.getElement());
    descriptiveMetadataTabPanel.addSelectionHandler(event -> {
      if (descriptiveMetadataSavedButtons.containsKey(event.getSelectedItem())) {
        descriptiveMetadataButtons.setWidget(descriptiveMetadataSavedButtons.get(event.getSelectedItem()));
      } else {
        descriptiveMetadataButtons.clear();
      }
    });

    if (descMetadata != null && !descMetadata.isEmpty()) {
      descriptiveMetadataTabPanel.getParent().setVisible(true);
      newDescriptiveMetadata.setVisible(false);

      int index = ConfigurationManager.getInt(0, "ui.browser.metadata.index.aip");
      if (index > 0) {
        if (descMetadata.size() > index) {
          descriptiveMetadataTabPanel.selectTab(index);
        } else {
          descriptiveMetadataTabPanel.selectTab(0);
        }
      } else {
        int count = descMetadata.size() - Math.abs(index);
        if (descMetadata.size() > count) {
          descriptiveMetadataTabPanel.selectTab(count);
        } else {
          descriptiveMetadataTabPanel.selectTab(0);
        }
      }
    } else {
      descriptiveMetadataTabPanel.getParent().setVisible(false);
      newDescriptiveMetadata.setVisible(true);
    }

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadataTabPanel.getElement());
  }

  private void updateSectionIdentification(BrowseAIPResponse response) {

    title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
    title.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
    Sliders.createAipInfoSlider(center, navigationToolbar.getInfoSidebarButton(), response);

    navigationToolbar.updateBreadcrumb(aip, response.getAncestors());
  }

  private void getDescriptiveMetadataHTML(final String aipId, final String descId, final DescriptiveMetadataInfo bundle,
    final Integer selectedIndex, final AsyncCallback<SafeHtml> callback) {
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

            if (bundle.isHasHistory() && PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_RETRIEVE_AIP_DESCRIPTIVE_METADATA_VERSIONS)) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                escapedDescId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            if (!AIPState.DESTROYED.equals(aip.getState()) && !aip.isOnHold()
              && aip.getDisposalConfirmationId() == null) {
              if (PermissionClientUtils.hasPermissions(aip.getPermissions(),
                RodaConstants.PERMISSION_METHOD_UPDATE_AIP_DESCRIPTIVE_METADATA_FILE)) {
                String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId,
                  escapedDescId);
                String editLinkHtml = "<a href='" + editLink
                  + "' class='toolbarLink' id='aipEditDescriptiveMetadata'><i class='fa fa-edit'></i></a>";
                b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));
              }
            }

            // Download link
            SafeUri downloadUri = RestUtils.createDescriptiveMetadataDownloadUri(aipId, escapedDescId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            HTMLPanel buttons = new HTMLPanel(b.toSafeHtml());
            descriptiveMetadataSavedButtons.put(selectedIndex, buttons);
            descriptiveMetadataButtons.setWidget(buttons);

            b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            SafeHtml safeHtml = b.toSafeHtml();
            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.isHasHistory() && PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_RETRIEVE_AIP_DESCRIPTIVE_METADATA_VERSIONS)) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId,
                escapedDescId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            if (PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_UPDATE_AIP_DESCRIPTIVE_METADATA_FILE)) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId,
                escapedDescId);
              String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));
            }

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

  private void newDescriptiveMetadataRedirect() {
    if (aipId != null) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER, CreateDescriptiveMetadata.RESOLVER.getHistoryToken(),
        RodaConstants.RODA_OBJECT_AIP, aipId);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, BrowseAIP> {
  }
}
