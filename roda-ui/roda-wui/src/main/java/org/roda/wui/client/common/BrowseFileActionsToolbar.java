package org.roda.wui.client.common;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.actions.FileToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseFileActionsToolbar extends ActionsToolbar {
  // Data
  private IndexedAIP aip;
  private IndexedFile file;

  public void setObjectAndBuild(IndexedAIP aip, IndexedFile file) {
    this.aip = aip;
    this.file = file;
    buildIcon();
    buildActions();
  }

  public void buildIcon() {
    setIcon("cmi cmi-file");
  }

  public void buildActions() {
    this.actions.clear();
    FileToolbarActions fileActions = FileToolbarActions.get(file.getAipId(), file.getRepresentationId(),
      file.isDirectory() ? file : null, aip.getPermissions());
    this.actions.add(
      new ActionableWidgetBuilder<IndexedFile>(fileActions).buildGroupedListWithObjects(new ActionableObject<>(file)));

  }
}
