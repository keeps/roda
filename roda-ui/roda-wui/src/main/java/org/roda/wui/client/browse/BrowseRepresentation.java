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
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.tabs.BrowseRepresentationTabs;
import org.roda.wui.client.common.BrowseRepresentationActionsToolbar;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.cards.RepresentationDisseminationCardList;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.RepresentationRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
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
  FlowPanel center;
  @UiField
  NavigationToolbar<IndexedRepresentation> navigationToolbar;
  @UiField
  BrowseRepresentationActionsToolbar objectToolbar;
  @UiField
  BrowseRepresentationTabs browseTab;

  // Side panel
  @UiField
  FocusPanel sidePanel;
  @UiField
  FlowPanel disseminationCards;

  // STATUS
  @UiField
  HTML representationIcon;

  // DESCRIPTIVE METADATA
  @UiField
  FlowPanel representationTitle;

  private final Map<Actionable.ActionImpact, Runnable> handlers;
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  // FILES
  private final IndexedAIP aip;

  // DISSEMINATIONS
  private final IndexedRepresentation representation;
  private final String aipId;
  private final String repId;
  private final String repUUID;

  public BrowseRepresentation(BrowseRepresentationResponse response) {
    this.representation = response.getIndexedRepresentation();
    this.aip = response.getIndexedAIP();
    this.aipId = aip.getId();
    this.repId = representation.getId();
    this.repUUID = representation.getUUID();

    handlers = new HashMap<>();

    final AIPState state = aip.getState();
    final boolean justActive = AIPState.ACTIVE.equals(state);

    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    if (justActive) {
      initHandlers();
    }

    // NAVIGATION TOOLBAR
    navigationToolbar.withObject(representation);
    navigationToolbar.withPermissions(aip.getPermissions());
    navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId));
    navigationToolbar.withActionImpactHandler(Actionable.ActionImpact.UPDATED,
      () -> refresh(aipId, repId, new NoAsyncCallback<>()));
    navigationToolbar.build();

    Services services = response.getServices();
    Filter dipsFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, repUUID));
    CountRequest dipCountRequest = new CountRequest(new Filter(dipsFilter), true);
    CompletableFuture<LongResponse> dipCounterCompletableFuture = services
      .rodaEntityRestService(s -> s.count(dipCountRequest), IndexedDIP.class).handle((longResponse, throwable1) -> {
        if (throwable1 != null) {
          return new LongResponse(-1L);
        }
        return longResponse;
      });

    CompletableFuture.allOf(dipCounterCompletableFuture).thenRun(() -> {
      LongResponse dipCounterResponse = dipCounterCompletableFuture.join();

      updateLayout(response);

      // CARDS
      if (dipCounterResponse.getResult() > 0) {
        this.disseminationCards.add(new RepresentationDisseminationCardList(aipId, repId));
      } else {
        this.sidePanel.setVisible(false);
      }

      // CSS
      keyboardFocus.addStyleName("browse browse-representation browse_main_panel");
      this.addStyleName(state.toString().toLowerCase());

      Element firstElement = this.getElement().getFirstChildElement();
      if ("input".equalsIgnoreCase(firstElement.getTagName())) {
        firstElement.setAttribute("title", "browse input");
      }
    });

    // OBJECT TOOLBAR
    objectToolbar.setObjectAndBuild(representation, aip.getState(), aip.getPermissions(), handler);

    // TABS
    browseTab.init(response);
  }

  private void initHandlers() {
    handlers.put(Actionable.ActionImpact.DESTROYED, () -> HistoryUtils.newHistory(BrowseTop.RESOLVER, aipId));
    handlers.put(Actionable.ActionImpact.UPDATED, () -> refresh(aipId, repId, new NoAsyncCallback<>()));
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

  private void updateLayout(final BrowseRepresentationResponse response) {
    // IDENTIFICATION
    representationIcon.setHTML(DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), false));

    String title = representation.getTitle() != null ? representation.getTitle() : representation.getType();
    title = title == null ? representation.getId() : title;
    representationTitle.clear();
    HtmlSnippetUtils.getRepresentationTypeHTML(representationTitle, title, representation.getRepresentationStates());

    navigationToolbar.updateBreadcrumb(response.getAncestors(), response.getIndexedAIP(),
      response.getIndexedRepresentation());
  }

  interface MyUiBinder extends UiBinder<Widget, BrowseRepresentation> {
  }
}
