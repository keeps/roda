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
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
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
    DOWNLOAD();

    private List<String> methods;

    PreservationAgentAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public PreservationAgentAction[] getActions() {
    return PreservationAgentAction.values();
  }

  @Override
  public PreservationAgentAction actionForName(String name) {
    return PreservationAgentAction.valueOf(name);
  }

  public static PreservationAgentActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Action<IndexedPreservationAgent> action, IndexedPreservationAgent agent) {
    return hasPermissions(action) && POSSIBLE_ACTIONS_ON_SINGLE_AGENT.contains(action);
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
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  @Override
  public ActionableBundle<IndexedPreservationAgent> createActionsBundle() {
    ActionableBundle<IndexedPreservationAgent> preservationAgentActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedPreservationAgent> managementGroup = new ActionableGroup<>(messages.sidebarActionsTitle());
    managementGroup.addButton(messages.downloadButton(), PreservationAgentAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");

    preservationAgentActionableBundle.addGroup(managementGroup);
    return preservationAgentActionableBundle;
  }
}
