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

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.disposal.DisposalDestroyedRecords;
import org.roda.wui.client.disposal.confirmations.CreateDisposalConfirmation;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.preingest.PreIngest;
import org.roda.wui.client.ingest.transfer.IngestTransfer;
import org.roda.wui.client.management.Management;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.NotificationRegister;
import org.roda.wui.client.management.Statistics;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.management.distributed.DistributedInstancesManagement;
import org.roda.wui.client.management.distributed.LocalInstanceManagement;
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
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import org.roda.wui.common.client.widgets.wcag.AcessibleMenuBar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
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
public class Header extends Composite {

  private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, Header> {
  }

  @UiField
  AccessibleFocusPanel homeLinkArea;

  @UiField
  FlowPanel bannerLogo;

  @UiField
  AcessibleMenuBar navigationMenu;

  private MenuItem about;

  private MenuItem disseminationBrowse;
  private MenuItem disseminationSearchBasic;

  private AcessibleMenuBar ingestMenu;
  private MenuItem ingestPre;
  private MenuItem ingestTransfer;
  private MenuItem ingestList;
  private MenuItem ingestAppraisal;

  private AcessibleMenuBar administrationMenu;
  private MenuItem administrationActions;
  private MenuItem administrationInternalActions;
  private MenuItem administrationUser;
  private MenuItem administrationLog;
  private MenuItem administrationNotifications;
  private MenuItem administrationStatistics;
  private MenuItem administrationDistributedInstances;
  private MenuItem administrationMonitoring;
  private MenuItem administrationMarketplace;

  private AcessibleMenuBar disposalMenu;
  private MenuItem disposalPolicy;
  private MenuItem disposalConfirmation;
  private MenuItem overdueActions;
  private MenuItem disposalDestroyedRecords;

  private AcessibleMenuBar planningMenu;
  private MenuItem planningRepresentationInformation;
  private MenuItem planningRisk;
  private MenuItem planningEvent;
  private MenuItem planningAgent;

  private MenuItem help;

  private int navigationMenuItemCount = 0;

