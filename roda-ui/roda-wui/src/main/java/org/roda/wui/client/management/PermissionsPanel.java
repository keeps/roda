/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;

import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.widgets.LoadingPopup;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class PermissionsPanel extends FlowPanel implements HasValueChangeHandlers<List<String>> {

  private class Permission extends HorizontalPanel implements HasValueChangeHandlers<String> {

    private final String role;
    private boolean locked;
    private boolean enabled;
    private final CheckBox checkbox;
    private final Label descriptionLabel;

    public Permission(String role, String description) {
      this.role = role;
      this.checkbox = new CheckBox();
      this.descriptionLabel = new Label(description);
      this.add(checkbox);
      this.add(descriptionLabel);
      this.locked = false;
      this.enabled = true;

      this.descriptionLabel.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          if (isEnabled() && !locked) {
            checkbox.setValue(!checkbox.getValue());
            onChange();
          }
        }
      });

      this.checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          onChange();
        }
      });

      this.addStyleName("permission");
      WCAGUtilities.addTitleToCheckbox(checkbox, description);
      checkbox.addStyleName("permission-checkbox");
      descriptionLabel.setStylePrimaryName("permission-description");
    }

    public boolean isLocked() {
      return locked;
    }

    public void setLocked(boolean locked) {
      this.locked = locked;
      checkbox.setEnabled(!locked);
    }

    public boolean isChecked() {
      return checkbox.getValue();
    }

    public void setChecked(boolean checked) {
      checkbox.setValue(checked);
    }

    public String getRole() {
      return role;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!locked) {
        this.checkbox.setEnabled(enabled);
        if (enabled) {
          this.descriptionLabel.removeStyleDependentName("off");
          this.descriptionLabel.addStyleDependentName("on");
        } else {
          this.descriptionLabel.removeStyleDependentName("on");
          this.descriptionLabel.addStyleDependentName("off");
        }
      }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      return addHandler(handler, ValueChangeEvent.getType());
    }

    protected void onChange() {
      ValueChangeEvent.fire(this, getValue());
    }

    public String getValue() {
      return getRole();
    }

    private PermissionsPanel getOuterType() {
      return PermissionsPanel.this;
    }
  }

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());

  private final List<Permission> permissions;

  private List<String> userSelections;

  private boolean enabled;

  private final LoadingPopup loading;

  /**
   *
   */
  public PermissionsPanel() {
    this.permissions = new ArrayList<>();
    this.userSelections = new ArrayList<>();

    loading = new LoadingPopup(this);
    loading.show();

    this.enabled = true;

    this.addStyleName("permissions");
  }

  public void init(final AsyncCallback<Boolean> callback) {
    List<String> roles = ConfigurationManager.getStringList("ui.role");
    for (String role : roles) {
      String description;
      try {
        description = messages.role(role);
      } catch (MissingResourceException e) {
        description = role + " (needs translation)";
      }

      Permission permission = new Permission(role, description);
      permissions.add(permission);
      PermissionsPanel.this.add(permission);
      permission.addValueChangeHandler(new ValueChangeHandler<String>() {

        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          if (userSelections.contains(event.getValue())) {
            userSelections.remove(event.getValue());
          } else {
            userSelections.add(event.getValue());
          }
          onChange();
        }
      });
    }

    loading.hide();
    callback.onSuccess(true);
  }

  public List<String> getUserSelections() {
    return userSelections;
  }

  /**
   * Set all permissions defined by roles checked and set locked with parameters
   *
   * @param roles
   *          roles of the permissions to check
   * @param lock
   *          if permissions should also be locked
   */
  public void checkPermissions(Set<String> roles, boolean lock) {
    Iterator<String> it = roles.iterator();

    while (it.hasNext()) {
      String role = it.next();
      boolean foundit = false;
      for (Iterator<Permission> j = permissions.iterator(); j.hasNext() && !foundit;) {
        Permission p = j.next();
        if (p.getRole().equals(role)) {
          foundit = true;
          p.setChecked(true);
          p.setLocked(lock);
        }
      }
    }
  }

  @Override
  public void clear() {
    for (Permission p : permissions) {
      p.setChecked(false);
      p.setLocked(false);
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    for (Permission p : permissions) {
      p.setEnabled(enabled);
    }
  }

  public void updateLockedPermissions(Set<String> memberGroups) {
    if (!memberGroups.isEmpty()) {
      this.setEnabled(false);
      loading.show();
    }
  }

  /**
   * Get roles that are directly defined, i.e. are not inherited
   *
   * @return
   */
  public Set<String> getDirectRoles() {
    List<Permission> checkedPermissions = new ArrayList<>();
    for (Permission p : permissions) {
      if (p.isChecked() && !p.isLocked()) {
        checkedPermissions.add(p);
      }
    }

    Set<String> specialRoles = new HashSet<>();
    for (int i = 0; i < checkedPermissions.size(); i++) {
      specialRoles.add(checkedPermissions.get(i).getRole());
    }

    return specialRoles;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    ValueChangeEvent.fire(this, getValue());
  }

  public List<String> getValue() {
    return getUserSelections();
  }
}
