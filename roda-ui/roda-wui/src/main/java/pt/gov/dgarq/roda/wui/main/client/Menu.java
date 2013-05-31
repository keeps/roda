/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import org.adamtacy.client.ui.EffectPanel;
import org.adamtacy.client.ui.effects.Effect;
import org.adamtacy.client.ui.effects.EffectHandler;
import org.adamtacy.client.ui.effects.impl.ChangeColor;
import org.adamtacy.client.ui.effects.impl.Show;

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
import pt.gov.dgarq.roda.wui.management.editor.client.MetadataEditor;
import pt.gov.dgarq.roda.wui.management.event.client.EventManagement;
import pt.gov.dgarq.roda.wui.management.statistics.client.Statistics;
import pt.gov.dgarq.roda.wui.management.user.client.UserLog;
import pt.gov.dgarq.roda.wui.management.user.client.WUIUserManagement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class Menu extends VerticalPanel {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainConstants constants = (MainConstants) GWT
			.create(MainConstants.class);

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

		UserLogin.getInstance().getAuthenticatedUser(
				new AsyncCallback<AuthenticatedUser>() {

					public void onFailure(Throwable caught) {
						logger
								.fatal("Error getting Authenticated user",
										caught);

					}

					public void onSuccess(AuthenticatedUser user) {
						updateVisibles(user);
						add(about);
						add(dissemination);
						add(ingest);
						add(administration);
					}

				});

		UserLogin.getInstance().addLoginStatusListener(
				new LoginStatusListener() {

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

		Label services = new Label(constants.title_about_services()
				.toUpperCase());
		Label policies = new Label(constants.title_about_policies()
				.toUpperCase());
		Label researchDevelopment = new Label(constants
				.title_about_researchDevelopment().toUpperCase());
		Label contacts = new Label(constants.title_about_contacts()
				.toUpperCase());
		Label aboutRegister = new Label(constants.title_about_register()
				.toUpperCase());
		Label help = new Label(constants.title_about_help().toUpperCase());

		Label[] items = { services, policies, researchDevelopment, contacts,
				aboutRegister, help };

		aboutTitle.addClickListener(createClickListener(About.getInstance()
				.getHistoryPath()));
		services.addClickListener(createClickListener(About.getInstance()
				.getHistoryPath()
				+ ".services"));
		policies.addClickListener(createClickListener(About.getInstance()
				.getHistoryPath()
				+ ".policies"));
		researchDevelopment.addClickListener(createClickListener(About
				.getInstance().getHistoryPath()
				+ ".research_development"));
		contacts.addClickListener(createClickListener(About.getInstance()
				.getHistoryPath()
				+ ".contacts"));
		aboutRegister.addClickListener(createClickListener(About.getInstance()
				.getHistoryPath()
				+ ".register"));

		help.addClickListener(createClickListener(About.getInstance()
				.getHistoryPath()
				+ ".help"));

		MenuPanel aboutMenu = new MenuPanel(aboutTitle, items, "#88AFBE");
		aboutMenu.addStyleName("about");
		return aboutMenu;
	}

	private MenuPanel createDisseminationMenu() {
		Label disseminationTitle = new Label(constants.title_dissemination()
				.toUpperCase());

		Label browser = new Label(constants.title_dissemination_browse()
				.toUpperCase());
		Label basicSearch = new Label(constants
				.title_dissemination_search_basic().toUpperCase());
		Label advancedSearch = new Label(constants
				.title_dissemination_search_advanced().toUpperCase());
		Label help = new Label(constants.title_dissemination_help()
				.toUpperCase());

		Label[] items = { browser, basicSearch, advancedSearch, help };

		disseminationTitle.addClickListener(createClickListener(Dissemination
				.getInstance().getHistoryPath()));
		browser.addClickListener(createClickListener(Browse.getInstance()
				.getHistoryPath()));
		basicSearch.addClickListener(createClickListener(BasicSearch
				.getInstance().getHistoryPath()));
		advancedSearch.addClickListener(createClickListener(AdvancedSearch
				.getInstance().getHistoryPath()));

		help.addClickListener(createClickListener(Dissemination.getInstance()
				.getHistoryPath()
				+ ".help"));

		MenuPanel disseminationMenu = new MenuPanel(disseminationTitle, items,
				"#79C2FF");
		disseminationMenu.addStyleName("dissemination");
		return disseminationMenu;
	}

	private MenuPanel createAdministrationMenu() {
		Label administrationTitle = new Label(constants.title_administration()
				.toUpperCase());

		Label user_management = new Label(constants.title_administration_user()
				.toUpperCase());
		Label event_management = new Label(constants
				.title_administration_event().toUpperCase());
		Label metadata_edition = new Label(constants
				.title_administration_metadataEditor().toUpperCase());
		Label statistics = new Label(constants
				.title_administration_statistics().toUpperCase());

		Label log = new Label(constants.title_administration_log()
				.toUpperCase());

		Label help = new Label(constants.title_administration_help()
				.toUpperCase());

		Label[] items = { user_management, event_management, /* metadata_edition, */
		statistics, log, help };

		administrationTitle.addClickListener(createClickListener(Management
				.getInstance().getHistoryPath()));
		user_management.addClickListener(createClickListener(WUIUserManagement
				.getInstance().getHistoryPath()));
		event_management.addClickListener(createClickListener(EventManagement
				.getInstance().getHistoryPath()));
		metadata_edition.addClickListener(createClickListener(MetadataEditor
				.getInstance().getHistoryPath()));
		statistics.addClickListener(createClickListener(Statistics
				.getInstance().getHistoryPath()));
		log.addClickListener(createClickListener(UserLog.getInstance()
				.getHistoryPath()));

		help.addClickListener(createClickListener(Management.getInstance()
				.getHistoryPath()
				+ ".help"));

		MenuPanel administrationMenu = new MenuPanel(administrationTitle,
				items, "#D7BC00");
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

		ingestTitle.addClickListener(createClickListener(Ingest.getInstance()
				.getHistoryPath()));
		pre_ingest.addClickListener(createClickListener(PreIngest.getInstance()
				.getHistoryPath()));
		submit.addClickListener(createClickListener(IngestSubmit.getInstance()
				.getHistoryPath()));
		list.addClickListener(createClickListener(IngestList.getInstance()
				.getHistoryPath()));

		help.addClickListener(createClickListener(Ingest.getInstance()
				.getHistoryPath()
				+ ".help"));

		MenuPanel ingestMenu = new MenuPanel(ingestTitle, items, "#7e8a57");
		ingestMenu.addStyleName("ingest");
		return ingestMenu;
	}

	private class MenuPanel extends FocusPanel {

		private static final boolean HIDE_MENU_ITEMS = false;

		private static final double MENU_ITEMS_FINAL_OPACITY = 0.4;

		private static final int HIDE_TIMEOUT_MS = 0;

		private static final double ON_EFFECT_LENGTH = 0.1;

		private static final double OFF_EFFECT_LENGTH = 2.0;

		private final EffectPanel menuEffectPanel;

		private final ChangeColor menuColorEffect;

		private EffectPanel itemsEffectPanel;

		private Show itemsShowEffect;

		private final HorizontalPanel menuPanel;

		private final HorizontalPanel itemsPanel;

		private boolean isEffectPlaying;

		private boolean isOpen;

		/**
		 * Create a new menu panel
		 * 
		 * @param title
		 *            the menu title
		 * @param items
		 *            the menu items
		 * @param color
		 *            the menu hover color
		 */
		public MenuPanel(Label title, Label[] items, final String color) {
			menuEffectPanel = new EffectPanel();
			itemsEffectPanel = new EffectPanel();
			menuPanel = new HorizontalPanel();
			itemsPanel = new HorizontalPanel();

			isEffectPlaying = false;
			menuEffectPanel.add(menuPanel);
			menuColorEffect = new ChangeColor();
			menuColorEffect.getProperties().setStartColor("#bcbec0");
			menuColorEffect.getProperties().setEndColor(color);
			menuColorEffect.getProperties().setEffectLength(ON_EFFECT_LENGTH);
			menuColorEffect.addEffectHandler(new EffectHandler() {

				public void interruptedEvent(Effect theEffect) {
					isEffectPlaying = false;
				}

				public void postEvent(Effect theEffect) {
					isEffectPlaying = false;
				}

				public void preEvent(Effect theEffect) {
					isEffectPlaying = true;
				}

			});
			menuEffectPanel.addEffect(menuColorEffect);
			this.add(menuEffectPanel);

			itemsEffectPanel.add(itemsPanel);
			menuPanel.add(title);
			menuPanel.add(itemsEffectPanel);
			if (HIDE_MENU_ITEMS) {
				itemsShowEffect = new Show();
				itemsShowEffect.getProperties().setEffectLength(
						ON_EFFECT_LENGTH);
				itemsEffectPanel.addEffect(itemsShowEffect);
			}

			title.addStyleName("title");
			menuPanel.setStylePrimaryName("menu");
			menuPanel.addStyleDependentName("closed");
			itemsEffectPanel.addStyleName("menu-items-effects");
			menuPanel.setVerticalAlignment(ALIGN_MIDDLE);
			menuPanel.setCellVerticalAlignment(title, ALIGN_MIDDLE);
			menuPanel.setCellWidth(itemsEffectPanel, "100%");
			itemsPanel.setVerticalAlignment(ALIGN_MIDDLE);
			menuPanel.addStyleName("menu-panel");
			itemsPanel.addStyleName("menu-items-panel");

			for (int i = 0; i < items.length; i++) {
				items[i].addStyleName("item");
				itemsPanel.add(items[i]);
				if (i < items.length - 1) {
					HTML separator = new HTML("&nbsp;·&nbsp;");
					separator.addStyleName("separator");
					itemsPanel.add(separator);
				}
			}

			if (HIDE_MENU_ITEMS) {
				itemsShowEffect.getProperties().setEffectLength(
						OFF_EFFECT_LENGTH);
				itemsShowEffect.play(0, MENU_ITEMS_FINAL_OPACITY);
			}

			this.addMouseListener(new MouseListener() {
				Timer scheduleHide = new Timer() {

					@Override
					public void run() {
						hide();
					}

				};

				public void onMouseDown(Widget sender, int x, int y) {

				}

				public void onMouseEnter(Widget sender) {
					if (HIDE_TIMEOUT_MS > 0) {
						scheduleHide.cancel();
					}
					show();
				}

				public void onMouseLeave(Widget sender) {
					if (HIDE_TIMEOUT_MS > 0) {
						scheduleHide.schedule(HIDE_TIMEOUT_MS);
					} else {
						hide();
					}
				}

				public void onMouseMove(Widget sender, int x, int y) {

				}

				public void onMouseUp(Widget sender, int x, int y) {

				}

			});

		}

		protected void show() {
			if (!isOpen) {
				isOpen = true;
				if (isEffectPlaying) {
					menuColorEffect.interrupt();
					if (HIDE_MENU_ITEMS) {
						itemsShowEffect.interrupt();
					}
				}

				double playPoint = menuColorEffect.getProperties()
						.getCurrentFrame()
						/ menuColorEffect.getProperties()
								.getTotalNumberFrames();

				menuColorEffect.getProperties().setEffectLength(
						ON_EFFECT_LENGTH);
				menuColorEffect.play(playPoint, 1);
				if (HIDE_MENU_ITEMS) {
					itemsShowEffect.getProperties().setEffectLength(
							ON_EFFECT_LENGTH);
					itemsShowEffect.play(playPoint, 1);
				}
			}
		}

		protected void hide() {
			if (isOpen) {
				isOpen = false;
				if (isEffectPlaying) {
					menuColorEffect.interrupt();
					if (HIDE_MENU_ITEMS) {
						itemsShowEffect.interrupt();
					}
				}

				double playPoint = menuColorEffect.getProperties()
						.getCurrentFrame()
						/ menuColorEffect.getProperties()
								.getTotalNumberFrames();

				menuColorEffect.getProperties().setEffectLength(
						OFF_EFFECT_LENGTH);
				menuColorEffect.play(playPoint, 0);

				if (HIDE_MENU_ITEMS) {
					itemsShowEffect.getProperties().setEffectLength(
							OFF_EFFECT_LENGTH);
					itemsShowEffect.play(playPoint, MENU_ITEMS_FINAL_OPACITY);
				}

			}
		}

		/**
		 * Set the visibility of a determined menu item
		 * 
		 * @param index
		 *            the item index
		 * @param visible
		 *            true to turn visible, false otherwise
		 */
		public void setItemVisible(int index, boolean visible) {
			// visibility of (index)th item
			itemsPanel.getWidget(2 * index).setVisible(visible);

			// check separators
			int count = itemsPanel.getWidgetCount();
			if (count > 1) {
				boolean lastItemVisible = itemsPanel.getWidget(0).isVisible();
				for (int i = 2; i <= count; i += 2) {
					boolean indexVisible = itemsPanel.getWidget(i).isVisible();

					// separator index-1
					itemsPanel.getWidget(i - 1).setVisible(
							lastItemVisible && indexVisible);

					lastItemVisible |= indexVisible;
				}
			}

		}

	}

	private void updateVisibles(AuthenticatedUser user) {

		logger.info("Updating menu visibility for user " + user.getName());

		// About
		About.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting roles", caught);
					}

					public void onSuccess(Boolean permitted) {
						about.setVisible(permitted);
					}

				});

		// Dissemination
		Browse.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger
								.error("Error getting browse permissions",
										caught);

					}

					public void onSuccess(Boolean asRole) {
						dissemination.setItemVisible(0, asRole.booleanValue());
					}

				});

		BasicSearch.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting basic search role", caught);
					}

					public void onSuccess(Boolean asRole) {
						dissemination.setItemVisible(1, asRole.booleanValue());
					}

				});

		AdvancedSearch.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting advanced search role",
								caught);
					}

					public void onSuccess(Boolean asRole) {
						dissemination.setItemVisible(2, asRole.booleanValue());
					}

				});

		Dissemination.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting roles", caught);
					}

					public void onSuccess(Boolean asRole) {
						dissemination.setVisible(asRole);
					}

				});

		// Administration

		WUIUserManagement.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting browse role", caught);
					}

					public void onSuccess(Boolean asRole) {
						administration.setItemVisible(0, asRole.booleanValue());
					}

				});

		EventManagement.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting advanced search role",
								caught);
					}

					public void onSuccess(Boolean asRole) {
						administration.setItemVisible(1, asRole.booleanValue());
					}

				});

		Statistics.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting advanced search role",
								caught);
					}

					public void onSuccess(Boolean asRole) {
						administration.setItemVisible(2, asRole.booleanValue());
					}

				});

		UserLog.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting advanced search role",
								caught);
					}

					public void onSuccess(Boolean asRole) {
						administration.setItemVisible(3, asRole.booleanValue());
					}

				});

		Management.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting roles", caught);
					}

					public void onSuccess(Boolean asRole) {
						administration.setVisible(asRole);
					}

				});

		// Ingest
		PreIngest.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting browse role", caught);
					}

					public void onSuccess(Boolean asRole) {
						ingest.setItemVisible(0, asRole.booleanValue());
					}

				});

		IngestSubmit.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting browse role", caught);
					}

					public void onSuccess(Boolean asRole) {
						ingest.setItemVisible(1, asRole.booleanValue());
					}

				});

		IngestList.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting browse role", caught);
					}

					public void onSuccess(Boolean asRole) {
						ingest.setItemVisible(2, asRole.booleanValue());
					}

				});

		Ingest.getInstance().isCurrentUserPermitted(
				new AsyncCallback<Boolean>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting roles", caught);
					}

					public void onSuccess(Boolean asRole) {
						ingest.setVisible(asRole);
					}

				});

	}

}
