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
