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

import org.fusesource.restygwt.client.Defaults;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.resources.MyResources;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.RODADispatcher;
import org.roda.wui.client.services.Services;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.ConfigurationManager;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class Main extends Composite implements EntryPoint {

  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @Override
  public void onModuleLoad() {

    // Set uncaught exception handler
    ClientLogger.setUncaughtExceptionHandler();

    JavascriptUtils.exportStaticMethod();

    // load shared properties before init
    Services services = new Services("Retrieve shared properties", "get");
    services.configurationsResource(s -> s.retrieveSharedProperties(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((sharedProperties, throwable) -> {
        if (throwable != null) {
          logger.error("Failed loading initial data", throwable);
        } else {
          ConfigurationManager.initialize(sharedProperties.getProperties());
          init();
        }
      });
  }

  interface Binder extends UiBinder<Widget, Main> {
  }

  @UiField(provided = true)
  UserMenu userMenu;

  @UiField(provided = true)
  Header header;

  @UiField(provided = true)
  ContentPanel contentPanel;

  Composite footer;

  /**
   * Create a new main
   */
  public Main() {
    userMenu = new UserMenu();
    header = new Header();
    contentPanel = ContentPanel.getInstance();
    footer = new Footer();

    Binder uiBinder = GWT.create(Binder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Initialize
   */
  public void init() {
    MyResources.INSTANCE.css().ensureInjected();

    Defaults.setDispatcher(RODADispatcher.INSTANCE);

    // Remove loading image
    RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));
    NodeList<Element> bodyChilds = RootPanel.getBodyElement().getElementsByTagName("iframe");
    for (int i = 0; i < bodyChilds.getLength(); i++) {
      Element bodyChild = bodyChilds.getItem(i);
      if (!bodyChild.hasAttribute("title")) {
        bodyChild.setAttribute("title", "iframe_title");
      }
    }

    // Add main widget to root panel
    RootPanel.get().add(this);
    RootPanel.get().add(footer);
    RootPanel.get().addStyleName("roda");

    // Initialize
    userMenu.init();
    header.init();
    contentPanel.init();
    onHistoryChanged(History.getToken());
    History.addValueChangeHandler(event -> onHistoryChanged(event.getValue()));

    if (ConfigurationManager.getBoolean(false, RodaConstants.UI_COOKIES_ACTIVE_PROPERTY)) {
      JavascriptUtils.setCookieOptions(messages.cookiesMessage(), messages.cookiesDismisse(),
        messages.cookiesLearnMore(), "#" + Theme.RESOLVER.getHistoryToken() + "/CookiesPolicy.html");
    }

    if (ConfigurationManager.getBoolean(true, RodaConstants.UI_EXPIRED_SESSION_DETECTOR_ACTIVE)) {
      ExpiredSessionDetector expiredSessionDetector = new ExpiredSessionDetector();
      expiredSessionDetector
        .setScheduleTime(ConfigurationManager.getInt(RodaConstants.UI_EXPIRED_SESSION_DETECTOR_TIME));
    }

  }

  private void onHistoryChanged(String historyToken) {
    if (historyToken.isEmpty()) {
      contentPanel.update(Welcome.RESOLVER.getHistoryPath());
      HistoryUtils.newHistory(Welcome.RESOLVER);
    } else {
      List<String> currentHistoryPath = HistoryUtils.getCurrentHistoryPath();
      contentPanel.update(currentHistoryPath);
      GAnalyticsTracker.track(historyToken);
    }
  }
}
