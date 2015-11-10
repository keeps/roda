/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.event.client;

import org.roda.core.data.PluginInfo;
import org.roda.core.data.Task;
import org.roda.core.data.TaskInstance;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RODAException;
import org.roda.wui.common.client.PrintReportException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Event Management service interface
 * 
 * @author Luis Faria
 * 
 */
public interface EventManagementServiceAsync {

  /**
   * Get task total count
   * 
   * @param filter
   * 
   * @return
   * @throws RODAException
   */
  public void getTaskCount(Filter filter, AsyncCallback<Integer> callback);

  /**
   * Get task list
   * 
   * @param adapter
   * 
   * @return
   * @throws RODAException
   */
  public void getTasks(ContentAdapter adapter, AsyncCallback<Task[]> callback);

  /**
   * Add a new task. Use start date as null to choose current date.
   * 
   * @param task
   * @return the task state after adding
   * @throws RODAException
   */
  public void addTask(Task task, AsyncCallback<Task> callback);

  /**
   * Remove an existing task
   * 
   * @param taskId
   * @throws RODAException
   */
  public void removeTask(String taskId, AsyncCallback<Void> callback);

  /**
   * Modify an existing task. Use start date as null to choose current date.
   * 
   * @param task
   * @return
   * @throws RODAException
   */
  public void modifyTask(Task task, AsyncCallback<Task> callback);

  public void pauseTask(String taskId, AsyncCallback<Task> callback);

  public void resumeTask(String taskId, AsyncCallback<Task> callback);

  /**
   * Get the plugin list
   * 
   * @return
   * @throws RODAException
   */
  public void getPlugins(AsyncCallback<PluginInfo[]> callback);

  /**
   * Get task instances count
   * 
   * @param filter
   * 
   * @return
   * @throws RODAException
   */
  public void getTaskInstanceCount(Filter filter, AsyncCallback<Integer> callback);

  /**
   * Get task instance list
   * 
   * @param adapter
   * @return
   * @throws RODAException
   */
  public void getTaskInstances(ContentAdapter adapter, AsyncCallback<TaskInstance[]> callback);

  /**
   * Get a task instance
   * 
   * @param taskInstanceId
   *          the task instance id
   * @return
   * @throws RODAException
   */
  public void getTaskInstance(String taskInstanceId, AsyncCallback<TaskInstance> callback);

  /**
   * Set task list report info parameters
   * 
   * @param adapter
   * @param localeString
   * @throws PrintReportException
   */
  public void setTaskListReportInfo(ContentAdapter adapter, String localeString, AsyncCallback<Void> callback);

  /**
   * Set instance list report info parameters
   * 
   * @param adapter
   * @param locale
   * @throws PrintReportException
   */
  public void setInstanceListReportInfo(ContentAdapter adapter, String locale, AsyncCallback<Void> callback);

}
