/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.disseminators.PhpMyAdmin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class for Servlet: PhpMyAdmin
 * 
 */
public class PhpMyAdmin extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  /**
   * Name of the disseminator
   */
  public static final String DISSEMINATOR_NAME = "PhpMyAdmin";

  private static final String PROPERTY_MYSQL_HOST = "roda.disseminators.phpmyadmin.mysql.host";

  private static final String PROPERTY_MYSQL_PORT = "roda.disseminators.phpmyadmin.mysql.port";

  private static final String PROPERTY_MYSQL_ADMIN_USER = "roda.disseminators.phpmyadmin.mysql.admin.user";

  private static final String PROPERTY_MYSQL_ADMIN_PASS = "roda.disseminators.phpmyadmin.mysql.admin.pass";

  private static final String PROPERTY_DATABASE = "roda.disseminators.phpmyadmin.database";

  private static final String PROPERTY_COLUMN_INFO = "roda.disseminators.phpmyadmin.column_info";

  private static final String PROPERTY_URL = "roda.disseminators.phpmyadmin.url";

  private Logger logger = LoggerFactory.getLogger(PhpMyAdmin.class);

  private final String mysqlHost;

  private final int mysqlPort;

  private final String mysqlAdminUser;

  private final String mysqlAdminPass;

  private final String pmaDatabase;

  private final String pmaColumnInfo;

  private final String pmaUrl;

  private Set<String> exportingPIDs;

  // private final MigratorClient migratorClient;
  private final String migratorUrl;

  /**
   * Create a new instance of PhpMyAdmin disseminator servlet. Properties will
   * be read from the main configuration file
   * 
   */
  public PhpMyAdmin() {
    super();
    // Properties rodaProperties = RodaClientFactory.getRodaProperties();
    // mysqlHost = rodaProperties.getProperty(PROPERTY_MYSQL_HOST);
    // mysqlPort = Integer.valueOf(
    // rodaProperties.getProperty(PROPERTY_MYSQL_PORT)).intValue();
    // mysqlAdminUser =
    // rodaProperties.getProperty(PROPERTY_MYSQL_ADMIN_USER);
    // mysqlAdminPass =
    // rodaProperties.getProperty(PROPERTY_MYSQL_ADMIN_PASS);
    // pmaDatabase = rodaProperties.getProperty(PROPERTY_DATABASE);
    // pmaColumnInfo = rodaProperties.getProperty(PROPERTY_COLUMN_INFO);
    // pmaUrl = rodaProperties.getProperty(PROPERTY_URL);
    pmaUrl = null;
    pmaDatabase = null;
    pmaColumnInfo = null;
    mysqlHost = null;
    mysqlPort = 3306;
    mysqlAdminUser = null;
    mysqlAdminPass = null;

    exportingPIDs = new HashSet<String>();

    // migratorClient = new MigratorClient();
    // migratorUrl = RodaClientFactory.getRodaProperties().getProperty(
    // "roda.disseminators.phpmyadmin.migrator");
    migratorUrl = null;

    logger.debug("Using MySQL host: " + mysqlHost + " port: " + mysqlPort);
  }

  // private PhpMyAdminExportModule getExportModule(String databaseName) {
  // return new PhpMyAdminExportModule(mysqlHost, mysqlPort, databaseName,
  // mysqlAdminUser, mysqlAdminPass, pmaDatabase, pmaColumnInfo);
  // }

  protected void loading(String pid, HttpServletRequest request, HttpServletResponse response) throws IOException {
    // String servletUrl = RodaClientFactory.getServletUrl(request);
    // response.sendRedirect(servletUrl + "/Loading.html#" + servletUrl
    // + "/PhpMyAdmin/" + pid);
  }

  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
    IOException {
    String pathInfo = request.getPathInfo();
    int separatorIndex = pathInfo.indexOf('/', 2);
    separatorIndex = separatorIndex != -1 ? separatorIndex : pathInfo.length();
    String pid = pathInfo.substring(1, separatorIndex);
    String databaseName = pid.replace(':', '_');
    // try {
    // final PhpMyAdminExportModule exportModule =
    // getExportModule(databaseName);
    //
    // // if (exportingPIDs.contains(pid)) {
    // // loading(pid, request, response);
    // // } else if (!exportModule.databaseExists()) {
    // // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
    // // .getSession());
    // // final RepresentationObject rep = rodaClient.getBrowserService()
    // // .getRepresentationObject(pid);
    // // final SynchronousConverter service = migratorClient
    // // .getSynchronousConverterService(migratorUrl, rodaClient
    // // .getCup(),rodaClient.getCasUtility());
    // // new Thread() {
    // // public void run() {
    // // try {
    // // service.convert(rep);
    // // } catch (ConverterException e) {
    // // logger.error("Error exporting database", e);
    // // } catch (RemoteException e) {
    // // logger.error("Error exporting database", e);
    // // }
    // // exportingPIDs.remove(rep.getPid());
    // // }
    // // }.start();
    // //
    // // loading(pid, request, response);
    // // RodaClientFactory.log(DISSEMINATOR_NAME, false, pid, request);
    // // } else {
    // // response.sendRedirect(pmaUrl + "/index.php?db=" + databaseName);
    // // RodaClientFactory.log(DISSEMINATOR_NAME, true, pid, request);
    // // }
    //
    // // } catch (ModuleException e) {
    // // if (e.getModuleErrors() != null) {
    // // for (Entry<String, Throwable> entry : e.getModuleErrors()
    // // .entrySet()) {
    // // logger.error(entry.getKey(), entry.getValue());
    // // }
    // // }
    // // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // // e
    // // .getMessage());
    // // } catch (LoginException e) {
    // // logger.error("Error getting phpMyAdmin disseminator", e);
    // // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // // e
    // // .getMessage());
    // // } catch (RODAClientException e) {
    // // logger.error("Error getting phpMyAdmin disseminator", e);
    // // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // // e
    // // .getMessage());
    // // } catch (BrowserException e) {
    // // logger.error("Error getting phpMyAdmin disseminator", e);
    // // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // // e
    // // .getMessage());
    // // } catch (NoSuchRODAObjectException e) {
    // // logger.error("Error getting phpMyAdmin disseminator", e);
    // // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // // e
    // // .getMessage());
    // // } catch (MigratorClientException e) {
    // // logger.error("Error getting phpMyAdmin disseminator", e);
    // // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    // // e
    // // .getMessage());
    // // } catch (RemoteException e) {
    // // RODAException exception = RODAClient.parseRemoteException(e);
    // // if (exception instanceof AuthorizationDeniedException) {
    // // response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
    // // exception.getMessage());
    // // } else {
    // // logger.error("RODA Exception", e);
    // // response.sendError(
    // // HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // // .getMessage());
    // // }
    //
    // } catch (Throwable e) {
    // logger.error("Uncaught exception", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}