package org.roda.wui.client.disposal.hold.tabs;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
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
    setData(disposalHold);
  }

  @Override
  protected FlowPanel createHeaderWidget(DisposalHold data) {
    if (data == null) {
      return null;
    }

    return new ActionableWidgetBuilder<DisposalHold>(DisposalHoldToolbarActions.get()).buildGroupedListWithObjects(
      new ActionableObject<>(data), List.of(DisposalHoldAction.EDIT), List.of(DisposalHoldAction.EDIT));
  }

  @Override
  protected void buildFields(DisposalHold data) {
    buildField(messages.disposalHoldTitle()).withValue(data.getTitle()).build();
    buildField(messages.disposalHoldDescription()).withValue(data.getDescription()).build();
    buildField(messages.disposalScheduleMandate()).withValue(data.getMandate()).build();
    buildField(messages.disposalHoldNotes()).withValue(data.getScopeNotes()).build();
    buildField(messages.showUserStatusLabel()).withHtml(HtmlSnippetUtils.getDisposalHoldStateHtml(data)).build();
  }
}
