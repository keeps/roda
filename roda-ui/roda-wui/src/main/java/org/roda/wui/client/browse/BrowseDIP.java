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
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
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
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.github.nmorel.gwtjackson.client.exception.JsonDeserializationException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

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

  public static final int DEFAULT_DIPFILE_INDEX = 0;

  public static final Sorter DEFAULT_DIPFILE_SORTER = new Sorter(new SortParameter(RodaConstants.DIPFILE_ID, false));

  // system
  private final Viewers viewers;
  private final DipBundle bundle;

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
  FocusPanel keyboardFocus;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel center;

  @UiField
  FocusPanel previousButton, nextButton, downloadButton;

  // state
  Sorter sorter;
  int index;
  int totalCount = -1;

  private List<DIPFile> dipFileAncestors;

  /**
   * Create a new panel to view a representation
   * 
   * @param viewers
   * @param index
   * @param aipId
   * @param itemBundle
   * @param representationUUID
   * @param fileUUID
   * @param file
   * 
   */
  public BrowseDIP(Viewers viewers, DipBundle bundle, Sorter sorter, int index) {
    this.viewers = viewers;
    this.bundle = bundle;

    this.aip = bundle.getAip();
    this.representation = bundle.getRepresentation();
    this.file = bundle.getFile();

    this.dip = bundle.getDip();

    this.dipFile = bundle.getDipFile();
    this.dipFileAncestors = bundle.getDipFileAncestors();

    this.sorter = sorter;
    this.index = index;

    initWidget(uiBinder.createAndBindUi(this));

    // TODO set title for previous and next button
    downloadButton.setTitle(messages.viewRepresentationDownloadFileButton());

    HtmlSnippetUtils.setCssClassDisabled(previousButton, true);
    HtmlSnippetUtils.setCssClassDisabled(nextButton, true);
    downloadButton.setVisible(false);

    show();

    keyboardFocus.setFocus(true);
    keyboardFocus.addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (dipFile != null && !dipFile.isDirectory()) {
          NativeEvent ne = event.getNativeEvent();
          if (ne.getKeyCode() == KeyCodes.KEY_RIGHT) {
            ne.preventDefault();
            next();
          } else if (ne.getKeyCode() == KeyCodes.KEY_LEFT) {
            ne.preventDefault();
            previous();
          }
        }
      }
    });

  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.scrollToHeader();
  }

  private void update() {
    center.clear();
    if (dipFile != null) {
      center.add(new DipFilePreview(viewers, dipFile, aip, representation, file));
    } else {
      final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()),
        new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));
      // TODO summary
      final SearchPanel dipFileSearch = new SearchPanel(filter, RodaConstants.DIPFILE_SEARCH,
        messages.searchPlaceHolder(), false, false, true);

      final DIPFileList dipFileList = new DIPFileList(filter, Facets.NONE, "", false);
      dipFileSearch.setList(dipFileList);
      dipFileSearch.setDefaultFilter(filter);
      dipFileSearch.setDefaultFilterIncremental(true);
      dipFileList.getSelectionModel().addSelectionChangeHandler(new Handler() {

        @Override
        public void onSelectionChange(SelectionChangeEvent event) {
          DIPFile selectedDipFile = dipFileList.getSelectionModel().getSelectedObject();
          int selectedDipFileIndex = dipFileList.getIndexOfVisibleObject(selectedDipFile);
          if (selectedDipFile != null) {
            // TODO infer referer object
            HistoryUtils.openBrowse(selectedDipFile, dipFileList.getSorter(), selectedDipFileIndex, aip, representation,
              file);
          }
        }
      });
      center.add(dipFileSearch);
      center.add(dipFileList);
    }

    // update breadcrumb
    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);
  }

  private void show() {
    if (dipFile != null) {

      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()));

      if (dipFile.getAncestorsPath().isEmpty()) {
        filter.add(new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));
      } else {
        String parentId = dipFile.getAncestorsPath().get(dipFile.getAncestorsPath().size() - 1);
        filter.add(new SimpleFilterParameter(RodaConstants.DIPFILE_PARENT_UUID, parentId));
      }

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
    } else {
      totalCount = 0;
      update();
      updateVisibles();
    }

  }

  public void open(String parentDipDileUUID, final Sorter sorter, final int openIndex) {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIPFILE_DIP_ID, dip.getId()));

    if (parentDipDileUUID == null) {
      filter.add(new EmptyKeyFilterParameter(RodaConstants.DIPFILE_PARENT_UUID));
    } else {
      filter.add(new SimpleFilterParameter(RodaConstants.DIPFILE_PARENT_UUID, parentDipDileUUID));
    }

    Sublist sublist = new Sublist(openIndex, 1);
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
            HistoryUtils.openBrowse(firstDipFile, sorter, openIndex, aip, representation, file);
          } else {
            Toast.showError("No files in the DIP");
            // TODO better handle this case
          }
        }
      });

  }

  protected void updateVisibles() {
    if (index < 0) {
      HtmlSnippetUtils.setCssClassDisabled(previousButton, true);
      HtmlSnippetUtils.setCssClassDisabled(nextButton, totalCount < 2);
    } else {
      HtmlSnippetUtils.setCssClassDisabled(previousButton, index == 0);
      HtmlSnippetUtils.setCssClassDisabled(nextButton, index >= totalCount - 1);
      downloadButton.setVisible(dipFile != null && !dipFile.isDirectory());
    }
  }

  @UiHandler("previousButton")
  void previousButtonHandler(ClickEvent e) {
    previous();
  }

  private void previous() {
    if (index > 0) {
      String parentUUID = dipFile != null && !dipFile.getAncestorsPath().isEmpty()
        ? dipFile.getAncestorsPath().get(dipFile.getAncestorsPath().size() - 1) : null;
      open(parentUUID, sorter, index - 1);
    }
  }

  private void next() {
    if (index < totalCount - 1) {
      String parentUUID = dipFile != null && !dipFile.getAncestorsPath().isEmpty()
        ? dipFile.getAncestorsPath().get(dipFile.getAncestorsPath().size() - 1) : null;
      open(parentUUID, sorter, index + 1);
    }
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
    return BreadcrumbUtils.getDipBreadcrumbs(aip, representation, file, dip, dipFile, dipFileAncestors);
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
        Sorter historyDipFileSorter = DEFAULT_DIPFILE_SORTER;
        int historyDipFileIndex = DEFAULT_DIPFILE_INDEX;
        try {
          if (historyTokens.size() > 3) {
            historyDipFileSorter = HistoryUtils.SORTER_MAPPER.read(historyTokens.get(2));
            historyDipFileIndex = Integer.valueOf(historyTokens.get(3));
          }

        } catch (NumberFormatException | JsonDeserializationException e) {
          // do nothing
          GWT.log("Could not parse sorter or index from history", e);
        }

        final Sorter loadSorter = historyDipFileSorter;
        final int loadIndex = historyDipFileIndex;

        IsIndexed lastObject = LastSelectedItemsSingleton.getInstance().getLastObject();

        String aipId = null;
        String representationId = null;
        List<String> filePath = null;
        String fileId = null;

        if (lastObject == null) {
          // infer from DIP
        } else if (lastObject instanceof IndexedAIP) {
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
              callback.onSuccess(new BrowseDIP(viewers, dipBundle, loadSorter, loadIndex));
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
