package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class BrowseAIPActionsToolbar extends BrowseObjectActionsToolbar<IndexedAIP> {
  public void buildIcon() {
    setIcon(DescriptionLevelUtils.getElementLevelIconCssClass(object.getLevel()));
  }

  public void buildTags() {
    this.tags.clear();
    // AIP tags
    // Use state
    AIPState state = object.getState();
    switch (state) {
      case ACTIVE:
        break;
      case UNDER_APPRAISAL:
        tags.add(Tag.fromText(messages.aipState(state), List.of(Tag.TagStyle.WARNING_LIGHT, Tag.TagStyle.MONO)));
        break;
      default:
        tags.add(Tag.fromText(messages.aipState(state), List.of(Tag.TagStyle.DANGER_LIGHT, Tag.TagStyle.MONO)));
        break;
    }
  }

  public void buildActions() {
    this.actions.clear();
    // AIP actions
    AipToolbarActions aipActions;
    aipActions = AipToolbarActions.get(object.getId(), object.getState(), actionPermissions);
    this.actions.add(new ActionableWidgetBuilder<IndexedAIP>(aipActions).withActionCallback(actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(object),
        List.of(AipToolbarActions.AIPAction.SEARCH_DESCENDANTS, AipToolbarActions.AIPAction.SEARCH_PACKAGE,
          AipToolbarActions.AIPAction.DOWNLOAD, AipToolbarActions.AIPAction.DOWNLOAD_EVENTS,
          AipToolbarActions.AIPAction.DOWNLOAD_DOCUMENTATION, AipToolbarActions.AIPAction.DOWNLOAD_SUBMISSIONS,
          AipToolbarActions.AIPAction.CHANGE_TYPE, AipToolbarActions.AIPAction.MOVE_IN_HIERARCHY,
          AipToolbarActions.AIPAction.REMOVE, AipToolbarActions.AIPAction.NEW_PROCESS,
          AipToolbarActions.AIPAction.NEW_CHILD_AIP_BELOW, AipToolbarActions.AIPAction.NEW_REPRESENTATION,
          AipToolbarActions.AIPAction.CREATE_DESCRIPTIVE_METADATA),
        List.of(AipToolbarActions.AIPAction.NEW_PROCESS)));

  }
}
