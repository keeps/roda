/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common;

import java.util.List;

import com.google.gwt.core.client.GWT;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.wui.client.common.actions.DisposalConfirmationToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.labels.Tag;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationActionsToolbar extends BrowseObjectActionsToolbar<DisposalConfirmation> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public void buildIcon() {
    // do nothing
  }

  public void buildTags() {
    tags.clear();

    Tag.TagStyle style;
    switch (object.getState()) {
      case PENDING:
        style = Tag.TagStyle.WARNING_LIGHT;
        break;
      case APPROVED:
        style = Tag.TagStyle.SUCCESS;
        break;
      case RESTORED:
        style = Tag.TagStyle.SUCCESS;
        break;
      case PERMANENTLY_DELETED:
        style = Tag.TagStyle.DANGER_LIGHT;
        break;
      case EXECUTION_FAILED:
        style = Tag.TagStyle.FAILURE;
        break;
      default:
        style =  Tag.TagStyle.NEUTRAL;
    }

    GWT.log("Disposal Confirmation state: " + object.getState() + " - Tag style: " + style);
    Tag tag = Tag.fromText(messages.disposalConfirmationState(object.getState()), style);
    tags.add(tag);
  }

  public void buildActions() {
    this.actions.clear();

    this.actions.add(new ActionableWidgetBuilder<DisposalConfirmation>(DisposalConfirmationToolbarActions.get())
      .withActionCallback(actionCallback).buildGroupedListWithObjects(new ActionableObject<>(object),
        List.of(DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.DESTROY,
          DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.WITHDRAW,
          DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.REMOVE_FROM_BIN,
          DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.RESTORE_FROM_BIN),
        List.of(DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.WITHDRAW,
          DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.REMOVE_FROM_BIN,
          DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.RESTORE_FROM_BIN,
          DisposalConfirmationToolbarActions.DisposalConfirmationReportAction.DESTROY)));
  }
}
