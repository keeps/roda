package org.roda.wui.client.common;

import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.actions.FileToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseFileActionsToolbar extends BrowseObjectActionsToolbar<IndexedFile> {
  protected void buildTags() {
    tags.clear();

    getStateTag().ifPresent(tag -> tags.add(tag));
  }

  protected void buildIcon() {
    setIcon("cmi cmi-file");
  }

  protected void buildActions() {
    this.actions.clear();
    FileToolbarActions fileActions = FileToolbarActions.get(object.getAipId(), object.getRepresentationId(), state,
      object.isDirectory() ? object : null, actionPermissions);
    this.actions.add(new ActionableWidgetBuilder<IndexedFile>(fileActions)
      .buildGroupedListWithObjects(new ActionableObject<>(object)));
  }
}
