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
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.UpdatePermissionsRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.MemberPermissionsSelectDialog;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisseminationActions extends AbstractActionable<IndexedDIP> {

  private static final DisseminationActions INSTANCE = new DisseminationActions(null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisseminationAction> POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION = new HashSet<>(
    Arrays.asList(DisseminationAction.values()));

  private static final Set<DisseminationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATIONS = new HashSet<>(
    Arrays.asList(DisseminationAction.REMOVE, DisseminationAction.NEW_PROCESS));

  private final Permissions permissions;

  private DisseminationActions(Permissions permissions) {
    this.permissions = permissions;
  }

  public static DisseminationActions get() {
    return INSTANCE;
  }

  public static DisseminationActions get(Permissions permissions) {
    return new DisseminationActions(permissions);
  }

  @Override
  public DisseminationAction[] getActions() {
    return DisseminationAction.values();
  }

  @Override
  public DisseminationAction actionForName(String name) {
    return DisseminationAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedDIP> action) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedDIP> action, IndexedDIP dip) {
    return new CanActResult(hasPermissions(action, dip.getPermissions()), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedDIP> action, IndexedDIP dip) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public CanActResult userCanAct(Action<IndexedDIP> action, SelectedItems<IndexedDIP> selectedItems) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedDIP> action, SelectedItems<IndexedDIP> selectedItems) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATIONS.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<IndexedDIP> action, IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    if (DisseminationAction.DOWNLOAD.equals(action)) {
      download(dissemination, callback);
    } else if (DisseminationAction.REMOVE.equals(action)) {
      remove(dissemination, callback);
    } else if (DisseminationAction.NEW_PROCESS.equals(action)) {
      newProcess(dissemination, callback);
    } else if (DisseminationAction.ADD_USER_PERMISSION.equals(action)) {
      addUserPermissions(dissemination, callback);
    } else if (DisseminationAction.ADD_GROUP_PERMISSION.equals(action)) {
      addGroupPermissions(dissemination, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Action<IndexedDIP> action, SelectedItems<IndexedDIP> selectedItems,
    AsyncCallback<ActionImpact> callback) {
    if (DisseminationAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (DisseminationAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createDipDownloadUri(dissemination.getUUID());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  private void remove(final IndexedDIP dip, AsyncCallback<ActionImpact> callback) {
    remove(objectToSelectedItems(dip, IndexedDIP.class), callback);
  }

  private void remove(final SelectedItems<IndexedDIP> selectedItems, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(final String details) {
                  Services services = new Services("Delete DIPs", "delete");
                  DeleteRequest deleteRequest = new DeleteRequest();
                  deleteRequest.setSelectedItemsToDelete(selectedItems);
                  deleteRequest.setDetails(details);

                  services.dipResource(s -> s.deleteDIPs(deleteRequest)).whenComplete((job, throwable) -> {
                    if (throwable != null) {
                      doActionCallbackNone();
                      Toast.showError("Unable to perform the actions", "");
                    } else {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                          doActionCallbackDestroyed();
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

  private void newProcess(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    newProcess(objectToSelectedItems(dissemination, IndexedDIP.class), callback);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void newProcess(SelectedItems<IndexedDIP> selected, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void addGroupPermissions(final IndexedDIP dip, final AsyncCallback<ActionImpact> callback) {
    MemberPermissionsSelectDialog dialog = new MemberPermissionsSelectDialog(messages.addGroupButton(),
      getMembersFilter(dip.getPermissions(), false), new AsyncCallback<MemberPermissionsSelectDialog.Result>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onSuccess(ActionImpact.NONE);
        }

        @Override
        public void onSuccess(MemberPermissionsSelectDialog.Result result) {
          Permissions newPermissions = new Permissions(dip.getPermissions());

          for (String groupName : result.getMemberNames()) {
            newPermissions.setGroupPermissions(groupName, result.getPermissions());
          }

          saveNewPermissions(dip, newPermissions, callback);
        }
      });

    dialog.showAndCenter();
  }

  private void addUserPermissions(final IndexedDIP dip, final AsyncCallback<ActionImpact> callback) {
    MemberPermissionsSelectDialog dialog = new MemberPermissionsSelectDialog(messages.addUserButton(),
      getMembersFilter(dip.getPermissions(), true), new AsyncCallback<MemberPermissionsSelectDialog.Result>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onSuccess(ActionImpact.NONE);
        }

        @Override
        public void onSuccess(MemberPermissionsSelectDialog.Result result) {
          Permissions newPermissions = new Permissions(dip.getPermissions());

          for (String username : result.getMemberNames()) {
            newPermissions.setUserPermissions(username, result.getPermissions());
          }

          saveNewPermissions(dip, newPermissions, callback);
        }
      });

    dialog.showAndCenter();
  }

  private void saveNewPermissions(IndexedDIP dip, Permissions newPermissions, AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
      new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(String details) {
          Services services = new Services("Update DIP permissions", "update");

          UpdatePermissionsRequest request = new UpdatePermissionsRequest();
          request.setPermissions(newPermissions);
          request.setDetails(details);
          request.setRecursive(false);
          request.setSelectedItems(SelectedItemsList.create(IndexedDIP.class.getName(), dip.getId()));

          services.dipResource(s -> s.updatePermissions(request)).whenComplete((job, throwable) -> {
            if (throwable != null) {
              callback.onFailure(throwable);
            } else {
              Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());
              dip.setPermissions(newPermissions);
              callback.onSuccess(ActionImpact.UPDATED);
            }
          });
        }
      });
  }

  // creates filter to remove any user or group already in dip's permissions
  private Filter getMembersFilter(Permissions dipPermissions, boolean isUser) {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, Boolean.toString(isUser)));

    if (dipPermissions != null) {
      if (isUser && dipPermissions.getUsernames() != null) {
        for (String username : dipPermissions.getUsernames()) {
          filter.add(new NotSimpleFilterParameter(RodaConstants.MEMBERS_ID, username));
        }
      } else if (!isUser && dipPermissions.getGroupnames() != null) {
        for (String groupName : dipPermissions.getGroupnames()) {
          filter.add(new NotSimpleFilterParameter(RodaConstants.MEMBERS_ID, groupName));
        }
      }
    }

    return filter;
  }

  @Override
  public ActionableBundle<IndexedDIP> createActionsBundle() {
    ActionableBundle<IndexedDIP> dipActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedDIP> managementGroup = new ActionableGroup<>(
      messages.viewRepresentationFileDisseminationTitle(), "btn-edit");
    managementGroup.addButton(messages.downloadButton(), DisseminationAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");
    managementGroup.addButton(messages.removeButton(), DisseminationAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");
    managementGroup.addButton(messages.addUserButton(), DisseminationAction.ADD_USER_PERMISSION, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.addGroupButton(), DisseminationAction.ADD_GROUP_PERMISSION, ActionImpact.UPDATED,
      "btn-plus-circle");

    // PRESERVATION
    ActionableGroup<IndexedDIP> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), DisseminationAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    dipActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return dipActionableBundle;
  }

  public enum DisseminationAction implements Action<IndexedDIP> {
    DOWNLOAD(), REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_DIP),
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    ADD_USER_PERMISSION(RodaConstants.PERMISSION_METHOD_UPDATE_DIP_PERMISSIONS),
    ADD_GROUP_PERMISSION(RodaConstants.PERMISSION_METHOD_UPDATE_DIP_PERMISSIONS);

    private List<String> methods;

    DisseminationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
