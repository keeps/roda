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

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseDIP extends Composite {

  interface MyUiBinder extends UiBinder<Widget, BrowseDIP> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DIP dip;
  private DIPFile dipFile;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  DipFilePreview dipFilePreview;

  @UiField
  FocusPanel downloadButton;

  @UiField
  FocusPanel removeButton;

  /**
   * Create a new panel to view a representation
   * 
   * @param viewers
   * @param aipId
   * @param itemBundle
   * @param representationUUID
   * @param fileUUID
   * @param file
   * 
   */
  public BrowseDIP(Viewers viewers, DIP dip, DIPFile dipFile) {
    this.dip = dip;
    this.dipFile = dipFile;

    // IndexedRepresentation rep = null;
    // for (IndexedRepresentation irep : itemBundle.getRepresentations()) {
    // if (irep.getUUID().equals(representationUUID)) {
    // rep = irep;
    // break;
    // }
    // }

    dipFilePreview = new DipFilePreview(viewers, dipFile);

    initWidget(uiBinder.createAndBindUi(this));

    // update breadcrumb
    // breadcrumb.updatePath(getBreadcrumbs());
    // breadcrumb.setVisible(true);

    downloadButton.setTitle(messages.viewRepresentationDownloadFileButton());
    removeButton.setTitle(messages.viewRepresentationRemoveFileButton());

    // update visibles
    downloadButton.setVisible(!dipFile.isDirectory());
    removeButton.setVisible(!dipFile.isDirectory());
  }

  @UiHandler("downloadButton")
  void buttonDownloadFileButtonHandler(ClickEvent e) {
    download();
  }

  private void download() {
    SafeUri downloadUri = null;
    if (dipFile != null) {
      downloadUri = RestUtils.createDipFileDownloadUri(dipFile.getUUID());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("removeButton")
  void buttonRemoveFileButtonHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
      messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            // BrowserService.Util.getInstance().deleteFile(file.getUUID(), new
            // AsyncCallback<Void>() {
            //
            // @Override
            // public void onSuccess(Void result) {
            // // clean();
            // }
            //
            // @Override
            // public void onFailure(Throwable caught) {
            // AsyncCallbackUtils.defaultFailureTreatment(caught);
            // }
            // });
            // TODO remove DIP
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }
      });
  }

  // TODO breadcrumbs
  // private List<BreadcrumbItem> getBreadcrumbs() {
  // return BreadcrumbUtils.getFileBreadcrumbs(itemBundle, aipId,
  // representationUUID, file);
  // }

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveViewersProperties(new AsyncCallback<Viewers>() {

        @Override
        public void onSuccess(Viewers viewers) {
          load(viewers, historyTokens, callback);
        }

        @Override
        public void onFailure(Throwable caught) {
          errorRedirect(callback);
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "dip";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        final String historyDipId = historyTokens.get(0);
        final String historyDipFileId = historyTokens.size() > 1 ? historyTokens.get(1) : null;

        BrowserService.Util.getInstance().retrieve(IndexedDIP.class.getName(), historyDipId,
          new AsyncCallback<IndexedDIP>() {

            @Override
            public void onFailure(Throwable caught) {
              errorRedirect(callback);
            }

            @Override
            public void onSuccess(final IndexedDIP dip) {
              if (historyDipFileId != null) {
                BrowserService.Util.getInstance().retrieve(DIPFile.class.getName(), historyDipFileId,
                  new AsyncCallback<DIPFile>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      errorRedirect(callback);
                    }

                    @Override
                    public void onSuccess(DIPFile dipFile) {
                      BrowseDIP view = new BrowseDIP(viewers, dip, dipFile);
                      callback.onSuccess(view);
                    }
                  });

              } else {
                showFirst(viewers, callback, dip);

              }

            }

          });

      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(Browse.RESOLVER);
      callback.onSuccess(null);
    }
  };

  protected static void showFirst(final Viewers viewers, final AsyncCallback<Widget> callback, final IndexedDIP dip) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_DIP_ID, dip.getId()));
    Sublist sublist = new Sublist(0, 1);
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(DIPFile.class.getName(), filter, Sorter.NONE, sublist, Facets.NONE,
      localeString, justActive, new AsyncCallback<IndexResult<DIPFile>>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(IndexResult<DIPFile> result) {
          if (!result.getResults().isEmpty()) {
            DIPFile firstDipFile = result.getResults().get(0);
            HistoryUtils.newHistory(BrowseDIP.RESOLVER, dip.getUUID(), firstDipFile.getUUID());
            callback.onSuccess(null);
          } else {
            Toast.showError("No files in the DIP");
            // TODO better handle this case
          }

        }
      });
  }

  public static void jumpTo(DIPFile selected) {
    HistoryUtils.newHistory(BrowseDIP.RESOLVER, selected.getUUID());
  }
}
