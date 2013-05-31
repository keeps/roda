/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.management.user.client.Preferences;
import pt.gov.dgarq.roda.wui.management.user.client.RecoverLoginRequest;
import pt.gov.dgarq.roda.wui.management.user.client.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;
import config.i18n.client.MainMessages;

/**
 * @author Luis Faria
 * 
 */
public class LoginPanel extends SimplePanel {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainMessages messages = (MainMessages) GWT
			.create(MainMessages.class);

	private static MainConstants constants = (MainConstants) GWT
			.create(MainConstants.class);

	private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private final DockPanel layout;

	private final Label message;

	private final Grid guestLayout;

	private final TextBox usernameBox;

	private boolean editingUsername;

	private final PasswordTextBox passwordBox;

	private final Label passwordLabel;

	private final Image passwordButton;

	private boolean editingPassword;

	private final VerticalPanel loggedLayout;

	private final Label logout;

	/**
	 * Quick links under login panel
	 */
	public class LoginLinks extends HorizontalPanel {
		private final Hyperlink userPreferences;

		private final Hyperlink register;

		private final Hyperlink recoverLogin;

		boolean isLogged;

		/**
		 * Create a new login links panel
		 * 
		 * @param isLogged
		 */
		public LoginLinks(boolean isLogged) {
			this.isLogged = isLogged;

			this.userPreferences = new Hyperlink(constants.loginPreferences(),
					Preferences.getInstance().getHistoryPath());
			this.register = new Hyperlink(constants.title_register(), Register
					.getInstance().getHistoryPath());
			this.recoverLogin = new Hyperlink(constants.title_recoverLogin(),
					RecoverLoginRequest.getInstance().getHistoryPath());

			this.addStyleName("wui-loginLinks");
			this.userPreferences.addStyleName("loginLink");
			this.register.addStyleName("loginLink");
			this.recoverLogin.addStyleName("loginLink");

			this.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

			updateLayout();
		}

		protected Widget createSeparator() {
			HTML sep = new HTML("&nbsp;·&nbsp;");
			sep.addStyleName("separator");
			return sep;
		}

		protected void updateLayout() {
			clear();
			if (isLogged) {
				add(userPreferences);
			} else {
				add(register);
				add(createSeparator());
				add(recoverLogin);
			}
		}

		/**
		 * Set login status
		 * 
		 * @param isLogged
		 */
		public void setLogged(boolean isLogged) {
			if (this.isLogged != isLogged) {
				this.isLogged = isLogged;
				updateLayout();
			}
		}

	}

	private final LoginLinks loginLinks;

