package org.roda.wui.client.management;

import org.roda.core.data.v2.log.LogEntry;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
public class DetailsPanelLogEntry extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel details;

  public DetailsPanelLogEntry(LogEntry logEntry) {
    initWidget(uiBinder.createAndBindUi(this));
    init(logEntry);
  }

  private void init(LogEntry logEntry) {
    addIfNotBlank(messages.logEntryIdentifier(), logEntry.getId());
    addIfNotBlank(messages.logEntryReason(),
      logEntry.getAuditLogRequestHeaders() != null ? logEntry.getAuditLogRequestHeaders().getReason() : null);
    addIfNotBlank(messages.logEntryInstanceId(), logEntry.getInstanceId());
    addIfNotBlank(messages.logEntryComponent(), logEntry.getActionComponent());
    addIfNotBlank(messages.logEntryMethod(), logEntry.getActionMethod());
    addIfNotBlank(messages.logEntryAddress(), logEntry.getAddress());

    if (logEntry.getDatetime() != null) {
      details.add(buildField(messages.logEntryDatetime(),
        new InlineHTML(SafeHtmlUtils.htmlEscape(Humanize.formatDateTime(logEntry.getDatetime())))));
    }

    details.add(buildField(messages.logEntryDuration(),
      new InlineHTML(SafeHtmlUtils.htmlEscape(Humanize.durationMillisToShortDHMS(logEntry.getDuration())))));

    addIfNotBlank(messages.logEntryRelatedObject(), logEntry.getRelatedObjectID());
    addIfNotBlank(messages.logEntryUsername(), logEntry.getUsername());

    if (logEntry.getParameters() != null && !logEntry.getParameters().isEmpty()) {
      String paramsInline = logEntry.getParameters().stream().map(p -> messages.logParameter(p.getName(), p.getValue()))
        .collect(java.util.stream.Collectors.joining(" | "));
      details.add(buildField(messages.logEntryParameters(), new InlineHTML(SafeHtmlUtils.htmlEscape(paramsInline))));
    }

    if (logEntry.getState() != null) {
      details.add(buildField(messages.logEntryState(),
        new InlineHTML(HtmlSnippetUtils.getLogEntryStateHtml(logEntry.getState()))));
    }
  }

  private void addIfNotBlank(String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      details.add(buildField(label, new InlineHTML(SafeHtmlUtils.htmlEscape(value))));
    }
  }

  private FlowPanel buildField(String labelText, InlineHTML html) {
    FlowPanel field = new FlowPanel();
    field.setStyleName("field");

    Label label = new Label(labelText);
    label.setStyleName("label");

    FlowPanel value = new FlowPanel();
    value.setStyleName("value");
    value.add(html);

    field.add(label);
    field.add(value);
    return field;
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelLogEntry> {
    Widget createAndBindUi(DetailsPanelLogEntry detailsPanelLogEntry);
  }

}
