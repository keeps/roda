/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
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
public class BrowseRepresentationActionsToolbar extends BrowseObjectActionsToolbar<IndexedRepresentation> {

  public void buildIcon() {
    setIcon(DescriptionLevelUtils.getRepresentationTypeIconCssClass(object.getType()));
  }

  public void buildTags() {
    this.tags.clear();

    getStateTag().ifPresent(tag -> tags.add(tag));

    for (String state : object.getRepresentationStates()) {
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
    RepresentationToolbarActions representationActions = RepresentationToolbarActions.get(object.getAipId(), state,
      actionPermissions);
    this.actions.add(new ActionableWidgetBuilder<IndexedRepresentation>(representationActions)
      .buildGroupedListWithObjects(new ActionableObject<>(object)));

  }
}
