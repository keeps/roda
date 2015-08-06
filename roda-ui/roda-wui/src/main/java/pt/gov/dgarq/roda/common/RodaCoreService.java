package pt.gov.dgarq.roda.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.roda.model.ModelServiceException;

import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.LogEntryParameter;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;

public abstract class RodaCoreService {
	private static final Logger LOGGER = Logger.getLogger(RodaCoreService.class);

	protected static void registerAction(CASUserPrincipal user, String actionComponent, String actionMethod,
			String aipId, long duration, String... parameters) {

		LogEntry logEntry = createLogEntry(user, actionComponent, actionMethod, aipId, duration, parameters);
		registerAction(logEntry);
	}

	protected static void registerAction(CASUserPrincipal user, String actionComponent, String actionMethod,
			String aipId, long duration, List<LogEntryParameter> parameters) {

		LogEntry logEntry = createLogEntry(user, actionComponent, actionMethod, aipId, duration, parameters);
		registerAction(logEntry);

	}

	protected static void registerAction(LogEntry logEntry) {
		try {
			RodaCoreFactory.getModelService().addLogEntry(logEntry, RodaCoreFactory.getLogPath());
		} catch (ModelServiceException e) {
			LOGGER.error("Error registering action '" + logEntry.getActionComponent() + "'", e);
		}
	}

	private static LogEntry createLogEntry(CASUserPrincipal user, String actionComponent, String actionMethod,
			String aipId, long duration, String... parameters) {
		LogEntry logEntry = null;
		List<LogEntryParameter> logParameters = null;

		if (parameters != null) {
			if ((parameters.length % 2) != 0) {

				LOGGER.warn("registerAction(" + actionComponent + "/" + actionMethod
						+ ",...) failed because parameters array must have pairs of elements (even length)");
			} else {

				logParameters = new ArrayList<LogEntryParameter>(parameters.length / 2);

				for (int i = 0, j = 0; i < logParameters.size(); i++, j = j + 2) {

					logParameters.add(new LogEntryParameter(parameters[j], parameters[j + 1]));
				}

			}
			logEntry = createLogEntry(user, actionComponent, actionMethod, aipId, duration, logParameters);
		}
		return logEntry;
	}

	private static LogEntry createLogEntry(CASUserPrincipal user, String actionComponent, String actionMethod,
			String aipId, long duration, List<LogEntryParameter> parameters) {
		if (parameters == null) {
			parameters = new ArrayList<LogEntryParameter>();
		}

		LogEntry logEntry = new LogEntry();
		logEntry.setId(UUID.randomUUID().toString());
		logEntry.setAddress(user.getClientIpAddress());
		logEntry.setUsername(user.getName());
		logEntry.setActionComponent(actionComponent);
		logEntry.setActionMethod(actionMethod);
		logEntry.setParameters(parameters);
		logEntry.setDuration(duration);
		logEntry.setDatetime(new Date());
		logEntry.setRelatedObjectID(aipId);

		return logEntry;
	}
}
