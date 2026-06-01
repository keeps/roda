package org.roda.wui.client.common.dialogs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaPrincipal;
import org.roda.wui.client.common.lists.RodaMemberList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class MemberPermissionsSelectDialog extends DialogBox {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final SearchWrapper searchWrapper;
  private final PermissionSelectionPanel permissionSelectionPanel;
  private final Button selectButton;

  public MemberPermissionsSelectDialog(String title, Filter filter, AsyncCallback<Result> callback) {
    super(false, true);

    FlowPanel layout = new FlowPanel();
    FlowPanel content = new FlowPanel();
    FlowPanel footer = new FlowPanel();

    Button cancelButton = new Button(messages.cancelButton());
    selectButton = new Button(messages.saveButton());
    selectButton.setEnabled(false);

    permissionSelectionPanel = new PermissionSelectionPanel(this::updateSelectButton);

    ListBuilder<RODAMember> listBuilder = new ListBuilder<>(() -> new RodaMemberList(),
      new AsyncTableCellOptions<>(RODAMember.class, "MemberPermissionsSelectDialog_rodaMembers").withFilter(filter)
        .withCsvDownloadButtonVisibility(false).withSummary(title).withRecenteringOfParentDialog(this)
        .withForceSelectable(true)
        .addCheckboxSelectionListener(new AsyncTableCell.CheckboxSelectionListener<RODAMember>() {
          @Override
          public void onSelectionChange(SelectedItems<RODAMember> selected) {
            updateSelectButton();
          }
        }));

    searchWrapper = new SearchWrapper(false).withListsInsideScrollPanel("selectAipResultsPanel")
      .createListAndSearchPanel(listBuilder);

    FlowPanel permissionsSection = new FlowPanel();
    permissionsSection.addStyleName("generic-metadata-panel");

    Label permissionsTitle = new Label(messages.userPermissions());
    permissionsTitle.addStyleName("form-separator");

    permissionsSection.add(permissionsTitle);
    permissionsSection.add(permissionSelectionPanel);

    content.add(searchWrapper);
    content.add(permissionsSection);

    footer.add(cancelButton);
    footer.add(selectButton);

    layout.add(content);
    layout.add(footer);

    setText(title);
    setWidget(layout);
    setGlassEnabled(true);
    setAnimationEnabled(false);
    addStyleName("wui-dialog-prompt");

    layout.addStyleName("wui-dialog-layout");
    content.addStyleName("wui-dialog-layout-content");
    footer.addStyleName("wui-dialog-layout-footer");
    cancelButton.addStyleName("btn btn-link");
    selectButton.addStyleName("pull-right btn btn-play");

    cancelButton.addClickHandler(event -> {
      hide();
      callback.onFailure(null);
    });

    selectButton.addClickHandler(event -> {
      List<String> selectedMembers = getSelectedMemberNames();

      if (!selectedMembers.isEmpty() && permissionSelectionPanel.hasSelectedPermissions()) {
        hide();
        callback.onSuccess(new Result(selectedMembers, permissionSelectionPanel.getPermissions()));
      }
    });
  }

  public void showAndCenter() {
    if (Window.getClientWidth() < 800) {
      setWidth(Window.getClientWidth() + "px");
    }

    show();
    center();
  }

  private void updateSelectButton() {
    selectButton.setEnabled(!getSelectedMemberNames().isEmpty() && permissionSelectionPanel.hasSelectedPermissions());
  }

  private List<String> getSelectedMemberNames() {
    SelectedItems<RODAMember> selectedItems = searchWrapper.getSelectedItems(RODAMember.class);

    if (selectedItems instanceof SelectedItemsList<?>) {
      return ((SelectedItemsList<RODAMember>) selectedItems).getIds().stream().map(RodaPrincipal::getId)
        .collect(Collectors.toList());
    }

    return List.of();
  }

  public static class Result {
    private final List<String> memberNames;
    private final Set<PermissionType> permissions;

    public Result(List<String> memberNames, Set<PermissionType> permissions) {
      this.memberNames = memberNames;
      this.permissions = permissions;
    }

    public List<String> getMemberNames() {
      return memberNames;
    }

    public Set<PermissionType> getPermissions() {
      return permissions;
    }
  }

  private static class PermissionSelectionPanel extends Composite {
    private final FlowPanel permissionsPanel;
    private final Runnable valueChangeHandler;

    public PermissionSelectionPanel(Runnable valueChangeHandler) {
      this.valueChangeHandler = valueChangeHandler;

      permissionsPanel = new FlowPanel();

      for (PermissionType permissionType : Permissions.PermissionType.values()) {
        PermissionCheckBox checkBox = new PermissionCheckBox(permissionType);
        checkBox.addStyleName("my-custom-checkbox");
        checkBox.addStyleName("permission-edit-checkbox");
        checkBox.addValueChangeHandler(event -> this.valueChangeHandler.run());
        permissionsPanel.add(checkBox);
      }

      initWidget(permissionsPanel);
    }

    public boolean hasSelectedPermissions() {
      return !getPermissions().isEmpty();
    }

    public Set<PermissionType> getPermissions() {
      Set<PermissionType> permissions = new HashSet<>();

      for (int i = 0; i < permissionsPanel.getWidgetCount(); i++) {
        PermissionCheckBox checkBox = (PermissionCheckBox) permissionsPanel.getWidget(i);
        if (Boolean.TRUE.equals(checkBox.getValue())) {
          permissions.add(checkBox.getPermissionType());
        }
      }

      return permissions;
    }
  }
}
