package org.roda.wui.client.ingest.transfer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.common.client.tools.Humanize;

public class DetailsPanelTransferredResource extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel details;

  public DetailsPanelTransferredResource(TransferredResource resource) {
    initWidget(uiBinder.createAndBindUi(this));
    init(resource);
  }

  public void init(TransferredResource resource) {

    if (resource.isFile()) {
      details.add(buildField("Size", new InlineHTML(Humanize.readableFileSize(resource.getSize()))));
    }
    details.add(buildField("Created at", new InlineHTML(Humanize.formatDateTime(resource.getCreationDate()))));
    details.add(buildField("Last scanned at", new InlineHTML(Humanize.formatDateTime(resource.getLastScanDate()))));
  }

  private FlowPanel buildField(String label, InlineHTML html) {
    FlowPanel fieldPanel = new FlowPanel();
    fieldPanel.setStyleName("field");

    Label fieldLabel = new Label(label);
    fieldLabel.setStyleName("label");

    FlowPanel fieldValuePanel = new FlowPanel();
    fieldValuePanel.setStyleName("value");
    fieldValuePanel.add(html);

    fieldPanel.add(fieldLabel);
    fieldPanel.add(fieldValuePanel);

    return fieldPanel;
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelTransferredResource> {
    Widget createAndBindUi(DetailsPanelTransferredResource detailsPanelTransferredResource);
  }
}
