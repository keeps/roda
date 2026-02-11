package org.roda.wui.client.management.members.tabs;

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
        List.of(RODAMemberAction.EDIT), List.of(RODAMemberAction.EDIT)),
      true);

    init(member);
  }

  public void refresh() {
    Services services = new Services("Get updated member details", "get");

    if (member.isUser()) {
      services.membersResource(s -> s.getUser(member.getUUID())).whenComplete((updatedUser, err) -> {
        if (err != null) {
          Toast.showError("Unable to fetch updated user details");
        } else if (updatedUser != null) {
          this.member = updatedUser;
          clear();
          init(updatedUser);
        }
      });
    } else {
      services.membersResource(s -> s.getUser(member.getUUID())).whenComplete((updatedGroup, err) -> {
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

    FlowPanel topPanel = new FlowPanel();
    FlowPanel status = new FlowPanel();
    Label statusLabel = new Label();
    statusLabel.addStyleName("label");
    statusLabel.setText(messages.showUserStatusLabel());
    status.add(statusLabel);
    HTML statusValue = new HTML();
    statusValue.addStyleName("value");
    statusValue.setHTML(HtmlSnippetUtils.getUserStateHtml(user));
    status.add(statusValue);
    status.addStyleName("field");
    topPanel.addStyleName("descriptiveMetadata");
    topPanel.add(status);
    detailsPanel.add(topPanel);

    if (!user.getExtra().isEmpty()) {
      HtmlSnippetUtils.createExtraShow(detailsPanel, user.getExtra(), false);
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
