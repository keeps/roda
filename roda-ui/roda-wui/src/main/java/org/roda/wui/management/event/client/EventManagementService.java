package org.roda.wui.management.event.client;

import org.roda.core.common.RODAException;
import org.roda.core.data.PluginInfo;
import org.roda.core.data.Task;
import org.roda.core.data.TaskInstance;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.wui.common.client.PrintReportException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Event Management service interface
 * 
 * @author Luis Faria
 * 
 */
public interface EventManagementService extends RemoteService {

  /**
   * Event Management service URI
   */
  public static final String SERVICE_URI = "eventmanagementservice";

  /**
   * Utilities
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return
     */
    public static EventManagementServiceAsync getInstance() {

      EventManagementServiceAsync instance = (EventManagementServiceAsync) GWT.create(EventManagementService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  /**
   * Get task total count
   * 
   * @param filter
   * 
   * @return
   * @throws RODAException
   */
  public int getTaskCount(Filter filter) throws RODAException;

  /**
   * Get task list
   * 
   * @param adapter
   * 
   * @return
   * @throws RODAException
   */
  public Task[] getTasks(ContentAdapter adapter) throws RODAException;

  /**
   * Add a new task. Use start date as null to choose current date.
   * 
   * @param task
   * @return the task state after adding
   * @throws RODAException
   */
  public Task addTask(Task task) throws RODAException;

  /**
   * Remove an existing task
   * 
   * @param taskId
   * @throws RODAException
   */
  public void removeTask(String taskId) throws RODAException;

  /**
   * Modify an existing task. Use start date as null to choose current date.
   * 
   * @param task
   * @return
   * @throws RODAException
   */
  public Task modifyTask(Task task) throws RODAException;

  public Task pauseTask(String taskId) throws RODAException;

  public Task resumeTask(String taskId) throws RODAException;

  /**
   * Get the plugin list
   * 
   * @return
   * @throws RODAException
   */
  public PluginInfo[] getPlugins() throws RODAException;

  /**
   * Get task instances count
   * 
   * @param filter
   * 
   * @return
   * @throws RODAException
   */
  public int getTaskInstanceCount(Filter filter) throws RODAException;

  /**
   * Get task instance list
   * 
   * @param adapter
   * @return
   * @throws RODAException
   */
  public TaskInstance[] getTaskInstances(ContentAdapter adapter) throws RODAException;

  /**
   * Get a task instance
   * 
   * @param taskInstanceId
   *          the task instance id
   * @return
   * @throws RODAException
   */
  public TaskInstance getTaskInstance(String taskInstanceId) throws RODAException;

  /**
   * Set task list report info parameters
   * 
   * @param adapter
   * @param localeString
   * @throws PrintReportException
   */
  public void setTaskListReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException;

  /**
   * Set instance list report info parameters
   * 
   * @param adapter
   * @param locale
   * @throws PrintReportException
   */
  public void setInstanceListReportInfo(ContentAdapter adapter, String locale) throws PrintReportException;

}
