/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.flashpageflipFree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.roda.core.data.RepresentationObject;
import org.roda.core.data.common.BrowserException;
import org.roda.core.data.common.LoginException;
import org.roda.core.data.common.NoSuchRODAObjectException;
import org.roda.core.data.common.RODAClientException;
import org.roda.disseminators.common.UnsupportedContentModel;
import org.roda.disseminators.common.cache.Cache;
import org.roda.disseminators.common.cache.CacheController;
import org.roda.disseminators.simpleviewer.SimpleViewer;

/**
 * Servlet implementation class for Servlet: FlashPageFlip
 * 
 */
public class FlashPageFlipFree extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
  /**
	 * 
	 */
  private static final long serialVersionUID = -8150743166614788976L;

  /**
   * The name of the disseminator, to use in cache
   */
  public static final String DISSEMINATOR_NAME = "FlashPageFlip";

  private Logger logger = Logger.getLogger(FlashPageFlipFree.class);

  private final Map<String, String> resources;

  // private final MigratorClient migratorClient;
  private final String dwMigratorUrl;
  private final String pdfMigratorUrl;

  private final CacheController cacheController;

  /**
   * Create a new FlashPageFlip server
   */
  public FlashPageFlipFree() {
    resources = new HashMap<String, String>();
    resources.put("/js/AC_RunActiveContent.js",
      "/org/roda/disseminators/flashpageflipFree/js/AC_RunActiveContent.js");
    resources.put("/js/PopUpWin.js", "/org/roda/disseminators/flashpageflipFree/js/PopUpWin.js");
    resources.put("/swf/Magazine.swf", "/org/roda/disseminators/flashpageflipFree/swf/Magazine.swf");
    resources.put("/swf/Pages.swf", "/org/roda/disseminators/flashpageflipFree/swf/Pages.swf");
    resources.put("/txt/Lang.txt", "/org/roda/disseminators/flashpageflipFree/txt/Lang.txt");

    // migratorClient = new MigratorClient();
    // dwMigratorUrl = RodaClientFactory.getRodaProperties().getProperty(
    // "roda.disseminators.flashpageflip.digitalizedwork.migrator");
    // pdfMigratorUrl = RodaClientFactory.getRodaProperties().getProperty(
    // "roda.disseminators.flashpageflip.pdf.migrator");
    dwMigratorUrl = null;
    pdfMigratorUrl = null;

    cacheController = new CacheController(DISSEMINATOR_NAME, "FlashPageFlipFree") {

      @Override
      protected void createResources(HttpServletRequest request, RepresentationObject rep, String cacheURL,
        File cacheFile) throws Exception {
        FlashPageFlipFree.this.createResources(request, rep, cacheURL, cacheFile);

      }

      @Override
      protected void sendResponse(HttpServletRequest request, RepresentationObject rep, String cacheURL,
        HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        sendIndex(request, rep, cacheURL, response);
      }

    };
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws ServletException,
    IOException {
    String pathInfo = request.getPathInfo();
    int separatorIndex = pathInfo.indexOf('/', 2);
    separatorIndex = separatorIndex != -1 ? separatorIndex : pathInfo.length();
    String pid = pathInfo.substring(1, separatorIndex);
    String path = pathInfo.substring(separatorIndex);

    if (!pid.matches("roda\\:.*")) {
      pid = null;
      path = pathInfo;
    }

    try {

      logger.debug("session defined pid=" + pid);

      logger.debug("lookup in resources path=" + path);

      if (path.length() <= 1) {
        // create cache and send index
        cacheController.get(pid, request, response);
      } else if (resources.containsKey(path)) {
        // check for static resources
        String resourcePath = resources.get(path);
        InputStream paramInputStream = FlashPageFlipFree.class.getClassLoader().getResourceAsStream(resourcePath);
        URL resourceURL = FlashPageFlipFree.class.getClassLoader().getResource(resourcePath);
        File resourceFile;
        try {
          resourceFile = new File(resourceURL.toURI());
        } catch (URISyntaxException e) {
          resourceFile = new File(resourceURL.getPath());
        }

        response.setHeader("Last-Modified", "" + resourceFile.lastModified() / 1000 * 1000);
        response.setDateHeader("Expires", System.currentTimeMillis() + 24 * 60 * 60 * 1000);
        FileHelper.copy(paramInputStream, response.getOutputStream());
      } else {
        // check for cached resources
        File resource = Cache.getCacheResource(request, pid, DISSEMINATOR_NAME, path);
        logger.debug("lookup in cache path=" + resource.getAbsolutePath());
        if (resource.exists()) {
          response.setContentLength((int) resource.length());
          FileHelper.copy(new FileInputStream(resource), response.getOutputStream());
        } else {
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
      }

    } catch (LoginException e) {
      logger.error("Login Failure", e);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    } catch (NoSuchRODAObjectException e) {
      logger.error("Object does not exist", e);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (BrowserException e) {
      logger.error("Browser Exception", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (RODAClientException e) {
      logger.error("RODA Client Exception", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (RemoteException e) {
      // RODAException exception = RODAClient.parseRemoteException(e);
      // if (exception instanceof AuthorizationDeniedException) {
      // response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
      // exception.getMessage());
      // } else {
      // logger.error("RODA Exception", e);
      // response.sendError(
      // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
      // .getMessage());
      // }

    }
  }

  /**
   * Create all needed resources
   * 
   * @param request
   * @param rep
   *          the representation object
   * @param cacheURL
   *          the cache URL
   * @param cache
   *          the cache directory
   * @throws RODAClientException
   * @throws LoginException
   * @throws MigratorClientException
   * @throws UnsupportedContentModel
   * @throws ConverterException
   * @throws IOException
   */
  public void createResources(HttpServletRequest request, RepresentationObject rep, String cacheURL, File cache)
    throws LoginException, RODAClientException,
    // MigratorClientException,
    // UnsupportedContentModel,
    // ConverterException,
    IOException {

    if (cache.canWrite()) {
      // clear resources
      cache.delete();
      cache.mkdir();

      // // Convert representation
      // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
      // .getSession());
      // SynchronousConverter service;
      // if (rep.getType().equals(RepresentationObject.DIGITALIZED_WORK))
      // {
      // service = migratorClient.getSynchronousConverterService(
      // dwMigratorUrl, rodaClient.getCup(),rodaClient.getCasUtility());
      // } else if (rep.getType().equals(
      // RepresentationObject.STRUCTURED_TEXT) ||
      // rep.getType().equals(RepresentationObject.PRESENTATION)) {
      // service = migratorClient.getSynchronousConverterService(
      // pdfMigratorUrl, rodaClient.getCup(),rodaClient.getCasUtility());
      // } else {
      // logger.error("Unsuported representation type: "
      // + rep.getContentModel());
      // throw new UnsupportedContentModel("" + rep.getContentModel());
      // }
      //
      // RepresentationObject converted = service.convert(rep)
      // .getRepresentation();
      //
      // // Download converted representation
      // migratorClient.writeRepresentationObject(converted, cache);
      //
      // // Move F0.xml to xml/Pages.xml
      // File xmlFolder = new File(cache, "xml");
      // xmlFolder.mkdir();
      // File f0 = new File(cache, "F0.xml");
      // File pages = new File(xmlFolder, "Pages.xml");
      // FileUtils.moveFile(f0, pages);

    } else {
      logger.error("Cannot write to cache");
    }
  }

  private void sendIndex(HttpServletRequest request, RepresentationObject rep, String cacheURL,
    HttpServletResponse response) throws IOException {
    InputStream indexTemplate = SimpleViewer.class.getClassLoader().getResourceAsStream(
      "/org/roda/disseminators/flashpageflipFree/index.html");
    String index;

    index = new String(StreamUtils.getBytes(indexTemplate));

    // String title;
    // try {
    // SimpleDescriptionObject sdo = RodaClientFactory.getRodaClient(
    // request.getSession()).getBrowserService()
    // .getSimpleDescriptionObject(rep.getDescriptionObjectPID());
    // title = sdo.getTitle();
    // } catch (Exception e) {
    // title = "";
    // }
    //
    // index = index.replaceAll("\\@TITLE",
    // Matcher.quoteReplacement(title));
    // PrintWriter printer = new PrintWriter(response.getOutputStream());
    // printer.write(index);
    // printer.flush();
    // printer.close();

  }

}
