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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class UserDataPanel extends Composite implements HasValueChangeHandlers<User> {

  interface MyUiBinder extends UiBinder<Widget, UserDataPanel> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  TextBox username;

  @UiField(provided = true)
  PasswordPanel password;

  @UiField
  TextBox fullname;

  @UiField
  ListBox businessCategory;

  @UiField
  ListBox idType;

  @UiField
  TextBox idNumber;

  @UiField
  DateBox idDate;

  @UiField
  TextBox idLocality;

  @UiField(provided = true)
  SuggestBox nationality;

  @UiField
  TextBox nif;

  @UiField
  TextBox email;

  @UiField
  TextArea postalAddress;

  @UiField
  TextBox postalCode;

  @UiField
  TextBox locality;

  @UiField(provided = true)
  SuggestBox country;

  @UiField
  TextBox phoneNumber;

  @UiField
  TextBox fax;

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

  private boolean changed = false;
  private boolean checked = false;

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

    MultiWordSuggestOracle nationalityOracle = new MultiWordSuggestOracle();
    List<String> nationalityList = new ArrayList<String>();

    int i = 0;
    String message = "";
    do {
      message = messages.nationalityList(i++);
      if (!message.isEmpty()) {
        nationalityList.add(message);
      }
    } while (!message.isEmpty());

    nationalityOracle.addAll(nationalityList);
    nationality = new SuggestBox(nationalityOracle);

    MultiWordSuggestOracle countryOracle = new MultiWordSuggestOracle();
    List<String> countryList = new ArrayList<String>();

    i = 0;
    do {
      message = messages.countryList(i++);
      if (!message.isEmpty()) {
        countryList.add(message);
      }
    } while (!message.isEmpty());

    countryOracle.addAll(countryList);
    country = new SuggestBox(countryOracle);

    groupSelect = new GroupSelect(enableGroupSelect);

    initWidget(uiBinder.createAndBindUi(this));

    this.editmode = editmode;
    super.setVisible(visible);
    this.enableGroupSelect = enableGroupSelect;

    groupSelectPanel.setVisible(enableGroupSelect);
    permissionsSelectPanel.setVisible(enablePermissions);

    businessCategory.setVisibleItemCount(1);

    i = 0;
    do {
      message = messages.getJobFunctions(i++);
      if (!message.isEmpty()) {
        businessCategory.addItem(message);
      }
    } while (!message.isEmpty());

    idType.setVisibleItemCount(1);
    for (String type : User.ID_TYPES) {
      String typeText = messages.id_type(type);

      if (typeText.isEmpty()) {
        typeText = type;
      }

      idType.addItem(typeText, type);
    }

    DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
    idDate.setFormat(dateFormat);
    idDate.getDatePicker().setYearArrowsVisible(true);
    idDate.setFireNullValues(true);

    // username.setFocus(true);

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

    SelectionHandler<Suggestion> selectionHandler = new SelectionHandler<Suggestion>() {

      @Override
      public void onSelection(SelectionEvent<Suggestion> event) {
        onChange();
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
    businessCategory.addChangeHandler(changeHandler);
    idType.addChangeHandler(changeHandler);
    idNumber.addChangeHandler(changeHandler);
    idNumber.addKeyUpHandler(keyUpHandler);
    idDate.addValueChangeHandler(new ValueChangeHandler<Date>() {

      @Override
      public void onValueChange(ValueChangeEvent<Date> event) {
        onChange();
      }
    });
    idLocality.addChangeHandler(changeHandler);
    idLocality.addKeyUpHandler(keyUpHandler);
    nationality.addValueChangeHandler(valueChangedHandler);
    nationality.addSelectionHandler(selectionHandler);
    nationality.addKeyUpHandler(keyUpHandler);
    nif.addChangeHandler(changeHandler);

    email.addChangeHandler(changeHandler);
    email.addKeyUpHandler(keyUpHandler);
    postalAddress.addChangeHandler(changeHandler);
    postalCode.addChangeHandler(changeHandler);
    locality.addChangeHandler(changeHandler);
    country.addValueChangeHandler(valueChangedHandler);
    country.addSelectionHandler(selectionHandler);
    country.addKeyUpHandler(keyUpHandler);
    phoneNumber.addChangeHandler(changeHandler);
    fax.addChangeHandler(changeHandler);

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
  }

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
    setSelected(businessCategory, user.getBusinessCategory());
    setSelected(idType, user.getIdDocumentType());
    this.idNumber.setText(user.getIdDocument());
    this.idDate.setValue(user.getIdDocumentDate());
    this.idLocality.setText(user.getIdDocumentLocation());
    this.nationality.setText(user.getBirthCountry());
    this.nif.setText(user.getFinanceIdentificationNumber());

    this.email.setText(user.getEmail());
    this.postalAddress.setText(user.getPostalAddress());
    this.postalCode.setText(user.getPostalCode());
    this.locality.setText(user.getLocalityName());
    this.country.setText(user.getCountryName());
    this.phoneNumber.setText(user.getTelephoneNumber());
    this.fax.setText(user.getFax());

    this.setMemberGroups(user.getAllGroups());
    this.setPermissions(user.getDirectRoles(), user.getAllRoles());
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
        Tools.newHistory(MemberManagement.RESOLVER);
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
    // user.setBusinessCategory(businessCategory.getValue(businessCategory.getSelectedIndex()));
    // user.setIdDocumentType(idType.getValue(idType.getSelectedIndex()));
    user.setIdDocument(idNumber.getText());
    // user.setIdDocumentDate(idDate.getValue());
    user.setIdDocumentLocation(idLocality.getText());
    user.setBirthCountry(nationality.getText());
    user.setFinanceIdentificationNumber(nif.getText());

    user.setEmail(email.getText());
    user.setPostalAddress(postalAddress.getText());
    user.setPostalCode(postalCode.getText());
    user.setLocalityName(locality.getText());
    user.setCountryName(country.getText());
    user.setTelephoneNumber(phoneNumber.getText());
    user.setFax(fax.getText());

    if (enableGroupSelect) {
      user.setDirectGroups(this.getMemberGroups());
    }

    user.setDirectRoles(permissionsPanel.getDirectRoles());

    return user;
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
          Tools.newHistory(MemberManagement.RESOLVER);
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
    boolean valid = true;

    if (username.getText().length() == 0) {
      valid = false;
      username.addStyleName("isWrong");
    } else {
      username.removeStyleName("isWrong");
    }

    valid &= password.isValid();

    if (fullname.getText().length() == 0) {
      valid = false;
      fullname.addStyleName("isWrong");
    } else {
      fullname.removeStyleName("isWrong");
    }

    if (idNumber.getText().length() == 0) {
      valid = false;
      idNumber.addStyleName("isWrong");
    } else {
      idNumber.removeStyleName("isWrong");
    }

    // idDate.setValidStyle(idDate.isValid(), "isWrong");

    if (idLocality.getText().length() == 0) {
      valid = false;
      idLocality.addStyleName("isWrong");
    } else {
      idLocality.removeStyleName("isWrong");
    }

    if (nationality.getText().length() == 0) {
      valid = false;
      nationality.addStyleName("isWrong");
    } else {
      nationality.removeStyleName("isWrong");
    }

    if (!email.getText()
      .matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)")) {
      valid = false;
      email.addStyleName("isWrong");
    } else {
      email.removeStyleName("isWrong");
    }

    if (country.getText().length() == 0) {
      valid = false;
      country.addStyleName("isWrong");
    } else {
      country.removeStyleName("isWrong");
    }

    checked = true;

    return valid;
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
    businessCategory.setSelectedIndex(0);
    fullname.setText("");
    idNumber.setText("");
    postalAddress.setText("");
    postalCode.setText("");
    locality.setText("");
    country.setText("");
    email.setText("");
    fax.setText("");
    phoneNumber.setText("");
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
}
