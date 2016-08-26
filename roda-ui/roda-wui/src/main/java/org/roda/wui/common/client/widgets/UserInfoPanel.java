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
    FlexTable layout = new FlexTable();
    int rows = 0;

    if (user.getFullName() != null) {
      Label fullNameLabel = new Label(messages.userInfoFullname() + ":");
      Label fullNameValue = new Label(user.getFullName());
      layout.setWidget(rows, 0, fullNameLabel);
      layout.setWidget(rows, 1, fullNameValue);
      fullNameLabel.addStyleName("info-label");
      fullNameValue.addStyleName("info-value");
      rows++;
    }

    // if (user.getBusinessCategory() != null) {
    // Label titleLabel = new Label(messages.userInfoBusinessCategory()+":");
    // Label titleValue = new Label(user.getBusinessCategory());
    // layout.setWidget(rows, 0, titleLabel);
    // layout.setWidget(rows, 1, titleValue);
    // titleLabel.addStyleName("info-label");
    // titleValue.addStyleName("info-value");
    // rows++;
    // }

    return layout;
  }

  private HTMLTable createDetails() {
    FlexTable layout = new FlexTable();
    int rows = 0;

    if (user.getEmail() != null) {
      Label emailLabel = new Label(messages.userInfoEmail() + ":");
      Label emailValue = new Label(user.getEmail());
      layout.setWidget(rows, 0, emailLabel);
      layout.setWidget(rows, 1, emailValue);
      emailLabel.addStyleName("info-label");
      emailValue.addStyleName("info-value");
      rows++;
    }

    // if (user.getTelephoneNumber() != null) {
    // Label phoneNumberLabel = new Label(messages.userInfoTelephoneNumber() +
    // ":");
    // Label phoneNumberValue = new Label(user.getTelephoneNumber());
    // layout.setWidget(rows, 0, phoneNumberLabel);
    // layout.setWidget(rows, 1, phoneNumberValue);
    // phoneNumberLabel.addStyleName("info-label");
    // phoneNumberValue.addStyleName("info-value");
    // rows++;
    // }
    //
    // if (user.getFax() != null) {
    // Label faxLabel = new Label(messages.userInfoFax() + ":");
    // Label faxValue = new Label(user.getFax());
    // layout.setWidget(rows, 0, faxLabel);
    // layout.setWidget(rows, 1, faxValue);
    // faxLabel.addStyleName("info-label");
    // faxValue.addStyleName("info-value");
    // rows++;
    // }
    //
    // if (user.getPostalAddress() != null) {
    // Label addressLabel = new Label(messages.userInfoPostalAddress() + ":");
    // Label addressValue = new Label(user.getPostalAddress());
    // layout.setWidget(rows, 0, addressLabel);
    // layout.setWidget(rows, 1, addressValue);
    // addressLabel.addStyleName("info-label");
    // addressValue.addStyleName("info-value");
    // rows++;
    // }
    //
    // if (user.getPostalCode() != null) {
    // Label postalCodeLabel = new Label(messages.userInfoPostalCode() + ":");
    // Label postalCodeValue = new Label(user.getPostalCode());
    // layout.setWidget(rows, 0, postalCodeLabel);
    // layout.setWidget(rows, 1, postalCodeValue);
    // postalCodeLabel.addStyleName("info-label");
    // postalCodeValue.addStyleName("info-value");
    // rows++;
    // }
    //
    // if (user.getLocalityName() != null) {
    // Label localityLabel = new Label(messages.userInfoLocality() + ":");
    // Label localityValue = new Label(user.getLocalityName());
    // layout.setWidget(rows, 0, localityLabel);
    // layout.setWidget(rows, 1, localityValue);
    // localityLabel.addStyleName("info-label");
    // localityValue.addStyleName("info-value");
    // rows++;
    // }
    //
    // if (user.getCountryName() != null) {
    // Label countryLabel = new Label(messages.userInfoCountry() + ":");
    // Label countryValue = new Label(user.getCountryName());
    // layout.setWidget(rows, 0, countryLabel);
    // layout.setWidget(rows, 1, countryValue);
    // countryLabel.addStyleName("info-label");
    // countryValue.addStyleName("info-value");
    // rows++;
    // }

    return rows == 0 ? null : layout;
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
