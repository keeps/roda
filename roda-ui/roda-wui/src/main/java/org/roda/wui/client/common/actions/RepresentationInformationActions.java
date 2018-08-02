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
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.CreateRepresentationInformation;
import org.roda.wui.client.planning.EditRepresentationInformation;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RepresentationInformationActions extends AbstractActionable<RepresentationInformation> {
  private static final RepresentationInformationActions INSTANCE = new RepresentationInformationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_WITHOUT_RI = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.NEW));

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_ON_SINGLE_RI = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.REMOVE, RepresentationInformationAction.START_PROCESS,
      RepresentationInformationAction.EDIT, RepresentationInformationAction.DOWNLOAD));

  private static final Set<RepresentationInformationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_RI = new HashSet<>(
    Arrays.asList(RepresentationInformationAction.REMOVE, RepresentationInformationAction.START_PROCESS));

  private RepresentationInformationActions() {
    // do nothing
  }

  public enum RepresentationInformationAction implements Action<RepresentationInformation> {
    NEW, REMOVE, START_PROCESS, EDIT, DOWNLOAD
  }

  @Override
  public RepresentationInformationAction actionForName(String name) {
    return RepresentationInformationAction.valueOf(name);
  }

  public static RepresentationInformationActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Action<RepresentationInformation> action) {
    return POSSIBLE_ACTIONS_WITHOUT_RI.contains(action);
  }

  @Override
  public boolean canAct(Action<RepresentationInformation> action, RepresentationInformation object) {
    return POSSIBLE_ACTIONS_ON_SINGLE_RI.contains(action);
  }

  @Override
  public boolean canAct(Action<RepresentationInformation> action, SelectedItems<RepresentationInformation> objects) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_RI.contains(action);
  }

  @Override
  public void act(Action<RepresentationInformation> action, AsyncCallback<ActionImpact> callback) {
    if (RepresentationInformationAction.NEW.equals(action)) {
      create(callback);
    } else {
      unsupportedAction(action, callback);
    }
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
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objects);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void download(RepresentationInformation object, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createRepresentationInformationDownloadUri(object.getId());
    Window.Location.assign(downloadUri.asString());
    callback.onSuccess(ActionImpact.NONE);
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
                BrowserService.Util.getInstance().deleteRepresentationInformation(objects, new AsyncCallback<Job>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    HistoryUtils.newHistory(InternalProcess.RESOLVER);
                    callback.onFailure(caught);
                  }

                  @Override
                  public void onSuccess(Job result) {
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
                          HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                          doActionCallbackDestroyed();
                        }
                      });
                  }
                });
              }
            }
          });
      }
    });
  }

  private void create(AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateRepresentationInformation.RESOLVER);
    callback.onSuccess(ActionImpact.NONE);
  }

  private void edit(RepresentationInformation object, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(EditRepresentationInformation.RESOLVER, object.getId());
    callback.onSuccess(ActionImpact.NONE);
  }

  @Override
  public ActionableBundle<RepresentationInformation> createActionsBundle() {
    ActionableBundle<RepresentationInformation> formatActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<RepresentationInformation> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newButton(), RepresentationInformationAction.NEW, ActionImpact.UPDATED,
      "btn-plus");
    managementGroup.addButton(messages.editButton(), RepresentationInformationAction.EDIT, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.downloadButton(), RepresentationInformationAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");
    managementGroup.addButton(messages.removeButton(), RepresentationInformationAction.REMOVE, ActionImpact.DESTROYED,
      "btn-ban");

    // PRESERVATION
    ActionableGroup<RepresentationInformation> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.formatRegisterProcessButton(), RepresentationInformationAction.START_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    formatActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);

    return formatActionableBundle;
  }
}
