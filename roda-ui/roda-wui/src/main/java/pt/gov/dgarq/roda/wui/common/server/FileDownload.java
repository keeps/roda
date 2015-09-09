package pt.gov.dgarq.roda.wui.common.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * File download servlet helper
 * 
 * @author Luis Faria
 */
public class FileDownload extends RemoteServiceServlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = -4513191839372139634L;
  Logger logger = Logger.getLogger(FileDownload.class);

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    URL rodaServices;
    String path = request.getParameter("path");
    try {
      path = URLDecoder.decode(path, "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      logger.error("Error decoding path", e1);
    }

    logger.info("Getting path " + path);

    // try {
    // try {
    // rodaServices = RodaClientFactory.getRodaCoreUrl();
    //
    // // Prepare method
    // GetMethod getFileMethod = new GetMethod(rodaServices + path);
    //
    // // Set up the client connection
    // HttpClient client = new HttpClient();
    // client.getParams().setAuthenticationPreemptive(true);
    // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
    // .getSession());
    // String username = rodaClient.getUsername();
    // String password =
    // rodaClient.getCasUtility().generateProxyTicket(rodaClient.getProxyGrantingTicket());
    //
    // Credentials credentials = new UsernamePasswordCredentials(
    // username, password);
    // client.getState().setCredentials(
    // new AuthScope(rodaServices.getHost(), rodaServices
    // .getPort(), AuthScope.ANY_REALM), credentials);
    //
    // // Execute method
    // int status = client.executeMethod(getFileMethod);
    //
    // Header[] responseHeaders = getFileMethod.getResponseHeaders();
    // for (int i = 0; i < responseHeaders.length; i++) {
    // response.setHeader(responseHeaders[i].getName(),
    // responseHeaders[i].getValue());
    // }
    // if (status == HttpStatus.SC_OK
    // || status == HttpStatus.SC_CREATED) {
    // send(getFileMethod.getResponseBodyAsStream(), response
    // .getOutputStream());
    // } else {
    // response.sendError(status);
    // }
    //
    // } catch (LoginException e) {
    // logger.error("Authorization Denied", e);
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e
    // .getMessage());
    // } catch (IOException e) {
    // logger.error("IO exception", e);
    // response.sendError(
    // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    //
    // } catch (RODAClientException e) {
    // logger.error("RODA Client Error", e);
    // response.sendError(
    // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // }
    // } catch (IOException e) {
    // logger.error("Could not send error message", e);
    //
    // }
  }

  private void send(InputStream in, OutputStream out) {

    try {
      FileHelper.copy(in, out);
    } catch (IOException e) {
      logger.debug("Error while sending", e);
    }
  }

  public void destroy() {
    super.destroy();
    LogManager.shutdown();
  }
}
