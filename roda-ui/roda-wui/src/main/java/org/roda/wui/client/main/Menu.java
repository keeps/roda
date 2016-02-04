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

import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.ingest.preingest.PreIngest;
import org.roda.wui.client.ingest.process.IngestProcess;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.Preferences;
import org.roda.wui.client.management.Register;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.search.BasicSearch;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.LoginStatusListener;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.management.client.Management;

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

import config.i18n.client.LanguageSwitcherPanelConstants;
import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class Menu extends Composite {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);
  private static LanguageSwitcherPanelConstants languagesConstants = (LanguageSwitcherPanelConstants) GWT
    .create(LanguageSwitcherPanelConstants.class);


  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, Menu> {
  }

  @UiField
  MenuBar leftMenu;

  @UiField
  MenuBar rightMenu;

  // private final MenuBar aboutMenu;
  private final MenuItem about;

  // private final MenuBar disseminationMenu;
  private MenuItem dissemination_browse;
  private MenuItem dissemination_searchBasic;
  // private MenuItem dissemination_searchAdvanced;

  private final MenuBar ingestMenu;
  private MenuItem ingest_pre;
  private MenuItem ingest_transfer;
  private MenuItem ingest_list;

  private final MenuBar administrationMenu;
  private MenuItem administration_user;
  // private MenuItem administration_event;
  // private MenuItem administration_statistics;
  private MenuItem administration_log;

  private final MenuBar userMenu;

  private final MenuBar languagesMenu;

  private final MenuBar settingsMenu;
  
  private String selectedLanguage;

  /**
   * Main menu constructor
   * 
   */
  public Menu() {
    initWidget(uiBinder.createAndBindUi(this));

    about = new MenuItem(constants.title_about(), createCommand(Welcome.RESOLVER.getHistoryPath()));

    dissemination_browse = new MenuItem(constants.title_dissemination_browse(),
      createCommand(Browse.RESOLVER.getHistoryPath()));
    dissemination_searchBasic = new MenuItem(constants.title_dissemination_search_basic(),
      createCommand(BasicSearch.RESOLVER.getHistoryPath()));

    ingestMenu = new MenuBar(true);
    ingest_pre = ingestMenu.addItem(constants.title_ingest_pre(), createCommand(PreIngest.RESOLVER.getHistoryPath()));
    ingest_transfer = ingestMenu.addItem(constants.title_ingest_transfer(),
      createCommand(IngestTransfer.RESOLVER.getHistoryPath()));
    ingest_list = ingestMenu.addItem(constants.title_ingest_list(),
      createCommand(IngestProcess.RESOLVER.getHistoryPath()));
    // ingestMenu.addItem(constants.title_ingest_help(),
    // createCommand(Ingest.RESOLVER.getHistoryPath() + ".help"));

    administrationMenu = new MenuBar(true);
    administration_user = administrationMenu.addItem(constants.title_administration_user(),
      createCommand(MemberManagement.RESOLVER.getHistoryPath()));
    // administration_event =
    // administrationMenu.addItem(constants.title_administration_event(),
    // createCommand(EventManagement.getInstance().getHistoryPath()));
    // administration_statistics =
    // administrationMenu.addItem(constants.title_administration_statistics(),
    // createCommand(Statistics.getInstance().getHistoryPath()));
    administration_log = administrationMenu.addItem(constants.title_administration_log(),
      createCommand(UserLog.RESOLVER.getHistoryPath()));
    // administrationMenu.addItem(constants.title_administration_help(),
    // createCommand(Management.RESOLVER + ".help"));

    userMenu = new MenuBar(true);
    userMenu.addItem(constants.loginPreferences(), createCommand(Preferences.RESOLVER.getHistoryPath()));
    userMenu.addItem(constants.loginLogout(), new ScheduledCommand() {

      @Override
      public void execute() {
        UserLogin.getInstance().logout();
      }
    });

    languagesMenu = new MenuBar(true);
    setLanguageMenu();

    settingsMenu = new MenuBar(true);

    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<RodaUser>() {

      public void onFailure(Throwable caught) {
        logger.fatal("Error getting Authenticated user", caught);
      }

      public void onSuccess(RodaUser user) {
        updateVisibles(user);
      }

    });

    UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {

      public void onLoginStatusChanged(RodaUser user) {
        updateVisibles(user);
      }

    });
  }

  private ScheduledCommand createCommand(final List<String> path) {
    return new ScheduledCommand() {

      @Override
      public void execute() {
        Tools.newHistory(path);
      }
    };
  }

  private ScheduledCommand createLoginCommand() {
    return new ScheduledCommand() {

      @Override
      public void execute() {
        UserLogin.getInstance().login();
      }
    };
  }

  private void updateVisibles(RodaUser user) {

    logger.info("Updating menu visibility for user " + user.getName());

    leftMenu.clearItems();
    leftMenuItemCount = 0;
    rightMenu.clearItems();

    // TODO make creating sync (not async)

    // Home
    updateResolverTopItemVisibility(Welcome.RESOLVER, about, 0);

    // Dissemination
    updateResolverTopItemVisibility(Browse.RESOLVER, dissemination_browse, 1);
    updateResolverTopItemVisibility(BasicSearch.RESOLVER, dissemination_searchBasic, 2);

    // Ingest
    updateResolverSubItemVisibility(PreIngest.RESOLVER, ingest_pre);
    updateResolverSubItemVisibility(IngestTransfer.RESOLVER, ingest_transfer);
    updateResolverSubItemVisibility(IngestProcess.RESOLVER, ingest_list);
    updateResolverTopItemVisibility(Ingest.RESOLVER, new MenuItem(constants.title_ingest(), ingestMenu), 3);

    // Administration
    updateResolverSubItemVisibility(MemberManagement.RESOLVER, administration_user);
    // updateResolverSubItemVisibility(EventManagement.RESOLVER,
    // administration_event);
    // updateResolverSubItemVisibility(Statistics.RESOLVER,
    // administration_statistics);
    updateResolverSubItemVisibility(UserLog.RESOLVER, administration_log);
    updateResolverTopItemVisibility(Management.RESOLVER,
      new MenuItem(constants.title_administration(), administrationMenu), 4);

    // User
    if (user.isGuest()) {
      rightMenu
        .addItem(customMenuItem("fa fa-user", constants.loginLogin(), "menu-item-label", null, createLoginCommand()));
      rightMenu.addItem(customMenuItem("fa fa-user-plus", constants.loginRegister(), "menu-item-label", null,
        createCommand(Register.RESOLVER.getHistoryPath())));
    } else {
      rightMenu.addItem(customMenuItem("fa fa-cog", constants.title_settings(), "menu-item-label", settingsMenu, null));
      rightMenu.addItem(customMenuItem("fa fa-user", user.getName(), "menu-item-label", userMenu, null));
    }
    rightMenu
      .addItem(customMenuItem("fa fa-globe", selectedLanguage, "menu-item-label", languagesMenu, null));
  }

  private MenuItem customMenuItem(String icon, String label, String styleNames, MenuBar subMenu,
    ScheduledCommand command) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    String iconHTML = "<i class='" + icon + "'></i>";

    b.append(SafeHtmlUtils.fromSafeConstant(iconHTML));
    if (label != null)
      b.append(SafeHtmlUtils.fromSafeConstant(label));

    MenuItem menuItem = null;
    if (subMenu != null) {
      menuItem = new MenuItem(b.toSafeHtml(), subMenu);
    } else if (command != null) {
      menuItem = new MenuItem(b.toSafeHtml(), command);
    } else {
      menuItem = new MenuItem(b.toSafeHtml());
    }
    menuItem.addStyleName("menu-item");
    menuItem.addStyleName(styleNames);

    return menuItem;
  }

  private void updateResolverTopItemVisibility(final HistoryResolver resolver, final MenuItem item, final int index) {
    resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        logger.error("Error getting role", caught);
      }

      public void onSuccess(Boolean asRole) {
        if (asRole) {
          insertIntoLeftMenu(item, index);
        }
      }
    });
  }

  private void updateResolverSubItemVisibility(final HistoryResolver resolver, final MenuItem item) {
    resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        logger.error("Error getting role", caught);
      }

      public void onSuccess(Boolean asRole) {
        item.setVisible(asRole);
      }
    });
  }

  private int leftMenuItemCount = 0;

  private void insertIntoLeftMenu(MenuItem item, int index) {
    int indexToInsert = index <= leftMenuItemCount ? index : leftMenuItemCount;
    leftMenu.insertItem(item, indexToInsert);
    leftMenuItemCount++;
  }

  private void setLanguageMenu() {
    String locale = LocaleInfo.getCurrentLocale().getLocaleName();
    
    // TODO externalize supported languages
    Map<String, String> supportedLanguages = new HashMap<String, String>();
    supportedLanguages.put("en", languagesConstants.lang_en());
    supportedLanguages.put("pt_PT", languagesConstants.lang_pt());
    supportedLanguages.put("cs_CZ", languagesConstants.lang_cz());

    languagesMenu.clearItems();

    for (final String key : supportedLanguages.keySet()) {
      if (key.equals(locale)) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String iconHTML = "<i class='fa fa-check'></i>";

        b.append(SafeHtmlUtils.fromSafeConstant(supportedLanguages.get(key)));
        b.append(SafeHtmlUtils.fromSafeConstant(iconHTML));

        MenuItem languageMenuItem = new MenuItem(b.toSafeHtml());
        languageMenuItem.addStyleName("menu-item-language-selected");
        languagesMenu.addItem(languageMenuItem);
        selectedLanguage = supportedLanguages.get(key);
      } else {
        MenuItem languageMenuItem = new MenuItem(SafeHtmlUtils.fromSafeConstant(supportedLanguages.get(key)),
          new ScheduledCommand() {

            @Override
            public void execute() {
              JavascriptUtils.changeLocale(key);
            }
          });
        languagesMenu.addItem(languageMenuItem);
      }
    }
  }
}
