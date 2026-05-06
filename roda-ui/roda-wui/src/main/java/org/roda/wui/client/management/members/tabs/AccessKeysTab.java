package org.roda.wui.client.management.members.tabs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.management.access.AccessKeyTablePanel;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class AccessKeysTab extends GenericMetadataCardPanel<RODAMember> {
  private final AsyncCallback<Actionable.ActionImpact> parentActionCallback;
  private AccessKeyTablePanel tablePanel;

  public AccessKeysTab(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    super();
    this.parentActionCallback = actionCallback;

    // This template method automatically calls createHeaderWidget() and
    // buildFields()
    setData(member);
  }

  @Override
  protected FlowPanel createHeaderWidget(RODAMember member) {
    if (member == null) {
      return null;
    }

    AsyncCallback<Actionable.ActionImpact> localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        if (parentActionCallback != null) {
          parentActionCallback.onFailure(caught);
        }
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          // Refresh the table locally WITHOUT reloading the whole page!
          if (tablePanel != null) {
            tablePanel.refresh();
          }
        } else {
          if (parentActionCallback != null) {
            parentActionCallback.onSuccess(result);
          }
        }
      }
    };

    return new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get()).withActionCallback(localCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(member), List.of(RODAMemberAction.NEW_ACCESS_KEY),
        List.of(RODAMemberAction.NEW_ACCESS_KEY));
  }

  @Override
  protected void buildFields(RODAMember member) {
    if (member != null && member.isUser()) {
      User user = (User) member;

      // Initialize the table panel with the user's ID
      tablePanel = new AccessKeyTablePanel(user.getId());

      // Since a table is usually full-width and doesn't need a "Label: Value" flexbox
      // layout,
      // we append it directly to the protected metadataContainer rather than using
      // the FieldBuilder.
      metadataContainer.add(tablePanel);
    }
  }
}