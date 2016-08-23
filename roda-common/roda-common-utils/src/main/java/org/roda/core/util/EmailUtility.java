/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based in an article by Sudhir Ancha
 * (http://www.javacommerce.com/displaypage.jsp?name=javamail.sql&id=18274).
 * 
 * @author Rui Castro
 * 
 * @deprecated 20160824 hsilva: not seeing any method using it, so it will be
 *             removed soon
 */
public class EmailUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtility.class);

  private String smtpHost = null;

  /**
   * Constructs a new {@link EmailUtility} that sends email messages through the
   * SMTP server given as argument.
   * 
   * @param smtpHost
   *          the name of the SMTP server. If <code>null</code> 'localhost' will
   *          be used.
   */
  public EmailUtility(String smtpHost) {

    if (StringUtils.isBlank(smtpHost)) {
      LOGGER.warn("SMTP host is not valid '{}'. Using default localhost.", smtpHost);
      this.smtpHost = "localhost";
    } else {
      this.smtpHost = smtpHost;
    }
  }

  /**
   * @param from
   * @param recipients
   * @param subject
   * @param message
   * @throws MessagingException
   */
  public void sendMail(String from, String recipients[], String subject, String message) throws MessagingException {
    boolean debug = false;

    // Set the host smtp address
    Properties props = new Properties();
    props.put("mail.smtp.host", this.smtpHost);

    // create some properties and get the default Session
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(debug);

    // create a message
    Message msg = new MimeMessage(session);

    // set the from and to address
    InternetAddress addressFrom = new InternetAddress(from);
    msg.setFrom(addressFrom);

    InternetAddress[] addressTo = new InternetAddress[recipients.length];
    for (int i = 0; i < recipients.length; i++) {
      addressTo[i] = new InternetAddress(recipients[i]);
    }
    msg.setRecipients(Message.RecipientType.TO, addressTo);

    // Optional : You can also set your custom headers in the Email if you
    // want
    // msg.addHeader("MyHeaderName", "myHeaderValue");

    String htmlMessage = String.format(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<html><body><pre>%s</pre></body></html>",
      StringEscapeUtils.escapeHtml4(message));

    MimeMultipart mimeMultipart = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    mimeBodyPart.setContent(htmlMessage, "text/html;charset=UTF-8");
    mimeMultipart.addBodyPart(mimeBodyPart);

    // Setting the Subject and Content Type
    msg.setSubject(subject);
    msg.setContent(mimeMultipart);
    // msg.setContent(message, "text/plain;charset=UTF-8");
    Transport.send(msg);
  }
}
