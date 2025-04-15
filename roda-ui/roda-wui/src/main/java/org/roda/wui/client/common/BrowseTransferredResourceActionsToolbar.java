package org.roda.wui.client.common;

import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.actions.TransferredResourceToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseTransferredResourceActionsToolbar extends BrowseObjectActionsToolbar<TransferredResource> {
  public void buildIcon() {
    setIcon(null);
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    // AIP actions
    TransferredResourceToolbarActions transferredResourceToolbarActions;
    transferredResourceToolbarActions = TransferredResourceToolbarActions.get(null);
    this.actions.add(new ActionableWidgetBuilder<TransferredResource>(transferredResourceToolbarActions)
      .buildGroupedListWithObjects(new ActionableObject<>(object)));

  }
}
