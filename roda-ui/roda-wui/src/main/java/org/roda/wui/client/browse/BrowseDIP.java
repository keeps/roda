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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexedFileRequest;
import org.roda.core.data.v2.index.IndexedRepresentationRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.wui.client.browse.tabs.BrowseDIPTabs;
import org.roda.wui.client.common.BrowseDIPActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.model.BrowseDIPResponse;
import org.roda.wui.client.common.utils.IndexedDIPUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.ConfigurationRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class BrowseDIP extends Composite {

  public static final Sorter DEFAULT_DIPFILE_SORTER = new Sorter(new SortParameter(RodaConstants.DIPFILE_ID, false));
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      Services services = new Services("Retrieve viewers configuration", "get");
      services.configurationsResource(ConfigurationRestService::retrieveViewersProperties)
        .whenComplete((viewers, throwable) -> {
          if (throwable != null) {
            errorRedirect(callback);
          } else {
            load(viewers, historyTokens, callback);
          }
        });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "dip";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        final String historyDipUUID = historyTokens.get(0);
        final String historyDipFileUUID = historyTokens.size() > 1 ? historyTokens.get(1) : null;

        Services services = new Services("Retrieve DIP", "get");

        services.dipResource(s -> s.findByUuid(historyDipUUID, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((indexedDIP, throwable) -> {
            if (throwable != null) {
              if (throwable instanceof NotFoundException) {
                // Toast.showError(messages.notFoundError(),
                // messages.couldNotFindPreservationEvent());
                HistoryUtils.newHistory(BrowseTop.RESOLVER);
              }
            } else {
              if (!indexedDIP.getAipIds().isEmpty()) {
                applyWhenIndexedAIP(services, historyDipUUID, historyDipFileUUID, indexedDIP, viewers, callback);
              } else if (!indexedDIP.getRepresentationIds().isEmpty()) {
                applyWhenIndexedRepresentation(services, historyDipUUID, historyDipFileUUID, indexedDIP, viewers,
                  callback);
              } else if (!indexedDIP.getFileIds().isEmpty()) {
                applyWhenIndexedFile(services, historyDipUUID, historyDipFileUUID, indexedDIP, viewers, callback);
              }
            }
          });
      } else {
        errorRedirect(callback);
      }
    }

    private void applyWhenIndexedAIP(Services services, String historyDipUUID, String historyDipFileUUID,
      IndexedDIP indexedDIP, Viewers viewers, AsyncCallback<Widget> callback) {
      CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services.aipResource(
        s -> s.findByUuid(indexedDIP.getAipIds().get(0).getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()));
      CompletableFuture<Boolean> showEmbeddedDIPFuture = services
        .configurationsResource(ConfigurationRestService::retrieveShowEmbeddedDIP).exceptionally(throwable1 -> false);

      CompletableFuture<IndexResult<DIPFile>> retrieveDIPFileCompletableFuture = buildCompletableFutureRetrieveDIPFile(
        historyDipUUID, historyDipFileUUID, services);

      CompletableFuture.allOf(indexedAIPCompletableFuture, retrieveDIPFileCompletableFuture, showEmbeddedDIPFuture)
        .thenApply(v -> {
          BrowseDIPResponse response = new BrowseDIPResponse();
          response.setEmbeddedDIP(showEmbeddedDIPFuture.join());
          IndexedAIP indexedAIP = indexedAIPCompletableFuture.join();
          IndexResult<DIPFile> dipFileIndexResult = retrieveDIPFileCompletableFuture.join();
          response.setIndexedAIP(indexedAIP);
          response.setPermissions(indexedAIP.getPermissions());
          response.setReferred(indexedAIP);
          response.setDip(indexedDIP);

          if (historyDipFileUUID != null) {
            response.setDipFile(dipFileIndexResult.getResults().get(0));
          } else {
            if (dipFileIndexResult.getTotalCount() == 1 && !dipFileIndexResult.getResults().get(0).isDirectory()) {
              response.setDipFile(dipFileIndexResult.getResults().get(0));
            }
          }
          return response;
        }).whenComplete((response, throwable1) -> {
          if (response.getDipFile() != null) {
            List<CompletableFuture<DIPFile>> dipFileAncestors = response.getDipFile().getAncestorsUUIDs().stream()
              .map(m -> services.dipFileResource(s -> s.findByUuid(m, LocaleInfo.getCurrentLocale().getLocaleName())))
              .collect(Collectors.toList());
            CompletableFuture<?>[] futuresArray = dipFileAncestors.toArray(new CompletableFuture<?>[0]);
            CompletableFuture.allOf(futuresArray).thenApply(v -> {
              for (CompletableFuture<DIPFile> dipFileAncestor : dipFileAncestors) {
                DIPFile file = dipFileAncestor.join();
                response.getDipFileAncestors().add(file);
              }
              return response;
            }).whenComplete((o, throwable) -> render(o, viewers, callback, services));
          } else {
            render(response, viewers, callback, services);
          }
        });
    }

    private void render(BrowseDIPResponse response, Viewers viewers, AsyncCallback<Widget> callback,
      Services services) {
      if (StringUtils.isNotBlank(response.getDip().getOpenExternalURL()) && !response.isEmbeddedDIP()) {
        String url = IndexedDIPUtils.interpolateOpenExternalURL(response.getDip(),
          LocaleInfo.getCurrentLocale().getLocaleName());
        Window.open(url, "_blank", "");
        Toast.showInfo(messages.browseFileDipOpenedExternalURL(), url);
        History.back();
      } else {
        callback.onSuccess(new BrowseDIP(viewers, response, services));
      }
    }

    private void applyWhenIndexedRepresentation(Services services, String historyDipUUID, String historyDipFileUUID,
      IndexedDIP indexedDIP, Viewers viewers, AsyncCallback<Widget> callback) {
      RepresentationLink representationLink = indexedDIP.getRepresentationIds().get(0);
      IndexedRepresentationRequest request = new IndexedRepresentationRequest();
      request.setAipId(representationLink.getAipId());
      request.setRepresentationId(representationLink.getRepresentationId());

      CompletableFuture<IndexedRepresentation> indexedRepresentationCompletableFuture = services
        .representationResource(s -> s.retrieveIndexedRepresentationViaRequest(request));
      CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services
        .aipResource(s -> s.findByUuid(request.getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()));
      CompletableFuture<Boolean> showEmbeddedDIPFuture = services
        .configurationsResource(ConfigurationRestService::retrieveShowEmbeddedDIP).exceptionally(throwable1 -> false);

      CompletableFuture<IndexResult<DIPFile>> retrieveDIPFileCompletableFuture = buildCompletableFutureRetrieveDIPFile(
        historyDipUUID, historyDipFileUUID, services);

      CompletableFuture.allOf(indexedAIPCompletableFuture, retrieveDIPFileCompletableFuture,
        indexedRepresentationCompletableFuture, showEmbeddedDIPFuture).thenApply(v -> {
          BrowseDIPResponse response = new BrowseDIPResponse();
          response.setEmbeddedDIP(showEmbeddedDIPFuture.join());
          IndexedAIP indexedAIP = indexedAIPCompletableFuture.join();
          IndexedRepresentation indexedRepresentation = indexedRepresentationCompletableFuture.join();
          IndexResult<DIPFile> dipFileIndexResult = retrieveDIPFileCompletableFuture.join();

          response.setIndexedAIP(indexedAIP);
          response.setPermissions(indexedAIP.getPermissions());
          response.setIndexedRepresentation(indexedRepresentation);
          response.setReferred(indexedRepresentation);
          response.setDip(indexedDIP);

          if (historyDipFileUUID != null) {
            response.setDipFile(dipFileIndexResult.getResults().get(0));
          } else {
            if (dipFileIndexResult.getTotalCount() == 1 && !dipFileIndexResult.getResults().get(0).isDirectory()) {
              response.setDipFile(dipFileIndexResult.getResults().get(0));
            }
          }

          return response;

        }).whenComplete((response, throwable1) -> {
          if (response.getDipFile() != null) {
            List<CompletableFuture<DIPFile>> dipFileAncestors = response.getDipFile().getAncestorsUUIDs().stream()
              .map(m -> services.dipFileResource(s -> s.findByUuid(m, LocaleInfo.getCurrentLocale().getLocaleName())))
              .collect(Collectors.toList());
            CompletableFuture<?>[] futuresArray = dipFileAncestors.toArray(new CompletableFuture<?>[0]);
            CompletableFuture.allOf(futuresArray).thenApply(v -> {
              for (CompletableFuture<DIPFile> dipFileAncestor : dipFileAncestors) {
                DIPFile file = dipFileAncestor.join();
                response.getDipFileAncestors().add(file);
              }
              return response;
            }).whenComplete((o, throwable) -> render(o, viewers, callback, services));
          } else {
            render(response, viewers, callback, services);
          }
        });
    }

    private void applyWhenIndexedFile(Services services, String historyDipUUID, String historyDipFileUUID,
      IndexedDIP indexedDIP, Viewers viewers, AsyncCallback<Widget> callback) {
      FileLink fileLink = indexedDIP.getFileIds().get(0);
      IndexedFileRequest request = new IndexedFileRequest();
      request.setAipId(fileLink.getAipId());
      request.setDirectoryPaths(fileLink.getPath());
      request.setFileId(fileLink.getFileId());
      request.setRepresentationId(fileLink.getRepresentationId());

      CompletableFuture<IndexedFile> indexedFileCompletableFuture = services
        .fileResource(s -> s.retrieveIndexedFileViaRequest(request));
      CompletableFuture<IndexedAIP> indexedAIPCompletableFuture = services
        .aipResource(s -> s.findByUuid(request.getAipId(), LocaleInfo.getCurrentLocale().getLocaleName()));
      CompletableFuture<Boolean> showEmbeddedDIPFuture = services
        .configurationsResource(ConfigurationRestService::retrieveShowEmbeddedDIP).exceptionally(throwable1 -> false);

      CompletableFuture<IndexResult<DIPFile>> retrieveDIPFileCompletableFuture = buildCompletableFutureRetrieveDIPFile(
        historyDipUUID, historyDipFileUUID, services);

      CompletableFuture.allOf(indexedAIPCompletableFuture, retrieveDIPFileCompletableFuture,
        indexedFileCompletableFuture, showEmbeddedDIPFuture).thenApply(v -> {
          BrowseDIPResponse response = new BrowseDIPResponse();
          IndexedAIP indexedAIP = indexedAIPCompletableFuture.join();
          IndexedFile indexedFile = indexedFileCompletableFuture.join();
          IndexResult<DIPFile> dipFileIndexResult = retrieveDIPFileCompletableFuture.join();

          response.setIndexedAIP(indexedAIP);
          response.setPermissions(indexedAIP.getPermissions());
          response.setIndexedFile(indexedFile);
          response.setReferred(indexedFile);
          response.setDip(indexedDIP);

          if (historyDipFileUUID != null) {
            response.setDipFile(dipFileIndexResult.getResults().get(0));
          } else {
            if (dipFileIndexResult.getTotalCount() == 1 && !dipFileIndexResult.getResults().get(0).isDirectory()) {
              response.setDipFile(dipFileIndexResult.getResults().get(0));
            }
          }
          return response;
        }).whenComplete((response, throwable1) -> {
          if (response.getDipFile() != null) {
            List<CompletableFuture<DIPFile>> dipFileAncestors = response.getDipFile().getAncestorsUUIDs().stream()
              .map(m -> services.dipFileResource(s -> s.findByUuid(m, LocaleInfo.getCurrentLocale().getLocaleName())))
              .collect(Collectors.toList());
            CompletableFuture<?>[] futuresArray = dipFileAncestors.toArray(new CompletableFuture<?>[0]);
            CompletableFuture.allOf(futuresArray).thenApply(v -> {
              for (CompletableFuture<DIPFile> dipFileAncestor : dipFileAncestors) {
                DIPFile file = dipFileAncestor.join();
                response.getDipFileAncestors().add(file);
              }
              return response;
            }).whenComplete((o, throwable) -> render(o, viewers, callback, services));
          } else {
            render(response, viewers, callback, services);
          }
        });
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
      callback.onSuccess(null);
    }

  };
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  AccessibleFocusPanel keyboardFocus;

  // interface

  @UiField
  FlowPanel container;
  @UiField
  NavigationToolbar navigationToolbar;
  @UiField
  BrowseDIPActionsToolbar objectToolbar;
  @UiField
  BrowseDIPTabs browseTab;

  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  public BrowseDIP(Viewers viewers, BrowseDIPResponse response, Services services) {
    // target
    IndexedDIP dip = response.getDip();
    DIPFile dipFile = response.getDipFile();

    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(dipFile != null ? dipFile : dip);

    handlers.put(Actionable.ActionImpact.DESTROYED, () -> {
      if (dipFile == null) {
        // dip was removed
        if (!dip.getFileIds().isEmpty()) {
          FileLink link = dip.getFileIds().get(0);
          HistoryUtils.openBrowse(link.getAipId(), link.getRepresentationId(), link.getPath(), link.getFileId());
        } else if (!dip.getRepresentationIds().isEmpty()) {
          RepresentationLink link = dip.getRepresentationIds().get(0);
          HistoryUtils.openBrowse(link.getAipId(), link.getRepresentationId());
        } else if (!dip.getAipIds().isEmpty()) {
          AIPLink link = dip.getAipIds().get(0);
          HistoryUtils.openBrowse(link.getAipId());
        }
      }
    });

    navigationToolbar.withPermissions(dip.getPermissions());
    navigationToolbar.updateBreadcrumb(response);
    navigationToolbar.build();
    container.insert(navigationToolbar, 0);

    if (response.getReferred() instanceof IndexedAIP || response.getReferred() instanceof IndexedRepresentation
      || response.getReferred() instanceof IndexedFile) {
      navigationToolbar.withAlternativeStyle(true);
    }

    objectToolbar.setObjectAndBuild(dip, dip.getPermissions(), handler);

    browseTab.init(viewers, response, handler);

    keyboardFocus.setFocus(true);
  }

  private static CompletableFuture<IndexResult<DIPFile>> buildCompletableFutureRetrieveDIPFile(String historyDipUUID,
    String historyDipFileUUID, Services services) {
    if (historyDipFileUUID != null) {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, historyDipFileUUID));
      Sublist sublist = new Sublist(0, 1);
      FindRequest findRequest = FindRequest.getBuilder(filter, false).withSublist(sublist).build();
      return services.dipFileResource(s -> s.find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName()));
    } else {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, historyDipUUID));
      Sublist sublist = new Sublist(0, 1);
      FindRequest findRequest = FindRequest.getBuilder(filter, false).withSublist(sublist).build();
      return services.dipFileResource(s -> s.find(findRequest, LocaleInfo.getCurrentLocale().getLocaleName()));
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.smoothScroll(keyboardFocus.getElement());
  }

  interface MyUiBinder extends UiBinder<Widget, BrowseDIP> {
  }
}
