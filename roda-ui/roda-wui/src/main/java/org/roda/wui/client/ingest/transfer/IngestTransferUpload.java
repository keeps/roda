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

import java.util.List;

import org.roda.core.data.v2.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.JavascriptUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "upload";
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(IngestTransfer.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestTransferUpload instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static IngestTransferUpload getInstance() {
    if (instance == null) {
      instance = new IngestTransferUpload();
    }
    return instance;
  }

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

  @UiField
  HTML uploadList;

  private String username;
  private TransferredResource resource;

  private IngestTransferUpload() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  private String getUploadUrl() {
    String ret;

    if (resource == null) {
      // upload to user root
      ret = RestUtils.createTransferredResourceUploadUri(username);
    } else if (resource != null && !resource.isFile()) {
      String id = resource.getId();
      ret = RestUtils.createTransferredResourceUploadUri(id);
    } else {
      ret = null;
    }

    return ret;
  }

  protected void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      Tools.newHistory(IngestTransfer.RESOLVER);
      callback.onSuccess(null);
    } else if (historyTokens.size() == 1) {
      // Upload to user root
      resource = null;
      username = historyTokens.get(0);
      callback.onSuccess(IngestTransferUpload.this);
      updateUploadForm();
    } else {
      // Upload to directory
      String transferredResourceId = IngestTransfer.getTransferredResourceIdFromPath(historyTokens);
      if (transferredResourceId != null) {
        BrowserService.Util.getInstance().retrieveTransferredResource(transferredResourceId,
          new AsyncCallback<TransferredResource>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(TransferredResource r) {
              resource = r;
              username= r.getOwner();
              callback.onSuccess(IngestTransferUpload.this);
              updateUploadForm();
            }
          });
      } else {
        Tools.newHistory(IngestTransfer.RESOLVER);
        callback.onSuccess(null);
      }
    }
  }

  private void updateUploadForm() {
    String uploadUrl = getUploadUrl();

    if (uploadUrl != null) {
      SafeHtml html = SafeHtmlUtils.fromSafeConstant("<form id='upload' method='post' action='" + getUploadUrl()
        + "' enctype='multipart/form-data'>" + "<div id='drop'>" + messages.ingestTransferUploadDropHere() + "<a>"
        + messages.ingestTransferUploadBrowseFiles() + "</a>" + "<input type='file' name='upl' multiple='true' />"
        + "</div>" + "</form>");

      uploadForm.setHTML(html);
      uploadList.setHTML(SafeHtmlUtils.fromSafeConstant("<ul id='upload-list'></ul>"));
      JavascriptUtils.runMiniUploadForm();
    } else {
      uploadForm.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }
  }

  @UiHandler("done")
  void buttonDoneHandler(ClickEvent e) {
    if (resource != null) {
      Tools.newHistory(IngestTransfer.RESOLVER, IngestTransfer.getPathFromTransferredResourceId(resource.getId()));
    } else {
      Tools.newHistory(IngestTransfer.RESOLVER);
    }
  }

}
