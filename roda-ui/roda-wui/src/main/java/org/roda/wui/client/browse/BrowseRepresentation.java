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
package org.roda.wui.client.browse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.browse.bundle.BrowseRepresentationBundle;
import org.roda.wui.client.browse.bundle.DescriptiveMetadataViewBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectFileDialog;
import org.roda.wui.client.common.lists.DIPList;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.search.SearchSuggestBox;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateJob;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.RestErrorOverlayType;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseRepresentation extends Composite {

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String historyAipId = historyTokens.get(0);
        final String histortyRepresentationId = historyTokens.get(1);

        BrowserService.Util.getInstance().retrieveBrowseRepresentationBundle(historyAipId, histortyRepresentationId,
          LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<BrowseRepresentationBundle>() {

            @Override
            public void onFailure(Throwable caught) {
              Toast.showError(caught.getClass().getSimpleName(), caught.getMessage());
              errorRedirect(callback);
            }

            @Override
            public void onSuccess(final BrowseRepresentationBundle representationBundle) {
              callback.onSuccess(new BrowseRepresentation(representationBundle));
            }
          });

      } else {
        errorRedirect(callback);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(BrowseAIP.RESOLVER, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "representation";
    }

    private void errorRedirect(AsyncCallback<Widget> callback) {
      HistoryUtils.newHistory(BrowseAIP.RESOLVER);
      callback.onSuccess(null);
    }
  };

  interface MyUiBinder extends UiBinder<Widget, BrowseRepresentation> {
  }

  // IDENTIFICATION

  @UiField
  SimplePanel representationIcon;

  @UiField
  Label representationType;

  @UiField
  Label representationId;

  @UiField
  BreadcrumbPanel breadcrumb;

  // DESCRIPTIVE METADATA

  @UiField
  TabPanel itemMetadata;

  @UiField
  Button newDescriptiveMetadata;

  // FILES

  @UiField(provided = true)
  SearchPanel filesSearch;

  @UiField(provided = true)
  SearchFileList filesList;

  // DISSEMINATIONS

  @UiField
  Label disseminationsTitle;

  @UiField(provided = true)
  SearchPanel disseminationsSearch;

  @UiField(provided = true)
  DIPList disseminationsList;

  // SIDEBAR

  @UiField
  Button renameFolders, moveFiles, uploadFiles, createFolder, identifyFormats, changeType;

  private List<HandlerRegistration> handlers;
  private IndexedRepresentation representation;
  private String aipId;
  private String repId;
  private String repUUID;

  private static final String ALL_FILTER = SearchFilters.allFilter(IndexedFile.class.getName());

  public BrowseRepresentation(BrowseRepresentationBundle bundle) {
    this.representation = bundle.getRepresentation();
    this.aipId = representation.getAipId();
    this.repId = representation.getId();
    this.repUUID = representation.getUUID();

    handlers = new ArrayList<HandlerRegistration>();
    String summary = messages.representationListOfFiles();
    boolean selectable = true;

    // FILES

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID, repUUID),
      new EmptyKeyFilterParameter(RodaConstants.FILE_PARENT_UUID));
    filesList = new SearchFileList(filter, true, Facets.NONE, summary, selectable);

    filesList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedFile selected = filesList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.openBrowse(selected);
        }
      }
    });

    filesList.addCheckboxSelectionListener(new CheckboxSelectionListener<IndexedFile>() {

      @Override
      public void onSelectionChange(SelectedItems<IndexedFile> selected) {
        final SelectedItems<IndexedFile> files = filesList.getSelected();
        boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
        moveFiles.setEnabled(!empty);
        createFolder.setEnabled(empty);
        uploadFiles.setEnabled(empty);

        ClientSelectedItemsUtils.size(IndexedFile.class, files, new AsyncCallback<Long>() {

          @Override
          public void onFailure(Throwable caught) {
            // do nothing
          }

          @Override
          public void onSuccess(Long result) {
            if (result == 1 && files instanceof SelectedItemsList) {
              SelectedItemsList<IndexedFile> fileList = (SelectedItemsList<IndexedFile>) files;

              BrowserService.Util.getInstance().retrieve(IndexedFile.class.getName(), fileList.getIds().get(0),
                new AsyncCallback<IndexedFile>() {

                  @Override
                  public void onSuccess(IndexedFile file) {
                    renameFolders.setEnabled(file.isDirectory());
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }
                });
            } else {
              renameFolders.setEnabled(false);
            }
          }
        });
      }
    });

    filesSearch = new SearchPanel(filter, ALL_FILTER, messages.searchPlaceHolder(), false, false, true);
    filesSearch.setDefaultFilterIncremental(true);
    filesSearch.setList(filesList);

    // DISSEMINATIONS
    disseminationsList = new DIPList(Filter.NULL, Facets.NONE, messages.listOfDisseminations(), true);
    disseminationsList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedDIP dissemination = disseminationsList.getSelectionModel().getSelectedObject();
        if (dissemination != null) {
          HistoryUtils.openBrowse(dissemination, representation);
        }
      }
    });

    disseminationsSearch = new SearchPanel(Filter.NULL, RodaConstants.DIP_SEARCH, messages.searchPlaceHolder(), false,
      false, true);
    disseminationsSearch.setDefaultFilterIncremental(true);
    disseminationsSearch.setList(disseminationsList);

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // IDENTIFICATION

    HTMLPanel representationIconHtmlPanel = new HTMLPanel(
      DescriptionLevelUtils.getRepresentationTypeIcon(representation.getType(), false));
    representationIconHtmlPanel.addStyleName("browseItemIcon-other");
    representationIcon.setWidget(representationIconHtmlPanel);
    representationType.setText(representation.getType() != null ? representation.getType() : representation.getId());
    representationId.setText(representation.getId());

    breadcrumb.updatePath(BreadcrumbUtils.getRepresentationBreadcrumbs(bundle));
    breadcrumb.setVisible(true);

    // DESCRIPTIVE METADATA

    final List<Pair<String, HTML>> descriptiveMetadataContainers = new ArrayList<Pair<String, HTML>>();
    final Map<String, DescriptiveMetadataViewBundle> bundles = new HashMap<>();
    for (DescriptiveMetadataViewBundle descMetadataBundle : bundle.getRepresentationDescriptiveMetadata()) {
      String title = descMetadataBundle.getLabel() != null ? descMetadataBundle.getLabel() : descMetadataBundle.getId();
      HTML container = new HTML();
      container.addStyleName("metadataContent");
      itemMetadata.add(container, title);
      descriptiveMetadataContainers.add(Pair.create(descMetadataBundle.getId(), container));
      bundles.put(descMetadataBundle.getId(), descMetadataBundle);
    }

    HandlerRegistration tabHandler = itemMetadata.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() < descriptiveMetadataContainers.size()) {
          Pair<String, HTML> pair = descriptiveMetadataContainers.get(event.getSelectedItem());
          String descId = pair.getFirst();
          final HTML html = pair.getSecond();
          final DescriptiveMetadataViewBundle bundle = bundles.get(descId);
          if (html.getText().length() == 0) {
            getDescriptiveMetadataHTML(descId, bundle, new AsyncCallback<SafeHtml>() {

              @Override
              public void onFailure(Throwable caught) {
                if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
                  Toast.showError(messages.errorLoadingDescriptiveMetadata(caught.getMessage()));
                }
              }

              @Override
              public void onSuccess(SafeHtml result) {
                html.setHTML(result);
              }
            });
          }
        }
      }
    });

    final int addTabIndex = itemMetadata.getWidgetCount();
    FlowPanel addTab = new FlowPanel();
    addTab.add(new HTML(SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-plus-circle\"></i>")));
    itemMetadata.add(new Label(), addTab);
    HandlerRegistration addTabHandler = itemMetadata.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if (event.getSelectedItem() == addTabIndex) {
          newRepresentationDescriptiveMetadata();
        }
      }
    });
    addTab.addStyleName("addTab");
    addTab.getParent().addStyleName("addTabWrapper");

    handlers.add(tabHandler);
    handlers.add(addTabHandler);

    if (!bundle.getRepresentationDescriptiveMetadata().isEmpty()) {
      newDescriptiveMetadata.setVisible(false);
      itemMetadata.setVisible(true);
      itemMetadata.selectTab(0);
    } else {
      newDescriptiveMetadata.setVisible(true);
      itemMetadata.setVisible(false);
    }

    // DISSEMINATIONS (POST-INIT)
    if (bundle.getDipCount() > 0) {
      Filter disseminationsFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.DIP_REPRESENTATION_UUIDS, repUUID));
      disseminationsList.set(disseminationsFilter, bundle.getAip().getState().equals(AIPState.ACTIVE), Facets.NONE);
      disseminationsSearch.setDefaultFilter(disseminationsFilter);
      disseminationsSearch.clearSearchInputBox();
    }
    disseminationsList.getParent().setVisible(bundle.getDipCount() > 0);

    // SIDEBAR
    renameFolders.setEnabled(false);
    moveFiles.setEnabled(false);
    uploadFiles.setEnabled(true);
    createFolder.setEnabled(true);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void getDescriptiveMetadataHTML(final String descId, final DescriptiveMetadataViewBundle bundle,
    final AsyncCallback<SafeHtml> callback) {
    SafeUri uri = RestUtils.createRepresentationDescriptiveMetadataHTMLUri(aipId, repId, descId);
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, uri.asString());
    requestBuilder.setHeader("Authorization", "Custom");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (200 == response.getStatusCode()) {
            String html = response.getText();

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }
            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
              descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            // Download link
            SafeUri downloadUri = RestUtils.createRepresentationDescriptiveMetadataDownloadUri(aipId, repId, descId);
            String downloadLinkHtml = "<a href='" + downloadUri.asString()
              + "' class='toolbarLink'><i class='fa fa-download'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(downloadLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataHTML'>"));
            b.append(SafeHtmlUtils.fromTrustedString(html));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
            SafeHtml safeHtml = b.toSafeHtml();

            callback.onSuccess(safeHtml);
          } else {
            String text = response.getText();
            String message;
            try {
              RestErrorOverlayType error = (RestErrorOverlayType) JsonUtils.safeEval(text);
              message = error.getMessage();
            } catch (IllegalArgumentException e) {
              message = text;
            }

            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='descriptiveMetadataLinks'>"));

            if (bundle.hasHistory()) {
              // History link
              String historyLink = HistoryUtils.createHistoryHashLink(DescriptiveMetadataHistory.RESOLVER, aipId, repId,
                descId);
              String historyLinkHtml = "<a href='" + historyLink
                + "' class='toolbarLink'><i class='fa fa-history'></i></a>";
              b.append(SafeHtmlUtils.fromSafeConstant(historyLinkHtml));
            }

            // Edit link
            String editLink = HistoryUtils.createHistoryHashLink(EditDescriptiveMetadata.RESOLVER, aipId, repId,
              descId);
            String editLinkHtml = "<a href='" + editLink + "' class='toolbarLink'><i class='fa fa-edit'></i></a>";
            b.append(SafeHtmlUtils.fromSafeConstant(editLinkHtml));

            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            // error message
            b.append(SafeHtmlUtils.fromSafeConstant("<div class='error'>"));
            b.append(messages.descriptiveMetadataTranformToHTMLError());
            b.append(SafeHtmlUtils.fromSafeConstant("<pre><code>"));
            b.append(SafeHtmlUtils.fromString(message));
            b.append(SafeHtmlUtils.fromSafeConstant("</core></pre>"));
            b.append(SafeHtmlUtils.fromSafeConstant("</div>"));

            callback.onSuccess(b.toSafeHtml());
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          callback.onFailure(exception);
        }
      });
    } catch (RequestException e) {
      callback.onFailure(e);
    }
  }

  private void newRepresentationDescriptiveMetadata() {
    HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, CreateDescriptiveMetadata.REPRESENTATION, aipId, repId);
  }

  @UiHandler("newDescriptiveMetadata")
  void buttonNewDescriptiveMetadataEventsHandler(ClickEvent e) {
    newRepresentationDescriptiveMetadata();
  }

  @UiHandler("download")
  void buttonDownloadHandler(ClickEvent e) {
    SafeUri downloadUri = null;
    if (repId != null) {
      downloadUri = RestUtils.createRepresentationDownloadUri(aipId, repId);
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
  }

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {
    final SelectedItems<IndexedFile> selected = (SelectedItems<IndexedFile>) filesList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      final SelectedItems<IndexedRepresentation> selectedList = new SelectedItemsList<IndexedRepresentation>(
        Arrays.asList(repUUID), IndexedRepresentation.class.getName());

      Dialogs.showConfirmDialog(messages.representationRemoveTitle(), messages.representationRemoveMessage(),
        messages.dialogCancel(), messages.dialogYes(), new AsyncCallback<Boolean>() {

          @Override
          public void onSuccess(Boolean confirmed) {
            if (confirmed) {
              Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
                RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }

                  @Override
                  public void onSuccess(String details) {
                    BrowserService.Util.getInstance().deleteRepresentation(selectedList, details,
                      new AsyncCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                          HistoryUtils.newHistory(BrowseAIP.RESOLVER, aipId);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                          AsyncCallbackUtils.defaultFailureTreatment(caught);
                        }
                      });
                  }
                });
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // nothing to do
          }
        });
    } else {
      Dialogs.showConfirmDialog(messages.filesRemoveTitle(), messages.selectedFileRemoveMessage(),
        messages.dialogCancel(), messages.dialogYes(), new AsyncCallback<Boolean>() {

          @Override
          public void onSuccess(Boolean confirmed) {
            if (confirmed) {
              Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
                RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }

                  @Override
                  public void onSuccess(final String details) {
                    BrowserService.Util.getInstance().deleteFile(selected, details, new AsyncCallback<Void>() {

                      @Override
                      public void onSuccess(Void result) {
                        filesList.refresh();
                        uploadFiles.setEnabled(true);
                        createFolder.setEnabled(true);
                        renameFolders.setEnabled(false);
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }
                    });
                  }
                });
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // nothing to do
          }

        });
    }
  }

  @UiHandler("newProcess")
  void buttonNewProcessHandler(ClickEvent e) {
    SelectedItems<? extends IsIndexed> selected = filesList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      selected = new SelectedItemsList<IndexedRepresentation>(Arrays.asList(repUUID),
        IndexedRepresentation.class.getName());
    }

    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateJob.RESOLVER, "action");
  }

  @UiHandler("risks")
  void buttonRisksHandler(ClickEvent e) {
    if (aipId != null) {
      HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, aipId, representation.getId());
    }
  }

  @UiHandler("preservationEvents")
  void buttonPreservationEventsHandler(ClickEvent e) {
    if (aipId != null) {
      HistoryUtils.newHistory(PreservationEvents.BROWSE_RESOLVER, aipId, repUUID);
    }
  }

  @UiHandler("renameFolders")
  void buttonRenameHandler(ClickEvent e) {
    if (!ClientSelectedItemsUtils.isEmpty(filesList.getSelected())) {
      final String folderUUID;
      if (filesList.getSelected() instanceof SelectedItemsList) {
        SelectedItemsList<IndexedFile> fileList = (SelectedItemsList<IndexedFile>) filesList.getSelected();
        folderUUID = fileList.getIds().get(0);
      } else {
        return;
      }

      Dialogs.showPromptDialog(messages.renameItemTitle(), null, messages.renamePlaceholder(), RegExp.compile(".*"),
        messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

          @Override
          public void onFailure(Throwable caught) {
            // do nothing
          }

          @Override
          public void onSuccess(final String newName) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().renameFolder(folderUUID, newName, details,
                    new LoadingAsyncCallback<IndexedFile>() {

                      @Override
                      public void onSuccessImpl(IndexedFile newFile) {
                        Toast.showInfo(messages.dialogSuccess(), messages.renameSuccessful());
                        HistoryUtils.openBrowse(newFile);
                      }
                    });
                }
              });
          }
        });
    }
  }

  @UiHandler("moveFiles")
  void buttonMoveHandler(ClickEvent e) {
    // FIXME missing filter to remove the files themselves
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_UUID, repUUID),
      new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId),
      new SimpleFilterParameter(RodaConstants.FILE_ISDIRECTORY, Boolean.toString(true)));
    SelectFileDialog selectFileDialog = new SelectFileDialog(messages.moveItemTitle(), filter, true, false);
    selectFileDialog.setEmptyParentButtonVisible(true);
    selectFileDialog.setSingleSelectionMode();
    selectFileDialog.showAndCenter();
    selectFileDialog.addValueChangeHandler(new ValueChangeHandler<IndexedFile>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedFile> event) {
        final IndexedFile toFolder = event.getValue();
        final SelectedItems<IndexedFile> selected = filesList.getSelected();

        if (!ClientSelectedItemsUtils.isEmpty(selected)) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().moveFiles(aipId, repId, selected, toFolder, details,
                  new LoadingAsyncCallback<Void>() {

                    @Override
                    public void onSuccessImpl(Void nothing) {
                      HistoryUtils.openBrowse(toFolder);
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof NotFoundException) {
                        Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                      } else {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }
                    }
                  });
              }
            });
        }
      }
    });
  }

  @UiHandler("uploadFiles")
  void buttonUploadFilesHandler(ClickEvent e) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String details) {
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setDetailsMessage(details);
          HistoryUtils.openUpload(representation);
        }
      });
  }

  @UiHandler("createFolder")
  void buttonCreateFolderHandler(ClickEvent e) {
    Dialogs.showPromptDialog(messages.createFolderTitle(), null, messages.createFolderPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          Toast.showInfo(messages.dialogFailure(), messages.renameFailed());
        }

        @Override
        public void onSuccess(final String newName) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(final String details) {
                BrowserService.Util.getInstance().createFolder(aipId, repId, null, newName, details,
                  new LoadingAsyncCallback<String>() {

                    @Override
                    public void onSuccessImpl(String newUUID) {
                      filesList.refresh();
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof NotFoundException) {
                        Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                      } else {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }
                    }

                  });
              }
            });
        }
      });
  }

  @UiHandler("identifyFormats")
  void buttonIdentifyFormatsHandler(ClickEvent e) {
    SelectedItems<?> selected = filesList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      selected = new SelectedItemsList<IndexedRepresentation>(Arrays.asList(representation.getUUID()),
        IndexedRepresentation.class.getName());
    }

    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new AsyncCallback<Void>() {

      @Override
      public void onSuccess(Void object) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }
      }
    });
  }

  @UiHandler("changeType")
  void buttonChangeTypeHandler(ClickEvent e) {
    final SelectedItemsList<IndexedRepresentation> selectedRepresentation = new SelectedItemsList<IndexedRepresentation>(
      Arrays.asList(representation.getUUID()), IndexedRepresentation.class.getName());

    SearchSuggestBox<IndexedRepresentation> suggestBox = new SearchSuggestBox<IndexedRepresentation>(
      IndexedRepresentation.class, RodaConstants.REPRESENTATION_TYPE, true);

    Dialogs.showPromptDialogSuggest(messages.changeTypeTitle(), null, messages.changeTypePlaceHolder(),
      messages.cancelButton(), messages.confirmButton(), suggestBox, new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(final String newType) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

              @Override
              public void onFailure(Throwable caught) {
                // do nothing
              }

              @Override
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().changeRepresentationType(selectedRepresentation, newType, details,
                  new LoadingAsyncCallback<Void>() {

                    @Override
                    public void onSuccessImpl(Void nothing) {
                      Toast.showInfo(messages.dialogSuccess(), messages.changeTypeSuccessful());
                      representationType.setText(newType);
                    }
                  });
              }
            });
        }
      });
  }

}
