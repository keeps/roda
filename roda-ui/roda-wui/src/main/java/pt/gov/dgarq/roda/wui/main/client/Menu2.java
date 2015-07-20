/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;
import pt.gov.dgarq.roda.wui.about.client.About;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;
import pt.gov.dgarq.roda.wui.dissemination.client.Dissemination;
import pt.gov.dgarq.roda.wui.dissemination.search.advanced.client.AdvancedSearch;
import pt.gov.dgarq.roda.wui.dissemination.search.basic.client.BasicSearch;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestList;
import pt.gov.dgarq.roda.wui.ingest.pre.client.PreIngest;
import pt.gov.dgarq.roda.wui.ingest.submit.client.IngestSubmit;
import pt.gov.dgarq.roda.wui.management.client.Management;
import pt.gov.dgarq.roda.wui.management.event.client.EventManagement;
import pt.gov.dgarq.roda.wui.management.statistics.client.Statistics;
import pt.gov.dgarq.roda.wui.management.user.client.Preferences;
import pt.gov.dgarq.roda.wui.management.user.client.Register;
import pt.gov.dgarq.roda.wui.management.user.client.UserLog;
import pt.gov.dgarq.roda.wui.management.user.client.WUIUserManagement;

/**
 * @author Luis Faria
 * 
 */
public class Menu2 extends Composite {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	interface MyUiBinder extends UiBinder<Widget, Menu2> {
	}

	@UiField
	MenuBar leftMenu;

	@UiField
	MenuBar rightMenu;

	private final MenuBar aboutMenu;

	private final MenuBar disseminationMenu;
	private MenuItem dissemination_browse;
	private MenuItem dissemination_searchBasic;
	private MenuItem dissemination_searchAdvanced;

	private final MenuBar ingestMenu;
	private MenuItem ingest_pre;
	private MenuItem ingest_submit;
	private MenuItem ingest_list;

	private final MenuBar administrationMenu;
	private MenuItem administration_user;
	private MenuItem administration_event;
	private MenuItem administration_statistics;
	private MenuItem administration_log;

	private final MenuBar userMenu;

