/**
 * 
 */
package pt.gov.dgarq.roda.wui.about.client;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.HTMLWidgetWrapper;

/**
 * @author Luis Faria
 * 
 */
public class About implements HistoryResolver {

  private static About instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the About singleton
   */
  public static About getInstance() {
    if (instance == null) {
      instance = new About();
    }
    return instance;
  }

  private boolean initialized;

  private HTMLWidgetWrapper page;

  private About() {
    initialized = false;
  }

  private void init() {
    if (!initialized) {
      initialized = true;
      page = new HTMLWidgetWrapper("About.html");
      page.addStyleName("aboutpage");
    }
  }

  public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      init();
      callback.onSuccess(page);
    } else if (historyTokens.size() == 1) {
      if (historyTokens.get(0).equals("services")) {
        HTMLWidgetWrapper servicesPage = new HTMLWidgetWrapper("Services.html");
        servicesPage.addStyleName("servicespage");
        callback.onSuccess(servicesPage);
      } else if (historyTokens.get(0).equalsIgnoreCase("policies")) {
        HTMLWidgetWrapper policiesPage = new HTMLWidgetWrapper("Policies.html");
        policiesPage.addStyleName("policiespage");
        callback.onSuccess(policiesPage);
      } else if (historyTokens.get(0).equalsIgnoreCase("research_development")) {
        HTMLWidgetWrapper researchDevelopmentPage = new HTMLWidgetWrapper("ResearchDevelopment.html");
        researchDevelopmentPage.addStyleName("researchdevelopmentpage");
        callback.onSuccess(researchDevelopmentPage);
      } else if (historyTokens.get(0).equalsIgnoreCase("contacts")) {
        HTMLWidgetWrapper contactsPage = new HTMLWidgetWrapper("Contacts.html");
        contactsPage.addStyleName("contactspage");
        callback.onSuccess(contactsPage);
      } else if (historyTokens.get(0).equalsIgnoreCase("register")) {
        HTMLWidgetWrapper aboutRegisterPage = new HTMLWidgetWrapper("AboutRegister.html");
        aboutRegisterPage.addStyleName("aboutregisterpage");
        callback.onSuccess(aboutRegisterPage);
      } else if (historyTokens.get(0).equalsIgnoreCase("help")) {
        HTMLWidgetWrapper aboutHelpPage = new HTMLWidgetWrapper("AboutHelp.html");
        aboutHelpPage.addStyleName("abouthelppage");
        callback.onSuccess(aboutHelpPage);
      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
      }
    } else {
      Tools.newHistory(this);
      callback.onSuccess(null);
    }
  }

  public List<String> getHistoryPath() {
    return Arrays.asList(getHistoryToken());
  }

  public String getHistoryToken() {
    return "about";
  }

  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
    callback.onSuccess(Boolean.TRUE);
  }

}
