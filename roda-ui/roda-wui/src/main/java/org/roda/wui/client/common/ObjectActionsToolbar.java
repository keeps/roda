package org.roda.wui.client.common;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.actions.AIPToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.client.common.utils.DisposalPolicyUtils;
import org.roda.wui.client.disposal.association.DisposalPolicyAssociationPanel;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class ObjectActionsToolbar<T extends IsIndexed> extends ActionsToolbar {
  // Data
  private T object;

  public void setObjectAndBuild(T object) {
    this.object = object;
    buildIcon();
    buildTags();
    buildActions();
  }

  public void buildIcon() {
    if (object instanceof IndexedAIP) {
      IndexedAIP aip = (IndexedAIP) object;
      setIcon(DescriptionLevelUtils.getElementLevelIconCssClass(aip.getLevel()));
    }
  }

  public void buildTags() {
    this.tags.clear();
    // AIP tags
    if (object instanceof IndexedAIP) {
      // Use state
      IndexedAIP aip = (IndexedAIP) object;
      AIPState state = aip.getState();
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
      // Disposal
      if (DisposalPolicyUtils.showDisposalPolicySummary(aip)) {
        Tag disposalTag = DisposalPolicyUtils.getDisposalPolicySummaryTag(aip);
        if (disposalTag != null) {
          disposalTag.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              HistoryUtils.newHistory(DisposalPolicyAssociationPanel.RESOLVER, aip.getId());
            }
          });
          tags.add(disposalTag);
        }
      }
    }
  }

  public void buildActions() {
    this.actions.clear();
    // AIP actions
    if (object instanceof IndexedAIP) {
      IndexedAIP aip = (IndexedAIP) object;
      AIPToolbarActions aipActions;
      aipActions = AIPToolbarActions.get(aip.getId(), aip.getState(), aip.getPermissions());
      this.actions.add(
        new ActionableWidgetBuilder<IndexedAIP>(aipActions).buildGroupedListWithObjects(new ActionableObject<>(aip),
          List.of(AIPToolbarActions.AIPAction.SEARCH_DESCENDANTS, AIPToolbarActions.AIPAction.SEARCH_PACKAGE,
            AIPToolbarActions.AIPAction.DOWNLOAD, AIPToolbarActions.AIPAction.DOWNLOAD_EVENTS,
            AIPToolbarActions.AIPAction.DOWNLOAD_DOCUMENTATION, AIPToolbarActions.AIPAction.DOWNLOAD_SUBMISSIONS,
            AIPToolbarActions.AIPAction.CHANGE_TYPE, AIPToolbarActions.AIPAction.MOVE_IN_HIERARCHY,
            AIPToolbarActions.AIPAction.REMOVE, AIPToolbarActions.AIPAction.NEW_PROCESS,
            AIPToolbarActions.AIPAction.NEW_CHILD_AIP_BELOW),
          List.of(AIPToolbarActions.AIPAction.NEW_PROCESS)));
    }
  }
}
