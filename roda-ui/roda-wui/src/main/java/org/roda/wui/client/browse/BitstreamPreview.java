/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.apache.commons.httpclient.HttpStatus;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class BitstreamPreview<T extends IsIndexed> extends Composite {

  private static final String VIEWER_TYPE_VIDEO = "video";
  private static final String VIEWER_TYPE_AUDIO = "audio";
  private static final String VIEWER_TYPE_TEXT = "text";
  private static final String VIEWER_TYPE_PDF = "pdf";
  private static final String VIEWER_TYPE_IMAGE = "image";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  // interface
  private final FlowPanel panel;

  // system properties
  private final Viewers viewers;

  // bitstream properties
  private final SafeUri bitstreamDownloadUri;
  private final FileFormat format;
  private final String filename;
  private final long size;
  private final boolean isDirectory;

  // other
  private final Command onPreviewFailure;

  private final T object;

  public BitstreamPreview(Viewers viewers, SafeUri bitstreamDownloadUri, FileFormat format, String filename, long size,
    boolean isDirectory, T object) {
    this(viewers, bitstreamDownloadUri, format, filename, size, isDirectory, new Command() {

      @Override
      public void execute() {
        // do nothing
      }
    }, object);
  }

  public BitstreamPreview(Viewers viewers, SafeUri bitstreamDownloadUri, FileFormat format, String filename, long size,
    boolean isDirectory, Command onPreviewFailure, T object) {
    super();
    this.object = object;
    this.panel = new FlowPanel();

    this.viewers = viewers;

    this.bitstreamDownloadUri = bitstreamDownloadUri;
    this.format = format;
    this.filename = filename;
    this.size = size;
    this.isDirectory = isDirectory;

    this.onPreviewFailure = onPreviewFailure;

    initWidget(panel);

    setStyleName("bitstreamPreview");
    if (isDirectory) {
      addStyleDependentName("directory");
    }

    init();

  }

  public SafeUri getBitstreamDownloadUri() {
    return bitstreamDownloadUri;
  }

  public FileFormat getFormat() {
    return format;
  }

  public String getFilename() {
    return filename;
  }

  public long getSize() {
    return size;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  private void init() {

    if (!isDirectory) {
      String type = viewerType();
      if (type != null) {
        if (type.equals(VIEWER_TYPE_IMAGE)) {
          imagePreview();
        } else if (type.equals(VIEWER_TYPE_PDF)) {
          pdfPreview();
        } else if (type.equals(VIEWER_TYPE_TEXT)) {
          textPreview();
        } else if (type.equals(VIEWER_TYPE_AUDIO)) {
          audioPreview();
        } else if (type.equals(VIEWER_TYPE_VIDEO)) {
          videoPreview();
        } else {
          notSupportedPreview();
        }
      } else {
        notSupportedPreview();
      }
    } else {
      panel.add(directoryPreview());
    }
  }

  private String viewerType() {
    String type = null;
    if (format != null) {
      if (format.getPronom() != null) {
        type = viewers.getPronoms().get(format.getPronom());
      }

      if (format.getMimeType() != null && type == null) {
        type = viewers.getMimetypes().get(format.getMimeType());
      }
    }

    if (type == null && filename.lastIndexOf('.') != -1) {
      String extension = getFileNameExtension();
      type = viewers.getExtensions().get(extension);
    }

    return type;
  }

  private String getFileNameExtension() {
    return filename.substring(filename.lastIndexOf('.')).toLowerCase();
  }

  private void imagePreview() {
    final SimplePanel imageContainer = new SimplePanel();

    panel.add(imageContainer);
    imageContainer.setStyleName("viewRepresentationImageFilePreview");

    imageContainer.addAttachHandler(new Handler() {

      private JavaScriptObject imageViewer = null;

      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          // load image
          imageViewer = JavascriptUtils.runImageViewerOn(imageContainer.getElement(), bitstreamDownloadUri.asString());
        } else {
          // destroy
          if (imageViewer != null) {
            JavascriptUtils.stopImageViewer(imageViewer);
          }
        }

      }
    });

  }

  private void pdfPreview() {
    String viewerHtml = GWT.getHostPageBaseURL() + "pdf/viewer.html?file="
      + encode(GWT.getHostPageBaseURL() + bitstreamDownloadUri.asString());

    Frame frame = new Frame(viewerHtml);
    panel.add(frame);
    frame.setStyleName("viewRepresentationPDFFilePreview");
  }

  private void textPreview() {
    if (StringUtils.isBlank(viewers.getTextLimit()) || size <= Long.parseLong(viewers.getTextLimit())) {
      RequestBuilder request = new RequestBuilder(RequestBuilder.GET, bitstreamDownloadUri.asString());
      try {
        request.sendRequest(null, new RequestCallback() {

          @Override
          public void onResponseReceived(Request request, Response response) {
            if (response.getStatusCode() == HttpStatus.SC_OK) {
              HTML html = new HTML("<pre><code>" + SafeHtmlUtils.htmlEscape(response.getText()) + "</code></pre>");
              FlowPanel frame = new FlowPanel();
              frame.add(html);

              panel.add(frame);
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

  private void audioPreview() {
    Audio audioPlayer = Audio.createIfSupported();
    if (audioPlayer != null) {
      HTML html = new HTML();
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-headphones fa-5'></i>"));
      html.setHTML(b.toSafeHtml());

      // TODO check if audio source type needs to be transformed
      // TODO check if audio player supports provided file format
      audioPlayer.addSource(bitstreamDownloadUri.asString(), getAudioSourceType());
      audioPlayer.setControls(true);
      panel.add(html);
      panel.add(audioPlayer);
      audioPlayer.addStyleName("viewRepresentationAudioFilePreview");
      html.addStyleName("viewRepresentationAudioFilePreviewHTML");
    } else {
      notSupportedPreview();
    }
  }

  private void videoPreview() {
    Video videoPlayer = Video.createIfSupported();
    if (videoPlayer != null) {
      videoPlayer.addSource(bitstreamDownloadUri.asString(), getVideoSourceType());
      videoPlayer.setControls(true);
      panel.add(videoPlayer);
      videoPlayer.addStyleName("viewRepresentationAudioFilePreview");
    } else {
      notSupportedPreview();
    }
  }

  private String getVideoSourceType() {
    String ret;

    if (format != null && StringUtils.isNotBlank(format.getMimeType())) {
      String mimetype = format.getMimeType();
      if ("application/mp4".equals(mimetype)) {
        ret = "video/mp4";
      } else if ("application/ogg".equals(mimetype)) {
        ret = "video/ogg";
      } else {
        ret = mimetype;
      }
    } else {
      String extension = getFileNameExtension();

      if (".ogg".equals(extension)) {
        ret = "video/ogg";
      } else {
        ret = "video/mp4";
      }
    }

    // TODO video player might not support provided file format
    return ret;
  }

  private String getAudioSourceType() {
    String ret;

    if (format != null && StringUtils.isNotBlank(format.getMimeType())) {
      String mimetype = format.getMimeType();
      ret = mimetype;
    } else {
      String extension = getFileNameExtension();

      if (".ogg".equals(extension)) {
        ret = "audio/ogg";
      } else {
        ret = "audio/mpeg";
      }
    }

    // TODO audio player might not support provided file format
    return ret;
  }

  private String encode(String string) {
    return string.replace("?", "%3F").replace("=", "%3D");
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
    panel.add(html);
    panel.add(downloadButton);
    html.setStyleName("viewRepresentationErrorPreview");
    downloadButton.setStyleName("btn btn-donwload viewRepresentationNotSupportedDownloadButton");

    onPreviewFailure.execute();
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
    panel.add(html);
    panel.add(downloadButton);
    html.setStyleName("viewRepresentationNotSupportedPreview");
    downloadButton.setStyleName("btn btn-download viewRepresentationNotSupportedDownloadButton");

    onPreviewFailure.execute();

  }

  protected Widget directoryPreview() {
    HTML html = new HTML();
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-open fa-5'></i>"));
    b.append(SafeHtmlUtils.fromSafeConstant("<h4 class='emptymessage'>"));
    b.append(SafeHtmlUtils.fromString(filename + " /"));
    b.append(SafeHtmlUtils.fromSafeConstant("</h4>"));

    html.setHTML(b.toSafeHtml());
    html.setStyleName("viewRepresentationEmptyPreview");
    return html;
  }

  private void downloadFile() {
    if (bitstreamDownloadUri != null) {
      Window.Location.assign(bitstreamDownloadUri.asString());
    }
  }

  public T getObject() {
    return object;
  }

}
