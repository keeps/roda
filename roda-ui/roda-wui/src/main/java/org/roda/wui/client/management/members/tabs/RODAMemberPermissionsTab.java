package org.roda.wui.client.management.members.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RODAMemberPermissionsTab extends Composite {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  // Changed from FlowPanel to PermissionsPanel to expose the refresh() method
  @UiField(provided = true)
  PermissionsPanel permissionsTablePanel;

  public RODAMemberPermissionsTab(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    permissionsTablePanel = new PermissionsPanel(member, true, false);
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create a local callback to intercept the UPDATED event
    AsyncCallback<Actionable.ActionImpact> localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        actionCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          // Refresh the table locally WITHOUT reloading the whole page!
          permissionsTablePanel.refresh();
        } else {
          actionCallback.onSuccess(result);
        }
      }
    };

    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);
    actionsToolbar.setActionableMenu(new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get())
      .withActionCallback(localCallback).buildGroupedListWithObjects(new ActionableObject<>(member),
        List.of(RODAMemberAction.EDIT_PERMISSIONS), List.of(RODAMemberAction.EDIT_PERMISSIONS)),
      true);
  }

  interface MyUiBinder extends UiBinder<Widget, RODAMemberPermissionsTab> {
    Widget createAndBindUi(RODAMemberPermissionsTab tab);
  }
}