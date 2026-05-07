package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.common.client.tools.HistoryUtils;
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

    addSeparator("Information");
    buildField(messages.reportJob()).withValue(data.getJobName())
      .onClick(event -> HistoryUtils.newHistory(ShowJob.RESOLVER, data.getJobId())).build();
    test(data);
  }

  private void test(IndexedReport jobReport) {
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

    GWT.log(jobReport.getSourceObjectClass() + " - " + jobReport.getSourceObjectId());

    buildField(messages.jobReportSource(jobReport.getSourceObjectClass())).withValue(value)
      .onClick(event -> HistoryUtils.newHistory(
        HistoryUtils.getHistoryUuidResolver(jobReport.getSourceObjectClass(), jobReport.getSourceObjectId())))
      .build();

  }
}
