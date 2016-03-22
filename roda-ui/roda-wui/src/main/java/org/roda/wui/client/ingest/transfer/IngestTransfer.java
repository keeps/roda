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
import java.util.HashSet;
import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.BasicSearch;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.SelectedItems;
import org.roda.wui.client.common.lists.SelectedItemsSet;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.utils.AsyncRequestUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.ingest.process.CreateJob;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class IngestTransfer extends Composite {

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
      return Tools.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestTransfer instance = null;

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

  private static final String TOP_ICON = "<span class='roda-logo'></span>";

  public static final SafeHtml FOLDER_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
  public static final SafeHtml FILE_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");

  private static final Filter DEFAULT_FILTER = new Filter(
    new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID));

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  private TransferredResource resource;

  @UiField
  Label ingestTransferTitle;

  @UiField
  FlowPanel ingestTransferDescription;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  BasicSearch basicSearch;

  @UiField
  Button download;

  @UiField(provided = true)
  TransferredResourceList transferredResourceList;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  // BUTTONS
  @UiField
  Button uploadFiles;

  @UiField
  Button createFolder;

  @UiField
  Button remove;

  @UiField
  Button startIngest;

  private IngestTransfer() {
    Facets facets = null;

    transferredResourceList = new TransferredResourceList(DEFAULT_FILTER, facets, messages.ingestTransferList(), true);

    basicSearch = new BasicSearch(DEFAULT_FILTER, RodaConstants.TRANSFERRED_RESOURCE_NAME,
      messages.ingestTransferSearchPlaceHolder(), false, false);

    initWidget(uiBinder.createAndBindUi(this));

    ingestTransferDescription.add(new HTMLWidgetWrapper("IngestTransferDescription.html"));

    transferredResourceList.addValueChangeHandler(new ValueChangeHandler<IndexResult<TransferredResource>>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<TransferredResource>> event) {
        updateVisibles();
      }
    });

    transferredResourceList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        TransferredResource r = transferredResourceList.getSelectionModel().getSelectedObject();
        if (r != null) {
          basicSearch.clearSearchInputBox();
          Tools.newHistory(RESOLVER, getPathFromTransferredResourceId(r.getId()));
        }
      }
    });

    transferredResourceList.addCheckboxSelectionListener(new CheckboxSelectionListener<TransferredResource>() {

      @Override
      public void onSelectionChange(SelectedItems<TransferredResource> selected) {
        boolean empty = SelectedItemsUtils.isEmpty(selected);

        remove.setText(empty ? messages.ingestTransferButtonRemoveWholeFolder()
          : messages.ingestTransferButtonRemoveSelectedItems());
        startIngest.setText(empty ? messages.ingestTransferButtonIngestWholeFolder()
          : messages.ingestTransferButtonIngestSelectedItems());
        updateVisibles();
      }

    });
  }

  protected void view(TransferredResource r) {
    resource = r;

    ingestTransferTitle.setVisible(false);
    ingestTransferDescription.setVisible(false);

    HTML itemIconHtmlPanel = new HTML(r.isFile() ? FILE_ICON : FOLDER_ICON);
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(r.getName());
    itemDates.setText(messages.ingestTransferItemInfo(r.getCreationDate(), Humanize.readableFileSize(r.getSize())));
    itemTitle.removeStyleName("browseTitle-allCollections");
    itemIcon.getParent().removeStyleName("browseTitle-allCollections-wrapper");

    if (r.isFile()) {
      // TODO add big download button
      basicSearch.setVisible(false);
      transferredResourceList.setVisible(false);
      download.setVisible(true);
    } else {

      Filter filter = new Filter(
        new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, r.getRelativePath()));
      transferredResourceList.setFilter(filter);

      basicSearch.setVisible(true);
      transferredResourceList.setVisible(true);
      download.setVisible(false);
    }
    breadcrumb.updatePath(getBreadcrumbs(r));
    breadcrumb.setVisible(true);

    updateVisibles();
  }

  protected void view() {
    resource = null;

    ingestTransferTitle.setVisible(true);
    ingestTransferDescription.setVisible(true);

    HTML itemIconHtmlPanel = new HTML(TOP_ICON);
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText("All transferred packages");
    itemDates.setText("");
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    basicSearch.setVisible(true);
    transferredResourceList.setVisible(true);
    download.setVisible(false);

    transferredResourceList.setFilter(DEFAULT_FILTER);
    breadcrumb.setVisible(false);

    updateVisibles();
  }

  private List<BreadcrumbItem> getBreadcrumbs(TransferredResource r) {
    List<BreadcrumbItem> ret = new ArrayList<BreadcrumbItem>();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), RESOLVER.getHistoryPath()));
    if (r != null) {
      List<String> pathBuilder = new ArrayList<String>();
      pathBuilder.addAll(RESOLVER.getHistoryPath());

      String[] parts = r.getId().split(TRANSFERRED_RESOURCE_ID_SEPARATOR);
      for (String part : parts) {
        SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(part);
        pathBuilder.add(part);
        List<String> path = new ArrayList<>(pathBuilder);
        ret.add(new BreadcrumbItem(breadcrumbLabel, path));
      }
    }

    return ret;
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      view();
      callback.onSuccess(this);
    } else if (historyTokens.size() >= 1
      && historyTokens.get(0).equals(IngestTransferUpload.RESOLVER.getHistoryToken())) {
      IngestTransferUpload.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else {
      String transferredResourceId = getTransferredResourceIdFromPath(historyTokens);
      if (transferredResourceId != null) {
        BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), transferredResourceId,
          new AsyncCallback<TransferredResource>() {

            @Override
            public void onFailure(Throwable caught) {
              if (caught instanceof NotFoundException) {
                Dialogs.showInformationDialog(messages.ingestTransferNotFoundDialogTitle(),
                  messages.ingestTransferNotFoundDialogMessage(), messages.ingestTransferNotFoundDialogButton(),
                  new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Tools.newHistory(IngestTransfer.RESOLVER);
                  }
                });
              } else {
                AsyncRequestUtils.defaultFailureTreatment(caught);
                Tools.newHistory(IngestTransfer.RESOLVER);
              }

              callback.onSuccess(null);
            }

            @Override
            public void onSuccess(TransferredResource r) {
              view(r);
              callback.onSuccess(IngestTransfer.this);
            }
          });
      } else {
        view();
        callback.onSuccess(this);
      }

    }

  }

  public static String getTransferredResourceIdFromPath(List<String> historyTokens) {
    String ret;
    if (historyTokens.size() > 0) {
      ret = Tools.join(historyTokens, TRANSFERRED_RESOURCE_ID_SEPARATOR);
    } else {
      ret = null;
    }

    return ret;
  }

  public static List<String> getPathFromTransferredResourceId(String transferredResourceId) {
    return Arrays.asList(transferredResourceId.split(TRANSFERRED_RESOURCE_ID_SEPARATOR));
  }

  protected void updateVisibles() {
    uploadFiles.setEnabled(resource == null || !resource.isFile());
    createFolder.setEnabled(resource == null || !resource.isFile());

    boolean empty = SelectedItemsUtils.isEmpty(transferredResourceList.getSelected());

    remove.setEnabled(resource != null || !empty);
    startIngest.setEnabled(resource != null || !empty);
  }

  @UiHandler("uploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {
    if (resource != null) {
      Tools.newHistory(IngestTransferUpload.RESOLVER, getPathFromTransferredResourceId(resource.getId()));
    } else {
      Tools.newHistory(IngestTransferUpload.RESOLVER);
    }
  }

  @UiHandler("createFolder")
  void buttonCreateFolderHandler(ClickEvent e) {
    Dialogs.showPromptDialog(messages.ingestTransferCreateFolderTitle(), messages.ingestTransferCreateFolderMessage(),
      RegExp.compile(".+"), messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String folderName) {
          String parent = resource != null ? resource.getId() : null;
          BrowserService.Util.getInstance().createTransferredResourcesFolder(parent, folderName,
            new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncRequestUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(String newResourceId) {
              Tools.newHistory(RESOLVER, getPathFromTransferredResourceId(newResourceId));
            }
          });
        }
      });
  }

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {

    final SelectedItems<TransferredResource> selected = transferredResourceList.getSelected();

    if (SelectedItemsUtils.isEmpty(selected)) {
      // Remove the whole folder

      if (resource != null) {
        Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
          messages.ingestTransferRemoveFolderConfirmDialogMessage(resource.getName()),
          messages.ingestTransferRemoveFolderConfirmDialogCancel(),
          messages.ingestTransferRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncRequestUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                SelectedItems<TransferredResource> s = new SelectedItemsSet<>(new HashSet<>(Arrays.asList(resource)));
                BrowserService.Util.getInstance().removeTransferredResources(s, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncRequestUtils.defaultFailureTreatment(caught);
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.ingestTransferRemoveSuccessTitle(),
                      messages.ingestTransferRemoveSuccessMessage(1L));
                    Tools.newHistory(RESOLVER, getPathFromTransferredResourceId(resource.getParentId()));
                  }
                });
              }
            }
          });
      }
      // else do nothing

    } else {
      // Remove all selected resources

      SelectedItemsUtils.size(TransferredResource.class, selected, new AsyncCallback<Long>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncRequestUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final Long size) {
          Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
            messages.ingestTransferRemoveSelectedConfirmDialogMessage(size),
            messages.ingestTransferRemoveFolderConfirmDialogCancel(),
            messages.ingestTransferRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              AsyncRequestUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().removeTransferredResources(selected, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncRequestUtils.defaultFailureTreatment(caught);
                    transferredResourceList.refresh();
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.ingestTransferRemoveSuccessTitle(),
                      messages.ingestTransferRemoveSuccessMessage(size));
                    transferredResourceList.refresh();
                  }
                });
              }
            }
          });
        }
      });

    }

  }

  @UiHandler("startIngest")
  void buttonStartIngestHandler(ClickEvent e) {
    Tools.newHistory(CreateJob.RESOLVER);
  }

  public SelectedItems<TransferredResource> getSelected() {
    SelectedItems<TransferredResource> selected = transferredResourceList.getSelected();
    if (selected instanceof SelectedItemsSet) {
      SelectedItemsSet<?> selectedset = (SelectedItemsSet<?>) selected;
      if (selectedset.getSet().isEmpty() && resource != null) {
        selected = new SelectedItemsSet<>(new HashSet<>(Arrays.asList(resource)));
      }
    }

    return selected;
  }

  @UiHandler("download")
  public void handleDownload(ClickEvent e) {
    if (resource != null) {
      SafeUri downloadUri = RestUtils.createTransferredResourceDownloadUri(resource.getId());
      Window.Location.assign(downloadUri.asString());
    }
  }
}
