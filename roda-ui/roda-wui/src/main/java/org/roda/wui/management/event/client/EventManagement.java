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
package org.roda.wui.management.event.client;

import java.util.List;

import org.roda.wui.common.client.BadHistoryTokenException;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.UserLogin;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.management.client.Management;

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
            Tools.newHistory(EventManagement.this, "tasks");
            taskList.init();
          } else if (tabIndex == 1) {
            Tools.newHistory(EventManagement.this, "tasksInstances");
            taskInstanceList.init();
          }
        }

      });

      tabPanel.addStyleName("wui-management-event-tabs");
    }
  }

  public List<String> getHistoryPath() {
    return Tools.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
  }

  public String getHistoryToken() {
    return "event";
  }

  public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
    UserLogin.getInstance().checkRole(this, callback);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      Tools.newHistory(this, "tasks");
      callback.onSuccess(null);
    } else if (historyTokens.size() == 1) {
      if (historyTokens.get(0).equals("tasks")) {
        init();
        tabPanel.selectTab(0);
        callback.onSuccess(tabPanel);
      } else if (historyTokens.get(0).equals("taskInstances")) {
        init();
        tabPanel.selectTab(1);
        callback.onSuccess(tabPanel);
      } else {
        callback.onFailure(new BadHistoryTokenException(historyTokens.get(0)));
      }
    } else {
      Tools.newHistory(this, "tasks");
      callback.onSuccess(null);
    }

  }

}
