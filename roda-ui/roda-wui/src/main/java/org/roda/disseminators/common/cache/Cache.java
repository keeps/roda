/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.common.cache;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.roda.core.common.BrowserException;
import org.roda.core.common.LoginException;
import org.roda.core.common.NoSuchRODAObjectException;
import org.roda.core.common.RODAClientException;

/**
 * Cache implementation
 * 
 * @author Luis Faria
 * 
 */
public class Cache extends HttpServlet implements Servlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  private static File cacheDir = null;

  private static File getCacheDir() {
    if (cacheDir == null) {
      // cacheDir = new File(RodaClientFactory.getRodaProperties()
      // .getProperty("roda.disseminators.cache.path"));
    }
    return cacheDir;
  }

  private static final Logger logger = Logger.getLogger(Cache.class);

  /**
   * Cache constructor
   */
  public Cache() {

    if (!cacheDir.isDirectory()) {
      logger.warn("Cache path does not point to a directory");
    }

    if (!cacheDir.canRead()) {
      logger.warn("No read permissions of cache directory");
    }

    if (!cacheDir.canWrite()) {
      logger.warn("No write permissions of cache directory");
    }
  }

  /**
   * Are the disseminator resources for an object cached
   * 
   * @param pid
   *          the object PID
   * @param lastModifiedDate
   *          the object last modification date
   * @param disseminatorName
   *          the disseminator name
   * @return true if cached
   */
  public static boolean isCached(final String pid, final String disseminatorName) {
    boolean isCached;
    File[] pidFiles = getCacheDir().listFiles(new FilenameFilter() {

      public boolean accept(File parent, String filename) {
        return filename.equals(pid);
      }

    });

    if (pidFiles == null) {
      logger.error(getCacheDir().getAbsolutePath() + " is not a directory or an I/O error occured");
      isCached = false;
    } else if (pidFiles.length > 0) {
      if (pidFiles.length > 1) {
        logger.warn("Found more than one directory for the same pid," + " ignoring");
      }
      isCached = Arrays.asList(pidFiles[0].list()).contains(disseminatorName);
      logger.debug(pid + " contain resources for " + disseminatorName + ": " + isCached);
    } else {
      isCached = false;
      logger.debug(pid + " folder not yet created");
    }

    logger.debug(disseminatorName + " resources for " + pid + (isCached ? " are cached" : " are not cached"));

    return isCached;
  }

  /**
   * Are the disseminator resources for an object cached
   * 
   * @param request
   * 
   * @param pid
   *          the object PID
   * @param disseminatorName
   *          the disseminator name
   * @return true if cached
   * @throws BrowserException
   * @throws LoginException
   * @throws NoSuchRODAObjectException
   * @throws GWTServiceException
   * @throws RemoteException
   * @throws RODAClientException
   */
  public boolean isCached(HttpServletRequest request, String pid, String disseminatorName) throws BrowserException,
    LoginException, NoSuchRODAObjectException, RemoteException, RODAClientException {
    return isCached(pid, disseminatorName);
  }

  /**
   * Get cache file
   * 
   * @param pid
   * @param lastModifiedDate
   * @param disseminatorName
   * @return the cached file
   */
  public static File getCacheFile(final String pid, final String disseminatorName) {
    logger.debug("getting cache for pid=" + pid + " disseminator=" + disseminatorName);

    File pidFile = getDirectory(getCacheDir(), pid);
    File disseminatorFile = getDirectory(pidFile, disseminatorName);
    return disseminatorFile;
  }

  /**
   * Get cache base file
   * 
   * @param request
   * @param pid
   * @param disseminatorName
   * @return the cached file
   * @throws BrowserException
   * @throws LoginException
   * @throws RemoteException
   * @throws NoSuchRODAObjectException
   * @throws RODAClientException
   * @throws GWTServiceException
   */
  public static File getCacheFile(HttpServletRequest request, final String pid, final String disseminatorName)
    throws BrowserException, LoginException, RemoteException, NoSuchRODAObjectException, RODAClientException {
    return getCacheFile(pid, disseminatorName);
  }

  /**
   * Get cache resource
   * 
   * @param request
   * 
   * @param pid
   * @param disseminatorName
   * @param path
   * @return the cached resource
   * @throws GWTServiceException
   * @throws RODAClientException
   * @throws NoSuchRODAObjectException
   * @throws RemoteException
   * @throws LoginException
   * @throws BrowserException
   */
  public static File getCacheResource(HttpServletRequest request, final String pid, final String disseminatorName,
    String path) throws BrowserException, LoginException, RemoteException, NoSuchRODAObjectException,
    RODAClientException {
    return new File(getCacheFile(request, pid, disseminatorName).getAbsolutePath() + path);

  }

  private static File getDirectory(File parent, final String filename) {
    File[] childFiles = parent.listFiles(new FilenameFilter() {

      public boolean accept(File parent, String otherFilename) {
        return otherFilename.equals(filename == null ? "null" : filename);
      }

    });

    File childFile;
    if (childFiles.length > 1) {
      logger.warn("Found more than one directory for the same pid," + " ignoring");
    }

    if (childFiles.length == 0) {
      childFile = new File(parent.getAbsolutePath() + "/" + filename);
      if (!childFile.mkdir()) {
        logger.warn("Could not create folder: " + childFile.getAbsolutePath());
      }
    } else {
      childFile = childFiles[0];
    }
    return childFile;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String path = request.getPathInfo();
    logger.info("path info =" + request.getPathInfo());
    String[] parsedPath = path.split("/");

    if (parsedPath.length > 1) {
      String pid = parsedPath[1];
      String disseminator = parsedPath[2];
      String relativePath = "";
      for (int i = 3; i < parsedPath.length; i++) {
        relativePath += "/" + parsedPath[i];
      }
      logger.info("pid=" + pid + " disseminator=" + disseminator + " relative path=" + relativePath);
      // try {
      // // test if current user has access permissions
      // RodaClientFactory.getRodaClient(request.getSession())
      // .getBrowserService().getRepresentationObject(pid);
      //
      // File cacheFile = getCacheFile(pid, disseminator);
      // File resource = new File(cacheFile.getAbsolutePath()
      // + relativePath);
      // if (resource.exists()) {
      // response.setContentLength((int) resource.length());
      // FileHelper.copy(new FileInputStream(resource), response
      // .getOutputStream());
      // } else {
      // response.sendError(HttpServletResponse.SC_NOT_FOUND);
      // }
      //
      // } catch (LoginException e) {
      // logger.error("Login Exception", e);
      // response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      // } catch (NoSuchRODAObjectException e) {
      // logger.error("Login Exception", e);
      // response.sendError(HttpServletResponse.SC_NOT_FOUND);
      // } catch (BrowserException e) {
      // logger.error("Browser Exception", e);
      // response
      // .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      // } catch (RODAClientException e) {
      // logger.error("RODA Client Exception", e);
      // response
      // .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      // }
    } else {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

  }

  /**
   * Get cache URL based on request
   * 
   * @param req
   * @return the cache URL
   */
  public static String getCacheUrl(HttpServletRequest req) {
    String scheme = req.getScheme();
    String serverName = req.getServerName();
    int serverPort = req.getServerPort();
    String contextPath = req.getContextPath();

    String url = scheme + "://" + serverName + ":" + serverPort + contextPath + "/Cache/";
    return url;
  }

}
