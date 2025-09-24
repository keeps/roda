/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.portal;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.DipFilePreview;
import org.roda.wui.client.browse.DipUrlPreview;
import org.roda.wui.client.browse.Viewers;
import org.roda.wui.client.browse.bundle.BrowseDipBundle;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.IndexedDIPUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
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
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseDIPPortal extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public String getHistoryToken() {
      return "dip";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveViewersProperties(new AsyncCallback<Viewers>() {
        @Override
        public void onFailure(Throwable throwable) {
          errorRedirect(callback);
        }

        @Override
        public void onSuccess(Viewers viewers) {
          load(viewers, historyTokens, callback);
        }
      });
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseTop.RESOLVER);
      callback.onSuccess(null);
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        final String historyDipUUID = historyTokens.get(0);
        final String historyDipFileUUID = historyTokens.size() > 1 ? historyTokens.get(1) : null;
        BrowserService.Util.getInstance().retrieveDipBundle(historyDipUUID, historyDipFileUUID,
          LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<BrowseDipBundle>() {
            @Override
            public void onFailure(Throwable throwable) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            }

            @Override
            public void onSuccess(BrowseDipBundle browseDipBundle) {
              BrowserService.Util.getInstance().showDIPEmbedded(new AsyncCallback<Boolean>() {
                @Override
                public void onFailure(Throwable throwable) {
                  AsyncCallbackUtils.defaultFailureTreatment(throwable);
                }

                @Override
                public void onSuccess(Boolean showEmbedded) {
                  IndexedDIP dip = browseDipBundle.getDip();
                  if (StringUtils.isNotBlank(dip.getOpenExternalURL()) && !showEmbedded) {
                    String url = IndexedDIPUtils.interpolateOpenExternalURL(dip,
                      LocaleInfo.getCurrentLocale().getLocaleName());
                    Window.open(url, "_blank", "");
                    Toast.showInfo(messages.browseFileDipOpenedExternalURL(), url);
                    History.back();
                  } else {
                    callback.onSuccess(new BrowseDIPPortal(viewers, browseDipBundle));
                  }

                }
              });
            }
          });
      } else {
        errorRedirect(callback);
      }
    }

  };

  interface MyUiBinder extends UiBinder<Widget, BrowseDIPPortal> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static ClientMessages messages = GWT.create(ClientMessages.class);

  // IDENTIFICATION
  @UiField
  TitlePanel title;

  // DESCRIPTIVE METADATA
  @UiField
  HTML descriptiveMetadata;

  @UiField
  FlowPanel preMetadata;

  // DIP
  @UiField
  AccessibleFocusPanel keyboardFocus;

  @UiField
  FlowPanel center;

  @UiField
  FlowPanel container;

  public BrowseDIPPortal(Viewers viewers, BrowseDipBundle bundle) {
    IndexedDIP dip = bundle.getDip();
    String aipId = dip.getFileIds().get(0).getAipId();
    String dipTitle = dip.getTitle();
    DIPFile dipFile = bundle.getDipFile();
    if (aipId == null) {
      Toast.showError(messages.notFoundError());
      return;
    }

    initWidget(uiBinder.createAndBindUi(this));

    preMetadata.add(new HTMLWidgetWrapper("PreMetadataPortal.html"));
    preMetadata.addStyleName("preSectionTitle preMetadataTitle");
    title.setText(dipTitle != null ? dipTitle : aipId);

    if (dipFile != null) {
      center.add(new DipFilePreview(viewers, dipFile));
    } else if (dip.getOpenExternalURL() != null) {
      center.add(new DipUrlPreview(viewers, dip));
    } else {
      final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()),
        new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));

      ListBuilder<DIPFile> dipFileListBuilder = new ListBuilder<>(DIPFileList::new,
        new AsyncTableCellOptions<>(DIPFile.class, "BrowseDIPPortal_dipFiles").withFilter(filter)
          .withSummary(messages.allOfAObject(DIPFile.class.getName())).bindOpener()
          .withActionable(DisseminationFileActions.get(dip.getPermissions())));

      SearchWrapper search = new SearchWrapper(false).createListAndSearchPanel(dipFileListBuilder);

      SimplePanel layout = new SimplePanel();
      layout.add(search);
      center.add(layout);
      layout.addStyleName("browseDip-topList");
    }

    // NAVIGATION DIP TOOLBAR
    Filter dipsFilter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_ALL_AIP_UUIDS, aipId));
    Sorter dipsSorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));
    getAIPassociatedDIPs(dipsFilter, dipsSorter, new AsyncCallback<List<IndexedDIP>>() {
      @Override
      public void onFailure(Throwable throwable) {
        // toolbar with just one DIP
        addNavigationToolBarDIPIteration(dipFile != null ? dipFile : dip, null, bundle, false);
      }

      @Override
      public void onSuccess(List<IndexedDIP> indexedDIPS) {
        // toolbar with several dips
        int currentIdx = -1;
        for (int i = 0; i < indexedDIPS.size(); i++) {
          if (indexedDIPS.get(i).getId().equals(dip.getId())) {
            currentIdx = i;
            break;
          }
        }
        if (currentIdx == -1) {
          Toast.showError(messages.browseFileDipEmpty());
          return;
        }
        ListSelectionUtils.save(ListSelectionUtils.create(dip, dipsFilter, true, Facets.NONE, dipsSorter, currentIdx,
          (long) indexedDIPS.size()));
        ListSelectionUtils.ProcessRelativeItem<IsIndexed> processor = nextDIP -> {
          HistoryUtils.newHistory(BrowseDIPPortal.RESOLVER, nextDIP.getUUID());
        };
        addNavigationToolBarDIPIteration(dip, processor, bundle, true);
      }
    });

    // DESCRIPTIVE METADATA
    updateSectionDescriptiveMetadata(aipId);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.smoothScroll(keyboardFocus.getElement());
  }

  private void updateSectionDescriptiveMetadata(String aipId) {
    getDescriptiveMetadataHTML(aipId, new AsyncCallback<SafeHtml>() {

      @Override
      public void onFailure(Throwable caught) {
        if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
          Toast.showError(messages.errorLoadingDescriptiveMetadata(caught.getMessage()));
        }
      }

      @Override
      public void onSuccess(SafeHtml result) {
        descriptiveMetadata.setHTML(result);
      }
    });

    WCAGUtilities.getInstance().makeAccessible(descriptiveMetadata.getElement());
  }

  private void getDescriptiveMetadataHTML(final String aipId, final AsyncCallback<SafeHtml> callback) {
    try {
      SafeUri uri = RestUtils.createDescriptiveMetadataHTMLUri(aipId, null);
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());

      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            b.append(SafeHtmlUtils.fromSafeConstant("<div style=\"clear: both\"/>"));
            callback.onSuccess(b.toSafeHtml());
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

  private static void getAIPassociatedDIPs(Filter dipsFilter, Sorter dipsSorter,
    AsyncCallback<List<IndexedDIP>> callback) {
    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), dipsFilter, dipsSorter, new Sublist(),
      Facets.NONE, LocaleInfo.getCurrentLocale().getLocaleName(), true,
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.DIP_ID), new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable throwable) {
          callback.onFailure(throwable);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          callback.onSuccess(result.getResults());

        }
      });
  }

  private void addNavigationToolBarDIPIteration(IsIndexed obj,
    ListSelectionUtils.ProcessRelativeItem<IsIndexed> processor, BrowseDipBundle bundle, boolean hasProcessor) {
    NavigationToolbar<IsIndexed> navigationToolbar = new NavigationToolbar<>();
    navigationToolbar.withObject(obj);
    if (hasProcessor) {
      navigationToolbar.withProcessor(processor);
    }

    if (obj instanceof IndexedDIP) {
      navigationToolbar.withPermissions(((IndexedDIP) obj).getPermissions());
    }
    navigationToolbar.updateBreadcrumb(bundle);
    navigationToolbar.setHeader(messages.catalogueDIPTitle());
    navigationToolbar.build();
    container.insert(navigationToolbar, 0);
  }

}
