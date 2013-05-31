/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import config.i18n.client.AdvancedSearchConstants;

/**
 * @author Luis Faria
 * 
 */
public class MonthPicker extends ListBox {

	/**
	 * The type of the month
	 * 
	 */
	public static enum MonthType {
		/**
		 * 31 days month
		 */
		MONTH_31_DAYS,
		/**
		 * 30 days month
		 */
		MONTH_30_DAYS,
		/**
		 * 29 and 28 days month
		 */
		FEBRUARY
	}

	private static AdvancedSearchConstants constants = (AdvancedSearchConstants) GWT
			.create(AdvancedSearchConstants.class);

	// private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

	/**
	 * Create a new month picker
	 */
	public MonthPicker() {
		this.setVisibleItemCount(1);
		this.setEnabled(false);
		init();
		this.addStyleName("monthPicker");
	}

	protected void init() {
		addItem(constants.january());
		addItem(constants.february());
		addItem(constants.march());
		addItem(constants.april());
		addItem(constants.may());
		addItem(constants.june());
		addItem(constants.july());
		addItem(constants.august());
		addItem(constants.september());
		addItem(constants.october());
		addItem(constants.november());
		addItem(constants.december());
		setSelectedIndex(0);
	}

	/**
	 * Get selected month
	 * 
	 * @return
	 * the month number, between 1 and 12
	 */
	public int getSelectedInt() {
		return getSelectedIndex() + 1;
	}

	/**
	 * Get selected month
	 * 
	 * @return
	 */
	public String getSelected() {
		int month = getSelectedInt();
		return month < 10 ? "0" + month : "" + month;
	}

	/**
	 * Set selected month
	 * 
	 * @param month
	 * the month in the year, from 1 to 12
	 */
	public void setSelected(int month) {
		setSelectedIndex(month - 1);
	}

	/**
	 * Get selected month type
	 * 
	 * @return
	 */
	public MonthType getSelectedMonthType() {
		int month = getSelectedInt();
		MonthType type;
		switch (month) {
		case 4:
		case 6:
		case 9:
		case 10:
			type = MonthType.MONTH_30_DAYS;
			break;
		case 2:
			type = MonthType.FEBRUARY;
			break;
		default:
			type = MonthType.MONTH_31_DAYS;
			break;
		}
		return type;
	}

}
