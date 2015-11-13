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
package org.roda.wui.ingest.submit.client;

import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.fileupload.client.FileNameConstraints;
import org.roda.wui.common.fileupload.client.FileUploadPanel;
import org.roda.wui.ingest.list.client.IngestList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestSubmitConstants;

/**
 * @author Luis Faria
 * 
 */
public class UploadSIP {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static IngestSubmitConstants constants = (IngestSubmitConstants) GWT.create(IngestSubmitConstants.class);

  private boolean initialized;

  private VerticalPanel layout;

  private Label title;

  private FileUploadPanel fileUpload;

  private HorizontalPanel actionLayout;

  private Button getRodaIn;

  private Button submitButton;

  private Image loading;

  private Label loadingMessage;

  private boolean ingesting;

  /**
   * Create a new upload SIP panel
   */
  public UploadSIP() {
    layout = new VerticalPanel();
    layout.addStyleName("wui-ingest-submit-upload");
    initialized = false;
  }

  /**
   * Initialize upload SIP panel
   */
  public void init() {
    if (!initialized) {
      initialized = true;

      ingesting = false;

      title = new Label(constants.uploadHeader());
      FileNameConstraints fileNameConstraints = new FileNameConstraints();
      fileNameConstraints.addConstraint(new String[] {"zip", "sip", "xml"}, -1);
      fileUpload = new FileUploadPanel(fileNameConstraints);
      actionLayout = new HorizontalPanel();

      submitButton = new Button(constants.uploadSubmitButton());

      submitButton.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          submitButton.setEnabled(false);
          if (!fileUpload.isEmpty()) {
            ingest();
          }

        }

      });

      getRodaIn = new Button(constants.uploadSubmitGetRodaIn());

      getRodaIn.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          Ingest.downloadRodaIn(null, null);
        }

      });

      fileUpload.addChangeListener(new ChangeListener() {

        public void onChange(Widget sender) {
          updateVisibles();
        }

      });

      loading = new Image(GWT.getModuleBaseURL() + "images/loadingSmall.gif");
      loadingMessage = new Label();

      updateVisibles();

      actionLayout.add(submitButton);
      actionLayout.add(loading);
      actionLayout.add(loadingMessage);
      actionLayout.add(getRodaIn);

      layout.add(title);
      layout.add(fileUpload.getWidget());
      layout.add(actionLayout);

      title.addStyleName("h3");
      fileUpload.getWidget().addStyleName("upload-file");
      actionLayout.addStyleName("upload-action");
      submitButton.addStyleName("upload-action-button");
      submitButton.addStyleName("btn");
      submitButton.addStyleName("btn-play");
      loading.addStyleName("upload-action-loading-image");
      loadingMessage.addStyleName("upload-action-loading-message");
      getRodaIn.addStyleName("upload-action-get-roda-in");
      getRodaIn.addStyleName("btn");
      getRodaIn.addStyleName("btn-download");
      actionLayout.setCellVerticalAlignment(loading, HasAlignment.ALIGN_MIDDLE);
      actionLayout.setCellVerticalAlignment(loadingMessage, HasAlignment.ALIGN_MIDDLE);
      actionLayout.setCellWidth(getRodaIn, "100%");
      actionLayout.setCellHorizontalAlignment(getRodaIn, HasHorizontalAlignment.ALIGN_RIGHT);
    }
  }

  private void updateVisibles() {
    submitButton.setEnabled(!fileUpload.isEmpty() && !ingesting);
    loading.setVisible(ingesting);
    loadingMessage.setVisible(ingesting);
  }

  /**
   * Get upload SIP panel widget
   * 
   * @return the widget
   */
  public Widget getWidget() {
    return layout;
  }

  private void ingest() {
    ingesting = true;
    loadingMessage.setText(constants.uploadLoadingData());
    updateVisibles();

    fileUpload.submit(new AsyncCallback<String[]>() {

      public void onFailure(Throwable caught) {
        logger.error("Error uploading files", caught);
        ingesting = false;
        updateVisibles();
      }

      public void onSuccess(String[] fileCodes) {
        loadingMessage.setText(constants.uploadLoadingIngest());
        IngestSubmitService.Util.getInstance().submitSIPs(fileCodes, new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
            logger.error("Error ingesting files", caught);
            submitButton.setEnabled(true);
            loading.setVisible(false);
            loadingMessage.setVisible(false);
          }

          public void onSuccess(Boolean allIngested) {
            if (!allIngested.booleanValue()) {
              Window.alert(constants.uploadSubmitFailure());
            }
            ingesting = false;
            updateVisibles();

            // Initialize ingest list
            // IngestList.getInstance().init();

            // Set processing state filter
            // FIXME
            // IngestList.getInstance().setStateFilter(IngestList.StateFilter.PROCESSING);

            // Update ingest list
            IngestList.getInstance().update();

            // Show ingest list
            Tools.newHistory(IngestList.RESOLVER);
          }

        });
      }

    });

  }

}
