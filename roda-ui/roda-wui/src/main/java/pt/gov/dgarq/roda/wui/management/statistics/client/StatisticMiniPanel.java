/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import java.util.List;

/**
 * @author Luis Faria
 * 
 */
public abstract class StatisticMiniPanel extends StatisticsPanel {

	private final String description;
	private final List<StatisticFunction> reportFunctions;
	private StatisticReportPanel reportPanel = null;

	/**
	 * Create a new statistic result panel
	 * 
	 * @param title
	 *            statistic panel title
	 * 
	 * @param description
	 *            a text explaining what this statistic is about
	 * @param type
	 *            statistic type or regexp of types
	 * @param dimension
	 *            the dimension (or unit) of the value
	 * @param functions
	 *            statistic functions
	 * @param segmentation
	 */
	public StatisticMiniPanel(String title, String description, String type,
			ValueDimension dimension, Segmentation segmentation,
			List<StatisticFunction> functions,
			List<StatisticFunction> reportFunctions) {
		super(title, type, dimension, segmentation, functions);
		this.description = description;
		this.reportFunctions = reportFunctions;
	}

	/**
	 * Show complete report for this statistic in a window
	 */
	public void showReport() {
		if (reportPanel == null) {
			reportPanel = getReportPanel();
		} else {
			reportPanel.update();
		}

		reportPanel.show();

	}

	protected abstract StatisticReportPanel getReportPanel();

	/**
	 * Get statistic mini panel description
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	protected List<StatisticFunction> getReportFunctions() {
		return reportFunctions;
	}

}
