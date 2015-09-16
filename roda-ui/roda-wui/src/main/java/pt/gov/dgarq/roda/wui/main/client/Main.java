/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.LanguageSwitcherPanel;
import config.i18n.client.MainConstants;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import pt.gov.dgarq.roda.wui.home.client.Home;

/**
 * @author Luis Faria
 * 
 */
public class Main extends Composite implements EntryPoint {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

  public void onModuleLoad() {

    // Set uncaught exception handler
    ClientLogger.setUncaughtExceptionHandler();

    // Remove loading image
    RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));

    // Add main widget to root panel
    RootPanel.get().add(this);
    RootPanel.get().add(footer);
    RootPanel.get().addStyleName("roda");

    // deferred call to init
    Scheduler.get().scheduleDeferred(new Command() {

      public void execute() {
        DescriptionLevelUtils.load(new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            logger.error("Failed loading initial data", caught);
          }

          @Override
          public void onSuccess(Void result) {
            init();
          }
        });
      }
    });
  }

  interface Binder extends UiBinder<Widget, Main> {
  }

  @UiField
  AccessibleFocusPanel homeLinkArea;

  @UiField(provided = true)
  LanguageSwitcherPanel languageSwitcherPanel;

  @UiField(provided = true)
  Menu menu;

  @UiField(provided = true)
  ContentPanel contentPanel;

  Composite footer;

  /**
   * Create a new main
   */
  public Main() {
    languageSwitcherPanel = new LanguageSwitcherPanel();
    menu = new Menu();
    contentPanel = ContentPanel.getInstance();

    footer = new Footer();

    Binder uiBinder = GWT.create(Binder.class);
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Initialize
   */
  public void init() {
    contentPanel.init();
    onHistoryChanged(History.getToken());
    History.addValueChangeHandler(new ValueChangeHandler<String>() {

      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onHistoryChanged(event.getValue());
      }
    });

    homeLinkArea.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        Tools.newHistory(Home.RESOLVER);
      }
    });

    homeLinkArea.setTitle(constants.homeTitle());

  }

  private void onHistoryChanged(String historyToken) {
    if (historyToken.length() == 0) {
      contentPanel.update(Home.RESOLVER.getHistoryPath());
      Tools.newHistory(Home.RESOLVER);
    } else {

      List<String> currentHistoryPath = Tools.getCurrentHistoryPath();
      GWT.log("History path: " + currentHistoryPath);
      contentPanel.update(currentHistoryPath);

      /*
       * Scheduler.get().scheduleDeferred(new Command() {
       * 
       * public void execute() {
       * ClientLoggerService.Util.getInstance().pagehit(decodedHistoryToken, new
       * AsyncCallback<Void>() {
       * 
       * public void onFailure(Throwable caught) { // do nothing }
       * 
       * public void onSuccess(Void result) { // do nothing }
       * 
       * }); }
       * 
       * });
       */
      GAnalyticsTracker.track(historyToken);
    }
  }
}
