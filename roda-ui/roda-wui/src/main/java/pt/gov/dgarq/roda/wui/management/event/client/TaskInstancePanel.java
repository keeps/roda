/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.event.client;

import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;
import pt.gov.dgarq.roda.wui.common.client.widgets.ElementPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.ReportWindow;
import pt.gov.dgarq.roda.wui.management.event.client.images.EventManagementImageBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
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
public class TaskInstancePanel extends ElementPanel<TaskInstance> {

	private static EventManagementImageBundle images = (EventManagementImageBundle) GWT
			.create(EventManagementImageBundle.class);

	private static CommonImageBundle commonImages = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private static EventManagementMessages messages = (EventManagementMessages) GWT
			.create(EventManagementMessages.class);

	private final HorizontalPanel layout;
	private Image runningState;
	private Label name;
	private Label startDate;
	private Label percentage;
	private Label username;
	private Image report;
	private ReportWindow reportWindow;

	/**
	 * Create a new task panel
	 * 
	 * @param taskInstance
	 */
	public TaskInstancePanel(TaskInstance taskInstance) {
		super(taskInstance);

		layout = new HorizontalPanel();
		this.setWidget(layout);

		runningState = new Image();
		name = new Label();
		startDate = new Label();
		percentage = new Label();
		username = new Label();
		report = commonImages.report().createImage();
		reportWindow = null;

		report.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if (reportWindow == null) {
					reportWindow = new ReportWindow(get().getReportID());
				}
				reportWindow.show();
			}

		});

		layout.add(runningState);
		layout.add(name);
		layout.add(startDate);
		layout.add(percentage);
		layout.add(username);
		layout.add(report);

		update(taskInstance);

		layout.setCellWidth(username, "100%");

		this.setStylePrimaryName("wui-task-instance");
		runningState.addStyleName("task-state-running");
		name.addStyleName("instance-name");
		startDate.addStyleName("instance-startDate");
		percentage.addStyleName("instance-percentage");
		username.addStyleName("instance-user");
		report.addStyleName("instance-report");
	}

	protected void update(TaskInstance taskInstance) {
		if (taskInstance.getState().equals(TaskInstance.STATE_RUNNING)) {
			images.taskInstanceRunning().applyTo(runningState);
		} else if (taskInstance.getState().equals(TaskInstance.STATE_PAUSED)) {
			images.taskInstancePaused().applyTo(runningState);
		} else /* if STATE_STOPPED */{
			images.taskInstanceStopped().applyTo(runningState);
		}

		name.setText(taskInstance.getName());
		name.setTitle(taskInstance.getDescription());

		startDate.setText(Tools.formatDateTime(taskInstance.getStartDate()));

		if (taskInstance.getFinishDate() == null) {
			percentage.setText(messages.percentage(taskInstance
					.getCompletePercentage()));
		} else {
			percentage.setText(Tools.formatDateTime(taskInstance
					.getFinishDate()));
		}

		username.setText(taskInstance.getUsername());

		report.setVisible(taskInstance.getReportID() != null);

	}

}
