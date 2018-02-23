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

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DisseminationFileActions extends AbstractActionable<DIPFile> {
  private static final DisseminationFileActions INSTANCE = new DisseminationFileActions();
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<DisseminationFileAction> POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION_FILE = new HashSet<>(
    Arrays.asList(DisseminationFileAction.values()));

  private static final Set<DisseminationFileAction> POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATION_FILES = new HashSet<>();

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
  public boolean canAct(Actionable.Action<DIPFile> action, DIPFile dip) {
    return POSSIBLE_ACTIONS_ON_SINGLE_DISSEMINATION_FILE.contains(action);
  }

  @Override
  public boolean canAct(Actionable.Action<DIPFile> action, SelectedItems<DIPFile> selectedItems) {
    return POSSIBLE_ACTIONS_ON_MULTIPLE_DISSEMINATION_FILES.contains(action);
  }

  @Override
  public void act(Actionable.Action<DIPFile> action, DIPFile disseminationFile, AsyncCallback<ActionImpact> callback) {
    if (DisseminationFileAction.DOWNLOAD.equals(action)) {
      download(disseminationFile, callback);
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Actionable.Action<DIPFile> action, SelectedItems<DIPFile> selectedItems,
    AsyncCallback<ActionImpact> callback) {
    callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
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
  public Widget createActionsLayout(DIPFile disseminationFile, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, "disseminationFileTitle", messages.disseminationFile(), disseminationFile,
      DisseminationFileAction.DOWNLOAD);

    // DOWNLOAD,REMOVE
    addButton(layout, "dipDownloadButton", messages.downloadButton(), DisseminationFileAction.DOWNLOAD,
      disseminationFile, ActionImpact.NONE, callback, "btn-download");

    return layout;
  }

  @Override
  public Widget createActionsLayout(SelectedItems<DIPFile> disseminationFiles, AsyncCallback<ActionImpact> callback) {
    // MANAGEMENT
    // addTitle(layout, messages.disseminationFile());

    return createLayout();
  }

}
