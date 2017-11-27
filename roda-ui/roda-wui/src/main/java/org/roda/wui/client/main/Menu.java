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
import org.roda.wui.client.browse.BrowseAIP;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.preingest.PreIngest;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.management.Management;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.NotificationRegister;
import org.roda.wui.client.management.Profile;
import org.roda.wui.client.management.Register;
import org.roda.wui.client.management.Statistics;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.planning.FormatRegister;
import org.roda.wui.client.planning.Planning;
import org.roda.wui.client.planning.PreservationAgents;
import org.roda.wui.client.planning.RepresentationInformationNetwork;
import org.roda.wui.client.planning.RiskRegister;
import org.roda.wui.client.process.ActionProcess;
import org.roda.wui.client.process.IngestProcess;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.welcome.Help;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.LoginStatusListener;
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
 * @author Luis Faria
 * 
 */
public class Menu extends Composite {

  private ClientLogger logger = new ClientLogger(getClass().getName());
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, Menu> {
  }

  @UiField
  AcessibleMenuBar leftMenu;

  @UiField
  AcessibleMenuBar rightMenu;

  private final MenuItem about;

  private MenuItem disseminationBrowse;
  private MenuItem disseminationSearchBasic;

  private final AcessibleMenuBar ingestMenu;
  private MenuItem ingestPre;
  private MenuItem ingestTransfer;
  private MenuItem ingestList;
  private MenuItem ingestAppraisal;

  private final AcessibleMenuBar administrationMenu;
  private MenuItem administrationActions;
  private MenuItem administrationInternalActions;
  private MenuItem administrationUser;
  private MenuItem administrationLog;
  private MenuItem administrationNotifications;
  private MenuItem administrationStatistics;
  // private MenuItem administrationPreferences;

  private final AcessibleMenuBar planningMenu;
  // private MenuItem planningMonitoring;
  private MenuItem planningRisk;
  private MenuItem planningRepresentationInformation;
  private MenuItem planningEvent;
  private MenuItem planningAgent;
  private MenuItem planningFormat;

  private final MenuItem help;
  private final AcessibleMenuBar userMenu;
  private final AcessibleMenuBar languagesMenu;

  // private final MenuBar settingsMenu;

  private String selectedLanguage;
  private int leftMenuItemCount = 0;

