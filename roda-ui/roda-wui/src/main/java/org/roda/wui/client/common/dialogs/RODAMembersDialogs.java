package org.roda.wui.client.common.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.accessKey.CreateAccessKeyRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.lists.RodaMemberList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.management.members.PasswordPanel;
import org.roda.wui.client.management.members.data.panels.PasswordDataPanel;
import org.roda.wui.client.management.members.tabs.PermissionsPanel;
import org.roda.wui.common.client.tools.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class RODAMembersDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private RODAMembersDialogs() {
    // private method
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

  public static void setUserPassword(String title, final AsyncCallback<String> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    final Button cancelButton = new Button(messages.cancelButton());
    final Button confirmButton = new Button(messages.confirmButton());
    final FlowPanel layout = new FlowPanel();
    final FlowPanel header = new FlowPanel();
    final FlowPanel footer = new FlowPanel();

    PasswordDataPanel passwordDataPanel = new PasswordDataPanel(true);

    dialogBox.setText(title);
    layout.add(header);
    layout.add(footer);
    header.add(passwordDataPanel);

    footer.add(cancelButton);
    footer.add(confirmButton);

    confirmButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        if (passwordDataPanel.isValid()) {
            dialogBox.hide();
            callback.onSuccess(passwordDataPanel.getValue());
        }
      }
    });

    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        dialogBox.hide();
        callback.onFailure(null);
      }
    });

    dialogBox.setWidget(layout);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    dialogBox.addStyleName("wui-dialog-prompt");
    layout.addStyleName("wui-dialog-layout");
    header.addStyleName("wui-dialog-message");
    footer.addStyleName("wui-dialog-layout-footer");

    confirmButton.addStyleName("btn btn-play");
    cancelButton.addStyleName("btn btn-link");

    dialogBox.center();
    dialogBox.show();
  }
}
