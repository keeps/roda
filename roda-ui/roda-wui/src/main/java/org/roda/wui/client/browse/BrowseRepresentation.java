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
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfo;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.services.RepresentationRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class BrowseRepresentation extends Composite {
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final List<String> fieldsToReturn = new ArrayList<>(Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_TYPE));
  private static SimplePanel container;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        getAndRefresh(historyTokens.get(0), historyTokens.get(1), callback);
      } else {
        HistoryUtils.newHistory(BrowseTop.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(BrowseTop.RESOLVER, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation";
    }
  };
  // Focus
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  HTML aipState;
  @UiField
  TabPanel itemMetadata;
  @UiField
  Button newDescriptiveMetadata;
  @UiField(provided = true)
  SearchWrapper filesSearch;
  @UiField(provided = true)
  SearchWrapper disseminationsSearch;
  @UiField
  FlowPanel center;
  @UiField
  NavigationToolbar<IndexedRepresentation> navigationToolbar;

  // STATUS
  @UiField
  HTML representationIcon;

  // DESCRIPTIVE METADATA
  @UiField
  FlowPanel representationTitle;
  @UiField
  FlowPanel risksEventsLogs;
  @UiField
  Label dateCreatedAndModified;
  private final List<HandlerRegistration> handlers;

  // FILES
  private final IndexedAIP aip;

  // DISSEMINATIONS
  private final IndexedRepresentation representation;
  private final String aipId;
  private final String repId;
  private final String repUUID;
  private SimplePanel descriptiveMetadataButtons;
  private Map<Integer, HTMLPanel> descriptiveMetadataSavedButtons;

  public BrowseRepresentation(BrowseRepresentationResponse response) {
    this.representation = response.getIndexedRepresentation();
    this.aip = response.getIndexedAIP();
    this.aipId = aip.getId();
    this.repId = representation.getId();
    this.repUUID = representation.getUUID();

    handlers = new ArrayList<>();
    String summary = messages.representationListOfFiles();

    final AIPState state = aip.getState();
    final boolean justActive = AIPState.ACTIVE.equals(state);

    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);

    // FILES
    Filter filesFilter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID, repUUID),
      new EmptyKeyFilterParameter(RodaConstants.FILE_PARENT_UUID));

    ListBuilder<IndexedFile> fileListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedFile.class, "BrowseRepresentation_files").withFilter(filesFilter)
        .withJustActive(justActive).withSummary(summary).bindOpener()
        .withActionable(FileActions.get(aipId, repId, aip.getPermissions())));

    filesSearch = new SearchWrapper(false).createListAndSearchPanel(fileListBuilder);

    // DISSEMINATIONS
    Filter disseminationsFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, repUUID));

    ListBuilder<IndexedDIP> disseminationsListBuilder = new ListBuilder<>(() -> new DIPList(),
      new AsyncTableCellOptions<>(IndexedDIP.class, "BrowseRepresentation_disseminations")
        .withFilter(disseminationsFilter).withSummary(messages.listOfDisseminations()).bindOpener()
        .withJustActive(justActive).withActionable(DisseminationActions.get()));

    disseminationsSearch = new SearchWrapper(false).createListAndSearchPanel(disseminationsListBuilder);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // NAVIGATION TOOLBAR
    navigationToolbar.withObject(representation);
    navigationToolbar.withPermissions(aip.getPermissions());
    navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(BrowseTop.RESOLVER, representation.getAipId()));
    navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.UPDATED,
      () -> refresh(aipId, repId, new NoAsyncCallback<>()));
    navigationToolbar.build();

    Services services = response.getServices();
    Filter dipsFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, representation.getUUID()));
    CountRequest dipCountRequest = new CountRequest(new Filter(dipsFilter), true);
    CompletableFuture<LongResponse> dipCounterCompletableFuture = services
      .rodaEntityRestService(s -> s.count(dipCountRequest), IndexedDIP.class).handle((longResponse, throwable1) -> {
        if (throwable1 != null) {
          return new LongResponse(-1L);
        }
        return longResponse;
      });

    AndFiltersParameters riskIncidenceFilter = new AndFiltersParameters(
      Arrays.asList(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representation.getId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, representation.getAipId())));
    CountRequest riskIncidenceCountRequest = new CountRequest(new Filter(riskIncidenceFilter), true);
    CompletableFuture<LongResponse> riskIncidenceCounterCompletableFuture = services
      .rodaEntityRestService(s -> s.count(riskIncidenceCountRequest), RiskIncidence.class)
      .handle((longResponse, throwable1) -> {
        if (throwable1 != null) {
          return new LongResponse(-1L);
        }
        return longResponse;
      });

    Filter preservationEventFilter = new Filter(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, representation.getUUID()));
    CountRequest preservationEventsCountRequest = new CountRequest(preservationEventFilter, true);

    CompletableFuture<LongResponse> preservationCounterCompletableFuture = services
      .rodaEntityRestService(s -> s.count(preservationEventsCountRequest), IndexedPreservationEvent.class)
      .handle((longResponse, throwable2) -> {
        if (throwable2 != null) {
          return new LongResponse(-1L);
        }
        return longResponse;
      });

    CompletableFuture
      .allOf(dipCounterCompletableFuture, riskIncidenceCounterCompletableFuture, preservationCounterCompletableFuture)
      .thenRun(() -> {
        LongResponse riskIncidenceCounterResponse = riskIncidenceCounterCompletableFuture.join();
        LongResponse preservationCounterResponse = preservationCounterCompletableFuture.join();
        LongResponse dipCounterResponse = dipCounterCompletableFuture.join();

        updateLayout(response, riskIncidenceCounterResponse.getResult(), preservationCounterResponse.getResult(), state,
          justActive);

        // DESCRIPTIVE METADATA
        final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<>();
        final Map<String, DescriptiveMetadataInfo> bundles = new HashMap<>();
        for (DescriptiveMetadataInfo descriptiveMetadata : response.getDescriptiveMetadataInfos()
          .getDescriptiveMetadataInfoList()) {
          String title = descriptiveMetadata.getLabel() != null ? descriptiveMetadata.getLabel()
            : descriptiveMetadata.getId();
          HTML container = new HTML();
          container.addStyleName("metadataContent");
          itemMetadata.add(container, title);
          descriptiveMetadataContainers.add(Pair.of(descriptiveMetadata.getId(), container));
          bundles.put(descriptiveMetadata.getId(), descriptiveMetadata);
        }

        HandlerRegistration tabHandler = itemMetadata.addSelectionHandler(event -> {
          if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
            Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
            String descId = pair.getFirst();
            final HTML html = pair.getSecond();
            final DescriptiveMetadataInfo bundle = bundles.get(descId);
            if (html.getText().isEmpty()) {
              getDescriptiveMetadataHTML(descId, bundle, event.getSelectedItem(), new AsyncCallback<SafeHtml>() {

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

        final int addTabIndex = itemMetadata.getWidgetCount();
        FlowPanel addTab = new FlowPanel();
        addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
        itemMetadata.add(new Label(), addTab);
        HandlerRegistration addTabHandler = itemMetadata.addSelectionHandler(event -> {
          if (event.getSelectedItem() == addTabIndex) {
            newRepresentationDescriptiveMetadata();
          }
        });
        addTab.addStyleName("addTab");
        addTab.getElement().setId("representationNewDescriptiveMetadata");
        addTab.getParent().addStyleName("addTabWrapper");

        PermissionClientUtils.bindPermission(newDescriptiveMetadata, aip.getPermissions(),
          RodaConstants.PERMISSION_METHOD_CREATE_DESCRIPTIVE_METADATA_FILE);

        handlers.add(tabHandler);
        handlers.add(addTabHandler);

        descriptiveMetadataSavedButtons = new HashMap<>();
        descriptiveMetadataButtons = new SimplePanel();
        descriptiveMetadataButtons.addStyleName("descriptiveMetadataTabButtons");
        itemMetadata.getTabBar().getElement().getStyle().clearProperty("width");
        itemMetadata.getTabBar().getElement().getParentElement().insertFirst(descriptiveMetadataButtons.getElement());
        itemMetadata.addSelectionHandler(event -> {
          if (descriptiveMetadataSavedButtons.containsKey(event.getSelectedItem())) {
            descriptiveMetadataButtons.setWidget(descriptiveMetadataSavedButtons.get(event.getSelectedItem()));
          } else {
            descriptiveMetadataButtons.clear();
          }
        });

        if (!response.getDescriptiveMetadataInfos().getDescriptiveMetadataInfoList().isEmpty()) {
          newDescriptiveMetadata.setVisible(false);
          itemMetadata.getParent().setVisible(true);

          int index = ConfigurationManager.getInt(0, "ui.browser.metadata.index.representation");
          if (index > 0) {
            if (response.getDescriptiveMetadataInfos().getDescriptiveMetadataInfoList().size() > index) {
              itemMetadata.selectTab(index);
            } else {
              itemMetadata.selectTab(0);
            }
          } else {
            int count = response.getDescriptiveMetadataInfos().getDescriptiveMetadataInfoList().size()
              - Math.abs(index);
            if (response.getDescriptiveMetadataInfos().getDescriptiveMetadataInfoList().size() > count) {
              itemMetadata.selectTab(count);
            } else {
              itemMetadata.selectTab(0);
            }
          }
        } else {
          newDescriptiveMetadata.setVisible(true);
          itemMetadata.getParent().setVisible(false);
        }

        // DISSEMINATIONS (POST-INIT)
        disseminationsSearch.getParent().setVisible(dipCounterResponse.getResult() > 0
          && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_DIP));

        // CSS
        this.addStyleName("browse");
        this.addStyleName("browse-representation");
        this.addStyleName(state.toString().toLowerCase());
        newDescriptiveMetadata.getElement().setId("representationNewDescriptiveMetadata");

        Element firstElement = this.getElement().getFirstChildElement();
        if ("input".equalsIgnoreCase(firstElement.getTagName())) {
          firstElement.setAttribute("title", "browse input");
        }

        WCAGUtilities.getInstance().makeAccessible(itemMetadata.getElement());

      });
  }

  private static void getAndRefresh(String aipId, String id, AsyncCallback<Widget> callback) {
    container = new SimplePanel();
    refresh(aipId, id, new AsyncCallback<BrowseRepresentationResponse>() {
      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(BrowseRepresentationResponse result) {
        callback.onSuccess(container);
      }
    });
  }

  private static void refresh(String aipId, String representationId,
    AsyncCallback<BrowseRepresentationResponse> callback) {
    Services services = new Services("Retrieve Representation", "get");
    CompletableFuture<List<IndexedAIP>> ancestorsCompletableFuture = services.aipResource(s -> s.getAncestors(aipId));
    CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services
      .aipResource(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()));
    CompletableFuture<IndexedRepresentation> indexedRepresentationCompletableFuture = services.representationResource(
      s -> s.retrieveIndexedRepresentationViaRequest(new IndexedRepresentationRequest(aipId, representationId)));
    CompletableFuture<DescriptiveMetadataInfos> descriptiveMetadataInfosCompletableFuture = services
      .aipResource(s -> s.retrieveRepresentationDescriptiveMetadata(aipId, representationId,
        LocaleInfo.getCurrentLocale().getLocaleName()));
    CompletableFuture<List<String>> riRulesCompletableFuture = services
      .representationResource(RepresentationRestService::retrieveRepresentationRuleProperties);

    CompletableFuture<Void> allFutures = CompletableFuture
      .allOf(ancestorsCompletableFuture, indexedAIPCompletableFuture, indexedRepresentationCompletableFuture,
        descriptiveMetadataInfosCompletableFuture, riRulesCompletableFuture)
      .exceptionally(throwable -> {
        if (throwable.getCause() instanceof AuthorizationDeniedException) {
          Toast.showError(messages.authorizationDeniedAlert(), "");
          HistoryUtils.newHistory(BrowseTop.RESOLVER);
        } else {
          Toast.showError(throwable.getClass().getSimpleName(), throwable.getMessage());
          HistoryUtils.newHistory(BrowseTop.RESOLVER);
        }
        return null;
      });

    allFutures.thenRun(() -> {
      BrowseRepresentationResponse response = new BrowseRepresentationResponse();
      response.setAncestors(ancestorsCompletableFuture.join());
      response.setIndexedAIP(indexedAIPCompletableFuture.join());
      response.setIndexedRepresentation(indexedRepresentationCompletableFuture.join());
      response.setDescriptiveMetadataInfos(descriptiveMetadataInfosCompletableFuture.join());
      response.setRiRules(riRulesCompletableFuture.join());
      response.setServices(services);

      container.setWidget(new BrowseRepresentation(response));
      callback.onSuccess(response);
    });

  }

  private void updateLayout(final BrowseRepresentationResponse response, long riskIncidenceCounter,
    long preservationCounter, final AIPState state, final boolean justActive) {
    // STATUS
    aipState.setHTML(HtmlSnippetUtils.getAIPStateHTML(state));
    aipState.setVisible(!justActive);

    // IDENTIFICATION
    representationIcon.setHTML(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), false));
    Sliders.createRepresentationInfoSlider(center, navigationToolbar.getInfoSidebarButton(), response);

    risksEventsLogs.clear();

    if (riskIncidenceCounter >= 0) {
      Anchor risksLink = new Anchor(messages.aipRiskIncidences(riskIncidenceCounter), HistoryUtils
        .createHistoryHashLink(RiskIncidenceRegister.RESOLVER, representation.getAipId(), representation.getId()));
      risksEventsLogs.add(risksLink);
    }

    if (preservationCounter >= 0) {
      Anchor eventsLink = new Anchor(messages.aipEvents(preservationCounter), HistoryUtils.createHistoryHashLink(
        PreservationEvents.BROWSE_RESOLVER, representation.getAipId(), representation.getUUID()));

      if (riskIncidenceCounter >= 0) {
        risksEventsLogs.add(new Label(" " + messages.and() + " "));
      }

      risksEventsLogs.add(eventsLink);
    }

    if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())
      && representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      dateCreatedAndModified.setText(messages.dateCreatedAndUpdated(Humanize.formatDate(representation.getCreatedOn()),
        representation.getCreatedBy(), Humanize.formatDate(representation.getUpdatedOn()),
        representation.getUpdatedBy()));
    } else if (representation.getCreatedOn() != null && StringUtils.isNotBlank(representation.getCreatedBy())) {
      dateCreatedAndModified.setText(
        messages.dateCreated(Humanize.formatDateTime(representation.getCreatedOn()), representation.getCreatedBy()));
    } else if (representation.getUpdatedOn() != null && StringUtils.isNotBlank(representation.getUpdatedBy())) {
      dateCreatedAndModified.setText(
        messages.dateUpdated(Humanize.formatDateTime(representation.getUpdatedOn()), representation.getUpdatedBy()));
    } else {
      dateCreatedAndModified.setText("");
    }

    String title = representation.getTitle() != null ? representation.getTitle() : representation.getType();
    title = title == null ? representation.getId() : title;
    representationTitle.clear();
    HtmlSnippetUtils.getRepresentationTypeHTML(representationTitle, title, representation.getRepresentationStates());

    navigationToolbar.updateBreadcrumb(response.getAncestors(), response.getIndexedAIP(),
      response.getIndexedRepresentation());
  }

  private void getDescriptiveMetadataHTML(final String descId, final DescriptiveMetadataInfo bundle,
    final Integer selectedIndex, final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createRepresentationDescriptiveMetadataHTMLUri(aipId, repId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.isHasHistory() && PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_RETRIEVE_DESCRIPTIVE_METADATA_VERSIONS_BUNDLE)) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            if (PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_UPDATE_DESCRIPTIVE_METADATA_FILE)) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
                descId);
              String editLinkHtml = "<a href='" + editLink
                + "' class='toolbarLink' id='representationEditDescriptiveMetadata'><i class='fa fa-edit'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));
            }

            // Download link
            SafeUri downloadUri = RestUtils.createRepresentationDescriptiveMetadataDownloadUri(aipId, repId, descId);
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
              RodaConstants.PERMISSION_METHOD_RETRIEVE_DESCRIPTIVE_METADATA_VERSIONS_BUNDLE)) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            if (PermissionClientUtils.hasPermissions(aip.getPermissions(),
              RodaConstants.PERMISSION_METHOD_UPDATE_DESCRIPTIVE_METADATA_FILE)) {
              String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
                descId);
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

  private void newRepresentationDescriptiveMetadata() {
    HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_REPRESENTATION, aipId, repId);
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataEventsHandler(ClickEvent e) {
    newRepresentationDescriptiveMetadata();
  }

  interface MyUiBinder extends UiBinder<Widget, BrowseRepresentation> {
  }
}
