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
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.BrowseTransferredResourceActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UpSalePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.search.TransferredResourceSearch;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ConfigurationManager;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class IngestTransfer extends Composite {

  @SuppressWarnings("unused")
  private static final String TRANSFERRED_RESOURCE_ID_SEPARATOR = "/";

  public static final String CARD_IDENTIFIER = "collapsable-ingest-transfer-card";

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
    public List<String> getHistoryPath() {
      return ListUtils.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "transfer";
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

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final boolean dropFolderActive = ConfigurationManager.getBoolean(false,
    RodaConstants.UI_SERVICE_DROPFOLDER_ACTIVE);
  private final TransferredResource resource;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static IngestTransfer getInstance() {
    if (instance == null) {
      instance = new IngestTransfer();
    }
    return instance;
  }

  @UiField
  FlowPanel ingestTransferDescription;

  @UiField
  SimplePanel ingestTransferPanel;

  @UiField(provided = true)
  TransferredResourceSearch resourceSearch;

  @UiField
  Button download;

  @UiField
  Label lastScanned;

  @UiField
  TitlePanel itemTitle;

  @UiField
  TitlePanel ingestTransferTitle;

  @UiField
  Label itemDates;

  @UiField
  NavigationToolbar<TransferredResource> navigationToolbar;

  @UiField
  BrowseTransferredResourceActionsToolbar objectToolbar;

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

    objectToolbar.setObjectAndBuild(resource, null, null);

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    draw();
  }

  private IngestTransfer() {
    this.resource = null;

    resourceSearch = new TransferredResourceSearch("IngestTransfer_transferredResources",
      new Filter(new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)),
      TransferredResourceActions.get(null), actionCallback);

    initWidget(uiBinder.createAndBindUi(this));
    UpSalePanel widgets = new UpSalePanel(messages.dropFolderInformationText(), messages.learnMore(),
      ConfigurationManager.getString(RodaConstants.UI_SERVICE_DROPFOLDER_URL), CARD_IDENTIFIER);
    ingestTransferPanel.setWidget(widgets);

    if (dropFolderActive) {
      ingestTransferPanel.setVisible(false);
    }

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    draw();
  }

  private void draw() {
    if (resource == null) {
      itemTitle.setVisible(false);
      ingestTransferTitle.setVisible(true);
      ingestTransferDescription.setVisible(true);
      itemDates.setText("");
      download.setVisible(false);
      navigationToolbar.setVisible(false);
      objectToolbar.setVisible(false);
      if (!dropFolderActive) {
        ingestTransferPanel.setVisible(JavascriptUtils.accessLocalStorage(CARD_IDENTIFIER));
      }
      lastScanned.setText("");
    } else {
      objectToolbar.setObjectAndBuild(resource, null, null);
      navigationToolbar.updateBreadcrumb(resource);
      navigationToolbar.withObject(resource).build();
      navigationToolbar.setVisible(true);
      ingestTransferPanel.setVisible(false);
      ingestTransferTitle.setVisible(false);
      ingestTransferDescription.setVisible(false);

      itemTitle.setVisible(true);
      if (resource.isFile()) {
        itemTitle
          .setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FILE, false));
      } else {
        itemTitle
          .setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
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

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      IngestTransfer ingestTransfer = new IngestTransfer();
      callback.onSuccess(ingestTransfer);
    } else if (historyTokens.get(0).equals(TransferUpload.INGEST_RESOLVER.getHistoryToken())) {
      TransferUpload.INGEST_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      ShowTransferredResource.getAndRefresh(historyTokens.get(0), callback);
    }
  }
}
