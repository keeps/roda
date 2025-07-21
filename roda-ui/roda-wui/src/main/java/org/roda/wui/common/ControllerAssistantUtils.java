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

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.log.AuditLogRequestHeaders;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.util.IdUtils;
import org.roda.wui.common.model.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ControllerAssistantUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerAssistantUtils.class);

  private ControllerAssistantUtils() {
    // do nothing
  }

  protected static void registerAction(User user, String actionComponent, String actionMethod, String relatedObjectId,
    long duration, LogEntryState state, Object... parameters) {
    LogEntry logEntry = createLogEntry(user, actionComponent, actionMethod, relatedObjectId, duration, state,
      parameters);
    registerAction(logEntry);
  }

  public static void registerAction(RequestContext requestContext, String actionComponent, String actionMethod,
    String relatedObjectId, long duration, LogEntryState state, Object... parameters) {
    LogEntry logEntry = createLogEntry(requestContext, actionComponent, actionMethod, relatedObjectId, duration, state,
      parameters);
    registerAction(logEntry);
  }

  private static LogEntry createLogEntry(RequestContext requestContext, String actionComponent, String actionMethod,
    String relatedObjectId, long duration, LogEntryState state, Object... parameters) {
    List<LogEntryParameter> logParameters = getLogEntryParameters(actionComponent, actionMethod, parameters);
    return createLogEntry(requestContext, actionComponent, actionMethod, relatedObjectId, duration, state,
      logParameters);
  }

  private static LogEntry createLogEntry(User user, String actionComponent, String actionMethod, String relatedObjectId,
    long duration, LogEntryState state, Object... parameters) {
    List<LogEntryParameter> logParameters = getLogEntryParameters(actionComponent, actionMethod, parameters);
    RequestContext context = new RequestContext();
    context.setUser(user);
    return createLogEntry(context, actionComponent, actionMethod, relatedObjectId, duration, state, logParameters);
  }

  private static List<LogEntryParameter> getLogEntryParameters(String actionComponent, String actionMethod,
    Object... parameters) {
    List<LogEntryParameter> logParameters = new ArrayList<>();
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

    return logParameters;
  }

  private static LogEntry createLogEntry(RequestContext context, String actionComponent, String actionMethod,
    String relatedObjectId, long duration, LogEntryState state, List<LogEntryParameter> parameters) {
    LogEntry logEntry = new LogEntry();
    logEntry.setUUID(IdUtils.createUUID());
    logEntry.setAddress(context.getUser().getIpAddress());
    logEntry.setUsername(context.getUser().getName());
    logEntry.setActionComponent(actionComponent);
    logEntry.setActionMethod(actionMethod);
    logEntry.setParameters(parameters);
    logEntry.setDuration(duration);
    logEntry.setDatetime(new Date());
    logEntry.setRelatedObjectID(relatedObjectId);
    logEntry.setState(state);

    if (context.getRequest() != null) {
      AuditLogRequestHeaders requestHeaders = new AuditLogRequestHeaders(context.getRequest().getUuid(),
        context.getRequest().getReason(), context.getRequest().getType());
      logEntry.setAuditLogRequestHeaders(requestHeaders);
    }

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