	/**
	 * Main menu constructor
	 * 
	 */
	public Menu2() {
		initWidget(uiBinder.createAndBindUi(this));

		aboutMenu = new MenuBar(true);
		aboutMenu.addItem(constants.title_about_services(),
				createCommand(About.getInstance().getHistoryPath() + ".services"));
		aboutMenu.addItem(constants.title_about_policies(),
				createCommand(About.getInstance().getHistoryPath() + ".policies"));
		aboutMenu.addItem(constants.title_about_researchDevelopment(),
				createCommand(About.getInstance().getHistoryPath() + ".research_development"));
		aboutMenu.addItem(constants.title_about_contacts(),
				createCommand(About.getInstance().getHistoryPath() + ".contacts"));
		aboutMenu.addItem(constants.title_about_register(),
				createCommand(About.getInstance().getHistoryPath() + ".register"));
		aboutMenu.addItem(constants.title_about_help(), createCommand(About.getInstance().getHistoryPath() + ".help"));

		disseminationMenu = new MenuBar(true);
		dissemination_browse = disseminationMenu.addItem(constants.title_dissemination_browse(),
				createCommand(Browse.RESOLVER.getHistoryPath()));
		dissemination_searchBasic = disseminationMenu.addItem(constants.title_dissemination_search_basic(),
				createCommand(BasicSearch.getInstance().getHistoryPath()));
		dissemination_searchAdvanced = disseminationMenu.addItem(constants.title_dissemination_search_advanced(),
				createCommand(AdvancedSearch.getInstance().getHistoryPath()));
		disseminationMenu.addItem(constants.title_dissemination_help(),
				createCommand(Dissemination.getInstance().getHistoryPath() + ".help"));

		ingestMenu = new MenuBar(true);
		ingest_pre = ingestMenu.addItem(constants.title_ingest_pre(),
				createCommand(PreIngest.getInstance().getHistoryPath()));
		ingest_submit = ingestMenu.addItem(constants.title_ingest_submit(),
				createCommand(IngestSubmit.getInstance().getHistoryPath()));
		ingest_list = ingestMenu.addItem(constants.title_ingest_list(),
				createCommand(IngestList.getInstance().getHistoryPath()));
		ingestMenu.addItem(constants.title_ingest_help(),
				createCommand(Ingest.getInstance().getHistoryPath() + ".help"));

		administrationMenu = new MenuBar(true);
		administration_user = administrationMenu.addItem(constants.title_administration_user(),
				createCommand(WUIUserManagement.getInstance().getHistoryPath()));
		administration_event = administrationMenu.addItem(constants.title_administration_event(),
				createCommand(EventManagement.getInstance().getHistoryPath()));
		administration_statistics = administrationMenu.addItem(constants.title_administration_statistics(),
				createCommand(Statistics.getInstance().getHistoryPath()));
		administration_log = administrationMenu.addItem(constants.title_administration_log(),
				createCommand(UserLog.getInstance().getHistoryPath()));
		administrationMenu.addItem(constants.title_administration_help(),
				createCommand(Management.getInstance().getHistoryPath() + ".help"));

		userMenu = new MenuBar(true);
		userMenu.addItem(constants.loginLogout(), new ScheduledCommand() {

			@Override
			public void execute() {
				UserLogin.getInstance().logout(new AsyncCallback<AuthenticatedUser>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.fatal("Error logging out", caught);
					}

					@Override
					public void onSuccess(AuthenticatedUser result) {
						// do nothing
					}
				});
			}
		});
		userMenu.addItem(constants.loginPreferences(), createCommand(Preferences.getInstance().getHistoryPath()));

		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {

			public void onFailure(Throwable caught) {
				logger.fatal("Error getting Authenticated user", caught);

			}

			public void onSuccess(AuthenticatedUser user) {
				updateVisibles(user);
			}

		});

		UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {

			public void onLoginStatusChanged(AuthenticatedUser user) {
				updateVisibles(user);
			}

		});

		this.addStyleName("menus2");
		rightMenu.setStyleName("my-MenuBarRight");
	}

	private ScheduledCommand createCommand(final String path) {
		return new ScheduledCommand() {

			@Override
			public void execute() {
				History.newItem(path);
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

	private void updateVisibles(AuthenticatedUser user) {

		logger.info("Updating menu visibility for user " + user.getName());

		leftMenu.clearItems();
		rightMenu.clearItems();

		// About
		About.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean permitted) {
				if (permitted) {
					leftMenu.addItem(constants.title_about(), aboutMenu);
				}
			}

		});

		// Dissemination
		Browse.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse permissions", caught);

			}

			public void onSuccess(Boolean asRole) {
				dissemination_browse.setVisible(asRole);
			}

		});

		BasicSearch.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting basic search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				dissemination_searchBasic.setVisible(asRole);
			}

		});

		AdvancedSearch.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				dissemination_searchAdvanced.setVisible(asRole);
			}

		});

		Dissemination.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					leftMenu.addItem(constants.title_dissemination(), disseminationMenu);
				}
			}

		});

		// Ingest
		PreIngest.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest_pre.setVisible(asRole);
			}

		});

		IngestSubmit.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest_submit.setVisible(asRole);
			}

		});

		IngestList.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest_list.setVisible(asRole);
			}

		});

		Ingest.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					leftMenu.addItem(constants.title_ingest(), ingestMenu);
				}
			}

		});

		// Administration

		WUIUserManagement.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_user.setVisible(asRole);
			}

		});

		EventManagement.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_event.setVisible(asRole);
			}

		});

		Statistics.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_statistics.setVisible(asRole);
			}

		});

		UserLog.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_log.setVisible(asRole);
			}

		});

		Management.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					leftMenu.addItem(constants.title_administration(), administrationMenu);
				}
			}

		});

		// User
		if (user.isGuest()) {
			rightMenu.addItem(constants.loginLogin(), createLoginCommand());
			rightMenu.addItem(constants.loginRegister(), createCommand(Register.getInstance().getHistoryPath()));
		} else {
			rightMenu.addItem(user.getName(), userMenu);
		}

	}

}
