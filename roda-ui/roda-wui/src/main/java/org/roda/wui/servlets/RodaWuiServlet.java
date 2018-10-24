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
    LOGGER.info("Starting up RODA Core...");
    RodaCoreFactory.instantiate();
    if (!RodaCoreFactory.instantiatedWithoutErrors()) {
      LOGGER.error(
        "RODA Core didn't start because errors have occurred! Therefore, RODA WUI cannot be started. Please see RODA logs to understand why...");
      throw new RodaCoreInstantiationException(
        "RODA Core didn't start because errors have occurred! Please see RODA logs to understand why...");
    } else {
      LOGGER.info("RODA Core started with success!");
    }
    try {
      LOGGER.info("Injecting RODA WUI configurations...");
      RodaCoreFactory.addConfiguration("roda-wui.properties");
      LOGGER.info("RODA WUI configurations injected with success!");
    } catch (ConfigurationException e) {
      LOGGER.error("RODA WUI configurations could not be injected!", e);
    }

    RodaCoreFactory.addLogger("logback_wui.xml");

    LOGGER.info("RODA WUI started with success!");
  }

  @Override
  public void destroy() {
    RodaCoreFactory.shutdown();
    LOGGER.info("Shutdown: ok...");
  }
}
