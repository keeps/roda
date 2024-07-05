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
import java.util.Collections;
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
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.DisposalPolicyUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationPanel;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.client.services.Services;
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
public class BrowseAIP extends Composite {

  private static final List<String> fieldsToReturn = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static SimplePanel container;
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

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
  HTML aipState;
  // IDENTIFICATION
  @UiField
  TitlePanel title;
  @UiField
  TabPanel descriptiveMetadata;
  @UiField
  Button newDescriptiveMetadata;

  // HEADER
  // REPRESENTATIONS
  @UiField
  SimplePanel addRepresentation;

  // STATUS
  @UiField
  SimplePanel representationsCard;
  // DISSEMINATIONS
  @UiField
  SimplePanel disseminationsCard;

  // DESCRIPTIVE METADATA
  // AIP CHILDREN
  @UiField
  SimplePanel aipChildrenCard;
  @UiField
  SimplePanel addChildAip;
  @UiField
  FlowPanel risksEventsLogs;
  @UiField
  FlowPanel disposalPolicy;
  @UiField
  FlowPanel center;
  @UiField
  Label dateCreatedAndModified;
  private String aipId;
  private IndexedAIP aip;
  private SimplePanel descriptiveMetadataButtons;
  private Map<Integer, HTMLPanel> descriptiveMetadataSavedButtons;

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

