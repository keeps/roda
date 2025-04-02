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

import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.tabs.BrowseAIPTabs;
import org.roda.wui.client.common.*;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipSearchWrapperActions;
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
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

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
  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  private BrowseAIP(BrowseAIPResponse response) {
    aip = response.getIndexedAIP();
    aipId = aip.getId();
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    AipSearchWrapperActions aipActions = AipSearchWrapperActions.get(aip.getId(), aip.getState(), aip.getPermissions());

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

    if (justActive) {
      initHandlers();
    }

    // TABS
    browseTab.init(response, handler);

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

    // CSS
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
      objectToolbar.setObjectAndBuild(aip, aip.getPermissions(), handler);
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

  private void initHandlers() {
    handlers.put(Actionable.ActionImpact.DESTROYED, () -> {
      if (StringUtils.isNotBlank(aip.getParentID())) {
        HistoryUtils.newHistory(BrowseTop.RESOLVER, aip.getParentID());
      } else {
        HistoryUtils.newHistory(BrowseTop.RESOLVER);
      }
    });
    handlers.put(Actionable.ActionImpact.UPDATED, () -> refresh(aipId, new NoAsyncCallback<>()));
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

  private void updateSectionIdentification(BrowseAIPResponse response) {

    title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));
    title.setText(aip.getTitle() != null ? aip.getTitle() : aip.getId());
    Sliders.createAipInfoSlider(center, navigationToolbar.getInfoSidebarButton(), response);

    navigationToolbar.updateBreadcrumb(aip, response.getAncestors());
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
