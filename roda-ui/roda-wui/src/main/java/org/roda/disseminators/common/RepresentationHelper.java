package org.roda.disseminators.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.roda.core.common.LoginException;
import org.roda.core.common.RODAClientException;
import org.roda.core.data.RepresentationFile;
import org.roda.core.data.RepresentationObject;

/**
 * @author Luis Faria
 * 
 */
public class RepresentationHelper {

  private static Logger logger = Logger.getLogger(RepresentationHelper.class);

  private final HttpClient client;

  private final URL rodaCoreURL;

  /**
   * Create a new representation helper
   * 
   * @throws IOException
   */
  public RepresentationHelper() throws IOException {
    client = new HttpClient();
    client.getParams().setAuthenticationPreemptive(true);

    // rodaCoreURL = RodaClientFactory.getRodaCoreUrl();
    rodaCoreURL = null;
  }

  /**
   * Get the root file GetMethod
   * 
   * @param request
   *          the request
   * @param obj
   *          the representation object
   * @return the GetMethod
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public GetMethod getRootMethod(HttpServletRequest request, RepresentationObject obj) throws LoginException,
    HttpException, IOException, RODAClientException {
    return getMethod(request, obj.getRootFile().getAccessURL());
  }

  /**
   * Get a part file GetMethod
   * 
   * @param request
   *          the request
   * @param obj
   *          the representation object
   * @param i
   *          the index of the part file in the part files list
   * @return the GetMethod
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public GetMethod getPartFileMethod(HttpServletRequest request, RepresentationObject obj, int i)
    throws LoginException, HttpException, IOException, RODAClientException {
    return getMethod(request, obj.getPartFiles()[i].getAccessURL());
  }

  /**
   * Get the root file input stream
   * 
   * @param request
   *          the request
   * @param obj
   *          the representation object
   * @return the input stream
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public InputStream getRootInputStream(HttpServletRequest request, RepresentationObject obj) throws LoginException,
    HttpException, IOException, RODAClientException {
    return getMethod(request, obj.getRootFile().getAccessURL()).getResponseBodyAsStream();
  }

  /**
   * Get a part file input stream
   * 
   * @param request
   *          the request
   * @param obj
   *          the representation object
   * @param i
   *          the index of the part file in the part files list
   * @return the input stream
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public InputStream getPartFileInputStream(HttpServletRequest request, RepresentationObject obj, int i)
    throws LoginException, HttpException, IOException, RODAClientException {
    return getMethod(request, obj.getPartFiles()[i].getAccessURL()).getResponseBodyAsStream();
  }

  /**
   * Get a part file input stream
   * 
   * @param request
   *          the request
   * @param obj
   *          the representation object
   * @param id
   *          the part file id
   * @return the input stream
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public InputStream getPartFileInputStream(HttpServletRequest request, RepresentationObject obj, String id)
    throws LoginException, HttpException, IOException, RODAClientException {
    return getMethod(request, obj.getPartFiles()[getPartFileIndex(obj, id)].getAccessURL()).getResponseBodyAsStream();
  }

  /**
   * Use the HTTP client to get the GetMethod
   * 
   * @param request
   *          the request
   * @param path
   *          the path relative to the RODA Core services
   * @return the GetMethod
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public GetMethod getMethod(HttpServletRequest request, String path) throws LoginException, HttpException,
    IOException, RODAClientException {
    // Prepare method
    GetMethod getFileMethod = new GetMethod(rodaCoreURL + path);

    // // Set up the client connection
    // HttpClient client = new HttpClient();
    // client.getParams().setAuthenticationPreemptive(true);
    // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
    // .getSession());
    // String username = rodaClient.getUsername();
    // String password =
    // rodaClient.getCasUtility().generateProxyTicket(rodaClient.getProxyGrantingTicket());
    // Credentials credentials = new UsernamePasswordCredentials(username,
    // password);
    //
    //
    // client.getState().setCredentials(
    // new AuthScope(rodaCoreURL.getHost(), rodaCoreURL.getPort(),
    // AuthScope.ANY_REALM), credentials);
    //
    // // Execute method
    // client.executeMethod(getFileMethod);
    //
    // // getFileMethod.releaseConnection();

    return getFileMethod;
  }

  /**
   * Forward a request from the HTTP client (GetMethod) to a HTTP servlet
   * response. This method can be used to directly forward content.
   * 
   * @param method
   *          the HTTP get method
   * 
   * @param response
   *          the HTTP servlet response
   * @throws IOException
   */
  public void forwardMethod(GetMethod method, HttpServletResponse response) throws IOException {

    logger.trace("Setting content type");
    // response.setContentLength((int) method.getResponseContentLength());
    Header contentType = method.getResponseHeader("Content-Type");

    if (contentType != null) {
      response.setContentType(contentType.getValue());
    }

    logger.trace("Setting file name");
    // Using special header to workaround HTTPClient limitation for
    // US-ASCII encoded headers
    Header filename = method.getResponseHeader("FILENAME");
    if (filename != null && filename.getValue() != null) {
      logger.debug("Got filename '" + filename.getValue() + "'");
      String name = URLDecoder.decode(filename.getValue(), "UTF-8");
      logger.debug("Decoded to '" + name + "'");
      response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");

    }
    // If special header not set, fall back to content-disposition
    else {
      logger.trace("No header FILENAME set, trying content-disposition");
      Header contentDisposition = method.getResponseHeader("Content-Disposition");
      if (contentDisposition != null && contentDisposition.getValue() != null) {
        response.setHeader("Content-Disposition", "attachment; " + contentDisposition.getValue());
        logger.debug("Got filename '" + contentDisposition.getValue() + "'");
      }
    }

    int status = method.getStatusCode();
    if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
      send(method.getResponseBodyAsStream(), response.getOutputStream());
      response.flushBuffer();
    } else {
      response.sendError(status);
    }
  }

  /**
   * Copy the contents of and input stream to an output stream
   * 
   * @param in
   *          the input stream
   * @param out
   *          the output stream
   * @throws IOException
   */
  public void send(InputStream in, OutputStream out) throws IOException {
    IOUtils.copyLarge(in, out);
  }

  /**
   * Send all representation files to a directory
   * 
   * @param request
   *          the request
   * @param rep
   *          the representation object
   * @param dir
   *          the directory
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public void sendToDir(HttpServletRequest request, RepresentationObject rep, File dir) throws LoginException,
    HttpException, IOException, RODAClientException {
    sendRootToDir(request, rep, dir);
    for (int i = 0; i < rep.getPartFiles().length; i++) {
      sendPartFileToDir(request, rep, i, dir);
    }

  }

  /**
   * Send the root file to a directory
   * 
   * @param request
   *          the request
   * @param rep
   *          the representation object
   * @param dir
   *          the directory
   * @return the root file
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public File sendRootToDir(HttpServletRequest request, RepresentationObject rep, File dir) throws LoginException,
    HttpException, IOException, RODAClientException {
    GetMethod rootMethod = getRootMethod(request, rep);
    String filename = rep.getRootFile().getId();
    File rootFile = new File(dir.getAbsoluteFile() + "/" + filename);
    send(rootMethod.getResponseBodyAsStream(), new FileOutputStream(rootFile));
    return rootFile;
  }

  /**
   * Send a part file to a directory
   * 
   * @param request
   *          the request
   * @param rep
   *          the representation object
   * @param index
   *          the index of the part file in the part files list
   * @param dir
   *          the directory
   * @return the part file
   * @throws LoginException
   * @throws HttpException
   * @throws IOException
   * @throws RODAClientException
   */
  public File sendPartFileToDir(HttpServletRequest request, RepresentationObject rep, int index, File dir)
    throws LoginException, HttpException, IOException, RODAClientException {
    GetMethod method = getPartFileMethod(request, rep, index);
    String filename = rep.getPartFiles()[index].getId();
    File file = new File(dir.getAbsoluteFile() + "/" + filename);
    send(method.getResponseBodyAsStream(), new FileOutputStream(file));
    return file;
  }

  /**
   * Get the index of the part file on the list by the datastream id
   * 
   * @param rep
   *          the representation object
   * @param pathFileId
   *          the path file datastream id
   * @return the index
   */
  public int getPartFileIndex(RepresentationObject rep, String pathFileId) {
    int ret = -1;
    for (int i = 0; i < rep.getPartFiles().length; i++) {
      RepresentationFile partFile = rep.getPartFiles()[i];
      if (partFile.getId().equals(pathFileId)) {
        ret = i;
      }
    }
    return ret;
  }

}
