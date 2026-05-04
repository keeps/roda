package org.roda.wui.client.ingest.transfer.tabs;

import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.tools.Humanize;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class TransferredResourceDetailsPanel extends GenericMetadataCardPanel<TransferredResource> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public TransferredResourceDetailsPanel(TransferredResource resource) {
    setData(resource);
  }

  @Override
  protected FlowPanel createHeaderWidget(TransferredResource data) {
    return null;
  }

  @Override
  protected void buildFields(TransferredResource resource) {
    buildField(messages.transferredResourceName()).withValue(resource.getName()).build();
    buildField(messages.transferredResourceSize()).withValue(Humanize.readableFileSize(resource.getSize())).build();
    buildField(messages.transferredResourceDateCreated()).withValue(Humanize.formatDateTime(resource.getCreationDate())).build();
    buildField(messages.transferredResourceLastScannedAt()).withValue(Humanize.formatDateTime(resource.getLastScanDate())).build();
  }
}
