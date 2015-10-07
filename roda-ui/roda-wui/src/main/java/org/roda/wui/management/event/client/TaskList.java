/**
 * 
 */
package org.roda.wui.management.event.client;

import org.roda.core.data.Task;
import org.roda.core.data.adapter.ContentAdapter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.RegexFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.SortParameter;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.ControlPanel;
import org.roda.wui.common.client.widgets.ElementPanel;
import org.roda.wui.common.client.widgets.LazyVerticalList;
import org.roda.wui.common.client.widgets.ListHeaderPanel;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.ControlPanel.ControlPanelListener;
import org.roda.wui.common.client.widgets.LazyVerticalList.ContentSource;
import org.roda.wui.common.client.widgets.LazyVerticalList.LazyVerticalListListener;
import org.roda.wui.management.event.client.CreateTask.CreateTaskListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementConstants;
import config.i18n.client.EventManagementMessages;

/**
 * Panel to manage all tasks
 * 
 * @author Luis Faria
 * 
 */
public class TaskList {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static EventManagementConstants constants = (EventManagementConstants) GWT
    .create(EventManagementConstants.class);

  private static EventManagementMessages messages = (EventManagementMessages) GWT.create(EventManagementMessages.class);

  /**
   * Filter to use in the task list
   */
  public static enum TaskFilter {
    /**
     * Show only tasks that are scheduled
     */
    SCHEDULED,

    /**
     * Show only paused tasks
     */
    PAUSED,

    /**
     * Show only tasks that are running
     */
    RUNNING,

    /**
     * Show only tasks that are NOT running
     */
    NOT_RUNNING, /**
     * Show all tasks
     */
    ALL
  }

  private boolean initialized;

  private DockPanel layout;

  private LazyVerticalList<Task> taskList;

  private ControlPanel controlPanel;

  private TaskFilter stateFilter;

  private String searchFilter;

  private WUIButton addTask;

  private WUIButton editTask;

  private WUIButton removeTask;

  private WUIButton pauseTask;

  private WUIButton resumeTask;

  /**
   * Create a new Task list panel
   */
  public TaskList() {
    layout = new DockPanel();
    initialized = false;
  }

