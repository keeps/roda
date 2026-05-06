package org.roda.wui.client.management.members.tabs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;

import java.util.List;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RODAMemberPermissionsTab extends GenericMetadataCardPanel<RODAMember> {

  private final AsyncCallback<Actionable.ActionImpact> parentActionCallback;
  private PermissionsPanel permissionsTablePanel;

  public RODAMemberPermissionsTab(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {
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

    // 1. Create a local callback to intercept the UPDATED event
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
          if (permissionsTablePanel != null) {
            permissionsTablePanel.refresh();
          }
        } else {
          if (parentActionCallback != null) {
            parentActionCallback.onSuccess(result);
          }
        }
      }
    };

    return new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get()).withActionCallback(localCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(member), List.of(RODAMemberAction.EDIT_PERMISSIONS),
        List.of(RODAMemberAction.EDIT_PERMISSIONS));
  }

  @Override
  protected void buildFields(RODAMember member) {
    if (member != null) {
      // Initialize the permissions panel
      permissionsTablePanel = new PermissionsPanel(member, true, true);

      // Append it directly to the protected metadataContainer
      metadataContainer.add(permissionsTablePanel);
    }
  }
}