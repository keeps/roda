/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.file.CreateFolderRequest;
import org.roda.core.data.v2.file.MoveFilesRequest;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectFileDialog;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class FileSearchWrapperActions extends AbstractActionable<IndexedFile> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<FileSearchWrapperAction> POSSIBLE_ACTIONS_WITH_REPRESENTATION = new HashSet<>(
    Arrays.asList(FileSearchWrapperAction.UPLOAD_FILES, FileSearchWrapperAction.CREATE_FOLDER));

  private static final Set<Action<IndexedFile>> POSSIBLE_ACTIONS_ON_SINGLE_FILE_DIRECTORY = new HashSet<>(
    Arrays.asList(FileSearchWrapperAction.MOVE, FileSearchWrapperAction.REMOVE, FileSearchWrapperAction.NEW_PROCESS,
      FileSearchWrapperAction.IDENTIFY_FORMATS));

  private static final Set<Action<IndexedFile>> POSSIBLE_ACTIONS_ON_SINGLE_FILE_BITSTREAM = new HashSet<>(
    Arrays.asList(FileSearchWrapperAction.MOVE, FileSearchWrapperAction.REMOVE, FileSearchWrapperAction.NEW_PROCESS,
      FileSearchWrapperAction.IDENTIFY_FORMATS));

  private static final Set<Action<IndexedFile>> POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_THE_SAME_REPRESENTATION = new HashSet<>(
    Arrays.asList(FileSearchWrapperAction.MOVE, FileSearchWrapperAction.REMOVE, FileSearchWrapperAction.NEW_PROCESS,
      FileSearchWrapperAction.IDENTIFY_FORMATS));

  private static final Set<Action<IndexedFile>> POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_DIFFERENT_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(FileSearchWrapperAction.REMOVE, FileSearchWrapperAction.NEW_PROCESS,
      FileSearchWrapperAction.IDENTIFY_FORMATS));

  private static final Set<Action<IndexedFile>> POSSIBLE_ACTIONS_ON_SINGLE_FILE_UNDER_APPRAISAL = new HashSet<>(
    Arrays.asList(FileSearchWrapperAction.MOVE, FileSearchWrapperAction.REMOVE));

  private final String aipId;
  private final String representationId;
  private final AIPState state;
  private final IndexedFile parentFolder;
  private final Permissions permissions;

  private FileSearchWrapperActions(String aipId, String representationId, AIPState state, IndexedFile parentFolder,
    Permissions permissions) {
    this.aipId = aipId;
    this.representationId = representationId;
    this.state = state;
    this.permissions = permissions;
    this.parentFolder = parentFolder != null && parentFolder.isDirectory() ? parentFolder : null;
  }

  public static FileSearchWrapperActions get(String aipId, String representationId, AIPState state,
    Permissions permissions) {
    return new FileSearchWrapperActions(aipId, representationId, state, null, permissions);
  }

  public static FileSearchWrapperActions get(String aipId, String representationId, AIPState state,
    IndexedFile parentFolder, Permissions permissions) {
    return new FileSearchWrapperActions(aipId, representationId, state, parentFolder, permissions);
  }

  public static FileSearchWrapperActions getWithoutNoFileActions(String aipId, String representationId, AIPState state,
    IndexedFile parentFolder, Permissions permissions) {
    return new FileSearchWrapperActions(aipId, representationId, state, parentFolder, permissions) {
      @Override
      public CanActResult contextCanAct(Action<IndexedFile> action) {
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
      }
    };
  }

  @Override
  public FileSearchWrapperAction[] getActions() {
    return FileSearchWrapperAction.values();
  }

  @Override
  public FileSearchWrapperAction actionForName(String name) {
    return FileSearchWrapperAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedFile> action) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedFile> action) {
    return new CanActResult(
      aipId != null && representationId != null && POSSIBLE_ACTIONS_WITH_REPRESENTATION.contains(action),
      CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedFile> action, IndexedFile file) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedFile> action, IndexedFile file) {
    if (AIPState.UNDER_APPRAISAL.equals(state)) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_FILE_UNDER_APPRAISAL.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonAIPUnderAppraisal());
    }

    if (file.isDirectory()) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_FILE_DIRECTORY.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnFileDirectory());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_FILE_BITSTREAM.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnFileBitstream());
    }
  }

  @Override
  public CanActResult userCanAct(Action<IndexedFile> action, SelectedItems<IndexedFile> selectedItems) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedFile> action, SelectedItems<IndexedFile> selectedItems) {
    if (AIPState.UNDER_APPRAISAL.equals(state)) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_FILE_UNDER_APPRAISAL.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonAIPUnderAppraisal());
    }

    if (aipId != null && representationId != null) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_THE_SAME_REPRESENTATION.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonFilesAreOnSameRepresentation());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_FILES_FROM_DIFFERENT_REPRESENTATIONS.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonFilesAreOnDifferentRepresentations());
    }
  }

  @Override
  public void act(Action<IndexedFile> action, AsyncCallback<ActionImpact> callback) {
    if (FileSearchWrapperAction.UPLOAD_FILES.equals(action)) {
      if (parentFolder != null) {
        uploadFiles(parentFolder, callback);
      } else {
        uploadFiles(callback);
      }
    } else if (FileSearchWrapperAction.CREATE_FOLDER.equals(action)) {
      if (parentFolder != null) {
        createFolder(parentFolder, callback);
      } else {
        createFolder(callback);
      }
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedFile> action, IndexedFile file, AsyncCallback<ActionImpact> callback) {
    if (FileSearchWrapperAction.MOVE.equals(action)) {
      move(file, callback);
    } else if (FileSearchWrapperAction.REMOVE.equals(action)) {
      remove(file, callback);
    } else if (FileSearchWrapperAction.NEW_PROCESS.equals(action)) {
      newProcess(file, callback);
    } else if (FileSearchWrapperAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(file, callback);
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
    if (FileSearchWrapperAction.MOVE.equals(action) && aipId != null && representationId != null) {
      move(aipId, representationId, selectedItems, callback);
    } else if (FileSearchWrapperAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (FileSearchWrapperAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (FileSearchWrapperAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS

  private void move(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    move(file.getAipId(), file.getRepresentationId(),
      new SelectedItemsList<>(Collections.singletonList(file.getUUID()), IndexedFile.class.getName()), callback);
  }

  private void move(final String aipId, final String representationId, final SelectedItems<IndexedFile> selectedItems,
    final AsyncCallback<ActionImpact> callback) {
    // FIXME missing filter to remove the files themselves
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId),
      new SimpleFilterParameter(RodaConstants.FILE_REPRESENTATION_ID, representationId),
      new SimpleFilterParameter(RodaConstants.FILE_ISDIRECTORY, Boolean.toString(true)));

    if (parentFolder != null) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, parentFolder.getUUID()));
    }

    SelectFileDialog selectFileDialog = new SelectFileDialog(messages.moveItemTitle(), filter, true);
    selectFileDialog.setEmptyParentButtonVisible(parentFolder != null);
    selectFileDialog.setSingleSelectionMode();
    selectFileDialog.showAndCenter();
    selectFileDialog.addCloseHandler(e -> callback.onSuccess(ActionImpact.NONE));
    selectFileDialog.addValueChangeHandler(new ValueChangeHandler<IndexedFile>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedFile> event) {
        final IndexedFile toFolder = event.getValue();

        Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
          RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
          new ActionNoAsyncCallback<String>(callback) {

            @Override
            public void onSuccess(String details) {
              Services services = new Services("Move a file", "move");
              MoveFilesRequest moveFilesRequest = new MoveFilesRequest();
              moveFilesRequest.setAipId(aipId);
              moveFilesRequest.setRepresentationId(representationId);
              moveFilesRequest.setItemsToMove(selectedItems);
              moveFilesRequest.setDetails(details);
              moveFilesRequest.setFileUUIDtoMove(toFolder != null ? toFolder.getUUID() : null);
              services.fileResource(s -> s.moveFileToFolder(moveFilesRequest)).whenComplete((job, throwable) -> {
                if (throwable != null) {
                  AsyncCallbackUtils.defaultFailureTreatment(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackNone();
                          if (toFolder != null) {
                            HistoryUtils.openBrowse(toFolder);
                          } else {
                            HistoryUtils.openBrowse(aipId, representationId);
                          }
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
            }
          });
      }
    });
  }

  private void uploadFiles(final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
      new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(String details) {
          doActionCallbackNone();
          LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
          selectedItems.setDetailsMessage(details);
          HistoryUtils.openUpload(aipId, representationId);
        }
      });
  }

  private void uploadFiles(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    if (file.isDirectory()) {
      Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
        RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
        new ActionNoAsyncCallback<String>(callback) {

          @Override
          public void onSuccess(String details) {
            doActionCallbackNone();
            LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
            selectedItems.setDetailsMessage(details);
            HistoryUtils.openUpload(file);
          }
        });
    }
  }

  private void createFolder(final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.createFolderTitle(), null, null, messages.createFolderPlaceholder(),
      RegExp.compile("^[^/]+$"), messages.cancelButton(), messages.confirmButton(), true, false,
      new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(final String newName) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(final String details) {
                Services services = new Services("Create folder under representation", "create");
                CreateFolderRequest request = new CreateFolderRequest();
                request.setAipId(aipId);
                request.setRepresentationId(representationId);
                request.setName(newName);
                request.setDetails(details);
                services.fileResource(s -> s.createFolderUnderRepresentation(request))
                  .whenComplete((file, throwable) -> {
                    if (throwable != null) {
                      if (throwable instanceof AlreadyExistsException) {
                        Dialogs.showInformationDialog(messages.createFolderAlreadyExistsTitle(),
                          messages.createFolderAlreadyExistsMessage(), messages.dialogOk(), false);
                      } else {
                        AsyncCallbackUtils.defaultFailureTreatment(throwable);
                      }
                    } else {
                      callback.onSuccess(ActionImpact.NONE);
                      HistoryUtils.openBrowse(file);
                    }
                  });
              }
            });
        }
      });
  }

  private void createFolder(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
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
                Services services = new Services("Create folder under representation", "create");
                CreateFolderRequest request = new CreateFolderRequest();
                request.setAipId(folder.getAipId());
                request.setRepresentationId(folder.getRepresentationId());
                request.setFolderUUID(folder.getUUID());
                request.setName(newName);
                request.setDetails(details);
                services.fileResource(s -> s.createFolderUnderRepresentation(request)).exceptionally(throwable -> {
                  Dialogs.showInformationDialog(messages.createFolderAlreadyExistsTitle(),
                    messages.createFolderAlreadyExistsMessage(), messages.dialogOk(), false);
                  return null;
                }).whenComplete((result, throwable) -> {
                  if (throwable != null) {
                    if (throwable instanceof AlreadyExistsException) {
                      Dialogs.showInformationDialog(messages.createFolderAlreadyExistsTitle(),
                        messages.createFolderAlreadyExistsMessage(), messages.dialogOk(), false);
                    } else {
                      AsyncCallbackUtils.defaultFailureTreatment(throwable);
                    }
                  } else {
                    callback.onSuccess(ActionImpact.NONE);
                    HistoryUtils.openBrowse(result);
                  }
                });
              }
            });
        }
      });
  }

  private void newProcess(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    newProcess(SelectedItemsList.create(IndexedFile.class.getName(), file.getUUID()), callback);
  }

  private void newProcess(SelectedItems<IndexedFile> selected, final AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void identifyFormats(IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    identifyFormats(SelectedItemsList.create(IndexedFile.class.getName(), file.getUUID()), callback);
  }

  private void identifyFormats(SelectedItems<IndexedFile> selected, final AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Create format identification job", "action");
    services.fileResource(s -> s.identifyFileFormat(SelectedItemsUtils.convertToRESTRequest(selected)))
      .whenComplete((job, throwable) -> {
        if (throwable == null) {
          Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());

          Dialogs.showJobRedirectDialog(messages.identifyFormatsJobCreatedMessage(),
            new ActionAsyncCallback<Void>(callback) {
              @Override
              public void onFailure(Throwable caught) {
                doActionCallbackUpdated();
              }

              @Override
              public void onSuccess(final Void nothing) {
                doActionCallbackNone();
                HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
              }
            });
        }
      });
  }

  private void remove(final IndexedFile file, final AsyncCallback<ActionImpact> callback) {
    remove(SelectedItemsList.create(IndexedFile.class, file.getUUID()), callback);
  }

  private void remove(final SelectedItems<IndexedFile> selected, final AsyncCallback<ActionImpact> callback) {
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
                  Services services = new Services("Remove files from representation", "delete");
                  DeleteRequest deleteRequest = new DeleteRequest();
                  deleteRequest.setDetails(details);
                  deleteRequest.setSelectedItemsToDelete(selected);
                  services.fileResource(s -> s.deleteFiles(deleteRequest)).whenComplete((job, throwable) -> {
                    if (throwable != null) {
                      callback.onFailure(throwable);
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    } else {
                      Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                        new ActionAsyncCallback<Void>(callback) {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                doActionCallbackDestroyed();
                                HistoryUtils.newHistory(HistoryUtils.getCurrentHistoryPath());
                              }
                            };

                            timer.schedule(RodaConstants.ACTION_TIMEOUT);
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            doActionCallbackNone();
                            HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                          }
                        });
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

  @Override
  public ActionableBundle<IndexedFile> createActionsBundle() {
    ActionableBundle<IndexedFile> fileActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedFile> managementGroup = new ActionableGroup<>(messages.sidebarFoldersFilesTitle());
    managementGroup.addButton(messages.moveButton(), FileSearchWrapperAction.MOVE, ActionImpact.UPDATED, "btn-edit",
      "fileMoveButton");
    managementGroup.addButton(messages.uploadFilesButton(), FileSearchWrapperAction.UPLOAD_FILES, ActionImpact.UPDATED,
      "btn-upload", "fileUploadButton");
    managementGroup.addButton(messages.createFolderButton(), FileSearchWrapperAction.CREATE_FOLDER,
      ActionImpact.UPDATED, "btn-plus-circle", "fileCreateFolderButton");
    managementGroup.addButton(messages.removeButton(), FileSearchWrapperAction.REMOVE, ActionImpact.DESTROYED,
      "btn-ban", "fileRemoveButton");

    // PRESERVATION
    ActionableGroup<IndexedFile> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), FileSearchWrapperAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play", "fileNewProcessButton");
    preservationGroup.addButton(messages.identifyFormatsButton(), FileSearchWrapperAction.IDENTIFY_FORMATS,
      ActionImpact.UPDATED, "btn-play", "fileIdentifyFormatsButton");

    fileActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return fileActionableBundle;
  }

  // MANAGEMENT
  public enum FileSearchWrapperAction implements Action<IndexedFile> {
    MOVE(RodaConstants.PERMISSION_METHOD_MOVE_FILES), REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_FILE),
    UPLOAD_FILES(RodaConstants.PERMISSION_METHOD_CREATE_FILE),
    CREATE_FOLDER(RodaConstants.PERMISSION_METHOD_CREATE_FOLDER),
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    IDENTIFY_FORMATS(RodaConstants.PERMISSION_METHOD_CREATE_JOB);

    private final List<String> methods;

    FileSearchWrapperAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
