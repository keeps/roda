package pt.gov.dgarq.roda.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Based in an article by Sudhir Ancha
 * (http://www.javacommerce.com/displaypage.jsp?name=javamail.sql&id=18274).
 * 
 * @author Rui Castro
 */
public class EmailUtility {
	private static final Logger logger = Logger.getLogger(EmailUtility.class);

	private String smtpHost = null;

	/**
	 * Constructs a new {@link EmailUtility} that sends email messages through
	 * the SMTP server given as argument.
	 * 
	 * @param smtpHost
	 *            the name of the SMTP server. If <code>null</code> 'localhost'
	 *            will be used.
	 */
	public EmailUtility(String smtpHost) {

		if (StringUtils.isBlank(smtpHost)) {
			logger.warn("SMTP host is not valid '" + smtpHost
					+ "'. Using default localhost.");
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
	public void sendMail(String from, String recipients[], String subject,
			String message) throws MessagingException {
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
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<html><body><pre>%s</pre></body></html>",
				StringEscapeUtils.escapeHtml(message));

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