  /**
   * Main navigationMenu constructor
   *
   */
  public Header() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void init() {
    bannerLogo.add(new HTMLWidgetWrapper("Banner.html"));
    homeLinkArea.addClickHandler(event -> HistoryUtils.newHistory(Welcome.RESOLVER));
    homeLinkArea.setTitle(messages.homeTitle());

    about = customMenuItem("fa fa-home", messages.title("about"), "navigationMenu-item-label", null,
      createCommand(Welcome.RESOLVER.getHistoryPath()));

    disseminationBrowse = new MenuItem(messages.title("browse"), createCommand(BrowseTop.RESOLVER.getHistoryPath()));
    disseminationBrowse.addStyleName("browse_menu_item");
    disseminationSearchBasic = new MenuItem(messages.title("search"), createCommand(Search.RESOLVER.getHistoryPath()));
    disseminationSearchBasic.addStyleName("search_menu_item");

    ingestMenu = new AcessibleMenuBar(true);
    ingestMenu.addStyleName("bannerHeaderColors");
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
    administrationMenu.addStyleName("bannerHeaderColors");
    administrationActions = administrationMenu.addItem(messages.title("administration_actions"),
      createCommand(ActionProcess.RESOLVER.getHistoryPath()));
    administrationActions.addStyleName("administration_actions_item");
    administrationInternalActions = administrationMenu.addItem(messages.title("administration_internal_actions"),
      createCommand(InternalProcess.RESOLVER.getHistoryPath()));
    administrationInternalActions.addStyleName("administration_internal_actions_item");
    administrationLog = administrationMenu.addItem(messages.title("administration_log"),
      createCommand(UserLog.RESOLVER.getHistoryPath()));
    administrationLog.addStyleName("administration_log_item");
    administrationNotifications = administrationMenu.addItem(messages.title("administration_notifications"),
      createCommand(NotificationRegister.RESOLVER.getHistoryPath()));
    administrationNotifications.addStyleName("administration_notifications_item");

    boolean reportingActive = ConfigurationManager.getBoolean(false,
        RodaConstants.UI_SERVICE_REPORTING_ACTIVE);

    if (reportingActive) {
      administrationStatistics = administrationMenu.addItem(messages.title("administration_statistics"),
          createURLCommand(ConfigurationManager.getString(RodaConstants.UI_SERVICE_REPORTING_URL)));
    } else {
      administrationStatistics = administrationMenu.addItem(messages.title("administration_statistics"),
          createCommand(Statistics.RESOLVER.getHistoryPath()));
    }

    administrationStatistics.addStyleName("administration_statistics_item");
    String distributedMode = ConfigurationManager.getStringWithDefault(
      RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name(), RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY);
    if (distributedMode.equals(RodaConstants.DistributedModeType.CENTRAL.name())) {
      administrationDistributedInstances = administrationMenu.addItem(
        messages.title("administration_distributed_instances"),
        createCommand(DistributedInstancesManagement.RESOLVER.getHistoryPath()));
      administrationDistributedInstances.addStyleName("administration_statistics_item");
    } else if (distributedMode.equals(RodaConstants.DistributedModeType.LOCAL.name())) {
      administrationDistributedInstances = administrationMenu.addItem(
        messages.title("administration_local_instance_configuration"),
        createCommand(LocalInstanceManagement.RESOLVER.getHistoryPath()));
      administrationDistributedInstances.addStyleName("administration_statistics_item");
    }

    String monitoringLink = ConfigurationManager.getStringWithDefault(RodaConstants.UI_SERVICE_MONITORING_DEFAULT_URL,
      RodaConstants.UI_SERVICE_MONITORING_URL);
    administrationMonitoring = administrationMenu.addItem(messages.title("administration_monitoring"),
      createURLCommand(monitoringLink));
    administrationMonitoring.addStyleName("administration_monitoring_item");
    administrationUser = administrationMenu.addItem(messages.title("administration_user"),
      createCommand(MemberManagement.RESOLVER.getHistoryPath()));
    administrationUser.addStyleName("administration_user_item");
    administrationMarketplace = administrationMenu.addItem(messages.title("administration_market_place"),
      createURLCommand(ConfigurationManager.getStringWithDefault(RodaConstants.UI_SERVICE_MARKETPLACE_DEFAULT_URL,
        RodaConstants.UI_SERVICE_MARKETPLACE_URL)));
    administrationMarketplace.addStyleName("administration_marketplace_item");

    disposalMenu = new AcessibleMenuBar(true);
    disposalMenu.addStyleName("bannerHeaderColors");
    disposalPolicy = disposalMenu.addItem(messages.title("disposal_policies"),
      createCommand(DisposalPolicy.RESOLVER.getHistoryPath()));
    disposalPolicy.addStyleName("disposal_policy_item");
    disposalConfirmation = disposalMenu.addItem(messages.title("disposal_confirmations"),
      createCommand(DisposalConfirmations.RESOLVER.getHistoryPath()));
    disposalConfirmation.addStyleName("disposal_confirmation_item");
    overdueActions = disposalMenu.addItem(messages.title("overdue_actions"),
      createCommand(CreateDisposalConfirmation.RESOLVER.getHistoryPath()));
    overdueActions.addStyleName("overdue_actions_item");
    disposalDestroyedRecords = disposalMenu.addItem(messages.title("disposal_destroyed_records"),
      createCommand(DisposalDestroyedRecords.RESOLVER.getHistoryPath()));
    disposalDestroyedRecords.addStyleName("disposal_destroyed_records_item");

    planningMenu = new AcessibleMenuBar(true);
    planningMenu.addStyleName("bannerHeaderColors");
    planningRepresentationInformation = planningMenu.addItem(messages.title("planning_representation_information"),
      createCommand(RepresentationInformationNetwork.RESOLVER.getHistoryPath()));
    planningRepresentationInformation.addStyleName("planning_representation_information_item");
    planningRisk = planningMenu.addItem(messages.title("planning_risk"),
      createCommand(RiskRegister.RESOLVER.getHistoryPath()));
    planningRisk.addStyleName("planning_risk_item");
    planningEvent = planningMenu.addItem(messages.title("planning_event"),
      createCommand(PreservationEvents.PLANNING_RESOLVER.getHistoryPath()));
    planningEvent.addStyleName("planning_event_item");
    planningAgent = planningMenu.addItem(messages.title("planning_agent"),
      createCommand(PreservationAgents.RESOLVER.getHistoryPath()));
    planningAgent.addStyleName("planning_agent_item");

    help = new MenuItem(messages.title("help"), createCommand(Help.RESOLVER.getHistoryPath()));
    help.addStyleName("help_menu_item");

    UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<User>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.fatal("Error getting Authenticated user", caught);
      }