    // REPRESENTATIONS
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
      ListBuilder<IndexedRepresentation> representationsListBuilder;
      if (aip.getState().equals(AIPState.DESTROYED) || aip.isOnHold() || aip.getDisposalConfirmationId() != null) {
        representationsListBuilder = new ListBuilder<>(ConfigurableAsyncTableCell::new,
          new AsyncTableCellOptions<>(IndexedRepresentation.class, "BrowseAIP_representations")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId())))
            .withJustActive(justActive).withSummary(messages.listOfRepresentations()).bindOpener());
      } else {
        representationsListBuilder = new ListBuilder<>(ConfigurableAsyncTableCell::new,
          new AsyncTableCellOptions<>(IndexedRepresentation.class, "BrowseAIP_representations")
            .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId())))
            .withJustActive(justActive).withSummary(messages.listOfRepresentations()).bindOpener()
            .withActionable(representationActions).withActionableCallback(listActionableCallback));
      }

      SearchWrapper representationsSearchWrapper = new SearchWrapper(false)
        .createListAndSearchPanel(representationsListBuilder);
      representationsCard.setWidget(representationsSearchWrapper);
      representationsCard.setVisible(response.getRepresentationCount().getResult() > 0);
    } else {
      representationsCard.setVisible(false);
    }

    // DISSEMINATIONS

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_DIP)) {
      ListBuilder<IndexedDIP> disseminationsListBuilder = new ListBuilder<>(DIPList::new,
        new AsyncTableCellOptions<>(IndexedDIP.class, "BrowseAIP_disseminations")
          .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_UUIDS, aip.getId())))
          .withJustActive(justActive).withSummary(messages.listOfDisseminations()).bindOpener()
          .withActionable(disseminationActions).withActionableCallback(listActionableCallback));

      SearchWrapper disseminationsSearchWrapper = new SearchWrapper(false)
        .createListAndSearchPanel(disseminationsListBuilder);
      disseminationsCard.setWidget(disseminationsSearchWrapper);
      disseminationsCard.setVisible(response.getDipCount().getResult() > 0);
    } else {
      disseminationsCard.setVisible(false);
    }

    // AIP CHILDREN
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
    addStyleName("browse browse_aip");

    // make FocusPanel comply with WCAG
    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    // STATE
    this.addStyleName(aip.getState().toString().toLowerCase());
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

    // IDENTIFICATION
    updateSectionIdentification(response);
    // DISPOSAL
    updateDisposalInformation();

    // DESCRIPTIVE METADATA
    updateSectionDescriptiveMetadata(response.getDescriptiveMetadataInfos());

    // REPRESENTATIONS
    if (response.getRepresentationCount().getResult() == 0 && aip.getState().equals(AIPState.ACTIVE) && !aip.isOnHold()
      && aip.getDisposalConfirmationId() == null) {
      addRepresentation.setWidget(new ActionableWidgetBuilder<>(representationActions).buildListWithObjects(
        new ActionableObject<>(IndexedRepresentation.class),
        Collections.singletonList(RepresentationActions.RepresentationAction.NEW)));
    }

    addRepresentation
      .setVisible(response.getRepresentationCount().getResult() == 0 && aip.getState().equals(AIPState.ACTIVE));

    // AIP CHILDREN
    if (aip.getState().equals(AIPState.ACTIVE)) {
      if (response.getChildAipsCount().getResult() > 0) {
        LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
      } else {
        if (!aip.isOnHold() && aip.getDisposalConfirmationId() == null) {
          addChildAip.setWidget(
            new ActionableWidgetBuilder<>(aipActions).buildListWithObjects(new ActionableObject<>(IndexedAIP.class),
              Collections.singletonList(AipActions.AipAction.NEW_CHILD_AIP_BELOW)));
        }
      }

      addChildAip.setVisible(response.getRepresentationCount().getResult() == 0);
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
        descriptiveMetadata.add(container, title);
        descriptiveMetadataContainers.add(Pair.of(descMetadatum.getId(), container));
        metadataInfos.put(descMetadatum.getId(), descMetadatum);
      }
    }

    descriptiveMetadata.addSelectionHandler(event -> {
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
      final int addTabIndex = descriptiveMetadata.getWidgetCount();
      FlowPanel addTab = new FlowPanel();
      addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
      descriptiveMetadata.add(new Label(), addTab);
      descriptiveMetadata.addSelectionHandler(event -> {
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
    descriptiveMetadata.getTabBar().getElement().getStyle().clearProperty("width");
    descriptiveMetadata.getTabBar().getElement().getParentElement()
      .insertFirst(descriptiveMetadataButtons.getElement());
    descriptiveMetadata.addSelectionHandler(event -> {
      if (descriptiveMetadataSavedButtons.containsKey(event.getSelectedItem())) {
        descriptiveMetadataButtons.setWidget(descriptiveMetadataSavedButtons.get(event.getSelectedItem()));
      } else {
        descriptiveMetadataButtons.clear();
      }
    });

    if (descMetadata != null && !descMetadata.isEmpty()) {
      descriptiveMetadata.getParent().setVisible(true);
      newDescriptiveMetadata.setVisible(false);

      int index = ConfigurationManager.getInt(0, "ui.browser.metadata.index.aip");
      if (index > 0) {
        if (descMetadata.size() > index) {
          descriptiveMetadata.selectTab(index);
        } else {
          descriptiveMetadata.selectTab(0);
        }
      } else {
        int count = descMetadata.size() - Math.abs(index);
        if (descMetadata.size() > count) {
          descriptiveMetadata.selectTab(count);
        } else {
          descriptiveMetadata.selectTab(0);
        }
      }
    } else {
      descriptiveMetadata.getParent().setVisible(false);
      newDescriptiveMetadata.setVisible(true);
    }

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
  }

  private void updateSectionIdentification(BrowseAIPResponse response) {

    title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
    title.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
    Sliders.createAipInfoSlider(center, navigationToolbar.getInfoSidebarButton(), response);

    risksEventsLogs.clear();

    if (response.getIncidenceCount().getResult() >= 0) {
      Anchor risksLink = new Anchor(messages.aipRiskIncidences(response.getIncidenceCount().getResult()),
        HistoryUtils.createHistoryHashLink(RiskIncidenceRegister.RESOLVER, aip.getId()));
      risksEventsLogs.add(risksLink);
    }

    if (response.getEventCount().getResult() >= 0) {
      Anchor eventsLink = new Anchor(messages.aipEvents(response.getEventCount().getResult()),
        HistoryUtils.createHistoryHashLink(PreservationEvents.BROWSE_RESOLVER, aip.getId()));

      if (response.getIncidenceCount().getResult() >= 0) {
        if (response.getLogCount().getResult() >= 0) {
          risksEventsLogs.add(new Label(", "));
        } else {
          risksEventsLogs.add(new Label(" " + messages.and() + " "));
        }
      }

      risksEventsLogs.add(eventsLink);
    }

    if (response.getLogCount().getResult() >= 0) {
      Anchor logsLink = new Anchor(messages.aipLogs(response.getLogCount().getResult()),
        HistoryUtils.createHistoryHashLink(UserLog.RESOLVER, aip.getId()));

      if (response.getIncidenceCount().getResult() >= 0 || response.getEventCount().getResult() >= 0) {
        risksEventsLogs.add(new Label(" " + messages.and() + " "));
      }

      risksEventsLogs.add(logsLink);
    }

    navigationToolbar.updateBreadcrumb(aip, response.getAncestors());

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

  private void updateDisposalInformation() {
    if (DisposalPolicyUtils.showDisposalPolicySummary(aip)) {
      Anchor disposalPolicyLink = new Anchor(DisposalPolicyUtils.getDisposalPolicySummarySafeHTML(aip),
        HistoryUtils.createHistoryHashLink(DisposalPolicyAssociationPanel.RESOLVER, aip.getId()));
      disposalPolicy.add(disposalPolicyLink);
    } else {
      disposalPolicy.setVisible(false);
    }
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

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataHandler(ClickEvent e) {
    newDescriptiveMetadataRedirect();
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
