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

import org.apache.commons.httpclient.HttpStatus;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.SimpleFile;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.FileList;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.JavascriptUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 * 
 */
public class ViewRepresentation extends Composite {

  private static final String TOP_ICON = "<i class='fa fa-circle-o'></i>";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 1) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.get(1);
        final String fileId = null;

        BrowserService.Util.getInstance().getItemBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
          new AsyncCallback<BrowseItemBundle>() {

          @Override
          public void onFailure(Throwable caught) {
            errorRedirect(callback);
          }

          @Override
          public void onSuccess(BrowseItemBundle itemBundle) {
            if (itemBundle != null && verifyRepresentation(itemBundle.getRepresentations(), representationId)) {
              ViewRepresentation view = new ViewRepresentation(aipId, itemBundle, representationId, fileId);
              callback.onSuccess(view);
            } else {
              errorRedirect(callback);
            }
          }
        });
      } else {
        errorRedirect(callback);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {Browse.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "view";
    }
    
    private boolean verifyRepresentation (List<Representation> representations, String representationId) {
      boolean exist = false;
      for (Representation representation: representations) {
        if (representation.getId().equals(representationId)) {
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

  interface MyUiBinder extends UiBinder<Widget, ViewRepresentation> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private String aipId;
  private String representationId;
  private String fileId;

  static final int WINDOW_WIDTH = 1200;

  private boolean uniqueFile = false;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  HorizontalPanel previewPanel;

  @UiField(provided = true)
  FileList filesPanel;

  @UiField
  FlowPanel filePreview;

  @UiField
  Button back;

  @UiField
  Button nextFile;

  @UiField
  Button previousFile;

  @UiField
  Button downloadFile;

  /**
   * Create a new panel to edit a user
   * 
   * @param descriptiveMetadataId
   * @param aipId
   * @param itemBundle
   * @param fileId
   * 
   * @param user
   *          the user to edit
   */
  public ViewRepresentation(String aipId, BrowseItemBundle itemBundle, String representationId, String fileId) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.fileId = fileId;

    Filter f = new Filter();
    f.add(new SimpleFilterParameter(RodaConstants.FILE_AIPID, aipId));
    f.add(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATIONID, representationId));
    filesPanel = new FileList(f, null, null);

    initWidget(uiBinder.createAndBindUi(this));

    breadcrumb.updatePath(getBreadcrumbs());
    breadcrumb.setVisible(true);

    back.setText(messages.backButton());
    nextFile.setText(messages.viewRepresentationNextFileButton());
    previousFile.setText(messages.viewRepresentationPreviousFileButton());
    downloadFile.setText(messages.viewRepresentationDownloadFileButton());

    filesPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        if (Window.getClientWidth() < WINDOW_WIDTH) {
          logger.debug("new history");
        } else {
          logger.debug("refresh file preview");
          filePreview();
        }
      }
    });

    filesPanel.addStyleName("viewRepresentationFilesPanel");
    filePreview.addStyleName("viewRepresentationFilePreview");
    previewPanel.setCellWidth(filePreview, "100%");

    panelsControl();
    filePreview();
  }

  private List<BreadcrumbItem> getBreadcrumbs() {
    List<BreadcrumbItem> ret = new ArrayList<>();

    logger.debug("HERE");

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), new ArrayList<String>()));
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), new ArrayList<String>()));
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), new ArrayList<String>()));
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), new ArrayList<String>()));
    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), new ArrayList<String>()));

    return ret;
  }

  @UiHandler("back")
  void buttonBackHandler(ClickEvent e) {
    Tools.newHistory(Browse.RESOLVER, aipId);
  }

  @UiHandler("nextFile")
  void buttonNextFileHandler(ClickEvent e) {
    filesPanel.nextItemSelection();
  }

  @UiHandler("previousFile")
  void buttonPreviousFileHandler(ClickEvent e) {
    filesPanel.previousItemSelection();
  }

  @UiHandler("downloadFile")
  void buttonDownloadFileHandler(ClickEvent e) {
    SafeUri downloadUri = null;
    if (fileId != null) {
      downloadUri = RestUtils.createRepresentationFileDownloadUri(aipId, representationId, fileId);
    } else if (filesPanel.getSelectionModel().getSelectedObject() != null) {
      downloadUri = RestUtils.createRepresentationFileDownloadUri(aipId, representationId,
        filesPanel.getSelectionModel().getSelectedObject().getId());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
  }

  private void panelsControl() {
    if (!uniqueFile) {
      if (Window.getClientWidth() < WINDOW_WIDTH) {
        hideFilePreview();
      } else {
        showFilePreview();
      }

      Window.addResizeHandler(new ResizeHandler() {

        @Override
        public void onResize(ResizeEvent event) {
          if (Window.getClientWidth() < WINDOW_WIDTH) {
            hideFilePreview();
          } else {
            showFilePreview();
          }
        }
      });
    } else {
      hideFilesList();
    }
  }

  private void hideFilesList() {
    filesPanel.setVisible(false);
  }

  private void showFilePreview() {
    filesPanel.removeStyleName("fullWidth");
    previewPanel.setCellWidth(filePreview, "100%");
    filePreview.setVisible(true);

    nextFile.setVisible(true);
    previousFile.setVisible(true);
  }

  private void hideFilePreview() {
    filesPanel.addStyleName("fullWidth");
    previewPanel.setCellWidth(filePreview, "0px");
    filePreview.setVisible(false);

    nextFile.setVisible(false);
    previousFile.setVisible(false);
  }

  private void filePreview() {
    SimpleFile file = filesPanel.getSelectionModel().getSelectedObject();
    if (file != null && file.getOriginalName() != null) {
      filePreview.clear();

      if (file.getOriginalName().contains(".png") || file.getOriginalName().contains(".jpg")) {
        imagePreview(file);
      } else if (file.getOriginalName().contains(".pdf")) {
        pdfPreview(file);
      } else if (file.getOriginalName().contains(".xml")) {
        textPreview(file);
      } else if (file.getOriginalName().contains(".mp3")) {
        audioPreview(file);
      } else if (file.getOriginalName().contains(".mp4")) {
        videoPreview(file);
      } else {
        notSupportedPreview();
      }
    } else if (file == null) {
      emptyPreview();
    } else {
      errorPreview();
    }
  }

  private void emptyPreview() {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='emptymessage'>"));
    b.append(SafeHtmlUtils.fromString("Please select a file from the list on left panel"));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    html.setHTML(b.toSafeHtml());
    filePreview.add(html);
    html.setStyleName("viewRepresentationEmptyPreview");
  }

  private void errorPreview() {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-exclamation-triangle fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='errormessage'>"));
    b.append(SafeHtmlUtils.fromString("An error occurred while trying to view the file"));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    html.setHTML(b.toSafeHtml());
    filePreview.add(html);
    html.setStyleName("viewRepresentationErrorPreview");
  }

  private void notSupportedPreview() {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-exclamation-triangle fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='errormessage'>"));
    b.append(SafeHtmlUtils.fromString("File preview not supported"));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    html.setHTML(b.toSafeHtml());
    filePreview.add(html);
    html.setStyleName("viewRepresentationNotSupportedPreview");
  }

  private void imagePreview(SimpleFile file) {
    Image image = new Image(RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()));
    filePreview.add(image);
    image.setStyleName("viewRepresentationImageFilePreview");
  }

  private void pdfPreview(SimpleFile file) {
    String viewerHtml = GWT.getHostPageBaseURL() + "pdf/viewer.html?file=" + encode(GWT.getHostPageBaseURL()
      + RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()).asString());

    Frame frame = new Frame(viewerHtml);
    filePreview.add(frame);
    frame.setStyleName("viewRepresentationPDFFilePreview");
  }

  private void textPreview(SimpleFile file) {
    RequestBuilder request = new RequestBuilder(RequestBuilder.GET,
      RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()).asString());
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
            JavascriptUtils.slideToggle(html.getElement(), ".toggle-next");
            JavascriptUtils.smoothScroll(html.getElement());
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
  }

  private void audioPreview(SimpleFile file) {
    Audio audioPlayer = Audio.createIfSupported();
    if (audioPlayer != null) {
      audioPlayer.addSource(
        RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()).asString(), "audio/mpeg");
      audioPlayer.setControls(true);
      filePreview.add(audioPlayer);
      audioPlayer.addStyleName("viewRepresentationAudioFilePreview");
    } else {
      notSupportedPreview();
    }
  }

  private void videoPreview(SimpleFile file) {
    Video videoPlayer = Video.createIfSupported();
    if (videoPlayer != null) {
      videoPlayer.addSource(
        RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()).asString(), "video/dvd");
      videoPlayer.setControls(true);
      filePreview.add(videoPlayer);
      videoPlayer.addStyleName("viewRepresentationAudioFilePreview");
    } else {
      notSupportedPreview();
    }
  }

  private String encode(String string) {
    return string.replace("?", "%3F").replace("=", "%3D");
  }
}
