/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ControllerAssistantUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAssistantUtils.class);

  protected static void registerAction(User user, String actionComponent, String actionMethod,
    String relatedObjectId, long duration, LOG_ENTRY_STATE state, Object... parameters) {
    LogEntry logEntry = createLogEntry(user, actionComponent, actionMethod, relatedObjectId, duration, state,
      parameters);
    registerAction(logEntry);
  }

  private static LogEntry createLogEntry(User user, String actionComponent, String actionMethod,
    String relatedObjectId, long duration, LOG_ENTRY_STATE state, Object... parameters) {
    List<LogEntryParameter> logParameters = new ArrayList<LogEntryParameter>();
    if (parameters != null && parameters.length > 0) {
      if ((parameters.length % 2) != 0) {
        LOGGER.warn(
          "registerAction (actionComponent={}, actionMethod={}) failed because parameters array must have pairs of elements (even length)",
          actionComponent, actionMethod);
      } else {
        for (int i = 0; i < parameters.length; i += 2) {
          Object key = parameters[i];
          Object value = parameters[i + 1];
          logParameters.add(
            new LogEntryParameter(key != null ? key.toString() : "null", value != null ? value.toString() : "null"));
        }
      }
    }
    return createLogEntry(user, actionComponent, actionMethod, relatedObjectId, duration, state, logParameters);
  }

  private static LogEntry createLogEntry(User user, String actionComponent, String actionMethod,
    String relatedObjectId, long duration, LOG_ENTRY_STATE state, List<LogEntryParameter> parameters) {

    LogEntry logEntry = new LogEntry();
    logEntry.setId(UUID.randomUUID().toString());
    logEntry.setAddress(user.getIpAddress());
    logEntry.setUsername(user.getName());
    logEntry.setActionComponent(actionComponent);
    logEntry.setActionMethod(actionMethod);
    logEntry.setParameters(parameters);
    logEntry.setDuration(duration);
    logEntry.setDatetime(new Date());
    logEntry.setRelatedObjectID(relatedObjectId);
    logEntry.setState(state);

    return logEntry;
  }

  private static void registerAction(LogEntry logEntry) {
    try {
      RodaCoreFactory.getModelService().addLogEntry(logEntry, RodaCoreFactory.getLogPath());
    } catch (RODAException e) {
      LOGGER.error("Error registering action (actionComponent={}, actionMethod={})", logEntry.getActionComponent(),
        logEntry.getActionMethod(), e);
    }
  }
}
