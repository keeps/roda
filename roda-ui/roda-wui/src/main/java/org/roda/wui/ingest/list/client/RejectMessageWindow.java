package org.roda.wui.ingest.list.client;

import java.util.Map;

import org.roda.core.data.SIPState;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.widgets.LoadingPopup;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
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
public class RejectMessageWindow extends WUIWindow {

  private static CommonConstants commonConstants = (CommonConstants) GWT.create(CommonConstants.class);

  private static IngestListConstants constants = (IngestListConstants) GWT.create(IngestListConstants.class);

  private static IngestListMessages messages = (IngestListMessages) GWT.create(IngestListMessages.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final SIPState sip;

  private final DockPanel layout;

  private final HorizontalPanel header;
  private final Label templatesLabel;
  private final ListBox templatesList;
  private final TextArea message;
  private final CheckBox notifyProducer;
  private final WUIButton reject;
  private final WUIButton cancel;

  private final LoadingPopup loading;

  /**
   * Create new reject message window
   * 
   * @param sip
   */
  public RejectMessageWindow(SIPState sip) {
    super(messages.rejectMessageWindowTitle(sip.getOriginalFilename()), 500, 250);
    this.sip = sip;

    layout = new DockPanel();
    loading = new LoadingPopup(layout);

    header = new HorizontalPanel();
    templatesLabel = new Label(constants.rejectMessageWindowTemplates() + ":");
    templatesList = new ListBox();
    templatesList.setVisibleItemCount(1);
    templatesList.addItem("");
    updateTemplates();
    templatesList.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        String value = templatesList.getValue(templatesList.getSelectedIndex());
        if (value.length() > 0) {
          message.setText(value);
        }
      }

    });

    message = new TextArea();

    notifyProducer = new CheckBox(constants.rejectMessageWindowNotifyProducer());

    reject = new WUIButton(constants.rejectMessageWindowReject(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_FORWARD);

    reject.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        reject.setEnabled(false);
        cancel.setEnabled(false);
        loading.show();
        IngestListService.Util.getInstance().rejectSIP(RejectMessageWindow.this.sip.getId(), message.getText(),
          notifyProducer.isChecked(), new AsyncCallback<Void>() {

            public void onFailure(Throwable caught) {
              logger.error("Error rejecting SIP", caught);
              reject.setEnabled(true);
              cancel.setEnabled(true);
              loading.hide();
            }

            public void onSuccess(Void result) {
              RejectMessageWindow.this.onSuccess();
              loading.hide();
              hide();
            }
          });

      }

    });

    cancel = new WUIButton(constants.rejectMessageWindowCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

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
    layout.add(notifyProducer, DockPanel.SOUTH);

    setWidget(layout);

    addToBottom(reject);
    addToBottom(cancel);

    layout.addStyleName("wui-ingest-reject-window");
    header.addStyleName("reject-window-header");
    templatesLabel.addStyleName("reject-window-templates-header");
    templatesList.addStyleName("reject-window-templates-list");
    message.addStyleName("reject-window-message");
    notifyProducer.addStyleName("reject-window-notifyProducer");

  }

  private void updateTemplates() {
    IngestListService.Util.getInstance().getRejectMessageTemplates(commonConstants.locale(),
      new AsyncCallback<Map<String, String>>() {

        public void onFailure(Throwable caught) {
          logger.error("Error getting reject message templates", caught);
        }

        public void onSuccess(Map<String, String> result) {
          for (Map.Entry<String, String> entry : result.entrySet()) {
            templatesList.addItem(entry.getKey(), entry.getValue());
          }

          templatesList.setSelectedIndex(0);
        }

      });

  }
}
