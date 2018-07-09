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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class PreservationAgentActions extends AbstractActionable<IndexedPreservationAgent> {
  private static final PreservationAgentActions INSTANCE = new PreservationAgentActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<PreservationAgentAction> POSSIBLE_ACTIONS_ON_SINGLE_AGENT = new HashSet<>(
    Arrays.asList(PreservationAgentAction.values()));

  private PreservationAgentActions() {
    // do nothing
  }

  public enum PreservationAgentAction implements Action<IndexedPreservationAgent> {
    DOWNLOAD;
  }

  public static PreservationAgentActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Action<IndexedPreservationAgent> action, IndexedPreservationAgent agent) {
    return POSSIBLE_ACTIONS_ON_SINGLE_AGENT.contains(action);
  }

  @Override
  public void act(Action<IndexedPreservationAgent> action, IndexedPreservationAgent agent,
    AsyncCallback<ActionImpact> callback) {
    if (PreservationAgentAction.DOWNLOAD.equals(action)) {
      download(agent, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(IndexedPreservationAgent agent, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createPreservationAgentUri(agent.getId(),
      RodaConstants.API_QUERY_VALUE_ACCEPT_FORMAT_BIN);
    Window.Location.assign(downloadUri.asString());
    callback.onSuccess(ActionImpact.NONE);
  }

  @Override
  public ActionsBundle<IndexedPreservationAgent> createActionsBundle() {
    ActionsBundle<IndexedPreservationAgent> preservationAgentActionableBundle = new ActionsBundle<>();

    // MANAGEMENT
    ActionsGroup<IndexedPreservationAgent> managementGroup = new ActionsGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.downloadButton(), PreservationAgentAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");

    preservationAgentActionableBundle.addGroup(managementGroup);

    return preservationAgentActionableBundle;
  }
}
