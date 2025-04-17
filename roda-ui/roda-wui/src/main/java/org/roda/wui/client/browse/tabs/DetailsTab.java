package org.roda.wui.client.browse.tabs;

import java.util.List;

import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.ingest.transfer.DetailsPanelTransferredResource;
import org.roda.wui.client.planning.DetailsPanelAIP;
import org.roda.wui.client.planning.DetailsPanelFile;
import org.roda.wui.client.planning.DetailsPanelRepresentation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsTab extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static DetailsTab.MyUiBinder uiBinder = GWT.create(DetailsTab.MyUiBinder.class);

  @UiField
  FlowPanel content;

  public DetailsTab(BrowseAIPResponse response) {
    initWidget(uiBinder.createAndBindUi(this));
    // Get metadata and populate widget
    DetailsPanelAIP detailsPanel = new DetailsPanelAIP(response);
    content.add(detailsPanel);
  }

  public DetailsTab(BrowseRepresentationResponse response) {
    initWidget(uiBinder.createAndBindUi(this));
    // Get metadata and populate widget
    DetailsPanelRepresentation detailsPanel = new DetailsPanelRepresentation(response);
    content.add(detailsPanel);
  }

  public DetailsTab(IndexedFile file, List<String> riRules) {
    initWidget(uiBinder.createAndBindUi(this));
    // Get metadata and populate widget
    DetailsPanelFile detailsPanel = new DetailsPanelFile(file, riRules);
    content.add(detailsPanel);
  }

  public DetailsTab(TransferredResource resource) {
    initWidget(uiBinder.createAndBindUi(this));

    DetailsPanelTransferredResource detailsPanel = new DetailsPanelTransferredResource(resource);
    content.add(detailsPanel);
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsTab> {
  }
}
