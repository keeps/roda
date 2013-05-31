/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Report panel
 * 
 * @author Luis Faria
 * 
 */
public class ReportPanel extends Composite {

	private Report report;
	private VerticalPanel layout;
	private FlexTable attributes;
	private VerticalPanel items;

	/**
	 * Create report panel
	 * 
	 * @param report
	 */
	public ReportPanel(Report report) {
		this.report = report;

		layout = new VerticalPanel();
		attributes = new FlexTable();
		items = new VerticalPanel();

		layout.add(attributes);
		layout.add(items);

		updateAttributes();
		updateItems();

		initWidget(layout);

		this.addStyleName("wui-report");
		layout.addStyleName("report-layout");
		attributes.addStyleName("report-attributes");
		items.addStyleName("report-items");
	}

	private void updateAttributes() {
		int row = 0;
		for (Attribute attrb : report.getAttributes()) {
			Label name = new Label(attrb.getName());
			Label value = new Label(attrb.getValue());

			attributes.setWidget(row, 0, name);
			attributes.setWidget(row, 1, value);
			
			attributes.getCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
			name.addStyleName("attribute-name");
			value.addStyleName("attribute-value");
			
			row++;
		}
	}

	private void updateItems() {
		for (ReportItem item : report.getItems()) {
			Widget reportItemPanel = createReportItemPanel(item);
			items.add(reportItemPanel);
		}
	}

	private Widget createReportItemPanel(ReportItem item) {
		DisclosurePanel disclosure = new DisclosurePanel(item.getTitle());
		FlexTable attributes = new FlexTable();
		int row = 0;
		for (Attribute attrb : item.getAttributes()) {
			Label name = new Label(attrb.getName());
			Label value = new Label(attrb.getValue());

			attributes.setWidget(row, 0, name);
			attributes.setWidget(row, 1, value);			

			attributes.getCellFormatter().setVerticalAlignment(row, 0, HasAlignment.ALIGN_TOP);
			name.addStyleName("attribute-name");
			value.addStyleName("attribute-value");
			
			row++;
		}
		
		disclosure.setContent(attributes);
		disclosure.addStyleName("report-item");
		attributes.addStyleName("report-item-attributes");
		return disclosure;
	}

	/**
	 * Get report
	 * 
	 * @return
	 */
	public Report getReport() {
		return report;
	}

}
