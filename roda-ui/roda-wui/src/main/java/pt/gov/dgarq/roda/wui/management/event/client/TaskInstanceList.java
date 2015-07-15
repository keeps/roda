/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.event.client;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.EventManagementConstants;
import config.i18n.client.EventManagementMessages;
import pt.gov.dgarq.roda.core.data.TaskInstance;
import pt.gov.dgarq.roda.wui.common.client.widgets.ControlPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.ControlPanel.ControlPanelListener;
import pt.gov.dgarq.roda.wui.common.client.widgets.ElementPanel;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList.ContentSource;
import pt.gov.dgarq.roda.wui.common.client.widgets.LazyVerticalList.LazyVerticalListListener;
import pt.gov.dgarq.roda.wui.common.client.widgets.ListHeaderPanel;

/**
 * Panel to manage all task instances
 * 
 * @author Luis Faria
 * 
 */
public class TaskInstanceList {
	// private ClientLogger logger = new ClientLogger(getClass().getName());

	private static EventManagementConstants constants = (EventManagementConstants) GWT
			.create(EventManagementConstants.class);

	private static EventManagementMessages messages = (EventManagementMessages) GWT
			.create(EventManagementMessages.class);

	/**
	 * Filter to use in the instance list
	 */
	public static enum TaskInstanceFilter {
		/**
		 * Only show running instances
		 */
		RUNNING,
		/**
		 * Only show stopped instances
		 */
		STOPPED,
		/**
		 * Show all instances
		 */
		ALL
	}

	private boolean initialized;

	private DockPanel layout;

	private LazyVerticalList<TaskInstance> instanceList;

	private ControlPanel controlPanel;

	private TaskInstanceFilter stateFilter;

	private String searchFilter;

	/**
	 * Create a new Task Instance list panel
	 */
	public TaskInstanceList() {
		initialized = false;
		layout = new DockPanel();
	}

	/**
	 * Initialize task instance list
	 */
	public void init() {
		if (!initialized) {
			initialized = true;

			controlPanel = new ControlPanel(constants.controlPanelTitle(),
					constants.controlPanelSearchTitle());

			controlPanel.addOption(constants.optionRunning());
			controlPanel.addOption(constants.optionStopped());
			controlPanel.addOption(constants.optionAll());

			controlPanel.setSelectedOptionIndex(1);
			stateFilter = TaskInstanceFilter.STOPPED;

			controlPanel.addControlPanelListener(new ControlPanelListener() {

				public void onOptionSelected(int option) {
					switch (option) {
					case 0:
						stateFilter = TaskInstanceFilter.RUNNING;
						break;
					case 1:
						stateFilter = TaskInstanceFilter.STOPPED;
						break;
					case 2:
						stateFilter = TaskInstanceFilter.ALL;
						break;
					}

					update();
				}

				public void onSearch(String keywords) {
					searchFilter = keywords;
					update();
				}

			});

			instanceList = new LazyVerticalList<TaskInstance>(
					new ContentSource<TaskInstance>() {

						public void getCount(Filter filter,
								AsyncCallback<Integer> callback) {
							EventManagementService.Util.getInstance()
									.getTaskInstanceCount(filter, callback);
						}

						public ElementPanel<TaskInstance> getElementPanel(
								TaskInstance elementPanel) {
							return new TaskInstancePanel(elementPanel);
						}

						public void getElements(ContentAdapter adapter,
								AsyncCallback<TaskInstance[]> callback) {
							EventManagementService.Util.getInstance()
									.getTaskInstances(adapter, callback);

						}

						public String getTotalMessage(int total) {
							return messages.instanceListTotal(total);
						}

						public void setReportInfo(ContentAdapter adapter,
								String locale, AsyncCallback<Void> callback) {
							EventManagementService.Util.getInstance()
									.setInstanceListReportInfo(adapter, locale,
											callback);

						}
					}, 30000, getFilter());

			instanceList
					.addLazyVerticalListListener(new LazyVerticalListListener<TaskInstance>() {

						public void onElementSelected(
								ElementPanel<TaskInstance> element) {
							updateVisibles();

						}

						public void onUpdateBegin() {
							controlPanel.setOptionsEnabled(false);

						}

						public void onUpdateFinish() {
							controlPanel.setOptionsEnabled(true);
						}
					});
			addInstanceListHeaders();

			layout.add(instanceList.getWidget(), DockPanel.CENTER);
			layout.add(controlPanel.getWidget(), DockPanel.EAST);

			updateVisibles();

			layout.addStyleName("wui-management-event");
			instanceList.getWidget().addStyleName("event-instance-list");

		}
	}

	private Filter getFilter() {
		Filter filter = new Filter();
		if (TaskInstanceFilter.RUNNING.equals(stateFilter)) {
			filter.add(new SimpleFilterParameter("state",
					TaskInstance.STATE_RUNNING));
		} else if (TaskInstanceFilter.STOPPED.equals(stateFilter)) {
			filter.add(new SimpleFilterParameter("state",
					TaskInstance.STATE_STOPPED));
		}

		if (searchFilter != null && searchFilter.length() > 0) {
			filter
					.add(new LikeFilterParameter("name", "%" + searchFilter
							+ "%"));
		}

		return filter;
	}

	protected void update() {
		instanceList.setFilter(getFilter());
		instanceList.reset();
	}

	protected void updateVisibles() {

	}

	private void addInstanceListHeaders() {
		ListHeaderPanel instanceListHeader = instanceList.getHeader();

		instanceListHeader.addHeader("", "instance-header-running",
				new SortParameter[] { new SortParameter("state", false),
						new SortParameter("id", false) }, true);

		instanceListHeader.addHeader(constants.instanceHeaderName(),
				"instance-header-name", new SortParameter[] {
						new SortParameter("name", false),
						new SortParameter("id", false) }, true);

		instanceListHeader.addHeader(constants.instanceHeaderStartDate(),
				"instance-header-startDate", new SortParameter[] {
						new SortParameter("startDate", false),
						new SortParameter("id", false) }, false);

		instanceListHeader.addHeader(constants.instanceHeaderCompleteness(),
				"instance-header-completeness", new SortParameter[] {
						new SortParameter("completePercentage", true),
						new SortParameter("finishDate", false),
						new SortParameter("id", false) }, true);

		instanceListHeader.addHeader(constants.instanceHeaderUser(),
				"instance-header-user", new SortParameter[] {
						new SortParameter("username", false),
						new SortParameter("id", false) }, true);

		instanceListHeader.addHeader("", "instance-header-report",
				new SortParameter[] {}, true);

		instanceListHeader.setSelectedHeader(2);
		instanceListHeader.setFillerHeader(4);

	}

	/**
	 * Get the panel widget
	 * 
	 * @return the widget
	 */
	public Widget getWidget() {
		return layout;
	}
}
