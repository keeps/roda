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
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.search.PreservationEventsSearch;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

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

  private static PreservationEvents instance = null;

  interface MyUiBinder extends UiBinder<Widget, PreservationEvents> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final List<String> aipFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_GHOST, RodaConstants.AIP_TITLE, RodaConstants.AIP_LEVEL));

  private static final List<String> representationFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID, RodaConstants.REPRESENTATION_ID,
      RodaConstants.REPRESENTATION_TYPE));

  private static final List<String> fileFieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_PARENT_UUID, RodaConstants.FILE_PATH,
      RodaConstants.FILE_ANCESTORS_PATH, RodaConstants.FILE_ORIGINALNAME, RodaConstants.INDEX_ID,
      RodaConstants.FILE_AIP_ID, RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_ISDIRECTORY));

  @UiField(provided = true)
  PreservationEventsSearch eventsSearch;

  @UiField
  FlowPanel pageDescription;

  @UiField
  NavigationToolbar navigationToolbar;

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
    BrowserService.Util.getInstance().retrieveBrowseAIPBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
      aipFieldsToReturn, new NoAsyncCallback<BrowseAIPBundle>() {
        @Override
        public void onSuccess(BrowseAIPBundle itemBundle) {
          navigationToolbar.updateBreadcrumb(itemBundle);
          navigationToolbar.build();
          navigationToolbar.setVisible(true);
        }
      });
  }

  private void setupRepresentationToolbar() {
    Services services = new Services("Build navigation toolbar", "get");
    services.representationResource(s -> s.findByUuid(representationUUID, LocaleInfo.getCurrentLocale().getLocaleName())).whenComplete((indexedRepresentation, throwable) -> {
      navigationToolbar.withObject(indexedRepresentation);

      CompletableFuture<List<IndexedAIP>> getAncestorsFuture = services.aipResource(s -> s.getAncestors(indexedRepresentation.getAipId()));
      CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services.aipResource(s -> s.findByUuid(indexedRepresentation.getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()));

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
}