      @Override
      public void onSuccess(User user) {
        updateVisibleItems(user);
      }
    });

    UserLogin.getInstance().addLoginStatusListener(this::updateVisibleItems);
  }

  private ScheduledCommand createCommand(final List<String> path) {
    return () -> HistoryUtils.newHistory(path);
  }

  private ScheduledCommand createURLCommand(String url) {
    return () -> Window.open(url, "_blank", "");
  }

  private void updateVisibleItems(User user) {
    navigationMenu.clearItems();
    navigationMenuItemCount = 0;

    // TODO make creating sync (not async)

    // Home
    updateResolverTopItemVisibility(Welcome.RESOLVER, about, 0);

    // Dissemination
    updateResolverTopItemVisibility(BrowseTop.RESOLVER, disseminationBrowse, 1);
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
    MenuItem adminItem = new MenuItem(messages.title("administration"), administrationMenu);
    adminItem.addStyleName("administration_menu_item");
    updateResolverTopItemVisibility(Management.RESOLVER, adminItem, 4);

    // Disposal
    updateResolverSubItemVisibility(DisposalPolicy.RESOLVER, disposalPolicy);
    updateResolverSubItemVisibility(DisposalConfirmations.RESOLVER, disposalConfirmation);
    updateResolverSubItemVisibility(DisposalDestroyedRecords.RESOLVER, disposalDestroyedRecords);
    updateResolverSubItemVisibility(DisposalConfirmations.RESOLVER, overdueActions);
    MenuItem disposalItem = new MenuItem(messages.title("disposal"), disposalMenu);
    disposalItem.addStyleName("disposal_menu_item");
    updateResolverTopItemVisibility(Disposal.RESOLVER, disposalItem, 5);

    // Planning
    updateResolverSubItemVisibility(RiskRegister.RESOLVER, planningRisk);
    updateResolverSubItemVisibility(RepresentationInformationNetwork.RESOLVER, planningRepresentationInformation);
    updateResolverSubItemVisibility(PreservationEvents.PLANNING_RESOLVER, planningEvent);
    updateResolverSubItemVisibility(PreservationAgents.RESOLVER, planningAgent);
    MenuItem planningItem = new MenuItem(messages.title("planning"), planningMenu);
    planningItem.addStyleName("planning_menu_item");
    updateResolverTopItemVisibility(Planning.RESOLVER, planningItem, 6);

    // Help
    updateResolverTopItemVisibility(Help.RESOLVER, help, 7);
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

  private void updateResolverTopItemVisibility(final HistoryResolver resolver, final MenuItem item, final int index) {
    resolver.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

      @Override
      public void onFailure(Throwable caught) {
        logger.error("Error getting role", caught);
      }

      @Override
      public void onSuccess(Boolean asRole) {
        if (asRole) {
          insertIntoNavigationMenu(item, index);
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

  private void insertIntoNavigationMenu(MenuItem item, int index) {
    int indexToInsert = index <= navigationMenuItemCount ? index : navigationMenuItemCount;
    navigationMenu.insertItem(item, indexToInsert);
    navigationMenuItemCount++;
  }
}
