package pt.gov.dgarq.roda.wui.ingest.list.client;

import java.util.Map;

import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.widgets.LoadingPopup;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.CommonConstants;
import config.i18n.client.IngestListConstants;
import config.i18n.client.IngestListMessages;

/**
 * 
 * @author Luis Faria
 * 
 */
public class AcceptMessageWindow extends WUIWindow {

	private static CommonConstants commonConstants = (CommonConstants) GWT
			.create(CommonConstants.class);

	private static IngestListConstants constants = (IngestListConstants) GWT
			.create(IngestListConstants.class);

	private static IngestListMessages messages = (IngestListMessages) GWT
			.create(IngestListMessages.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private final SIPState sip;

	private final DockPanel layout;

	private final HorizontalPanel header;
	private final Label templatesLabel;
	private final ListBox templatesList;
	private final TextArea message;
	private final WUIButton accept;
	private final WUIButton cancel;

	private final LoadingPopup loading;

	/**
	 * Create new accept message window
	 * 
	 * @param sip
	 */
	public AcceptMessageWindow(SIPState sip) {
		super(messages.acceptMessageWindowTitle(sip.getOriginalFilename()),
				500, 250);
		this.sip = sip;

		layout = new DockPanel();
		loading = new LoadingPopup(layout);

		header = new HorizontalPanel();
		templatesLabel = new Label(constants.acceptMessageWindowTemplates()
				+ ":");
		templatesList = new ListBox();
		templatesList.setVisibleItemCount(1);
		templatesList.addItem("");
		updateTemplates();

		templatesList.addChangeListener(new ChangeListener() {

			public void onChange(Widget sender) {
				String value = templatesList.getValue(templatesList
						.getSelectedIndex());
				if (value.length() > 0) {
					message.setText(value);
				}
			}

		});

		message = new TextArea();

		accept = new WUIButton(constants.acceptMessageWindowAccept(),
				WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

		accept.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				accept.setEnabled(false);
				cancel.setEnabled(false);
				loading.show();
				IngestListService.Util.getInstance().acceptSIP(
						AcceptMessageWindow.this.sip.getId(),
						message.getText(), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Window.alert(caught.getMessage());
								logger.error("Error accepting SIP", caught);
								accept.setEnabled(false);
								cancel.setEnabled(true);
								loading.hide();
								AcceptMessageWindow.this.onCancel();
								hide();
							}

							public void onSuccess(Void result) {
								AcceptMessageWindow.this.onSuccess();
								loading.hide();
								hide();
							}
						});

			}

		});

		cancel = new WUIButton(constants.acceptMessageWindowCancel(),
				WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				onCancel();
				hide();

			}

		});

		header.add(templatesLabel);
		header.add(templatesList);

		layout.add(header, DockPanel.NORTH);
		layout.add(message, DockPanel.CENTER);

		setWidget(layout);

		addToBottom(accept);
		addToBottom(cancel);

		layout.addStyleName("wui-ingest-accept-window");
		header.addStyleName("accept-window-header");
		templatesLabel.addStyleName("accept-window-templates-header");
		templatesList.addStyleName("accept-window-templates-list");
		message.addStyleName("accept-window-message");

	}

	private void updateTemplates() {
		IngestListService.Util.getInstance().getAcceptMessageTemplates(
				commonConstants.locale(),
				new AsyncCallback<Map<String, String>>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting accept message templates",
								caught);
					}

					public void onSuccess(Map<String, String> result) {
						for (Map.Entry<String, String> entry : result
								.entrySet()) {
							templatesList.addItem(entry.getKey(), entry
									.getValue());
						}
						templatesList.setSelectedIndex(1);
						message.setText(templatesList.getValue(1));

					}

				});

	}
}
