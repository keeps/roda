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
package org.roda.wui.common.client.widgets;

import org.roda.core.data.v2.user.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class UserInfoPanel {

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  private final User user;
  private final DockPanel layout;
  private final Label username;
  private final VerticalPanel centerLayout;
  private final HTMLTable infoLayout;
  private final DisclosurePanel detailsDisclosure;
  private final HTMLTable detailsLayout;

  /**
   * Create a new user info panel
   * 
   * @param user
   */
  public UserInfoPanel(User user) {
    this.user = user;

    layout = new DockPanel();
    username = new Label(user.getName());
    centerLayout = new VerticalPanel();

    infoLayout = createInfo();
    centerLayout.add(infoLayout);

    detailsDisclosure = new DisclosurePanel(messages.userInfoDetails());
    detailsLayout = createDetails();

    if (detailsLayout != null) {
      detailsDisclosure.setContent(detailsLayout);
      centerLayout.add(detailsDisclosure);
      detailsLayout.addStyleName("info-details-layout");
    }

    layout.add(username, DockPanel.NORTH);
    layout.add(centerLayout, DockPanel.CENTER);

    layout.addStyleName("wui-user-info");
    username.addStyleName("info-username");
    centerLayout.addStyleName("info-layout");
    infoLayout.addStyleName("info-essentialInfo");
    detailsDisclosure.addStyleName("info-details");
  }

  private HTMLTable createInfo() {
    FlexTable tableLayout = new FlexTable();

    if (user.getFullName() != null) {
      Label fullNameLabel = new Label(messages.userInfoFullname() + ":");
      Label fullNameValue = new Label(user.getFullName());
      tableLayout.setWidget(0, 0, fullNameLabel);
      tableLayout.setWidget(0, 1, fullNameValue);
      fullNameLabel.addStyleName("info-label");
      fullNameValue.addStyleName("info-value");
    }

    return tableLayout;
  }

  private HTMLTable createDetails() {
    FlexTable tableLayout = new FlexTable();
    int rows = 0;

    if (user.getEmail() != null) {
      Label emailLabel = new Label(messages.userInfoEmail() + ":");
      Label emailValue = new Label(user.getEmail());
      tableLayout.setWidget(rows, 0, emailLabel);
      tableLayout.setWidget(rows, 1, emailValue);
      emailLabel.addStyleName("info-label");
      emailValue.addStyleName("info-value");
      rows++;
    }

    return rows == 0 ? null : tableLayout;
  }

  /**
   * Get user info panel widget
   * 
   * @return
   */
  public Widget getWidget() {
    return layout;
  }

}
