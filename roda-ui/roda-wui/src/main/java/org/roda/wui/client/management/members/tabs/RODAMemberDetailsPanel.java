package org.roda.wui.client.management.members.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RODAMemberDetailsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  @UiField
  FlowPanel detailsPanel;

  private RODAMember member;

  public RODAMemberDetailsPanel(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    initWidget(uiBinder.createAndBindUi(this));
    this.member = member;

    AsyncCallback<Actionable.ActionImpact> localCallback = new AsyncCallback<Actionable.ActionImpact>() {
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
    actionsToolbar.setActionableMenu(new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get())
      .withActionCallback(localCallback).buildGroupedListWithObjects(new ActionableObject<>(member),
        List.of(RODAMemberAction.EDIT, RODAMemberAction.CHANGE_PASSWORD),
        List.of(RODAMemberAction.EDIT, RODAMemberAction.CHANGE_PASSWORD)),
      true);

    init(member);
  }

  public void refresh() {
    Services services = new Services("Get updated member details", "get");

    if (member.isUser()) {
      services.membersResource(s -> s.getUser(member.getId())).whenComplete((updatedUser, err) -> {
        if (err != null) {
          Toast.showError("Unable to fetch updated user details");
        } else if (updatedUser != null) {
          this.member = updatedUser;
          clear();
          init(updatedUser);
        }
      });
    } else {
      services.membersResource(s -> s.getUser(member.getId())).whenComplete((updatedGroup, err) -> {
        if (err != null) {
          Toast.showError("Unable to fetch updated group details");
        } else if (updatedGroup != null) {
          this.member = updatedGroup;
          clear();
          init(updatedGroup);
        }
      });
    }
  }

  private void init(RODAMember member) {
    if (member.isUser()) {
      initUser(member);
    } else {
      initGroup(member);
    }
  }

  private void initUser(RODAMember member) {
    User user = (User) member;

    FormUtilities.addIfNotBlank(detailsPanel, messages.username(), user.getId());
    FormUtilities.addIfNotBlank(detailsPanel, messages.fullname(), user.getFullName());
    FormUtilities.addIfNotBlank(detailsPanel, messages.email(), user.getEmail());

    FormUtilities.addIfNotBlank(detailsPanel, messages.showUserStatusLabel(), HtmlSnippetUtils.getUserStateHtml(user));

    if (!user.getExtra().isEmpty()) {
      String pendingSeparatorLabel = null;

      for (MetadataValue extra : user.getExtra()) {
        if ("separator".equals(extra.getOptions().get("type"))) {
          // Store the separator label but do not render it yet
          pendingSeparatorLabel = HtmlSnippetUtils.getMetadataValueLabel(extra);
        } else {
          String value = extra.get("value");
          // Check if this field actually has a value
          if (StringUtils.isNotBlank(value)) {

            // If we have a pending separator, render it NOW before the field
            if (pendingSeparatorLabel != null) {
              FormUtilities.addSeparator(detailsPanel, pendingSeparatorLabel);
              pendingSeparatorLabel = null; // Clear it so it doesn't render again
            }

            FormUtilities.addIfNotBlank(detailsPanel, HtmlSnippetUtils.getMetadataValueLabel(extra), value);
          }
        }
      }
    }
  }

  private void initGroup(RODAMember member) {
    Group group = (Group) member;

    FormUtilities.addIfNotBlank(detailsPanel, messages.username(), group.getId());
    FormUtilities.addIfNotBlank(detailsPanel, messages.fullname(), group.getFullName());
  }

  public void clear() {
    detailsPanel.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, RODAMemberDetailsPanel> {
    Widget createAndBindUi(RODAMemberDetailsPanel detailsPanel);
  }
}
