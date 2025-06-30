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
import java.util.stream.Collectors;

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
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.tabs.BrowseAIPTabs;
import org.roda.wui.client.common.BrowseAIPActionsToolbar;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
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
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
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
    aipChildrenTitle.setIcon("ma ma-account-tree");
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
            .withActionBlacklist(List.of(AipSearchWrapperActions.AipSearchWrapperAction.NEW_CHILD_AIP_TOP,
              AipSearchWrapperActions.AipSearchWrapperAction.APPRAISAL_ACCEPT,
              AipSearchWrapperActions.AipSearchWrapperAction.APPRAISAL_REJECT))
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
    objectToolbar.setObjectAndBuild(aip, aip.getState(), aip.getPermissions(), handler);

    // IDENTIFICATION
    updateSectionIdentification(response);

    // AIP CHILDREN
    if (aip.getState().equals(AIPState.ACTIVE) && response.getChildAipsCount().getResult() > 0) {
      LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
    }

    lowerContent.setVisible(response.getChildAipsCount().getResult() > 0);

    // Side panel representations
    // Check if user has permissions to see the representations
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_REPRESENTATION)) {
      boolean showSidePanel = false;
      if (Boolean.TRUE.equals(response.getIndexedAIP().getHasRepresentations())) {
        showSidePanel = true;
        this.representationCards.add(new AIPRepresentationCardList(aipId, justActive));
      }

      if (response.getDipCount().getResult() > 0) {
        showSidePanel = true;
        this.disseminationCards.add(new AIPDisseminationCardList(aipId));
      }

      this.sidePanel.setVisible(showSidePanel);
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
            Toast.showError(messages.notFoundError());
            HistoryUtils.newHistory(BrowseTop.RESOLVER);
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(error);
          }
        } else {
          CompletableFuture<List<IndexedAIP>> futureAncestors = service.aipResource(s -> s.getAncestors(id));

          CompletableFuture<List<String>> futureRepFields = service
            .aipResource(AIPRestService::retrieveAIPRuleProperties);

          CompletableFuture<DescriptiveMetadataInfos> futureDescriptiveMetadataInfos = service
            .aipResource(s -> s.getDescriptiveMetadata(id, LocaleInfo.getCurrentLocale().getLocaleName()))
            .exceptionally(throwable -> new DescriptiveMetadataInfos());

          CompletableFuture<LongResponse> futureChildAipCount = service
            .rodaEntityRestService(
              s -> s.count(new FindRequest.FindRequestBuilder(
                new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, id)), false).build()),
              IndexedAIP.class);

          CompletableFuture<LongResponse> futureDipCount = service.rodaEntityRestService(
            s -> s.count(new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.DIP_ALL_AIP_UUIDS, id)), false)),
            IndexedDIP.class);

          List<String> jobIds = new ArrayList<>();
          if (StringUtils.isNotBlank(aip.getIngestJobId())) {
            jobIds.add(aip.getIngestJobId());
          }
          jobIds.addAll(aip.getIngestUpdateJobIds());

          List<CompletableFuture<Job>> futures = jobIds.isEmpty() ? new ArrayList<>() : jobIds.stream()
            .map(jobId -> service.jobsResource(s -> s.getJobFromModel(jobId))).collect(Collectors.toList());

          CompletableFuture<List<Job>> futureIngestJobs = futures.isEmpty()
            ? CompletableFuture.completedFuture(new ArrayList<>())
            : CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream().map(CompletableFuture::join) // join each individual Job future here
              .collect(Collectors.toList()));

          CompletableFuture.allOf(futureChildAipCount, futureDipCount, futureAncestors, futureRepFields,
            futureDescriptiveMetadataInfos, futureIngestJobs).thenApply(v -> {
              BrowseAIPResponse rp = new BrowseAIPResponse();
              rp.setIndexedAIP(aip);
              rp.setAncestors(futureAncestors.join());
              rp.setRepresentationInformationFields(futureRepFields.join());
              rp.setDescriptiveMetadataInfos(futureDescriptiveMetadataInfos.join());
              rp.setChildAipsCount(futureChildAipCount.join());
              rp.setDipCount(futureDipCount.join());
              rp.setIngestJobs(futureIngestJobs.join());

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
