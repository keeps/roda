package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobReportDetailsPanel extends GenericMetadataCardPanel<IndexedReport> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public JobReportDetailsPanel(IndexedReport report) {
    setData(report);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedReport data) {
    return null;
  }

  @Override
  protected void buildFields(IndexedReport data) {

    // TODO: i18n
    addSeparator("Information");

    buildField(messages.jobInstanceId()).withValue(data.getInstanceId()).build();

    buildField(messages.reportJob()).withValue(data.getJobName()).build();

    buildSourceDetails(data);
    buildOutcomeDetails(data);
    buildField(messages.jobStartDate()).withValue(Humanize.formatDateTime(data.getDateCreated())).build();
    buildField(messages.jobEndDate()).withValue(Humanize.formatDateTime(data.getDateUpdated())).build();
    buildField(messages.reportDuration())
      .withValue(Humanize.durationInDHMS(data.getDateCreated(), data.getDateUpdated(), Humanize.DHMSFormat.LONG))
      .build();
    buildField(messages.reportStatus()).withHtml(HtmlSnippetUtils.getPluginStateHTML(data.getPluginState())).build();
    if (data.getIngestType() != null) {
      buildField(messages.reportIngestType())
        .withValue(data.getIngestType().equals("NEW") ? messages.newIngestion() : messages.ingestionUpdate()).build();
    }
    buildField(messages.reportProgress())
      .withValue(
        messages.showJobReportProgress(data.getCompletionPercentage(), data.getStepsCompleted(), data.getTotalSteps()))
      .build();
  }

  private void buildSourceDetails(IndexedReport jobReport) {
    boolean hasSource = !jobReport.getSourceObjectOriginalIds().isEmpty() || !jobReport.getSourceObjectId().isEmpty();

    String value = null;
    if (hasSource) {
      String idText = !jobReport.getSourceObjectOriginalIds().isEmpty()
        ? " (" + StringUtils.prettyPrint(jobReport.getSourceObjectOriginalIds()) + ")"
        : "";

      if (StringUtils.isNotBlank(jobReport.getSourceObjectOriginalName())) {
        value = jobReport.getSourceObjectOriginalName() + idText;
      } else if (StringUtils.isNotBlank(jobReport.getSourceObjectLabel())) {
        value = jobReport.getSourceObjectLabel() + idText;
      } else if (StringUtils.isNotBlank(jobReport.getSourceObjectId())) {
        value = jobReport.getSourceObjectId() + idText;
      }
    }

    buildField(messages.jobReportSource(jobReport.getSourceObjectClass())).withValue(value).build();
  }

  private void buildOutcomeDetails(IndexedReport jobReport) {
    boolean hasOutcome = StringUtils.isNotBlank(jobReport.getOutcomeObjectId())
      && !jobReport.getOutcomeObjectId().equals(jobReport.getSourceObjectId());
    String outcomeValue = null;
    if (hasOutcome) {
      if (jobReport.getOutcomeObjectLabel() != null) {
        outcomeValue = jobReport.getOutcomeObjectLabel();
      } else if (jobReport.getOutcomeObjectId() != null) {
        outcomeValue = jobReport.getOutcomeObjectId();
      }

      buildField(messages.jobReportOutcome(jobReport.getOutcomeObjectClass())).withValue(outcomeValue)
        .withInlineBadge(HtmlSnippetUtils.getAIPStateHTML(jobReport.getOutcomeObjectState())).build();
    }
  }
}
