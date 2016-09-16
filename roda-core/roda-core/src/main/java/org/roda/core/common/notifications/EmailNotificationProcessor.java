/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.notifications;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConfigurableEmailUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class EmailNotificationProcessor implements NotificationProcessor {
  private static final String FROM = "from";
  private static final String RECIPIENT = "recipient";
  private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationProcessor.class);
  private Map<String, Object> scope;
  private String templateName;

  public EmailNotificationProcessor(String template) {
    this.scope = new HashMap<String, Object>();
    this.templateName = template;
  }

  public EmailNotificationProcessor(String template, Map<String, Object> scope) {
    this.scope = scope;
    this.templateName = template;
  }

  @Override
  public Notification processNotification(ModelService model, final Notification notification) {
    Notification processedNotification = new Notification(notification);
    try {
      List<String> recipients = notification.getRecipientUsers();
      String templatePath = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "template_path");
      InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(templatePath + templateName);
      String template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
      IOUtils.closeQuietly(templateStream);
      if (!scope.containsKey(FROM)) {
        scope.put(FROM, notification.getFromUser());
      }
      if (recipients.size() == 1) {
        scope.put(RECIPIENT, recipients.get(0));
      } else {
        scope.put(RECIPIENT, RodaConstants.NOTIFICATION_VARIOUS_RECIPIENT_USERS);
      }
      processedNotification.setBody(executeHandlebars(template, scope));
      scope.remove(RECIPIENT);
      ConfigurableEmailUtility emailUtility = new ConfigurableEmailUtility(notification.getFromUser(),
        notification.getSubject());
      for (String recipient : recipients) {
        String modifiedBody = getUpdatedMessageBody(model, notification, recipient, template, scope);
        String host = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "host");
        if (StringUtils.isNotBlank(host)) {
          LOGGER.debug("Sending email ...");
          emailUtility.sendMail(recipient, modifiedBody);
          LOGGER.debug("Email sent");
          processedNotification.setState(NOTIFICATION_STATE.COMPLETED);
        } else {
          processedNotification.setState(NOTIFICATION_STATE.FAILED);
          LOGGER.debug("SMTP not defined, cannot send emails");
        }
      }
    } catch (IOException | MessagingException e) {
      processedNotification.setState(NOTIFICATION_STATE.FAILED);
      LOGGER.debug("Error sending e-mail: {}", e.getMessage());
    }
    return processedNotification;
  }

  private String getUpdatedMessageBody(ModelService model, Notification notification, String recipient, String template,
    Map<String, Object> scopes) {

    // update body message with the recipient user and acknowledge URL
    String userUUID = UUID.nameUUIDFromBytes(recipient.getBytes()).toString();
    String ackUrl = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "acknowledge");
    ackUrl = ackUrl.replaceAll("\\{notificationId\\}", notification.getId());
    ackUrl = ackUrl.replaceAll("\\{token\\}", notification.getAcknowledgeToken() + userUUID);
    ackUrl = ackUrl.replaceAll("\\{email\\}", recipient);

    scopes.put("acknowledge", ackUrl);
    scopes.put(RECIPIENT, recipient);

    try {
      User user = model.retrieveUserByEmail(recipient);
      if (user != null) {
        scopes.put(RECIPIENT, user.getName());
      }
    } catch (GenericException e) {
      // do nothing
    }

    return executeHandlebars(template, scopes);
  }

  private String executeHandlebars(String template, Map<String, Object> scopes) {
    Handlebars handlebars = new Handlebars();
    String result = "";
    try {
      Template templ = handlebars.compileInline(template);
      result = templ.apply(scopes);
    } catch (IOException e) {
      LOGGER.error("Error executing handlebars", e);
    }
    return result;
  }

}
