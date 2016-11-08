/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAS client.
 * 
 * Adapted from https://wiki.jasig.org/display/casum/restful+api.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CasClient {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CasClient.class);
  /** No ticket message. */
  private static final String NO_TICKET = "Successful ticket granting request, but no ticket found!";
  /** CAS server URL. */
  private final String casServerUrlPrefix;

  /**
   * Constructor.
   * 
   * @param casServerUrlPrefix
   *          CAS server URL.
   */
  public CasClient(final String casServerUrlPrefix) {
    this.casServerUrlPrefix = casServerUrlPrefix;
  }

  /**
   * Get a <strong>Ticket Granting Ticket</strong> from the CAS server for the
   * specified <i>username</i> and <i>password</i>.
   * 
   * @param username
   *          the username.
   * @param password
   *          the password.
   * @return the <strong>Ticket Granting Ticket</strong>
   * @throws AuthenticationDeniedException
   *           if the CAS server rejected the specified credentials.
   * @throws GenericException
   *           if some error occurred.
   */
  public String getTicketGrantingTicket(final String username, final String password)
    throws AuthenticationDeniedException, GenericException {
    final HttpClient client = new HttpClient();
    final PostMethod post = new PostMethod(String.format("%s/v1/tickets", this.casServerUrlPrefix));
    post.setRequestBody(
      new NameValuePair[] {new NameValuePair("username", username), new NameValuePair("password", password)});
    try {
      client.executeMethod(post);
      final String response = post.getResponseBodyAsString();
      if (post.getStatusCode() == HttpStatus.SC_CREATED) {
        final Matcher matcher = Pattern.compile(".*action=\".*/(.*?)\".*").matcher(response);
        if (matcher.matches()) {
          return matcher.group(1);
        }
        LOGGER.warn(NO_TICKET);
        throw new GenericException(NO_TICKET);
      } else if (post.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
        throw new AuthenticationDeniedException("Could not create ticket: " + post.getStatusText());
      } else {
        LOGGER.warn(invalidResponseMessage(post));
        throw new GenericException(invalidResponseMessage(post));
      }
    } catch (final IOException e) {
      throw new GenericException(e.getMessage(), e);
    } finally {
      post.releaseConnection();
    }
  }

  /**
   * Get a <strong>Service Ticket</strong> from the CAS server for the specified
   * <strong>Ticket Granting Ticket</strong> and <strong>service</strong>.
   * 
   * @param ticketGrantingTicket
   *          the <strong>Ticket Granting Ticket</strong>.
   * @param service
   *          the service URL.
   * @return the <strong>Service Ticket</strong>.
   * @throws GenericException
   *           if some error occurred.
   */
  public String getServiceTicket(final String ticketGrantingTicket, final String service) throws GenericException {
    final HttpClient client = new HttpClient();
    final PostMethod post = new PostMethod(String.format("%s/v1/tickets/%s", casServerUrlPrefix, ticketGrantingTicket));
    post.setRequestBody(new NameValuePair[] {new NameValuePair("service", service)});
    try {
      client.executeMethod(post);
      final String response = post.getResponseBodyAsString();
      if (post.getStatusCode() == HttpStatus.SC_OK) {
        return response;
      } else {
        LOGGER.warn(invalidResponseMessage(post));
        throw new GenericException(invalidResponseMessage(post));
      }
    } catch (final IOException e) {
      throw new GenericException(e.getMessage(), e);
    } finally {
      post.releaseConnection();
    }
  }

  /**
   * Logout from the CAS server.
   * 
   * @param ticketGrantingTicket
   *          the <strong>Ticket Granting Ticket</strong>.
   * @throws GenericException
   *           if some error occurred.
   */
  public void logout(final String ticketGrantingTicket) throws GenericException {
    final HttpClient client = new HttpClient();
    final DeleteMethod method = new DeleteMethod(
      String.format("%s/v1/tickets/%s", casServerUrlPrefix, ticketGrantingTicket));
    try {
      client.executeMethod(method);
      if (method.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("Logged out");
      } else {
        LOGGER.warn(invalidResponseMessage(method));
        throw new GenericException(invalidResponseMessage(method));
      }
    } catch (final IOException e) {
      throw new GenericException(e.getMessage(), e);
    } finally {
      method.releaseConnection();
    }
  }

  /**
   * Returns an error message for invalid response from CAS server.
   * 
   * @param method
   *          the HTTP method
   * @return a String with the error message.
   */
  private String invalidResponseMessage(final HttpMethod method) {
    return String.format("Invalid response from CAS server: %s - %s", method.getStatusCode(), method.getStatusText());
  }

}
