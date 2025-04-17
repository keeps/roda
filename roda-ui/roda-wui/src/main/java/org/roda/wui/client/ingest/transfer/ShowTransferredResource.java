package org.roda.wui.client.ingest.transfer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.tabs.BrowseTransferredResourceTabs;
import org.roda.wui.client.common.BrowseTransferredResourceActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

public class ShowTransferredResource extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  TitlePanel title;
  @UiField
  BrowseTransferredResourceActionsToolbar objectToolbar;
  @UiField
  NavigationToolbar<TransferredResource> navigationToolbar;
  @UiField
  FocusPanel keyboardFocus;
  @UiField
  BrowseTransferredResourceTabs browseTab;

  public static void getAndRefresh(String uuid, AsyncCallback<Widget> callback) {
    Services services = new Services("Get Transferred resource", "get");
    services.transferredResource(s -> s.getResource(uuid)).whenComplete((value, error) -> {
      if (error != null) {
        if (error instanceof NotFoundException) {
          Toast.showError(messages.notFoundError());
          HistoryUtils.newHistory(IngestTransfer.RESOLVER);
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        }
      } else {
        ShowTransferredResource browseTransferredResource = new ShowTransferredResource(value);
        callback.onSuccess(browseTransferredResource);
      }
    });
  }

  public ShowTransferredResource(TransferredResource resource) {
    initWidget(uiBinder.createAndBindUi(this));
    objectToolbar.setObjectAndBuild(resource, null, null);
    navigationToolbar.updateBreadcrumb(resource);
    buildTitle(resource);

    keyboardFocus.setFocus(true);
    this.keyboardFocus.addStyleName("browse browse-file browse_main_panel");

    // TABS
    browseTab.init(resource);
  }

  private void buildTitle(TransferredResource resource) {
    if (resource.isFile()) {
      title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FILE, false));
    } else {
      title.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
    }
    title.setText(resource.getName());

  }

  interface MyUiBinder extends UiBinder<Widget, ShowTransferredResource> {
  }
}
