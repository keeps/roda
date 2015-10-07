package org.roda.wui.management.event.server;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.roda.core.common.RODAException;
import org.roda.core.data.PluginInfo;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Task;
import org.roda.core.data.TaskInstance;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.wui.common.client.PrintReportException;
import org.roda.wui.common.server.ServerTools;
import org.roda.wui.management.event.client.EventManagementService;
import org.w3c.util.DateParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import config.i18n.server.EventManagementMessages;

/**
 * Event Management service implementation
 * 
 * @author Luis Faria
 * 
 */
public class EventManagementServiceImpl extends RemoteServiceServlet implements EventManagementService {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  public int getTaskCount(Filter filter) throws RODAException {
    return getTaskCount(getThreadLocalRequest().getSession(), filter);
  }

  protected int getTaskCount(HttpSession session, Filter filter) throws RODAException {
    int taskCount;
    // try {
    // TODO move to new implementation
    // taskCount =
    // RodaClientFactory.getRodaClient(session).getSchedulerService().getTaskCount(filter);
    taskCount = 0;
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return taskCount;
  }

  public Task[] getTasks(ContentAdapter adapter) throws RODAException {
    return getTasks(getThreadLocalRequest().getSession(), adapter);
  }

  protected Task[] getTasks(HttpSession session, ContentAdapter adapter) throws RODAException {
    Task[] tasks;

    // try {
    // TODO move to new implementation
    // tasks =
    // RodaClientFactory.getRodaClient(session).getSchedulerService().getTasks(adapter);
    tasks = new Task[] {};
    // if (tasks == null) {
    // tasks = new Task[] {};
    // }
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }

