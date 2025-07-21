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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class TransferUpload extends Composite {

  public static final HistoryResolver INGEST_RESOLVER = new HistoryResolver() {

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
      return ListUtils.concat(IngestTransfer.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  public static final HistoryResolver BROWSE_RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().browseResolve(historyTokens, callback);
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
      return ListUtils.concat(BrowseTop.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, TransferUpload> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String DRAGOVER = "dragover";

  @UiField
  TitlePanel uploadTitle;

  @UiField
  Button done;

  @UiField
  HTML uploadMessage;

  @UiField
  HTML uploadForm;

  @UiField
  HTML uploadList;

  // INGEST TRANSFER
  private TransferredResource resource;

  // BROWSE UPLOAD
  private String aipId;
  private String representationId;
  private List<String> folderPath;
  private String folderId;

  private boolean isIngest = true;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.TRANSFERRED_RESOURCE_ID, RodaConstants.TRANSFERRED_RESOURCE_ISFILE);

  private HandlerRegistration handlerRegistration;

  private static TransferUpload instance = null;
  protected boolean verified = false;

  private TransferUpload() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static TransferUpload getInstance() {
    if (instance == null) {
      instance = new TransferUpload();
    }
    return instance;
  }

  private String getUploadUrl() {
    String ret = null;

    if (isIngest) {
      uploadTitle.setText(messages.ingestTransferUploadTitle());
      if (resource == null) {
        // upload to root
        ret = RestUtils.createTransferredResourceUploadUri(null, LocaleInfo.getCurrentLocale().getLocaleName());
      } else if (!resource.isFile()) {
        ret = RestUtils.createTransferredResourceUploadUri(resource.getUUID(),
          LocaleInfo.getCurrentLocale().getLocaleName());
      }
    } else {
      uploadTitle.setText(messages.fileUploadTitle());
      LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
      String details = selectedItems.getDetailsMessage();

      List<String> directory = new ArrayList<>();
      if (folderPath != null) {
        directory.addAll(folderPath);
      }
      if (folderId != null) {
        directory.add(folderId);
      }

      ret = RestUtils.createFileUploadUri(aipId, representationId, directory, details);
    }

    return ret;
  }

  @Override
  protected void onAttach() {
    verified = false;
    super.onAttach();
  }

  protected void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
    isIngest = true;

    uploadMessage.getElement().setId("upload-message");
    uploadMessage.setHTML("<span class='success'>" + messages.uploadDoneMessage() + "</span>");
    uploadMessage.setVisible(false);

    if (historyTokens.isEmpty()) {
      // Upload to root
      resource = null;
      callback.onSuccess(TransferUpload.this);
      updateUploadForm();
    } else if (historyTokens.size() == 1) {
      // Upload to directory
      String transferredResourceUUID = historyTokens.get(0);
      if (transferredResourceUUID != null) {
        Services services = new Services("Upload resource", "Upload");
        services.transferredResource(s -> s.getResource(transferredResourceUUID)).whenComplete((value, error) -> {
          if (value != null) {
            resource = value;
            callback.onSuccess(TransferUpload.this);
            updateUploadForm();
          } else if (error != null) {
            callback.onFailure(error);
          }
        });
      } else {
        HistoryUtils.newHistory(IngestTransfer.RESOLVER);
        callback.onSuccess(null);
      }
    }
  }

  protected void browseResolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
    isIngest = false;

    uploadMessage.getElement().setId("upload-message");
    uploadMessage.setHTML("<span class='success'>" + messages.uploadDoneMessage() + "</span>");
    uploadMessage.setVisible(false);

    if (historyTokens.size() >= 2) {
      aipId = historyTokens.get(0);
      representationId = historyTokens.get(1);
      if (historyTokens.size() >= 3) {
        folderPath = new ArrayList<>(historyTokens.subList(2, historyTokens.size() - 1));
        folderId = historyTokens.get(historyTokens.size() - 1);
      } else {
        folderPath = null;
        folderId = null;
      }

      callback.onSuccess(TransferUpload.this);
      updateUploadForm();
    }
  }

  private void updateUploadForm() {
    String uploadUrl = getUploadUrl();

    if (uploadUrl != null) {
      SafeHtml html = SafeHtmlUtils.fromSafeConstant("<form id='upload' method='post' action='" + uploadUrl
        + "' enctype='multipart/form-data'>" + "<div id='drop'><h4>" + messages.ingestTransferUploadDropHere()
        + "</h4><a>" + messages.ingestTransferUploadBrowseFiles() + "</a>" + "<input title='"
        + RodaConstants.API_PARAM_UPLOAD + "' type='file' name='" + "resource" + "' multiple='true' />" + " </div>"
        + "<input title='hiddenSubmit' type='submit' hidden/> </form>");

      uploadForm.setHTML(html);
      uploadList.setHTML(SafeHtmlUtils.fromSafeConstant("<ul id='upload-list'></ul>"));

      uploadForm.addDomHandler(new DragOverHandler() {
        @Override
        public void onDragOver(DragOverEvent event) {
          uploadForm.addStyleName(DRAGOVER);
        }
      }, DragOverEvent.getType());

      uploadForm.addDomHandler(new DragLeaveHandler() {
        @Override
        public void onDragLeave(DragLeaveEvent event) {
          uploadForm.removeStyleName(DRAGOVER);
        }
      }, DragLeaveEvent.getType());

      uploadForm.addDomHandler(new DropHandler() {
        @Override
        public void onDrop(DropEvent event) {
          uploadForm.removeStyleName(DRAGOVER);
        }
      }, DropEvent.getType());

      JavascriptUtils.runMiniUploadForm();
    } else {
      uploadForm.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }
  }

  @UiHandler("done")
  void buttonDoneHandler(ClickEvent e) {
    if (isIngest) {
      if (resource != null) {
        HistoryUtils.newHistory(IngestTransfer.RESOLVER, resource.getUUID());
      } else {
        HistoryUtils.newHistory(IngestTransfer.RESOLVER);
      }
    } else {
      if (folderId != null) {
        HistoryUtils.openBrowse(aipId, representationId, folderPath, folderId);
      } else {
        HistoryUtils.openBrowse(aipId, representationId);
      }
    }
  }
}
