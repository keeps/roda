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
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.TransferredResourceActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
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
import com.google.gwt.user.client.ui.HTMLPanel;
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
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private TransferredResource resource;
  private ActionableWidgetBuilder<TransferredResource> actionableWidgetBuilder;

  @UiField
  Label ingestTransferTitle;

  @UiField
  FlowPanel ingestTransferDescription;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  TransferredResourceSearch resourceSearch;

  @UiField
  Button download;

  @UiField
  Label lastScanned;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  @UiField
  SimplePanel actionsSidebar;

  private IngestTransfer() {
    resourceSearch = new TransferredResourceSearch("IngestTransfer_transferredResources");
    resourceSearch.defaultFilters(new Filter(new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)));
    resourceSearch.getList().setActionable(TransferredResourceActions.get(null));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(TransferredResourceActions.get(null));

    initWidget(uiBinder.createAndBindUi(this));

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    actionableWidgetBuilder.withCallback(new NoAsyncCallback<Actionable.ActionImpact>() {

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        resourceSearch.getList().refresh();
      }

      @Override
      public void onSuccess(Actionable.ActionImpact impact) {
        if (Actionable.ActionImpact.UPDATED.equals(impact)) {
          if (resource != null) {
            updateView(resource.getUUID(), new NoAsyncCallback<>());
          } else {
            view();
          }
          resourceSearch.getList().refresh();
        } else if (Actionable.ActionImpact.DESTROYED.equals(impact)) {
          String parentUUID = resource != null ? resource.getParentUUID() : null;
          if (parentUUID != null) {
            HistoryUtils.newHistory(RESOLVER, parentUUID);
          } else {
            HistoryUtils.newHistory(RESOLVER);
          }
        }
      }
    });

    actionsSidebar
      .setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(TransferredResource.class)));
  }

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

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  protected void view(TransferredResource r) {
    resource = r;

    actionableWidgetBuilder.changeActionable(TransferredResourceActions.get(resource));
    actionsSidebar.setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(resource)));

    ingestTransferTitle.setVisible(false);
    ingestTransferDescription.setVisible(false);

    HTML itemIconHtmlPanel = new HTML(
      r.isFile() ? DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FILE, false)
        : DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(r.getName());
    itemDates.setText(messages.ingestTransferItemInfo(Humanize.formatDateTime(r.getCreationDate()),
      Humanize.readableFileSize(r.getSize())));
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");

    if (r.isFile()) {
      resourceSearch.setVisible(false);
      download.setVisible(true);
    } else {
      Filter filter = new Filter(
        new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, r.getRelativePath()));
      resourceSearch.defaultFilters(filter);
      resourceSearch.setVisible(true);
      download.setVisible(false);
    }

    breadcrumb.updatePath(BreadcrumbUtils.getTransferredResourceBreadcrumbs(r));
    breadcrumb.setVisible(true);

    lastScanned.setText(messages.ingestTransferLastScanned(resource.getLastScanDate()));
  }

  protected void view() {
    resource = null;

    actionableWidgetBuilder.changeActionable(TransferredResourceActions.get(null));
    actionsSidebar
      .setWidget(actionableWidgetBuilder.buildListWithObjects(new ActionableObject<>(TransferredResource.class)));

    ingestTransferTitle.setVisible(true);
    ingestTransferDescription.setVisible(true);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(messages.ingestAllTransferredPackages());
    itemDates.setText("");
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    resourceSearch.setVisible(true);
    download.setVisible(false);

    resourceSearch.defaultFilters(DEFAULT_FILTER);
    breadcrumb.setVisible(false);

    lastScanned.setText("");
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      view();
      callback.onSuccess(this);
    } else if (historyTokens.get(0).equals(TransferUpload.INGEST_RESOLVER.getHistoryToken())) {
      TransferUpload.INGEST_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      String transferredResourceUUID = historyTokens.get(0);
      if (transferredResourceUUID != null) {
        updateView(transferredResourceUUID, callback);
      } else {
        view();
        callback.onSuccess(this);
      }
    }
  }

  private void updateView(String transferredResourceUUID, AsyncCallback<Widget> callback) {
    BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), transferredResourceUUID,
      fieldsToReturn, new AsyncCallback<TransferredResource>() {

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof NotFoundException) {
            Dialogs.showInformationDialog(messages.ingestTransferNotFoundDialogTitle(),
              messages.ingestTransferNotFoundDialogMessage(), messages.ingestTransferNotFoundDialogButton(), false,
              new AsyncCallback<Void>() {

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
        public void onSuccess(TransferredResource r) {
          view(r);
          callback.onSuccess(IngestTransfer.this);
        }
      });
  }

  public SelectedItems<TransferredResource> getSelected() {
    SelectedItems<TransferredResource> selected = resourceSearch.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected) && resource != null) {
      selected = new SelectedItemsList<>(Arrays.asList(resource.getUUID()), TransferredResource.class.getName());
    }

    return selected;
  }

  @UiHandler("download")
  public void handleDownload(ClickEvent e) {
    if (resource != null) {
      SafeUri downloadUri = RestUtils.createTransferredResourceDownloadUri(resource.getUUID());
      Window.Location.assign(downloadUri.asString());
    }
  }
}
