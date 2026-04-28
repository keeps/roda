/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.tabs;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.wui.client.browse.DetailsPanelPreservationEvent;
import org.roda.wui.client.common.model.BrowseAIPResponse;
import org.roda.wui.client.common.model.BrowseRepresentationResponse;
import org.roda.wui.client.planning.DetailsPanelRepresentationInformation;
import org.roda.wui.client.management.DetailsPanelNotification;
import org.roda.wui.client.management.DetailsPanelLogEntry;
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
import org.roda.wui.client.planning.DetailsPanelRisk;

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

  public DetailsTab(RepresentationInformation ri){
    initWidget(uiBinder.createAndBindUi(this));

    DetailsPanelRepresentationInformation detailsPanel = new DetailsPanelRepresentationInformation(ri);
    content.add(detailsPanel);
  }

  public DetailsTab(TransferredResource resource) {
    initWidget(uiBinder.createAndBindUi(this));

    DetailsPanelTransferredResource detailsPanel = new DetailsPanelTransferredResource(resource);
    content.add(detailsPanel);
  }

  public DetailsTab(IndexedRisk risk) {
    initWidget(uiBinder.createAndBindUi(this));

    DetailsPanelRisk detailsPanel = new DetailsPanelRisk(risk, "RiskShowPanel_riskIncidences");
    content.add(detailsPanel);
  }

  public DetailsTab(Notification notification) {
    initWidget(uiBinder.createAndBindUi(this));
    DetailsPanelNotification detailsPanel = new DetailsPanelNotification(notification);
    content.add(detailsPanel);
  }

  public DetailsTab(LogEntry logEntry) {
    initWidget(uiBinder.createAndBindUi(this));
    DetailsPanelLogEntry detailsPanel = new DetailsPanelLogEntry(logEntry);
    content.add(detailsPanel);
  }

  public DetailsTab(IndexedPreservationEvent event, SafeHtml outcomeDetails){
    initWidget(uiBinder.createAndBindUi(this));
    DetailsPanelPreservationEvent detailsPanel = new DetailsPanelPreservationEvent(event, outcomeDetails);
    content.add(detailsPanel);
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsTab> {
  }
}
