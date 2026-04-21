package org.roda.wui.client.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.HTML;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
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
public class DetailsPanelNotification extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final MyUiBinder uiBinder = GWT.create(DetailsPanelNotification.MyUiBinder.class);

  @UiField
  FlowPanel details;

  public DetailsPanelNotification(Notification resource) {
    initWidget(uiBinder.createAndBindUi(this));
    init(resource);
  }

  public void init(Notification n) {
    addIfNotBlank(messages.notificationIdentifier(), n.getId());
    addIfNotBlank(messages.notificationSubject(), n.getSubject());

    if (StringUtils.isNotBlank(n.getBody())) {
      details.add(buildBodyField(messages.notificationBody(), n.getBody()));
    }

    if (n.getSentOn() != null) {
      details.add(buildField(messages.notificationSentOn(),
        new InlineHTML(SafeHtmlUtils.htmlEscape(Humanize.formatDateTime(n.getSentOn())))));
    }

    addIfNotBlank(messages.notificationFromUser(), n.getFromUser());
    addIfNotBlank(messages.notificationIsAcknowledged(),
      messages.isAcknowledged(Boolean.toString(n.isAcknowledged()).toLowerCase()));

    if (n.getState() != null) {
      details.add(buildField(messages.notificationState(),
        new InlineHTML(HtmlSnippetUtils.getNotificationStateHTML(n.getState()))));
    }

    if (n.getAcknowledgedUsers() != null && !n.getAcknowledgedUsers().isEmpty()) {
      FlowPanel ack = new FlowPanel();
      for (Map.Entry<String, String> e : n.getAcknowledgedUsers().entrySet()) {
        ack.add(new InlineHTML(SafeHtmlUtils.htmlEscape(e.getKey() + " " + e.getValue())));
      }
      details.add(buildField(messages.notificationAcknowledgedUsers(), ack));
    }

    List<String> remaining = new ArrayList<>();
    if (n.getRecipientUsers() != null) {
      remaining.addAll(n.getRecipientUsers());
    }

    if (n.getAcknowledgedUsers() != null) {
      remaining.removeAll(n.getAcknowledgedUsers().keySet());
    }
    if (!remaining.isEmpty()) {
      FlowPanel notAck = new FlowPanel();
      for (String user : remaining) {
        notAck.add(new InlineHTML(SafeHtmlUtils.htmlEscape(user)));
      }
      details.add(buildField(messages.notificationNotAcknowledgedUsers(), notAck));
    }
  }

  private void addIfNotBlank(String label, String value) {
    if (StringUtils.isNotBlank(value)) {
      details.add(buildField(label, new InlineHTML(SafeHtmlUtils.htmlEscape(value))));
    }
  }

  private FlowPanel buildField(String label, Widget valueWidget) {
    FlowPanel fieldPanel = new FlowPanel();
    fieldPanel.setStyleName("field");

    Label fieldLabel = new Label(label);
    fieldLabel.setStyleName("label");

    FlowPanel fieldValuePanel = new FlowPanel();
    fieldValuePanel.setStyleName("value");
    fieldValuePanel.add(valueWidget);

    fieldPanel.add(fieldLabel);
    fieldPanel.add(fieldValuePanel);

    return fieldPanel;
  }

  private FlowPanel buildBodyField(String label, String rawBody) {
    FlowPanel bodyPanel = new FlowPanel();
    bodyPanel.setStyleName("field");

    Label fieldLabel = new Label(label);
    fieldLabel.setStyleName("label");

    FlowPanel fieldValuePanel = new FlowPanel();
    fieldValuePanel.setStyleName("value");
    fieldValuePanel.addStyleName("code-pre");
    fieldValuePanel.addStyleName("notification-body-content");

    fieldValuePanel.add(buildNotificationBody(rawBody));

    bodyPanel.add(fieldLabel);
    bodyPanel.add(fieldValuePanel);

    return bodyPanel;
  }

  private Widget buildNotificationBody(String rawBody) {
    String body = rawBody == null ? "" : rawBody.trim();

    if (body.isEmpty()) {
      return new InlineHTML("");
    }

    if (isJson(body)) {
      return buildHighlightedCodeBlock(body, "json");
    }

    if (isHtml(body)) {
      return buildHighlightedCodeBlock(body, "html");
    }

    return new HTML("<pre>" + SafeHtmlUtils.htmlEscape(body) + "</pre>");
  }

  private boolean isJson(String body) {
    try {
      JSONParser.parseStrict(body);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isHtml(String body) {
    String s = body == null ? "" : body.trim().toLowerCase();
    return s.contains("<html") || s.contains("<body") || s.contains("<div") || s.contains("<p") || s.contains("<h1")
            || s.contains("<h2") || s.contains("<ul") || s.contains("<table") || s.contains("<a ") || s.contains("<style");
  }

  private HTML buildHighlightedCodeBlock(String body, String language) {
    String escaped = SafeHtmlUtils.htmlEscape(body);
    HTML codeHtml = new HTML("<pre><code class=\"language-" + language + "\">" + escaped + "</code></pre>");
    codeHtml.addAttachHandler(event -> {
      if (event.isAttached()) {
        JavascriptUtils.runHighlighter(codeHtml.getElement());
      }
    });
    return codeHtml;
  }

  interface MyUiBinder extends UiBinder<Widget, DetailsPanelNotification> {
    Widget createAndBindUi(DetailsPanelNotification detailsPanelNotification);
  }
}
