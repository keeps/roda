package org.roda.wui.client.management.members.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.management.access.AccessKeyTablePanel;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class AccessKeysTab extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  @UiField
  FlowPanel accessKeyTablePanel;

  // Keep a reference to the panel so we can refresh it
  private AccessKeyTablePanel tablePanel;

  public AccessKeysTab(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    initWidget(uiBinder.createAndBindUi(this));

    if (member.isUser()) {
      User user = (User) member;
      tablePanel = new AccessKeyTablePanel(user.getId());
      accessKeyTablePanel.add(tablePanel);
    }

    AsyncCallback<Actionable.ActionImpact> localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        // Pass failures up to the parent handler
        actionCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          // Refresh the table locally WITHOUT reloading the whole page!
          if (tablePanel != null) {
            tablePanel.refresh();
          }
        } else {
          // If it's something else (e.g., DESTROYED), let the parent handle it
          actionCallback.onSuccess(result);
        }
      }
    };

    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);
    actionsToolbar.setActionableMenu(new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get())
      .withActionCallback(localCallback).buildGroupedListWithObjects(new ActionableObject<>(member),
        List.of(RODAMemberAction.NEW_ACCESS_KEY), List.of(RODAMemberAction.NEW_ACCESS_KEY)),
      true);

  }

  interface MyUiBinder extends UiBinder<Widget, AccessKeysTab> {
    Widget createAndBindUi(AccessKeysTab tab);
  }
}
