/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionLoadingAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectFileDialog;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class FileActions extends AbstractActionable<IndexedFile> {

  private static final FileActions GENERAL_INSTANCE = new FileActions(null, null);

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

  private final String aipId;
  private final String representationId;

  private FileActions(String aipId, String representationId) {
    this.aipId = aipId;
    this.representationId = representationId;
  }

  // MANAGEMENT
  public enum FileAction implements Action<IndexedFile> {
    DOWNLOAD(), RENAME("org.roda.wui.api.controllers.Browser.renameFolder"),
    MOVE("org.roda.wui.api.controllers.Browser.moveFiles"),
    REMOVE("org.roda.wui.api.controllers.Browser.delete(IndexedFile)"),
    UPLOAD_FILES("org.roda.wui.api.controllers.Browser.createFile"),
    CREATE_FOLDER("org.roda.wui.api.controllers.Browser.createFolder"),
    NEW_PROCESS("org.roda.wui.api.controllers.Jobs.createJob"),
    IDENTIFY_FORMATS("org.roda.wui.api.controllers.Jobs.createJob"),
    SHOW_EVENTS("org.roda.wui.api.controllers.Browser.find(IndexedPreservationEvent)"),
    SHOW_RISKS("org.roda.wui.api.controllers.Browser.find(IndexedRisk)");

    private List<String> methods;

    FileAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public FileAction actionForName(String name) {
    return FileAction.valueOf(name);
  }

  public static FileActions get() {
    return GENERAL_INSTANCE;
  }

  public static FileActions get(String aipId, String representationId) {
    return new FileActions(aipId, representationId);
  }

  @Override
  public boolean canAct(Action<IndexedFile> action, IndexedFile file) {
    boolean canAct = false;

    if (hasPermissions(action, Optional.of(file))) {
      if (file.isDirectory()) {
        canAct = POSSIBLE_ACTIONS_ON_SINGLE_FILE_DIRECTORY.contains(action);
      } else {
        canAct = POSSIBLE_ACTIONS_ON_SINGLE_FILE_BITSTREAM.contains(action);
      }
    }

    return canAct;
  }

  @Override
  public boolean canAct(Action<IndexedFile> action, SelectedItems<IndexedFile> selectedItems) {
    boolean canAct = false;

    if (hasPermissions(action)) {
      if (aipId != null && representationId != null) {
        canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_THE_SAME_REPRESENTATION.contains(action);
      } else {
        canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_DIFFERENT_REPRESENTATIONS.contains(action);
      }
    }

    return canAct;
  }

  @Override
  public void act(Action<IndexedFile> action, IndexedFile file, AsyncCallback<ActionImpact> callback) {
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
      unsupportedAction(action, callback);
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Action<IndexedFile> action, SelectedItems<IndexedFile> selectedItems,
    AsyncCallback<ActionImpact> callback) {
    if (FileAction.MOVE.equals(action) && aipId != null && representationId != null) {
      move(aipId, representationId, selectedItems, callback);
    } else if (FileAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (FileAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (FileAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS

  public void download(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = null;
    if (file != null) {
      downloadUri = RestUtils.createRepresentationFileDownloadUri(file.getUUID());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
    callback.onSuccess(ActionImpact.NONE);
  }

  public void rename(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.renameItemTitle(), null, file.getId(), null, RegExp.compile("^[^/]+$"),
      messages.cancelButton(), messages.confirmButton(), true, false, new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(final String newName) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().renameFolder(file.getUUID(), newName, details,
                  new ActionLoadingAsyncCallback<IndexedFile>(callback) {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      Toast.showInfo(messages.dialogSuccess(), messages.renameSuccessful());
                      HistoryUtils.openBrowse(newFolder);
                      doActionCallbackUpdated();
                    }
                  });
              }
            });
        }
      });
  }

  public void move(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    move(file.getAipId(), file.getRepresentationId(),
      new SelectedItemsList<>(Arrays.asList(file.getUUID()), IndexedFile.class.getName()), callback);
  }

  public void move(final String aipId, final String representationId, final SelectedItems<IndexedFile> selectedItems,
    final AsyncCallback<ActionImpact> callback) {
    // FIXME missing filter to remove the files themselves
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId),
      new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, representationId),
      new SimpleFilterParameter(RodaConstants.FILE_ISDIRECTORY, Boolean.toString(true)));
    SelectFileDialog selectFileDialog = new SelectFileDialog(messages.moveItemTitle(), filter, true);
    selectFileDialog.setEmptyParentButtonVisible(true);
    selectFileDialog.setSingleSelectionMode();
    selectFileDialog.showAndCenter();
    selectFileDialog.addValueChangeHandler(new ValueChangeHandler<IndexedFile>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedFile> event) {
        final IndexedFile toFolder = event.getValue();

        Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
          RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
          new ActionNoAsyncCallback<String>(callback) {

            @Override
            public void onSuccess(String details) {
              BrowserService.Util.getInstance().moveFiles(aipId, representationId, selectedItems, toFolder, details,
                new ActionLoadingAsyncCallback<Job>(callback) {

                  @Override
                  public void onSuccessImpl(Job result) {
                    Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                        Timer timer = new Timer() {
                          @Override
                          public void run() {
                            if (toFolder != null) {
                              HistoryUtils.openBrowse(toFolder);
                            } else {
                              HistoryUtils.openBrowse(aipId, representationId);
                            }
                            doActionCallbackUpdated();
                          }
                        };

                        timer.schedule(RodaConstants.ACTION_TIMEOUT);
                      }

                      @Override
                      public void onSuccess(final Void nothing) {
                        HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                        doActionCallbackUpdated();
                      }
                    });
                  }

                  @Override
                  public void onFailureImpl(Throwable caught) {
                    HistoryUtils.newHistory(InternalProcess.RESOLVER);
                  }
                });
            }
          });
      }
    });
  }

  public void uploadFiles(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    if (file.isDirectory()) {
      Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
        RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
        new ActionNoAsyncCallback<String>(callback) {

          @Override
          public void onSuccess(String details) {
            LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
            selectedItems.setDetailsMessage(details);
            HistoryUtils.openUpload(file);
            doActionCallbackUpdated();
          }
        });
    }
  }

  public void createFolder(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.createFolderTitle(), null, null, messages.createFolderPlaceholder(),
      RegExp.compile("^[^/]+$"), messages.cancelButton(), messages.confirmButton(), false, false,
      new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(final String newName) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(String details) {
                IndexedFile folder = file;
                String aipId = folder.getAipId();
                String repId = folder.getRepresentationId();
                String folderUUID = folder.getUUID();
                BrowserService.Util.getInstance().createFolder(aipId, repId, folderUUID, newName, details,
                  new ActionLoadingAsyncCallback<IndexedFile>(callback) {

                    @Override
                    public void onSuccessImpl(IndexedFile newFolder) {
                      HistoryUtils.openBrowse(newFolder);
                      doActionCallbackUpdated();
                    }

                    @Override
                    public void onFailureImpl(Throwable caught) {
                      if (caught instanceof AlreadyExistsException) {
                        Dialogs.showInformationDialog(messages.createFolderAlreadyExistsTitle(),
                          messages.createFolderAlreadyExistsMessage(), messages.dialogOk(), false);
                      }
                    }
                  });
              }
            });
        }
      });
  }

  public void newProcess(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    newProcess(new SelectedItemsList<>(Arrays.asList(file.getUUID()), IndexedFile.class.getName()), callback);
  }

  public void newProcess(SelectedItems<IndexedFile> selected, final AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  public void identifyFormats(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    identifyFormats(new SelectedItemsList<>(Arrays.asList(file.getUUID()), IndexedFile.class.getName()), callback);
  }

  public void identifyFormats(SelectedItems<IndexedFile> selected, final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new ActionAsyncCallback<Void>(callback) {
      @Override
      public void onSuccess(Void object) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());
        doActionCallbackUpdated();
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        }
        super.onFailure(caught);
      }
    });
  }

  public void remove(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.viewRepresentationRemoveFileTitle(),
      messages.viewRepresentationRemoveFileMessage(), messages.dialogCancel(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().deleteFile(file.getUUID(), details,
                    new ActionAsyncCallback<Void>(callback) {

                      @Override
                      public void onSuccess(Void result) {
                        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                        Timer timer = new Timer() {
                          @Override
                          public void run() {
                            List<String> path = file.getPath();
                            if (path.isEmpty()) {
                              HistoryUtils.openBrowse(file.getAipId(), file.getRepresentationId());
                            } else {
                              int lastIndex = path.size() - 1;
                              List<String> parentPath = new ArrayList<>(path.subList(0, lastIndex));
                              String parentId = path.get(lastIndex);
                              HistoryUtils.openBrowse(file.getAipId(), file.getRepresentationId(), parentPath,
                                parentId);
                            }
                            doActionCallbackDestroyed();
                          }
                        };

                        timer.schedule(RodaConstants.ACTION_TIMEOUT);
                      }
                    });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  public void remove(final SelectedItems<IndexedFile> selected, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.filesRemoveTitle(), messages.selectedFileRemoveMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(final String details) {
                  BrowserService.Util.getInstance().deleteFile(selected, details, new AsyncCallback<Job>() {

                    @Override
                    public void onSuccess(Job result) {
                      Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                        new ActionAsyncCallback<Void>(callback) {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                HistoryUtils.newHistory(HistoryUtils.getCurrentHistoryPath());
                                doActionCallbackDestroyed();
                              }
                            };

                            timer.schedule(RodaConstants.ACTION_TIMEOUT);
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                            doActionCallbackDestroyed();
                          }
                        });
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                      callback.onFailure(caught);
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  public void showEvents(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    List<String> history = new ArrayList<>();
    history.add(file.getAipId());
    history.add(file.getRepresentationUUID());
    history.add(file.getUUID());
    HistoryUtils.newHistory(PreservationEvents.BROWSE_RESOLVER, history);
    callback.onSuccess(ActionImpact.NONE);
  }

  public void showRisks(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    List<String> history = new ArrayList<>();
    history.add(RiskIncidenceRegister.RESOLVER.getHistoryToken());
    history.add(file.getAipId());
    history.add(file.getRepresentationId());
    history.addAll(file.getPath());
    history.add(file.getId());
    HistoryUtils.newHistory(Planning.RESOLVER, history);
    callback.onSuccess(ActionImpact.NONE);
  }

  @Override
  public ActionableBundle<IndexedFile> createActionsBundle() {
    ActionableBundle<IndexedFile> fileActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedFile> managementGroup = new ActionableGroup<>(messages.sidebarFoldersFilesTitle());
    managementGroup.addButton(messages.downloadButton(), FileAction.DOWNLOAD, ActionImpact.NONE, "btn-download",
      "fileDownloadButton");
    managementGroup.addButton(messages.renameButton(), FileAction.RENAME, ActionImpact.UPDATED, "btn-edit",
      "fileRenameButton");
    managementGroup.addButton(messages.moveButton(), FileAction.MOVE, ActionImpact.UPDATED, "btn-edit",
      "fileMoveButton");
    managementGroup.addButton(messages.uploadFilesButton(), FileAction.UPLOAD_FILES, ActionImpact.UPDATED, "btn-upload",
      "fileUploadButton");
    managementGroup.addButton(messages.createFolderButton(), FileAction.CREATE_FOLDER, ActionImpact.UPDATED, "btn-plus",
      "fileCreateFolderButton");
    managementGroup.addButton(messages.removeButton(), FileAction.REMOVE, ActionImpact.DESTROYED, "btn-ban",
      "fileRemoveButton");

    // PRESERVATION
    ActionableGroup<IndexedFile> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), FileAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-play", "fileNewProcessButton");
    preservationGroup.addButton(messages.identifyFormatsButton(), FileAction.IDENTIFY_FORMATS, ActionImpact.UPDATED,
      "btn-play", "fileIdentifyFormatsButton");
    preservationGroup.addButton(messages.preservationEvents(), FileAction.SHOW_EVENTS, ActionImpact.NONE, "btn-play",
      "fileShowEventsButton");
    preservationGroup.addButton(messages.preservationRisks(), FileAction.SHOW_RISKS, ActionImpact.NONE, "btn-play",
      "fileShowRisksButton");

    fileActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return fileActionableBundle;
  }
}
