package org.roda.wui.client.common.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectFileDialog;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;

import config.i18n.client.ClientMessages;

public class FileActions {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<FileAction> POSSIBLE_ACTIONS_ON_SINGLE_FILE_DIRECTORY = new HashSet<>(
    Arrays.asList(FileAction.values()));

  private static final Set<FileAction> POSSIBLE_ACTIONS_ON_SINGLE_FILE_BITSTREAM = new HashSet<>(
    Arrays.asList(FileAction.DOWNLOAD, FileAction.MOVE, FileAction.REMOVE, FileAction.NEW_PROCESS,
      FileAction.IDENTIFY_FORMATS, FileAction.SHOW_EVENTS, FileAction.SHOW_RISKS));

  private static final Set<FileAction> POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_THE_SAME_REPRESENTATION = new HashSet<>(
    Arrays.asList(FileAction.MOVE, FileAction.REMOVE, FileAction.NEW_PROCESS, FileAction.IDENTIFY_FORMATS));

  private static final Set<FileAction> POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_DIFFERENT_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(FileAction.REMOVE, FileAction.NEW_PROCESS, FileAction.IDENTIFY_FORMATS));

  private FileActions() {

  }

  public enum FileAction {
    DOWNLOAD, RENAME, MOVE, REMOVE, UPLOAD_FILES, CREATE_FOLDER, NEW_PROCESS, IDENTIFY_FORMATS, SHOW_EVENTS, SHOW_RISKS;
  }

  public static boolean canAct(FileAction action, IndexedFile file) {
    boolean ret;
    if (file.isDirectory()) {
      ret = POSSIBLE_ACTIONS_ON_SINGLE_FILE_DIRECTORY.contains(action);
    } else {
      ret = POSSIBLE_ACTIONS_ON_SINGLE_FILE_BITSTREAM.contains(action);
    }
    return ret;
  }

  public static boolean canAct(FileAction action, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedItems) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_THE_SAME_REPRESENTATION.contains(action);
  }

  public static boolean canAct(FileAction action, SelectedItems<IndexedFile> selectedItems) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_DIFFERENT_REPRESENTATIONS.contains(action);
  }

  private static AsyncCallback<Void> createDefaultAsyncCallback() {
    return new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    };
  }

  public static void act(FileAction action, IndexedFile file) {
    act(action, file, createDefaultAsyncCallback());
  }

  public static void act(FileAction action, IndexedFile file, AsyncCallback<Void> callback) {
    if (FileAction.DOWNLOAD.equals(action)) {
      download(file, callback);
    } else if (FileAction.RENAME.equals(action)) {
      rename(file, callback);
    } else if (FileAction.MOVE.equals(action)) {
      move(file, callback);
    } else if (FileAction.UPLOAD_FILES.equals(action)) {
      uploadFiles(file, callback);
    } else if (FileAction.CREATE_FOLDER.equals(action)) {
      createFolder(file, callback);
    } else if (FileAction.REMOVE.equals(action)) {
      remove(file, callback);
    } else if (FileAction.NEW_PROCESS.equals(action)) {
      newProcess(file, callback);
    } else if (FileAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(file, callback);
    } else if (FileAction.SHOW_EVENTS.equals(action)) {
      showEvents(file, callback);
    } else if (FileAction.SHOW_RISKS.equals(action)) {
      showRisks(file, callback);
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
    }
  }

  /**
   * Act on multiple files from the same representation
   * 
   */
  public static void act(FileAction action, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedItems) {
    act(action, aipId, representationId, selectedItems, createDefaultAsyncCallback());
  }

  /**
   * Act on multiple files from the same representation
   * 
   */
  public static void act(FileAction action, String aipId, String representationId,
    SelectedItems<IndexedFile> selectedItems, AsyncCallback<Void> callback) {
    if (FileAction.MOVE.equals(action)) {
      move(aipId, representationId, selectedItems, callback);
    } else if (FileAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (FileAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (FileAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      Toast.showError("Unsupported action in this context: " + action);
    }
  }

  /**
   * Act on multiple files from different representations
   * 
   */
  public static void act(FileAction action, SelectedItems<IndexedFile> selectedItems) {
    act(action, selectedItems, createDefaultAsyncCallback());
  }

  /**
   * Act on multiple files from different representations
   */
  public static void act(FileAction action, SelectedItems<IndexedFile> selectedItems, AsyncCallback<Void> callback) {
    if (FileAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (FileAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (FileAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      Toast.showError("Unsupported action in this context: " + action);
    }
  }

  public static Button createButton(final String text, final FileAction action, final IndexedFile file,
    final String... extraCssClasses) {

    // Construct
    Button downloadButton = new Button(text);

    // CSS
    downloadButton.addStyleName("btn");
    downloadButton.addStyleName("btn-block");

    for (String extraCssClass : extraCssClasses) {
      downloadButton.addStyleName(extraCssClass);
    }

    // Action
    downloadButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        FileActions.act(action, file);
      }
    });
    return downloadButton;
  }

  // ACTIONS

  public static void download(IndexedFile file, final AsyncCallback<Void> callback) {
    SafeUri downloadUri = null;
    if (file != null) {
      downloadUri = RestUtils.createRepresentationFileDownloadUri(file.getUUID());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
    callback.onSuccess(null);
  }

  public static void rename(final IndexedFile file, final AsyncCallback<Void> callback) {
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
                BrowserService.Util.getInstance().renameFolder(file.getUUID(), newName, details,
                  new LoadingAsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      Toast.showInfo(messages.dialogSuccess(), messages.renameSuccessful());
                      HistoryUtils.openBrowse(newFolder);
                      callback.onSuccess(null);
                    }
                  });
              }
            });
        }
      });
  }

  public static void move(final IndexedFile file, final AsyncCallback<Void> callback) {
    move(file.getAipId(), file.getRepresentationId(),
      new SelectedItemsList<IndexedFile>(Arrays.asList(file.getUUID()), IndexedFile.class.getName()), callback);
  }

  public static void move(final String aipId, final String representationId,
    final SelectedItems<IndexedFile> selectedItems, final AsyncCallback<Void> callback) {
    // FIXME missing filter to remove the files themselves
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId),
      new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, representationId),
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
              BrowserService.Util.getInstance().moveFiles(aipId, representationId, selectedItems, toFolder, details,
                new LoadingAsyncCallback<Void>() {

                  @Override
                  public void onSuccessImpl(Void nothing) {
                    if (toFolder != null) {
                      HistoryUtils.openBrowse(toFolder);
                    } else {
                      HistoryUtils.openBrowse(aipId, representationId);
                    }
                    callback.onSuccess(null);
                  }

                  @Override
                  public void onFailureImpl(Throwable caught) {
                    if (caught instanceof NotFoundException) {
                      Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                    } else {
                      callback.onFailure(caught);
                    }
                  }

                });
            }
          });
      }
    });
  }

  public static void uploadFiles(final IndexedFile file, final AsyncCallback<Void> callback) {
    if (file.isDirectory()) {
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
            HistoryUtils.openUpload(file);
            callback.onSuccess(null);
          }

        });
    }
  }

  public static void createFolder(final IndexedFile file, final AsyncCallback<Void> callback) {
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
                IndexedFile folder = file;
                String aipId = folder.getAipId();
                String repId = folder.getRepresentationId();
                String folderUUID = folder.getUUID();
                BrowserService.Util.getInstance().createFolder(aipId, repId, folderUUID, newName, details,
                  new LoadingAsyncCallback<IndexedFile>() {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      HistoryUtils.openBrowse(newFolder);
                      callback.onSuccess(null);
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof NotFoundException) {
                        Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                      } else {
                        callback.onFailure(caught);
                      }
                    }

                  });
              }
            });
        }
      });
  }

  public static void newProcess(IndexedFile file, final AsyncCallback<Void> callback) {
    newProcess(new SelectedItemsList<IndexedFile>(Arrays.asList(file.getUUID()), IndexedFile.class.getName()),
      callback);
  }

  public static void newProcess(SelectedItems<IndexedFile> selected, final AsyncCallback<Void> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateJob.RESOLVER, "action");
    callback.onSuccess(null);
  }

  public static void identifyFormats(IndexedFile file, final AsyncCallback<Void> callback) {
    identifyFormats(new SelectedItemsList<IndexedFile>(Arrays.asList(file.getUUID()), IndexedFile.class.getName()),
      callback);
  }

  public static void identifyFormats(SelectedItems<IndexedFile> selected, final AsyncCallback<Void> callback) {
    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new AsyncCallback<Void>() {
      @Override
      public void onSuccess(Void object) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());
        callback.onSuccess(null);
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        } else {
          callback.onFailure(caught);
        }
      }
    });
  }

  public static void remove(final IndexedFile file, final AsyncCallback<Void> callback) {
    Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
      messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

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
                  BrowserService.Util.getInstance().deleteFile(file.getUUID(), details, new AsyncCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                      List<String> path = file.getPath();
                      if (path.isEmpty()) {
                        HistoryUtils.openBrowse(file.getAipId(), file.getRepresentationId());
                      } else {
                        int lastIndex = path.size() - 1;
                        List<String> parentPath = new ArrayList<>(path.subList(0, lastIndex));
                        String parentId = path.get(lastIndex);
                        HistoryUtils.openBrowse(file.getAipId(), file.getRepresentationId(), parentPath, parentId);
                      }
                      callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      callback.onFailure(caught);
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

  public static void remove(final SelectedItems<IndexedFile> selected, final AsyncCallback<Void> callback) {
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
                      callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      callback.onFailure(caught);
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

  public static void showEvents(IndexedFile file, final AsyncCallback<Void> callback) {
    List<String> history = new ArrayList<>();
    history.add(file.getAipId());
    history.add(file.getRepresentationUUID());
    history.add(file.getUUID());
    HistoryUtils.newHistory(PreservationEvents.BROWSE_RESOLVER, history);
    callback.onSuccess(null);
  }

  public static void showRisks(IndexedFile file, final AsyncCallback<Void> callback) {
    List<String> history = new ArrayList<>();
    history.add(RiskIncidenceRegister.RESOLVER.getHistoryToken());
    history.add(file.getAipId());
    history.add(file.getRepresentationId());
    history.addAll(file.getPath());
    history.add(file.getId());
    HistoryUtils.newHistory(Planning.RESOLVER, history);
    callback.onSuccess(null);
  }

}
