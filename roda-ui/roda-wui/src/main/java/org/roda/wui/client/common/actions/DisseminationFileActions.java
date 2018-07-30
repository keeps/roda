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

import org.roda.core.data.v2.ip.DIPFile;
import org.roda.wui.client.common.actions.model.ActionsBundle;
import org.roda.wui.client.common.actions.model.ActionsGroup;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class DisseminationFileActions extends AbstractActionable<DIPFile> {
  private static final DisseminationFileActions INSTANCE = new DisseminationFileActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisseminationFileAction> POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION_FILE = new HashSet<>(
    Arrays.asList(DisseminationFileAction.values()));

  private DisseminationFileActions() {
    // do nothing
  }

  public enum DisseminationFileAction implements Actionable.Action<DIPFile> {
    DOWNLOAD;
  }

  public static DisseminationFileActions get() {
    return INSTANCE;
  }

  @Override
  public boolean canAct(Action<DIPFile> action, DIPFile dip) {
    return POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION_FILE.contains(action);
  }

  @Override
  public void act(Actionable.Action<DIPFile> action, DIPFile disseminationFile, AsyncCallback<ActionImpact> callback) {
    if (DisseminationFileAction.DOWNLOAD.equals(action)) {
      download(disseminationFile, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void download(DIPFile disseminationFile, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createDipFileDownloadUri(disseminationFile.getUUID());

    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }

    callback.onSuccess(ActionImpact.NONE);
  }

  @Override
  public ActionsBundle<DIPFile> createActionsBundle() {
    ActionsBundle<DIPFile> dipFileActionableBundle = new ActionsBundle<>();

    // MANAGEMENT
    ActionsGroup<DIPFile> managementGroup = new ActionsGroup<>(messages.disseminationFile());
    managementGroup.addButton(messages.downloadButton(), DisseminationFileAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");

    dipFileActionableBundle.addGroup(managementGroup);

    return dipFileActionableBundle;
  }
}
