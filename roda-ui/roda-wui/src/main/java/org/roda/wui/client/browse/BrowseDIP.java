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
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.DipBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
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
import com.google.gwt.user.client.ui.FlowPanel;
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

  // system
  private final Viewers viewers;

  // source
  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private IndexedFile file;

  // target
  private IndexedDIP dip;
  private DIPFile dipFile;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  // interface

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel center;

  @UiField
  FocusPanel previousButton, nextButton, downloadButton;

  // state
  int index;
  int totalCount = -1;

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
  public BrowseDIP(Viewers viewers, DipBundle bundle) {
    this.viewers = viewers;

    this.aip = bundle.getAip();
    this.representation = bundle.getRepresentation();
    this.file = bundle.getFile();

    this.dip = bundle.getDip();

    initWidget(uiBinder.createAndBindUi(this));

    // TODO set title for previous and next button
    downloadButton.setTitle(messages.viewRepresentationDownloadFileButton());

    previousButton.setVisible(false);
    nextButton.setVisible(false);

    

    if (bundle.getDipFile() != null) {
      index = -1;
      showFromBundle(bundle.getDipFile());
    } else {
      index = 0;
      show();
    }
  }

  private void update() {
    center.clear();
    if (dipFile != null) {
      center.add(new DipFilePreview(viewers, dipFile));
    } else {
      // TODO prompt to select some file or that are no files
    }

    // update breadcrumb
    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);
  }
  
  private void showFromBundle(DIPFile selected) {
    dipFile = selected;
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()));

    BrowserService.Util.getInstance().count(DIPFile.class.getName(), filter, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Long count) {
        totalCount = count.intValue();
        update();
        updateVisibles();
      }
    });

  }

  public void show() {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()));
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIPFILE_ID, false));
    Sublist sublist = new Sublist(index, 1);
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(DIPFile.class.getName(), filter, sorter, sublist, Facets.NONE, localeString,
      justActive, new AsyncCallback<IndexResult<DIPFile>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<DIPFile> result) {
          if (!result.getResults().isEmpty()) {
            DIPFile firstDipFile = result.getResults().get(0);
            dipFile = firstDipFile;
            totalCount = (int) result.getTotalCount();

            update();
          } else {
            Toast.showError("No files in the DIP");
            // TODO better handle this case
          }

          updateVisibles();

        }
      });

  }

  protected void updateVisibles() {
    previousButton.setVisible(index > 0);
    nextButton.setVisible(index < totalCount - 1);
    downloadButton.setVisible(dipFile != null && !dipFile.isDirectory());
  }

  @UiHandler("previousButton")
  void previousButtonHandler(ClickEvent e) {
    previous();
  }

  private void previous() {
    if (index > 0) {
      index--;
    }
    show();
  }

  private void next() {
    if (index < totalCount - 1) {
      index++;
    }
    show();
  }

  @UiHandler("nextButton")
  void nextButtonHandler(ClickEvent e) {
    next();
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

  private List<BreadcrumbItem> getBreadcrumbs() {
    return BreadcrumbUtils.getDipBreadcrumbs(aip, representation, file, dip, dipFile);
  }

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
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "dip";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (!historyTokens.isEmpty()) {
        final String historyDipUUID = historyTokens.get(0);
        final String historyDipFileUUID = historyTokens.size() > 1 ? historyTokens.get(1) : null;

        IsIndexed lastObject = LastSelectedItemsSingleton.getInstance().getLastObject();

        String aipId = null;
        String representationId = null;
        List<String> filePath = null;
        String fileId = null;

        if (lastObject instanceof IndexedAIP) {
          IndexedAIP lastAIP = (IndexedAIP) lastObject;
          aipId = lastAIP.getId();
        } else if (lastObject instanceof IndexedRepresentation) {
          IndexedRepresentation lastRepresentation = (IndexedRepresentation) lastObject;
          aipId = lastRepresentation.getAipId();
          representationId = lastRepresentation.getId();
        } else if (lastObject instanceof IndexedFile) {
          IndexedFile lastFile = (IndexedFile) lastObject;
          aipId = lastFile.getAipId();
          representationId = lastFile.getRepresentationId();
          filePath = lastFile.getPath();
          fileId = lastFile.getId();
        }

        BrowserService.Util.getInstance().getDipBundle(historyDipUUID, historyDipFileUUID, aipId, representationId,
          filePath, fileId, new AsyncCallback<DipBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(DipBundle dipBundle) {
              callback.onSuccess(new BrowseDIP(viewers, dipBundle));
            }
          });

      } else {
        errorRedirect(callback);
      }
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseAIP.RESOLVER);
      callback.onSuccess(null);
    }

  };
}