  /**
   * Initialize task list
   */
  public void init() {
    if (!initialized) {

      initialized = true;

      controlPanel = new ControlPanel(constants.controlPanelTitle(), constants.controlPanelSearchTitle());

      controlPanel.addOption(constants.optionScheduled());
      controlPanel.addOption(constants.optionPaused());
      controlPanel.addOption(constants.optionRunning());
      controlPanel.addOption(constants.optionNotRunning());
      controlPanel.addOption(constants.optionAll());

      controlPanel.setSelectedOptionIndex(0);
      stateFilter = TaskFilter.SCHEDULED;
      searchFilter = "";

      controlPanel.addControlPanelListener(new ControlPanelListener() {

        public void onOptionSelected(int option) {
          switch (option) {
            case 0:
              stateFilter = TaskFilter.SCHEDULED;
              break;
            case 1:
              stateFilter = TaskFilter.PAUSED;
              break;
            case 2:
              stateFilter = TaskFilter.RUNNING;
              break;
            case 3:
              stateFilter = TaskFilter.NOT_RUNNING;
              break;
            case 4:
              stateFilter = TaskFilter.ALL;
              break;
          }
          update();
        }

        public void onSearch(String keywords) {
          if (keywords.length() > 0) {
            searchFilter = ".*(?i)" + keywords + ".*";
          } else {
            searchFilter = "";
          }
          update();
        }

      });

      addTask = new WUIButton(constants.actionAddTask(), WUIButton.Left.SQUARE, WUIButton.Right.PLUS);

      editTask = new WUIButton(constants.actionEditTask(), WUIButton.Left.SQUARE, WUIButton.Right.ARROW_FORWARD);

      removeTask = new WUIButton(constants.actionRemoveTask(), WUIButton.Left.SQUARE, WUIButton.Right.CROSS);

      pauseTask = new WUIButton(constants.actionPauseTask(), WUIButton.Left.SQUARE, WUIButton.Right.ARROW_FORWARD);

      resumeTask = new WUIButton(constants.actionResumeTask(), WUIButton.Left.SQUARE, WUIButton.Right.ARROW_FORWARD);

      addTask.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          CreateTask createTask = new CreateTask();
          createTask.show();
          createTask.addCreateTaskListener(new CreateTaskListener() {

            public void onCancel() {
              // nothing to do
            }

            public void onCreate(Task task) {
              taskList.update();
              updateVisibles();
            }

          });
        }

      });

      editTask.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          ElementPanel<Task> selected = taskList.getSelected();
          if (selected != null && selected instanceof TaskPanel) {
            TaskPanel selectedTaskPanel = (TaskPanel) selected;
            selectedTaskPanel.edit();
          }

        }

      });

      removeTask.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          ElementPanel<Task> selected = taskList.getSelected();
          if (selected != null && selected instanceof TaskPanel) {
            TaskPanel selectedTaskPanel = (TaskPanel) selected;
            EventManagementService.Util.getInstance().removeTask(selectedTaskPanel.get().getName(),
              new AsyncCallback<Void>() {

                public void onFailure(Throwable caught) {
                  logger.error("Error removing task", caught);
                }

                public void onSuccess(Void result) {
                  taskList.update();
                  updateVisibles();

                }
              });

          }
        }

      });

      pauseTask.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          ElementPanel<Task> selected = taskList.getSelected();
          if (selected != null && selected instanceof TaskPanel) {
            TaskPanel selectedTaskPanel = (TaskPanel) selected;
            EventManagementService.Util.getInstance().pauseTask(selectedTaskPanel.get().getName(),
              new AsyncCallback<Task>() {

                public void onFailure(Throwable caught) {
                  logger.error("Error removing task", caught);
                }

                public void onSuccess(Task result) {
                  update();
                }
              });
          }

        }

      });

      resumeTask.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          ElementPanel<Task> selected = taskList.getSelected();
          if (selected != null && selected instanceof TaskPanel) {
            TaskPanel selectedTaskPanel = (TaskPanel) selected;
            EventManagementService.Util.getInstance().resumeTask(selectedTaskPanel.get().getName(),
              new AsyncCallback<Task>() {

                public void onFailure(Throwable caught) {
                  logger.error("Error removing task", caught);
                }

                public void onSuccess(Task result) {
                  update();
                }
              });
          }

        }

      });

      controlPanel.addActionButton(addTask);
      controlPanel.addActionButton(editTask);
      controlPanel.addActionButton(removeTask);
      controlPanel.addActionButton(pauseTask);
      controlPanel.addActionButton(resumeTask);

      taskList = new LazyVerticalList<Task>(new ContentSource<Task>() {

        public void getCount(Filter filter, AsyncCallback<Integer> callback) {
          EventManagementService.Util.getInstance().getTaskCount(filter, callback);
        }

        public ElementPanel<Task> getElementPanel(Task element) {
          return new TaskPanel(element);
        }

        public void getElements(ContentAdapter adapter, AsyncCallback<Task[]> callback) {
          EventManagementService.Util.getInstance().getTasks(adapter, callback);
        }

        public String getTotalMessage(int total) {
          return messages.taskListTotal(total);
        }

        public void setReportInfo(ContentAdapter adapter, String locale, AsyncCallback<Void> callback) {
          EventManagementService.Util.getInstance().setTaskListReportInfo(adapter, locale, callback);

        }
      }, 30000, getFilter());

      taskList.addLazyVerticalListListener(new LazyVerticalListListener<Task>() {

        public void onElementSelected(ElementPanel<Task> element) {
          updateVisibles();
        }

        public void onUpdateBegin() {
          controlPanel.setOptionsEnabled(false);
        }

        public void onUpdateFinish() {
          controlPanel.setOptionsEnabled(true);
        }

      });

      addTaskListHeaders();

      layout.add(taskList.getWidget(), DockPanel.CENTER);
      layout.add(controlPanel.getWidget(), DockPanel.EAST);

      updateVisibles();

      layout.addStyleName("wui-management-event");
      taskList.getWidget().addStyleName("event-task-list");

    }
  }

  private Filter getFilter() {
    Filter filter = new Filter();
    if (TaskFilter.SCHEDULED.equals(stateFilter)) {
      filter.add(new SimpleFilterParameter("scheduled", Boolean.TRUE.toString()));
    } else if (TaskFilter.PAUSED.equals(stateFilter)) {
      filter.add(new SimpleFilterParameter("paused", Boolean.TRUE.toString()));
    } else if (TaskFilter.RUNNING.equals(stateFilter)) {
      filter.add(new SimpleFilterParameter("running", Boolean.TRUE.toString()));
    } else if (TaskFilter.NOT_RUNNING.equals(stateFilter)) {
      filter.add(new SimpleFilterParameter("running", Boolean.FALSE.toString()));
    }

    if (searchFilter != null && searchFilter.length() > 0) {
      filter.add(new RegexFilterParameter("name", searchFilter));
    }

    return filter;
  }

  protected void update() {
    taskList.setFilter(getFilter());
    taskList.reset();
    updateVisibles();
  }

  protected void updateVisibles() {
    ElementPanel<Task> selected = taskList.getSelected();
    if (selected != null) {
      editTask.setEnabled(true);
      removeTask.setEnabled(true);
      pauseTask.setEnabled(!selected.get().isPaused());
      resumeTask.setEnabled(selected.get().isPaused());
    } else {
      editTask.setEnabled(false);
      removeTask.setEnabled(false);
      pauseTask.setEnabled(false);
      resumeTask.setEnabled(false);
    }
  }

  private void addTaskListHeaders() {
    ListHeaderPanel taskListHeader = taskList.getHeader();

    taskListHeader.addHeader("", "task-list-header-scheduled", new SortParameter[] {}, true);

    taskListHeader.addHeader(constants.taskHeaderName(), "task-list-header-name",
      new SortParameter[] {new SortParameter("name", false)}, true);

    taskListHeader.addHeader(constants.taskHeaderStartDate(), "task-list-header-startDate", new SortParameter[] {
      new SortParameter("startDate", false), new SortParameter("name", false)}, false);

    taskListHeader.addHeader(constants.taskHeaderRepeat(), "task-list-header-repeat", new SortParameter[] {
      new SortParameter("repeatCount", false), new SortParameter("repeatInterval", false),
      new SortParameter("name", false)}, true);

    taskListHeader.addHeader(constants.taskHeaderUsername(), "task-list-header-user", new SortParameter[] {
      new SortParameter("username", false), new SortParameter("name", false)}, true);

    taskListHeader.addHeader("", "task-list-header-running", new SortParameter[] {new SortParameter("running", false),
      new SortParameter("name", false)}, true);

    taskListHeader.setSelectedHeader(1);
    taskListHeader.setFillerHeader(3);

  }

  /**
   * Get the panel widget
   * 
   * @return get task list widget
   */
  public Widget getWidget() {
    return layout;
  }
}
