/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectTransferResourceDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.ingest.transfer.TransferUpload;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class TransferredResourceActions extends AbstractActionable<TransferredResource> {

  public static final TransferredResource NO_TRANSFERRED_RESOURCE = null;

  private static final TransferredResourceActions GENERAL_INSTANCE = new TransferredResourceActions(
    NO_TRANSFERRED_RESOURCE);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<TransferredResourceAction> POSSIBLE_ACTIONS_WITHOUT_TRANSFERRED_RESOURCE = new HashSet<>(
    Arrays.asList(TransferredResourceAction.REFRESH, TransferredResourceAction.UPLOAD,
      TransferredResourceAction.NEW_FOLDER));

  private static final Set<TransferredResourceAction> POSSIBLE_ACTIONS_ON_FOLDER_TRANSFERRED_RESOURCE = new HashSet<>(
    Arrays.asList(TransferredResourceAction.RENAME, TransferredResourceAction.MOVE, TransferredResourceAction.REMOVE,
      TransferredResourceAction.NEW_PROCESS));

  private static final Set<TransferredResourceAction> POSSIBLE_ACTIONS_ON_FILE_TRANSFERRED_RESOURCE = new HashSet<>(
    Arrays.asList(TransferredResourceAction.RENAME, TransferredResourceAction.MOVE, TransferredResourceAction.REMOVE,
      TransferredResourceAction.NEW_PROCESS));

  private static final Set<TransferredResourceAction> POSSIBLE_ACTIONS_ON_MULTIPLE_TRANSFERRED_RESOURCES = new HashSet<>(
    Arrays.asList(TransferredResourceAction.MOVE, TransferredResourceAction.REMOVE,
      TransferredResourceAction.NEW_PROCESS));

  private final TransferredResource parentTransferredResource;

  private TransferredResourceActions(TransferredResource parentTransferredResource) {
    this.parentTransferredResource = parentTransferredResource;
  }

  public enum TransferredResourceAction implements Action<TransferredResource> {
    REFRESH(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    RENAME(RodaConstants.PERMISSION_METHOD_RENAME_TRANSFERRED_RESOURCE),
    MOVE(RodaConstants.PERMISSION_METHOD_MOVE_TRANSFERRED_RESOURCE),
    UPLOAD(RodaConstants.PERMISSION_METHOD_CREATE_TRANSFERRED_RESOURCE_FILE),
    NEW_FOLDER(RodaConstants.PERMISSION_METHOD_CREATE_TRANSFERRED_RESOURCE_FOLDER),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_TRANSFERRED_RESOURCE),
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB);

    private List<String> methods;

    TransferredResourceAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public TransferredResourceAction[] getActions() {
    return TransferredResourceAction.values();
  }

  @Override
  public TransferredResourceAction actionForName(String name) {
    return TransferredResourceAction.valueOf(name);
  }

  public static TransferredResourceActions get(TransferredResource parentTransferredResource) {
    if (parentTransferredResource == NO_TRANSFERRED_RESOURCE) {
      return GENERAL_INSTANCE;
    } else {
      return new TransferredResourceActions(parentTransferredResource);
    }
  }

  @Override
  public CanActResult userCanAct(Action<TransferredResource> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<TransferredResource> action) {
    return new CanActResult(POSSIBLE_ACTIONS_WITHOUT_TRANSFERRED_RESOURCE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonNoObjectSelected());
  }

  @Override
  public CanActResult userCanAct(Action<TransferredResource> action, TransferredResource object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());

  }

  @Override
  public CanActResult contextCanAct(Action<TransferredResource> action, TransferredResource object) {
    if (object.isFile()) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_FILE_TRANSFERRED_RESOURCE.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonCantActOnFileBitstream());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_FOLDER_TRANSFERRED_RESOURCE.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonCantActOnFileDirectory());
    }
  }

  @Override
  public CanActResult userCanAct(Action<TransferredResource> action, SelectedItems<TransferredResource> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<TransferredResource> action, SelectedItems<TransferredResource> objects) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_TRANSFERRED_RESOURCES.contains(action),
      CanActResult.Reason.CONTEXT, messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<TransferredResource> action, AsyncCallback<ActionImpact> callback) {
    if (action.equals(TransferredResourceAction.REFRESH)) {
      refresh(callback);
    } else if (action.equals(TransferredResourceAction.UPLOAD)) {
      upload(callback);
    } else if (action.equals(TransferredResourceAction.NEW_FOLDER)) {
      newFolder(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<TransferredResource> action, TransferredResource object,
    AsyncCallback<ActionImpact> callback) {
    if (action.equals(TransferredResourceAction.REFRESH)) {
      refresh(object, callback);
    } else if (action.equals(TransferredResourceAction.RENAME)) {
      rename(object, callback);
    } else if (action.equals(TransferredResourceAction.MOVE)) {
      move(objectToSelectedItems(object, TransferredResource.class), callback);
    } else if (action.equals(TransferredResourceAction.UPLOAD)) {
      upload(object, callback);
    } else if (action.equals(TransferredResourceAction.NEW_FOLDER)) {
      newFolder(object, callback);
    } else if (action.equals(TransferredResourceAction.REMOVE)) {
      remove(objectToSelectedItems(object, TransferredResource.class), callback);
    } else if (action.equals(TransferredResourceAction.NEW_PROCESS)) {
      newProcess(objectToSelectedItems(object, TransferredResource.class), callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<TransferredResource> action, SelectedItems<TransferredResource> objects,
    AsyncCallback<ActionImpact> callback) {
    if (action.equals(TransferredResourceAction.REFRESH)) {
      refresh(callback);
    } else if (action.equals(TransferredResourceAction.MOVE)) {
      move(objects, callback);
    } else if (action.equals(TransferredResourceAction.UPLOAD)) {
      upload(callback);
    } else if (action.equals(TransferredResourceAction.NEW_FOLDER)) {
      newFolder(callback);
    } else if (action.equals(TransferredResourceAction.REMOVE)) {
      remove(objects, callback);
    } else if (action.equals(TransferredResourceAction.NEW_PROCESS)) {
      newProcess(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void newProcess(SelectedItems<TransferredResource> objects, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    selectedItems.setSelectedItems(objects);
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_INGEST);
    callback.onSuccess(ActionImpact.NONE);
  }

  private void rename(TransferredResource object, AsyncCallback<ActionImpact> callback) {
    Services service = new Services("Renaming transferred resource", "rename");

    service.transferredResource(s -> s.getResource(object.getUUID())).whenComplete((value, error) -> {
      if (value != null) {
        Dialogs.showPromptDialog(messages.renameTransferredResourcesDialogTitle(), null, value.getName(), null,
          RegExp.compile("^[^/]*$"), messages.cancelButton(), messages.confirmButton(), true, false,
          new ActionNoAsyncCallback<String>(callback) {

            @Override
            public void onSuccess(String result) {
              service.transferredResource(s -> s.renameTransferredResource(object.getUUID(), result, true))
                .whenComplete((value, error) -> {
                  if (value != null) {
                    Toast.showInfo(messages.dialogSuccess(), messages.renameSIPSuccessful());
                    HistoryUtils.newHistory(IngestTransfer.RESOLVER, value.getUUID());
                    doActionCallbackNone();
                  } else if (error != null) {
                    Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
                    callback.onFailure(error);
                  }
                });
            }
          });
      } else if (error != null) {
        Toast.showInfo(messages.dialogFailure(), messages.renameSIPFailed());
        callback.onFailure(error);
      }
    });
  }

  private void move(SelectedItems<TransferredResource> objects, AsyncCallback<ActionImpact> callback) {
    Services service = new Services("Moving transferred resource", "move");

    service
      .transferredResource(s -> s.getSelectedTransferredResources(SelectedItemsUtils.convertToRESTRequest(objects)))
      .whenComplete((resources, error) -> {
        if (resources != null && resources.getObjects() != null) {
          Filter filter = new Filter();
          filter.add(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, Boolean.FALSE.toString()));

          boolean moveToRootVisible = false;
          if (!resources.getObjects().isEmpty() && resources.getObjects().get(0) != NO_TRANSFERRED_RESOURCE
            && StringUtils.isNotBlank(resources.getObjects().get(0).getParentUUID())) {
            filter.add(
              new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, resources.getObjects().get(0).getParentUUID()));
            moveToRootVisible = true;
          }

          SelectTransferResourceDialog dialog = new SelectTransferResourceDialog(messages.selectParentTitle(), filter);

          if (resources.getObjects().size() <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
            dialog.addStyleName("object-dialog");
          }
          dialog.setEmptyParentButtonVisible(moveToRootVisible);
          dialog.showAndCenter();
          dialog.addCloseHandler(e -> callback.onSuccess(ActionImpact.NONE));
          dialog.addValueChangeHandler(event -> {
            TransferredResource transferredResource = event.getValue();
            String resourceId = transferredResource == null ? null : transferredResource.getUUID();
            service
              .transferredResource(
                s -> s.moveTransferredResources(SelectedItemsUtils.convertToRESTRequest(objects), resourceId))
              .whenComplete((result, err) -> {
                if (result != null) {
                  Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          if (transferredResource != null) {
                            HistoryUtils.newHistory(IngestTransfer.RESOLVER, transferredResource.getUUID());
                          } else {
                            HistoryUtils.newHistory(IngestTransfer.RESOLVER);
                          }
                          callback.onSuccess(Actionable.ActionImpact.UPDATED);
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                      callback.onSuccess(Actionable.ActionImpact.NONE);
                    }
                  });
                } else if (err != null) {
                  Toast.showError(messages.dialogFailure(), messages.moveSIPFailed());
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                  callback.onSuccess(Actionable.ActionImpact.UPDATED);
                }
              });
          });
        } else if (error != null) {
          Toast.showInfo(messages.dialogFailure(), messages.moveSIPFailed());
          callback.onSuccess(Actionable.ActionImpact.UPDATED);
        }
      });
  }

  private void remove(SelectedItems<TransferredResource> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(TransferredResource.class, objects, new ActionNoAsyncCallback<Long>(callback) {
      Services service = new Services("Remove transferred resource", "remove");

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.ingestTransferRemoveFolderConfirmDialogTitle(),
          messages.ingestTransferRemoveSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                service
                  .transferredResource(s -> s.deleteMultipleResources(SelectedItemsUtils.convertToRESTRequest(objects)))
                  .whenComplete((value, error) -> {
                    if (error == null) {
                      Toast.showInfo(messages.removeSuccessTitle(), messages.removeSuccessMessage(size));
                      doActionCallbackDestroyed();
                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void refresh(AsyncCallback<ActionImpact> callback) {
    refresh(parentTransferredResource, callback);
  }

  private void refresh(TransferredResource object, AsyncCallback<ActionImpact> callback) {
    String relativePath = object != NO_TRANSFERRED_RESOURCE ? object.getRelativePath() : null;
    Services services = new Services("Refresh transferred resource", "refresh");
    services.transferredResource(s -> s.refreshTransferResource(relativePath)).whenComplete((value, error) -> {
      if (error == null) {
        Toast.showInfo(messages.dialogRefresh(), messages.updatedFilesUnderFolder());
        callback.onSuccess(Actionable.ActionImpact.UPDATED);
        History.fireCurrentHistoryState();
      } else {
        if (error instanceof IsStillUpdatingException) {
          Toast.showInfo(messages.dialogRefresh(), messages.updateIsCurrentlyRunning());
        } else {
          callback.onFailure(error);
        }
      }
    });
  }

  private void upload(AsyncCallback<ActionImpact> callback) {
    upload(parentTransferredResource, callback);
  }

  private void upload(TransferredResource object, AsyncCallback<ActionImpact> callback) {
    if (object != NO_TRANSFERRED_RESOURCE) {
      HistoryUtils.newHistory(TransferUpload.INGEST_RESOLVER, object.getUUID());
    } else {
      HistoryUtils.newHistory(TransferUpload.INGEST_RESOLVER);
    }
    callback.onSuccess(ActionImpact.NONE);
  }

  private void newFolder(AsyncCallback<ActionImpact> callback) {
    newFolder(parentTransferredResource, callback);
  }

  private void newFolder(TransferredResource object, AsyncCallback<ActionImpact> callback) {
    Services services = new Services("Renaming transferred resource", "rename");
    Dialogs.showPromptDialog(messages.ingestTransferCreateFolderTitle(), messages.ingestTransferCreateFolderMessage(),
      null, null, RegExp.compile("^[^/]+$"), messages.dialogCancel(), messages.dialogOk(), true, false,
      new ActionNoAsyncCallback<String>(callback) {
        @Override
        public void onSuccess(String folderName) {
          services.transferredResource(
            s -> s.createTransferredResourcesFolder(object != NO_TRANSFERRED_RESOURCE ? object.getUUID() : null,
              folderName, true))
            .whenComplete((value, error) -> {
              if (value != null) {
                HistoryUtils.newHistory(IngestTransfer.RESOLVER, value.getUUID());
                doActionCallbackUpdated();
              }
            });
        }
      });
  }

  @Override
  public ActionableBundle<TransferredResource> createActionsBundle() {
    ActionableBundle<TransferredResource> transferredResourcesActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<TransferredResource> managementGroup = new ActionableGroup<>(messages.sidebarFoldersFilesTitle());
    managementGroup.addButton(messages.refreshButton(), TransferredResourceAction.REFRESH, ActionImpact.UPDATED,
      "btn-refresh");
    // TODO: add title:
    // messages.ingestTransferLastScanned(resource.getLastScanDate())
    managementGroup.addButton(messages.renameButton(), TransferredResourceAction.RENAME, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.moveButton(), TransferredResourceAction.MOVE, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.uploadFilesButton(), TransferredResourceAction.UPLOAD, ActionImpact.NONE,
      "btn-upload");
    managementGroup.addButton(messages.createFolderButton(), TransferredResourceAction.NEW_FOLDER, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.removeWholeFolderButton(), TransferredResourceAction.REMOVE,
      ActionImpact.DESTROYED, "btn-danger btn-ban");

    ActionableGroup<TransferredResource> preservationGroup = new ActionableGroup<>(messages.sidebarIngestTitle());
    preservationGroup.addButton(messages.ingestWholeFolderButton(), TransferredResourceAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    transferredResourcesActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return transferredResourcesActionableBundle;
  }
}
