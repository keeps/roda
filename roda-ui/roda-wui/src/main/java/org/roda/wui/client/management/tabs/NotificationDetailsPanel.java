package org.roda.wui.client.management.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
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
    super();
    setData(notification);
  }

  @Override
  public void setData(Notification data) {
    // 1. Clear any existing fields in case setData is called multiple times
    metadataContainer.clear();

    if (data == null) {
      return;
    }

    addFieldIfNotNull(messages.notificationIdentifier(), data.getId());
    addFieldIfNotNull(messages.notificationSubject(), data.getSubject());
    addPreCodeFieldIfNotNull(messages.notificationBody(), buildNotificationBody(data.getBody()));
    addFieldIfNotNull(messages.notificationSentOn(),
            SafeHtmlUtils.htmlEscape(Humanize.formatDateTime(data.getSentOn())));
    addFieldIfNotNull(messages.notificationFromUser(), data.getFromUser());
    addFieldIfNotNull(messages.notificationIsAcknowledged(),
            messages.isAcknowledged(Boolean.toString(data.isAcknowledged()).toLowerCase()));

    addFieldIfNotNull(messages.notificationState(), HtmlSnippetUtils.getNotificationStateHTML(data.getState()));

    if (data.getAcknowledgedUsers() != null && !data.getAcknowledgedUsers().isEmpty()) {
      addSeparator(messages.notificationAcknowledgedUsers());

      SafeHtmlBuilder ackUsersHtml = new SafeHtmlBuilder();
      boolean first = true;
      for (Map.Entry<String, String> e : data.getAcknowledgedUsers().entrySet()) {
        if (!first) {
          ackUsersHtml.appendHtmlConstant("<br/>");
        }

        String convertedDate = Humanize.convertStringToStrictUTC(e.getValue());

        ackUsersHtml.appendEscaped(e.getKey() + " @ " + convertedDate);
        first = false;
      }
      addFieldIfNotNull("Users", ackUsersHtml.toSafeHtml());
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

      SafeHtmlBuilder notAckUsersHtml = new SafeHtmlBuilder();
      boolean first = true;
      for (String user : remaining) {
        if (!first) {
          notAckUsersHtml.appendHtmlConstant("<br/>");
        }
        // appendEscaped handles the HTML escaping securely
        notAckUsersHtml.appendEscaped(user);
        first = false;
      }
      addFieldIfNotNull("Users", notAckUsersHtml.toSafeHtml());
    }
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
}
