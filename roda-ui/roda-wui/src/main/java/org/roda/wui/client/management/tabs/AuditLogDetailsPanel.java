package org.roda.wui.client.management.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class AuditLogDetailsPanel extends GenericMetadataCardPanel<LogEntry> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public AuditLogDetailsPanel(LogEntry auditLog) {
    setData(auditLog);
  }

  @Override
  protected FlowPanel createHeaderWidget(LogEntry data) {
    return null;
  }

  @Override
  protected void buildFields(LogEntry data) {
    buildField(messages.logEntryIdentifier()).withValue(data.getId()).build();
    buildField(messages.logEntryReason())
      .withValue(data.getAuditLogRequestHeaders() != null ? data.getAuditLogRequestHeaders().getReason() : null)
      .build();
    buildField(messages.logEntryInstanceId()).withValue(data.getInstanceId()).build();
    buildField(messages.logEntryComponent()).withValue(data.getActionComponent()).build();
    buildField(messages.logEntryMethod()).withValue(data.getActionMethod()).build();
    buildField(messages.logEntryAddress()).withValue(data.getAddress()).build();

    buildField(messages.logEntryDatetime()).withValue(Humanize.formatDateTime(data.getDatetime())).build();
    buildField(messages.logEntryDuration()).withValue(Humanize.durationMillisToShortDHMS(data.getDuration())).build();
    buildField(messages.logEntryRelatedObject()).withValue(data.getRelatedObjectID()).build();
    buildField(messages.logEntryUsername()).withValue(data.getUsername()).build();

    if (data.getParameters() != null && !data.getParameters().isEmpty()) {
      String paramsInline = data.getParameters().stream().map(p -> messages.logParameter(p.getName(), p.getValue()))
        .collect(java.util.stream.Collectors.joining(" | "));
      buildField(messages.logEntryParameters()).withValue((SafeHtmlUtils.htmlEscape(paramsInline))).build();
    }

    if (data.getState() != null) {
      buildField(messages.logEntryState()).withHtml(HtmlSnippetUtils.getLogEntryStateHtml(data.getState())).build();
    }
  }
}
