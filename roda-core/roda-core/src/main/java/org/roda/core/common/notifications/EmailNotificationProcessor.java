package org.roda.core.common.notifications;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ConfigurableEmailUtility;
import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class EmailNotificationProcessor implements NotificationProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationProcessor.class);
  private Map<String,Object> scope;
  private String template;
  
  
  

  public EmailNotificationProcessor( String template, Map<String, Object> scope) {
    this.scope = scope;
    this.template = template;
  }

  @Override
  public Notification processNotification(Notification notification) throws RODAException {
    try{
      List<String> recipients = notification.getRecipientUsers();
      String templatePath = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "template_path");
      InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(templatePath + template);
      String template = IOUtils.toString(templateStream, "UTF-8");
      IOUtils.closeQuietly(templateStream);
      if (!scope.containsKey("from")) {
        scope.put("from", notification.getFromUser());
      }
      if (recipients.size() == 1) {
        scope.put("recipient", recipients.get(0));
      } else {
        scope.put("recipient", RodaConstants.NOTIFICATION_VARIOUS_RECIPIENT_USERS);
      }
      notification.setBody(executeHandlebars(template, template, scope));
      scope.remove("recipient");
      ConfigurableEmailUtility emailUtility = new ConfigurableEmailUtility(notification.getFromUser(),
        notification.getSubject());
      for (String recipient : recipients) {
        String modifiedBody = getUpdatedMessageBody(notification, recipient, template, template, scope);
        String host = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "host");
        if (host != null && !host.equals("")) {
          LOGGER.debug("Sending email ...");
          emailUtility.sendMail(recipient, modifiedBody);
          LOGGER.debug("Email sent");
        } else {
          LOGGER.warn("SMTP not defined, cannot send emails");
        }
      }
    }catch(IOException | MessagingException e){
      throw new GenericException(e);
    }
    return notification;
  }
  
  private String getUpdatedMessageBody(Notification notification, String recipient, String template,
    String templateName, Map<String, Object> scopes) {

    // update body message with the recipient user and acknowledge URL
    String userUUID = UUID.nameUUIDFromBytes(recipient.getBytes()).toString();
    String ackUrl = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "acknowledge");
    ackUrl = ackUrl.replaceAll("\\{notificationId\\}", notification.getId());
    ackUrl = ackUrl.replaceAll("\\{token\\}", notification.getAcknowledgeToken() + userUUID);
    ackUrl = ackUrl.replaceAll("\\{email\\}", recipient);

    scopes.put("acknowledge", ackUrl);
    scopes.put("recipient", recipient);

    try {
      User user = UserUtility.getLdapUtility().getUserWithEmail(recipient);
      if (user != null) {
        scopes.put("recipient", user.getName());
      }
    } catch (LdapUtilityException e) {
      // do nothing
    }

    return executeHandlebars(template, templateName, scopes);
  }
  
  private String executeHandlebars(String template, String templateName, Map<String, Object> scopes) {
    Handlebars handlebars = new Handlebars();
    String result = "";
    try {
      Template templ = handlebars.compileInline(template);
      result = templ.apply(scopes);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

}
