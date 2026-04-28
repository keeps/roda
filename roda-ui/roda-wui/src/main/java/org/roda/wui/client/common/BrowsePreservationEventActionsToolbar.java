package org.roda.wui.client.common;

import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.wui.client.common.actions.PreservationEventActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import java.util.List;

public class BrowsePreservationEventActionsToolbar extends BrowseObjectActionsToolbar<IndexedPreservationEvent> {
  public void buildIcon() {
    setIcon(null);
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    PreservationEventActions preservationEventActions;
    preservationEventActions = PreservationEventActions.get();
    this.actions.add(new ActionableWidgetBuilder<IndexedPreservationEvent>(preservationEventActions)
      .buildGroupedListWithObjects(new ActionableObject<>(object), List.of(),
        List.of(PreservationEventActions.PreservationEventAction.DOWNLOAD)));
  }

}
