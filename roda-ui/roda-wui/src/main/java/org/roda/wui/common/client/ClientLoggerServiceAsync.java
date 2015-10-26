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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 * 
 */
public interface ClientLoggerServiceAsync {

  /**
   * Log at trace level
   * 
   * @param classname
   * @param object
   */
  public void trace(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at trace level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void trace(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at debug level
   * 
   * @param classname
   * @param object
   */
  public void debug(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at debug level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void debug(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at info level
   * 
   * @param classname
   * @param object
   */
  public void info(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at info level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void info(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at warn level
   * 
   * @param classname
   * @param object
   */
  public void warn(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at warn level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void warn(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at error level
   * 
   * @param classname
   * @param object
   */
  public void error(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at error level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void error(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at fatal level
   * 
   * @param classname
   * @param object
   */
  public void fatal(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at fatal level
   * 
   * @param classname
   * @param object
   * @param error
   */
  public void fatal(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log a page hit
   * 
   * @param pagename
   * @throws LoginException
   * @throws LoggerException
   */
  public void pagehit(String pagename, AsyncCallback<Void> callback);

}
