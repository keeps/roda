/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.server;

import org.apache.log4j.LogManager;
import org.roda.wui.common.client.ClientLoggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Client logging servlet implementation
 * 
 * @author Luis Faria
 * 
 */
public class ClientLoggerImpl extends RemoteServiceServlet implements ClientLoggerService {

  private static final long serialVersionUID = 3694771724613537482L;

  private String getUserInfo() {
    String ret;
    String address = this.getThreadLocalRequest().getRemoteAddr();
    ret = "[" + address + "] ";
    return ret;
  }

  public void debug(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(constructMessage(getUserInfo(), object));
  }

  public void debug(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(constructMessage(getUserInfo(), object), error);
  }

  public void error(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object);
    sendError(classname, object, null);

  }

  public void error(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object, error);
    sendError(classname, object, error);
  }

  public void fatal(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object);
    sendError(classname, object, null);
  }

  public void fatal(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object, error);
    sendError(classname, object, error);
  }

  public void info(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(constructMessage(getUserInfo(), object));
  }

  public void info(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(constructMessage(getUserInfo(), object), error);
  }

  public void trace(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(constructMessage(getUserInfo(), object));
  }

  public void trace(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(constructMessage(getUserInfo(), object), error);
  }

  public void warn(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(constructMessage(getUserInfo(), object));
  }

  public void warn(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(constructMessage(getUserInfo(), object), error);
  }

  private String constructMessage(String userInfo, String object) {
    return String.format("%s%s", userInfo, object);
  }

  public void pagehit(String pagename) {
    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaWuiClient();
    // String username = RodaClientFactory.getRodaClient(
    // this.getThreadLocalRequest().getSession()).getUsername();
    // LogEntryParameter[] parameters = new LogEntryParameter[] {
    // new LogEntryParameter("hostname", getThreadLocalRequest()
    // .getRemoteHost()),
    // new LogEntryParameter("address", getThreadLocalRequest()
    // .getRemoteAddr()),
    // new LogEntryParameter("user", getThreadLocalRequest()
    // .getRemoteUser()),
    // new LogEntryParameter("pagename", pagename) };
    //
    // LogEntry logEntry = new LogEntry();
    // logEntry.setAction(LOG_ACTION_WUI_PAGEHIT);
    // logEntry.setParameters(parameters);
    // logEntry.setUsername(username);
    //
    // rodaClient.getLoggerService().addLogEntry(logEntry);
    // } catch (RemoteException e) {
    // logger.error("Error logging page hit", e);
    // } catch (RODAClientException e) {
    // logger.error("Error logging page hit", e);
    // } catch (LoginException e) {
    // logger.error("Error logging page hit", e);
    // } catch (LoggerException e) {
    // logger.error("Error logging page hit", e);
    // }
  }

  public void destroy() {
    super.destroy();
    LogManager.shutdown();
  }

  /**
   * Send error to logging services
   * 
   * @param classname
   *          the name of the class that generated the error
   * @param message
   *          the error message
   * @param error
   *          the error throwable
   */
  public void sendError(String classname, String message, Throwable error) {

    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaWuiClient();
    // String username = RodaClientFactory.getRodaClient(
    // this.getThreadLocalRequest().getSession()).getUsername();
    // List<LogEntryParameter> parameters = new Vector<LogEntryParameter>();
    // parameters.add(new LogEntryParameter("hostname",
    // getThreadLocalRequest().getRemoteHost()));
    // parameters.add(new LogEntryParameter("address",
    // getThreadLocalRequest().getRemoteAddr()));
    // parameters.add(new LogEntryParameter("port",
    // getThreadLocalRequest().getRemotePort() + ""));
    // parameters.add(new LogEntryParameter("classname", classname));
    // parameters.add(new LogEntryParameter("error", message));
    // if (error != null) {
    // parameters.add(new LogEntryParameter("message", error
    // .getMessage()));
    // }
    //
    // LogEntry logEntry = new LogEntry();
    // logEntry.setAction(LOG_ACTION_WUI_ERROR);
    // logEntry.setParameters(parameters
    // .toArray(new LogEntryParameter[parameters.size()]));
    // logEntry.setUsername(username);
    //
    // rodaClient.getLoggerService().addLogEntry(logEntry);
    // } catch (RemoteException e) {
    // logger.error("Error logging login", e);
    // } catch (LoginException e) {
    // logger.error("Error logging login", e);
    // } catch (LoggerException e) {
    // logger.error("Error logging login", e);
    // } catch (RODAClientException e) {
    // logger.error("Error logging login", e);
    // }
  }

}
