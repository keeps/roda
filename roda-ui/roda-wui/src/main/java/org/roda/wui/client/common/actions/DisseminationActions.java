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
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.browse.BrowseAIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

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

  public enum DisseminationAction implements Actionable.Action<IndexedDIP> {
    DOWNLOAD, REMOVE, NEW_PROCESS, UPDATE_PERMISSIONS;
  }

  public static DisseminationActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Actionable.Action<IndexedDIP> action, IndexedDIP dip) {
    return POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION.contains(action);
  }

  @Override
  public boolean canAct(Actionable.Action<IndexedDIP> action, SelectedItems<IndexedDIP> selectedItems) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATIONS.contains(action);
  }

  @Override
  public void act(Actionable.Action<IndexedDIP> action, IndexedDIP dissemination,
    AsyncCallback<ActionImpact> callback) {
    if (DisseminationAction.DOWNLOAD.equals(action)) {
      download(dissemination, callback);
    } else if (DisseminationAction.REMOVE.equals(action)) {
      remove(dissemination, callback);
    } else if (DisseminationAction.NEW_PROCESS.equals(action)) {
      newProcess(dissemination, callback);
    } else if (DisseminationAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(dissemination, callback);
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Actionable.Action<IndexedDIP> action, SelectedItems<IndexedDIP> selectedItems,
    AsyncCallback<ActionImpact> callback) {
    if (DisseminationAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (DisseminationAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (DisseminationAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(selectedItems, callback);
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
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

  private void remove(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    remove(objectToSelectedItems(dissemination), callback);
  }

  private void remove(final SelectedItems<IndexedDIP> selectedItems, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.browseFileDipRepresentationConfirmTitle(),
      messages.browseFileDipRepresentationConfirmMessage(), messages.dialogCancel(), messages.dialogYes(),
      new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            BrowserService.Util.getInstance().deleteDIPs(selectedItems, new LoadingAsyncCallback<Void>() {

              @Override
              public void onFailure(Throwable caught) {
                callback.onFailure(caught);
              }

              @Override
              public void onSuccessImpl(Void result) {
                History.fireCurrentHistoryState();
                callback.onSuccess(ActionImpact.DESTROYED);
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

  private void newProcess(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    newProcess(objectToSelectedItems(dissemination), callback);
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
    HistoryUtils.newHistory(BrowseAIP.RESOLVER, EditPermissions.DIP_RESOLVER.getHistoryToken(), dip.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void updatePermissions(SelectedItems<IndexedDIP> dips, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    LastSelectedItemsSingleton.getInstance().setSelectedItems(dips);
    HistoryUtils.newHistory(BrowseAIP.RESOLVER, EditPermissions.DIP_RESOLVER.getHistoryToken());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  @Override
  public Widget createActionsLayout(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.viewRepresentationFileDisseminationTitle(), dissemination, DisseminationAction.DOWNLOAD,
      DisseminationAction.REMOVE);

    // DOWNLOAD,REMOVE
    addButton(layout, messages.downloadButton(), DisseminationAction.DOWNLOAD, dissemination, ActionImpact.NONE,
      callback, "btn-download");

    addButton(layout, messages.removeButton(), DisseminationAction.REMOVE, dissemination, ActionImpact.DESTROYED,
      callback, "btn-ban");

    addButton(layout, messages.disseminationPermissions(), DisseminationAction.UPDATE_PERMISSIONS, dissemination,
      ActionImpact.UPDATED, callback, "btn-edit");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle(), dissemination, DisseminationAction.NEW_PROCESS);

    // NEW_PROCESS
    addButton(layout, messages.newProcessPreservation(), DisseminationAction.NEW_PROCESS, dissemination,
      ActionImpact.UPDATED, callback, "btn-play");

    return layout;
  }

  @Override
  public Widget createActionsLayout(SelectedItems<IndexedDIP> disseminations, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.viewRepresentationFileDisseminationTitle(), disseminations, DisseminationAction.DOWNLOAD,
      DisseminationAction.REMOVE);

    // DOWNLOAD,REMOVE
    addButton(layout, messages.downloadButton(), DisseminationAction.DOWNLOAD, disseminations, ActionImpact.NONE,
      callback, "btn-download");

    addButton(layout, messages.removeButton(), DisseminationAction.REMOVE, disseminations, ActionImpact.DESTROYED,
      callback, "btn-ban");

    addButton(layout, messages.disseminationPermissions(), DisseminationAction.UPDATE_PERMISSIONS, disseminations,
      ActionImpact.UPDATED, callback, "btn-edit");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle(), disseminations, DisseminationAction.NEW_PROCESS);

    // NEW_PROCESS
    addButton(layout, messages.newProcessPreservation(), DisseminationAction.NEW_PROCESS, disseminations,
      ActionImpact.UPDATED, callback, "btn-play");

    return layout;
  }

}
