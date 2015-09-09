/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.event.client;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.Task;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class EditTask extends WUIWindow {

  private static EventManagementConstants constants = (EventManagementConstants) GWT
    .create(EventManagementConstants.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  /**
   * Edit task listener
   * 
   */
  public interface EditTaskListener {

    /**
     * On task modified
     * 
     * @param task
     *          the modified task
     */
    public void onSave(Task task);

    /**
     * On task edition cancellation
     */
    public void onCancel();
  }

  private final ScrollPanel scroll;
  private final TaskDataPanel taskData;
  private final WUIButton cancel;
  private final WUIButton save;

  private final List<EditTaskListener> editTaskListeners;

  /**
   * Edit Task panel constructor
   * 
   * @param task
   *          task to be edited
   */
  public EditTask(Task task) {
    super(constants.editTask(), 600, 500);

    taskData = new TaskDataPanel();
    taskData.setTask(task);
    scroll = new ScrollPanel(taskData.getWidget());

    cancel = new WUIButton(constants.editTaskCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

    save = new WUIButton(constants.editTaskSave(), WUIButton.Left.ROUND, WUIButton.Right.REC);

    cancel.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        onCancel();
        hide();
      }

    });

    save.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        if (taskData.isValid()) {
          Task task = taskData.getTask();
          EventManagementService.Util.getInstance().modifyTask(task, new AsyncCallback<Task>() {

            public void onFailure(Throwable caught) {
              logger.error("Error creating task", caught);
            }

            public void onSuccess(Task newTask) {
              onEdit(newTask);
              hide();
            }

          });
        } else {
          Window.alert(constants.editTaskNotValid());
        }

      }

    });

    setWidget(scroll);
    addToBottom(cancel);
    addToBottom(save);

    editTaskListeners = new ArrayList<EditTaskListener>();

    scroll.addStyleName("wui-task-edit-scroll");
  }

  /**
   * Add a create task listener
   * 
   * @param listener
   */
  public void addEditTaskListener(EditTaskListener listener) {
    editTaskListeners.add(listener);
  }

  /**
   * Remove a create task listener
   * 
   * @param listener
   */
  public void removeEditTaskListener(EditTaskListener listener) {
    editTaskListeners.remove(listener);
  }

  protected void onEdit(Task task) {
    for (EditTaskListener listener : editTaskListeners) {
      listener.onSave(task);
    }
  }

  protected void onCancel() {
    for (EditTaskListener listener : editTaskListeners) {
      listener.onCancel();
    }
  }

}
