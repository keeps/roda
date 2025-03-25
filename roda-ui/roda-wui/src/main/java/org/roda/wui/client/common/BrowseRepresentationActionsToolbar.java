package org.roda.wui.client.common;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.actions.RepresentationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseRepresentationActionsToolbar extends ActionsToolbar {
  // Data
  private IndexedAIP aip;
  private IndexedRepresentation representation;

  public void setObjectAndBuild(IndexedAIP aip, IndexedRepresentation representation) {
    this.aip = aip;
    this.representation = representation;
    buildIcon();
    buildTags();
    buildActions();
  }

  public void buildIcon() {
    setIcon(DescriptionLevelUtils.getRepresentationTypeIconCssClass(representation.getType()));
  }

  public void buildTags() {
    this.tags.clear();
    for (String state : representation.getRepresentationStates()) {
      tags.add(Tag.fromText(state, Tag.TagStyle.SUCCESS));
    }
  }

  public void buildActions() {
    this.actions.clear();
    RepresentationActions representationActions = RepresentationActions.get(aip.getId(), aip.getPermissions());
    this.actions.add(new ActionableWidgetBuilder<IndexedRepresentation>(representationActions)
      .buildGroupedListWithObjects(new ActionableObject<>(representation)));

  }
}
