package org.roda.wui.client.management.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.tools.Humanize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NotificationDetailsPanel extends GenericMetadataCardPanel<Notification> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public NotificationDetailsPanel(Notification notification) {
    setData(notification);
  }

  @Override
  protected FlowPanel createHeaderWidget(Notification notification) {
    return null;
  }

  @Override
  protected void buildFields(Notification data) {
    buildField(messages.notificationIdentifier()).withValue(data.getId()).build();

    buildField(messages.notificationSubject()).withValue(data.getSubject()).build();

    // Map the custom widget and apply the pre-code CSS styles natively via the
    // builder
    buildField(messages.notificationBody()).withWidget(buildNotificationBody(data.getBody())).asPreCode().build();

    buildField(messages.notificationSentOn())
      .withValue(SafeHtmlUtils.htmlEscape(Humanize.formatDateTime(data.getSentOn()))).build();

    buildField(messages.notificationFromUser()).withValue(data.getFromUser()).build();

    buildField(messages.notificationIsAcknowledged())
      .withValue(messages.isAcknowledged(Boolean.toString(data.isAcknowledged()).toLowerCase())).build();

    buildField(messages.notificationState()).withHtml(HtmlSnippetUtils.getNotificationStateHTML(data.getState()))
      .build();

    if (data.getAcknowledgedUsers() != null && !data.getAcknowledgedUsers().isEmpty()) {
      addSeparator(messages.notificationAcknowledgedUsers());

      FlowPanel list = new FlowPanel();
      list.addStyleName("generic-multiline");

      for (Map.Entry<String, String> e : data.getAcknowledgedUsers().entrySet()) {
        String convertedDate = Humanize.convertStringToStrictUTC(e.getValue());
        list.add(new HTMLPanel("span", SafeHtmlUtils.htmlEscape(e.getKey() + " @ " + convertedDate)));
      }

      buildField("Users").withWidget(list).build();
    }

    List<String> remaining = new ArrayList<>();
    if (data.getRecipientUsers() != null) {
      remaining.addAll(data.getRecipientUsers());
    }

    if (data.getAcknowledgedUsers() != null) {
      remaining.removeAll(data.getAcknowledgedUsers().keySet());
    }

    if (!remaining.isEmpty()) {
      addSeparator(messages.notificationNotAcknowledgedUsers());
      FlowPanel list = new FlowPanel();
      list.addStyleName("generic-multiline");

      for (String user : remaining) {
        list.add(new HTMLPanel("span", SafeHtmlUtils.htmlEscape(user)));
      }

      buildField("Users").withWidget(list).build();
    }
  }

  // ==========================================
  // HELPER METHODS
  // ==========================================

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
}