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

import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisseminationFileActions extends AbstractActionable<DIPFile> {
  private static final DisseminationFileActions INSTANCE = new DisseminationFileActions(null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisseminationFileAction> POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION_FILE = new HashSet<>(
    Arrays.asList(DisseminationFileAction.values()));

  private Permissions permissions;

  private DisseminationFileActions(Permissions permissions) {
    this.permissions = permissions;
  }

  public enum DisseminationFileAction implements Action<DIPFile> {
    DOWNLOAD();

    private List<String> methods;

    DisseminationFileAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public DisseminationFileAction[] getActions() {
    return DisseminationFileAction.values();
  }

  @Override
  public DisseminationFileAction actionForName(String name) {
    return DisseminationFileAction.valueOf(name);
  }

  public static DisseminationFileActions get() {
    return INSTANCE;
  }

  public static DisseminationFileActions get(Permissions permissions) {
    return new DisseminationFileActions(permissions);
  }

  @Override
  public CanActResult userCanAct(Action<DIPFile> action, DIPFile dipFile) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<DIPFile> action, DIPFile dipFile) {
    return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION_FILE.contains(action), CanActResult.Reason.CONTEXT,
      messages.reasonInvalidContext());
  }

  @Override
  public void act(Action<DIPFile> action, DIPFile disseminationFile, AsyncCallback<ActionImpact> callback) {
    if (DisseminationFileAction.DOWNLOAD.equals(action)) {
      download(disseminationFile, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(DIPFile disseminationFile, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createDipFileDownloadUri(disseminationFile.getUUID());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  @Override
  public ActionableBundle<DIPFile> createActionsBundle() {
    ActionableBundle<DIPFile> dipFileActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<DIPFile> managementGroup = new ActionableGroup<>(messages.disseminationFile());
    managementGroup.addButton(messages.downloadButton(), DisseminationFileAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");

    dipFileActionableBundle.addGroup(managementGroup);
    return dipFileActionableBundle;
  }
}
