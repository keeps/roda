/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.event.client;

import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.management.client.Management;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class EventManagement implements HistoryResolver {

  private static EventManagement instance = null;

  /**
   * Get singleton instance
   * 
   * @return
   */
  public static EventManagement getInstance() {
    if (instance == null) {
      instance = new EventManagement();
    }
    return instance;
  }

  private static EventManagementConstants constants = (EventManagementConstants) GWT
    .create(EventManagementConstants.class);

  private TabPanel tabPanel;
  private TaskList taskList;
  private TaskInstanceList taskInstanceList;
  private boolean initialized;

  private EventManagement() {
    initialized = false;
  }

  protected void init() {
    if (!initialized) {
      initialized = true;
      tabPanel = new TabPanel();
      taskList = new TaskList();
      taskInstanceList = new TaskInstanceList();

      tabPanel.add(taskList.getWidget(), constants.taskListTab());
      tabPanel.add(taskInstanceList.getWidget(), constants.taskInstanceListTab());

      tabPanel.addTabListener(new TabListener() {

        public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
          return true;
        }

        public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
          if (tabIndex == 0) {
            History.newItem(getHistoryPath() + ".tasks");
            taskList.init();
          } else if (tabIndex == 1) {
            History.newItem(getHistoryPath() + ".taskInstances");
            taskInstanceList.init();
          }
        }

      });

      tabPanel.addStyleName("wui-management-event-tabs");
    }
  }

  public String getHistoryPath() {
    return Management.RESOLVER.getHistoryPath() + "." + getHistoryToken();
  }

  public String getHistoryToken() {
    return "event";
  }

  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().checkRole(this, callback);
  }

  public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.length == 0) {
      History.newItem(getHistoryPath() + ".tasks");
      callback.onSuccess(null);
    } else if (historyTokens.length == 1) {
      if (historyTokens[0].equals("tasks")) {
        init();
        tabPanel.selectTab(0);
        callback.onSuccess(tabPanel);
      } else if (historyTokens[0].equals("taskInstances")) {
        init();
        tabPanel.selectTab(1);
        callback.onSuccess(tabPanel);
      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens[0]));
      }
    } else {
      History.newItem(getHistoryPath() + ".tasks");
      callback.onSuccess(null);
    }

  }

}
