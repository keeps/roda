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
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.browse.bundle.BrowseFileBundle;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectFileDialog;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell.CheckboxSelectionListener;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateJob;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class BrowseFolder extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() > 2) {
        final String historyAipId = historyTokens.get(0);
        final String historyRepresentationId = historyTokens.get(1);
        final List<String> historyFilePath = new ArrayList<String>(historyTokens.subList(2, historyTokens.size() - 1));
        final String historyFileId = historyTokens.get(historyTokens.size() - 1);

        BrowserService.Util.getInstance().retrieveBrowseFileBundle(historyAipId, historyRepresentationId,
          historyFilePath, historyFileId, LocaleInfo.getCurrentLocale().getLocaleName(),
          new AsyncCallback<BrowseFileBundle>() {

            @Override
            public void onSuccess(final BrowseFileBundle bundle) {
              if (bundle.getFile().isDirectory()) {
                callback.onSuccess(new BrowseFolder(bundle));
              } else {
                Toast.showError(messages.openFileAsFolderError());
                HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                callback.onSuccess(null);
              }
            }

            @Override
            public void onFailure(Throwable caught) {
              if (!AsyncCallbackUtils.treatCommonFailures(caught, BrowseAIP.RESOLVER.getHistoryPath())) {
                Toast.showError(caught);
              }
              callback.onSuccess(null);
            }
          });
      } else {
        HistoryUtils.newHistory(BrowseAIP.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "folder";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(BrowseAIP.RESOLVER.getHistoryPath(), getHistoryToken());
    }

  };

  public static final SafeHtml FOLDER_ICON = SafeHtmlUtils.fromSafeConstant("<i class='fa fa-folder-o'></i>");

  interface MyUiBinder extends UiBinder<Widget, BrowseFolder> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  SimplePanel folderIcon;

  @UiField
  Label folderName;

  @UiField
  Label folderId;

  @UiField
  Button rename, createFolder, identifyFormats, risks, preservationEvents;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  SearchFileList filesList;

  private final BrowseFileBundle bundle;

  private static final String ALL_FILTER = SearchFilters.allFilter(IndexedFile.class.getName());

  private BrowseFolder(BrowseFileBundle bundle) {
    this.bundle = bundle;
    String summary = messages.representationListOfFiles();
    boolean selectable = true;

    final IndexedFile folder = bundle.getFile();
    final Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_PARENT_UUID, folder.getUUID()));
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
        final SelectedItems<IndexedFile> files = !ClientSelectedItemsUtils.isEmpty(filesList.getSelected())
          ? filesList.getSelected()
          : new SelectedItemsList<IndexedFile>(Arrays.asList(folder.getUUID()), IndexedFile.class.getName());

        boolean empty = ClientSelectedItemsUtils.isEmpty(selected);
        createFolder.setEnabled(empty);

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
                    rename.setEnabled(file.isDirectory());
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }
                });
            } else {
              rename.setEnabled(false);
            }
          }
        });
      }
    });

    searchPanel = new SearchPanel(filter, ALL_FILTER, messages.searchPlaceHolder(), false, false, false);
    searchPanel.setDefaultFilterIncremental(true);
    searchPanel.setList(filesList);

    initWidget(uiBinder.createAndBindUi(this));

    String folderLabel = folder.getOriginalName() != null ? folder.getOriginalName() : folder.getId();

    HTMLPanel itemIconHtmlPanel = new HTMLPanel(
      DescriptionLevelUtils.getElementLevelIconSafeHtml(RodaConstants.VIEW_REPRESENTATION_FOLDER, false));
    itemIconHtmlPanel.addStyleName("browseItemIcon-other");

    folderIcon.setWidget(itemIconHtmlPanel);
    folderName.setText(folderLabel);
    folderId.setText(folder.getUUID());

    breadcrumb.updatePath(BreadcrumbUtils.getFileBreadcrumbs(bundle));
    breadcrumb.setVisible(true);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("rename")
  void buttonRenameHandler(ClickEvent e) {
    final String selectedUUID;

    if (ClientSelectedItemsUtils.isEmpty(filesList.getSelected())) {
      selectedUUID = bundle.getFile().getUUID();
    } else {
      if (filesList.getSelected() instanceof SelectedItemsList) {
        SelectedItemsList<IndexedFile> fileList = (SelectedItemsList<IndexedFile>) filesList.getSelected();
        selectedUUID = (String) fileList.getIds().get(0);
      } else {
        return;
      }
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
                BrowserService.Util.getInstance().renameFolder(selectedUUID, newName, details,
                  new LoadingAsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      Toast.showInfo(messages.dialogSuccess(), messages.renameSuccessful());
                      HistoryUtils.openBrowse(newFolder);
                    }
                  });
              }
            });
        }
      });
  }

  @UiHandler("move")
  void buttonMoveHandler(ClickEvent e) {
    // FIXME missing filter to remove the files themselves
    IndexedFile file = bundle.getFile();
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, file.getAipId()),
      new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId()),
      new SimpleFilterParameter(RodaConstants.FILE_ISDIRECTORY, Boolean.toString(true)));
    SelectFileDialog selectFileDialog = new SelectFileDialog(messages.moveItemTitle(), filter, true, false);
    selectFileDialog.setEmptyParentButtonVisible(true);
    selectFileDialog.setSingleSelectionMode();
    selectFileDialog.showAndCenter();
    selectFileDialog.addValueChangeHandler(new ValueChangeHandler<IndexedFile>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedFile> event) {
        final IndexedFile toFolder = event.getValue();

        Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
          RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String details) {
              SelectedItems<IndexedFile> selected = filesList.getSelected();

              if (ClientSelectedItemsUtils.isEmpty(selected)) {
                selected = new SelectedItemsList<IndexedFile>(Arrays.asList(bundle.getFile().getUUID()),
                  IndexedFile.class.getName());
              }

              final SelectedItems<IndexedFile> selectedItems = selected;
              final String aipId = bundle.getFile().getAipId();
              final String repId = bundle.getFile().getRepresentationId();

              BrowserService.Util.getInstance().moveFiles(aipId, repId, selectedItems, toFolder, details,
                new LoadingAsyncCallback<Void>() {

                  @Override
                  public void onSuccessImpl(Void nothing) {
                    if (toFolder != null) {
                      HistoryUtils.newHistory(BrowseFolder.RESOLVER, aipId, repId, toFolder.getUUID());
                    } else {
                      HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, repId);
                    }
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
          HistoryUtils.openUpload(bundle.getFile());
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
              public void onSuccess(String details) {
                IndexedFile folder = bundle.getFile();
                String aipId = folder.getAipId();
                String repId = folder.getRepresentationId();
                String folderUUID = folder.getUUID();
                BrowserService.Util.getInstance().createFolder(aipId, repId, folderUUID, newName, details,
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

  @UiHandler("remove")
  void buttonRemoveHandler(ClickEvent e) {
    final SelectedItems<IndexedFile> files = (SelectedItems<IndexedFile>) filesList.getSelected();
    final boolean deleteItself = ClientSelectedItemsUtils.isEmpty(files);
    final IndexedFile folder = bundle.getFile();
    final String aipId = folder.getAipId();
    final String repId = folder.getRepresentationId();
    final String folderParent = folder.getParentUUID();

    if (deleteItself) {
      final SelectedItems<IndexedFile> file = new SelectedItemsList<IndexedFile>(Arrays.asList(folder.getUUID()),
        IndexedFile.class.getName());

      Dialogs.showConfirmDialog(messages.fileRemoveTitle(), messages.folderRemoveMessage(), messages.dialogCancel(),
        messages.dialogYes(), new AsyncCallback<Boolean>() {

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
                    BrowserService.Util.getInstance().deleteFile(file, details, new LoadingAsyncCallback<Void>() {

                      @Override
                      public void onFailureImpl(Throwable caught) {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }

                      @Override
                      public void onSuccessImpl(Void returned) {
                        Toast.showInfo(messages.removeSuccessTitle(), messages.removeAllSuccessMessage());

                        if (folderParent == null) {
                          HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aipId, repId);
                        } else {
                          HistoryUtils.newHistory(BrowseFolder.RESOLVER, aipId, repId, folderParent);
                        }
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
                  public void onSuccess(String details) {
                    BrowserService.Util.getInstance().deleteFile(files, details, new LoadingAsyncCallback<Void>() {

                      @Override
                      public void onFailureImpl(Throwable caught) {
                        AsyncCallbackUtils.defaultFailureTreatment(caught);
                      }

                      @Override
                      public void onSuccessImpl(Void returned) {
                        Toast.showInfo(messages.removeSuccessTitle(), messages.removeAllSuccessMessage());
                        filesList.refresh();
                        rename.setEnabled(true);
                        createFolder.setEnabled(true);
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
    SelectedItems<IndexedFile> files = (SelectedItems<IndexedFile>) filesList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(files)) {
      files = new SelectedItemsList<IndexedFile>(Arrays.asList(bundle.getFile().getUUID()),
        IndexedFile.class.getName());
    }

    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(files);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateJob.RESOLVER, "action");
  }

  @UiHandler("identifyFormats")
  void buttonIdentifyFormatsHandler(ClickEvent e) {
    SelectedItems<IndexedFile> selected = (SelectedItems<IndexedFile>) filesList.getSelected();

    if (ClientSelectedItemsUtils.isEmpty(selected)) {
      Filter filter = new Filter(
        new SimpleFilterParameter(RodaConstants.FILE_ANCESTORS_PATH, bundle.getFile().getUUID()));
      selected = new SelectedItemsFilter<IndexedFile>(filter, IndexedFile.class.getName(), false);
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

  @UiHandler("risks")
  void buttonRisksButtonHandler(ClickEvent e) {
    List<String> history = new ArrayList<>();
    history.add(bundle.getAip().getId());
    history.add(bundle.getRepresentation().getId());
    history.addAll(bundle.getFile().getPath());
    history.add(bundle.getFile().getId());
    HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, history);
  }

  @UiHandler("preservationEvents")
  void buttonPreservationEventsHandler(ClickEvent e) {
    List<String> history = new ArrayList<>();
    history.add(bundle.getAip().getId());
    history.add(bundle.getRepresentation().getUUID());
    history.add(bundle.getFile().getUUID());
    HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, history);
  }

}
