/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.ClientLoggerService;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.home.client.Home;
import pt.gov.dgarq.roda.wui.main.client.logos.LogosBundle;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.LanguageSwitcherPanel;
import config.i18n.client.MainConstants;

/**
 * @author Luis Faria
 * 
 */
public class Main extends SimplePanel implements EntryPoint, HistoryListener {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainConstants constants = (MainConstants) GWT
			.create(MainConstants.class);

	public void onModuleLoad() {

		// Set uncaught exception handler
		ClientLogger.setUncaughtExceptionHandler();

		// Remove loading image
		DOM.removeChild(RootPanel.getBodyElement(), DOM
				.getElementById("loading"));

		// Add main widget to root panel
		RootPanel.get().add(this);

		// deferred call to init
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				init();
			}
		});

	}

	private VerticalPanel layout;

	private HorizontalPanel banner;

	private FocusPanel homeLinkArea;

	private LanguageSwitcherPanel languageSwitcherPanel;

	private LoginPanel loginPanel;

	private Menu menu;

	private BreadcrumbPanel breadcrumbPanel;

	private ContentPanel contentPanel;

	private SimplePanel footer;

	private HorizontalPanel logos;

	private LogosBundle logosBundle = (LogosBundle) GWT
			.create(LogosBundle.class);

	/**
	 * Create a new main
	 */
	public Main() {
		this.addStyleName("main");
	}

	/**
	 * Initialize
	 */
	public void init() {

		logger.info("Creating banner");

		layout = new VerticalPanel();

		this.setWidget(layout);

		banner = new HorizontalPanel();
		homeLinkArea = new FocusPanel();
		languageSwitcherPanel = new LanguageSwitcherPanel();
		loginPanel = new LoginPanel();

		banner.add(homeLinkArea);
		banner.add(languageSwitcherPanel);
		banner.add(loginPanel);

		banner.addStyleName("banner");
		homeLinkArea.addStyleName("homeLink");
		banner.setCellHorizontalAlignment(languageSwitcherPanel,
				HorizontalPanel.ALIGN_RIGHT);
		banner.setCellHorizontalAlignment(loginPanel,
				HorizontalPanel.ALIGN_RIGHT);

		menu = new Menu();
		contentPanel = ContentPanel.getInstance();
		breadcrumbPanel = new BreadcrumbPanel(contentPanel);

		logos = createLogos();

		layout.add(banner);
		layout.add(menu);
		layout.add(breadcrumbPanel.getWidget());
		layout.add(contentPanel);
		layout.add(logos);

		homeLinkArea.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				History.newItem(Home.getInstance().getHistoryPath());
			}

		});

		homeLinkArea.setTitle(constants.homeTitle());

		onHistoryChanged(History.getToken());
		History.addHistoryListener(this);

		footer = new SimplePanel();

		RootPanel.get().add(footer);

		logos.addStyleName("main-logos");

		footer.addStyleName("main-footer");

		layout.setCellHeight(contentPanel, "100%");

		layout.addStyleName("main-layout");
	}

	private HorizontalPanel createLogos() {
		HorizontalPanel ret = new HorizontalPanel();

		Image poap = new Image();
		Image uniao_europeia = new Image();
		Image rep_portuguesa = new Image();
		Image min_cultura = new Image();

		Image dgarq = new Image();
		Image uminho = new Image();

		applyImages(poap, logosBundle.poap_bw(), logosBundle.poap());
		applyImages(uniao_europeia, logosBundle.uniao_europeia_bw(),
				logosBundle.uniao_europeia());
		applyImages(rep_portuguesa, logosBundle.rep_portuguesa_bw(),
				logosBundle.rep_portuguesa());
		applyImages(min_cultura, logosBundle.min_cultura_bw(), logosBundle
				.min_cultura());

		applyImages(dgarq, logosBundle.dgarq_bw(), logosBundle.dgarq());
		applyImages(uminho, logosBundle.uminho_bw(), logosBundle.uminho());

		addLink(poap, "http://www.poap.pt", "_blank");
		addLink(uniao_europeia, "http://europa.eu", "_blank");
		addLink(rep_portuguesa, "http://www.portugal.gov.pt", "_blank");
		addLink(min_cultura, "http://www.min-cultura.pt", "_blank");

		addLink(dgarq, "http://www.dgarq.gov.pt", "_blank");
		addLink(uminho, "http://www.uminho.pt", "_blank");

		ret.add(poap);
		ret.add(uniao_europeia);
		ret.add(rep_portuguesa);
		ret.add(min_cultura);

		ret.add(dgarq);
		ret.add(uminho);

		ret.setCellWidth(poap, "70px");
		ret.setCellWidth(uniao_europeia, "70px");
		ret.setCellWidth(rep_portuguesa, "70px");
		ret.setCellWidth(min_cultura, "100%");

		ret.setCellWidth(dgarq, "70px");
		ret.setCellWidth(uminho, "70px");

		ret.setHorizontalAlignment(HasAlignment.ALIGN_RIGHT);

		poap.addStyleName("main-logos-logo");
		uniao_europeia.addStyleName("main-logos-logo");
		rep_portuguesa.addStyleName("main-logos-logo");
		min_cultura.addStyleName("main-logos-logo");
		dgarq.addStyleName("main-logos-logo");
		uminho.addStyleName("main-logos-logo");

		return ret;
	}

	private void addLink(Image image, final String url, final String target) {
		image.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				Window.open(url, target, "");

			}

		});
	}

	private void applyImages(final Image image,
			final AbstractImagePrototype normal,
			final AbstractImagePrototype hover) {
		normal.applyTo(image);
		image.addMouseListener(new MouseListener() {

			public void onMouseDown(Widget sender, int x, int y) {
				// nothing to do
			}

			public void onMouseEnter(Widget sender) {
				hover.applyTo(image);
			}

			public void onMouseLeave(Widget sender) {
				normal.applyTo(image);
			}

			public void onMouseMove(Widget sender, int x, int y) {
				// nothing to do
			}

			public void onMouseUp(Widget sender, int x, int y) {
				// nothing to do
			}

		});
	}

	public void onHistoryChanged(String historyToken) {
		if (historyToken.length() == 0) {
			breadcrumbPanel.updatePath(Tools.splitHistory(Home.getInstance()
					.getHistoryPath()));
			History.newItem(Home.getInstance().getHistoryPath());
		} else {
			final String decodedHistoryToken = URL.decode(historyToken);
			String[] historyPath = Tools.splitHistory(decodedHistoryToken);
			breadcrumbPanel.updatePath(historyPath);
			DeferredCommand.addCommand(new Command() {

				public void execute() {
					ClientLoggerService.Util.getInstance().pagehit(
							decodedHistoryToken, new AsyncCallback<Void>() {

								public void onFailure(Throwable caught) {
									// do nothing
								}

								public void onSuccess(Void result) {
									// do nothing

								}

							});
				}

			});

		}
	}

}
