/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.statistics.client;

import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;

/**
 * @author Luis Faria
 * 
 */
public class SystemStatistics extends StatisticTab {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private DockPanel layout;
	private HTML link;
	private Frame iframe;

	/**
	 * Create a new System Statistics
	 */
	public SystemStatistics() {
		super();
		layout = new DockPanel();
		initWidget(layout);
		this.addStyleName("statistics-system");
	}

	protected boolean init() {
		boolean ret = false;
		if (super.init()) {
			link = new HTML("<a href='Munin/index.html' target='_blank'>"
					+ constants.systemStatisticsLink() + "</a>");

			iframe = new Frame("Munin/index.html");
			
			layout.add(link, DockPanel.NORTH);
			layout.add(iframe, DockPanel.CENTER);
			
			iframe.setWidth("100%");
			iframe.setHeight("450px");
			
			layout.setCellHorizontalAlignment(link, HasAlignment.ALIGN_RIGHT);

			link.addStyleName("statistics-system-link");
			iframe.addStyleName("statistics-system-frame");
		}
		return ret;
	}


	@Override
	public String getTabText() {
		return constants.systemStatistics();
	}
}
