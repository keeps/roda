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
package org.roda.wui.client.ingest.transfer;

import org.roda.core.data.v2.TransferredResource;
import org.roda.wui.common.client.tools.JavascriptUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class IngestTransferUpload extends Composite {

  interface MyUiBinder extends UiBinder<Widget, IngestTransferUpload> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  // BUTTONS
  @UiField
  Button done;

  @UiField
  HTML uploadForm;

  private TransferredResource resource;

  private IngestTransferUpload(TransferredResource resource) {
    this.resource = resource;
    initWidget(uiBinder.createAndBindUi(this));
    updateUploadForm();
  }

  private String getUploadUrl() {
    String ret;

    if (resource != null && !resource.isFile()) {
      String id = resource.getId();
      ret = RestUtils.createTransferredResourceUploadUri(id);
    } else {
      ret = null;
    }

    return ret;
  }

  private void updateUploadForm() {
    String uploadUrl = getUploadUrl();

    if (uploadUrl != null) {
      SafeHtml html = SafeHtmlUtils.fromSafeConstant(
        "<form id='upload' method='post' action='" + getUploadUrl() + "' enctype='multipart/form-data'>"
          + "<div id='drop'>" + messages.ingestTransferUploadDropHere() + "<a>" + messages.ingestTransferUploadBrowse()
          + "</a>" + "<input type='file' name='upl' multiple='true' />" + "</div>" + "</form>");

      uploadForm.setHTML(html);
      JavascriptUtils.runMiniUploadForm();
    } else {
      uploadForm.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }
  }

  @UiHandler("done")
  void buttonDoneHandler(ClickEvent e) {
    // TODO go back to previous resource
    // Tools.newHistory(IngestTransfer.RESOLVER);
  }

}
