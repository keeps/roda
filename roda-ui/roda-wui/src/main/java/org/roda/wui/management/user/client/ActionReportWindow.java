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
package org.roda.wui.management.user.client;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class ActionReportWindow extends WUIWindow {

  private static UserManagementConstants constants = (UserManagementConstants) GWT
    .create(UserManagementConstants.class);

  private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

  private final WUIButton close;

  private final UserLog actionReportPanel;

  /**
   * Create new user action report window
   * 
   * @param user
   */
  public ActionReportWindow(User user) {
    super(messages.actionResportTitle(user.getName()), 850, 500);

    // FIXME set user
    actionReportPanel = new UserLog();

    this.addTab(actionReportPanel, constants.actionReportLogTabTitle());
    this.selectTab(0);

    close = new WUIButton(constants.actionReportClose(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);
    close.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });

    this.addToBottom(close);

  }
}
