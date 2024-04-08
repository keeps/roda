/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ContextListener implements ServletContextListener {

  private static ServletContext context;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    setServletContext(servletContextEvent);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    destroy();
  }

  public static ServletContext getServletContext() {
    return context;
  }

  private static void setServletContext(ServletContextEvent servletContextEvent) {
    ContextListener.context = servletContextEvent.getServletContext();
  }

  private static void destroy() {
    context = null;
  }
}
