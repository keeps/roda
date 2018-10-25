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
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.planning.CreateFormat;
import org.roda.wui.client.planning.EditFormat;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class FormatActions extends AbstractActionable<Format> {
  private static final FormatActions INSTANCE = new FormatActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<FormatAction> POSSIBLE_ACTIONS_WITHOUT_FORMAT = new HashSet<>(
    Arrays.asList(FormatAction.NEW));

  private static final Set<FormatAction> POSSIBLE_ACTIONS_ON_SINGLE_FORMAT = new HashSet<>(
    Arrays.asList(FormatAction.REMOVE, FormatAction.START_PROCESS, FormatAction.EDIT));

  private static final Set<FormatAction> POSSIBLE_ACTIONS_ON_MULTIPLE_FORMATS = new HashSet<>(
    Arrays.asList(FormatAction.REMOVE, FormatAction.START_PROCESS));

  private FormatActions() {
    // do nothing
  }

  public enum FormatAction implements Action<Format> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_FORMAT), REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_FORMAT),
    START_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB), EDIT(RodaConstants.PERMISSION_METHOD_UPDATE_FORMAT);

    private List<String> methods;

    FormatAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public FormatAction[] getActions() {
    return FormatAction.values();
  }

  @Override
  public FormatAction actionForName(String name) {
    return FormatAction.valueOf(name);
  }

  public static FormatActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Action<Format> action) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_WITHOUT_FORMAT.contains(action);
  }

  @Override
  public boolean canAct(Action<Format> action, Format object) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_ON_SINGLE_FORMAT.contains(action);
  }

  @Override
  public boolean canAct(Action<Format> action, SelectedItems<Format> objects) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_ON_MULTIPLE_FORMATS.contains(action);
  }

  @Override
  public void act(Action<Format> action, AsyncCallback<ActionImpact> callback) {
    if (FormatAction.NEW.equals(action)) {
      create(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<Format> action, Format object, AsyncCallback<ActionImpact> callback) {
    if (FormatAction.REMOVE.equals(action)) {
      remove(objectToSelectedItems(object, Format.class), callback);
    } else if (FormatAction.START_PROCESS.equals(action)) {
      startProcess(objectToSelectedItems(object, Format.class), callback);
    } else if (FormatAction.EDIT.equals(action)) {
      edit(object, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<Format> action, SelectedItems<Format> objects, AsyncCallback<ActionImpact> callback) {
    if (FormatAction.REMOVE.equals(action)) {
      remove(objects, callback);
    } else if (FormatAction.START_PROCESS.equals(action)) {
      startProcess(objects, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void startProcess(SelectedItems<Format> objects, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(objects);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void remove(SelectedItems<Format> formats, AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(Format.class, formats, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.formatRemoveFolderConfirmDialogTitle(),
          messages.formatRemoveSelectedConfirmDialogMessage(size), messages.formatRemoveFolderConfirmDialogCancel(),
          messages.formatRemoveFolderConfirmDialogOk(), new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                BrowserService.Util.getInstance().deleteFormat(formats, new AsyncCallback<Job>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                    HistoryUtils.newHistory(InternalProcess.RESOLVER);
                  }

                  @Override
                  public void onSuccess(Job result) {
                    Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        Timer timer = new Timer() {
                          @Override
                          public void run() {
                            Toast.showInfo(messages.formatRemoveSuccessTitle(),
                              messages.formatRemoveSuccessMessage(size));
                            doActionCallbackDestroyed();
                          }
                        };

                        timer.schedule(RodaConstants.ACTION_TIMEOUT);
                      }

                      @Override
                      public void onSuccess(final Void nothing) {
                        doActionCallbackNone();
                        HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
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
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(CreateFormat.RESOLVER);
  }

  private void edit(Format format, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    HistoryUtils.newHistory(EditFormat.RESOLVER, format.getId());
  }

  @Override
  public ActionableBundle<Format> createActionsBundle() {
    ActionableBundle<Format> formatActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<Format> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.newButton(), FormatAction.NEW, ActionImpact.UPDATED, "btn-plus");
    managementGroup.addButton(messages.editButton(), FormatAction.EDIT, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.removeButton(), FormatAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<Format> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.formatRegisterProcessButton(), FormatAction.START_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    formatActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return formatActionableBundle;
  }
}
