package org.roda.wui.client.planning;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
public class DetailsPanelFile extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static DetailsPanelFile.MyUiBinder uiBinder = GWT.create(DetailsPanelFile.MyUiBinder.class);
  @UiField
  Label aipID;
  @UiField
  Label aipType;
  @UiField
  Label aipState;
  @UiField
  Label aipCreatedOn;
  @UiField
  Label aipCreatedBy;

  @UiField
  Label modifiedOn;

  @UiField
  Label modifiedBy;

  @UiField
  FlowPanel jobsPanel;

  @UiField
  FlowPanel identifiersPanel;

  public DetailsPanelFile(IndexedAIP aip, IndexedRepresentation representation, IndexedFile file) {
    initWidget(uiBinder.createAndBindUi(this));
    init(aip, representation, file);
  }

  public void init(IndexedAIP aip, IndexedRepresentation representation, IndexedFile file) {
    GWT.log("DetailsPanel init");

    aipID.setText(aip.getId());
    aipType.setText(aip.getType());

    aipState.setText(aip.getState().name());
    aipCreatedOn.setText(Humanize.formatDateTime(aip.getCreatedOn()));
    aipCreatedBy.setText(aip.getCreatedBy());

    modifiedOn.setText(Humanize.formatDateTime(aip.getUpdatedOn()));
    modifiedBy.setText(aip.getUpdatedBy());

    // SIP IDENTIFIERS

    for (String sipId : aip.getIngestSIPIds()) {
      Label identifierValue = new Label(sipId);
      identifierValue.setStyleName("value");
      identifierValue.addStyleName("details-uuid");
      identifiersPanel.add(identifierValue);
    }

    identifiersPanel.setStyleName("value");

    // AIP JOBS
    Services services = new Services("Browse AIP details tab information", "get");

    List<String> jobs = new ArrayList<>();

    jobs.add(aip.getIngestJobId());
    jobs.addAll(aip.getIngestUpdateJobIds());

    jobs.forEach(jobId -> {

      Anchor jobIdentifier = new Anchor();
      services.jobsResource(s -> s.getJobFromModel(jobId)).whenComplete((job, error) -> {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        jobIdentifier.setHTML(b.appendEscaped(job.getName()).appendHtmlConstant(" <span class='details-date'>")
          .appendHtmlConstant(Humanize.formatDateTime(job.getStartDate())).appendHtmlConstant("</span>").toSafeHtml());
      });

      jobIdentifier.setHref(HistoryUtils.createHistoryHashLink(ShowJob.RESOLVER, jobId,
        RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, aip.getId()));
      jobIdentifier.setStyleName("value");
      jobIdentifier.addStyleName("details-anchor");
      jobsPanel.add(jobIdentifier);
    });

    jobsPanel.setStyleName("value");

  }

  public void clear() {
    aipID.setText("");
    aipType.setText("");
    aipState.setText("");
    aipCreatedOn.setText("");
    aipCreatedBy.setText("");
    modifiedOn.setText("");
    modifiedBy.setText("");
    jobsPanel.clear();
    identifiersPanel.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelFile> {
    Widget createAndBindUi(DetailsPanelFile detailsPanel);
  }

}
