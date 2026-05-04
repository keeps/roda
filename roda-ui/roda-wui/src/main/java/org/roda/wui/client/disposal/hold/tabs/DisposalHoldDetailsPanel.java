package org.roda.wui.client.disposal.hold.tabs;

import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.DisposalHoldAction;
import org.roda.wui.client.common.actions.DisposalHoldToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalHoldDetailsPanel extends GenericMetadataCardPanel<DisposalHold> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalHoldDetailsPanel(DisposalHold disposalHold) {
    super(createConfiguredToolbar(disposalHold));

    setData(disposalHold);
  }

  private static ActionsToolbar createConfiguredToolbar(DisposalHold hold) {
    if (hold == null) {
      return null;
    }

    ActionsToolbar actionsToolbar = new ActionsToolbar();
    actionsToolbar.setActionableMenu(
      new ActionableWidgetBuilder<DisposalHold>(DisposalHoldToolbarActions.get()).buildGroupedListWithObjects(
        new ActionableObject<>(hold), List.of(DisposalHoldAction.EDIT), List.of(DisposalHoldAction.EDIT)),
      true);
    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);

    return actionsToolbar;
  }

  @Override
  public void setData(DisposalHold data) {
    addFieldIfNotNull(messages.disposalHoldTitle(), DisposalHold::getTitle, data);
    addFieldIfNotNull(messages.disposalHoldDescription(), DisposalHold::getDescription, data);
    addFieldIfNotNull(messages.disposalHoldMandate(), DisposalHold::getMandate, data);
    addFieldIfNotNull(messages.disposalHoldNotes(), DisposalHold::getScopeNotes, data);
    addFieldIfNotNull(messages.showUserStatusLabel(), HtmlSnippetUtils.getDisposalHoldStateHtml(data));
  }
}
