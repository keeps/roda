/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class GwtCacheServer
 */
public class GwtCacheServer extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(GwtCacheServer.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public GwtCacheServer() {
    super();
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String path = request.getServletPath();
    logger.debug("servlet path: " + request.getServletPath());

    // Set content type
    if (path.endsWith(".html")) {
      response.setContentType("text/html");
    } else if (path.endsWith(".png")) {
      response.setContentType("image/png");
    } else if (path.endsWith(".gif")) {
      response.setContentType("image/gif");
    }

    // Set headers
    response.setHeader("Pragma", "");
    response.setHeader("Cache-Control", "private");

    Calendar c = GregorianCalendar.getInstance();
    c.add(Calendar.MONTH, 1);
    response.setHeader("Expires", c.getTime().toString());

    // Send content
    InputStream resource = request.getSession().getServletContext().getResourceAsStream(path);

    if (resource != null) {
      IOUtils.copy(resource, response.getOutputStream());
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, path + " was not found in resources");
    }

  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

}
