/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.actions.TransferredResourceToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import java.util.List;

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
      .buildGroupedListWithObjects(new ActionableObject<>(object), List.of(),
        List.of(TransferredResourceToolbarActions.TransferredResourceAction.DOWNLOAD,
          TransferredResourceToolbarActions.TransferredResourceAction.NEW_PROCESS)));
  }
}
