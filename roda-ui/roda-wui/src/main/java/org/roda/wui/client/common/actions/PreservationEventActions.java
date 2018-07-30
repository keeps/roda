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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.common.actions.model.ActionsBundle;
import org.roda.wui.client.common.actions.model.ActionsGroup;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class PreservationEventActions extends AbstractActionable<IndexedPreservationEvent> {
  private static final PreservationEventActions INSTANCE = new PreservationEventActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<PreservationEventAction> POSSIBLE_ACTIONS_ON_SINGLE_EVENT = new HashSet<>(
    Arrays.asList(PreservationEventAction.values()));
  private final String aipId;
  private final String representationUUID;
  private final String fileUUID;

  private PreservationEventActions() {
    this.aipId = null;
    this.representationUUID = null;
    this.fileUUID = null;
  }

  private PreservationEventActions(String aipId, String representationUUID, String fileUUID) {
    this.aipId = aipId;
    this.representationUUID = representationUUID;
    this.fileUUID = fileUUID;
  }

  public enum PreservationEventAction implements Action<IndexedPreservationEvent> {
    DOWNLOAD;
  }

  /**
   * Use this when the IndexedPreservationEvent is not a partial object
   */
  public static PreservationEventActions get() {
    return INSTANCE;
  }

  /**
   * Use this when the IndexedPreservationEvent is a partial object (in order to
   * have all the info needed to generate the download link)
   */
  public static PreservationEventActions get(String aipId, String representationUUID, String fileUUID) {
    return new PreservationEventActions(aipId, representationUUID, fileUUID);
  }

  @Override
  public boolean canAct(Action<IndexedPreservationEvent> action, IndexedPreservationEvent event) {
    return POSSIBLE_ACTIONS_ON_SINGLE_EVENT.contains(action);
  }

  @Override
  public void act(Action<IndexedPreservationEvent> action, IndexedPreservationEvent event,
    AsyncCallback<ActionImpact> callback) {
    if (PreservationEventAction.DOWNLOAD.equals(action)) {
      download(event, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(IndexedPreservationEvent event, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri;
    // objectClass being null means that we are using a partial object and need to
    // try to get the aip, representation and file IDs from somewhere else
    if (event.getObjectClass() == null) {
      downloadUri = RestUtils.createPreservationEventDetailsUri(event.getId(), aipId, representationUUID, fileUUID,
        false, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);
    } else {
      downloadUri = RestUtils.createPreservationEventDetailsUri(event.getId(), event.getAipID(),
        event.getRepresentationUUID(), event.getFileUUID(), false, RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);
    }
    Window.Location.assign(downloadUri.asString());
    callback.onSuccess(ActionImpact.NONE);
  }

  @Override
  public ActionsBundle<IndexedPreservationEvent> createActionsBundle() {
    ActionsBundle<IndexedPreservationEvent> preservationEventActionableBundle = new ActionsBundle<>();

    // MANAGEMENT
    ActionsGroup<IndexedPreservationEvent> managementGroup = new ActionsGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.downloadButton(), PreservationEventAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");

    preservationEventActionableBundle.addGroup(managementGroup);

    return preservationEventActionableBundle;
  }
}
