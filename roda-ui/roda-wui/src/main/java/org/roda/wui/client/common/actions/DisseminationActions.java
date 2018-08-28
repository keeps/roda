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
import java.util.Optional;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionLoadingAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisseminationActions extends AbstractActionable<IndexedDIP> {

  private static final DisseminationActions INSTANCE = new DisseminationActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisseminationAction> POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION = new HashSet<>(
    Arrays.asList(DisseminationAction.values()));

  private static final Set<DisseminationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATIONS = new HashSet<>(
    Arrays.asList(DisseminationAction.REMOVE, DisseminationAction.NEW_PROCESS, DisseminationAction.UPDATE_PERMISSIONS));

  private DisseminationActions() {
  }

  public enum DisseminationAction implements Action<IndexedDIP> {
    DOWNLOAD(), REMOVE("org.roda.wui.api.controllers.Browser.delete(IndexedDIP)"),
    NEW_PROCESS("org.roda.wui.api.controllers.Jobs.createJob"),
    UPDATE_PERMISSIONS("org.roda.wui.api.controllers.Browser.updateDIPPermissions");

    private List<String> methods;

    DisseminationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public DisseminationAction actionForName(String name) {
    return DisseminationAction.valueOf(name);
  }

  public static DisseminationActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Action<IndexedDIP> action, IndexedDIP dip) {
    return hasPermissions(action, dip.getPermissions()) && POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION.contains(action);
  }

  @Override
  public boolean canAct(Action<IndexedDIP> action, SelectedItems<IndexedDIP> selectedItems) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATIONS.contains(action);
  }

  @Override
  public void act(Action<IndexedDIP> action, IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    if (DisseminationAction.DOWNLOAD.equals(action)) {
      download(dissemination, callback);
    } else if (DisseminationAction.REMOVE.equals(action)) {
      remove(dissemination, callback);
    } else if (DisseminationAction.NEW_PROCESS.equals(action)) {
      newProcess(dissemination, callback);
    } else if (DisseminationAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(dissemination, callback);
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
    } else if (DisseminationAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createDipDownloadUri(dissemination.getUUID());

    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }

    callback.onSuccess(ActionImpact.NONE);
  }

  private void remove(final IndexedDIP dip, AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIPs(objectToSelectedItems(dip, IndexedDIP.class),
              new ActionLoadingAsyncCallback<Void>(callback) {

                @Override
                public void onSuccessImpl(Void result) {
                  if (!dip.getFileIds().isEmpty()) {
                    FileLink link = dip.getFileIds().get(0);
                    HistoryUtils.openBrowse(link.getAipId(), link.getRepresentationId(), link.getPath(),
                      link.getFileId());
                  } else if (!dip.getRepresentationIds().isEmpty()) {
                    RepresentationLink link = dip.getRepresentationIds().get(0);
                    HistoryUtils.openBrowse(link.getAipId(), link.getRepresentationId());
                  } else if (!dip.getAipIds().isEmpty()) {
                    AIPLink link = dip.getAipIds().get(0);
                    HistoryUtils.openBrowse(link.getAipId());
                  }
                  doActionCallbackDestroyed();
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void remove(final SelectedItems<IndexedDIP> selectedItems, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIPs(selectedItems, new ActionLoadingAsyncCallback<Void>(callback) {

              @Override
              public void onSuccessImpl(Void result) {
                History.fireCurrentHistoryState();
                doActionCallbackDestroyed();
              }
            });
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

  private void updatePermissions(IndexedDIP dip, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(BrowseTop.RESOLVER, EditPermissions.DIP_RESOLVER.getHistoryToken(), dip.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void updatePermissions(SelectedItems<IndexedDIP> dips, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    LastSelectedItemsSingleton.getInstance().setSelectedItems(dips);
    HistoryUtils.newHistory(BrowseTop.RESOLVER, EditPermissions.DIP_RESOLVER.getHistoryToken());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  @Override
  public ActionableBundle<IndexedDIP> createActionsBundle() {
    ActionableBundle<IndexedDIP> dipActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedDIP> managementGroup = new ActionableGroup<>(
      messages.viewRepresentationFileDisseminationTitle());
    managementGroup.addButton(messages.downloadButton(), DisseminationAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");
    managementGroup.addButton(messages.removeButton(), DisseminationAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");
    managementGroup.addButton(messages.disseminationPermissions(), DisseminationAction.UPDATE_PERMISSIONS,
      ActionImpact.UPDATED, "btn-edit");

    // PRESERVATION
    ActionableGroup<IndexedDIP> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), DisseminationAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    dipActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return dipActionableBundle;
  }
}
