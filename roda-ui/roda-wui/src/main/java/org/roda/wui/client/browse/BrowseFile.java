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

import org.apache.commons.httpclient.HttpStatus;
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
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
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

  private Viewers viewers;
  private String aipId;
  private BrowseItemBundle itemBundle;
  private String representationUUID;
  @SuppressWarnings("unused")
  private String fileUUID;
  private IndexedFile file;

  private boolean infoPanelOpen = false;
  private boolean disseminationsPanelOpen = false;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  FlowPanel filePreview;

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
    this.viewers = viewers;
    this.aipId = aipId;
    this.itemBundle = itemBundle;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;
    this.file = file;
    IndexedRepresentation rep = null;
    for (IndexedRepresentation irep : itemBundle.getRepresentations()) {
      if (irep.getUUID().equals(representationUUID)) {
        rep = irep;
        break;
      }
    }

    initWidget(uiBinder.createAndBindUi(this));

    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);

    downloadFileButton.setVisible(false);
    removeFileButton.setVisible(false);
    infoFileButton.setVisible(false);

    downloadDocumentationButton.setVisible(rep.getNumberOfDocumentationFiles() > 0);
    downloadSchemasButton.setVisible(rep.getNumberOfSchemaFiles() > 0);

    downloadFileButton.setTitle(messages.viewRepresentationDownloadFileButton());
    removeFileButton.setTitle(messages.viewRepresentationRemoveFileButton());
    infoFileButton.setTitle(messages.viewRepresentationInfoFileButton());

    filePreview.addStyleName("viewRepresentationFilePreview");

    filePreview();
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
            BrowserService.Util.getInstance().deleteFile(file.getUUID(), new AsyncCallback<Void>() {

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

  private void filePreview() {
    filePreview.clear();
    breadcrumb.updatePath(getBreadcrumbs());

    if (file != null && !file.isDirectory()) {
      downloadFileButton.setVisible(true);
      infoFileButton.setVisible(true);

      String type = viewerType(file);
      if (type != null) {
        if (type.equals("image")) {
          imagePreview(file);
        } else if (type.equals("pdf")) {
          pdfPreview(file);
        } else if (type.equals("text")) {
          textPreview(file);
        } else if (type.equals("audio")) {
          audioPreview(file);
        } else if (type.equals("video")) {
          videoPreview(file);
        } else {
          notSupportedPreview();
        }
      } else {
        notSupportedPreview();
      }
    } else {
      emptyPreview();
    }
  }

  private List<BreadcrumbItem> getBreadcrumbs() {
    return BreadcrumbUtils.getFileBreadcrumbs(itemBundle, aipId, representationUUID, file);
  }

  private String viewerType(IndexedFile file) {
    String type = null;
    if (file.getFileFormat() != null) {
      if (file.getFileFormat().getPronom() != null) {
        type = viewers.getPronoms().get(file.getFileFormat().getPronom());
      }

      if (file.getFileFormat().getMimeType() != null && type == null) {
        type = viewers.getMimetypes().get(file.getFileFormat().getMimeType());
      }
    }

    String fileName = file.getOriginalName() != null ? file.getOriginalName() : file.getId();

    if (type == null && fileName.lastIndexOf(".") != -1) {
      String extension = fileName.substring(fileName.lastIndexOf("."));
      type = viewers.getExtensions().get(extension);
    }

    return type;
  }

  private void emptyPreview() {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='emptymessage'>"));
    b.append(SafeHtmlUtils.fromString(messages.viewRepresentationEmptyPreview()));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    html.setHTML(b.toSafeHtml());
    filePreview.add(html);
    html.setStyleName("viewRepresentationEmptyPreview");

    downloadFileButton.setVisible(false);
    removeFileButton.setVisible(false);
    infoFileButton.setVisible(false);
  }

  private void errorPreview() {
    errorPreview(messages.viewRepresentationErrorPreview());
  }

  private void errorPreview(String errorPreview) {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-download fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='errormessage'>"));
    b.append(SafeHtmlUtils.fromString(errorPreview));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    Button downloadButton = new Button(messages.viewRepresentationDownloadFileButton());
    downloadButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        downloadFile();
      }
    });

    html.setHTML(b.toSafeHtml());
    filePreview.add(html);
    filePreview.add(downloadButton);
    html.setStyleName("viewRepresentationErrorPreview");
    downloadButton.setStyleName("btn btn-donwload viewRepresentationNotSupportedDownloadButton");
  }

  private void notSupportedPreview() {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-picture-o fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='errormessage'>"));
    b.append(SafeHtmlUtils.fromString(messages.viewRepresentationNotSupportedPreview()));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    Button downloadButton = new Button(messages.viewRepresentationDownloadFileButton());
    downloadButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        downloadFile();
      }
    });

    html.setHTML(b.toSafeHtml());
    filePreview.add(html);
    filePreview.add(downloadButton);
    html.setStyleName("viewRepresentationNotSupportedPreview");
    downloadButton.setStyleName("btn btn-download viewRepresentationNotSupportedDownloadButton");

    Scheduler.get().scheduleDeferred(new Command() {
      public void execute() {
        toggleDisseminationsPanel();
      }
    });

  }

  private void imagePreview(IndexedFile file) {
    Image image = new Image(RestUtils.createRepresentationFileDownloadUri(file.getUUID()));
    image.addErrorHandler(new ErrorHandler() {

      @Override
      public void onError(ErrorEvent event) {
        filePreview.clear();
        errorPreview();
      }
    });
    filePreview.add(image);
    image.setStyleName("viewRepresentationImageFilePreview");
  }

  private void pdfPreview(IndexedFile file) {
    String viewerHtml = GWT.getHostPageBaseURL() + "pdf/viewer.html?file="
      + encode(GWT.getHostPageBaseURL() + RestUtils.createRepresentationFileDownloadUri(file.getUUID()).asString());

    Frame frame = new Frame(viewerHtml);
    filePreview.add(frame);
    frame.setStyleName("viewRepresentationPDFFilePreview");
  }

  private void textPreview(IndexedFile file) {
    if (StringUtils.isBlank(viewers.getTextLimit()) || file.getSize() <= Long.parseLong(viewers.getTextLimit())) {
      RequestBuilder request = new RequestBuilder(RequestBuilder.GET,
        RestUtils.createRepresentationFileDownloadUri(file.getUUID()).asString());
      try {
        request.sendRequest(null, new RequestCallback() {

          @Override
          public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == HttpStatus.SC_OK) {
              HTML html = new HTML("<pre><code>" + SafeHtmlUtils.htmlEscape(response.getText()) + "</code></pre>");
              FlowPanel frame = new FlowPanel();
              frame.add(html);

              filePreview.add(frame);
              frame.setStyleName("viewRepresentationTextFilePreview");
              JavascriptUtils.runHighlighter(html.getElement());
            } else {
              errorPreview();
            }
          }

          @Override
          public void onError(Request request, Throwable exception) {
            errorPreview();
          }
        });
      } catch (RequestException e) {
        errorPreview();
      }
    } else {
      errorPreview(messages.viewRepresentationTooLargeErrorPreview());
    }
  }

  private void audioPreview(IndexedFile file) {
    Audio audioPlayer = Audio.createIfSupported();
    if (audioPlayer != null) {
      HTML html = new HTML();
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-headphones fa-5'></i>"));
      html.setHTML(b.toSafeHtml());

      audioPlayer.addSource(RestUtils.createRepresentationFileDownloadUri(file.getUUID()).asString(),
        file.getFileFormat().getMimeType());
      audioPlayer.setControls(true);
      filePreview.add(html);
      filePreview.add(audioPlayer);
      audioPlayer.addStyleName("viewRepresentationAudioFilePreview");
      html.addStyleName("viewRepresentationAudioFilePreviewHTML");
    } else {
      notSupportedPreview();
    }
  }

  private void videoPreview(IndexedFile file) {
    Video videoPlayer = Video.createIfSupported();
    if (videoPlayer != null) {
      videoPlayer.addSource(RestUtils.createRepresentationFileDownloadUri(file.getUUID()).asString(),
        convertVideoMimetypes(file.getFileFormat().getMimeType()));
      videoPlayer.setControls(true);
      filePreview.add(videoPlayer);
      videoPlayer.addStyleName("viewRepresentationAudioFilePreview");
    } else {
      notSupportedPreview();
    }
  }

  private String convertVideoMimetypes(String mimetype) {
    if (mimetype.equals("application/mp4")) {
      return "video/mp4";
    } else if (mimetype.equals("application/ogg")) {
      return "video/ogg";
    } else {
      return mimetype;
    }
  }

  private String encode(String string) {
    return string.replace("?", "%3F").replace("=", "%3D");
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
    for (final IndexedDIP dip : dips) {

      FlowPanel entry = new FlowPanel();
      FocusPanel focus = new FocusPanel(entry);

      Label titleLabel = new Label(dip.getTitle());
      Label descriptionLabel = new Label(dip.getDescription());

      entry.add(titleLabel);
      entry.add(descriptionLabel);

      dipFilePanel.add(focus);

      titleLabel.addStyleName("dipTitle");
      descriptionLabel.addStyleName("dipDescription");
      entry.addStyleName("dip");
      focus.addStyleName("dip-focus");

      focus.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          if (StringUtils.isNotBlank(dip.getOpenExternalURL())) {
            Window.open(dip.getOpenExternalURL(), "_blank", "");
            Toast.showInfo("Opened dissemination", dip.getOpenExternalURL());
          } else {
            Toast.showInfo("Feature not yet implemented", "Will open the DIP in a new panel");
          }
        }
      });

    }
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
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
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
      Tools.newHistory(Browse.RESOLVER);
      callback.onSuccess(null);
    }
  };

  public static void jumpTo(IndexedFile selected) {
    Tools.newHistory(BrowseFile.RESOLVER, selected.getAipId(), selected.getRepresentationUUID(), selected.getUUID());
  }
}
