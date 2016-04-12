/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.client.common.SearchPanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.SimpleRodaMemberList;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

public class EditPermissions extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        final String aipId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieve(IndexedAIP.class.getName(), aipId, new AsyncCallback<IndexedAIP>() {

          @Override
          public void onFailure(Throwable caught) {
            Tools.newHistory(Browse.RESOLVER);
            callback.onSuccess(null);
          }

          @Override
          public void onSuccess(IndexedAIP aip) {
            EditPermissions edit = new EditPermissions(aip);
            callback.onSuccess(edit);
          }
        });

      } else {
        Tools.newHistory(Browse.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {EditPermissions.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Browse.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_permissions";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditPermissions> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true)
  SearchPanel searchPanel;

  @UiField(provided = true)
  SimpleRodaMemberList list;

  @UiField
  FlowPanel permissionsPanel;

  private IndexedAIP aip;

  public EditPermissions(IndexedAIP aip) {
    this.aip = aip;

    Filter filter = null;
    list = new SimpleRodaMemberList(filter, null, "Users and groups", false);

    searchPanel = new SearchPanel(null, null, "", false, false);
    searchPanel.setList(list);

    initWidget(uiBinder.createAndBindUi(this));

    list.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        RODAMember selected = list.getSelectionModel().getSelectedObject();
        if (selected != null) {
          addPermissionPanel(selected);
        }
      }
    });

    createPermissionPanel();
  }

  private void createPermissionPanel() {
    Permissions permissions = aip.getPermissions();
    
    GWT.log(aip.getPermissions().toString());

    for (String username : permissions.getUsernames()) {
      permissionsPanel.add(new PermissionPanel(username, true, permissions.getUserPermissions(username)));
    }

    for (String groupname : permissions.getGroupnames()) {
      permissionsPanel.add(new PermissionPanel(groupname, false, permissions.getGroupPermissions(groupname)));
    }
  }

  public void addPermissionPanel(RODAMember member) {
    permissionsPanel.insert(new PermissionPanel(member), 0);
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    Permissions permissions = new Permissions();

    for (int i = 0; i < permissionsPanel.getWidgetCount(); i++) {
      PermissionPanel permissionPanel = (PermissionPanel) permissionsPanel.getWidget(i);

      if (permissionPanel.isUser()) {
        permissions.setUserPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      } else {
        permissions.setGroupPermissions(permissionPanel.getName(), permissionPanel.getPermissions());
      }
    }
    
    BrowserService.Util.getInstance().updateAIPPermssions(aip.getId(), permissions, new AsyncCallback<Void>() {
      
      @Override
      public void onSuccess(Void result) {
        cancel();
      }
      
      @Override
      public void onFailure(Throwable caught) {
        Toast.showError(caught.getMessage());
        cancel();
      }
    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }
  
  public void cancel() {
    Tools.newHistory(Browse.RESOLVER, aip.getId());
  }

  public class PermissionPanel extends Composite {
    private FlowPanel panel;
    private FlowPanel panelBody;
    private SafeHtml type;
    private Label nameLabel;
    private FlowPanel showPermissionsPanel;
    private FlowPanel editPermissionsPanel;
    private Button removePanel;
    private Button editPermissionsButton;
    private Button savePermissionsButton;
    private Button cancelEditPermissionsButton;

    private String name;
    private boolean isUser;

    public PermissionPanel(RODAMember member) {
      this(member.getName(), member.isUser(), new HashSet<PermissionType>());
    }

    public PermissionPanel(String name, boolean isUser, Set<PermissionType> permissions) {
      this.name = name;
      this.isUser = isUser;

      panel = new FlowPanel();
      panelBody = new FlowPanel();

      type = SafeHtmlUtils.fromSafeConstant(isUser ? "<i class='fa fa-user'></i>" : "<i class='fa fa-users'></i>");
      nameLabel = new Label(name);

      showPermissionsPanel = new FlowPanel();
      editPermissionsPanel = new FlowPanel();

      for (PermissionType permissionType : Permissions.PermissionType.values()) {
        ValueCheckBox valueCheckBox = new ValueCheckBox(permissionType, permissionType.toString());
        if (permissions.contains(permissionType)) {
          valueCheckBox.setValue(true);
        }
        editPermissionsPanel.add(valueCheckBox);
      }

      removePanel = new Button("Remove");
      editPermissionsButton = new Button("Edit");
      savePermissionsButton = new Button("Save");
      cancelEditPermissionsButton = new Button("Cancel");

      panelBody.add(new HTML(type));
      panelBody.add(nameLabel);
      panelBody.add(showPermissionsPanel);
      panelBody.add(editPermissionsPanel);
      panelBody.add(removePanel);
      panelBody.add(editPermissionsButton);
      panelBody.add(savePermissionsButton);
      panelBody.add(cancelEditPermissionsButton);

      panel.add(panelBody);

      initWidget(panel);

      panel.addStyleName("panel");
      panelBody.addStyleName("panel-body");

      removePanel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          PermissionPanel.this.removeFromParent();
        }
      });

      editPermissionsButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          setEditMode(true);
        }
      });

      savePermissionsButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          showPermissionsPanel.clear();
          for (int i = 0; i < editPermissionsPanel.getWidgetCount(); i++) {
            ValueCheckBox valueCheckBox = (ValueCheckBox) editPermissionsPanel.getWidget(i);
            if (valueCheckBox.getValue()) {
              showPermissionsPanel.add(new Label(valueCheckBox.getPermissionType().toString()));
            }
          }
          setEditMode(false);
        }
      });

      cancelEditPermissionsButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          setEditMode(false);
        }
      });

      setEditMode(true);
    }

    private void setEditMode(boolean editMode) {
      showPermissionsPanel.setVisible(!editMode);
      removePanel.setVisible(!editMode);
      editPermissionsButton.setVisible(!editMode);

      editPermissionsPanel.setVisible(editMode);
      savePermissionsButton.setVisible(editMode);
      cancelEditPermissionsButton.setVisible(editMode);
    }

    public Set<PermissionType> getPermissions() {
      HashSet<PermissionType> permissions = new HashSet<>();
      for (int i = 0; i < editPermissionsPanel.getWidgetCount(); i++) {
        ValueCheckBox valueCheckBox = (ValueCheckBox) editPermissionsPanel.getWidget(i);
        if (valueCheckBox.getValue()) {
          permissions.add(valueCheckBox.getPermissionType());
        }
      }
      return permissions;
    }
    
    public String getName() {
      return name;
    }

    public boolean isUser() {
      return isUser;
    }

    public class ValueCheckBox extends CheckBox {
      private PermissionType permissionType;

      public ValueCheckBox(PermissionType permissionType, String label) {
        super(label);
        this.permissionType = permissionType;
      }

      public PermissionType getPermissionType() {
        return permissionType;
      }
    }
  }
}
