/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.ConfigurationException;
import org.roda.core.RodaCoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RodaWuiServlet extends HttpServlet {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaWuiServlet.class);
  private static final long serialVersionUID = 1523530268219980563L;

  @Override
  public void init() throws ServletException {
    RodaCoreFactory.instantiate();
    try {
      RodaCoreFactory.addConfiguration("roda-wui.properties");
    } catch (ConfigurationException e) {
      LOGGER.error("Error while loading roda-wui properties", e);
    }

    RodaCoreFactory.addLogger("logback_wui.xml");

    LOGGER.info("Init: ok...");
  }

  @Override
  public void destroy() {
    try {
      RodaCoreFactory.shutdown();
      LOGGER.info("Shudown: ok...");
    } catch (IOException e) {
      LOGGER.error("Error while shutting down {}", RodaCoreFactory.class.getName());
    }

  }
}
