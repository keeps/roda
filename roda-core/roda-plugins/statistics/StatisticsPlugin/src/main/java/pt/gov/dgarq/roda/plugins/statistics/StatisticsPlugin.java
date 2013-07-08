package pt.gov.dgarq.roda.plugins.statistics;

import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.IngestMonitorException;
import pt.gov.dgarq.roda.core.common.LoggerException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.common.SchedulerException;
import pt.gov.dgarq.roda.core.common.StatisticsException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.core.data.LogEntry;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.RODAObject;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.SIPStateTransition;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.StatisticData;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.IngestMonitor;
import pt.gov.dgarq.roda.core.stubs.LogMonitor;
import pt.gov.dgarq.roda.core.stubs.Scheduler;
import pt.gov.dgarq.roda.core.stubs.Statistics;
import pt.gov.dgarq.roda.core.stubs.StatisticsMonitor;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;

/**
 * Plugin to update the statistics
 * 
 * @author Luis Faria
 * 
 */
public class StatisticsPlugin extends AbstractPlugin {
	private static Logger logger = Logger.getLogger(StatisticsPlugin.class);

	private static final int MAX_BLOCK_SIZE = 500;

	private RODAClient rodaClient = null;
	private Date currentDate = null;
	private List<StatisticData> entries = null;

	/**
	 * Create a new instance of the plugin
	 */
	public StatisticsPlugin() {
		super();
	}

	/**
	 * @see Plugin#getName()
	 */
	public String getName() {
		return "Statistics/Calculate statistics";
	}

