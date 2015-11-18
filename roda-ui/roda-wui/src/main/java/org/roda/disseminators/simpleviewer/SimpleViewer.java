/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.simpleviewer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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

/**
 * Servlet implementation class for Servlet: SimpleViewer
 */
public class SimpleViewer extends HttpServlet implements Servlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Name of the disseminator
   */
  public static final String DISSEMINATOR_NAME = "SimpleViewer";

  private Logger logger = LoggerFactory.getLogger(SimpleViewer.class);

  // private final MigratorClient migratorClient;
  private final String migratorUrl;

  private final CacheController cacheController;

  /**
   * Create a new instance of the SimpleViewer disseminator servlet
   * 
   */
  public SimpleViewer() {
    // migratorClient = new MigratorClient();
    // migratorUrl = RodaClientFactory.getRodaProperties().getProperty(
    // "roda.disseminators.simpleviewer.migrator");
    migratorUrl = null;

    cacheController = new CacheController(DISSEMINATOR_NAME, "SimpleViewer") {

      @Override
      protected void createResources(HttpServletRequest request, RepresentationObject rep, String cacheURL,
        File cacheFile) throws Exception {
        SimpleViewer.this.createResources(request, rep, cacheURL, cacheFile);
      }

      @Override
      protected void sendResponse(HttpServletRequest request, RepresentationObject rep, String cacheURL,
        HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        sendIndex(request, rep, cacheURL, response.getOutputStream());

      }

    };

  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    int separatorIndex = pathInfo.indexOf('/', 2);
    separatorIndex = separatorIndex != -1 ? separatorIndex : pathInfo.length();
    String pid = pathInfo.substring(1, separatorIndex);
    try {
      logger.debug("Getting pid " + pid);
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

  /**
   * Create the object dissemination resources in cache
   * 
   * @param request
   * 
   * @param rep
   *          the representation object
   * @param cacheURL
   *          the cache URL that refers to this object dissemination
   * @param cache
   *          the cache directory file referent to this object dissemination
   * @throws Exception
   */
  public void createResources(HttpServletRequest request, RepresentationObject rep, String cacheURL, File cache)
    throws Exception {

    logger.debug("Creating resources of " + rep);

    // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
    // .getSession());
    // SynchronousConverter service = migratorClient
    // .getSynchronousConverterService(migratorUrl, rodaClient
    // .getCup(),rodaClient.getCasUtility());
    // RepresentationObject converted = service.convert(rep)
    // .getRepresentation();
    //
    // // Download converted representation
    // migratorClient.writeRepresentationObject(converted, cache);
    //
    // // Make modifications on image and thumbs path in gallery.xml
    // File galleryFile = new File(cache, converted.getRootFile().getId());
    //
    // String imagePath = "../../Cache/" + rep.getPid() + "/"
    // + DISSEMINATOR_NAME + "/images/";
    // String thumbPath = "../../Cache/" + rep.getPid() + "/"
    // + DISSEMINATOR_NAME + "/thumbs/";
    //
    // String galleryXml = new String(StreamUtils
    // .getBytes(new FileInputStream(galleryFile)));
    // galleryXml = galleryXml.replaceAll("\\@IMAGEPATH", Matcher
    // .quoteReplacement(imagePath));
    // galleryXml = galleryXml.replaceAll("\\@THUMBPATH", Matcher
    // .quoteReplacement(thumbPath));
    //
    // PrintWriter printer = new PrintWriter(new
    // FileOutputStream(galleryFile));
    // printer.write(galleryXml);
    // printer.flush();
    // printer.close();
    //
    // // Move images to 'images' folder
    // File images = new File(cache, "images");
    // images.mkdir();
    // File[] imageFiles = cache.listFiles(new FilenameFilter() {
    //
    // public boolean accept(File dir, String name) {
    // return name.matches("image_.*");
    // }
    //
    // });
    //
    // for (File imageFile : imageFiles) {
    // String name = imageFile.getName();
    // File newImageFile = new File(images, name.substring(name
    // .indexOf('_') + 1));
    // FileUtils.moveFile(imageFile, newImageFile);
    // }
    //
    // // Move thumbs to 'thumbs' folder
    // File thumbs = new File(cache, "thumbs");
    // thumbs.mkdir();
    // File[] thumbFiles = cache.listFiles(new FilenameFilter() {
    //
    // public boolean accept(File dir, String name) {
    // return name.matches("thumb_.*");
    // }
    //
    // });
    //
    // for (File thumbFile : thumbFiles) {
    // String name = thumbFile.getName();
    // File newThumbFile = new File(thumbs, name.substring(name
    // .indexOf('_') + 1));
    // FileUtils.moveFile(thumbFile, newThumbFile);
    // }
    //
    // // delete migration from cache
    // migratorClient.deleteCachedRepresentation(converted);
  }

  private void sendIndex(HttpServletRequest request, RepresentationObject rep, String cacheURL, OutputStream out)
    throws IOException {

    logger.debug("Sending index of " + rep);

    InputStream indexTemplate = SimpleViewer.class.getClassLoader()
      .getResourceAsStream("/org/roda/disseminators/simpleviewer/index.html");
    // String galleryURL = cacheURL + rep.getPid() + "/" + DISSEMINATOR_NAME +
    // "/F0.xml";
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
    // index = index.replaceAll("\\@TITLE",
    // Matcher.quoteReplacement(title));
    // index = index.replaceAll("\\@GALLERYURL", Matcher
    // .quoteReplacement(galleryURL));
    // index = index.replaceAll("\\@CONTEXT", request.getContextPath());
    // PrintWriter printer = new PrintWriter(out);
    // printer.write(index);
    // printer.flush();
    // printer.close();
  }

}
