package org.roda.wui.client.ingest.process;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.client.common.panels.GenericCollapsibleCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class JobReportItemPanel extends GenericCollapsibleCardPanel<Report> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public JobReportItemPanel(Report reportItem) {
    super(!PluginState.FAILURE.equals(reportItem.getPluginState()));
    // This triggers defineHeader and buildFields automatically
    setData(reportItem);
  }

  @Override
  protected void defineHeader(Report data) {
    // Determine the title (fallback to plugin name if title is empty)
    String headerTitle = data.getTitle();

    // Use the HeaderBuilder to recreate the layout from your image
    buildHeader(headerTitle).withBadge(HtmlSnippetUtils.getPluginMandatoryHTML(data.getPluginIsMandatory()))
      .withBadge(HtmlSnippetUtils.getPluginStateHTML(data.getPluginState())).build();
  }

  @Override
  protected void buildFields(Report data) {
    // Add the specific metadata fields you want to show when expanded
    GenericCollapsibleCardPanel<Report>.FieldBuilder agentFieldBuilder = buildField(messages.reportAgent());
    if (StringUtils.isNotBlank(data.getPluginVersion())) {
      agentFieldBuilder.withValue(messages.pluginLabelWithVersion(data.getPlugin(), data.getPluginVersion()));
    } else {
      agentFieldBuilder.withValue(messages.pluginLabel(data.getPlugin()));
    }
    agentFieldBuilder.build();

    if (data.getDateCreated() != null) {
      buildField(messages.jobStartDate()).withValue(Humanize.formatDateTime(data.getDateCreated())).build();
    }

    if (data.getDateUpdated() != null) {
      buildField(messages.jobEndDate()).withValue(Humanize.formatDateTime(data.getDateUpdated())).build();
    }

    buildField(messages.reportOutcome()).withHtml(HtmlSnippetUtils.getPluginStateHTML(data.getPluginState())).build();

    if (data.getPluginDetails() != null && !"".equals(data.getPluginDetails())) {
      GenericCollapsibleCardPanel<Report>.FieldBuilder fieldBuilder = buildField(messages.reportOutcomeDetails());
      if (data.isHtmlPluginDetails()) {
        fieldBuilder.withHtml(SafeHtmlUtils.fromTrustedString(data.getPluginDetails()));
      } else {
        fieldBuilder.withValue(data.getPluginDetails());
      }

      fieldBuilder.asPreCode().build();
    }
  }
}