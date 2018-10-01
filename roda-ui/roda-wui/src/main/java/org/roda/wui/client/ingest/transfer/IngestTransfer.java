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

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.search.TransferredResourceSearch;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class IngestTransfer extends Composite {

  @SuppressWarnings("unused")
  private static final String TRANSFERRED_RESOURCE_ID_SEPARATOR = "/";

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        callback.onSuccess(new IngestTransfer());
      } else if (historyTokens.get(0).equals(TransferUpload.INGEST_RESOLVER.getHistoryToken())) {
        TransferUpload.INGEST_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        String transferredResourceUUID = historyTokens.get(0);
        if (transferredResourceUUID != null) {
          BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), transferredResourceUUID,
            fieldsToReturn, new AsyncCallback<TransferredResource>() {

              @Override
              public void onFailure(Throwable caught) {
                if (caught instanceof NotFoundException) {
                  Dialogs.showInformationDialog(messages.ingestTransferNotFoundDialogTitle(),
                    messages.ingestTransferNotFoundDialogMessage(), messages.ingestTransferNotFoundDialogButton(),
                    false, new AsyncCallback<Void>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        // do nothing
                      }

                      @Override
                      public void onSuccess(Void result) {
                        HistoryUtils.newHistory(IngestTransfer.RESOLVER);
                      }
                    });
                } else {
                  AsyncCallbackUtils.defaultFailureTreatment(caught);
                  HistoryUtils.newHistory(IngestTransfer.RESOLVER);
                }

                callback.onSuccess(null);
              }

              @Override
              public void onSuccess(TransferredResource resource) {
                callback.onSuccess(new IngestTransfer(resource));
              }
            });
        } else {
          callback.onSuccess(new IngestTransfer());
        }
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "transfer";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestTransfer instance = null;

  private static final Filter DEFAULT_FILTER = new Filter(
    new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID));

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.TRANSFERRED_RESOURCE_NAME, RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID,
    RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID, RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH,
    RodaConstants.TRANSFERRED_RESOURCE_SIZE, RodaConstants.TRANSFERRED_RESOURCE_DATE,
    RodaConstants.TRANSFERRED_RESOURCE_ISFILE);

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final TransferredResource resource;

  @UiField
  Label ingestTransferTitle;

  @UiField
  FlowPanel ingestTransferDescription;

  @UiField(provided = true)
  TransferredResourceSearch resourceSearch;

  @UiField
  Button download;

  @UiField
  Label lastScanned;

  @UiField
  HTML itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  @UiField
  NavigationToolbar<TransferredResource> navigationToolbar;

  private NoAsyncCallback<Actionable.ActionImpact> actionCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onFailure(Throwable caught) {
      super.onFailure(caught);
      resourceSearch.refresh();
    }

    @Override
    public void onSuccess(Actionable.ActionImpact impact) {
      if (Actionable.ActionImpact.UPDATED.equals(impact)) {
        if (resource != null) {
          HistoryUtils.newHistory(RESOLVER, resource.getUUID());
        } else {
          HistoryUtils.newHistory(RESOLVER);
        }
      } else if (Actionable.ActionImpact.DESTROYED.equals(impact)) {
        String parentUUID = resource != null ? resource.getParentUUID() : null;
        if (parentUUID != null) {
          HistoryUtils.newHistory(RESOLVER, parentUUID);
        } else {
          HistoryUtils.newHistory(RESOLVER);
        }
      }
    }
  };

  private IngestTransfer(final TransferredResource resource) {
    this.resource = resource;

    if (resource.isFile()) {
      resourceSearch = new TransferredResourceSearch();
    } else {
      resourceSearch = new TransferredResourceSearch("IngestTransfer_transferredResources",
        new Filter(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, resource.getRelativePath())),
        TransferredResourceActions.get(resource), actionCallback);
    }

    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.setHeader(messages.oneOfAObject(TransferredResource.class.getName()));

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    draw();
  }

  private IngestTransfer() {
    this.resource = null;

    resourceSearch = new TransferredResourceSearch("IngestTransfer_transferredResources",
      new Filter(new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)),
      TransferredResourceActions.get(null), actionCallback);

    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.setHeader(messages.oneOfAObject(TransferredResource.class.getName()));

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    draw();
  }

  private void draw() {
    if (resource == null) {
      ingestTransferTitle.setVisible(true);
      ingestTransferDescription.setVisible(true);

      itemIcon.setHTML(DescriptionLevelUtils.getTopIconSafeHtml());
      itemTitle.setText(messages.ingestAllTransferredPackages());
      itemDates.setText("");
      download.setVisible(false);
      navigationToolbar.setVisible(false);

      lastScanned.setText("");
    } else {
      navigationToolbar.updateBreadcrumb(resource);
      navigationToolbar.withObject(resource).build();
      navigationToolbar.setVisible(true);

      ingestTransferTitle.setVisible(false);
      ingestTransferDescription.setVisible(false);

      if (resource.isFile()) {
        itemIcon
          .setHTML(DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FILE, false));
      } else {
        itemIcon
          .setHTML(DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
      }

      itemTitle.setText(resource.getName());
      itemDates.setText(messages.ingestTransferItemInfo(Humanize.formatDateTime(resource.getCreationDate()),
        Humanize.readableFileSize(resource.getSize())));

      download.setVisible(resource.isFile());
      lastScanned.setText(messages.ingestTransferLastScanned(resource.getLastScanDate()));
    }
  }

  @UiHandler("download")
  public void handleDownload(ClickEvent e) {
    if (resource != null) {
      SafeUri downloadUri = RestUtils.createTransferredResourceDownloadUri(resource.getUUID());
      Window.Location.assign(downloadUri.asString());
    }
  }
}
