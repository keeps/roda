package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.client.common.actions.Actionable.ActionImpact;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
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
    Arrays.asList(DisseminationAction.REMOVE, DisseminationAction.NEW_PROCESS));

  private DisseminationActions() {
  }

  public enum DisseminationAction implements Actionable.Action<IndexedDIP> {
    DOWNLOAD, REMOVE, NEW_PROCESS; // UPLOAD_FILES, CREATE_FOLDER;
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
    // TODO Auto-generated method stub
    Toast.showInfo("Not yet implemented", "The action you have requested has not yet been implemented");
  }

  private void remove(SelectedItems<IndexedDIP> selectedItems, AsyncCallback<ActionImpact> callback) {
    // TODO Auto-generated method stub
    Toast.showInfo("Not yet implemented", "The action you have requested has not yet been implemented");
  }

  private void newProcess(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    // TODO Auto-generated method stub
    Toast.showInfo("Not yet implemented", "The action you have requested has not yet been implemented");
  }

  private void newProcess(SelectedItems<IndexedDIP> selectedItems, AsyncCallback<ActionImpact> callback) {
    // TODO Auto-generated method stub
    Toast.showInfo("Not yet implemented", "The action you have requested has not yet been implemented");
  }

  @Override
  public Widget createActionsLayout(IndexedDIP dissemination, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.viewRepresentationFileDisseminationTitle());

    // DOWNLOAD,REMOVE
    addButton(layout, messages.downloadButton(), DisseminationAction.DOWNLOAD, dissemination, ActionImpact.NONE,
      callback, "btn-download");

    addButton(layout, messages.removeButton(), DisseminationAction.REMOVE, dissemination, ActionImpact.DESTROYED,
      callback, "btn-ban");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle());

    // NEW_PROCESS

    addButton(layout, messages.newProcessPreservation(), DisseminationAction.NEW_PROCESS, dissemination,
      ActionImpact.UPDATED, callback, "btn-play");

    return layout;
  }

  @Override
  public Widget createActionsLayout(SelectedItems<IndexedDIP> disseminations, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.viewRepresentationFileDisseminationTitle());

    // DOWNLOAD,REMOVE
    addButton(layout, messages.downloadButton(), DisseminationAction.DOWNLOAD, disseminations, ActionImpact.NONE,
      callback, "btn-download");

    addButton(layout, messages.removeButton(), DisseminationAction.REMOVE, disseminations, ActionImpact.DESTROYED,
      callback, "btn-ban");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle());

    // NEW_PROCESS

    addButton(layout, messages.newProcessPreservation(), DisseminationAction.NEW_PROCESS, disseminations,
      ActionImpact.UPDATED, callback, "btn-play");

    return layout;
  }

}
