package org.roda.wui.client.browse;

import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

public class DetailsPanelPreservationEvent extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FlowPanel detailsPanel;

  @UiField
  FlowPanel outcomeDetailsSection;

  @UiField
  FlowPanel outcomeDetailsPanel;

  public DetailsPanelPreservationEvent(IndexedPreservationEvent event, String outcomeDetails) {
    initWidget(uiBinder.createAndBindUi(this));
    init(event, outcomeDetails);
  }

  private void init(IndexedPreservationEvent event, String outcomeDetails) {
    if (event == null) {
      return;
    }
    addIfNotBlank(detailsPanel, messages.preservationEventId(), event.getId());
    addIfNotBlank(detailsPanel, messages.preservationEventType(), event.getEventType());

    if (event.getEventDateTime() != null) {
      detailsPanel.add(buildField(messages.preservationEventDatetime(),
        new InlineHTML(SafeHtmlUtils.htmlEscape(Humanize.formatDateTime(event.getEventDateTime())))));
    }

    Widget outcomeWidget = buildOutcomeWidget(event.getEventOutcome());
    if (outcomeWidget != null) {
      detailsPanel.add(buildField(messages.preservationEventOutcome(), outcomeWidget));
    }

    if (StringUtils.isNotBlank(outcomeDetails)) {
      outcomeDetailsPanel.clear();
      outcomeDetailsPanel.add(new HTML(outcomeDetails));
      outcomeDetailsSection.setVisible(true);
    } else {
      outcomeDetailsSection.setVisible(false);
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

  private void addIfNotBlank(FlowPanel panel, String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      panel.add(buildField(label, new InlineHTML(SafeHtmlUtils.htmlEscape(value))));
    }
  }

  private FlowPanel buildField(String labelText, Widget valueWidget) {
    FlowPanel field = new FlowPanel();
    field.setStyleName("field");

    Label label = new Label(labelText);
    label.setStyleName("label");

    FlowPanel value = new FlowPanel();
    value.setStyleName("value");
    value.add(valueWidget);

    field.add(label);
    field.add(value);
    return field;
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelPreservationEvent> {
    Widget createAndBindUi(DetailsPanelPreservationEvent preservationEventPanel);
  }
}
