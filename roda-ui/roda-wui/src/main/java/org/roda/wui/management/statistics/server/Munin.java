/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.management.statistics.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.roda.core.RodaCoreFactory;

/**
 * Servlet implementation class Munin
 */
public class Munin extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private Logger logger = Logger.getLogger(Munin.class);

  private String role;
  private String munin_dir;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public Munin() {
    super();

    role = RodaCoreFactory.getRodaConfiguration().getString("roda.wui.munin.role", null);
    munin_dir = RodaCoreFactory.getRodaConfiguration().getString("roda.wui.munin.dir", null);

  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaClient(request
    // .getSession());
    // User authenticatedUser = rodaClient.getAuthenticatedUser();
    //
    // if (authenticatedUser.hasRole(role)) {
    // String pathInfo = request.getPathInfo();
    // File resource = new File(munin_dir, pathInfo);
    // response.setContentLength((int) resource.length());
    // IOUtils.copy(new FileInputStream(resource), response
    // .getOutputStream());
    //
    // } else {
    // logger.warn("Access denied because user "
    // + authenticatedUser.getName() + " does not have role "
    // + role);
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User "
    // + authenticatedUser.getName() + " does not have role "
    // + role);
    // }
    //
    // } catch (LoginException e) {
    // logger.error("Error getting RODA Client", e);
    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
    // "Login exception: " + e.getMessage());
    // } catch (RODAClientException e) {
    // logger.error("Error getting RODA Client", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // } catch (Throwable e) {
    // logger.error("Error getting RODA Client", e);
    // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
    // .getMessage());
    // }
  }

}
