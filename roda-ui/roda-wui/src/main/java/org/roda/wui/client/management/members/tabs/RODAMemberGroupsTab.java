package org.roda.wui.client.management.members.tabs;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.RODAMemberAction;
import org.roda.wui.client.common.actions.RODAMemberToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ActionMenuCell;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RODAMemberGroupsTab extends GenericMetadataCardPanel<RODAMember> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String id;
  private final RODAMember member;
  private final AsyncCallback<Actionable.ActionImpact> actionCallback;
  private FlowPanel groupsPanel;

  public RODAMemberGroupsTab(RODAMember member, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    super();
    this.member = member;
    this.id = member.getId();
    this.actionCallback = actionCallback;

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
        if (actionCallback != null) {
          actionCallback.onFailure(caught);
        }
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          // Refresh the table locally
          refresh();
          if (actionCallback != null) {
            actionCallback.onSuccess(result);
          }
        } else {
          // Pass other events (like DESTROYED) up the chain
          if (actionCallback != null) {
            actionCallback.onSuccess(result);
          }
        }
      }
    };

    // 2. Bind the local callback to the toolbar builder
    return new ActionableWidgetBuilder<RODAMember>(RODAMemberToolbarActions.get()).withActionCallback(localCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(member),
        List.of(RODAMemberAction.ADD_NEW_GROUP, RODAMemberAction.ADD_NEW_MEMBER),
        List.of(RODAMemberAction.ADD_NEW_GROUP, RODAMemberAction.ADD_NEW_MEMBER));
  }

  @Override
  protected void buildFields(RODAMember member) {
    if (member != null) {
      // Initialize the container for the tables
      groupsPanel = new FlowPanel();
      metadataContainer.add(groupsPanel);

      // Call unified refresh to fetch and populate data
      refresh();
    }
  }

  public void refresh() {
    if (member.isUser()) {
      Services services = new Services("Get user groups", "get");
      services.membersResource(s -> s.getUserGroups(id)).whenComplete((groupsSet, error) -> {
        if (groupsSet != null) {
          groupsPanel.clear();
          groupsPanel.add(createTable(groupsSet));
        } else if (error != null) {
          Toast.showError(error.getMessage());
        }
      });
    } else {
      Services services = new Services("Get members of group", "get");
      services.membersResource(s -> s.getGroupMembers(id)).whenComplete((members, error) -> {
        if (members != null) {
          groupsPanel.clear();
          groupsPanel.add(createTableGroup(members));
        } else if (error != null) {
          Toast.showError(error.getMessage());
        }
      });
    }
  }

  public ScrollPanel createTableGroup(Set<User> members) {
    BasicTablePanel<User> table;
    ScrollPanel scrollPanel;

    if (members.isEmpty()) {
      String someOfAObject = messages.someOfAObject(User.class.getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      scrollPanel = new ScrollPanel(label);
    } else {
      FlowPanel panel = new FlowPanel();
      table = getBasicTableForGroups(members);
      table.removeSelectionModel();

      panel.add(table);
      scrollPanel = new ScrollPanel(panel);
    }

    return scrollPanel;
  }

  public ScrollPanel createTable(Set<Group> groups) {
    BasicTablePanel<Group> table;
    ScrollPanel scrollPanel;

    if (groups.isEmpty()) {
      String someOfAObject = messages.someOfAObject(Group.class.getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      scrollPanel = new ScrollPanel(label);
    } else {
      FlowPanel panel = new FlowPanel();
      table = getBasicTableForUsers(groups);
      table.removeSelectionModel();

      panel.add(table);
      scrollPanel = new ScrollPanel(panel);
    }

    return scrollPanel;
  }

  private void showActionsMenu(Group key, int left, int top) {
    // 1. Create the Popup
    PopupPanel popup = new PopupPanel(true); // true = auto-hide when clicking away

    // 2. Create your FlowPanel and add your action items
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    Button removeBtn = new Button(messages.removeButton());
    removeBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    removeBtn.addClickHandler(e -> {
      popup.hide();

      Dialogs.showConfirmDialog(messages.groups(), messages.removeGroupConfirmationMessage(key.getFullName()),
        messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              Services services = new Services("Remove group from user", "update");
              services.membersResource(s -> s.removeGroupsFromUser(id, key.getId())).whenComplete((response, error) -> {
                if (response != null) {
                  Toast.showInfo(messages.groups(), messages.groupSuccessfullyRemoved());

                  // Simple call to the unified refresh method
                  refresh();
                  if (actionCallback != null) {
                    actionCallback.onSuccess(Actionable.ActionImpact.UPDATED);
                  }

                } else {
                  Toast.showError("Failed to remove group from user");
                }
              });
            }
          }
        });
    });

    menuPanel.add(removeBtn);

    // 3. Show the popup at the calculated coordinates
    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private void showUserActionsMenu(User key, int left, int top) {
    // 1. Create the Popup
    PopupPanel popup = new PopupPanel(true); // true = auto-hide when clicking away

    // 2. Create your FlowPanel and add your action items
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    Button removeBtn = new Button(messages.removeButton());
    removeBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    removeBtn.addClickHandler(e -> {
      popup.hide();

      Dialogs.showConfirmDialog(messages.removeMemberConfirmationTitle(),
        messages.removeMemberConfirmationMessage(key.getFullName()), messages.cancelButton(), messages.confirmButton(),
        new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              Services services = new Services("Remove member from group", "update");
              services.membersResource(s -> s.removeMembersFromGroup(id, key.getId()))
                .whenComplete((response, error) -> {
                  if (response != null) {
                    Toast.showInfo(messages.groups(), messages.groupSuccessfullyRemoved());

                    // Simple call to the unified refresh method
                    refresh();
                  } else {
                    Toast.showError("Failed to remove group from user");
                  }
                });
            }
          }
        });
    });

    menuPanel.add(removeBtn);

    // 3. Show the popup at the calculated coordinates
    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private TextColumn<Group> getNameColumn() {
    return new TextColumn<Group>() {
      @Override
      public String getValue(Group group) {
        return group.getName();
      }
    };
  }

  private TextColumn<Group> getFullNameColumn() {
    return new TextColumn<Group>() {
      @Override
      public String getValue(Group group) {
        return group.getFullName();
      }
    };
  }

  private TextColumn<User> getUserNameColumn() {
    return new TextColumn<User>() {
      @Override
      public String getValue(User group) {
        return group.getName();
      }
    };
  }

  private TextColumn<User> getUserFullNameColumn() {
    return new TextColumn<User>() {
      @Override
      public String getValue(User group) {
        return group.getFullName();
      }
    };
  }

  private Column<Group, Group> getActionsColumn() {
    ActionMenuCell<Group> actionCell = new ActionMenuCell<Group>(this::showActionsMenu);

    return new Column<Group, Group>(actionCell) {
      @Override
      public Group getValue(Group object) {
        return object;
      }
    };
  }

  private Column<User, User> getUserActionsColumn() {
    ActionMenuCell<User> actionCell = new ActionMenuCell<User>(this::showUserActionsMenu);

    return new Column<User, User>(actionCell) {
      @Override
      public User getValue(User object) {
        return object;
      }
    };
  }

  private BasicTablePanel<Group> getBasicTableForUsers(Set<Group> groups) {
    if (groups.isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstanceLabel()));
    } else {
      return new BasicTablePanel<>(groups.iterator(),
        new BasicTablePanel.ColumnInfo<Group>(messages.groupFullname(), 15, getFullNameColumn()),
        new BasicTablePanel.ColumnInfo<Group>(messages.groupName(), 15, getNameColumn()),
        new BasicTablePanel.ColumnInfo<>(messages.actions(), 5, getActionsColumn()));
    }
  }

  private BasicTablePanel<User> getBasicTableForGroups(Set<User> members) {
    if (members.isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstanceLabel()));
    } else {
      return new BasicTablePanel<>(members.iterator(),
        new BasicTablePanel.ColumnInfo<User>(messages.groupFullname(), 15, getUserFullNameColumn()),
        new BasicTablePanel.ColumnInfo<User>(messages.groupName(), 15, getUserNameColumn()),
        new BasicTablePanel.ColumnInfo<>(messages.actions(), 5, getUserActionsColumn()));
    }
  }
}