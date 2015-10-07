/**
 * 
 */
package org.roda.wui.main.client;

import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.UserLogin;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.WUIWindow;
import org.roda.wui.home.client.Home;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class CasForwardDialog extends WUIWindow {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private String serviceURL;

  private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

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
        Tools.newHistory(Home.RESOLVER);
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
