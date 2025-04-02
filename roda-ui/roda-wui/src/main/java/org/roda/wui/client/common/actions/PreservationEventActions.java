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

import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
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
  public PreservationEventAction[] getActions() {
    return PreservationEventAction.values();
  }

  @Override
  public PreservationEventAction actionForName(String name) {
    return PreservationEventAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedPreservationEvent> action, IndexedPreservationEvent event) {
    return new CanActResult(hasPermissions(action), CanActResult.Reason.USER, messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedPreservationEvent> action, IndexedPreservationEvent event) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_EVENT.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonCantActOnMultipleObjects());
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
    // objectClass being null means that we are using a partial object and need to
    // try to get the aip, representation and file IDs from somewhere else
    SafeUri downloadUri = RestUtils.createPreservationEventDownloadUri(event.getId());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  @Override
  public ActionableBundle<IndexedPreservationEvent> createActionsBundle() {
    ActionableBundle<IndexedPreservationEvent> preservationEventActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedPreservationEvent> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.downloadButton(), PreservationEventAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");

    preservationEventActionableBundle.addGroup(managementGroup);
    return preservationEventActionableBundle;
  }

  public enum PreservationEventAction implements Action<IndexedPreservationEvent> {
    DOWNLOAD();

    private List<String> methods;

    PreservationEventAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
