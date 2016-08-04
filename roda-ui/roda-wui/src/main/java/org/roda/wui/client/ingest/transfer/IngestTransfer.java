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

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.NotSimpleFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.CreateJob;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.SelectTransferResourceDialog;
import org.roda.wui.client.common.lists.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.SelectedItemsUtils;
import org.roda.wui.client.common.lists.TransferredResourceList;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

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

  public static final SafeHtml FOLDER_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
  public static final SafeHtml FILE_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");

  private static final Filter DEFAULT_FILTER = new Filter(
    new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID));

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private TransferredResource resource;

  @UiField
  Label ingestTransferTitle;

  @UiField
  FlowPanel ingestTransferDescription;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField
  Button download;

  @UiField(provided = true)
  TransferredResourceList transferredResourceList;

  @UiField
  Label lastScanned;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  // BUTTONS
  @UiField
  Button refresh;

  @UiField
  Button uploadFiles;

  @UiField
  Button createFolder;

  @UiField
  Button remove;

  @UiField
  Button startIngest;

  @UiField
  Button rename;

  @UiField
  Button move;

  private IngestTransfer() {
    Facets facets = null;
    transferredResourceList = new TransferredResourceList(Filter.NULL, facets, messages.ingestTransferList(), true);

    searchPanel = new SearchPanel(Filter.NULL, RodaConstants.TRANSFERRED_RESOURCE_SEARCH,
      messages.ingestTransferSearchPlaceHolder(), false, false);
    searchPanel.setList(transferredResourceList);
    searchPanel.setDefaultFilterIncremental(true);

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
          searchPanel.clearSearchInputBox();
          Tools.newHistory(RESOLVER, r.getUUID());
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

        if (selected instanceof SelectedItemsList) {
          SelectedItemsList selectedList = (SelectedItemsList) selected;
          if (selectedList.getIds().size() > 0) {
            move.setEnabled(true);
            if (selectedList.getIds().size() == 1) {
              rename.setEnabled(true);
            } else {
              rename.setEnabled(false);
            }
          } else {
            rename.setEnabled(false);
            move.setEnabled(false);
          }
        }
      }

    });

    rename.setEnabled(false);
    move.setEnabled(false);
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
      searchPanel.setVisible(false);
      transferredResourceList.setVisible(false);
      download.setVisible(true);
      rename.setEnabled(true);
      move.setEnabled(true);
    } else {

      Filter filter = new Filter(
        new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, r.getRelativePath()));
      transferredResourceList.setFilter(filter);
      searchPanel.setDefaultFilter(filter);

      searchPanel.setVisible(true);
      transferredResourceList.setVisible(true);
      download.setVisible(false);
      rename.setEnabled(false);
      move.setEnabled(false);
    }

    breadcrumb.updatePath(getBreadcrumbs(r));
    breadcrumb.setVisible(true);

    lastScanned.setText(messages.ingestTransferLastScanned(resource.getLastScanDate()));
    refresh.setTitle(messages.ingestTransferLastScanned(resource.getLastScanDate()));

    updateVisibles();
  }

  protected void view() {
    resource = null;

    ingestTransferTitle.setVisible(true);
    ingestTransferDescription.setVisible(true);

    HTMLPanel itemIconHtmlPanel = DescriptionLevelUtils.getTopIconHTMLPanel();
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(messages.ingestAllTransferredPackages());
    itemDates.setText("");
    itemTitle.addStyleName("browseTitle-allCollections");
    itemIcon.getParent().addStyleName("browseTitle-allCollections-wrapper");

    searchPanel.setVisible(true);
    transferredResourceList.setVisible(true);
    download.setVisible(false);

    transferredResourceList.setFilter(DEFAULT_FILTER);
    searchPanel.setDefaultFilter(DEFAULT_FILTER);
    breadcrumb.setVisible(false);

    lastScanned.setText("");
    refresh.setTitle("");

    updateVisibles();
  }

  private List<BreadcrumbItem> getBreadcrumbs(TransferredResource r) {
    List<BreadcrumbItem> ret = new ArrayList<BreadcrumbItem>();

    ret.add(new BreadcrumbItem(DescriptionLevelUtils.getTopIconSafeHtml(), RESOLVER.getHistoryPath()));
    if (r != null) {

      // add parent
      if (r.getParentUUID() != null) {
        List<String> path = new ArrayList<String>();
        path.addAll(RESOLVER.getHistoryPath());
        path.add(r.getParentUUID());
        SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(r.getParentId());
        ret.add(new BreadcrumbItem(breadcrumbLabel, path));
      }

      // add self
      List<String> path = new ArrayList<String>();
      path.addAll(RESOLVER.getHistoryPath());
      path.add(r.getUUID());
      SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(r.getName());
      ret.add(new BreadcrumbItem(breadcrumbLabel, path));
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
      String transferredResourceUUID = historyTokens.get(0);
      if (transferredResourceUUID != null) {
        BrowserService.Util.getInstance().retrieve(TransferredResource.class.getName(), transferredResourceUUID,
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
                AsyncCallbackUtils.defaultFailureTreatment(caught);
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

  protected void updateVisibles() {
    uploadFiles.setEnabled(resource == null || !resource.isFile());
    createFolder.setEnabled(resource == null || !resource.isFile());

    boolean empty = SelectedItemsUtils.isEmpty(transferredResourceList.getSelected());

    remove.setEnabled(resource != null || !empty);
    startIngest.setEnabled(resource != null || !empty);
  }

  @UiHandler("refresh")
  void buttonRefreshHandler(ClickEvent e) {
    String uuid = resource != null ? resource.getUUID() : null;
    refresh.setEnabled(false);

    Toast.showInfo(messages.dialogRefresh(), messages.updateIsBeginning());
    BrowserService.Util.getInstance().transferScanRequestUpdate(uuid, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof IsStillUpdatingException) {
          Toast.showInfo(messages.dialogRefresh(), messages.updateIsCurrentlyRunning());
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        transferredResourceList.refresh();
        refresh.setEnabled(true);
      }

      @Override
      public void onSuccess(Void result) {
        Toast.showInfo(messages.dialogRefresh(), messages.updatedFilesUnderFolder());
        transferredResourceList.refresh();
        refresh.setEnabled(true);
      }
    });
  }

  @UiHandler("uploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {
    if (resource != null) {
      Tools.newHistory(IngestTransferUpload.RESOLVER, resource.getUUID());
    } else {
      Tools.newHistory(IngestTransferUpload.RESOLVER);
    }
  }

  @UiHandler("createFolder")
  void buttonCreateFolderHandler(ClickEvent e) {
    Dialogs.showPromptDialog(messages.ingestTransferCreateFolderTitle(), messages.ingestTransferCreateFolderMessage(),
      null, RegExp.compile(".+"), messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String folderName) {
          String parent = resource != null ? resource.getUUID() : null;
          BrowserService.Util.getInstance().createTransferredResourcesFolder(parent, folderName,
            new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(String newResourceUUID) {
                Tools.newHistory(RESOLVER, newResourceUUID);
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
              AsyncCallbackUtils.defaultFailureTreatment(caught);
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                SelectedItems<TransferredResource> s = new SelectedItemsList<TransferredResource>(
                  Arrays.asList(resource.getUUID()), TransferredResource.class.getName());
                BrowserService.Util.getInstance().removeTransferredResources(s, new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    AsyncCallbackUtils.defaultFailureTreatment(caught);
                  }

                  @Override
                  public void onSuccess(Void result) {
                    Toast.showInfo(messages.ingestTransferRemoveSuccessTitle(),
                      messages.ingestTransferRemoveSuccessMessage(1L));
                    Tools.newHistory(RESOLVER, resource.getParentUUID());
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
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(final Long size) {
          Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
            messages.ingestTransferRemoveSelectedConfirmDialogMessage(size),
            messages.ingestTransferRemoveFolderConfirmDialogCancel(),
            messages.ingestTransferRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

              @Override
              public void onFailure(Throwable caught) {
                AsyncCallbackUtils.defaultFailureTreatment(caught);
              }

              @Override
              public void onSuccess(Boolean confirmed) {
                if (confirmed) {
                  BrowserService.Util.getInstance().removeTransferredResources(selected, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      AsyncCallbackUtils.defaultFailureTreatment(caught);
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
    Tools.newHistory(CreateJob.RESOLVER, "ingest");
  }

  public SelectedItems getSelected() {
    SelectedItems selected = transferredResourceList.getSelected();
    if (selected instanceof SelectedItemsList) {
      SelectedItemsList selectedset = (SelectedItemsList) selected;

      if (SelectedItemsUtils.isEmpty(selectedset) && resource != null) {
        selected = new SelectedItemsList(Arrays.asList(resource.getUUID()), TransferredResource.class.getName());
      }
    }

    return selected;
  }

  public void refreshList() {
    transferredResourceList.refresh();
  }

  @UiHandler("download")
  public void handleDownload(ClickEvent e) {
    if (resource != null) {
      SafeUri downloadUri = RestUtils.createTransferredResourceDownloadUri(resource.getUUID());
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("rename")
  void buttonRenameHandler(ClickEvent e) {
    final String transferredResourceId;

    if (resource.isFile()) {
      transferredResourceId = resource.getUUID();
    } else {
      if (getSelected() instanceof SelectedItemsList) {
        SelectedItemsList resourceList = (SelectedItemsList) getSelected();
        transferredResourceId = (String) resourceList.getIds().get(0);
      } else {
        return;
      }
    }

    Dialogs.showPromptDialog(messages.renameTransferredResourcesDialogTitle(), null, messages.renameSIPPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
        }

        @Override
        public void onSuccess(String result) {
          BrowserService.Util.getInstance().renameTransferredResource(transferredResourceId, result,
            new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
              }

              @Override
              public void onSuccess(String result) {
                Toast.showInfo(messages.dialogSuccess(), messages.renameSIPSuccessful());
                Tools.newHistory(IngestTransfer.RESOLVER, result);
              }
            });
        }
      });
  }

  @UiHandler("move")
  void buttonMoveHandler(ClickEvent e) {
    BrowserService.Util.getInstance().getSelectedTransferredResource(getSelected(),
      new AsyncCallback<List<TransferredResource>>() {

        @Override
        public void onFailure(Throwable caught) {
          Toast.showInfo(messages.dialogFailure(), messages.moveSIPFailed());
        }

        @Override
        public void onSuccess(List<TransferredResource> result) {
          doTransferredResourceMove(result);
        }

      });
  }

  private void doTransferredResourceMove(List<TransferredResource> resources) {

    Filter filter = new Filter();

    if (resource != null) {
      boolean isFile = resource.isFile();

      if (isFile) {
        filter
          .add(new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_UUID, resources.get(0).getParentUUID()));
      } else {
        filter.add(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, Boolean.FALSE.toString()));

        for (TransferredResource resource : resources) {
          filter.add(new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_UUID, resource.getUUID()));
          filter.add(
            new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, resource.getRelativePath()));
          filter.add(new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_UUID, resource.getParentUUID()));
        }
      }
    } else {
      filter.add(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, Boolean.FALSE.toString()));

      for (TransferredResource resource : resources) {
        filter.add(new NotSimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_UUID, resource.getUUID()));
      }
    }

    SelectTransferResourceDialog dialog = new SelectTransferResourceDialog(messages.selectParentTitle(), filter);
    dialog.setEmptyParentButtonVisible(true);
    dialog.showAndCenter();
    dialog.addValueChangeHandler(new ValueChangeHandler<TransferredResource>() {

      @Override
      public void onValueChange(ValueChangeEvent<TransferredResource> event) {
        final TransferredResource transferredResource = event.getValue();

        BrowserService.Util.getInstance().moveTransferredResource(getSelected(), transferredResource,
          new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              Toast.showInfo(messages.dialogFailure(), messages.moveSIPFailed());
            }

            @Override
            public void onSuccess(String result) {
              Toast.showInfo(messages.dialogSuccess(), messages.moveSIPSuccessful());
              if (resource != null && resource.isFile()) {
                Tools.newHistory(IngestTransfer.RESOLVER, result);
              } else {
                transferredResourceList.refresh();
              }
            }
          });
      }
    });
  }
}
