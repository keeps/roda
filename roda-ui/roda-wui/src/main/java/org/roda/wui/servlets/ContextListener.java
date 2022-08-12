/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ContextListener implements ServletContextListener {

  private static ServletContext context;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    context = servletContextEvent.getServletContext();
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    context = null;
  }

  public static ServletContext getServletContext() {
    return context;
  }

}
