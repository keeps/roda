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
public class CreateTask extends WUIWindow {

	private static EventManagementConstants constants = (EventManagementConstants) GWT
			.create(EventManagementConstants.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	/**
	 * Create task listener
	 * 
	 */
	public interface CreateTaskListener {

		/**
		 * On task created
		 * 
		 * @param task
		 *            the created task
		 */
		public void onCreate(Task task);

		/**
		 * On task creation cancellation
		 */
		public void onCancel();
	}

	private final ScrollPanel scroll;
	private final TaskDataPanel taskData;
	private final WUIButton cancel;
	private final WUIButton create;

	private final List<CreateTaskListener> createTaskListeners;

	/**
	 * Create Task panel constructor
	 */
	public CreateTask() {
		super(constants.createTask(), 600, 500);

		taskData = new TaskDataPanel();
		taskData.setSelectedPlugin(0);

		scroll = new ScrollPanel(taskData.getWidget());

		cancel = new WUIButton(constants.createTaskCancel(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		create = new WUIButton(constants.createTaskCreate(),
				WUIButton.Left.ROUND, WUIButton.Right.PLUS);

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				onCancel();
				hide();
			}

		});

		create.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if (taskData.isValid()) {
					Task task = taskData.getTask();
					EventManagementService.Util.getInstance().addTask(task,
							new AsyncCallback<Task>() {

								public void onFailure(Throwable caught) {
									logger.error("Error creating task", caught);
								}

								public void onSuccess(Task newTask) {
									onCreate(newTask);
									hide();
								}

							});
				} else {
					Window.alert(constants.createTaskNotValid());
				}

			}

		});

		setWidget(scroll);
		addToBottom(cancel);
		addToBottom(create);

		createTaskListeners = new ArrayList<CreateTaskListener>();

		scroll.addStyleName("wui-task-create-scroll");

	}

	/**
	 * Add a create task listener
	 * 
	 * @param listener
	 */
	public void addCreateTaskListener(CreateTaskListener listener) {
		createTaskListeners.add(listener);
	}

	/**
	 * Remove a create task listener
	 * 
	 * @param listener
	 */
	public void removeCreateTaskListener(CreateTaskListener listener) {
		createTaskListeners.remove(listener);
	}

	protected void onCreate(Task task) {
		for (CreateTaskListener listener : createTaskListeners) {
			listener.onCreate(task);
		}
	}

	protected void onCancel() {
		for (CreateTaskListener listener : createTaskListeners) {
			listener.onCancel();
		}
	}

}
