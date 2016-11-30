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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowseFolder;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.HistoryUtils;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
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
      return ListUtils.concat(BrowseFolder.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static TransferUpload instance = null;

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

  interface MyUiBinder extends UiBinder<Widget, TransferUpload> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

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

  private TransferredResource resource;

  private String folderUUID;
  private String representationUUID;
  private String aipId;

  private boolean isIngest = true;

  @SuppressWarnings("unused")
  private HandlerRegistration handlerRegistration;

  protected boolean verified = false;

  private TransferUpload() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private String getUploadUrl() {
    String ret = null;

    if (isIngest) {
      if (resource == null) {
        // upload to root
        ret = RestUtils.createTransferredResourceUploadUri(null, LocaleInfo.getCurrentLocale().getLocaleName());
      } else if (resource != null && !resource.isFile()) {
        ret = RestUtils.createTransferredResourceUploadUri(resource.getUUID(),
          LocaleInfo.getCurrentLocale().getLocaleName());
      }
    } else {
      LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
      String details = selectedItems.getDetailsMessage();

      if (folderUUID == null) {
        // upload to root
        ret = RestUtils.createFileUploadUri(aipId, representationUUID, details);
      } else {
        ret = RestUtils.createFileUploadUri(folderUUID, details);
      }
    }

    return ret;
  }

  protected void onAttach() {
    verified = false;
    super.onAttach();
  }

  protected void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
    isIngest = true;

    if (historyTokens.size() == 0) {
      // Upload to root
      resource = null;
      callback.onSuccess(TransferUpload.this);
      updateUploadForm();
    } else if (historyTokens.size() == 1) {
      // Upload to directory
      String transferredResourceUUID = historyTokens.get(0);
      if (transferredResourceUUID != null) {
        BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), transferredResourceUUID,
          new AsyncCallback<TransferredResource>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(TransferredResource r) {
              resource = r;
              callback.onSuccess(TransferUpload.this);
              updateUploadForm();
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

    if (historyTokens.size() == 2 || historyTokens.size() == 3) {
      aipId = historyTokens.get(0);
      representationUUID = historyTokens.get(1);
      folderUUID = historyTokens.size() == 3 ? historyTokens.get(2) : null;
      callback.onSuccess(TransferUpload.this);
      updateUploadForm();
    }
  }

  private void updateUploadForm() {
    String uploadUrl = getUploadUrl();

    if (uploadUrl != null) {
      SafeHtml html = SafeHtmlUtils.fromSafeConstant("<form id='upload' method='post' action='" + uploadUrl
        + "' enctype='multipart/form-data'>" + "<div id='drop'><h4>" + messages.ingestTransferUploadDropHere()
        + "</h4><a>" + messages.ingestTransferUploadBrowseFiles() + "</a>" + "<input type='file' name='"
        + RodaConstants.API_PARAM_UPLOAD + "' multiple='true' />" + "</div>" + "</form>");

      uploadForm.setHTML(html);
      uploadList.setHTML(SafeHtmlUtils.fromSafeConstant("<ul id='upload-list'></ul>"));

      uploadForm.addDomHandler(new DragOverHandler() {

        @Override
        public void onDragOver(DragOverEvent event) {
          uploadForm.addStyleName("dragover");
        }
      }, DragOverEvent.getType());

      uploadForm.addDomHandler(new DragLeaveHandler() {

        @Override
        public void onDragLeave(DragLeaveEvent event) {
          uploadForm.removeStyleName("dragover");
        }
      }, DragLeaveEvent.getType());

      uploadForm.addDomHandler(new DropHandler() {

        @Override
        public void onDrop(DropEvent event) {
          uploadForm.removeStyleName("dragover");
        }
      }, DropEvent.getType());

      JavascriptUtils.runMiniUploadForm();
    } else {
      uploadForm.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }
  }

  @UiHandler("done")
  void buttonDoneHandler(ClickEvent e) {
    historyBack();
  }

  void historyBack() {
    if (isIngest) {
      if (resource != null) {
        HistoryUtils.newHistory(IngestTransfer.RESOLVER, resource.getUUID());
      } else {
        HistoryUtils.newHistory(IngestTransfer.RESOLVER);
      }
    } else {
      if (folderUUID != null) {
        HistoryUtils.newHistory(BrowseFolder.RESOLVER, aipId, representationUUID, folderUUID);
      } else {
        HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, representationUUID);
      }
    }
  }
}
