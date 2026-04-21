package org.roda.wui.client.disposal.hold.tabs;

import java.util.List;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisposalHoldAction;
import org.roda.wui.client.common.actions.DisposalHoldToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.tools.Humanize;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalHoldDetailsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  @UiField
  FlowPanel detailsPanel;

  private DisposalHold hold;
  private AsyncCallback<Actionable.ActionImpact> localCallback;

  public DisposalHoldDetailsPanel(DisposalHold disposalHold, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    initWidget(uiBinder.createAndBindUi(this));

    // Promote localCallback to an instance variable so refresh() can use it
    this.localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        actionCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          actionCallback.onSuccess(result);
        }
      }
    };

    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);

    // Initial load
    refresh(disposalHold);
  }

  // Update the method signature to accept the new hold
  public void refresh(DisposalHold newHold) {
    this.hold = newHold;

    // 1. Clear out the old details
    clear();

    // 2. Re-populate text fields with the new data
    init(this.hold);

    // 3. Re-bind the actions toolbar with the new hold object
    actionsToolbar.setActionableMenu(new ActionableWidgetBuilder<DisposalHold>(DisposalHoldToolbarActions.get())
      .withActionCallback(localCallback).buildGroupedListWithObjects(new ActionableObject<>(hold),
        List.of(DisposalHoldAction.EDIT), List.of(DisposalHoldAction.EDIT)),
      true);
  }

  private void init(DisposalHold hold) {
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalHoldTitle(), hold.getTitle());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalHoldDescription(), hold.getDescription());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalHoldMandate(), hold.getMandate());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalHoldNotes(), hold.getScopeNotes());

    FlowPanel topPanel = new FlowPanel();
    FlowPanel status = new FlowPanel();
    Label statusLabel = new Label();
    statusLabel.addStyleName("label");
    statusLabel.setText(messages.showUserStatusLabel());
    status.add(statusLabel);
    HTML statusValue = new HTML();
    statusValue.addStyleName("value");
    statusValue.setHTML(HtmlSnippetUtils.getDisposalHoldStateHtml(hold));
    status.add(statusValue);
    status.addStyleName("field");
    topPanel.addStyleName("descriptiveMetadata");
    topPanel.add(status);
    detailsPanel.add(topPanel);
  }

  public void clear() {
    detailsPanel.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalHoldDetailsPanel> {
    Widget createAndBindUi(DisposalHoldDetailsPanel detailsPanel);
  }

}
