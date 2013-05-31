package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Rui Castro
 * 
 */
public class LogEntry implements Serializable {
	private static final long serialVersionUID = -178083792639806983L;

	public static final String[] ACCEPT_SIP_ACTIONS = new String[] { "AcceptSIP.acceptSIP" };

	public static final String[] BROWSER_ACTIONS = new String[] {
			"Browser.getRODAObject", "Browser.getRODAObjectCount",
			"Browser.getRODAObjects", "Browser.getSimpleDescriptionObject",
			"Browser.getSimpleDescriptionObjectCount",
			"Browser.getSimpleDescriptionObjects",
			"Browser.getSimpleDescriptionObjectIndex",
			"Browser.getDescriptionObject", "Browser.getDOPIDs",
			"Browser.getDOAncestorPIDs", "Browser.getDORepresentations",
			"Browser.getDOOriginalRepresentation",
			"Browser.getDONormalizedRepresentation",
			"Browser.getDOPreservationObjects",
			"Browser.getSimpleRepresentationObject",
			"Browser.getSimpleRepresentationObjectCount",
			"Browser.getSimpleRepresentationObjects",
			"Browser.getRepresentationObject", "Browser.getRepresentationFile",
			"Browser.getROPreservationObject",
			"Browser.getSimpleRepresentationPreservationObject",
			"Browser.getSimpleRepresentationPreservationObjectCount",
			"Browser.getSimpleRepresentationPreservationObjects",
			"Browser.getRepresentationPreservationObject",
			"Browser.getSimpleEventPreservationObject",
			"Browser.getSimpleEventPreservationObjectCount",
			"Browser.getSimpleEventPreservationObjects",
			"Browser.getEventPreservationObject",
			"Browser.getPreservationEvents",
			"Browser.getAgentPreservationObject",
			"Browser.getRODAObjectPermissions",
			"Browser.getRODAObjectUserPermissions",
			"Browser.hasModifyPermission", "Browser.hasRemovePermission",
			"Browser.hasGrantPermission", "Browser.getProducers", };

	public static final String[] EDITOR_ACTIONS = new String[] {
			"Editor.createDescriptionObject", "Editor.modifyDescriptionObject",
			"Editor.removeDescriptionObject", "Editor.getDOPossibleLevels",
			"Editor.setRODAObjectPermissions", "Editor.setProducers", };

	public static final String[] FILE_ACCESS_SERVLET_ACTIONS = new String[] { "FileAccessServlet.GET" };

	public static final String[] FILE_UPLOAD_SERVLET_ACTIONS = new String[] { "FileUploadServlet.POST" };

	public static final String[] INGEST_ACTIONS = new String[] {
			"Ingest.createDetachedDescriptionObject",
			"Ingest.createDescriptionObject", "Ingest.removeDescriptionObject",
			"Ingest.createRepresentationObject",
			"Ingest.setDONormalizedRepresentation",
			"Ingest.registerIngestEvent", "Ingest.registerEvent",
			"Ingest.registerDerivationEvent", "Ingest.removeObjects", };

	public static final String[] INGEST_MONITOR_ACTIONS = new String[] {
			"IngestMonitor.getSIPsCount", "IngestMonitor.getSIPs",
			"IngestMonitor.getPossibleStates", };

	public static final String[] LOGGER_ACTIONS = new String[] { "Logger.addLogEntry", };

	public static final String[] LOGIN_ACTIONS = new String[] {
			"Login.getAuthenticatedUser", "Login.getGuestUser",
			"Login.getGuestCredentials", };

	public static final String[] PLUGINS_ACTIONS = new String[] { "Plugins.getPluginsInfo", };

	public static final String[] REPORTS_ACTIONS = new String[] {
			"Reports.getReport", "Reports.getReportsCount",
			"Reports.getReports", };

	public static final String[] SCHEDULER_ACTIONS = new String[] {
			"Scheduler.getTasks", "Scheduler.getTaskCount",
			"Scheduler.getTask", "Scheduler.addTask", "Scheduler.modifyTask",
			"Scheduler.removeTask", "Scheduler.getTaskInstance",
			"Scheduler.getTaskInstances", "Scheduler.getTaskInstanceCount", };

	public static final String[] SEARCH_ACTIONS = new String[] {
			"Search.basicSearch", "Search.advancedSearch", };

	public static final String[] SIP_UPLOAD_SERVLET_ACTIONS = new String[] { "SIPUploadServlet.POST" };

	public static final String[] STATISTICS_ACTIONS = new String[] {
			"Statistics.insertStatisticData",
			"Statistics.insertStatisticDataList", };

	public static final String[] STATISTICS_MONITOR_ACTIONS = new String[] {
			"StatisticsMonitor.getStatisticDataCount",
			"StatisticsMonitor.getStatisticData", };

	public static final String[] USER_BROWSER_ACTIONS = new String[] {
			"UserBrowser.getGroupCount", "UserBrowser.getGroup",
			"UserBrowser.getGroups", "UserBrowser.getUsersInGroup",
			"UserBrowser.getUserCount", "UserBrowser.getUser",
			"UserBrowser.getUsers", "UserBrowser.getUserNames",
			"UserBrowser.getRoles", "UserBrowser.getGroupDirectRoles",
			"UserBrowser.getUserDirectRoles", };

	public static final String[] USER_EDITOR_ACTIONS = new String[] { "UserEditor.modifyUser", };

