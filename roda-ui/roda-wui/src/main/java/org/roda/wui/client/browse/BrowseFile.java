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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.browse.bundle.BrowseItemBundle;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseFile extends Composite {

  interface MyUiBinder extends UiBinder<Widget, BrowseFile> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private String aipId;
  private BrowseItemBundle itemBundle;
  private String representationUUID;
  private String fileUUID;
  private IndexedFile file;

  private boolean infoPanelOpen = false;
  private boolean disseminationsPanelOpen = false;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  IndexedFilePreview filePreview;

  @UiField
  FocusPanel downloadFileButton;

  @UiField
  FocusPanel removeFileButton;

  @UiField
  FocusPanel infoFileButton, disseminationsButton;

  @UiField
  FlowPanel infoFilePanel, dipFilePanel;

  @UiField
  FocusPanel downloadDocumentationButton;

  @UiField
  FocusPanel downloadSchemasButton;

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
  public BrowseFile(Viewers viewers, String aipId, BrowseItemBundle itemBundle, String representationUUID,
    String fileUUID, IndexedFile file) {
    this.aipId = aipId;
    this.itemBundle = itemBundle;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;
    this.file = file;

    // find representation (needed for later)
    IndexedRepresentation rep = null;
    for (IndexedRepresentation irep : itemBundle.getRepresentations()) {
      if (irep.getUUID().equals(representationUUID)) {
        rep = irep;
        break;
      }
    }

    // initialize preview
    filePreview = new IndexedFilePreview(viewers, file, new Command() {

      @Override
      public void execute() {
        Scheduler.get().scheduleDeferred(new Command() {
          @Override
          public void execute() {
            toggleDisseminationsPanel();
          }
        });
      }
    });

    // initialize widget
    initWidget(uiBinder.createAndBindUi(this));

    // breadcrumb
    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);

    // set title
    downloadFileButton.setTitle(messages.viewRepresentationDownloadFileButton());
    removeFileButton.setTitle(messages.viewRepresentationRemoveFileButton());
    infoFileButton.setTitle(messages.viewRepresentationInfoFileButton());

    // update visibles
    downloadFileButton.setVisible(!file.isDirectory());
    removeFileButton.setVisible(!file.isDirectory());
    infoFileButton.setVisible(!file.isDirectory());
    downloadDocumentationButton.setVisible(rep.getNumberOfDocumentationFiles() > 0);
    downloadSchemasButton.setVisible(rep.getNumberOfSchemaFiles() > 0);

  }

  @UiHandler("downloadFileButton")
  void buttonDownloadFileButtonHandler(ClickEvent e) {
    downloadFile();
  }

  private void downloadFile() {
    SafeUri downloadUri = null;
    if (file != null) {
      downloadUri = RestUtils.createRepresentationFileDownloadUri(file.getUUID());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("downloadDocumentationButton")
  void buttonDownloadDocumentationButtonHandler(ClickEvent e) {
    SafeUri downloadUri = null;
    downloadUri = RestUtils.createRepresentationPartDownloadUri(representationUUID,
      RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
    Window.Location.assign(downloadUri.asString());
  }

  @UiHandler("downloadSchemasButton")
  void buttonDownloadSchemasButtonHandler(ClickEvent e) {
    SafeUri downloadUri = null;
    downloadUri = RestUtils.createRepresentationPartDownloadUri(representationUUID,
      RodaConstants.STORAGE_DIRECTORY_SCHEMAS);
    Window.Location.assign(downloadUri.asString());
  }

  @UiHandler("removeFileButton")
  void buttonRemoveFileButtonHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
      messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().deleteFile(file.getUUID(), details, new AsyncCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                      // clean();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      AsyncCallbackUtils.defaultFailureTreatment(caught);
                    }
                  });
                }
              });
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }
      });
  }

  @UiHandler("infoFileButton")
  void buttonInfoFileButtonHandler(ClickEvent e) {
    toggleInfoPanel();
  }

  @UiHandler("disseminationsButton")
  void buttonDisseminationsButtonHandler(ClickEvent e) {
    toggleDisseminationsPanel();
  }

  private void toggleInfoPanel() {
    infoPanelOpen = !infoPanelOpen;

    updateInfoPanel();
  }

  private void updateInfoPanel() {
    if (infoPanelOpen) {
      infoFileButton.addStyleName("active");
      updateInfoFile();

      if (disseminationsPanelOpen) {
        toggleDisseminationsPanel();
      }
    } else {
      infoFileButton.removeStyleName("active");
    }

    JavascriptUtils.toggleRightPanel(".infoFilePanel");
  }

  private void toggleDisseminationsPanel() {
    disseminationsPanelOpen = !disseminationsPanelOpen;
    updateDisseminationPanel();
  }

  private void updateDisseminationPanel() {
    if (disseminationsPanelOpen) {
      disseminationsButton.addStyleName("active");
      updateDisseminations();

      if (infoPanelOpen) {
        toggleInfoPanel();
      }

    } else {
      disseminationsButton.removeStyleName("active");
    }

    JavascriptUtils.toggleRightPanel(".dipFilePanel");
  }

  private List<BreadcrumbItem> getBreadcrumbs() {
    return BreadcrumbUtils.getFileBreadcrumbs(itemBundle, aipId, representationUUID, file);
  }

  public void updateInfoFile() {
    HashMap<String, SafeHtml> values = new HashMap<String, SafeHtml>();
    infoFilePanel.clear();

    if (file != null) {
      String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();
      values.put(messages.viewRepresentationInfoFilename(), SafeHtmlUtils.fromString(fileName));

      values.put(messages.viewRepresentationInfoSize(),
        SafeHtmlUtils.fromString(Humanize.readableFileSize(file.getSize())));

      if (file.getFileFormat() != null) {
        FileFormat fileFormat = file.getFileFormat();

        if (StringUtils.isNotBlank(fileFormat.getMimeType())) {
          values.put(messages.viewRepresentationInfoMimetype(), SafeHtmlUtils.fromString(fileFormat.getMimeType()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationName())) {
          values.put(messages.viewRepresentationInfoFormat(),
            SafeHtmlUtils.fromString(fileFormat.getFormatDesignationName()));
        }

        if (StringUtils.isNotBlank(fileFormat.getFormatDesignationVersion())) {
          values.put(messages.viewRepresentationInfoFormatVersion(),
            SafeHtmlUtils.fromString(fileFormat.getFormatDesignationVersion()));
        }

        if (StringUtils.isNotBlank(fileFormat.getPronom())) {
          values.put(messages.viewRepresentationInfoPronom(), SafeHtmlUtils.fromString(fileFormat.getPronom()));
        }

      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationName())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationName(),
          SafeHtmlUtils.fromString(file.getCreatingApplicationName()));
      }

      if (StringUtils.isNotBlank(file.getCreatingApplicationVersion())) {
        values.put(messages.viewRepresentationInfoCreatingApplicationVersion(),
          SafeHtmlUtils.fromString(file.getCreatingApplicationVersion()));
      }

      if (StringUtils.isNotBlank(file.getDateCreatedByApplication())) {
        values.put(messages.viewRepresentationInfoDateCreatedByApplication(),
          SafeHtmlUtils.fromString(file.getDateCreatedByApplication()));
      }

      if (file.getHash() != null && !file.getHash().isEmpty()) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        boolean first = true;
        for (String hash : file.getHash()) {
          if (first) {
            first = false;
          } else {
            b.append(SafeHtmlUtils.fromSafeConstant("<br/>"));
          }
          b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
          b.append(SafeHtmlUtils.fromString(hash));
          b.append(SafeHtmlUtils.fromSafeConstant("</small>"));
        }
        values.put(messages.viewRepresentationInfoHash(), b.toSafeHtml());
      }

      if (file.getStoragePath() != null) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.append(SafeHtmlUtils.fromSafeConstant("<small>"));
        b.append(SafeHtmlUtils.fromString(file.getStoragePath()));
        b.append(SafeHtmlUtils.fromSafeConstant("</small>"));

        values.put(messages.viewRepresentationInfoStoragePath(), b.toSafeHtml());
      }
    }

    for (String key : values.keySet()) {
      FlowPanel entry = new FlowPanel();

      Label keyLabel = new Label(key);
      HTML valueLabel = new HTML(values.get(key));

      entry.add(keyLabel);
      entry.add(valueLabel);

      infoFilePanel.add(entry);

      keyLabel.addStyleName("infoFileEntryKey");
      valueLabel.addStyleName("infoFileEntryValue");
      entry.addStyleName("infoFileEntry");
    }
  }

  private void updateDisseminations() {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.DIP_FILE_UUIDS, fileUUID));
    Sorter sorter = new Sorter(new SortParameter(RodaConstants.DIP_DATE_CREATED, true));
    Sublist sublist = new Sublist(0, 100);
    Facets facets = Facets.NONE;
    String localeString = LocaleInfo.getCurrentLocale().getLocaleName();
    boolean justActive = true;

    BrowserService.Util.getInstance().find(IndexedDIP.class.getName(), filter, sorter, sublist, facets, localeString,
      justActive, new AsyncCallback<IndexResult<IndexedDIP>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(IndexResult<IndexedDIP> result) {
          updateDisseminations(result.getResults());
        }
      });
  }

  private void updateDisseminations(List<IndexedDIP> dips) {
    dipFilePanel.clear();

    if (dips.isEmpty()) {
      // TODO show message or hide
      dipFilePanel.add(new Label("No entries"));
    } else {
      for (final IndexedDIP dip : dips) {
        createDipPanel(dip);

      }
    }
  }

  private void createDipPanel(final IndexedDIP dip) {
    FlowPanel layout = new FlowPanel();

    // open layout
    FlowPanel leftLayout = new FlowPanel();
    Label titleLabel = new Label(dip.getTitle());
    Label descriptionLabel = new Label(dip.getDescription());

    leftLayout.add(titleLabel);
    leftLayout.add(descriptionLabel);

    FocusPanel openFocus = new FocusPanel(leftLayout);
    layout.add(openFocus);

    // delete
    HTML deleteIcon = new HTML(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-ban'></i>"));
    FocusPanel deleteButton = new FocusPanel(deleteIcon);
    deleteButton.addStyleName("lightbtn");
    deleteIcon.addStyleName("lightbtn-icon");
    // TODO i18n
    deleteButton.setTitle("Delete DIP");

    deleteButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        deleteDIP(dip);
      }
    });

    layout.add(deleteButton);

    dipFilePanel.add(layout);

    titleLabel.addStyleName("dipTitle");
    descriptionLabel.addStyleName("dipDescription");
    layout.addStyleName("dip");
    leftLayout.addStyleName("dip-left");
    openFocus.addStyleName("dip-focus");
    deleteButton.addStyleName("dip-delete");

    openFocus.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (StringUtils.isNotBlank(dip.getOpenExternalURL())) {
          Window.open(dip.getOpenExternalURL(), "_blank", "");
          // TODO i18n
          Toast.showInfo("Opened dissemination", dip.getOpenExternalURL());
        } else {
          HistoryUtils.newHistory(BrowseDIP.RESOLVER, dip.getUUID(), file.getAipId(), file.getRepresentationUUID(),
            file.getUUID());
        }
      }
    });
  }

  protected void deleteDIP(final IndexedDIP dip) {
    // TODO update messages
    Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
      messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIP(dip.getId(), new AsyncCallback<Void>() {

              @Override
              public void onSuccess(Void result) {
                updateDisseminations();
              }

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }
            });

          }
        }

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }
      });
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
      UserLogin.getInstance().checkRole(Browse.RESOLVER, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "file";
    }

    private void load(final Viewers viewers, final List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 2) {
        final String historyAipId = historyTokens.get(0);
        final String historyRepresentationUUID = historyTokens.get(1);
        final String historyFileUUID = historyTokens.get(2);

        BrowserService.Util.getInstance().retrieveItemBundle(historyAipId,
          LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<BrowseItemBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              errorRedirect(callback);
            }

            @Override
            public void onSuccess(final BrowseItemBundle itemBundle) {
              if (itemBundle != null
                && verifyRepresentation(itemBundle.getRepresentations(), historyRepresentationUUID)) {
                BrowserService.Util.getInstance().retrieve(IndexedFile.class.getName(), historyFileUUID,
                  new AsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccess(IndexedFile simpleFile) {
                      BrowseFile view = new BrowseFile(viewers, historyAipId, itemBundle, historyRepresentationUUID,
                        historyFileUUID, simpleFile);
                      callback.onSuccess(view);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
                      errorRedirect(callback);
                    }
                  });
              } else {
                errorRedirect(callback);
              }
            }
          });
      } else {
        errorRedirect(callback);
      }
    }

    private boolean verifyRepresentation(List<IndexedRepresentation> representations, String representationUUID) {
      boolean exist = false;
      for (IndexedRepresentation representation : representations) {
        if (representation.getUUID().equals(representationUUID)) {
          exist = true;
        }
      }
      return exist;
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(Browse.RESOLVER);
      callback.onSuccess(null);
    }
  };

  public static void jumpTo(IndexedFile selected) {
    HistoryUtils.newHistory(BrowseFile.RESOLVER, selected.getAipId(), selected.getRepresentationUUID(),
      selected.getUUID());
  }
}
