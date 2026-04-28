package org.roda.wui.client.browse;

import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DetailsPanelPreservationEvent extends GenericMetadataCardPanel<IndexedPreservationEvent> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final String outcomeDetails;

  public DetailsPanelPreservationEvent(IndexedPreservationEvent event, String outcomeDetails) {
    this.outcomeDetails = outcomeDetails;
    setData(event);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedPreservationEvent preservationEvent) {
    return null;
  }

  @Override
  protected void buildFields(IndexedPreservationEvent event) {
    if (event == null) {
      return;
    }

    buildField(messages.preservationEventId()).withValue(event.getId()).build();
    buildField(messages.preservationEventType()).withValue(event.getEventType()).build();

    if (event.getEventDateTime() != null) {
      buildField(messages.preservationEventDatetime()).withValue(Humanize.formatDateTime(event.getEventDateTime()))
        .build();
    }

    Widget outcomeWidget = buildOutcomeWidget(event.getEventOutcome());
    if (outcomeWidget != null) {
      buildField(messages.preservationEventOutcome()).withWidget(outcomeWidget).build();
    }

    if (StringUtils.isNotBlank(outcomeDetails)) {
      InlineHTML outcomeDetailValue = new InlineHTML(SafeHtmlUtils.htmlEscape(outcomeDetails));
      outcomeDetailValue.addStyleName("code-pre");
      buildField(messages.preservationEventOutcomeDetailHeader()).withWidget(outcomeDetailValue).build();
    }
  }

  private Widget buildOutcomeWidget(String eventOutcome) {
    if (StringUtils.isBlank(eventOutcome)) {
      return null;
    }

    try {
      PluginState outcome = PluginState.valueOf(eventOutcome);
      String labelClass = "";

      if (PluginState.SUCCESS.equals(outcome)) {
        labelClass = "label-success";
      } else if (PluginState.FAILURE.equals(outcome)) {
        labelClass = "label-danger";
      } else if (PluginState.PARTIAL_SUCCESS.equals(outcome) || PluginState.SKIPPED.equals(outcome)) {
        labelClass = "label-warning";
      }

      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      builder.appendHtmlConstant("<span class='" + labelClass + "'>");
      builder.appendEscaped(messages.pluginStateMessage(outcome));
      builder.appendHtmlConstant("</span>");

      return new InlineHTML(builder.toSafeHtml());
    } catch (IllegalArgumentException e) {
      return new InlineHTML(SafeHtmlUtils.htmlEscape(eventOutcome));
    }
  }

}
