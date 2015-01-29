/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.ImageLink;
import pt.gov.dgarq.roda.wui.management.user.client.Preferences;
import pt.gov.dgarq.roda.wui.management.user.client.RecoverLoginRequest;
import pt.gov.dgarq.roda.wui.management.user.client.Register;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;
import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class LoginPanel extends SimplePanel {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainConstants constants = (MainConstants) GWT
			.create(MainConstants.class);
	private static final CommonConstants commonConstants = GWT.create(CommonConstants.class);

	private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private final DockPanel layout;

	private final VerticalPanel guestLayout;
	private final ImageLink login;
	private final HorizontalPanel guestLinks;
	private final Hyperlink register;
	private final Hyperlink recover;

	private final VerticalPanel loggedLayout;
	private final ImageLink user;
	private final HorizontalPanel loggedLinks;
	private final Label logout;
	private final Hyperlink preferences;

	public LoginPanel() {
		this.layout = new DockPanel();
		this.setWidget(layout);

		guestLayout = new VerticalPanel();
		login = new ImageLink(commonImageBundle.login().createImage(),
				constants.loginLogin());
		guestLinks = new HorizontalPanel();
		register = new Hyperlink(constants.loginRegister(), Register
				.getInstance().getHistoryPath());
		recover = new Hyperlink(constants.loginRecover(), RecoverLoginRequest
				.getInstance().getHistoryPath());

		loggedLayout = new VerticalPanel();
		user = new ImageLink(commonImageBundle.user().createImage(), "");
		loggedLinks = new HorizontalPanel();
		logout = new Label(constants.loginLogout());
		preferences = new Hyperlink(constants.loginPreferences(), Preferences
				.getInstance().getHistoryPath());

		guestLinks.add(register);
		guestLinks.add(createSeparator());
		guestLinks.add(recover);
		guestLayout.add(login);
		guestLayout.add(guestLinks);

		loggedLinks.add(logout);
		loggedLinks.add(createSeparator());
		loggedLinks.add(preferences);
		loggedLayout.add(user);
		loggedLayout.add(loggedLinks);

		layout.add(guestLayout, DockPanel.CENTER);

		guestLinks.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		loggedLinks.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

		login.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				UserLogin.getInstance().login();
			}
		});

		logout.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				UserLogin.getInstance().logout(
						new AsyncCallback<AuthenticatedUser>() {

							public void onFailure(Throwable caught) {
								logger.fatal("Error logging out", caught);
							}

							public void onSuccess(AuthenticatedUser user) {
							}
						}

				);
			}
		});

		UserLogin.getInstance().getAuthenticatedUser(
				new AsyncCallback<AuthenticatedUser>() {

					public void onFailure(Throwable caught) {
						logger.fatal("Error getting authenticated user", caught);
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

		addStyleName("wui-login");
		layout.addStyleName("wui-login-layout");
		guestLayout.addStyleName("wui-login-layout-guest");
		loggedLayout.addStyleName("wui-login-layout-logged");

		guestLinks.addStyleName("wui-loginLinks");
		loggedLinks.addStyleName("wui-loginLinks");

		register.addStyleName("loginLink");
		recover.addStyleName("loginLink");
		logout.addStyleName("loginLink");
		preferences.addStyleName("loginLink");

		login.addStyleName("wui-login-login");
		user.addStyleName("wui-login-user");
	}

	protected Widget createSeparator() {
		HTML sep = new HTML("&nbsp;·&nbsp;");
		sep.addStyleName("separator");
		return sep;
	}

	private void setLoginPanelUser(AuthenticatedUser user) {
		if (user.isGuest()) {
			layout.remove(loggedLayout);
			layout.add(guestLayout, DockPanel.CENTER);
		} else {
			layout.remove(guestLayout);
			layout.add(loggedLayout, DockPanel.CENTER);
			this.user.setText(user.getName());
		}
	}
}
