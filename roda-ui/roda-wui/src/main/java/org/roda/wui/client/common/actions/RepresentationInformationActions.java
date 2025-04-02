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
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationFilterRequest;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RepresentationInformationDialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.CreateRepresentationInformation;
import org.roda.wui.client.planning.EditRepresentationInformation;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

import javax.naming.Context;

public class RepresentationInformationActions extends AbstractActionable<RepresentationInformation> {
  private static final RepresentationInformationActions INSTANCE = new RepresentationInformationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_WITHOUT_RI = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.NEW));

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_WITHOUT_RI_ASSOCIATING = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.ASSOCIATE_WITH_NEW,
      RepresentationInformationAction.ASSOCIATE_WITH_EXISTING));

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_ON_SINGLE_RI = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.REMOVE, RepresentationInformationAction.START_PROCESS,
      RepresentationInformationAction.EDIT, RepresentationInformationAction.DOWNLOAD));

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_RI = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.REMOVE, RepresentationInformationAction.START_PROCESS));

  private final Filter objectsToAssociate;

  private RepresentationInformationActions() {
    this.objectsToAssociate = null;
  }

  private RepresentationInformationActions(Filter objectsToAssociate) {
    this.objectsToAssociate = objectsToAssociate;
  }

  public enum RepresentationInformationAction implements Action<RepresentationInformation> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_REPRESENTATION_INFORMATION),
    ASSOCIATE_WITH_NEW(RodaConstants.PERMISSION_METHOD_CREATE_REPRESENTATION_INFORMATION),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_REPRESENTATION_INFORMATION),
    START_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_REPRESENTATION_INFORMATION),
    ASSOCIATE_WITH_EXISTING(RodaConstants.PERMISSION_METHOD_UPDATE_REPRESENTATION_INFORMATION), DOWNLOAD();

    private List<String> methods;

    RepresentationInformationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public RepresentationInformationAction[] getActions() {
    return RepresentationInformationAction.values();
  }

  @Override
  public RepresentationInformationAction actionForName(String name) {
    return RepresentationInformationAction.valueOf(name);
  }

  public static RepresentationInformationActions get() {
    return INSTANCE;
  }

  public static RepresentationInformationActions getForAssociation(Filter objectsToAssociate) {
    return new RepresentationInformationActions(objectsToAssociate);
  }

  @Override
  public CanActResult userCanAct(Action<RepresentationInformation> action) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<RepresentationInformation> action) {
    return new CanActResult(
      objectsToAssociate == null ? POSSIBLE_ACTIONS_WITHOUT_RI.contains(action)
        : POSSIBLE_ACTIONS_WITHOUT_RI_ASSOCIATING.contains(action),
      CanActResult.Reason.CONTEXT, messages.reasonInvalidContext());
  }

  @Override
  public CanActResult userCanAct(Action<RepresentationInformation> action, RepresentationInformation object) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<RepresentationInformation> action, RepresentationInformation object) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_RI.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnSingleObject());
  }

  @Override
  public CanActResult userCanAct(Action<RepresentationInformation> action,
    SelectedItems<RepresentationInformation> objects) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<RepresentationInformation> action,
    SelectedItems<RepresentationInformation> objects) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_RI.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
  }

  @Override
  public void act(Action<RepresentationInformation> action, AsyncCallback<ActionImpact> callback) {
    if (RepresentationInformationAction.NEW.equals(action)) {
      create(callback);
    } else if (RepresentationInformationAction.ASSOCIATE_WITH_NEW.equals(action)) {
      associateWithNew(callback);
    } else if (RepresentationInformationAction.ASSOCIATE_WITH_EXISTING.equals(action)) {
      associateWithExisting(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void associateWithExisting(AsyncCallback<ActionImpact> callback) {
    RepresentationInformationDialogs.showPromptAddRepresentationInformationWithAssociation(
      RepresentationInformationAssociations.getAssociateWithExistingDialogTitle(), messages.cancelButton(),
      messages.addToExistingRepresentationInformation(), messages.createNewRepresentationInformation(),
      new ActionAsyncCallback<SelectedItems<RepresentationInformation>>(callback) {
        @Override
        public void onSuccess(final SelectedItems<RepresentationInformation> selectedItems) {
          if (selectedItems != null) {
            String filterToAdd = HistoryUtils.getCurrentHistoryPath()
              .get(HistoryUtils.getCurrentHistoryPath().size() - 1);

            Services services = new Services("Update representation information with filter", "update");
            RepresentationInformationFilterRequest request = new RepresentationInformationFilterRequest();
            request.setSelectedItems(selectedItems);
            request.setFilterToAdd(filterToAdd);
            services.representationInformationResource(s -> s.addFilterToRepresentationInformation(request))
              .whenComplete((job, throwable) -> {
                if (throwable == null) {
                  Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      doActionCallbackUpdated();
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            associateWithNew(callback);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          doActionCallbackNone();
        }
      });
  }

  private void associateWithNew(AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateRepresentationInformation.RESOLVER);
  }

  @Override
  public void act(Action<RepresentationInformation> action, RepresentationInformation object,
    AsyncCallback<ActionImpact> callback) {
    if (RepresentationInformationAction.REMOVE.equals(action)) {
      remove(objectToSelectedItems(object, RepresentationInformation.class), callback);
    } else if (RepresentationInformationAction.START_PROCESS.equals(action)) {
      startProcess(objectToSelectedItems(object, RepresentationInformation.class), callback);
    } else if (RepresentationInformationAction.EDIT.equals(action)) {
      edit(object, callback);
    } else if (RepresentationInformationAction.DOWNLOAD.equals(action)) {
      download(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<RepresentationInformation> action, SelectedItems<RepresentationInformation> objects,
    AsyncCallback<ActionImpact> callback) {
    if (RepresentationInformationAction.REMOVE.equals(action)) {
      remove(objects, callback);
    } else if (RepresentationInformationAction.START_PROCESS.equals(action)) {
      startProcess(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void startProcess(SelectedItems<RepresentationInformation> objects, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objects);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void download(RepresentationInformation object, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createRepresentationInformationDownloadUri(object.getId());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  private void remove(SelectedItems<RepresentationInformation> objects, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(RepresentationInformation.class, objects, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.representationInformationRemoveFolderConfirmDialogTitle(),
          messages.representationInformationRemoveSelectedConfirmDialogMessage(size),
          messages.representationInformationRemoveFolderConfirmDialogCancel(),
          messages.representationInformationRemoveFolderConfirmDialogOk(),
          new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Services services = new Services("Delete representation information", "delete");
                services
                  .representationInformationResource(
                    s -> s.deleteMultipleRepresentationInformation(SelectedItemsUtils.convertToRESTRequest(objects)))
                  .whenComplete((job, throwable) -> {
                    if (throwable == null) {
                      Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                        new ActionAsyncCallback<Void>(callback) {

                          @Override
                          public void onFailure(Throwable caught) {
                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                Toast.showInfo(messages.representationInformationRemoveSuccessTitle(),
                                  messages.representationInformationRemoveSuccessMessage(size));
                                doActionCallbackDestroyed();
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
                    } else {
                      HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    }
                  });
              }
            }
          });
      }
    });
  }

  private void create(AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateRepresentationInformation.RESOLVER);
  }

  private void edit(RepresentationInformation object, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(EditRepresentationInformation.RESOLVER, object.getId());
  }

  @Override
  public ActionableBundle<RepresentationInformation> createActionsBundle() {
    ActionableBundle<RepresentationInformation> formatActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<RepresentationInformation> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newButton(), RepresentationInformationAction.NEW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.createNewRepresentationInformation(),
      RepresentationInformationAction.ASSOCIATE_WITH_NEW, ActionImpact.UPDATED, "btn-plus-circle");
    managementGroup.addButton(messages.editButton(), RepresentationInformationAction.EDIT, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.addToExistingRepresentationInformation(),
      RepresentationInformationAction.ASSOCIATE_WITH_EXISTING, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.downloadButton(), RepresentationInformationAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");
    managementGroup.addButton(messages.removeButton(), RepresentationInformationAction.REMOVE, ActionImpact.DESTROYED,
      "btn-ban");

    // PRESERVATION
    ActionableGroup<RepresentationInformation> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), RepresentationInformationAction.START_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    formatActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return formatActionableBundle;
  }
}
