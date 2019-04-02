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
package org.roda.wui.client.portal;

import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.resources.MyResources;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.main.GAnalyticsTracker;
import org.roda.wui.client.main.Theme;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class MainPortal extends Composite implements EntryPoint {
  private ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @Override
  public void onModuleLoad() {
    // Set uncaught exception handler
    ClientLogger.setUncaughtExceptionHandler();

    // load shared properties before init
    BrowserService.Util.getInstance().retrieveSharedProperties(LocaleInfo.getCurrentLocale().getLocaleName(),
      new AsyncCallback<Map<String, List<String>>>() {
        @Override
        public void onFailure(Throwable caught) {
          logger.error("Failed loading initial data", caught);
        }

        @Override
        public void onSuccess(Map<String, List<String>> sharedProperties) {
          ConfigurationManager.initialize(sharedProperties);
          init();
        }
      });

  }

  interface Binder extends UiBinder<Widget, MainPortal> {
  }

  @UiField(provided = true)
  ContentPanelPortal contentPanel;

  /**
   * Create a new main
   */
  public MainPortal() {
    contentPanel = ContentPanelPortal.getInstance();
    Binder uiBinder = GWT.create(Binder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Initialize
   */
  public void init() {
    MyResources.INSTANCE.css().ensureInjected();

    HistoryUtils.initEndpoint(true);

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
    RootPanel.get().addStyleName("roda");

    // Initialize
    contentPanel.init();
    onHistoryChanged(History.getToken());
    History.addValueChangeHandler(event -> onHistoryChanged(event.getValue()));

    if (ConfigurationManager.getBoolean(false, RodaConstants.UI_COOKIES_ACTIVE_PROPERTY)) {
      JavascriptUtils.setCookieOptions(messages.cookiesMessage(), messages.cookiesDismisse(),
        messages.cookiesLearnMore(), "#" + Theme.RESOLVER.getHistoryToken() + "/CookiesPolicy.html");
    }
  }

  private void onHistoryChanged(String historyToken) {
    if (historyToken.length() == 0) {
      contentPanel.update(WelcomePortal.RESOLVER.getHistoryPath());
      HistoryUtils.newHistory(WelcomePortal.RESOLVER);
    } else {
      List<String> currentHistoryPath = HistoryUtils.getCurrentHistoryPath();
      contentPanel.update(currentHistoryPath);
      GAnalyticsTracker.track(historyToken);
    }
  }
}
