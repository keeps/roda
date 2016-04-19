/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.roda.core.RodaCoreFactory;

import com.sun.mail.smtp.SMTPTransport;

public class ConfigurableEmailUtility {

  private String protocol;
  private String login;
  private String password;
  private Message msg;
  private Session session;

  public ConfigurableEmailUtility(String from, String subject) {
    this.protocol = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "protocol");
    this.login = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "login");
    this.password = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "password");

    try {
      session = getSession();
      session.setDebug(false);

      // create a message
      msg = new MimeMessage(session);

      // set the from and to address
      InternetAddress addressFrom = new InternetAddress(login);

      msg.setFrom(addressFrom);

      // Setting the Subject and Content Type
      msg.addHeader("name", from);
      msg.setSubject(subject);

    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  public void sendMail(String recipient, String message) throws MessagingException {

    InternetAddress recipientAddress = new InternetAddress(recipient);
    msg.setRecipient(Message.RecipientType.TO, recipientAddress);

    String htmlMessage = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%s", message);
    MimeMultipart mimeMultipart = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(htmlMessage, "text/html;charset=UTF-8");
    mimeMultipart.addBodyPart(mimeBodyPart);
    msg.setContent(mimeMultipart);

    // sending the message
    SMTPTransport transport = (SMTPTransport) session.getTransport(this.protocol);
    transport.connect();
    transport.sendMessage(msg, msg.getAllRecipients());
    transport.close();
  }

  private Session getSession() {
    javax.mail.Authenticator authenticator = null;
    boolean hasAuth = false;

    Properties props = new Properties();
    String properties = RodaCoreFactory.getRodaConfigurationAsString("core", "email", "properties");
    String[] propertyList = properties.split(" ");

    for (String property : propertyList) {
      String mailProperty = RodaCoreFactory.getRodaConfigurationAsString("core", "email", property);
      props.put("mail." + this.protocol + "." + property, mailProperty);
      if (property.equals("auth") && mailProperty.equals("true"))
        hasAuth = true;
    }

    if (hasAuth) {
      authenticator = new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(login, password);
        }
      };
    }

    return Session.getDefaultInstance(props, authenticator);
  }
}
