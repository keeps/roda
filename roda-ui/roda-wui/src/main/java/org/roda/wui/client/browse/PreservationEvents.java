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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadataInfos;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.NavigationToolbarLegacy;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.search.PreservationEventsSearch;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class PreservationEvents extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final List<String> aipFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));
  public static final HistoryResolver PLANNING_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().planningResolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "events";
    }
  };
  private static final List<String> representationFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID,
      RodaConstants.REPRESENTATION_TYPE));
  private static final List<String> fileFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_PARENT_UUID, RodaConstants.FILE_PATH,
      RodaConstants.FILE_ANCESTORS_PATH, RodaConstants.FILE_ORIGINALNAME, RodaConstants.INDEX_ID,
      RodaConstants.FILE_AIP_ID, RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_ISDIRECTORY));
  private static PreservationEvents instance = null;
  public static final HistoryResolver BROWSE_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().browseResolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {BrowseTop.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "events";
    }
  };
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField(provided = true)
  PreservationEventsSearch eventsSearch;
  @UiField
  FlowPanel pageDescription;
  @UiField
  NavigationToolbarLegacy navigationToolbar;
  private String aipId;
  private String representationUUID;
  private String fileUUID;

  public PreservationEvents() {
    this(null);
  }

  public PreservationEvents(final String aipId) {
    this(aipId, null);
  }

  public PreservationEvents(final String aipId, final String representationUUID) {
    this(aipId, representationUUID, null);
  }

  public PreservationEvents(final String aipId, final String representationUUID, final String fileUUID) {
    this.aipId = aipId;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;

    eventsSearch = new PreservationEventsSearch("PreservationEvents_events", aipId, representationUUID, fileUUID);

    initWidget(uiBinder.createAndBindUi(this));

    // NAVIGATION TOOLBAR
    if (fileUUID != null || representationUUID != null || aipId != null) {
      navigationToolbar.withoutButtons();
      if (fileUUID != null) {
        setupFileToolbar();
      } else if (representationUUID != null) {
        setupRepresentationToolbar();
      } else {
        setupAipToolbar();
      }
    }

    pageDescription.add(new HTMLWidgetWrapper("PreservationEventsDescription.html"));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static PreservationEvents getInstance() {
    if (instance == null) {
      instance = new PreservationEvents();
    }
    return instance;
  }

  private void setupAipToolbar() {
    Services service = new Services("Create AIP breadcrumb", "get");
    service
      .rodaEntityRestService(s -> s.findByUuid(aipId, LocaleInfo.getCurrentLocale().getLocaleName()), IndexedAIP.class)
      .whenComplete((aip, error) -> { // get aip
        // ancestors
        if (error != null) {
          if (error instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationEvent());
            HistoryUtils.newHistory(ListUtils.concat(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(error);
          }
        } else {
          CompletableFuture<List<IndexedAIP>> futureAncestors = service.aipResource(s -> s.getAncestors(aipId));

          CompletableFuture<List<String>> futureRepFields = service
            .aipResource(AIPRestService::retrieveAIPRuleProperties);

          CompletableFuture<DescriptiveMetadataInfos> futureDescriptiveMetadataInfos = service
            .aipResource(s -> s.getDescriptiveMetadata(aipId, LocaleInfo.getCurrentLocale().getLocaleName()));

          CompletableFuture<LongResponse> futureChildAipCount = service.rodaEntityRestService(
            s -> s.count(new FindRequest.FindRequestBuilder(
              new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, aipId)), false).build()),
            IndexedAIP.class);

          CompletableFuture<LongResponse> futureDipCount = service.rodaEntityRestService(
            s -> s
              .count(new CountRequest(new Filter(new SimpleFilterParameter(RodaConstants.DIP_AIP_IDS, aipId)), false)),
            IndexedDIP.class);

          CompletableFuture<LongResponse> futureIncidenceCount = service.rodaEntityRestService(
            s -> s.count(new CountRequest(
              new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipId)), false)),
            RiskIncidence.class);

          CompletableFuture<LongResponse> futureEventCount = service.rodaEntityRestService(
            s -> s.count(new CountRequest(
              new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId)), false)),
            IndexedPreservationEvent.class);

          CompletableFuture<LongResponse> futureLogCount = service.rodaEntityRestService(
            s -> s.count(new CountRequest(
              new Filter(new SimpleFilterParameter(RodaConstants.LOG_RELATED_OBJECT_ID, aipId)), false)),
            LogEntry.class);

          CompletableFuture.allOf(futureChildAipCount, futureDipCount, futureAncestors, futureAncestors,
            futureRepFields, futureDescriptiveMetadataInfos, futureIncidenceCount, futureEventCount, futureLogCount)
            .thenApply(v -> {
              BrowseAIPResponse rp = new BrowseAIPResponse();
              rp.setIndexedAIP(aip);
              rp.setAncestors(futureAncestors.join());
              rp.setRepresentationInformationFields(futureRepFields.join());
              rp.setDescriptiveMetadataInfos(futureDescriptiveMetadataInfos.join());
              rp.setChildAipsCount(futureChildAipCount.join());
              rp.setDipCount(futureDipCount.join());
              return rp;
            }).whenComplete((value, throwable) -> {

              if (throwable == null) {
                navigationToolbar.updateBreadcrumb(value.getIndexedAIP(), value.getAncestors());
                navigationToolbar.build();
                navigationToolbar.setVisible(true);
              }
            });
        }
      });

  }

  private void setupRepresentationToolbar() {
    Services services = new Services("Build navigation toolbar", "get");
    services
      .representationResource(s -> s.findByUuid(representationUUID, LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((indexedRepresentation, throwable) -> {
        navigationToolbar.withObject(indexedRepresentation);

        CompletableFuture<List<IndexedAIP>> getAncestorsFuture = services
          .aipResource(s -> s.getAncestors(indexedRepresentation.getAipId()));
        CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services.aipResource(
          s -> s.findByUuid(indexedRepresentation.getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(getAncestorsFuture, indexedAIPCompletableFuture);

        allFutures.thenRun(() -> {
          // All futures completed
          navigationToolbar.updateBreadcrumb(getAncestorsFuture.join(), indexedAIPCompletableFuture.join(),
            indexedRepresentation);
          navigationToolbar.build();
          navigationToolbar.setVisible(true);
        });
      });
  }

  private void setupFileToolbar() {
    Services services = new Services("Build navigation toolbar", "get");
    services.fileResource(s -> s.findByUuid(fileUUID, LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((indexedFile, throwable) -> {
        navigationToolbar.withObject(indexedFile);

        CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services
          .aipResource(s -> s.findByUuid(indexedFile.getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()));
        CompletableFuture<IndexedRepresentation> indexedRepresentationCompletableFuture = services
          .representationResource(
            s -> s.findByUuid(indexedFile.getRepresentationUUID(), LocaleInfo.getCurrentLocale().getLocaleName()));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(indexedAIPCompletableFuture,
          indexedRepresentationCompletableFuture);

        allFutures.thenRun(() -> {
          navigationToolbar.updateBreadcrumb(indexedAIPCompletableFuture.join(),
            indexedRepresentationCompletableFuture.join(), indexedFile);
          navigationToolbar.build();
          navigationToolbar.setVisible(true);
        });
      });
  }

  private void browseResolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      final String aipId = historyTokens.get(0);
      if (aipId.equals(this.aipId) && StringUtils.isBlank(this.representationUUID)) {
        callback.onSuccess(this);
      } else {
        instance = new PreservationEvents(aipId);
        callback.onSuccess(instance);
      }
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(ShowPreservationEvent.RESOLVER.getHistoryToken())) {
      ShowPreservationEvent.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.size() == 2) {
      final String aipId = historyTokens.get(0);
      final String representationUUID = historyTokens.get(1);

      if (aipId.equals(this.aipId) && representationUUID.equals(this.representationUUID)
        && StringUtils.isBlank(this.fileUUID)) {
        callback.onSuccess(this);
      } else {
        instance = new PreservationEvents(aipId, representationUUID);
        callback.onSuccess(instance);
      }
    } else if (historyTokens.size() == 3) {
      final String aipId = historyTokens.get(0);
      final String representationUUID = historyTokens.get(1);
      final String fileUUID = historyTokens.get(2);

      if (aipId.equals(this.aipId) && representationUUID.equals(this.representationUUID)
        && fileUUID.equals(this.fileUUID)) {
        callback.onSuccess(this);
      } else {
        instance = new PreservationEvents(aipId, representationUUID, fileUUID);
        callback.onSuccess(instance);
      }
    } else {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
      callback.onSuccess(null);
    }
  }

  private void planningResolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty() && StringUtils.isBlank(this.aipId)) {
      callback.onSuccess(this);
    } else {
      instance = new PreservationEvents();
      HistoryUtils.newHistory(PLANNING_RESOLVER);
      callback.onSuccess(null);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, PreservationEvents> {
  }

}
