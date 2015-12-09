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

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.SimpleFile;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.FileList;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
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

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 1) {
        final String aipId = historyTokens.get(0);
        final String representationId = historyTokens.get(1);
        // final String fileId = historyTokens.get(2);

        ViewRepresentation view = new ViewRepresentation(aipId, representationId, null);
        callback.onSuccess(view);
      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
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
  };

  interface MyUiBinder extends UiBinder<Widget, ViewRepresentation> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private String aipId;
  private String representationId;
  private String fileId;

  static final int WINDOW_WIDTH = 950;

  private boolean uniqueFile = false;

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
   * @param fileId
   * 
   * @param user
   *          the user to edit
   */
  public ViewRepresentation(String aipId, String representationId, String fileId) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.fileId = fileId;

    filesPanel = new FileList();
    Filter f = new Filter();
    f.add(new SimpleFilterParameter(RodaConstants.FILE_AIPID, aipId));
    f.add(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATIONID, representationId));
    filesPanel.setFilter(f);

    initWidget(uiBinder.createAndBindUi(this));

    back.setText(messages.backButton());
    nextFile.setText(messages.viewRepresentationNextFileButton());
    previousFile.setText(messages.viewRepresentationPreviousFileButton());
    downloadFile.setText(messages.viewRepresentationDownloadFileButton());

    // nextFile.setVisible(false);
    // previousFile.setVisible(false);
    // downloadFile.setVisible(false);

    filesPanel.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        // SimpleFile file = filesPanel.getSelectionModel().getSelectedObject();
        if (Window.getClientWidth() < WINDOW_WIDTH) {
          logger.debug("new history");
        } else {
          logger.debug("refresh file preview");
          filePreview();
        }
      }
    });

    filesPanel.addValueChangeHandler(new ValueChangeHandler<IndexResult<SimpleFile>>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<SimpleFile>> event) {

      }
    });

    filesPanel.addStyleName("viewRepresentationFilesPanel");
    filePreview.addStyleName("viewRepresentationFilePreview");
    previewPanel.setCellWidth(filePreview, "100%");

    panelsControl();
    filePreview();
  }

  @UiHandler("back")
  void buttonBackHandler(ClickEvent e) {
    Tools.newHistory(Browse.RESOLVER, aipId);
  }

  private void panelsControl() {
    if (!uniqueFile) {
      if (Window.getClientWidth() < WINDOW_WIDTH) {
        filesPanel.addStyleName("fullWidth");
        previewPanel.setCellWidth(filePreview, "0px");
        filePreview.setVisible(false);
      } else {
        filesPanel.removeStyleName("fullWidth");
        previewPanel.setCellWidth(filePreview, "100%");
        filePreview.setVisible(true);
      }

      Window.addResizeHandler(new ResizeHandler() {

        @Override
        public void onResize(ResizeEvent event) {
          if (Window.getClientWidth() < WINDOW_WIDTH) {
            filesPanel.addStyleName("fullWidth");
            previewPanel.setCellWidth(filePreview, "0px");
            filePreview.setVisible(false);
          } else {
            filesPanel.removeStyleName("fullWidth");
            previewPanel.setCellWidth(filePreview, "100%");
            filePreview.setVisible(true);
          }
        }
      });
    } else {
      filesPanel.setVisible(false);
    }
  }

  private void filePreview() {
    filePreview.clear();

    SimpleFile file = filesPanel.getSelectionModel().getSelectedObject();
    if (file != null && file.getOriginalName() != null) {

      /* IMAGE */
      if (file.getOriginalName().contains(".png") || file.getOriginalName().contains(".jpg")) {
        Image image = new Image(RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()));
        filePreview.add(image);
        image.setWidth("100%");
      } else if (file.getOriginalName().contains(".pdf")) {

      } else if (file.getOriginalName().contains(".xml")) {

      } else if (file.getOriginalName().contains(".webm")) {
      } else if (file.getOriginalName().contains(".mp3")) {
        Audio audioPlayer = Audio.createIfSupported();
        if (audioPlayer != null) {
          audioPlayer.addSource(
            RestUtils.createRepresentationFileDownloadUri(aipId, representationId, file.getId()).asString(),
            "audio/mpeg");
          audioPlayer.setControls(true);
          filePreview.add(audioPlayer);
          audioPlayer.setWidth("100%");
          audioPlayer.setHeight("100px");
        } else {
          // TODO show error preview
        }
      }

    } else {
      // TODO show error preview
    }
  }
}
