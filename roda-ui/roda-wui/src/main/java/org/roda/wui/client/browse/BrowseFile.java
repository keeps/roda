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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.IndexedFileRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.BrowseFileActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.cards.FileDisseminationCardList;
import org.roda.wui.client.common.model.BrowseFileResponse;
import org.roda.wui.client.common.slider.Sliders;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.client.services.FileRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class BrowseFile extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      Services services = new Services("Retrieve viewers configuration", "get");
      services.configurationsResource(ConfigurationRestService::retrieveViewersProperties)
        .whenComplete((viewers, throwable) -> {
          if (throwable != null) {
            Toast.showError(throwable);
            errorRedirect(callback);
          } else {
            load(viewers, historyTokens, callback);
          }
        });
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
      return "file";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 2) {
        final String historyAipId = historyTokens.get(0);
        final String historyRepresentationId = historyTokens.get(1);
        final List<String> historyFilePath = new ArrayList<>(historyTokens.subList(2, historyTokens.size() - 1));
        final String historyFileId = historyTokens.get(historyTokens.size() - 1);

        Services services = new Services("Retrieve File", "get");
        IndexedFileRequest request = new IndexedFileRequest();
        request.setAipId(historyAipId);
        request.setRepresentationId(historyRepresentationId);
        request.setDirectoryPaths(historyFilePath);
        request.setFileId(historyFileId);
        services.fileResource(s -> s.retrieveIndexedFileViaRequest(request)).whenComplete((indexedFile, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            Filter riskIncidenceFilter = new Filter(
              new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, indexedFile.getId()));
            CountRequest riskIncidenceCountRequest = new CountRequest(riskIncidenceFilter, true);
            CompletableFuture<LongResponse> riskCounterCompletableFuture = services
              .rodaEntityRestService(s -> s.count(riskIncidenceCountRequest), RiskIncidence.class)
              .handle((longResponse, throwable1) -> {
                if (throwable1 != null) {
                  return new LongResponse(-1L);
                }
                return longResponse;
              });

            Filter preservationEventFilter = new Filter(
              new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_FILE_UUID, indexedFile.getUUID()));
            CountRequest preservationEventsCountRequest = new CountRequest(preservationEventFilter, true);

            CompletableFuture<LongResponse> preservationCounterCompletableFuture = services
              .rodaEntityRestService(s -> s.count(preservationEventsCountRequest), IndexedPreservationEvent.class)
              .handle((longResponse, throwable2) -> {
                if (throwable2 != null) {
                  return new LongResponse(-1L);
                }
                return longResponse;
              });

            Filter dipsFilter = new Filter(
              new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, indexedFile.getUUID()));
            CountRequest dipsCountRequest = new CountRequest(dipsFilter, true);
            CompletableFuture<LongResponse> dipCounterCompletableFuture = services
              .rodaEntityRestService(s -> s.count(dipsCountRequest), IndexedDIP.class)
              .handle((longResponse, throwable3) -> {
                if (throwable3 != null) {
                  return new LongResponse(-1L);
                }
                return longResponse;
              });

            CompletableFuture<IndexedAIP> retrieveAIPCompletableFuture = services.rodaEntityRestService(
              s -> s.findByUuid(indexedFile.getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()),
              IndexedAIP.class);

            CompletableFuture<IndexedRepresentation> retrieveRepresentationCompletableFuture = services
              .rodaEntityRestService(
                s -> s.findByUuid(indexedFile.getRepresentationUUID(), LocaleInfo.getCurrentLocale().getLocaleName()),
                IndexedRepresentation.class);

            CompletableFuture<List<String>> getRepresentationInformationFields = services
              .fileResource(FileRestService::retrieveFileRuleProperties);

            CompletableFuture.allOf(riskCounterCompletableFuture, preservationCounterCompletableFuture,
              dipCounterCompletableFuture, retrieveAIPCompletableFuture, retrieveRepresentationCompletableFuture)
              .thenApply(unused -> {
                BrowseFileResponse response = new BrowseFileResponse();
                response.setIndexedAIP(retrieveAIPCompletableFuture.join());
                response.setIndexedRepresentation(retrieveRepresentationCompletableFuture.join());
                response.setRiskCounterResponse(riskCounterCompletableFuture.join());
                response.setDipCounterResponse(dipCounterCompletableFuture.join());
                response.setPreservationCounterResponse(preservationCounterCompletableFuture.join());
                response.setRepresentationInformationFields(getRepresentationInformationFields.join());
                return response;
              }).whenComplete((response, caught) -> {
                instance = new BrowseFile(viewers, response, indexedFile, services);
                callback.onSuccess(instance);
              });
          }
        });
      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      callback.onSuccess(null);
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseFile> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static BrowseFile instance = null;

  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  AccessibleFocusPanel keyboardFocus;

  @UiField(provided = true)
  IndexedFilePreview filePreview;

  @UiField
  FlowPanel center;

  @UiField
  NavigationToolbar<IndexedFile> navigationToolbar;

  @UiField
  BrowseFileActionsToolbar objectToolbar;

  @UiField
  FocusPanel sidePanel;

  @UiField
  FlowPanel disseminationCards;

  private final Map<Actionable.ActionImpact, Runnable> handlers;
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };
  private String aipId;
  private String repId;

  public BrowseFile(Viewers viewers, final BrowseFileResponse response, IndexedFile indexedFile, Services services) {
    final boolean justActive = AIPState.ACTIVE.equals(response.getIndexedAIP().getState());
    // initialize preview
    filePreview = new IndexedFilePreview(viewers, indexedFile, indexedFile.isAvailable(), justActive,
      response.getIndexedAIP().getPermissions(), () -> {
      });

    // initialize widget
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(indexedFile).withPermissions(response.getIndexedAIP().getPermissions())
      .withActionImpactHandler(Actionable.ActionImpact.DESTROYED, () -> HistoryUtils
        .newHistory(BrowseRepresentation.RESOLVER, indexedFile.getAipId(), indexedFile.getRepresentationId()))
      .build();
    navigationToolbar.updateBreadcrumb(response.getIndexedAIP(), response.getIndexedRepresentation(), indexedFile);

    handlers = new HashMap<>();

    aipId = indexedFile.getAipId();
    repId = indexedFile.getRepresentationId();
    initHandlers();

    // STATUS
    this.keyboardFocus.addStyleName(response.getIndexedAIP().getState().toString().toLowerCase());

    // TOOLBAR
    this.objectToolbar.setObjectAndBuild(indexedFile, response.getIndexedAIP().getPermissions(), handler);

    // SIDEBAR
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, indexedFile.getUUID()));
    services.rodaEntityRestService(s -> s.count(new CountRequest(filter, justActive)), IndexedDIP.class)
      .whenComplete((longResponse, caught) -> {
        if (caught != null) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        } else {
          if (longResponse.getResult() > 0) {
            this.disseminationCards.add(new FileDisseminationCardList(response.getIndexedAIP().getId(),
              response.getIndexedRepresentation().getId(), indexedFile.getId(), indexedFile.getUUID()));
          } else {
            this.sidePanel.setVisible(false);
          }
        }
      });

    // bind slider buttons
    Sliders.createFileInfoSlider(center, navigationToolbar.getInfoSidebarButton(), indexedFile, response);

    keyboardFocus.setFocus(true);

    this.keyboardFocus.addStyleName("browse browse-file browse_main_panel");

    Element firstElement = this.keyboardFocus.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

    WCAGUtilities.getInstance().makeAccessible(center.getElement());
  }

  private void initHandlers() {
    this.handlers.put(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, repId));
  }
}
