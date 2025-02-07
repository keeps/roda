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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.v2.user.User;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.management.Profile;
import org.roda.wui.client.management.Register;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.wcag.AcessibleMenuBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Alexandre Flores
 *
 */
public class UserMenu extends Composite {

  private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, UserMenu> {
  }

  @UiField
  AcessibleMenuBar menu;

  private AcessibleMenuBar userMenu;
  private AcessibleMenuBar languagesMenu;

  private String selectedLanguage;

  /**
   * User menu constructor
   *
   */
  public UserMenu() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void init() {
    userMenu = new AcessibleMenuBar(true);
    userMenu.addStyleName("userMenuColors");
    MenuItem profile = userMenu.addItem(messages.loginProfile(), createCommand(Profile.RESOLVER.getHistoryPath()));
    profile.addStyleName("profile_user_item");
    MenuItem login = userMenu.addItem(messages.loginLogout(), () -> UserLogin.getInstance().logout());
    login.addStyleName("login_user_item");

    languagesMenu = new AcessibleMenuBar(true);
    languagesMenu.addStyleName("userMenuColors");
    setLanguageMenu();

    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.fatal("Error getting Authenticated user", caught);
      }

      @Override
      public void onSuccess(User user) {
        updateVisibles(user);
      }
    });

    UserLogin.getInstance().addLoginStatusListener(this::updateVisibles);
  }

  private ScheduledCommand createCommand(final List<String> path) {
    return () -> HistoryUtils.newHistory(path);
  }

  private ScheduledCommand createLoginCommand() {
    return () -> UserLogin.getInstance().login();
  }

  private void updateVisibles(User user) {
    menu.clearItems();

    // TODO make creating sync (not async)
    // User
    if (user.isGuest()) {
      MenuItem loginItem = customMenuItem("fa fa-user", messages.loginLogin(), "navigationMenu-item-label", null,
        createLoginCommand());
      loginItem.addStyleName("user_menu_item");
      menu.addItem(loginItem);

      MenuItem registerItem = customMenuItem("fa fa-user-plus", messages.loginRegister(),
        "navigationMenu-item-label navigationMenu-register", null, createCommand(Register.RESOLVER.getHistoryPath()));
      registerItem.addStyleName("user_menu_item_register");
      menu.addItem(registerItem);
    } else {
      MenuItem userItem = customMenuItem("fa fa-user", user.getName(), "navigationMenu-item-label", userMenu, null);
      userItem.addStyleName("user_menu_item");
      menu.addItem(userItem);
    }

    MenuItem languageMenuItem = customMenuItem("fa fa-globe", selectedLanguage, "navigationMenu-item-label", languagesMenu, null);
    languageMenuItem.addStyleName("navigationMenu-item-language");
    menu.addItem(languageMenuItem);
  }

  private MenuItem customMenuItem(String icon, String label, String styleNames, MenuBar subMenu,
    ScheduledCommand command) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    String iconHTML = "<i class='" + icon + "'></i>";

    b.append(SafeHtmlUtils.fromSafeConstant(iconHTML));
    if (label != null) {
      b.append(SafeHtmlUtils.fromSafeConstant(label));
    }

    MenuItem menuItem;
    if (subMenu != null) {
      menuItem = new MenuItem(b.toSafeHtml(), subMenu);
    } else if (command != null) {
      menuItem = new MenuItem(b.toSafeHtml(), command);
    } else {
      menuItem = new MenuItem(b.toSafeHtml());
    }
    menuItem.addStyleName("navigationMenu-item");
    menuItem.addStyleName(styleNames);

    return menuItem;
  }

  private void setLanguageMenu() {
    String locale = LocaleInfo.getCurrentLocale().getLocaleName();

    // Getting supported languages and their display name
    Map<String, String> supportedLanguages = new HashMap<>();

    for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
      if (!"default".equals(localeName)) {
        supportedLanguages.put(localeName, LocaleInfo.getLocaleNativeDisplayName(localeName));
      }
    }

    languagesMenu.clearItems();

    for (final Entry<String, String> entry : supportedLanguages.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();

      if (key.equals(locale)) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String iconHTML = "<i class='fa fa-check'></i>";

        b.append(SafeHtmlUtils.fromSafeConstant(value));
        b.append(SafeHtmlUtils.fromSafeConstant(iconHTML));

        MenuItem languageMenuItem = new MenuItem(b.toSafeHtml());
        languageMenuItem.addStyleName("navigationMenu-item-language-selected");
        languageMenuItem.addStyleName("navigationMenu-item-language");
        languagesMenu.addItem(languageMenuItem);
        selectedLanguage = value;
      } else {
        MenuItem languageMenuItem = new MenuItem(SafeHtmlUtils.fromSafeConstant(value),
          () -> JavascriptUtils.changeLocale(key));
        languagesMenu.addItem(languageMenuItem);
        languageMenuItem.addStyleName("navigationMenu-item-language");
      }
    }
  }
}