	/**
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return "Plugin that creates current statistics and updates statistics service ";
	}

	/**
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return 1.0f;
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
		// nothing to do
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(PARAMETER_RODA_CORE_URL(),
				PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {
		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("Statistic Plugin Report");
		List<Attribute> attributes = new ArrayList<Attribute>();
		List<ReportItem> items = new ArrayList<ReportItem>();

		try {
			long t0 = new Date().getTime();

			URL rodaCore = new URL(getParameterValues().get(
					PARAMETER_RODA_CORE_URL().getName()));
			String username = getParameterValues().get(
					PARAMETER_RODA_CORE_USERNAME().getName());
			String password = getParameterValues().get(
					PARAMETER_RODA_CORE_PASSWORD().getName());

			rodaClient = new RODAClient(rodaCore, username, password);
			currentDate = new Date();
			entries = new ArrayList<StatisticData>();

			long t1 = new Date().getTime();

			// create repository objects statistics
			browserStatistics();

			long t2 = new Date().getTime();
			System.out.println("Browser statistics took " + (t2 - t1) + "ms");

			items.add(new ReportItem("Browse statistics",
					new Attribute[] { new Attribute("duration", (t2 - t1)
							+ "ms") }));

			// create users and groups statistics
			userBrowserStatistics();

			long t3 = new Date().getTime();
			System.out.println("User statistics took " + (t3 - t2) + "ms");
			items.add(new ReportItem("User statistics",
					new Attribute[] { new Attribute("duration", (t3 - t2)
							+ "ms") }));

			// create user actions statistics
			userLogStatistics();

			long t4 = new Date().getTime();
			System.out.println("User log statistics took " + (t4 - t3) + "ms");
			items.add(new ReportItem("User log statistics",
					new Attribute[] { new Attribute("duration", (t4 - t3)
							+ "ms") }));

			// create events and tasks statistics
			eventManagementStatistics();

			long t5 = new Date().getTime();
			System.out.println("Event statistics took " + (t5 - t4) + "ms");

			items.add(new ReportItem("Event statistics",
					new Attribute[] { new Attribute("duration", (t5 - t4)
							+ "ms") }));

			// create ingestion statistics
			ingestStatistics();

			long t6 = new Date().getTime();
			System.out.println("Ingest statistics took " + (t6 - t5) + "ms");

			items.add(new ReportItem("Ingest statistics",
					new Attribute[] { new Attribute("duration", (t6 - t5)
							+ "ms") }));

			int entrySize = entries.size();

			sendEntries();

			long t7 = new Date().getTime();
			System.out.println("Commit took " + (t7 - t6) + "ms");

			items.add(new ReportItem("Commit", new Attribute[] {
					new Attribute("duration", (t7 - t6) + "ms"),
					new Attribute("entries", entrySize + "") }));

			long tf = new Date().getTime();
			System.out.println("Total statistics took " + (tf - t0) + "ms");
			attributes.add(new Attribute("total duration", (tf - t0) + "ms"));

		} catch (Exception e) {
			report.setAttributes(attributes.toArray(new Attribute[] {}));
			report.setItems(items.toArray(new ReportItem[] {}));
			throw new PluginException(
					"Error while executing statistics plugin", e, report);
		}

		report.setAttributes(attributes.toArray(new Attribute[] {}));
		report.setItems(items.toArray(new ReportItem[] {}));

		return report;
	}

	private void insert(Date timestamp, String type, String value) {
		StatisticData entry = new StatisticData(timestamp, type, value);
		logger.info("ADD " + entry);
		entries.add(entry);
	}

	private StatisticData lastData(String type) throws RODAClientException,
			RemoteException, StatisticsException {
		StatisticData ret;
		StatisticsMonitor statisticsMonitor = rodaClient
				.getStatisticsMonitorService();
		ContentAdapter adapter = new ContentAdapter();
		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter("type", type));

		Sorter sorter = new Sorter(new SortParameter[] { new SortParameter(
				"timestamp", true) });
		Sublist sublist = new Sublist(0, 1);

		adapter.setFilter(filter);
		adapter.setSorter(sorter);
		adapter.setSublist(sublist);

		StatisticData[] statisticData = statisticsMonitor
				.getStatisticData(adapter);
		if (statisticData != null && statisticData.length > 0) {
			ret = statisticData[0];
		} else {
			ret = new StatisticData();
			ret.setType(type);
			ret.setValue("" + 0);
		}

		return ret;
	}

	private FilterParameter sinceDataFilterParameter(StatisticData lastData,
			String dataField) {
		return (lastData != null && lastData.getTimestamp() != null) ? new RangeFilterParameter(
				dataField, DateParser.getIsoDate(lastData.getTimestamp()), null)
				: null;
	}

	private Filter sinceDataFilter(StatisticData lastData, String dataField) {
		FilterParameter parameter = sinceDataFilterParameter(lastData,
				dataField);
		Filter filter = null;
		if (parameter != null) {
			filter = new Filter();
			filter.add(sinceDataFilterParameter(lastData, dataField));
		}

		return filter;
	}

	private void sendEntries() throws RODAException {
		// send entries
		Statistics statistics = rodaClient.getStatisticsService();
		try {
			statistics.insertStatisticDataList(entries
					.toArray(new StatisticData[] {}));
		} catch (RemoteException e) {
			throw RODAClient.parseRemoteException(e);
		} finally {
			// clean entries
			entries = null;
		}
	}

	private void browserStatistics() throws BrowserException, RemoteException,
			RODAClientException, StatisticsException {
		Browser browser = rodaClient.getBrowserService();

		SimpleFilterParameter activeFilterParam = new SimpleFilterParameter(
				"state", RODAObject.STATE_ACTIVE);
		Filter activeFilter = new Filter();
		activeFilter.add(activeFilterParam);

		// Total number of descriptive objects
		int doCount = browser.getSimpleDescriptionObjectCount(activeFilter);
		insert(currentDate, "object.descriptive", "" + doCount);

		// Total number of fonds
		int fondsCount = browser
				.getSimpleDescriptionObjectCount(DescriptionObject.FONDS_FILTER);
		insert(currentDate, "object.descriptive.fonds", "" + fondsCount);

		int eventPresCount = browser
				.getSimpleEventPreservationObjectCount(activeFilter);
		insert(currentDate, "object.preservation.event", "" + eventPresCount);

		int repPresCount = browser
				.getSimpleRepresentationPreservationObjectCount(activeFilter);
		insert(currentDate, "object.preservation.representation", ""
				+ repPresCount);

		// Total number of representation objects
		int repCount = browser.getSimpleRepresentationObjectCount(activeFilter);
		insert(currentDate, "object.representation", "" + repCount);

		// Distribution of representation types (or classes)
		for (String type : RepresentationObject.TYPES) {
			Filter typeFilter = new Filter();
			typeFilter.add(activeFilterParam);
			typeFilter.add(new SimpleFilterParameter("type", type));
			int repTypeCount = browser
					.getSimpleRepresentationObjectCount(typeFilter);
			insert(currentDate, "object.representation.type." + type, ""
					+ repTypeCount);
		}

		// Distribution of representation sub-types (or formats)
		Map<String, Integer> subTypesCount = new HashMap<String, Integer>();

		// Count sub-types of new added representations
		ContentAdapter subTypeAdapter = new ContentAdapter();
		subTypeAdapter.setFilter(activeFilter);

		int sRepsCount = browser
				.getSimpleRepresentationObjectCount(activeFilter);
		int sRepsTreatedCount = 0;
		while (sRepsTreatedCount < sRepsCount) {
			subTypeAdapter.setSublist(new Sublist(sRepsTreatedCount,
					MAX_BLOCK_SIZE));
			SimpleRepresentationObject[] sreps = browser
					.getSimpleRepresentationObjects(subTypeAdapter);
			sreps = (sreps == null) ? new SimpleRepresentationObject[] {}
					: sreps;
			sRepsTreatedCount += sreps.length;

			for (SimpleRepresentationObject srep : sreps) {
				String subtype = srep.getSubType();
				if (subtype != null) {
					int subTypeCount = 1;
					if (subTypesCount.containsKey(subtype)) {
						subTypeCount += subTypesCount.get(subtype);
					}
					subTypesCount.put(subtype, subTypeCount);
				}
			}
		}

		// Insert updated sub-types list
		Set<String> subTypeList = subTypesCount.keySet();
		for (String subtype : subTypeList) {
			insert(currentDate, "object.representation.subtype." + subtype,
					subTypesCount.get(subtype).toString());
		}

	}

	private void userBrowserStatistics() throws RODAClientException,
			UserManagementException, RemoteException {
		UserBrowser userBrowser = rodaClient.getUserBrowserService();

		// User Count
		int userCount = userBrowser.getUserCount(null);
		insert(currentDate, "users", "" + userCount);

		// Active users
		Filter activeUserFilter = new Filter();
		activeUserFilter.add(new SimpleFilterParameter("active", "true"));
		int activeUserCount = userBrowser.getUserCount(activeUserFilter);
		insert(currentDate, "users.state.active", "" + activeUserCount);

		// Inactive users
		Filter inactiveUserFilter = new Filter();
		inactiveUserFilter.add(new SimpleFilterParameter("active", "false"));
		int inactiveUserCount = userBrowser.getUserCount(inactiveUserFilter);
		insert(currentDate, "users.state.inactive", "" + inactiveUserCount);

		// Group Count
		int groupCount = userBrowser.getGroupCount(null);
		insert(currentDate, "groups", "" + groupCount);

		// Group distribution (Top 5)
		List<Group> groups = Arrays.asList(userBrowser.getGroups(null));
		Map<String, Group> groupMap = new HashMap<String, Group>();
		for (Group group : groups) {
			groupMap.put(group.getName(), group);
		}
		TopList topList = new TopList(5);
		for (Group group : groups) {
			if (!group.getName().equals("guests")) {
				int groupUserCount = getGroupUserCount(group, groupMap);
				topList.put("users.group." + group.getName(), groupUserCount);
			}

		}

		for (int i = 0; i < topList.getTopSize(); i++) {
			if (topList.exists(i)) {
				insert(currentDate, topList.getLabel(i), topList.getValue(i)
						.toString());
			} else {
				break;
			}
		}
	}

	private int getGroupUserCount(Group group, Map<String, Group> groupMap) {
		int ret = group.getMemberUserNames().length;
		for (String groupName : group.getMemberGroupNames()) {
			ret += getGroupUserCount(groupMap.get(groupName), groupMap);
		}
		return ret;
	}

	private void userLogStatistics() throws RODAClientException,
			RemoteException, LoggerException, StatisticsException {

		LogMonitor logMonitor = rodaClient.getLogMonitorService();

		// Get log entries
		int logEntriesCount = logMonitor.getLogEntriesCount(null);
		insert(currentDate, "logs", "" + logEntriesCount);

		String[] accessActions = new String[] { "Browser.getDescriptionObject",
				"Search.basicSearch", "Search.advancedSearch",
				"RODAWUI.pageHit", "RODAWUI.error", "RODAWUI.login",
				"Browser.getPreservationEvents",
				"UserRegistration.registerUser",
				"UserRegistration.confirmUserEmail",
				"UserRegistration.resetUserPassword",
				"disseminator.hit.AIPDownload",
				"disseminator.miss.AIPDownload",
				"disseminator.hit.SimpleViewer",
				"disseminator.miss.SimpleViewer",
				"disseminator.hit.FlashPageFlip",
				"disseminator.miss.FlashPageFlip",
				"disseminator.hit.PhpMyAdmin", "disseminator.miss.PhpMyAdmin",
				"disseminator.hit.MediaPlayer", "disseminator.miss.MediaPlayer" };

		// Get log action distribution
		for (String action : accessActions) {
			Filter actionFilter = new Filter();
			if (action.contains("*")) {
				actionFilter.add(new RegexFilterParameter("action", action));
			} else {
				actionFilter.add(new SimpleFilterParameter("action", action));
			}

			int logActionCount = logMonitor.getLogEntriesCount(actionFilter);
			insert(currentDate, "logs.action." + action, "" + logActionCount);
		}

		// TODO add pause/resume/stop task instance
		String[] userSegmentedActions = new String[] {
				"Editor.createDescriptionObject",
				"Editor.modifyDescriptionObject",
				"Editor.removeDescriptionObject",
				"Editor.moveDescriptionObject", "UserManagement.addUser",
				"UserManagement.modifyUser", "UserManagement.addGroup",
				"UserManagement.modifyGroup", "UserManagement.removeUser",
				"UserManagement.removeGroup", "UserManagement.setUserPassword",
				"AcceptSIP.acceptSIP", "Scheduler.addTask",
				"Scheduler.modifyTask", "Scheduler.removeTask" };

		for (String action : userSegmentedActions) {
			// get last data
			StatisticData actionLastData = lastData("logs.action." + action);

			ContentAdapter adapter = new ContentAdapter();
			Filter filter = new Filter();
			FilterParameter actionLastDataParameter = sinceDataFilterParameter(
					actionLastData, "datetime");
			if (actionLastDataParameter != null) {
				filter.add(actionLastDataParameter);
			}
			filter.add(new SimpleFilterParameter("action", action));
			adapter.setFilter(filter);

			int actionEntriesCount = logMonitor.getLogEntriesCount(filter);
			int actionEntriesParsedCount = 0;

			Map<String, Integer> userCache = new HashMap<String, Integer>();
			long actionCount = Long.parseLong(actionLastData.getValue());

			while (actionEntriesParsedCount < actionEntriesCount) {
				adapter.setSublist(new Sublist(actionEntriesParsedCount,
						MAX_BLOCK_SIZE));
				LogEntry[] logEntries = logMonitor.getLogEntries(adapter);

				logEntries = logEntries == null ? new LogEntry[] {}
						: logEntries;

				actionEntriesParsedCount += logEntries.length;

				// parse
				for (LogEntry entry : logEntries) {
					String username = entry.getUsername();
					if (userCache.containsKey(username)) {
						userCache.put(username, userCache.get(username) + 1);
					} else {
						userCache.put(username, 1);
					}
					actionCount++;
				}

			}

			// insert
			insert(currentDate, "logs.action." + action, actionCount + "");
			for (Entry<String, Integer> entry : userCache.entrySet()) {
				insert(currentDate,
						"logs.user." + action + "." + entry.getKey(), entry
								.getValue().toString());
			}
		}

	}

	private void eventManagementStatistics() throws RODAClientException,
			SchedulerException, RemoteException {
		Scheduler scheduler = rodaClient.getSchedulerService();

		// Tasks count
		int tasksCount = scheduler.getTaskCount(null);
		insert(currentDate, "tasks", "" + tasksCount);

		// Task state distribution (running or suspended)
		Filter taskRunningFilter = new Filter();
		taskRunningFilter.add(new SimpleFilterParameter("running", "true"));
		int tasksRunningCount = scheduler.getTaskCount(taskRunningFilter);
		int tasksSuspendedCount = tasksCount - tasksRunningCount;
		insert(currentDate, "tasks.state.running", "" + tasksRunningCount);
		insert(currentDate, "tasks.state.suspended", "" + tasksSuspendedCount);

		// Task instances count
		int instancesCount = scheduler.getTaskInstanceCount(null);
		insert(currentDate, "instances", "" + instancesCount);

		// Task instance state distribution (running, paused or stopped)
		Filter instanceRunningFilter = new Filter();
		taskRunningFilter.add(new SimpleFilterParameter("state",
				"STATE_RUNNING"));
		int instancesRunningCount = scheduler
				.getTaskInstanceCount(instanceRunningFilter);

		Filter instancePausedFilter = new Filter();
		instancePausedFilter.add(new SimpleFilterParameter("state",
				"STATE_PAUSED"));
		int instancesPausedCount = scheduler
				.getTaskInstanceCount(instancePausedFilter);

		int instanceStoppedCount = instancesCount - instancesRunningCount
				- instancesPausedCount;
		insert(currentDate, "instances.state.running", ""
				+ instancesRunningCount);
		insert(currentDate, "instances.state.paused", "" + instancesPausedCount);
		insert(currentDate, "instances.state.stopped", ""
				+ instanceStoppedCount);

	}

	private void ingestStatistics() throws RODAClientException,
			IngestMonitorException, RemoteException, InvalidDateException,
			StatisticsException, ParseException {
		IngestMonitor ingest = rodaClient.getIngestMonitorService();

		// SIPs count
		int sipsCount = ingest.getSIPsCount(null);
		insert(currentDate, "sips", "" + sipsCount);

		// SIPs completeness
		Filter sipCompletenessFilter = new Filter();
		sipCompletenessFilter
				.add(new SimpleFilterParameter("complete", "true"));
		int sipsCompleteCount = ingest.getSIPsCount(sipCompletenessFilter);
		int sipsNotCompleteCount = sipsCount - sipsCompleteCount;
		insert(currentDate, "sips.complete.true", "" + sipsCompleteCount);
		insert(currentDate, "sips.complete.false", "" + sipsNotCompleteCount);

		// SIPs state distribution
		for (String state : ingest.getPossibleStates()) {
			Filter stateFilter = new Filter();
			stateFilter.add(new SimpleFilterParameter("state", state));
			int stateCount = ingest.getSIPsCount(stateFilter);
			insert(currentDate, "sips.state." + state, "" + stateCount);
		}

		// Processing time
		// get last data
		StatisticData lastDurationAvg = lastData("sips.duration.total");
		StatisticData lastDurationAutoAvg = lastData("sips.duration.auto");
		StatisticData lastDurationManualAvg = lastData("sips.duration.manual");
		StatisticData lastDurationManualTotal = lastData("sips.manual.total");

		StatisticData lastDurationAutoMin = lastData("sips.duration.auto.min");
		StatisticData lastDurationAutoMax = lastData("sips.duration.auto.max");
		StatisticData lastDurationManualMin = lastData("sips.duration.manual.min");
		StatisticData lastDurationManualMax = lastData("sips.duration.manual.max");
		StatisticData lastDurationAutoMinId = lastData("sips.duration.auto.min.id");
		StatisticData lastDurationAutoMaxId = lastData("sips.duration.auto.max.id");
		StatisticData lastDurationManualMinId = lastData("sips.duration.manual.min.id");
		StatisticData lastDurationManualMaxId = lastData("sips.duration.manual.max.id");

		StatisticData lastProducers = lastData("producers");

		// fetch data after last data
		ContentAdapter adapter = new ContentAdapter();
		Filter filter = sinceDataFilter(lastDurationAvg, "datetime");
		adapter.setFilter(filter);

		int stateCount = ingest.getSIPsCount(filter);
		int stateParsedCount = 0;

		// initialize
		long durationTotalAvg = Long.parseLong(lastDurationAvg.getValue());
		long durationAutoAvg = Long.parseLong(lastDurationAutoAvg.getValue());
		long durationManualAvg = Long.parseLong(lastDurationManualAvg
				.getValue());

		long durationAutoMin = Long.parseLong(lastDurationAutoMin.getValue());
		long durationAutoMax = Long.parseLong(lastDurationAutoMax.getValue());
		long durationManualMin = Long.parseLong(lastDurationManualMin
				.getValue());
		long durationManualMax = Long.parseLong(lastDurationManualMax
				.getValue());
		String sipAutoMinId = lastDurationAutoMinId.getValue();
		String sipAutoMaxId = lastDurationAutoMaxId.getValue();
		String sipManualMinId = lastDurationManualMinId.getValue();
		String sipManualMaxId = lastDurationManualMaxId.getValue();

		int n = sipsCount - stateCount;
		int manualTotal = Integer.parseInt(lastDurationManualTotal.getValue());

		Set<String> producers = new HashSet<String>(translateList(
				lastProducers.getValue(), ' '));

		while (stateParsedCount < stateCount) {
			adapter.setSublist(new Sublist(stateParsedCount, MAX_BLOCK_SIZE));

			SIPState[] states = ingest.getSIPs(adapter);
			states = (states == null) ? new SIPState[] {} : states;

			stateParsedCount += states.length;

			// process sips
			for (SIPState sip : states) {
				SIPStateTransition[] transitions = sip.getStateTransitions();
				if (transitions.length > 0) {
					Date t0 = transitions[0].getDatetime();
					Date tf = transitions[transitions.length - 1].getDatetime();
					Date tSipIngested = null;
					for (SIPStateTransition transition : transitions) {
						if (transition.getToState().equals("SIP_INGESTED")) {
							tSipIngested = transition.getDatetime();
							break;
						}
					}

					long t0ms = t0.getTime();
					long tfms = tf.getTime();
					long tAutoMs = tSipIngested != null ? tSipIngested
							.getTime() : tfms;

					long durationTotal = tfms - t0ms;
					long durationAuto = tAutoMs - t0ms;
					long durationManual = tfms - tAutoMs;

					// update average
					durationTotalAvg = Math.round(durationTotalAvg
							+ ((durationTotal - durationTotalAvg) / (n + 1.0)));
					durationAutoAvg = Math.round(durationAutoAvg
							+ ((durationAuto - durationAutoAvg) / (n + 1.0)));

					if (durationAutoMin == 0 || durationAutoMin >= durationAuto) {
						durationAutoMin = durationAuto;
						sipAutoMinId = sip.getId();
					}

					if (durationAutoMax <= durationAuto) {
						durationAutoMax = durationAuto;
						sipAutoMaxId = sip.getId();
					}

					if (tSipIngested != null) {
						durationManualAvg = Math
								.round(durationManualAvg
										+ ((durationManual - durationManualAvg) / (manualTotal + 1.0)));

						if (durationManualMin == 0
								|| durationManualMin >= durationManual) {
							durationManualMin = durationManual;
							sipManualMinId = sip.getId();
						}

						if (durationManualMax <= durationManual) {
							durationManualMax = durationManual;
							sipManualMaxId = sip.getId();
						}

						manualTotal++;
					}

					producers.add(sip.getUsername());
					n++;
				} else {
					// TODO warning?
				}
			}

		}

		insert(currentDate, "sips.duration.total", "" + durationTotalAvg);
		insert(currentDate, "sips.duration.auto", "" + durationAutoAvg);
		insert(currentDate, "sips.duration.manual", "" + durationManualAvg);

		insert(currentDate, "sips.duration.manual.min", "" + durationManualMin);
		insert(currentDate, "sips.duration.manual.max", "" + durationManualMax);
		insert(currentDate, "sips.duration.auto.min", "" + durationAutoMin);
		insert(currentDate, "sips.duration.auto.max", "" + durationAutoMax);

		insert(currentDate, "sips.duration.manual.min.id", sipManualMinId);
		insert(currentDate, "sips.duration.manual.max.id", sipManualMaxId);
		insert(currentDate, "sips.duration.auto.min.id", sipAutoMinId);
		insert(currentDate, "sips.duration.auto.max.id", sipAutoMaxId);

		insert(currentDate, "sips.manual.total", "" + manualTotal);

		insert(currentDate, "producers", untranslateList(producers, ' '));

		// producers
		for (String producer : producers) {
			FilterParameter producerParameter = new SimpleFilterParameter(
					"username", producer);

			// last submission
			ContentAdapter lastSubmissionAdapter = new ContentAdapter();
			Filter lastSubmissionFilter = new Filter();
			lastSubmissionFilter.add(producerParameter);
			Sorter lastSubmissionSorter = new Sorter();
			lastSubmissionSorter.add(new SortParameter("datetime", true));
			Sublist lastSubmissionSubList = new Sublist(0, 1);
			lastSubmissionAdapter.setFilter(lastSubmissionFilter);
			lastSubmissionAdapter.setSorter(lastSubmissionSorter);
			lastSubmissionAdapter.setSublist(lastSubmissionSubList);
			SIPState[] prodStates = ingest.getSIPs(lastSubmissionAdapter);

			if (prodStates != null && prodStates.length > 0) {
				insert(currentDate,
						"producer." + producer + ".submission.last",
						DateParser.getIsoDate(prodStates[0].getDatetime()));
			}

			// Total of submissions
			Filter totalSubmissionsFilter = new Filter();
			totalSubmissionsFilter.add(producerParameter);
			int totalSubmissions = ingest.getSIPsCount(totalSubmissionsFilter);

			// Accepted submissions
			Filter acceptedSubmissionsFilter = new Filter();
			acceptedSubmissionsFilter.add(producerParameter);
			acceptedSubmissionsFilter.add(new SimpleFilterParameter("state",
					"ACCEPTED"));
			int acceptedSubmissions = ingest
					.getSIPsCount(acceptedSubmissionsFilter);

			// Rejected submissions
			Filter rejectedSubmissionsFilter = new Filter();
			rejectedSubmissionsFilter.add(producerParameter);
			rejectedSubmissionsFilter.add(new SimpleFilterParameter("state",
					"QUARANTINE"));
			int rejectedSubmissions = ingest
					.getSIPsCount(rejectedSubmissionsFilter);

			int processingSubmissions = totalSubmissions - acceptedSubmissions
					- rejectedSubmissions;

			insert(currentDate, "producer." + producer
					+ ".submission.state.accepted", "" + acceptedSubmissions);
			insert(currentDate, "producer." + producer
					+ ".submission.state.rejected", "" + rejectedSubmissions);
			insert(currentDate, "producer." + producer
					+ ".submission.state.processing", ""
					+ processingSubmissions);

		}

	}

	protected List<String> translateList(String list, char sep) {
		return list.equals("0") ? new ArrayList<String>() : Arrays.asList(list
				.split(sep + ""));
	}

	protected String untranslateList(Collection<String> list, char sep) {
		String ret = "";

		boolean first = true;

		for (String item : list) {
			if (first) {
				first = false;
				ret += item;
			} else {
				ret += sep + item;
			}
		}

		return ret;
	}

	public void shutdown() {
		// nothing to do
	}

	/**
	 * Class to get the TOP N of some statistics
	 * 
	 * @author Luis Faria
	 * 
	 */
	private class TopList {
		private final int topSize;
		private String[] labels;
		private Integer[] values;

		public TopList(int topSize) {
			this.topSize = topSize;
			labels = new String[topSize];
			values = new Integer[topSize];

			for (int i = 0; i < topSize; i++) {
				labels[i] = null;
				values[i] = Integer.MIN_VALUE;
			}
		}

		public int getTopSize() {
			return topSize;
		}

		public String getLabel(int i) {
			return labels[i];
		}

		public Integer getValue(int i) {
			return values[i];
		}

		public boolean exists(int i) {
			return labels[i] != null;
		}

		public boolean put(String label, Integer value) {
			boolean onTop = false;

			// Try to insert into an empty slot
			for (int i = 0; i < topSize; i++) {
				if (!exists(i)) {
					labels[i] = label;
					values[i] = value;
					onTop = true;
					break;
				}
			}

			if (!onTop) {
				// Try to replace slot with lower value
				for (int i = 0; i < topSize; i++) {
					if (values[i] < value) {
						labels[i] = label;
						values[i] = value;
						onTop = true;
						break;
					}
				}
			}

			return onTop;
		}

	}

}
