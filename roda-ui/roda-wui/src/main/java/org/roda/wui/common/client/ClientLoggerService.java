/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client;

import org.roda.core.data.exceptions.LoggerException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 * 
 */
public interface ClientLoggerService extends RemoteService {

  /**
   * logger service URI
   */
  public static final String SERVICE_URI = "wuilogger";

  /**
   * Utilities
   * 
   */
  public static class Util {

    /**
     * Get service instance
     * 
     * @return
     */
    public static ClientLoggerServiceAsync getInstance() {

      ClientLoggerServiceAsync instance = (ClientLoggerServiceAsync) GWT.create(ClientLoggerService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  /**
   * Log at trace level
   * 
   * @param classname
   * @param object
   */
  public void trace(String classname, String object);

  /**
   * Log at trace level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void trace(String classname, String object, Throwable error);

  /**
   * Log at debug level
   * 
   * @param classname
   * @param object
   */
  public void debug(String classname, String object);

  /**
   * Log at debug level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void debug(String classname, String object, Throwable error);

  /**
   * Log at info level
   * 
   * @param classname
   * @param object
   */
  public void info(String classname, String object);

  /**
   * Log at info level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void info(String classname, String object, Throwable error);

  /**
   * Log at warn level
   * 
   * @param classname
   * @param object
   */
  public void warn(String classname, String object);

  /**
   * Log at warn level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void warn(String classname, String object, Throwable error);

  /**
   * Log at error level
   * 
   * @param classname
   * @param object
   */
  public void error(String classname, String object);

  /**
   * Log at error level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void error(String classname, String object, Throwable error);

  /**
   * Log at fatal level
   * 
   * @param classname
   * @param object
   */
  public void fatal(String classname, String object);

  /**
   * Log at fatal level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void fatal(String classname, String object, Throwable error);

  /**
   * Log a page hit
   * 
   * @param pagename
   * @throws LoginException
   * @throws LoggerException
   */
  public void pagehit(String pagename) throws LoggerException;

}
