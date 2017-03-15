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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.user.Group;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 *
 */
public class GroupDataPanel extends Composite implements HasValueChangeHandlers<Group> {

  interface MyUiBinder extends UiBinder<Widget, GroupDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Group group = new Group();

  @UiField
  TextBox groupname;

  @UiField
  TextBox fullname;

  @UiField
  Label usersLabel, usersValue;

  @UiField
  FlowPanel permissionsSelectPanel;

  @UiField
  PermissionsPanel permissionsPanel;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean editmode;

  private boolean changed = false;
  private boolean checked = false;

  /**
   * Create a new group data panel
   *
   * @param editmode
   *          if group name should be editable
   */
  public GroupDataPanel(boolean editmode) {
    this(true, editmode);
  }

  /**
   *
   * @param visible
   * @param editmode
   */
  public GroupDataPanel(boolean visible, boolean editmode) {

    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        GroupDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    groupname.setEnabled(!isEditmode());
    groupname.addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();

        if (!(keyCode >= '0' && keyCode <= '9') && !(keyCode >= 'A' && keyCode <= 'Z')
          && !(keyCode >= 'a' && keyCode <= 'z') && keyCode != '.' && keyCode != '_' && (keyCode != KeyCodes.KEY_TAB)
          && (keyCode != KeyCodes.KEY_DELETE) && (keyCode != KeyCodes.KEY_ENTER) && (keyCode != KeyCodes.KEY_HOME)
          && (keyCode != KeyCodes.KEY_END) && (keyCode != KeyCodes.KEY_LEFT) && (keyCode != KeyCodes.KEY_UP)
          && (keyCode != KeyCodes.KEY_RIGHT) && (keyCode != KeyCodes.KEY_DOWN) && (keyCode != KeyCodes.KEY_BACKSPACE)) {
          ((TextBox) event.getSource()).cancelKey();
        }
      }
    });
    groupname.addChangeHandler(changeHandler);
    groupname.addKeyUpHandler(keyUpHandler);
    fullname.addChangeHandler(changeHandler);
    fullname.addKeyUpHandler(keyUpHandler);

    permissionsPanel.addValueChangeHandler(new ValueChangeHandler<List<String>>() {

      @Override
      public void onValueChange(ValueChangeEvent<List<String>> event) {
        onChange();
      }
    });
  }

  /**
   * Set group information of group
   *
   * @param group
   */
  public void setGroup(Group group) {
    this.group = group;
    this.groupname.setText(group.getName());
    this.fullname.setText(group.getFullName());
    this.usersValue.setText(StringUtils.prettyPrint(group.getUsers()));

    this.setPermissions(group.getDirectRoles(), group.getAllRoles());

    // update visibility
    this.usersLabel.setVisible(!group.getUsers().isEmpty());
    this.usersValue.setVisible(!group.getUsers().isEmpty());

  }

  private void setPermissions(final Set<String> directRoles, final Set<String> allRoles) {
    permissionsPanel.init(new AsyncCallback<Boolean>() {

      @Override
      public void onSuccess(Boolean result) {
        Set<String> indirectRoles = new HashSet<>(allRoles);
        indirectRoles.removeAll(directRoles);

        permissionsPanel.checkPermissions(directRoles, false);
        permissionsPanel.checkPermissions(indirectRoles, true);
        WCAGUtilities.getInstance().makeAccessible(permissionsSelectPanel.getElement());
      }

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        HistoryUtils.newHistory(MemberManagement.RESOLVER);
      }
    });
  }

  /**
   * Get group defined by this panel. This panel defines: name, fullname
   *
   * @return the group modified by this panel
   */
  public Group getGroup() {
    group.setId(groupname.getText());
    group.setName(groupname.getText());
    group.setFullName(fullname.getText());
    group.setDirectRoles(permissionsPanel.getDirectRoles());

    return group;
  }

  /**
   * Is group data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    boolean valid = true;

    if (groupname.getText().length() == 0) {
      valid = false;
      groupname.addStyleName("isWrong");
    } else {
      groupname.removeStyleName("isWrong");
    }

    if (fullname.getText().length() == 0) {
      valid = false;
      fullname.addStyleName("isWrong");
    } else {
      fullname.removeStyleName("isWrong");
    }

    checked = true;

    return valid;
  }

  /**
   * Is group name read only
   *
   * @return true if read only
   */
  public boolean isGroupnameReadOnly() {
    return groupname.isReadOnly();
  }

  /**
   * Set group name read only
   *
   * @param readonly
   */
  public void setGroupnameReadOnly(boolean readonly) {
    groupname.setReadOnly(readonly);
  }

  public void clear() {
    groupname.setText("");
    fullname.setText("");
  }

  /**
   * Is group data panel editable, i.e. on create group mode
   *
   * @return true if editable
   */
  public boolean isEditmode() {
    return editmode;
  }

  /**
   * Is group data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Group> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public Group getValue() {
    return getGroup();
  }
}
