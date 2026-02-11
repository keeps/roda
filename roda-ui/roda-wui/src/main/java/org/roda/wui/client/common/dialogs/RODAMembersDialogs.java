package org.roda.wui.client.common.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.CreateGroupRequest;
import org.roda.core.data.v2.user.requests.CreateUserRequest;
import org.roda.core.data.v2.user.requests.UpdateUserRequest;
import org.roda.wui.client.common.lists.RodaMemberList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.management.members.CreateUserPanel;
import org.roda.wui.client.management.members.GroupDataPanel;
import org.roda.wui.client.management.members.UserDataPanel;
import org.roda.wui.client.management.members.tabs.PermissionsPanel;

import java.util.HashSet;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RODAMembersDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RODAMembersDialogs() {
    // private method
  }

  public static void createGroup(final String title, final String cancelButtonText, final String saveButtonText,
    final AsyncCallback<CreateGroupRequest> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setHTML(title);
    dialogBox.addStyleName("create-group-dialog");

    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button saveButton = new Button(saveButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    final FlowPanel content = new FlowPanel();
    GroupDataPanel groupDataPanel = new GroupDataPanel(false);

    content.add(groupDataPanel);
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!groupDataPanel.isChanged()) {
          dialogBox.hide();
          callback.onFailure(null);
        } else {
          if (groupDataPanel.isValid()) {
            dialogBox.hide();
            Group group = groupDataPanel.getGroup();
            CreateGroupRequest createGroupRequest = new CreateGroupRequest(group.getName(), group.getFullName(),
              new HashSet<>());
            callback.onSuccess(createGroupRequest);
          }
        }
      }
    });

    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void createUser(final String title, final String cancelButtonText, final String saveButtonText,
    final AsyncCallback<CreateUserRequest> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setHTML(title);
    dialogBox.addStyleName("create-user-dialog");

    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button saveButton = new Button(saveButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    final FlowPanel content = new FlowPanel();
    CreateUserPanel userPanel = new CreateUserPanel();

    userPanel.setOnFormReadyCallback(new Runnable() {
      @Override
      public void run() {
        dialogBox.center();
      }
    });

    content.add(userPanel);
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!userPanel.isChanged()) {
          dialogBox.hide();
          callback.onFailure(null);
        } else {
          if (userPanel.isValid()) {
            dialogBox.hide();
            User user = userPanel.getUser();
            CreateUserRequest request = new CreateUserRequest(user.getEmail(), user.getName(), user.getFullName(),
              new HashSet<>(), new HashSet<>(), user.isGuest(), null, userPanel.getUserExtra());
            callback.onSuccess(request);
          }
        }
      }
    });

    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void editGroupInformation(final String title, final String cancelButtonText, final String saveButtonText,
                                         Group group, final AsyncCallback<Group> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setHTML(title);
    dialogBox.addStyleName("edit-group-information-dialog");

    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button saveButton = new Button(saveButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    final FlowPanel content = new FlowPanel();
    GroupDataPanel groupPanel = new GroupDataPanel(true);
    groupPanel.setGroupNameReadOnly(true);
    groupPanel.setGroup(group);

    content.add(groupPanel);
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!groupPanel.isChanged()) {
          dialogBox.hide();
          callback.onFailure(null);
        } else {
          if (groupPanel.isValid()) {
            dialogBox.hide();
            callback.onSuccess(groupPanel.getGroup());
          }
        }
      }
    });

    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void editUserInformation(final String title, final String cancelButtonText, final String saveButtonText,
    User user, final AsyncCallback<UpdateUserRequest> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setHTML(title);
    dialogBox.addStyleName("edit-user-information-dialog");

    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button saveButton = new Button(saveButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    final FlowPanel content = new FlowPanel();
    UserDataPanel userPanel = new UserDataPanel(true, true);
    userPanel.setUsernameReadOnly(true);
    userPanel.setUser(user);
    userPanel.setUserExtra(user.getExtra());

    content.add(userPanel);
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!userPanel.isChanged()) {
          dialogBox.hide();
          callback.onFailure(null);
        } else {
          if (userPanel.isValid()) {
            dialogBox.hide();
            UpdateUserRequest request = new UpdateUserRequest();
            request.setUser(userPanel.getUser());
            request.getUser().setDirectRoles(user.getDirectRoles());
            request.getUser().setGroups(user.getGroups());
            if (userPanel.isPasswordChanged()) {
              SecureString securePassword = new SecureString(userPanel.getPassword().toCharArray());
              request.setPassword(securePassword);
            } else {
              request.setPassword(null);
            }
            request.setValues(userPanel.getUserExtra());
            callback.onSuccess(request);
          }
        }
      }
    });

    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showEditRODAMemberPermissionsPanel(final String title, final String cancelButtonText,
    final String saveButtonText, Widget panel, final AsyncCallback<List<String>> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setHTML(title);
    dialogBox.addStyleName("edit-permissions-dialog");

    if (panel instanceof PermissionsPanel) {
      ((PermissionsPanel) panel).setOnDataLoadedCallback(new Runnable() {
        @Override
        public void run() {
          dialogBox.center();
        }
      });
    }

    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    buttonPanel.addStyleName("dialog-button-panel");
    final Button cancelButton = new Button(cancelButtonText);
    final Button saveButton = new Button(saveButtonText);
    buttonPanel.add(cancelButton);
    buttonPanel.add(saveButton);

    final FlowPanel content = new FlowPanel();
    content.addStyleName("content");
    content.add(panel);
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onSuccess(((PermissionsPanel) panel).getUserSelections());
      }
    });

    cancelButton.addStyleName("btn btn-link");
    saveButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static void showAddGroupsToRODAMember(SafeHtml title, final String cancelButtonText,
    final String confirmButtonText, Filter filter, final AsyncCallback<SelectedItems<RODAMember>> callback) {

    final DialogBox dialogBox = new DialogBox(false, true);

    dialogBox.addStyleName("ri-dialog add-groups-to-user-dialog");
    dialogBox.setHTML(title);
    final FlowPanel layout = new FlowPanel();

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");

    final FlowPanel buttonPanel = new FlowPanel();
    final Button cancelButton = new Button(cancelButtonText);
    final Button confirmButton = new Button(confirmButtonText);
    confirmButton.setEnabled(false);
    buttonPanel.add(cancelButton);
    buttonPanel.add(confirmButton);

    final FlowPanel content = new FlowPanel();
    content.addStyleName("row skip_padding full_width content");
    content.add(createInnerAddGroupList(dialogBox, confirmButton, filter, callback));
    layout.add(content);
    layout.add(buttonPanel);
    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    cancelButton.addStyleName("btn btn-link");
    confirmButton.addStyleName("pull-right btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }

  public static FlowPanel createInnerAddGroupList(final DialogBox dialogBox, final Button addGroupButton,
    final Filter filter, final AsyncCallback<SelectedItems<RODAMember>> callback) {
    FlowPanel container = new FlowPanel();
    container.addStyleName("wui-dialog-message");

    // create search box and results list

    ListBuilder<RODAMember> rodaMemberListBuilder = new ListBuilder<>(() -> new RodaMemberList(),
      new AsyncTableCellOptions<>(RODAMember.class, "RepresentationInformationDialogs_RI")
        .withSummary(messages.representationInformationTitle()).withInitialPageSize(10).withPageSizeIncrement(10)
        .withCsvDownloadButtonVisibility(false).withRecenteringOfParentDialog(dialogBox).withForceSelectable(true)
        .withFilter(filter).addCheckboxSelectionListener(new AsyncTableCell.CheckboxSelectionListener<RODAMember>() {
          @Override
          public void onSelectionChange(SelectedItems<RODAMember> selected) {
            addGroupButton.setEnabled(!(selected instanceof SelectedItemsNone)
              && (!(selected instanceof SelectedItemsList) || !((SelectedItemsList) selected).getIds().isEmpty()));
          }
        }));

    SearchWrapper searchWrapper = new SearchWrapper(false).withListsInsideScrollPanel("ri-dialog-list-scroll")
      .createListAndSearchPanel(rodaMemberListBuilder);

    container.add(searchWrapper);

    addGroupButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(searchWrapper.getSelectedItems(RODAMember.class));
    });

    return container;
  }

}
