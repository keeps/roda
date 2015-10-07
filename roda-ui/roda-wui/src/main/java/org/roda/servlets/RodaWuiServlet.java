package org.roda.servlets;

import java.io.IOException;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.roda.common.RodaCoreFactory;

public class RodaWuiServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(ServletContextListener.class);
  private static final long serialVersionUID = 1523530268219980563L;

  @Override
  public void init() throws ServletException {
    RodaCoreFactory.instantiate();
    LOGGER.info("Init: ok...");
  }

  @Override
  public void destroy() {
    try {
      RodaCoreFactory.shutdown();
      LOGGER.info("Shudown: ok...");
    } catch (IOException e) {
      LOGGER.error("Error while shutting down " + RodaCoreFactory.class.getCanonicalName());
    }

  }
}
