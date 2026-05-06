package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.wui.client.common.actions.RepresentationInformationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class BrowseRepresentationInformationActionsToolbar
  extends BrowseObjectActionsToolbar<RepresentationInformation> {
  public void buildIcon() {
    setIcon(null);
  }

  public void buildTags() {
    // do nothing
  }

  public void buildActions() {
    this.actions.clear();
    RepresentationInformationActions representationInformationActions;
    representationInformationActions = RepresentationInformationActions.get();
    this.actions.add(new ActionableWidgetBuilder<RepresentationInformation>(representationInformationActions)
      .buildGroupedListWithObjects(new ActionableObject<>(object), List.of(RepresentationInformationActions.RepresentationInformationAction.START_PROCESS, RepresentationInformationActions.RepresentationInformationAction.REMOVE
              ,RepresentationInformationActions.RepresentationInformationAction.DOWNLOAD),
        List.of(RepresentationInformationActions.RepresentationInformationAction.START_PROCESS)));
  }
}
