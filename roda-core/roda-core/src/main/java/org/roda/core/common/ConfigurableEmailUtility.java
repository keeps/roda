/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import org.roda.core.RodaCoreFactory;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class ConfigurableEmailUtility {

  private static final List<String> DEFAULT_PROPERTIES = Arrays.asList("host", "port", "auth", "starttls.enable");
  private String protocol;
  private String user;
  private String password;
  private String from;
  private String fromActor;
  private String subject;
  private Authenticator authenticator = null;
  private Properties props = new Properties();

  public ConfigurableEmailUtility(String fromActor, String subject) {
    this.protocol = RodaCoreFactory.getRodaConfiguration().getString("core.email.protocol", "");
    this.user = RodaCoreFactory.getRodaConfiguration().getString("core.email.user", "");
    this.from = RodaCoreFactory.getRodaConfiguration().getString("core.email.from", "");
    this.password = RodaCoreFactory.getRodaConfiguration().getString("core.email.password", "");
    this.fromActor = fromActor;
    this.subject = subject;

    createSessionParameters();
  }

  public void sendMail(String recipient, String message) throws MessagingException, IOException {

    if ("".equals(from)) {
      throw new MessagingException();
    }

    Session session = Session.getDefaultInstance(props, authenticator);
    session.setDebug(false);

    Message msg = new MimeMessage(session);

    InternetAddress addressFrom = new InternetAddress(from);
    msg.setFrom(addressFrom);
    msg.addHeader("name", fromActor);
    msg.setSubject(subject);

    InternetAddress recipientAddress = new InternetAddress(recipient);
    msg.setRecipient(Message.RecipientType.TO, recipientAddress);

    String htmlMessage = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", message);
    MimeMultipart mimeMultipart = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(htmlMessage, "text/html;charset=UTF-8");
    mimeMultipart.addBodyPart(mimeBodyPart);
    msg.setContent(mimeMultipart);

    msg.setDataHandler(new DataHandler(new ByteArrayDataSource(htmlMessage, "text/html")));

    // sending the message
    Transport transport = session.getTransport(this.protocol);
    transport.connect();
    transport.sendMessage(msg, msg.getAllRecipients());
    transport.close();
  }

  private void createSessionParameters() {
    boolean hasAuth = false;

    String properties = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "properties");
    List<String> propertyList = new ArrayList<>(DEFAULT_PROPERTIES);
    if (properties != null) {
      propertyList.addAll(Arrays.asList(properties.split(" ")));
    }

    for (String property : propertyList) {
      String mailProperty = RodaCoreFactory.getRodaConfigurationAsString("core", "email", property);
      if (mailProperty != null) {
        props.put("mail." + this.protocol + "." + property, mailProperty);
        if ("auth".equals(property) && "true".equals(mailProperty)) {
          hasAuth = true;
        }
      }
    }

    if (hasAuth) {
      authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(user, password);
        }
      };
    }
  }
}
