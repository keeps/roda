/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.widgets.DatePicker;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class UserDataPanel extends VerticalPanel implements SourcesChangeEvents {

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private class PasswordPanel extends SimplePanel implements
			SourcesChangeEvents {

		private DockPanel editLayout;

		private PasswordTextBox editPassword;

		private PasswordTextBox editPasswordRepeat;

		private Label editPasswordNote;

		private WUIButton editButton;

		private boolean buttonMode;

		private boolean changed;

		private List<ChangeListener> changeListeners;

		public PasswordPanel(boolean editmode) {
			changed = false;
			changeListeners = new Vector<ChangeListener>();
			editLayout = new DockPanel();
			editPassword = new PasswordTextBox();
			editPasswordRepeat = new PasswordTextBox();
			editPasswordNote = new Label(constants.passwordNote());

			editLayout.add(editPassword, DockPanel.CENTER);
			editLayout.add(editPasswordRepeat, DockPanel.EAST);
			editLayout.add(editPasswordNote, DockPanel.SOUTH);

			editButton = new WUIButton(constants.userDataChangePassword(),
					WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

			editButton.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					setWidget(editLayout);
					buttonMode = false;
					onChange();
				}

			});

			if (editmode) {
				setWidget(editButton);
				buttonMode = true;
			} else {
				setWidget(editLayout);
				buttonMode = false;
			}

			KeyboardListener listener = new KeyboardListener() {

				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				}

				public void onKeyPress(Widget sender, char keyCode,
						int modifiers) {
				}

				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					PasswordPanel.this.onChange();
				}
			};
			editPassword.addKeyboardListener(listener);
			editPasswordRepeat.addKeyboardListener(listener);

			this.addStyleName("password");
			editPassword.addStyleName("password-input");
			editPasswordRepeat.addStyleName("password-input-repeat");
			editPasswordNote.addStyleName("password-note");
			editButton.addStyleName("password-button");

		}

		public boolean isChanged() {
			return changed;
		}

		public boolean isValid() {
			boolean valid = true;
			if (buttonMode) {
				valid = true;
			} else if (!editPassword.getText().equals(
					editPasswordRepeat.getText())) {
				valid = false;
			} else if (editPassword.getText().length() < 6) {
				valid = false;
			}

			if (!valid) {
				editPassword.addStyleName("isWrong");
				editPasswordRepeat.addStyleName("isWrong");
			} else {
				editPassword.removeStyleName("isWrong");
				editPasswordRepeat.removeStyleName("isWrong");
			}

			return valid;
		}

		public void addChangeListener(ChangeListener listener) {
			changeListeners.add(listener);
		}

		public void removeChangeListener(ChangeListener listener) {
			changeListeners.remove(listener);
		}

		protected void onChange() {
			changed = true;
			for (ChangeListener listener : changeListeners) {
				listener.onChange(this);
			}
		}

		/**
		 * Get the new password
		 * 
		 * @return the new password or null if none set
		 * 
		 */
		public String getPassword() {
			return changed && isValid() ? editPassword.getText() : null;
		}

		public void clear() {
			editPassword.setText("");
			editPasswordRepeat.setText("");
		}

	}

	private final List<ChangeListener> changeListeners;

	private final TextBox username;

	private final PasswordPanel password;

	private final TextBox fullname;

	private final ListBox businessCategory;

	private final ListBox idType;

	private final TextBox idNumber;

	private final DatePicker idDate;

	private final TextBox idLocality;

	private final SuggestBox nationality;

	private final List<String> nationalityList;

	private final MultiWordSuggestOracle nationalityOracle;

	private final TextBox nif;

	private final TextBox email;

	private final TextArea postalAddress;

	private final TextBox postalCode;

	private final TextBox locality;

	private final SuggestBox country;

	private final List<String> countryList;

	private final MultiWordSuggestOracle countryOracle;

	private final TextBox phoneNumber;

	private final TextBox fax;

	private final Label userdataNote;

	private GroupSelect groupSelect;

	private boolean enableGroupSelect;

	private boolean editmode;

	/**
	 * Create a new user data panel
	 * 
	 * @param editmode
	 *            if user name should be editable
	 * @param enableGroupSelect
	 *            if the list of groups to which the user belong to should be
	 *            editable
	 * 
	 */
	public UserDataPanel(boolean editmode, boolean enableGroupSelect) {
		this(true, editmode, enableGroupSelect);
	}

	/**
	 * 
	 * @param visible
	 * @param editmode
	 * @param enableGroupSelect
	 */
	public UserDataPanel(boolean visible, boolean editmode,
			boolean enableGroupSelect) {
		this.editmode = editmode;
		super.setVisible(visible);
		this.changeListeners = new Vector<ChangeListener>();
		this.enableGroupSelect = enableGroupSelect;

		HorizontalPanel loginAttributes = new HorizontalPanel();
		this.add(loginAttributes);

		username = new TextBox();
		username.setReadOnly(editmode);
		password = new PasswordPanel(editmode);

		loginAttributes.add(concatInPanel(constants.username(), username));
		VerticalPanel passwordpanel = concatInPanel(constants.password(),
				password);
		loginAttributes.add(passwordpanel);

		Grid personalAttributes = new Grid(1, 2);
		VerticalPanel leftColumn = new VerticalPanel();
		VerticalPanel rightColumn = new VerticalPanel();
		this.add(personalAttributes);
		personalAttributes.setWidget(0, 0, leftColumn);
		personalAttributes.setWidget(0, 1, rightColumn);

		personalAttributes.getCellFormatter().setVerticalAlignment(0, 0,
				HasAlignment.ALIGN_TOP);
		personalAttributes.getCellFormatter().setVerticalAlignment(0, 1,
				HasAlignment.ALIGN_TOP);

		fullname = new TextBox();
		businessCategory = new ListBox();
		businessCategory.setVisibleItemCount(1);
		for (String function : constants.getJobFunctions()) {
			businessCategory.addItem(function);
		}

		idType = new ListBox();
		idType.setVisibleItemCount(1);
		for (String type : User.ID_TYPES) {
			String typeText;
			try {
				typeText = constants.getString("id_type_" + type);
			} catch (MissingResourceException e) {
				typeText = type;
			}
			idType.addItem(typeText, type);
		}

		idNumber = new TextBox();
		HorizontalPanel idTypeAndNumber = new HorizontalPanel();
		idTypeAndNumber.add(idType);
		idTypeAndNumber.add(idNumber);

		idDate = new DatePicker();
		idLocality = new TextBox();
		HorizontalPanel idDateAndLocality = new HorizontalPanel();
		idDateAndLocality.add(idDate);
		idDateAndLocality.add(idLocality);

		nif = new TextBox();

		nationalityOracle = new MultiWordSuggestOracle();
		nationalityList = Arrays.asList(constants.nationalityList());
		nationalityOracle.addAll(nationalityList);
		nationality = new SuggestBox(nationalityOracle);

		postalAddress = new TextArea();
		postalCode = new TextBox();
		locality = new TextBox();
		HorizontalPanel postalCodeAndLocality = new HorizontalPanel();
		postalCodeAndLocality.add(postalCode);
		postalCodeAndLocality.add(locality);

		countryOracle = new MultiWordSuggestOracle();
		countryList = Arrays.asList(constants.countryList());
		countryOracle.addAll(countryList);
		country = new SuggestBox(countryOracle);

		email = new TextBox();
		phoneNumber = new TextBox();
		fax = new TextBox();

		leftColumn.add(concatInPanel(constants.fullname(), fullname));
		leftColumn
				.add(concatInPanel(constants.jobFunction(), businessCategory));

		leftColumn.add(concatInPanel(constants.idTypeAndNumber(),
				idTypeAndNumber));
		leftColumn.add(concatInPanel(constants.idDateAndLocality(),
				idDateAndLocality));

		leftColumn.add(concatInPanel(constants.nationality(), nationality));

		leftColumn.add(concatInPanel(constants.nif(), nif));

		rightColumn.add(concatInPanel(constants.email(), email));

		rightColumn.add(concatInPanel(constants.address(), postalAddress));

		rightColumn.add(concatInPanel(constants.postalCodeAndLocality(),
				postalCodeAndLocality));

		rightColumn.add(concatInPanel(constants.country(), country));
		rightColumn.add(concatInPanel(constants.phonenumber(), phoneNumber));
		rightColumn.add(concatInPanel(constants.fax(), fax));

		if (enableGroupSelect) {
			groupSelect = new GroupSelect(visible);
			this.add(groupSelect);

			groupSelect.addChangeListener(new ChangeListener() {

				public void onChange(Widget sender) {
					UserDataPanel.this.onChange(sender);
				}

			});
		}

		userdataNote = new Label(constants.userDataNote());
		this.add(userdataNote);

		this.addStyleName("wui-user-data");

		loginAttributes.addStyleName("wui-user-data-loginAttributes");
		personalAttributes.addStyleName("wui-user-data-personalPanel");

		username.setFocus(true);

		KeyboardListener keyboardListener = new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				UserDataPanel.this.onChange(sender);
			}

		};

		ChangeListener changeListener = new ChangeListener() {

			public void onChange(Widget sender) {
				UserDataPanel.this.onChange(sender);
			}

		};
		
		SuggestionHandler suggestionHandler = new SuggestionHandler() {

			public void onSuggestionSelected(SuggestionEvent event) {
				UserDataPanel.this.onChange(null);				
			}
			
		};

		username.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				if (!(keyCode >= '0' && keyCode <= '9')
						&& !(keyCode >= 'A' && keyCode <= 'Z')
						&& !(keyCode >= 'a' && keyCode <= 'z')
						&& keyCode != '.' && keyCode != '_'
						&& (keyCode != (char) KEY_TAB)
						&& (keyCode != (char) KEY_BACKSPACE)
						&& (keyCode != (char) KEY_DELETE)
						&& (keyCode != (char) KEY_ENTER)
						&& (keyCode != (char) KEY_HOME)
						&& (keyCode != (char) KEY_END)
						&& (keyCode != (char) KEY_LEFT)
						&& (keyCode != (char) KEY_UP)
						&& (keyCode != (char) KEY_RIGHT)
						&& (keyCode != (char) KEY_DOWN)) {
					((TextBox) sender).cancelKey();
				}

			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
			}

		});
		
		

		username.addKeyboardListener(keyboardListener);
		password.addChangeListener(changeListener);

		fullname.addKeyboardListener(keyboardListener);
		businessCategory.addChangeListener(changeListener);
		idType.addChangeListener(changeListener);
		idNumber.addKeyboardListener(keyboardListener);
		idDate.addChangeListener(changeListener);
		idLocality.addKeyboardListener(keyboardListener);
		nationality.addKeyboardListener(keyboardListener);
		nationality.addEventHandler(suggestionHandler);
		nif.addKeyboardListener(keyboardListener);

		email.addKeyboardListener(keyboardListener);
		postalAddress.addKeyboardListener(keyboardListener);
		postalCode.addKeyboardListener(keyboardListener);
		locality.addKeyboardListener(keyboardListener);
		country.addKeyboardListener(keyboardListener);
		country.addEventHandler(suggestionHandler);
		phoneNumber.addKeyboardListener(keyboardListener);
		fax.addKeyboardListener(keyboardListener);

		postalAddress.addStyleName("postalAddress");
		postalCode.addStyleName("postalCode");
		locality.addStyleName("locality");
		idLocality.addStyleName("id-locality");

		userdataNote.addStyleName("wui-user-data-note");

	}

	private VerticalPanel concatInPanel(String title, Widget input) {
		VerticalPanel vp = new VerticalPanel();
		Label label = new Label(title);
		vp.add(label);
		vp.add(input);

		vp.addStyleName("office-input-panel");
		label.addStyleName("office-input-title");
		input.addStyleName("office-input-widget");

		return vp;
	}

	private int setSelected(ListBox listbox, String text) {
		int index = -1;
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
		this.idDate.setDate(user.getIdDocumentDate());
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
	}

	/**
	 * Get user defined by this panel. This panel defines: name, fullname,
	 * title, organization name, postal address, postal code, locality, country,
	 * email, phone number, fax and which groups this user belongs to.
	 * 
	 * @return the user modified by this panel
	 */
	public User getUser() {
		User user = new User();
		user.setName(username.getText());
		user.setFullName(fullname.getText());
		user.setBusinessCategory(businessCategory.getValue(businessCategory
				.getSelectedIndex()));
		user.setIdDocumentType(idType.getValue(idType.getSelectedIndex()));
		user.setIdDocument(idNumber.getText());
		user.setIdDocumentDate(idDate.getDate());
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
			user.setAllGroups(this.getMemberGroups());
		}

		return user;
	}

	/**
	 * Set the groups of which this user is member of
	 * 
	 * @param groups
	 */
	public void setMemberGroups(Set<String> groups) {
		if (enableGroupSelect) {
			groupSelect.setMemberGroups(groups);
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
		return password.getPassword();
	}

	/**
	 * Check if password changed
	 * 
	 * @return true if password changed, false otherwise
	 */
	public boolean isPasswordChanged() {
		return password.isChanged();
	}

	public void addChangeListener(ChangeListener listener) {
		this.changeListeners.add(listener);

	}

	public void removeChangeListener(ChangeListener listener) {
		this.changeListeners.remove(listener);
	}

	protected void onChange(Widget sender) {
		for (ChangeListener listener : changeListeners) {
			listener.onChange(sender);
		}
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

		idDate.setValidStyle(idDate.isValid(), "isWrong");

		if (idLocality.getText().length() == 0) {
			valid = false;
			idLocality.addStyleName("isWrong");
		} else {
			idLocality.removeStyleName("isWrong");
		}

		if (!nationalityList.contains(nationality.getText())) {
			valid = false;
			nationality.addStyleName("isWrong");
		} else {
			nationality.removeStyleName("isWrong");
		}

		if (!email
				.getText()
				.matches(
						"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)")) {
			valid = false;
			email.addStyleName("isWrong");
		} else {
			email.removeStyleName("isWrong");
		}

		if (!countryList.contains(country.getText())) {
			valid = false;
			country.addStyleName("isWrong");
		} else {
			country.removeStyleName("isWrong");
		}

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

}
