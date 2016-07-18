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
import org.roda.core.data.v2.user.RodaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME 20160714 hsilva: to be removed and code moved to ControllerAssistant
public abstract class RodaCoreService {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaCoreService.class);

  /**
   * @deprecated 20160718 hsilva: use
   *             {@link RodaCoreService#registerAction(RodaUser, String, String, String, long, LOG_ENTRY_STATE, Object...)}
   *             instead
   */
  public static void registerAction(RodaUser user, String actionComponent, String actionMethod, String aipId,
    long duration, Object... parameters) {
    registerAction(user, actionComponent, actionMethod, aipId, duration, LOG_ENTRY_STATE.UNKNOWN, parameters);
  }

  public static void registerAction(RodaUser user, String actionComponent, String actionMethod, String aipId,
    long duration, LOG_ENTRY_STATE state, Object... parameters) {
    LogEntry logEntry = createLogEntry(user, actionComponent, actionMethod, aipId, duration, state, parameters);
    registerAction(logEntry);
  }

  private static LogEntry createLogEntry(RodaUser user, String actionComponent, String actionMethod, String aipId,
    long duration, LOG_ENTRY_STATE state, Object... parameters) {
    List<LogEntryParameter> logParameters = new ArrayList<LogEntryParameter>();
    if (parameters != null && parameters.length > 0) {
      if ((parameters.length % 2) != 0) {
        LOGGER.warn("registerAction(" + actionComponent + "/" + actionMethod
          + ",...) failed because parameters array must have pairs of elements (even length)");
      } else {
        for (int i = 0; i < parameters.length; i += 2) {
          Object key = parameters[i];
          Object value = parameters[i + 1];
          logParameters.add(
            new LogEntryParameter(key != null ? key.toString() : "null", value != null ? value.toString() : "null"));
        }
      }
    }
    return createLogEntry(user, actionComponent, actionMethod, aipId, duration, state, logParameters);
  }

  private static LogEntry createLogEntry(RodaUser user, String actionComponent, String actionMethod, String aipId,
    long duration, LOG_ENTRY_STATE state, List<LogEntryParameter> parameters) {

    LogEntry logEntry = new LogEntry();
    logEntry.setId(UUID.randomUUID().toString());
    logEntry.setAddress(user.getIpAddress());
    logEntry.setUsername(user.getName());
    logEntry.setActionComponent(actionComponent);
    logEntry.setActionMethod(actionMethod);
    logEntry.setParameters(parameters);
    logEntry.setDuration(duration);
    logEntry.setDatetime(new Date());
    logEntry.setRelatedObjectID(aipId);
    logEntry.setState(state);

    return logEntry;
  }

  private static void registerAction(LogEntry logEntry) {
    try {
      RodaCoreFactory.getModelService().addLogEntry(logEntry, RodaCoreFactory.getLogPath());
    } catch (RODAException e) {
      LOGGER.error("Error registering action '" + logEntry.getActionComponent() + "'", e);
    }
  }
}
