package pt.gov.dgarq.roda.wui.management.statistics.client;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Luis Faria
 * 
 */
public class EventStatistics extends StatisticTab {

	private VerticalPanel layout;
	private StatisticMiniPanel taskCount;
	private StatisticMiniPanel taskState;
	private StatisticMiniPanel instanceCount;
	private StatisticMiniPanel instanceState;

	/**
	 * Create new repository statistics
	 */
	public EventStatistics() {
		layout = new VerticalPanel();
		initWidget(layout);
	}

	@Override
	protected boolean init() {
		boolean ret = false;
		if (super.init()) {
			ret = true;
			taskCount = createStatisticPanel(constants.taskCountTitle(),
					constants.taskCountDesc(), "tasks", false, AGGREGATION_LAST);

			taskState = createStatisticPanel(constants.taskStateTitle(),
					constants.taskStateDesc(), "tasks\\.state\\..*", true,
					AGGREGATION_LAST);

			instanceCount = createStatisticPanel(constants
					.taskInstanceCountTitle(), constants
					.taskInstanceCountDesc(), "instances", false,
					AGGREGATION_LAST);

			instanceState = createStatisticPanel(constants
					.taskInstanceStateTitle(), constants
					.taskInstanceStateDesc(), "instances\\.state\\..*", true,
					AGGREGATION_LAST);

			layout.add(taskCount);
			layout.add(taskState);
			layout.add(instanceCount);
			layout.add(instanceState);

			layout.addStyleName("wui-statistics-events");

		}
		return ret;

	}

	@Override
	public String getTabText() {
		return constants.repositoryStatistics();
	}

}
