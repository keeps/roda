/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class CasForwardDialog extends WUIWindow {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private String serviceURL;

	private static MainConstants constants = (MainConstants) GWT
			.create(MainConstants.class);

	private final Grid layout;
	private final Label warning;

	private final WUIButton forwardToCas;
	private final WUIButton cancel;

	public CasForwardDialog(String serviceURL) {
		super(constants.loginDialogTitle(), 340, 70);
		this.serviceURL = serviceURL;

		layout = new Grid(1, 1);
		warning = new Label(constants.casForwardWarning());

		layout.setWidget(0, 0, warning);

		forwardToCas = new WUIButton(constants.loginDialogLogin(),
				WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);
		cancel = new WUIButton(constants.loginDialogCancel(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		setWidget(layout);
		addToBottom(cancel);
		addToBottom(forwardToCas);

		forwardToCas.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				UserLogin.getInstance().login();
				hide();
			}
		});

		cancel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				hide();
				History.newItem("");
			}
		});

		layout.addStyleName("wui-login-dialog");
		layout.setCellSpacing(0);
		layout.getCellFormatter().addStyleName(0, 0, "wui-login-dialog-cell");
		warning.addStyleName("username-label");
		forwardToCas.addStyleName("login");
		cancel.addStyleName("cancel");

	}

	private native void reload() /*-{
		$wnd.location.reload();
	}-*/;

}