    return tasks;
  }

  public Task addTask(Task task) throws RODAException {
    Task addedTask;
    // try {
    // if (task.getStartDate() == null) {
    // task.setStartDate(new Date());
    // }
    // logger.debug("Adding task: " + task);
    // addedTask =
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getSchedulerService()
    // .addTask(task);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }

    return null;
  }

  public Task modifyTask(Task task) throws RODAException {
    Task modifiedTask;
    // try {
    // if (task.getStartDate() == null) {
    // task.setStartDate(new Date());
    // }
    // modifiedTask =
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getSchedulerService()
    // .modifyTask(task);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return null;

  }

  public void removeTask(String taskId) throws RODAException {
    // try {
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getSchedulerService()
    // .removeTask(taskId);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
  }

  public Task pauseTask(String taskId) throws RODAException {
    Task ret = null;
    // try {
    // ret =
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getSchedulerService()
    // .pauseTask(taskId);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return ret;
  }

  public Task resumeTask(String taskId) throws RODAException {
    Task ret = null;
    // try {
    // ret =
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getSchedulerService()
    // .resumeTask(taskId);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return ret;
  }

  public PluginInfo[] getPlugins() throws RODAException {
    // PluginInfo[] plugins;
    // try {
    // plugins =
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getPluginsService()
    // .getPluginsInfo();
    // if (plugins == null) {
    // plugins = new PluginInfo[] {};
    // }
    //
    // Arrays.sort(plugins, new Comparator<PluginInfo>() {
    //
    // public int compare(PluginInfo o1, PluginInfo o2) {
    // return o1.getName().compareTo(o2.getName());
    // }
    //
    // });
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return null;
  }

  public int getTaskInstanceCount(Filter filter) throws RODAException {
    return getTaskInstanceCount(getThreadLocalRequest().getSession(), filter);
  }

  protected int getTaskInstanceCount(HttpSession session, Filter filter) throws RODAException {
    // TODO move to new implementation
    // try {
    // return
    // RodaClientFactory.getRodaClient(session).getSchedulerService().getTaskInstanceCount(filter);
    return 0;
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
  }

  public TaskInstance[] getTaskInstances(ContentAdapter adapter) throws RODAException {
    return getTaskInstances(getThreadLocalRequest().getSession(), adapter);
  }

  protected TaskInstance[] getTaskInstances(HttpSession session, ContentAdapter adapter) throws RODAException {
    TaskInstance[] taskInstances;
    // try {
    // TODO move to new implementation
    // taskInstances =
    // RodaClientFactory.getRodaClient(session).getSchedulerService().getTaskInstances(adapter);
    taskInstances = new TaskInstance[] {};

    if (taskInstances == null) {
      taskInstances = new TaskInstance[] {};
    }
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }

    return taskInstances;
  }

  public TaskInstance getTaskInstance(String taskInstanceId) throws RODAException {
    // try {
    // return
    // RodaClientFactory.getRodaClient(getThreadLocalRequest().getSession()).getSchedulerService()
    // .getTaskInstance(taskInstanceId);
    // } catch (RemoteException e) {
    // logger.error("Remote Exception", e);
    // throw RODAClient.parseRemoteException(e);
    // }
    return null;
  }

  public void setTaskListReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException {
    final Locale locale = ServerTools.parseLocale(localeString);
    final EventManagementMessages messages = new EventManagementMessages(locale);
    // TODO move to new implementation
    // ReportDownload.getInstance().createPDFReport(getThreadLocalRequest().getSession(),
    // new ReportContentSource<Task>() {
    //
    // public int getCount(HttpSession session, Filter filter) throws
    // Exception {
    // return getTaskCount(session, filter);
    // }
    //
    // public Task[] getElements(HttpSession session, ContentAdapter
    // adapter) throws Exception {
    // return getTasks(session, adapter);
    // }
    //
    // public Map<String, String> getElementFields(HttpServletRequest req,
    // Task task) {
    // return EventManagementServiceImpl.this.getTaskFields(task, messages);
    // }
    //
    // public String getElementId(Task task) {
    // return String.format(messages.getString("task.title"),
    // task.getName());
    //
    // }
    //
    // public String getReportTitle() {
    // return messages.getString("task.report.title");
    // }
    //
    // public String getFieldNameTranslation(String name) {
    // String translation;
    // try {
    // translation = messages.getString("task.label." + name);
    // } catch (MissingResourceException e) {
    // translation = name;
    // }
    //
    // return translation;
    // }
    //
    // public String getFieldValueTranslation(String value) {
    // String translation;
    // try {
    // translation = messages.getString("task.value." + value);
    // } catch (MissingResourceException e) {
    // translation = value;
    // }
    //
    // return translation;
    // }
    //
    // }, adapter);
  }

  protected Map<String, String> getTaskFields(Task task, EventManagementMessages messages) {
    Map<String, String> ret = new LinkedHashMap<String, String>();
    ret.put(messages.getString("task.label.name"), task.getName());
    ret.put(messages.getString("task.label.description"), task.getDescription());
    ret.put(messages.getString("task.label.username"), task.getUsername());
    ret.put(messages.getString("task.label.startDate"), DateParser.getIsoDate(task.getStartDate()));
    ret.put(
      messages.getString("task.label.repeatCount"),
      task.getRepeatCount() < 0 ? messages.getString("task.value.repeatCount.forever") : String.format(
        messages.getString("task.value.repeatCount"), task.getRepeatCount()));
    ret.put(
      messages.getString("task.label.repeatInterval"),
      task.getRepeatInterval() == 0 ? messages.getString("task.value.repeatInterval.continuously") : String.format(
        messages.getString("task.value.repeatInterval"), task.getRepeatInterval()));

    ret.put(messages.getString("task.label.plugin.name"), String.format(messages.getString("task.value.plugin.name"),
      task.getPluginInfo().getName(), task.getPluginInfo().getVersion()));
    ret.put(messages.getString("task.label.plugin.description"), task.getPluginInfo().getDescription());
    for (PluginParameter parameter : task.getPluginInfo().getParameters()) {
      if (!parameter.getName().equals("password")) {
        ret.put(String.format(messages.getString("task.label.plugin.parameter"), parameter.getName()),
          parameter.getValue());
      }
    }

    return ret;
  }

  public void setInstanceListReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException {
    final Locale locale = ServerTools.parseLocale(localeString);
    final EventManagementMessages messages = new EventManagementMessages(locale);
    // TODO move to new implementation
    // ReportDownload.getInstance().createPDFReport(getThreadLocalRequest().getSession(),
    // new ReportContentSource<TaskInstance>() {
    //
    // public int getCount(HttpSession session, Filter filter) throws
    // Exception {
    // return getTaskInstanceCount(session, filter);
    // }
    //
    // public TaskInstance[] getElements(HttpSession session, ContentAdapter
    // adapter) throws Exception {
    // return getTaskInstances(session, adapter);
    // }
    //
    // public Map<String, String> getElementFields(HttpServletRequest req,
    // TaskInstance instance) {
    // return EventManagementServiceImpl.this.getInstanceFields(instance,
    // messages);
    // }
    //
    // public String getElementId(TaskInstance instance) {
    // return String.format(messages.getString("instance.title"),
    // instance.getId());
    //
    // }
    //
    // public String getReportTitle() {
    // return messages.getString("instance.report.title");
    // }
    //
    // public String getFieldNameTranslation(String name) {
    // String translation;
    // try {
    // translation = messages.getString("instance.label." + name);
    // } catch (MissingResourceException e) {
    // translation = name;
    // }
    //
    // return translation;
    // }
    //
    // public String getFieldValueTranslation(String value) {
    // String translation;
    // try {
    // translation = messages.getString("instance.value." + value);
    // } catch (MissingResourceException e) {
    // translation = value;
    // }
    //
    // return translation;
    // }
    //
    // }, adapter);
  }

  protected Map<String, String> getInstanceFields(TaskInstance instance, EventManagementMessages messages) {
    Map<String, String> ret = new LinkedHashMap<String, String>();
    ret.put(messages.getString("instance.label.name"), instance.getName());
    ret.put(messages.getString("instance.label.description"), instance.getDescription());
    ret.put(messages.getString("instance.label.username"), instance.getUsername());
    ret.put(messages.getString("instance.label.state"), messages.getString("instance.value." + instance.getState()));
    ret.put(messages.getString("instance.label.startDate"), DateParser.getIsoDate(instance.getStartDate()));
    if (instance.getFinishDate() == null) {
      ret.put(messages.getString("instance.label.completePercentage"),
        String.format(messages.getString("instance.value.completePercentage"), instance.getCompletePercentage()));
    } else {
      ret.put(messages.getString("instance.label.finishDate"), DateParser.getIsoDate(instance.getFinishDate()));
    }

    ret.put(messages.getString("instance.label.plugin.name"), String.format(messages
      .getString("instance.value.plugin.name"), instance.getPluginInfo().getName(), instance.getPluginInfo()
      .getVersion()));
    ret.put(messages.getString("instance.label.plugin.description"), instance.getPluginInfo().getDescription());
    for (PluginParameter parameter : instance.getPluginInfo().getParameters()) {
      if (!parameter.getName().equals("password")) {
        ret.put(String.format(messages.getString("instance.label.plugin.parameter"), parameter.getName()),
          parameter.getValue());
      }
    }

    return ret;
  }

}
