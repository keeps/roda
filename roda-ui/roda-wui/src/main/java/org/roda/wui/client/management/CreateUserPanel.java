package org.roda.wui.client.management;

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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class CreateUserPanel extends Composite implements HasValueChangeHandlers<User> {

  interface MyUiBinder extends UiBinder<Widget, CreateUserPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  TextBox username;

  @UiField
  Label usernameError;

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
  private Set<MetadataValue> userExtra;

  @UiField
  HTML errors;

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   * @param enableGroupSelect
   *          if the list of groups to which the user belong to should be editable
   *
   */
  public CreateUserPanel(boolean editmode, boolean enableGroupSelect) {
    this(true, editmode, enableGroupSelect, true);
  }

  /**
   * Create a new user data panel
   *
   * @param editmode
   *          if user name should be editable
   * @param enableGroupSelect
   *          if the list of groups to which the user belong to should be editable
   *
   */
  public CreateUserPanel(boolean visible, boolean editmode, boolean enableGroupSelect) {
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
  public CreateUserPanel(boolean visible, boolean editmode, boolean enableGroupSelect, boolean enablePermissions) {

    groupSelect = new GroupSelect();

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
        CreateUserPanel.this.onChange();
      }
    };

    KeyUpHandler keyUpHandler = new KeyUpHandler() {

      @Override
      public void onKeyUp(KeyUpEvent event) {
        onChange();
      }
    };

    username.addKeyDownHandler(new UserAndGroupKeyDownHandler());

    username.addChangeHandler(changeHandler);
    username.addKeyUpHandler(keyUpHandler);
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


    Services services = new Services("Get User extra", "get");
    services.membersResource(s -> s.getUserExtra(user.getName())).whenComplete((extra, error) -> {
      if (extra != null) {
        CreateUserPanel.this.userExtra = extra;
        createForm(userExtra);
      } else if (error != null) {
        if (error instanceof AuthorizationDeniedException) {
          // TODO inform user he does not have permissions to see to which
          // groups he belongs to.
          GWT.log("No permissions: " + error.getMessage());
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        }
      }
    });
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
        if (caught instanceof AuthorizationDeniedException) {
          // TODO inform user he does not have permissions to see to which
          // groups he belongs to.
        } else {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
          HistoryUtils.newHistory(MemberManagement.RESOLVER);
        }
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

  public void createForm(Set<MetadataValue> userExtra) {
    extra.clear();
    FormUtilities.create(extra, userExtra, false);
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
          WCAGUtilities.getInstance().makeAccessible(groupSelectPanel.getElement());
        }

        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof AuthorizationDeniedException) {
            // TODO inform user he does not have permissions to see to which
            // groups he belongs to.
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(caught);
          }
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
   * Is user data panel valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    List<String> errorList = new ArrayList<>();
    if (username.getText().length() == 0) {
      username.addStyleName("isWrong");
      usernameError.setText(messages.mandatoryField());
      usernameError.setVisible(true);
      Window.scrollTo(username.getAbsoluteLeft(), username.getAbsoluteTop());
      errorList.add(messages.isAMandatoryField(messages.username()));
    } else {
      username.removeStyleName("isWrong");
      usernameError.setVisible(false);
    }

    if (fullname.getText().length() == 0) {
      fullname.addStyleName("isWrong");
      fullnameError.setText(messages.mandatoryField());
      fullnameError.setVisible(true);
      if (errorList.isEmpty()) {
        Window.scrollTo(fullname.getAbsoluteLeft(), fullname.getAbsoluteTop());
      }
      errorList.add(messages.isAMandatoryField(messages.fullname()));
    } else {
      fullname.removeStyleName("isWrong");
      fullnameError.setVisible(false);
    }

    if (email.getText() == null || "".equals(email.getText().trim())) {
      email.addStyleName("isWrong");
      emailError.setText(messages.mandatoryField());
      emailError.setVisible(true);
      if (errorList.isEmpty()) {
        Window.scrollTo(email.getAbsoluteLeft(), email.getAbsoluteTop());
      }
      errorList.add(messages.isAMandatoryField(messages.email()));
    } else if (!email.getText()
      .matches("^[_A-Za-z0-9-%+]+(\\.[_A-Za-z0-9-%+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)")) {
      email.addStyleName("isWrong");
      emailError.setText(messages.wrongMailFormat());
      emailError.setVisible(true);
      if (errorList.isEmpty()) {
        Window.scrollTo(email.getAbsoluteLeft(), email.getAbsoluteTop());
      }
      errorList.add(messages.emailNotValid());
    } else {
      email.removeStyleName("isWrong");
      emailError.setVisible(false);
    }

    List<String> extraErrors = FormUtilities.validate(userExtra, extra);
    errorList.addAll(extraErrors);
    checked = true;

    if (!errorList.isEmpty()) {
      errors.setVisible(true);
      StringBuilder errorString = new StringBuilder();
      for (String error : errorList) {
        errorString.append("<span class='error'>").append(error).append("</span>");
        errorString.append("<br/>");
      }
      errors.setHTML(errorString.toString());
    } else {
      errors.setVisible(false);
    }
    return errorList.isEmpty() ? true : false;
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

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (enableGroupSelect) {
      groupSelect.setVisible(visible);
    }
  }

  public void clear() {
    username.setText("");
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

  public Set<MetadataValue> getUserExtra() {
    return userExtra;
  }

  public void setUserExtra(Set<MetadataValue> userExtra) {
    this.userExtra = userExtra;
  }
}

