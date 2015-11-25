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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.TransferredResource;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.Dialogs;
import org.roda.wui.client.common.TransferredResourceList;
import org.roda.wui.client.common.TransferredResourceList.CheckboxSelectionListener;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.MessagePopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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

  public static final String TOP_ICON = "<i class='fa fa-circle-o'></i>";
  public static final SafeHtml FOLDER_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");
  public static final SafeHtml FILE_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-file-o'></i>");

  private static final Filter DEFAULT_FILTER = new Filter(
    new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENTPATH));

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  private TransferredResource resource;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  TransferredResourceList transferredResourceList;

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField
  Label itemDates;

  // FILTERS
  @UiField
  FlowPanel filtersPanel;

  @UiField(provided = true)
  FlowPanel facetOwner;

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
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.TRANSFERRED_RESOURCE_OWNER));

    // TODO externalise strings
    transferredResourceList = new TransferredResourceList(DEFAULT_FILTER, facets, "Transferred resources list");

    facetOwner = new FlowPanel();
    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.TRANSFERRED_RESOURCE_OWNER, facetOwner);
    FacetUtils.bindFacets(transferredResourceList, facetPanels, true);

    initWidget(uiBinder.createAndBindUi(this));

    transferredResourceList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        TransferredResource r = transferredResourceList.getSelectionModel().getSelectedObject();
        if (r != null && !r.isFile()) {
          Tools.newHistory(RESOLVER, getPathFromTransferredResourceId(r.getId()));
        } else if (r != null && r.isFile()) {
          // disable selection
          transferredResourceList.getSelectionModel().clear();
        }
      }
    });

    transferredResourceList.addValueChangeHandler(new ValueChangeHandler<IndexResult<TransferredResource>>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexResult<TransferredResource>> event) {
        boolean visible = event.getValue().getTotalCount() > 0;
        // transferredResourceList.setVisible(visible);
        filtersPanel.setVisible(visible);
      }
    });

    transferredResourceList.addCheckboxSelectionListener(new CheckboxSelectionListener() {

      @Override
      public void onSelectionChange(Set<TransferredResource> selected) {
        remove.setText(selected.isEmpty() ? messages.ingestTransferButtonRemoveWholeFolder()
          : messages.ingestTransferButtonRemoveSelectedItems());
        startIngest.setText(selected.isEmpty() ? messages.ingestTransferButtonIngestWholeFolder()
          : messages.ingestTransferButtonIngestSelectedItems());
      }
    });

  }

  protected void view(TransferredResource r) {
    resource = r;

    HTML itemIconHtmlPanel = new HTML(r.isFile() ? FILE_ICON : FOLDER_ICON);
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText(r.getName());
    itemDates.setText(
      messages.ingestTransferItemInfo(r.getCreationDate(), Humanize.readableFileSize(r.getSize()), r.getOwner()));

    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENTPATH, r.getRelativePath()),
      new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_OWNER, r.getOwner()));
    transferredResourceList.setFilter(filter);

    breadcrumb.updatePath(getBreadcrumbs(r));
    breadcrumb.setVisible(true);

    updateVisibles();
  }

  protected void view() {
    resource = null;

    HTML itemIconHtmlPanel = new HTML(TOP_ICON);
    itemIconHtmlPanel.addStyleName("browseItemIcon-all");

    itemIcon.setWidget(itemIconHtmlPanel);
    itemTitle.setText("All transferred packages");
    itemDates.setText("");

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
    } else if (historyTokens.size() > 1
      && historyTokens.get(0).equals(IngestTransferUpload.RESOLVER.getHistoryToken())) {
      IngestTransferUpload.RESOLVER.resolve(Tools.tail(historyTokens), callback);
    } else {
      String transferredResourceId = getTransferredResourceIdFromPath(historyTokens);
      if (transferredResourceId != null) {
        BrowserService.Util.getInstance().retrieveTransferredResource(transferredResourceId,
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
                MessagePopup.showError(caught.getClass().getSimpleName(), caught.getMessage());
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
    if (historyTokens.size() > 1) {
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
    startIngest.setVisible(resource != null);
    createFolder.setVisible(resource == null || !resource.isFile());
    uploadFiles.setVisible(resource != null && !resource.isFile());
    remove.setVisible(resource != null);
  }

  @UiHandler("uploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {
    Tools.newHistory(IngestTransferUpload.RESOLVER, getPathFromTransferredResourceId(resource.getId()));
  }

  @UiHandler("createFolder")
  void buttonCreateFolderHandler(ClickEvent e) {
    // TODO create folder
  }

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {
    Set<TransferredResource> selected = transferredResourceList.getSelected();

    if (selected.isEmpty()) {
      // Remove the whole folder

      if (resource != null) {
        Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
          messages.ingestTransferRemoveFolderConfirmDialogMessage(resource.getName()),
          messages.ingestTransferRemoveFolderConfirmDialogCancel(),
          messages.ingestTransferRemoveFolderConfirmDialogOk(), new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
              MessagePopup.showError(caught.getMessage());
            }

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().removeTransferredResources(Arrays.asList(resource.getId()),
                  new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    MessagePopup.showError("Error removing", caught.getMessage());
                  }

                  @Override
                  public void onSuccess(Void result) {
                    MessagePopup.showInfo(messages.ingestTransferRemoveSuccessTitle(),
                      messages.ingestTransferRemoveSuccessMessage(1));
                    // TODO jump to ancestor
                  }
                });
              }
            }
          });
      }
      // else do nothing

    } else {
      // Remove all selected resources

      final List<String> idsToRemove = new ArrayList<>();
      for (TransferredResource r : selected) {
        idsToRemove.add(r.getId());
      }

      Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
        messages.ingestTransferRemoveSelectedConfirmDialogMessage(selected.size()),
        messages.ingestTransferRemoveFolderConfirmDialogCancel(), messages.ingestTransferRemoveFolderConfirmDialogOk(),
        new AsyncCallback<Boolean>() {

          @Override
          public void onFailure(Throwable caught) {
            MessagePopup.showError(caught.getMessage());
          }

          @Override
          public void onSuccess(Boolean confirmed) {
            if (confirmed) {
              BrowserService.Util.getInstance().removeTransferredResources(idsToRemove, new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                  MessagePopup.showError("Error", caught.getMessage());
                  transferredResourceList.refresh();
                }

                @Override
                public void onSuccess(Void result) {
                  MessagePopup.showInfo(messages.ingestTransferRemoveSuccessTitle(),
                    messages.ingestTransferRemoveSuccessMessage(idsToRemove.size()));
                  transferredResourceList.refresh();
                }
              });
            }
          }
        });
    }

  }

  @UiHandler("startIngest")
  void buttonStartIngestHandler(ClickEvent e) {
    if (resource != null) {
      // TODO remove resource
    }
  }

}
