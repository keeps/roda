/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.common;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.roda.core.common.LoggerException;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.LogEntryParameter;
import org.roda.core.data.v2.RodaSimpleUser;
import org.roda.core.data.v2.User;
import org.roda.model.ModelServiceException;

// FIXME remove this RodaCoreService from roda-wui already provides these functionalities 
@Deprecated
public class LogUtility {
  private static final Logger logger = Logger.getLogger(LogUtility.class);

  public static void registerAction(RodaSimpleUser user, String action, String[] parameters, String description,
    long duration) {

    if (parameters != null && (parameters.length % 2) != 0) {
      logger.warn("registerAction(" + action
        + ",...) failed because parameters array must have pairs of elements (even length)");
    } else {

      LogEntryParameter[] logParameters = null;

      if (parameters != null) {

        logParameters = new LogEntryParameter[parameters.length / 2];

        for (int i = 0, j = 0; i < logParameters.length; i++, j = j + 2) {

          logParameters[i] = new LogEntryParameter(parameters[j], parameters[j + 1]);
        }

      } else {
        // no parameters
      }

      registerAction(user, action, logParameters, description, duration);
    }
  }

  // TODO: get client adress...
  public static void registerAction(RodaSimpleUser user, String action, LogEntryParameter[] parameters,
    String description, long duration) {

    if (user == null) {

      logger.debug("registerAction(" + action + ",...) failed because there's no user in context.");

    } else {

      if (parameters == null) {
        parameters = new LogEntryParameter[0];
      }

      LogEntry logEntry = new LogEntry();
      logEntry.setAddress("");
      logEntry.setUsername(user.getName());
      logEntry.setActionComponent(action);
      logEntry.setParameters(Arrays.asList(parameters));
      logEntry.setActionMethod(description.replaceAll("%username%", user.getName()));
      logEntry.setDuration(duration);

      try {
        RodaCoreFactory.getModelService().addLogEntry(logEntry, RodaCoreFactory.getLogPath());

      } catch (ModelServiceException e) {
        logger.error("registerAction(" + logEntry.getActionMethod() + ",...) failed because of a LoggerException - "
          + e.getMessage(), e);
      }

      // logEntryQueue.add(logEntry);
      //
      // synchronized (logEntryQueue) {
      // logEntryQueue.notify();
      // }

    }
  }

}
