/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class LoginDialog extends WUIWindow {

	private static LoginDialog instance = null;

	/**
	 * Get singleton instance
	 * 
	 * @return the singleton instance
	 */
	public static LoginDialog getInstance() {
		if (instance == null) {
			instance = new LoginDialog();
		}
		return instance;
	}

	private static MainConstants constants = (MainConstants) GWT
			.create(MainConstants.class);

	private final Grid layout;
	private final Label userLabel;
	private final Label passLabel;
	private final TextBox userBox;
	private final PasswordTextBox passBox;

	private final WUIButton login;
	private final WUIButton cancel;

	private LoginDialog() {
		super(constants.loginDialogTitle(), 340, 70);

		layout = new Grid(2, 2);
		userLabel = new Label(constants.loginDialogUsername());
		passLabel = new Label(constants.loginDialogPassword());

		userBox = new TextBox();
		passBox = new PasswordTextBox();

		layout.setWidget(0, 0, userLabel);
		layout.setWidget(1, 0, passLabel);

		layout.setWidget(0, 1, userBox);
		layout.setWidget(1, 1, passBox);

		login = new WUIButton(constants.loginDialogLogin(),
				WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);
		cancel = new WUIButton(constants.loginDialogCancel(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		setWidget(layout);
		addToBottom(cancel);
		addToBottom(login);

		userBox.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				// do nothing

			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				// do nothing

			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER) {
					DeferredCommand.addCommand(new Command() {

						public void execute() {
							passBox.setFocus(true);
						}

					});

				}

			}

		});

		passBox.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				// do nothing
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				// do nothing

			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if (keyCode == KEY_ENTER && passBox.getText().length() > 0) {
					login();
				}

			}

		});

		login.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				login();
			}

		});

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
				UserLogin.getInstance().checkForLoginReset();
				History.newItem("");
			}

		});

		layout.addStyleName("wui-login-dialog");
		layout.setCellSpacing(0);
		layout.getCellFormatter().addStyleName(0, 0, "wui-login-dialog-cell");
		layout.getCellFormatter().addStyleName(0, 1, "wui-login-dialog-cell");
		layout.getCellFormatter().addStyleName(1, 0, "wui-login-dialog-cell");
		layout.getCellFormatter().addStyleName(1, 1, "wui-login-dialog-cell");
		userLabel.addStyleName("username-label");
		userBox.addStyleName("username-box");
		passLabel.addStyleName("password-label");
		passBox.addStyleName("password-box");
		login.addStyleName("login");
		cancel.addStyleName("cancel");

	}

	/**
	 * Try to login
	 */
	public void login() {
		String username = userBox.getText();
		String password = passBox.getText();
		if (username.length() > 0) {
			UserLogin.getInstance().login(username, password,
					new AsyncCallback<AuthenticatedUser>() {

						public void onFailure(Throwable caught) {
							Window.alert(caught.getMessage());
						}

						public void onSuccess(AuthenticatedUser user) {
							reload();
						}

					});
		}
	}

	private native void reload() /*-{
	    $wnd.location.reload();
	   }-*/;

}
