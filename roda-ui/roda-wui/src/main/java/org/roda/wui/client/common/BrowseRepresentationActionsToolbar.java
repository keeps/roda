package org.roda.wui.client.common;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.actions.RepresentationToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

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
      Tag tag = Tag.fromText(state, Tag.TagStyle.SUCCESS);
      final String filter = RepresentationInformationUtils.createRepresentationInformationFilter(
        RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_STATES, state);
      tag.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          HistoryUtils.newHistory(RepresentationInformationAssociations.RESOLVER,
            RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter);
        }
      });
      tags.add(tag);
    }
  }

  public void buildActions() {
    this.actions.clear();
    RepresentationToolbarActions representationActions = RepresentationToolbarActions.get(aip.getId(),
      aip.getPermissions());
    this.actions.add(new ActionableWidgetBuilder<IndexedRepresentation>(representationActions)
      .buildGroupedListWithObjects(new ActionableObject<>(representation)));

  }
}
