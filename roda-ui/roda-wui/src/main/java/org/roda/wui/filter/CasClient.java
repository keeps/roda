package org.roda.wui.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
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

  private String invalidResponseMessage(final HttpMethod method) {
    return String.format("Invalid response from CAS server: %s - %s", method.getStatusCode(), method.getStatusText());
  }

  /**
   * Do a service call with the specified service ticket. <strong>This is a TEST
   * method</strong>.
   * 
   * @param service
   *          the service URL.
   * @param serviceTicket
   *          the <strong>Service Ticket</strong>.
   */
  private void getServiceCall(final String service, final String serviceTicket) {
    final HttpClient client = new HttpClient();
    final GetMethod method = new GetMethod(service);
    if (StringUtils.isBlank(method.getQueryString())) {
      method.setQueryString(String.format("ticket=%s", serviceTicket));
    } else {
      method.setQueryString(String.format("%s&ticket=%s", method.getQueryString(), serviceTicket));
    }
    LOGGER.info(String.format("GET %s?%s", method.getPath(), method.getQueryString()));
    try {
      client.executeMethod(method);
      final String response = method.getResponseBodyAsString();
      if (method.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("Response: {}", response);
      } else {
        LOGGER.warn(invalidResponseMessage(method));
      }
    } catch (final IOException e) {
      LOGGER.warn(e.getMessage(), e);
    } finally {
      method.releaseConnection();
    }
  }

  public static void main(final String[] args) {
    try {
      final String casServer = "https://localhost:8443/cas";
      final String username = args[0];
      final String password = args[1];

      final String service1 = "http://localhost:8888/api/v1/index?returnClass=org.roda.core.data.v2.ip.IndexedAIP";
      final String service2 = "http://localhost:8888/api/v1/index?returnClass=org.roda.core.data.v2.user.RODAMember";

      final CasClient client = new CasClient(casServer);
      final String ticketGrantingTicket = client.getTicketGrantingTicket(username, password);
      LOGGER.info("TicketGrantingTicket is {}", ticketGrantingTicket);

      final String serviceTicket1 = client.getServiceTicket(ticketGrantingTicket, service1);
      LOGGER.info("ServiceTicket is {}", serviceTicket1);
      client.getServiceCall(service1, serviceTicket1);

      final String serviceTicket2 = client.getServiceTicket(ticketGrantingTicket, service2);
      LOGGER.info("ServiceTicket is {}", serviceTicket1);
      client.getServiceCall(service2, serviceTicket2);

      client.logout(ticketGrantingTicket);
    } catch (final Exception e) {
      LOGGER.error(e.getMessage(), e);
      System.exit(1);
    }
  }
}
