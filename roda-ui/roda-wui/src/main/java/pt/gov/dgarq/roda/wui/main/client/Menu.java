/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
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
import pt.gov.dgarq.roda.wui.main.client.MenuPanel.MenuColor;
import pt.gov.dgarq.roda.wui.management.client.Management;
import pt.gov.dgarq.roda.wui.management.editor.client.MetadataEditor;
import pt.gov.dgarq.roda.wui.management.event.client.EventManagement;
import pt.gov.dgarq.roda.wui.management.statistics.client.Statistics;
import pt.gov.dgarq.roda.wui.management.user.client.UserLog;
import pt.gov.dgarq.roda.wui.management.user.client.WUIUserManagement;

/**
 * @author Luis Faria
 * 
 */
public class Menu extends VerticalPanel {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

	private Widget choosenItem;

	private final MenuPanel about;

	private final MenuPanel dissemination;

	private final MenuPanel administration;

	private final MenuPanel ingest;

	/**
	 * Main menu constructor
	 * 
	 */
	public Menu() {
		about = createAboutMenu();
		dissemination = createDisseminationMenu();
		ingest = createIngestMenu();
		administration = createAdministrationMenu();

		about.setVisible(false);
		dissemination.setVisible(false);
		ingest.setVisible(false);
		administration.setVisible(false);

		this.choosenItem = null;

		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {

			public void onFailure(Throwable caught) {
				logger.fatal("Error getting Authenticated user", caught);

			}

			public void onSuccess(AuthenticatedUser user) {
				updateVisibles(user);
				add(about);
				add(dissemination);
				add(ingest);
				add(administration);
			}

		});

		UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {

			public void onLoginStatusChanged(AuthenticatedUser user) {
				updateVisibles(user);
			}

		});

		this.addStyleName("menus");
	}

	private ClickListener createClickListener(final String path) {
		return new ClickListener() {

			public void onClick(Widget sender) {
				sender.addStyleName("menu-item-on");
				if (choosenItem != null) {
					choosenItem.removeStyleName("menu-item-on");
				}
				choosenItem = sender;
				History.newItem(path);
			}

		};
	}

	private MenuPanel createAboutMenu() {
		Label aboutTitle = new Label(constants.title_about().toUpperCase());

		Label services = new Label(constants.title_about_services().toUpperCase());
		Label policies = new Label(constants.title_about_policies().toUpperCase());
		Label researchDevelopment = new Label(constants.title_about_researchDevelopment().toUpperCase());
		Label contacts = new Label(constants.title_about_contacts().toUpperCase());
		Label aboutRegister = new Label(constants.title_about_register().toUpperCase());
		Label help = new Label(constants.title_about_help().toUpperCase());

		Label[] items = { services, policies, researchDevelopment, contacts, aboutRegister, help };

		aboutTitle.addClickListener(createClickListener(About.getInstance().getHistoryPath()));
		services.addClickListener(createClickListener(About.getInstance().getHistoryPath() + ".services"));
		policies.addClickListener(createClickListener(About.getInstance().getHistoryPath() + ".policies"));
		researchDevelopment
				.addClickListener(createClickListener(About.getInstance().getHistoryPath() + ".research_development"));
		contacts.addClickListener(createClickListener(About.getInstance().getHistoryPath() + ".contacts"));
		aboutRegister.addClickListener(createClickListener(About.getInstance().getHistoryPath() + ".register"));

		help.addClickListener(createClickListener(About.getInstance().getHistoryPath() + ".help"));

		MenuPanel aboutMenu = new MenuPanel(aboutTitle, items, MenuColor.DARK_BLUE);
		aboutMenu.addStyleName("about");
		return aboutMenu;
	}

	private MenuPanel createDisseminationMenu() {
		Label disseminationTitle = new Label(constants.title_dissemination().toUpperCase());

		Label browser = new Label(constants.title_dissemination_browse().toUpperCase());
		Label basicSearch = new Label(constants.title_dissemination_search_basic().toUpperCase());
		Label advancedSearch = new Label(constants.title_dissemination_search_advanced().toUpperCase());
		Label help = new Label(constants.title_dissemination_help().toUpperCase());

		Label[] items = { browser, basicSearch, advancedSearch, help };

		disseminationTitle.addClickListener(createClickListener(Dissemination.getInstance().getHistoryPath()));
		browser.addClickListener(createClickListener(Browse.getInstance().getHistoryPath()));
		basicSearch.addClickListener(createClickListener(BasicSearch.getInstance().getHistoryPath()));
		advancedSearch.addClickListener(createClickListener(AdvancedSearch.getInstance().getHistoryPath()));

		help.addClickListener(createClickListener(Dissemination.getInstance().getHistoryPath() + ".help"));

		MenuPanel disseminationMenu = new MenuPanel(disseminationTitle, items, MenuColor.LIGHT_BLUE);
		disseminationMenu.addStyleName("dissemination");
		return disseminationMenu;
	}

	private MenuPanel createAdministrationMenu() {
		Label administrationTitle = new Label(constants.title_administration().toUpperCase());

		Label user_management = new Label(constants.title_administration_user().toUpperCase());
		Label event_management = new Label(constants.title_administration_event().toUpperCase());
		Label metadata_edition = new Label(constants.title_administration_metadataEditor().toUpperCase());
		Label statistics = new Label(constants.title_administration_statistics().toUpperCase());

		Label log = new Label(constants.title_administration_log().toUpperCase());

		Label help = new Label(constants.title_administration_help().toUpperCase());

		Label[] items = { user_management, event_management, /* metadata_edition, */
				statistics, log, help };

		administrationTitle.addClickListener(createClickListener(Management.getInstance().getHistoryPath()));
		user_management.addClickListener(createClickListener(WUIUserManagement.getInstance().getHistoryPath()));
		event_management.addClickListener(createClickListener(EventManagement.getInstance().getHistoryPath()));
		metadata_edition.addClickListener(createClickListener(MetadataEditor.getInstance().getHistoryPath()));
		statistics.addClickListener(createClickListener(Statistics.getInstance().getHistoryPath()));
		log.addClickListener(createClickListener(UserLog.getInstance().getHistoryPath()));

		help.addClickListener(createClickListener(Management.getInstance().getHistoryPath() + ".help"));

		MenuPanel administrationMenu = new MenuPanel(administrationTitle, items, MenuColor.GREEN);
		administrationMenu.addStyleName("administration");
		return administrationMenu;
	}

	private MenuPanel createIngestMenu() {
		Label ingestTitle = new Label(constants.title_ingest().toUpperCase());

		Label pre_ingest = new Label(constants.title_ingest_pre().toUpperCase());
		Label submit = new Label(constants.title_ingest_submit().toUpperCase());
		Label list = new Label(constants.title_ingest_list().toUpperCase());
		Label help = new Label(constants.title_ingest_help().toUpperCase());

		Label[] items = { pre_ingest, submit, list, help };

		ingestTitle.addClickListener(createClickListener(Ingest.getInstance().getHistoryPath()));
		pre_ingest.addClickListener(createClickListener(PreIngest.getInstance().getHistoryPath()));
		submit.addClickListener(createClickListener(IngestSubmit.getInstance().getHistoryPath()));
		list.addClickListener(createClickListener(IngestList.getInstance().getHistoryPath()));

		help.addClickListener(createClickListener(Ingest.getInstance().getHistoryPath() + ".help"));

		MenuPanel ingestMenu = new MenuPanel(ingestTitle, items, MenuColor.YELLOW);
		ingestMenu.addStyleName("ingest");
		return ingestMenu;
	}

	private void updateVisibles(AuthenticatedUser user) {

		logger.info("Updating menu visibility for user " + user.getName());

		// About
		About.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean permitted) {
				about.setVisible(permitted);
			}

		});

		// Dissemination
		Browse.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse permissions", caught);

			}

			public void onSuccess(Boolean asRole) {
				dissemination.setItemVisible(0, asRole.booleanValue());
			}

		});

		BasicSearch.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting basic search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				dissemination.setItemVisible(1, asRole.booleanValue());
			}

		});

		AdvancedSearch.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				dissemination.setItemVisible(2, asRole.booleanValue());
			}

		});

		Dissemination.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				dissemination.setVisible(asRole);
			}

		});

		// Administration

		WUIUserManagement.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration.setItemVisible(0, asRole.booleanValue());
			}

		});

		EventManagement.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration.setItemVisible(1, asRole.booleanValue());
			}

		});

		Statistics.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration.setItemVisible(2, asRole.booleanValue());
			}

		});

		UserLog.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration.setItemVisible(3, asRole.booleanValue());
			}

		});

		Management.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration.setVisible(asRole);
			}

		});

		// Ingest
		PreIngest.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest.setItemVisible(0, asRole.booleanValue());
			}

		});

		IngestSubmit.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest.setItemVisible(1, asRole.booleanValue());
			}

		});

		IngestList.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest.setItemVisible(2, asRole.booleanValue());
			}

		});

		Ingest.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest.setVisible(asRole);
			}

		});

	}

}