  /**
   * Main menu constructor
   * 
   */
  public Menu() {
    initWidget(uiBinder.createAndBindUi(this));

    about = customMenuItem("fa fa-home", messages.title("about"), "menu-item-label", null,
      createCommand(Welcome.RESOLVER.getHistoryPath()));

    disseminationBrowse = new MenuItem(messages.title("browse"), createCommand(BrowseAIP.RESOLVER.getHistoryPath()));
    disseminationBrowse.addStyleName("browse_menu_item");
    disseminationSearchBasic = new MenuItem(messages.title("search"), createCommand(Search.RESOLVER.getHistoryPath()));
    disseminationSearchBasic.addStyleName("search_menu_item");

    ingestMenu = new AcessibleMenuBar(true);
    ingestPre = ingestMenu.addItem(messages.title("ingest_preIngest"),
      createCommand(PreIngest.RESOLVER.getHistoryPath()));
    ingestPre.addStyleName("ingest_pre_item");
    ingestTransfer = ingestMenu.addItem(messages.title("ingest_transfer"),
      createCommand(IngestTransfer.RESOLVER.getHistoryPath()));
    ingestTransfer.addStyleName("ingest_transfer_item");
    ingestList = ingestMenu.addItem(messages.title("ingest_list"),
      createCommand(IngestProcess.RESOLVER.getHistoryPath()));
    ingestList.addStyleName("ingest_list_item");
    ingestAppraisal = ingestMenu.addItem(messages.title("ingest_appraisal"),
      createCommand(IngestAppraisal.RESOLVER.getHistoryPath()));
    ingestAppraisal.addStyleName("ingest_appraisal_item");

    administrationMenu = new AcessibleMenuBar(true);
    administrationActions = administrationMenu.addItem(messages.title("administration_actions"),
      createCommand(ActionProcess.RESOLVER.getHistoryPath()));
    administrationActions.addStyleName("administration_actions_item");
    administrationInternalActions = administrationMenu.addItem(messages.title("administration_internal_actions"),
      createCommand(InternalProcess.RESOLVER.getHistoryPath()));
    administrationInternalActions.addStyleName("administration_internal_actions_item");
    administrationUser = administrationMenu.addItem(messages.title("administration_user"),
      createCommand(MemberManagement.RESOLVER.getHistoryPath()));
    administrationUser.addStyleName("administration_user_item");
    administrationLog = administrationMenu.addItem(messages.title("administration_log"),
      createCommand(UserLog.RESOLVER.getHistoryPath()));
    administrationLog.addStyleName("administration_log_item");
    administrationNotifications = administrationMenu.addItem(messages.title("administration_notifications"),
      createCommand(NotificationRegister.RESOLVER.getHistoryPath()));
    administrationNotifications.addStyleName("administration_notifications_item");
    administrationStatistics = administrationMenu.addItem(messages.title("administration_statistics"),
      createCommand(Statistics.RESOLVER.getHistoryPath()));
    administrationStatistics.addStyleName("administration_statistics_item");
    // administration_preferences =
    // administrationMenu.addItem(messages.title("administrationPreferences"),
    // createCommand(Management.RESOLVER.getHistoryPath()));

    planningMenu = new AcessibleMenuBar(true);
    // planningMonitoring =
    // planningMenu.addItem(messages.title("planning_monitoring"),
    // createCommand(Planning.RESOLVER.getHistoryPath()));
    planningRisk = planningMenu.addItem(messages.title("planning_risk"),
      createCommand(RiskRegister.RESOLVER.getHistoryPath()));
    planningRisk.addStyleName("planning_risk_item");
    planningRepresentationInformation = planningMenu.addItem(messages.title("planning_representation_information"),
      createCommand(RepresentationInformationNetwork.RESOLVER.getHistoryPath()));
    planningRepresentationInformation.addStyleName("planning_representation_information_item");
    planningEvent = planningMenu.addItem(messages.title("planning_event"),
      createCommand(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
    planningEvent.addStyleName("planning_event_item");
    planningAgent = planningMenu.addItem(messages.title("planning_agent"),
      createCommand(PreservationAgents.RESOLVER.getHistoryPath()));
    planningAgent.addStyleName("planning_agent_item");
    planningFormat = planningMenu.addItem(messages.title("planning_format"),
      createCommand(FormatRegister.RESOLVER.getHistoryPath()));
    planningFormat.addStyleName("planning_format_item");

    help = new MenuItem(messages.title("help"), createCommand(Help.RESOLVER.getHistoryPath()));
    help.addStyleName("help_menu_item");

    userMenu = new AcessibleMenuBar(true);
    MenuItem profile = userMenu.addItem(messages.loginProfile(), createCommand(Profile.RESOLVER.getHistoryPath()));
    profile.addStyleName("profile_user_item");
    MenuItem login = userMenu.addItem(messages.loginLogout(), new ScheduledCommand() {

      @Override
      public void execute() {
        UserLogin.getInstance().logout();
      }
    });
    login.addStyleName("login_user_item");

    languagesMenu = new AcessibleMenuBar(true);
    setLanguageMenu();

    // settingsMenu = new MenuBar(true);

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

    UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {

      @Override
      public void onLoginStatusChanged(User user) {
        updateVisibles(user);
      }

    });
  }

  private ScheduledCommand createCommand(final List<String> path) {
    return new ScheduledCommand() {

      @Override
      public void execute() {
        HistoryUtils.newHistory(path);
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

  private void updateVisibles(User user) {

    leftMenu.clearItems();
    leftMenuItemCount = 0;
    rightMenu.clearItems();

    // TODO make creating sync (not async)

    // Home
    updateResolverTopItemVisibility(Welcome.RESOLVER, about, 0);

    // Dissemination
    updateResolverTopItemVisibility(BrowseAIP.RESOLVER, disseminationBrowse, 1);
    updateResolverTopItemVisibility(Search.RESOLVER, disseminationSearchBasic, 2);

    // Ingest
    updateResolverSubItemVisibility(PreIngest.RESOLVER, ingestPre);
    updateResolverSubItemVisibility(IngestTransfer.RESOLVER, ingestTransfer);
    updateResolverSubItemVisibility(IngestProcess.RESOLVER, ingestList);
    updateResolverSubItemVisibility(IngestAppraisal.RESOLVER, ingestAppraisal);

    MenuItem ingestItem = new MenuItem(messages.title("ingest"), ingestMenu);
    ingestItem.addStyleName("ingest_menu_item");
    updateResolverTopItemVisibility(Ingest.RESOLVER, ingestItem, 3);

    // Administration
    updateResolverSubItemVisibility(ActionProcess.RESOLVER, administrationActions);
    updateResolverSubItemVisibility(InternalProcess.RESOLVER, administrationInternalActions);
    updateResolverSubItemVisibility(MemberManagement.RESOLVER, administrationUser);
    updateResolverSubItemVisibility(UserLog.RESOLVER, administrationLog);
    updateResolverSubItemVisibility(NotificationRegister.RESOLVER, administrationNotifications);
    updateResolverSubItemVisibility(Statistics.RESOLVER, administrationStatistics);
    // updateResolverSubItemVisibility(Management.RESOLVER,
    // administrationPreferences);
    MenuItem adminItem = new MenuItem(messages.title("administration"), administrationMenu);
    adminItem.addStyleName("administration_menu_item");
    updateResolverTopItemVisibility(Management.RESOLVER, adminItem, 4);

    // Planning
    // updateResolverSubItemVisibility(Planning.RESOLVER, planningMonitoring);
    updateResolverSubItemVisibility(RiskRegister.RESOLVER, planningRisk);
    updateResolverSubItemVisibility(FormatRegister.RESOLVER, planningFormat);
    updateResolverSubItemVisibility(RepresentationInformationNetwork.RESOLVER, planningRepresentationInformation);
    updateResolverSubItemVisibility(PreservationEvents.PLANNING_RESOLVER, planningEvent);
    updateResolverSubItemVisibility(PreservationAgents.RESOLVER, planningAgent);
    MenuItem planningItem = new MenuItem(messages.title("planning"), planningMenu);
    planningItem.addStyleName("planning_menu_item");
    updateResolverTopItemVisibility(Planning.RESOLVER, planningItem, 5);

    // Help
    updateResolverTopItemVisibility(Help.RESOLVER, help, 6);

    // User
    if (user.isGuest()) {
      MenuItem loginItem = customMenuItem("fa fa-user", messages.loginLogin(), "menu-item-label", null,
        createLoginCommand());
      loginItem.addStyleName("user_menu_item");
      rightMenu.addItem(loginItem);

      MenuItem registerItem = customMenuItem("fa fa-user-plus", messages.loginRegister(),
        "menu-item-label menu-register", null, createCommand(Register.RESOLVER.getHistoryPath()));
      registerItem.addStyleName("user_menu_item_register");
      rightMenu.addItem(registerItem);
    } else {
      // rightMenu.addItem(customMenuItem("fa fa-cog",
      // messages.title("settings"), "menu-item-label", settingsMenu, null));
      MenuItem userItem = customMenuItem("fa fa-user", user.getName(), "menu-item-label", userMenu, null);
      userItem.addStyleName("user_menu_item");
      rightMenu.addItem(userItem);
    }

    MenuItem languageMenuItem = customMenuItem("fa fa-globe", selectedLanguage, "menu-item-label", languagesMenu, null);
    languageMenuItem.addStyleName("menu-item-language");
    rightMenu.addItem(languageMenuItem);
  }

  private MenuItem customMenuItem(String icon, String label, String styleNames, MenuBar subMenu,
    ScheduledCommand command) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    String iconHTML = "<i class='" + icon + "'></i>";

    b.append(SafeHtmlUtils.fromSafeConstant(iconHTML));
    if (label != null)
      b.append(SafeHtmlUtils.fromSafeConstant(label));

    MenuItem menuItem;
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

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error getting role", caught);
      }

      @Override
      public void onSuccess(Boolean asRole) {
        if (asRole) {
          insertIntoLeftMenu(item, index);
        }
      }
    });
  }

  private void updateResolverSubItemVisibility(final HistoryResolver resolver, final MenuItem item) {
    resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error getting role", caught);
      }

      @Override
      public void onSuccess(Boolean asRole) {
        item.setVisible(asRole);
      }
    });
  }

  private void insertIntoLeftMenu(MenuItem item, int index) {
    int indexToInsert = index <= leftMenuItemCount ? index : leftMenuItemCount;
    leftMenu.insertItem(item, indexToInsert);
    leftMenuItemCount++;
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
        languageMenuItem.addStyleName("menu-item-language-selected");
        languageMenuItem.addStyleName("menu-item-language");
        languagesMenu.addItem(languageMenuItem);
        selectedLanguage = value;
      } else {
        MenuItem languageMenuItem = new MenuItem(SafeHtmlUtils.fromSafeConstant(value), new ScheduledCommand() {

          @Override
          public void execute() {
            JavascriptUtils.changeLocale(key);
          }
        });
        languagesMenu.addItem(languageMenuItem);
        languageMenuItem.addStyleName("menu-item-language");
      }
    }
  }
}
