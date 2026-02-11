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
package org.roda.wui.client.management.members;

import org.roda.core.data.v2.user.Group;
import org.roda.wui.common.client.ClientLogger;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 *
 */
public class GroupDataPanel extends Composite implements HasValueChangeHandlers<Group> {

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  TextBox groupName;
  @UiField
  TextBox fullName;
  
  private Group group = new Group();
  @SuppressWarnings("unused")
  private final ClientLogger logger = new ClientLogger(getClass().getName());
  
  private final boolean editMode;
  private boolean changed = false;
  private boolean checked = false;
  /**
   * Create a new group data panel
   *
   * @param editMode
   *          if group name should be editable
   */
  public GroupDataPanel(boolean editMode) {
    initWidget(uiBinder.createAndBindUi(this));

    this.editMode = editMode;
    super.setVisible(true);

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

    groupName.setEnabled(!isEditMode());
    groupName.addKeyDownHandler(new UserAndGroupKeyDownHandler());
    groupName.addChangeHandler(changeHandler);
    groupName.addKeyUpHandler(keyUpHandler);
    fullName.addChangeHandler(changeHandler);
    fullName.addKeyUpHandler(keyUpHandler);
  }
  
  /**
   * Get group defined by this panel. This panel defines: name, fullname
   *
   * @return the group modified by this panel
   */
  public Group getGroup() {
    group.setId(groupName.getText());
    group.setName(groupName.getText());
    group.setFullName(fullName.getText());
    return group;
  }

  /**
   * Set group information of group
   *
   * @param group
   */
  public void setGroup(Group group) {
    this.group = group;
    this.groupName.setText(group.getName());
    this.fullName.setText(group.getFullName());
  }

  /**
   * Is group data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    boolean valid = true;

    if (groupName.getText().isEmpty()) {
      valid = false;
      groupName.addStyleName("isWrong");
    } else {
      groupName.removeStyleName("isWrong");
    }

    if (fullName.getText().isEmpty()) {
      valid = false;
      fullName.addStyleName("isWrong");
    } else {
      fullName.removeStyleName("isWrong");
    }

    checked = true;
    return valid;
  }

  /**
   * Is group name read only
   *
   * @return true if read only
   */
  public boolean isGroupNameReadOnly() {
    return groupName.isReadOnly();
  }

  /**
   * Set group name read only
   *
   * @param readonly
   */
  public void setGroupNameReadOnly(boolean readonly) {
    groupName.setReadOnly(readonly);
  }

  public void clear() {
    groupName.setText("");
    fullName.setText("");
  }

  /**
   * Is group data panel editable, i.e. on create group mode
   *
   * @return true if editable
   */
  public boolean isEditMode() {
    return editMode;
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

  interface MyUiBinder extends UiBinder<Widget, GroupDataPanel> {
  }
}
