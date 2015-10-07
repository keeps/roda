/**
 * 
 */
package org.roda.wui.management.event.client;

import org.roda.core.data.Task;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.ElementPanel;
import org.roda.wui.management.event.client.EditTask.EditTaskListener;
import org.roda.wui.management.event.client.images.EventManagementImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class TaskPanel extends ElementPanel<Task> {

  private static EventManagementImageBundle images = (EventManagementImageBundle) GWT
    .create(EventManagementImageBundle.class);

  private static EventManagementMessages messages = (EventManagementMessages) GWT.create(EventManagementMessages.class);

  private final HorizontalPanel layout;

  private final Image scheduledState;

  private final Label name;

  private final Label startDate;

  private final Label repeat;

  private final Label username;

  private final Image runningState;

  private EditTask editTask;

  /**
   * Create a new task panel
   * 
   * @param task
   */
  public TaskPanel(Task task) {
    super(task);
    layout = new HorizontalPanel();
    scheduledState = new Image();
    name = new Label();
    startDate = new Label();
    repeat = new Label();
    username = new Label();
    runningState = new Image();

    this.setWidget(layout);

    layout.add(scheduledState);
    layout.add(name);
    layout.add(startDate);
    layout.add(repeat);
    layout.add(username);
    layout.add(runningState);

    layout.setCellWidth(repeat, "100%");

    name.addStyleName("task-name");
    startDate.addStyleName("task-startDate");
    repeat.addStyleName("task-repeat");
    username.addStyleName("task-user");
    runningState.addStyleName("task-state-running");
    scheduledState.addStyleName("task-state-scheduled");

    update(task);

    this.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        setSelected(true);
      }

    });

    editTask = null;

    this.setStylePrimaryName("wui-task");
  }

  protected void update(Task task) {
    if (task.isPaused()) {
      images.taskPaused().applyTo(scheduledState);
    } else if (task.isScheduled()) {
      images.taskScheduled().applyTo(scheduledState);
    } else {
      images.taskSuspended().applyTo(scheduledState);
    }

    name.setText(task.getName());
    name.setTitle(task.getDescription());

    startDate.setText(Tools.formatDateTime(task.getStartDate()));
    repeat.setText(getRepeatMessage(task.getRepeatCount(), task.getRepeatInterval()));

    username.setText(task.getUsername());

    if (task.isRunning()) {
      images.taskInstanceRunning().applyTo(runningState);
    } else {
      images.taskInstanceStopped().applyTo(runningState);
    }

    runningState.addStyleName("task-state-running");
    scheduledState.addStyleName("task-state-scheduled");
  }

  protected String getRepeatMessage(int repeatCount, long interval) {
    String ret;
    if (repeatCount == -1 && interval == 0) {
      ret = messages.repeatContinuouslyForever();
    } else if (repeatCount == 0) {
      ret = messages.runOnce();
    } else if (interval == 0) {
      ret = messages.repeatContinuously(repeatCount);
    } else if (interval % 31557600 == 0) {
      if (repeatCount == -1) {
        ret = messages.repeatForeverYears(interval / 31557600);
      } else {
        ret = messages.repeatYears(repeatCount, interval / 31557600);
      }
    } else if (interval % 2678400 == 0) {
      if (repeatCount == -1) {
        ret = messages.repeatForeverMonths(interval / 2678400);
      } else {
        ret = messages.repeatMonths(repeatCount, interval / 2678400);
      }
    } else if (interval % 86400 == 0) {
      if (repeatCount == -1) {
        ret = messages.repeatForeverDays(interval / 86400);
      } else {
        ret = messages.repeatDays(repeatCount, interval / 86400);
      }
    } else if (interval % 3600 == 0) {
      if (repeatCount == -1) {
        ret = messages.repeatForeverHours(interval / 3600);
      } else {
        ret = messages.repeatHours(repeatCount, interval / 3600);
      }
    } else if (interval % 60 == 0) {
      if (repeatCount == -1) {
        ret = messages.repeatForeverMinutes(interval / 60);
      } else {
        ret = messages.repeatMinutes(repeatCount, interval / 60);
      }
    } else /* SECONDS */{
      if (repeatCount == -1) {
        ret = messages.repeatForeverSeconds(interval);
      } else {
        ret = messages.repeatSeconds(repeatCount, interval);
      }
    }

    return ret;
  }

  /**
   * Edit task
   */
  public void edit() {
    if (editTask == null) {
      editTask = new EditTask(get());
      editTask.addEditTaskListener(new EditTaskListener() {

        public void onCancel() {
          // nothing to do
        }

        public void onSave(Task task) {
          set(task);
        }

      });
    }
    editTask.show();
  }

}
