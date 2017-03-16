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

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConfigurableEmailUtility;
import org.roda.core.common.HandlebarsUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.IdUtils;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailNotificationProcessor implements NotificationProcessor {
  private static final String FROM = "from";
  private static final String RECIPIENT = "recipient";
  private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationProcessor.class);
  private Map<String, Object> scope;
  private String templateName;
  private String localeString;

  public EmailNotificationProcessor(String templateName) {
    this.scope = new HashMap<>();
    this.templateName = templateName;
  }

  public EmailNotificationProcessor(String templateName, Map<String, Object> scope) {
    this.templateName = templateName;
    this.scope = scope;
  }

  public EmailNotificationProcessor(String templateName, Map<String, Object> scope, String localeString) {
    this.templateName = templateName;
    this.scope = scope;
    this.localeString = localeString;
  }

  @Override
  public Notification processNotification(ModelService model, final Notification notification) {
    Notification processedNotification = new Notification(notification);
    try {
      List<String> recipients = processedNotification.getRecipientUsers();
      String templatePath = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "template_path");
      String templateCompletePath = templatePath + templateName;
      InputStream templateStream;
      if (localeString != null) {
        String localeTemplateCompletePath = templateCompletePath.replace(RodaConstants.EMAIL_TEMPLATE_EXTENSION,
          "_" + localeString + RodaConstants.EMAIL_TEMPLATE_EXTENSION);
        templateStream = RodaCoreFactory.getConfigurationFileAsStream(localeTemplateCompletePath, templateCompletePath);
      } else {
        templateStream = RodaCoreFactory.getConfigurationFileAsStream(templateCompletePath);
      }
      String template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
      IOUtils.closeQuietly(templateStream);
      if (!scope.containsKey(FROM)) {
        scope.put(FROM, processedNotification.getFromUser());
      }
      if (recipients.size() == 1) {
        scope.put(RECIPIENT, recipients.get(0));
      } else {
        scope.put(RECIPIENT, RodaConstants.NOTIFICATION_VARIOUS_RECIPIENT_USERS);
      }

      String strippedTemplate = template.replace("<a href=\"{{acknowledge}}\">", "").replace("</a>", "");
      try {
        processedNotification.setBody(HandlebarsUtility.executeHandlebars(strippedTemplate, scope));
      } catch (GenericException e) {
        LOGGER.error("Error processing notification body", e);
        processedNotification.setBody(strippedTemplate);
      }
      scope.remove(RECIPIENT);
      ConfigurableEmailUtility emailUtility = new ConfigurableEmailUtility(processedNotification.getFromUser(),
        processedNotification.getSubject());
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
    } catch (IOException | MessagingException | GenericException e) {
      processedNotification.setState(NOTIFICATION_STATE.FAILED);
      LOGGER.debug("Error sending e-mail: {}", e.getMessage());
    }
    return processedNotification;
  }

  private String getUpdatedMessageBody(ModelService model, Notification notification, String recipient, String template,
    Map<String, Object> scopes) throws GenericException {

    // update body message with the recipient user and acknowledge URL
    String userUUID = IdUtils.createUUID(recipient);
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

    return HandlebarsUtility.executeHandlebars(template, scopes);
  }

}