	/**
	 * Create a new login panel
	 */
	public LoginPanel() {

		this.layout = new DockPanel();
		this.setWidget(layout);

		message = new Label(constants.loginVisitorMessage());
		guestLayout = new Grid(2, 3);

		usernameBox = new TextBox();
		usernameBox.setText("");
		editingUsername = false;
		usernameBox.addStyleName("wui-login-box-user-dummy");

		passwordBox = new PasswordTextBox();
		passwordLabel = new Label(constants.loginPassword());
		passwordButton = commonImageBundle.forwardLight().createImage();
		editingPassword = false;

		guestLayout.setWidget(0, 0, usernameBox);
		guestLayout.setWidget(1, 0, passwordBox);
		guestLayout.setWidget(1, 1, passwordLabel);
		guestLayout.setWidget(1, 2, passwordButton);

		loggedLayout = new VerticalPanel();
		logout = new Label(constants.loginLogout());

		loggedLayout.add(logout);

		layout.add(message, DockPanel.NORTH);

		updateLayout();

		layout.add(guestLayout, DockPanel.CENTER);

		usernameBox.addFocusListener(new FocusListener() {
			public void onFocus(Widget sender) {
				if (!editingUsername) {
					usernameBox.setText("");
				}
				editingUsername = true;
				updateLayout();
			}

			public void onLostFocus(Widget sender) {
				if (usernameBox.getText().length() == 0) {
					editingUsername = false;
				} else {
					editingUsername = true;
				}
				updateLayout();
			}
		});

		passwordBox.addFocusListener(new FocusListener() {

			public void onFocus(Widget sender) {
				editingPassword = true;
				updateLayout();
			}

			public void onLostFocus(Widget sender) {
				editingPassword = false;
				updateLayout();
			}

		});

		usernameBox.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER) {
					DeferredCommand.addCommand(new Command() {

						public void execute() {
							passwordBox.setFocus(true);
						}

					});

				}

			}

		});

		passwordBox.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER && passwordBox.getText().length() > 0) {
					onDoLogin();

				}

			}

		});

		passwordLabel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				DeferredCommand.addCommand(new Command() {

					public void execute() {
						passwordBox.setFocus(true);
					}

				});
			}

		});

		passwordButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				onDoLogin();
			}

		});

		ChangeListener inputsChanges = new ChangeListener() {

			public void onChange(Widget sender) {
				updateLayout();
			}

		};

		usernameBox.addChangeListener(inputsChanges);
		passwordBox.addChangeListener(inputsChanges);

		logout.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				UserLogin.getInstance().logout(
						new AsyncCallback<AuthenticatedUser>() {

							public void onFailure(Throwable caught) {
								logger.fatal("Error logging out", caught);
							}

							public void onSuccess(AuthenticatedUser user) {
								usernameBox.setText("");
								passwordBox.setText("");
							}

						});

			}

		});

		logout.addMouseListener(new MouseListener() {

			public void onMouseDown(Widget sender, int x, int y) {
			}

			public void onMouseEnter(Widget sender) {
				logout.addStyleName("wui-logout-button-hover");
			}

			public void onMouseLeave(Widget sender) {
				logout.removeStyleName("wui-logout-button-hover");
			}

			public void onMouseMove(Widget sender, int x, int y) {

			}

			public void onMouseUp(Widget sender, int x, int y) {

			}

		});

		UserLogin.getInstance().getAuthenticatedUser(
				new AsyncCallback<AuthenticatedUser>() {

					public void onFailure(Throwable caught) {
						logger
								.fatal("Error getting authenticated user",
										caught);
						Window.alert("Authentication service unavailable: "
								+ caught.getMessage());
					}

					public void onSuccess(AuthenticatedUser user) {
						setLoginPanelUser(user);
					}

				});

		UserLogin.getInstance().addLoginStatusListener(
				new LoginStatusListener() {

					public void onLoginStatusChanged(AuthenticatedUser user) {
						setLoginPanelUser(user);
					}

				});

		this.loginLinks = new LoginLinks(false);
		layout.add(loginLinks, DockPanel.SOUTH);

		addStyleName("wui-login");
		layout.addStyleName("wui-login-layout");
		message.addStyleName("wui-login-message");
		guestLayout.addStyleName("wui-login-layout-guest");
		usernameBox.addStyleName("wui-login-box-user");
		passwordBox.addStyleName("wui-login-box-pass");
		passwordLabel.addStyleName("wui-password-label");
		passwordButton.addStyleName("wui-password-button");
		loggedLayout.addStyleName("wui-login-layout-logged");
		logout.addStyleName("wui-logout-button");
	}

	private void onDoLogin() {
		final String username = usernameBox.getText();
		final String password = passwordBox.getText();
		if (username.length() > 0) {
			UserLogin.getInstance().login(username, password,
					new AsyncCallback<AuthenticatedUser>() {

						public void onFailure(Throwable caught) {
							Window.alert(messages.loginFailure(caught
									.getMessage()));
							passwordBox.setText("");
							passwordBox.setFocus(true);
						}

						public void onSuccess(AuthenticatedUser user) {
							// Nothing special to do besides normal
							// onLoginChanged behavior, done in the
							// listener
						}

					});
			editingUsername = false;
			editingPassword = false;
		}
	}

	private void updateLayout() {
		if (usernameBox.getText().length() == 0 && !editingUsername) {
			usernameBox.addStyleName("wui-login-box-user-dummy");
			usernameBox.setText(constants.loginUsername());
		} else /* if (usernameBox.getText().length() > 0 && editingUsername) */{
			usernameBox.removeStyleName("wui-login-box-user-dummy");
		}

		if (passwordBox.getText().length() == 0 && !editingPassword) {
			passwordLabel.setVisible(true);
		} else if (editingPassword) {
			passwordLabel.setVisible(false);
		}
	}

	private void setLoginPanelUser(AuthenticatedUser user) {
		if (user.isGuest()) {
			message.setText(constants.loginVisitorMessage());
			layout.remove(loggedLayout);
			updateLayout();
			layout.add(guestLayout, DockPanel.CENTER);
			loginLinks.setLogged(false);
		} else {
			message.setText(messages.loginSuccessMessage(user.getName()));
			layout.remove(guestLayout);
			layout.add(loggedLayout, DockPanel.CENTER);
			loginLinks.setLogged(true);
		}
	}

}
