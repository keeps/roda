/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.mediaplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.lf5.util.StreamUtils;
import org.roda.core.data.RepresentationObject;
import org.roda.core.data.common.BrowserException;
import org.roda.core.data.common.LoginException;
import org.roda.core.data.common.NoSuchRODAObjectException;
import org.roda.core.data.common.RODAClientException;
import org.roda.disseminators.common.cache.CacheController;
import org.roda.disseminators.simpleviewer.SimpleViewer;

/**
 * Servlet implementation class for Servlet: MediaPlayer, which wrapps
 * disseminator for <a href="http://www.jeroenwijering.com">JW FLV Media
 * Player</a>.
 * 
 */
public class MediaPlayer extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
  static final long serialVersionUID = 1L;

  static final String DISSEMINATOR_NAME = "MediaPlayer";

  private Logger logger = LoggerFactory.getLogger(MediaPlayer.class);

  private final CacheController cacheController;
  // private RepresentationHelper representationHelper = null;

  // private final MigratorClient migratorClient;
  private final String videoMigratorUrl;
  private final String audioMigratorUrl;

  /**
   * Create a new media player disseminator servlet
   */
  public MediaPlayer() {
    super();

    // migratorClient = new MigratorClient();
    // videoMigratorUrl = RodaClientFactory.getRodaProperties().getProperty(
    // "roda.disseminators.mediaplayer.video.migrator");
    // audioMigratorUrl = RodaClientFactory.getRodaProperties().getProperty(
    // "roda.disseminators.mediaplayer.audio.migrator");
    videoMigratorUrl = null;
    audioMigratorUrl = null;

    cacheController = new CacheController(DISSEMINATOR_NAME, "MediaPlayer") {

      @Override
      protected void createResources(HttpServletRequest request, RepresentationObject rep, String cacheURL,
        File cacheFile) throws Exception {
        MediaPlayer.this.createResources(request, rep, cacheURL, cacheFile);

      }

      @Override
      protected void sendResponse(HttpServletRequest request, RepresentationObject rep, String cacheURL,
        HttpServletResponse response) throws Exception {
        sendIndex(request, rep, cacheURL, response);
      }

    };
  }

  // protected RepresentationHelper getRepresentationHelper() throws
  // IOException {
  // if (representationHelper == null) {
  // representationHelper = new RepresentationHelper();
  // }
  // return representationHelper;
  // }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    int separatorIndex = pathInfo.indexOf('/', 2);
    separatorIndex = separatorIndex != -1 ? separatorIndex : pathInfo.length();
    String pid = pathInfo.substring(1, separatorIndex);
    try {
      cacheController.get(pid, request, response);
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

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  /**
   * Create media player resources
   * 
   * @param session
   * @param rep
   * @param cacheURL
   * @param cache
   * @throws RODAClientException
   * @throws LoginException
   * @throws MigratorClientException
   * @throws ConverterException
   * @throws IOException
   */
  private void createResources(HttpServletRequest request, RepresentationObject rep, String cacheURL, File cache)
  // throws UnsupportedContentModel, LoginException,
  // RODAClientException, MigratorClientException, ConverterException,
  // IOException
  {

    if (cache.canWrite()) {
      // clear resources
      cache.delete();
      cache.mkdir();

      // // Convert representation
      // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
      // .getSession());
      // SynchronousConverter service;
      //
      // String extension;
      //
      // if (rep.getType().equals(RepresentationObject.VIDEO)) {
      // service = migratorClient.getSynchronousConverterService(
      // videoMigratorUrl,
      // rodaClient.getCup(),rodaClient.getCasUtility());
      // extension = ".flv";
      //
      // } else if (rep.getType().equals(RepresentationObject.AUDIO)) {
      // service = migratorClient.getSynchronousConverterService(
      // audioMigratorUrl,
      // rodaClient.getCup(),rodaClient.getCasUtility());
      // extension = ".mp3";
      //
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
      // // Add file name extension to result files
      // for (File file : cache.listFiles()) {
      // FileUtils.moveFile(file, new File(file.getAbsolutePath()
      // + extension));
      // }

    }
  }

  private void sendIndex(HttpServletRequest request, RepresentationObject rep, String cacheURL,
    HttpServletResponse response) throws IOException
  // ,UnsupportedContentModel
  {

    response.setContentType("text/html");
    OutputStream out = response.getOutputStream();

    InputStream indexTemplate = SimpleViewer.class.getClassLoader()
      .getResourceAsStream("/org/roda/disseminators/mediaplayer/index.html");

    String filename = null;
    // FIXME
    // if (rep.getType().equals(RepresentationObject.VIDEO)) {
    // filename = "F0.flv";
    //
    // } else if (rep.getType().equals(RepresentationObject.AUDIO)) {
    // filename = "F0.mp3";
    //
    // } else {
    // logger.error("Unsuported representation type: " + rep.getContentModel());
    // // throw new UnsupportedContentModel("" + rep.getContentModel());
    // }
    //
    // String mediaURL = cacheURL + rep.getPid() + "/" + DISSEMINATOR_NAME + "/"
    // + filename;

    String index = new String(StreamUtils.getBytes(indexTemplate));

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
    // index = index.replaceAll("\\@TITLE", title);
    // index = index.replaceAll("\\@VIDEOURL", mediaURL);
    // index = index.replaceAll("\\@CONTEXT", request.getContextPath());
    // PrintWriter printer = new PrintWriter(out);
    // printer.write(index);
    // printer.flush();
    // printer.close();
  }
}
