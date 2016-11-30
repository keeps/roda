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
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.FormUtilities;
import org.roda.wui.client.browse.UserExtraBundle;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;

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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class UserDataPanel extends Composite implements HasValueChangeHandlers<User> {

  interface MyUiBinder extends UiBinder<Widget, UserDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox username;

  @UiField
  Label usernameError;

  @UiField(provided = true)
  PasswordPanel password;

  @UiField
  Label passwordError;

  @UiField
  TextBox fullname;

  @UiField
  Label fullnameError;

  @UiField
  TextBox email;

  @UiField
  Label emailError;

  @UiField
  FlowPanel extra;

  @UiField
  FlowPanel groupSelectPanel;

  @UiField(provided = true)
  GroupSelect groupSelect;

  @UiField
  FlowPanel permissionsSelectPanel;

  @UiField
  PermissionsPanel permissionsPanel;

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean enableGroupSelect;

  private boolean editmode;

  // has to be true to detected new field changes
  private boolean changed = true;
  private boolean checked = false;
  private UserExtraBundle userExtraBundle = null;

  @UiField
  HTML errors;

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   * @param enableGroupSelect
   *          if the list of groups to which the user belong to should be
   *          editable
   *
   */
  public UserDataPanel(boolean editmode, boolean enableGroupSelect) {
    this(true, editmode, enableGroupSelect, true);
  }

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   * @param enableGroupSelect
   *          if the list of groups to which the user belong to should be
   *          editable
   *
   */
  public UserDataPanel(boolean visible, boolean editmode, boolean enableGroupSelect) {
    this(visible, editmode, enableGroupSelect, true);
  }

  /**
   * Create a new user data panel
   *
   * @param visible
   * @param editmode
   * @param enableGroupSelect
   * @param enablePermissions
   */
  public UserDataPanel(boolean visible, boolean editmode, boolean enableGroupSelect, boolean enablePermissions) {

    password = new PasswordPanel(editmode);
    groupSelect = new GroupSelect(enableGroupSelect);

    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);
    this.enableGroupSelect = enableGroupSelect;

    errors.setVisible(false);

    groupSelectPanel.setVisible(enableGroupSelect);
    permissionsSelectPanel.setVisible(enablePermissions);

    ValueChangeHandler<String> valueChangedHandler = new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onChange();
      }
    };

    ChangeHandler changeHandler = new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        UserDataPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    username.addKeyDownHandler(new KeyDownHandler() {

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

    username.addChangeHandler(changeHandler);
    username.addKeyUpHandler(keyUpHandler);
    password.addValueChangeHandler(valueChangedHandler);
    fullname.addChangeHandler(changeHandler);
    fullname.addKeyUpHandler(keyUpHandler);

    permissionsPanel.addValueChangeHandler(new ValueChangeHandler<List<String>>() {

      @Override
      public void onValueChange(ValueChangeEvent<List<String>> event) {
        onChange();
      }
    });

    groupSelect.addValueChangeHandler(new ValueChangeHandler<List<Group>>() {

      @Override
      public void onValueChange(ValueChangeEvent<List<Group>> event) {
        updatePermissions(event.getValue());
        onChange();
      }
    });

    usernameError.setVisible(false);
    passwordError.setVisible(false);
    fullnameError.setVisible(false);
    emailError.setVisible(false);
  }

  @SuppressWarnings("unused")
  private int setSelected(ListBox listbox, String text) {
    int index = -1;
    if (text != null) {
      for (int i = 0; i < listbox.getItemCount(); i++) {
        if (listbox.getValue(i).equals(text)) {
          index = i;
          break;
        }
      }
      if (index >= 0) {
        listbox.setSelectedIndex(index);
      } else {
        listbox.addItem(text);
        index = listbox.getItemCount() - 1;
        listbox.setSelectedIndex(index);
      }
    } else {
      listbox.setSelectedIndex(-1);
    }
    return index;
  }

  /**
   * Set user information of user
   *
   * @param user
   */
  public void setUser(User user) {
    this.username.setText(user.getName());
    this.fullname.setText(user.getFullName());
    this.email.setText(user.getEmail());

    this.setMemberGroups(user.getGroups());
    this.setPermissions(user.getDirectRoles(), user.getAllRoles());

    UserManagementService.Util.getInstance().retrieveUserExtraBundle(user.getName(),
      new AsyncCallback<UserExtraBundle>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(UserExtraBundle userExtra) {
          UserDataPanel.this.userExtraBundle = userExtra;
          createForm(userExtra);
        }
      });
  }

  public void setExtraBundle(UserExtraBundle bundle) {
    UserDataPanel.this.userExtraBundle = bundle;
    createForm(bundle);
  }

  private void setPermissions(final Set<String> directRoles, final Set<String> allRoles) {
    permissionsPanel.init(new AsyncCallback<Boolean>() {

      @Override
      public void onSuccess(Boolean result) {
        Set<String> indirectRoles = new HashSet<String>(allRoles);
        indirectRoles.removeAll(directRoles);

        permissionsPanel.checkPermissions(directRoles, false);
        permissionsPanel.checkPermissions(indirectRoles, true);
      }

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
        HistoryUtils.newHistory(MemberManagement.RESOLVER);
      }
    });
  }

  private void updatePermissions(List<Group> groups) {
    permissionsPanel.clear();
    permissionsPanel.checkPermissions(new HashSet<String>(permissionsPanel.getUserSelections()), false);
    for (Group group : groups) {
      permissionsPanel.checkPermissions(group.getAllRoles(), true);
    }
  }

  /**
   * Get user defined by this panel. This panel defines: name, fullname, title,
   * organization name, postal address, postal code, locality, country, email,
   * phone number, fax and which groups this user belongs to.
   *
   * @return the user modified by this panel
   */
  public User getUser() {
    User user = new User();
    user.setId(username.getText());
    user.setName(username.getText());
    user.setFullName(fullname.getText());
    user.setEmail(email.getText());

    if (enableGroupSelect) {
      user.setGroups(this.getMemberGroups());
    }

    user.setDirectRoles(permissionsPanel.getDirectRoles());
    return user;
  }

  public void createForm(UserExtraBundle bundle) {
    extra.clear();
    FormUtilities.create(extra, bundle.getValues(), false);

  }

  /**
   * Set the groups of which this user is member of
   *
   * @param groups
   */
  public void setMemberGroups(final Set<String> groups) {
    if (enableGroupSelect) {
      groupSelect.init(new AsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean result) {
          groupSelect.setMemberGroups(groups);
        }

        @Override
        public void onFailure(Throwable caught) {
          // Tools.newHistory(MemberManagement.RESOLVER);
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }
      });
    }
  }

  /**
   * Get the groups of which this user is member of
   *
   * @return a list of group names
   */
  public Set<String> getMemberGroups() {
    return enableGroupSelect ? groupSelect.getMemberGroups() : null;
  }

  /**
   * Get the password
   *
   * @return the password if changed, or null if it remains the same
   */
  public String getPassword() {
    return password.getValue();
  }

  /**
   * Check if password changed
   *
   * @return true if password changed, false otherwise
   */
  public boolean isPasswordChanged() {
    return password.isChanged();
  }

  /**
   * Is user data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<String>();
    if (username.getText().length() == 0) {
      username.addStyleName("isWrong");
      usernameError.setText(messages.mandatoryField());
      usernameError.setVisible(true);
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      username.removeStyleName("isWrong");
      usernameError.setVisible(false);
    }

    if (!password.isValid()) {
      errorList.add(messages.isNotValid(messages.password()));
    }

    if (fullname.getText().length() == 0) {
      fullname.addStyleName("isWrong");
      fullnameError.setText(messages.mandatoryField());
      fullnameError.setVisible(true);
      errorList.add(messages.isAMandatoryField(messages.fullname()));

    } else {
      fullname.removeStyleName("isWrong");
      fullnameError.setVisible(false);
    }

    if (email.getText() == null || email.getText().trim().equalsIgnoreCase("")) {
      email.addStyleName("isWrong");
      emailError.setText(messages.mandatoryField());
      emailError.setVisible(true);
      errorList.add(messages.isAMandatoryField(messages.email()));
    } else if (!email.getText()
      .matches("^[_A-Za-z0-9-%+]+(\\.[_A-Za-z0-9-%+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)")) {
      email.addStyleName("isWrong");
      emailError.setText(messages.wrongMailFormat());
      emailError.setVisible(true);
      errorList.add(messages.isNotValid(messages.email()));
    } else {
      email.removeStyleName("isWrong");
      emailError.setVisible(false);
    }

    List<String> extraErrors = FormUtilities.validate(userExtraBundle.getValues(), extra);

    errorList.addAll(extraErrors);

    checked = true;

    GWT.log("ERRORS: " + errorList.size());
    if (errorList.size() > 0) {
      errors.setVisible(true);
      String errorString = "";
      for (String error : errorList) {
        errorString += "<span class='error'>" + error + "</span>";
        errorString += "<br/>";
      }
      errors.setHTML(errorString);
    } else {
      errors.setVisible(false);
    }
    return errorList.size() == 0 ? true : false;
  }

  /**
   * Is user name read only
   *
   * @return true if read only
   */
  public boolean isUsernameReadOnly() {
    return username.isReadOnly();
  }

  /**
   * Set user name read only
   *
   * @param readonly
   */
  public void setUsernameReadOnly(boolean readonly) {
    username.setReadOnly(readonly);
  }

  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (enableGroupSelect) {
      groupSelect.setVisible(visible);
    }
  }

  public void clear() {
    username.setText("");
    password.clear();
    fullname.setText("");
    email.setText("");
  }

  /**
   * Is user data panel editable, i.e. on create user mode
   *
   * @return true if editable
   */
  public boolean isEditmode() {
    return editmode;
  }

  /**
   * Is user data panel has been changed
   *
   * @return changed
   */
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<User> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange() {
    changed = true;
    if (checked) {
      isValid();
    }
    ValueChangeEvent.fire(this, getValue());
  }

  public User getValue() {
    return getUser();
  }

  public UserExtraBundle getExtra() {
    return userExtraBundle;
  }
}