	public static final String[] USER_MANAGEMENT_ACTIONS = new String[] {
			"UserManagement.addGroup", "UserManagement.modifyGroup",
			"UserManagement.removeGroup", "UserManagement.addUser",
			"UserManagement.modifyUser", "UserManagement.removeUser",
			"UserManagement.setUserPassword", };

	public static final String[] USER_REGISTRATION_ACTIONS = new String[] {
			"UserRegistration.registerUser",
			"UserRegistration.getUnconfirmedUser",
			"UserRegistration.modifyUnconfirmedEmail",
			"UserRegistration.confirmUserEmail",
			"UserRegistration.requestPasswordReset",
			"UserRegistration.resetUserPassword", };

	public static final String[] RODAWUI_ACTIONS = new String[] {
			"RODAWUI.pageHit", "RODAWUI.error", "RODAWUI.login" };

	public static final String[] DISSEMINATOR_ACTIONS = new String[] {
			"disseminator.hit.AIPDownload", "disseminator.miss.AIPDownload",
			"disseminator.hit.SimpleViewer", "disseminator.miss.SimpleViewer",
			"disseminator.hit.FlashPageFlip",
			"disseminator.miss.FlashPageFlip", "disseminator.hit.PhpMyAdmin",
			"disseminator.miss.PhpMyAdmin", "disseminator.hit.MediaPlayer",
			"disseminator.miss.MediaPlayer" };

	public static final String[] ACTIONS = join(ACCEPT_SIP_ACTIONS,
			BROWSER_ACTIONS, EDITOR_ACTIONS, FILE_ACCESS_SERVLET_ACTIONS,
			FILE_UPLOAD_SERVLET_ACTIONS, INGEST_ACTIONS,
			INGEST_MONITOR_ACTIONS, LOGGER_ACTIONS, LOGIN_ACTIONS,
			PLUGINS_ACTIONS, REPORTS_ACTIONS, SCHEDULER_ACTIONS,
			SEARCH_ACTIONS, SIP_UPLOAD_SERVLET_ACTIONS, STATISTICS_ACTIONS,
			STATISTICS_MONITOR_ACTIONS, USER_BROWSER_ACTIONS,
			USER_EDITOR_ACTIONS, USER_MANAGEMENT_ACTIONS,
			USER_REGISTRATION_ACTIONS, RODAWUI_ACTIONS, DISSEMINATOR_ACTIONS);

	private static String[] join(String[]... lists) {
		List<String> elements = new ArrayList<String>();
		for (String[] list : lists) {
			Collections.addAll(elements, list);
		}
		return elements.toArray(new String[elements.size()]);
	}

	private String id;
	private String address;
	private String datetime;
	private String username;
	private String action;
	private String description;
	private String relatedObjectPID;
	private long duration;

	private LogEntryParameter[] parameters;

	/**
	 * Constructs an empty {@link LogEntry}.
	 */
	public LogEntry() {
	}

	/**
	 * Constructs a new {@link LogEntry} cloning an existing {@link LogEntry}.
	 * 
	 * @param logEntry
	 *            the {@link LogEntry} to clone.
	 */
	public LogEntry(LogEntry logEntry) {
		this(logEntry.getId(), logEntry.getAddress(), logEntry.getDatetime(),
				logEntry.getUsername(), logEntry.getAction(), logEntry
						.getParameters(), logEntry.getDescription(), logEntry
						.getRelatedObjectPID(), logEntry.getDuration());
	}

	/**
	 * Constructs a new {@link LogEntry} from the specified parameters.
	 * 
	 * @param id
	 *            the unique identifier.
	 * @param address
	 *            the IP address.
	 * @param datetime
	 *            the datetime.
	 * @param username
	 *            the username.
	 * @param action
	 *            the action.
	 * @param parameters
	 *            the action parameters.
	 * @param description
	 *            the description of the action.
	 * @param relatedObjectPID
	 *            the PID of the object related with this action.
	 */
	public LogEntry(String id, String address, String datetime,
			String username, String action, LogEntryParameter[] parameters,
			String description, String relatedObjectPID, long duration) {

		setId(id);
		setAddress(address);
		setDatetime(datetime);
		setUsername(username);
		setAction(action);
		setParameters(parameters);
		setDescription(description);
		setRelatedObjectPID(relatedObjectPID);
		setDuration(duration);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "LogEntry (datetime=" + getDatetime() + ", address="
				+ getAddress() + ", username=" + getUsername() + ", action="
				+ getAction() + ", parameters="
				+ Arrays.toString(getParameters()) + ", description="
				+ getDescription() + ", relatedObjectPID="
				+ getRelatedObjectPID() + ", duration=" + getDuration() + ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {

		boolean equal = false;

		if (obj != null && obj instanceof LogEntry) {
			LogEntry other = (LogEntry) obj;
			equal = getId().equals(other.getId());
		} else {
			equal = false;
		}

		return equal;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the datetime
	 */
	public String getDatetime() {
		return datetime;
	}

	/**
	 * @param datetime
	 *            the datetime to set
	 */
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the parameters
	 */
	public LogEntryParameter[] getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(LogEntryParameter[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the relatedObjectPID
	 */
	public String getRelatedObjectPID() {
		return relatedObjectPID;
	}

	/**
	 * @param relatedObjectPID
	 *            the PID of the related object to set
	 */
	public void setRelatedObjectPID(String relatedObjectPID) {
		this.relatedObjectPID = relatedObjectPID;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

}
