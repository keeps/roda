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
package org.roda.wui.client.main;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.BrowseConstants;

/**
 * @author Luis Faria
 * 
 */
public class CasForwardDialog extends WUIWindow {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private String serviceURL;

  private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

  private final Label warning;

  private final Button forwardToCas;
  private final Button cancel;

  public CasForwardDialog(String serviceURL) {
    super(constants.loginDialogTitle(), 340, 70);
    this.serviceURL = serviceURL;

    warning = new Label(constants.casForwardWarning());
    forwardToCas = new Button(constants.loginDialogLogin());
    cancel = new Button(constants.loginDialogCancel());

    forwardToCas.addStyleName("btn");
    forwardToCas.addStyleName("btn-play");
    cancel.addStyleName("btn");
    cancel.addStyleName("btn-times-circle");

    setWidget(warning);
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
        Tools.newHistory(Welcome.RESOLVER);
      }
    });

    addStyleName("wui-login-dialog");
    forwardToCas.addStyleName("login");
    cancel.addStyleName("cancel");

  }

  private native void reload() /*-{
		$wnd.location.reload();
  }-*/;

}
