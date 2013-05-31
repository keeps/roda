/**
 * 
 */
package pt.gov.dgarq.roda.wui.ingest.list.client;

import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.IngestListConstants;

/**
 * @author Luis Faria
 * 
 */
public class IngestReportWindow extends WUIWindow {
	private static IngestListConstants constants = (IngestListConstants) GWT
			.create(IngestListConstants.class);

	private final ScrollPanel scroll;

	private final IngestReportPanel ingestReportPanel;

	private final WUIButton close;

	public IngestReportWindow(SIPState sip) {
		super(constants.ingestReportWindowTitle(), 600, 400);
		this.ingestReportPanel = new IngestReportPanel(sip);
		this.scroll = new ScrollPanel(ingestReportPanel.getWidget());
		this.setWidget(scroll);
		close = new WUIButton("CLOSE", WUIButton.Left.ROUND,
				WUIButton.Right.CROSS);
		close.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				IngestReportWindow.this.hide();
			}

		});

		addToBottom(close);

		scroll.addStyleName("wui-ingest-report-scroll");

	}

	public void update(SIPState sip) {
		ingestReportPanel.update(sip);

	}

}
