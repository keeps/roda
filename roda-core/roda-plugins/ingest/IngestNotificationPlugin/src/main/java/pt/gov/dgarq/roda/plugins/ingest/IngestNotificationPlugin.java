package pt.gov.dgarq.roda.plugins.ingest;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.IngestMonitorException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.SIPStateTransition;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.IngestMonitor;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.util.EmailUtility;

/**
 * Notifies producers about the outcome of their SIPs.
 * 
 * @author Rui Castro
 */
public class IngestNotificationPlugin extends AbstractPlugin {
	private static final Logger logger = Logger
			.getLogger(IngestNotificationPlugin.class);

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(IngestNotificationPlugin.class.getName() + "_messages");

	private final String name = "Ingest tools/Notify producers"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Notifies producers about the outcome of their SIPs."; //$NON-NLS-1$

	public static PluginParameter PARAMETER_NOTIFIER_EMAIL_ADDRESS() {
		return new PluginParameter(
				"notifierEmailAddress", PluginParameter.TYPE_STRING, //$NON-NLS-1$ 
				"ingest-notifier@roda.dgarq.gov.pt", true, false, //$NON-NLS-1$
				"Notifier's email address"); //$NON-NLS-1$
	}

	private static String RODA_HOME = null;
	private static File NOTIFICATIONS_STATE_FILE = null;
	//private static final String RODA_HOME = System.getenv().get("RODA_HOME"); //$NON-NLS-1$
	// private static final File NOTIFICATIONS_STATE_FILE = new File(RODA_HOME +
	// "/core/data/ingest/notifications.state");

	private static final String LAST_NOTIFICATION_DATE_KEY = "lastNotificationDate"; //$NON-NLS-1$

	private static final String SIP_STATE_ACCEPTED = "ACCEPTED"; //$NON-NLS-1$
	private static final String SIP_STATE_QUARANTINE = "QUARANTINE"; //$NON-NLS-1$

	private static final String[] COMPLETE_SIP_STATES = new String[] {
			SIP_STATE_ACCEPTED, SIP_STATE_QUARANTINE };

	private RODAClient rodaClient = null;
	private IngestMonitor ingestMonitorService = null;
	private UserBrowser userBrowserService = null;

	private EmailUtility emailUtility = null;
	private PropertiesConfiguration notifications = null;

	private String lastNotificationDate = null;
	private String currentNotificationDate = null;

	/**
	 *  
	 */
	public IngestNotificationPlugin() {
		super();
	}

	/**
	 * 
	 * @see pt.gov.dgarq.roda.core.plugins.Plugin#init()
	 */
	public void init() throws PluginException {

		if (System.getProperty("roda.home") != null) {
			RODA_HOME = System.getProperty("roda.home");//$NON-NLS-1$
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = System.getenv("RODA_HOME"); //$NON-NLS-1$
		} else {
			RODA_HOME = null;
		}

		if (StringUtils.isBlank(RODA_HOME)) {
			throw new PluginException(
					"RODA_HOME enviroment variable is not set. Can't find notifications state file."); //$NON-NLS-1$
		}

		NOTIFICATIONS_STATE_FILE = new File(RODA_HOME,
				"data/ingest/notifications.state");

		this.notifications = new PropertiesConfiguration();

		if (!NOTIFICATIONS_STATE_FILE.exists()) {

			logger.info("Notifications file doesn't exist. A new one will be created in " //$NON-NLS-1$
					+ NOTIFICATIONS_STATE_FILE);

			try {

				this.notifications.save(NOTIFICATIONS_STATE_FILE);

			} catch (ConfigurationException e) {
				logger.debug("Exception writting notifications state file - " //$NON-NLS-1$
						+ NOTIFICATIONS_STATE_FILE + " - " + e.getMessage(), e); //$NON-NLS-1$
				throw new PluginException(
						"Exception writting notifications state file - " //$NON-NLS-1$
								+ NOTIFICATIONS_STATE_FILE + " - " //$NON-NLS-1$
								+ e.getMessage(), e);
			}

		} else {

			try {

				this.notifications.load(NOTIFICATIONS_STATE_FILE);

			} catch (ConfigurationException e) {
				logger.debug("Exception reading notifications state file - " //$NON-NLS-1$
						+ NOTIFICATIONS_STATE_FILE + " - " + e.getMessage(), e); //$NON-NLS-1$
				throw new PluginException(
						"Exception reading notifications state file - " //$NON-NLS-1$
								+ NOTIFICATIONS_STATE_FILE + " - " //$NON-NLS-1$
								+ e.getMessage(), e);
			}
		}

		this.emailUtility = new EmailUtility("localhost"); //$NON-NLS-1$

		logger.debug("init() OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		logger.debug("shutdown() OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(PARAMETER_RODA_CORE_URL(),
				PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD(),
				PARAMETER_NOTIFIER_EMAIL_ADDRESS(),AbstractPlugin.PARAMETER_RODA_CAS_URL());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("Ingest Notification Plugin report"); //$NON-NLS-1$
		report.addAttribute(new Attribute("Start datetime", DateParser //$NON-NLS-1$
				.getIsoDate(new Date())));

		initRODAServices();

		this.lastNotificationDate = this.notifications
				.getString(LAST_NOTIFICATION_DATE_KEY);

		this.currentNotificationDate = DateParser.getIsoDate(new Date());

		Set<String> producers = getProducers(lastNotificationDate);

		logger.info("Producers since last notification: " + producers); //$NON-NLS-1$

		for (String producer : producers) {

			String producerLastNotificationDate = this.notifications
					.getString(producer);

			if (StringUtils.isBlank(producerLastNotificationDate)) {
				logger.info("Producer " + producer //$NON-NLS-1$
						+ " not yet notified of any SIP"); //$NON-NLS-1$
			} else {
				logger.info("Producer " + producer //$NON-NLS-1$
						+ " was notified of SIPs until " //$NON-NLS-1$
						+ producerLastNotificationDate);
			}

			List<SIPState> producerSIPStates = getProducerSIPStatesSinceDate(
					producer, producerLastNotificationDate);

			if (producerSIPStates.size() == 0) {

				logger.info("Producer " + producer //$NON-NLS-1$
						+ " has no SIPs to be notified about."); //$NON-NLS-1$

			} else {

				logger.info("Producer " + producer + " has " //$NON-NLS-1$ //$NON-NLS-2$
						+ producerSIPStates.size() + " SIPs to be notified."); //$NON-NLS-1$

				try {

					notifyProducer(producer, producerSIPStates);

					logger.info("Notification email sent to producer " //$NON-NLS-1$
							+ producer);

					this.notifications.clearProperty(producer);
					this.notifications.addProperty(producer,
							this.currentNotificationDate);

					logger.info("Notification date added " + producer + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ this.currentNotificationDate);

				} catch (PluginException e) {

					logger.info("Exception notifying producer " + producer //$NON-NLS-1$
							+ " - " + e.getMessage()); //$NON-NLS-1$

					// If the producer didn't exist in the notification list,
					// add it with an empty notification date.
					if (this.notifications.containsKey(producer)) {

						logger.info("Keeping last notification date for producer " //$NON-NLS-1$
								+ producer + ": " //$NON-NLS-1$
								+ producerLastNotificationDate);

					} else {
						logger.info("Setting empty notification date for producer " //$NON-NLS-1$
								+ producer);
						this.notifications.addProperty(producer, ""); //$NON-NLS-1$
					}
				}

			}

		}

		logger.info("All producers were proccessed."); //$NON-NLS-1$
		logger.info("Setting global last modification date to " //$NON-NLS-1$
				+ this.currentNotificationDate);

		this.notifications.clearProperty(LAST_NOTIFICATION_DATE_KEY);
		this.notifications.addProperty(LAST_NOTIFICATION_DATE_KEY,
				this.currentNotificationDate);

		try {

			this.notifications.save(NOTIFICATIONS_STATE_FILE);
			logger.info("Notifications state file saved to " //$NON-NLS-1$
					+ NOTIFICATIONS_STATE_FILE);

		} catch (ConfigurationException e) {
			logger.error("Error saving notifications state file to " //$NON-NLS-1$
					+ NOTIFICATIONS_STATE_FILE + " - " + e.getMessage(), e); //$NON-NLS-1$
		}

		this.lastNotificationDate = null;
		this.currentNotificationDate = null;

		report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
				.getIsoDate(new Date())));
		return report;
	}

	private void notifyProducer(String producer, List<SIPState> sipStates)
			throws PluginException {

		Collections.sort(sipStates, new Comparator<SIPState>() {
			/**
			 * Compare 2 SIPs by it's submission date.
			 */
			public int compare(SIPState sip1, SIPState sip2) {
				return sip1.getStateTransitions()[0].getDatetime().compareTo(
						sip2.getStateTransitions()[0].getDatetime());
			}
		});

		User producerUser;
		try {

			producerUser = this.userBrowserService.getUser(producer);

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			throw new PluginException(e.getMessage(), e);
		}

		if (producerUser == null) {
			throw new PluginException("User information for user " + producer //$NON-NLS-1$
					+ " is null"); //$NON-NLS-1$
		}
		if (StringUtils.isBlank(producerUser.getEmail())) {
			throw new PluginException("Email address for user " + producer //$NON-NLS-1$
					+ " is not available"); //$NON-NLS-1$
		}

		String fromAddress = getParameterValues().get(
				PARAMETER_NOTIFIER_EMAIL_ADDRESS().getName());
		String toAddresses[] = new String[] { producerUser.getEmail() };
		String subject = getString("IngestNotificationPlugin.EMAIL_SUBJECT"); //$NON-NLS-1$

		String messageBody = getProducerNotificationMessage(producer, sipStates);

		try {

			logger.debug("Sending email to " + toAddresses[0] //$NON-NLS-1$
					+ " with subject '" + subject + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			logger.debug("Email message:\n" + messageBody); //$NON-NLS-1$

			this.emailUtility.sendMail(fromAddress, toAddresses, subject,
					messageBody);

		} catch (MessagingException e) {
			logger.debug(e.getMessage(), e);
			throw new PluginException(e.getMessage(), e);
		}
	}

	private String getProducerNotificationMessage(String producer,
			List<SIPState> sipStates) {

		List<SIPState> acceptedSIPStates = getSIPs(sipStates,
				SIP_STATE_ACCEPTED);
		List<SIPState> rejectedSIPStates = getSIPs(sipStates,
				SIP_STATE_QUARANTINE);

		StringBuffer message = new StringBuffer();

		// Header
		String headerLine1 = StringUtils.center(
				getString("IngestNotificationPlugin.EMAIL_HEADER_1"), //$NON-NLS-1$
				78);
		String headerLine2 = StringUtils
				.center(String
						.format("%s %tF", //$NON-NLS-1$
								getString("IngestNotificationPlugin.EMAIL_HEADER_2"), new Date()), 78); //$NON-NLS-1$

		message.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		message.append(String.format("*%s*%n", headerLine1)); //$NON-NLS-1$
		message.append(String.format("*%s*%n", headerLine2)); //$NON-NLS-1$
		message.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$//$NON-NLS-2$

		message.append("\n\n"); //$NON-NLS-1$

		// Summary
		message.append(getString("IngestNotificationPlugin.EMAIL_SUMMARY") + ":\n"); //$NON-NLS-1$//$NON-NLS-2$
		message.append(StringUtils.repeat("=", 60) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		message.append(StringUtils.rightPad(
				getString("IngestNotificationPlugin.EMAIL_PRODUCER"), 20) + ": " + producer //$NON-NLS-1$//$NON-NLS-2$
				+ "\n"); //$NON-NLS-1$
		message.append(StringUtils.rightPad(
				getString("IngestNotificationPlugin.EMAIL_ACCEPTED_SIPS"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
				+ acceptedSIPStates.size() + "\n"); //$NON-NLS-1$
		message.append(StringUtils.rightPad(
				getString("IngestNotificationPlugin.EMAIL_REJECTED_SIPS"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
				+ rejectedSIPStates.size() + "\n"); //$NON-NLS-1$

		message.append("\n\n"); //$NON-NLS-1$

		// List of accepted SIPs
		message.append(getString("IngestNotificationPlugin.EMAIL_LIST_OF_ACCEPTED_SIPS") + ":\n"); //$NON-NLS-1$//$NON-NLS-2$
		message.append(StringUtils.repeat("=", 60) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		for (SIPState sipState : acceptedSIPStates) {

			SIPStateTransition[] stateTransitions = sipState
					.getStateTransitions();
			SIPStateTransition firstTransition = stateTransitions[0];
			SIPStateTransition lastTransition = stateTransitions[stateTransitions.length - 1];

			message.append(StringUtils.rightPad(
					getString("IngestNotificationPlugin.EMAIL_SIP_ID"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ sipState.getId() + "\n"); //$NON-NLS-1$
			message.append(StringUtils.rightPad(
					getString("IngestNotificationPlugin.EMAIL_SIP_NAME"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ sipState.getOriginalFilename() + "\n"); //$NON-NLS-1$
			message.append(StringUtils
					.rightPad(
							getString("IngestNotificationPlugin.EMAIL_SUBMISSION_DATE"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ DateParser.getIsoDate(firstTransition.getDatetime())
					+ "\n"); //$NON-NLS-1$
			message.append(StringUtils
					.rightPad(
							getString("IngestNotificationPlugin.EMAIL_ACCEPT_DATE"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ DateParser.getIsoDate(lastTransition.getDatetime())
					+ "\n"); //$NON-NLS-1$

			message.append("\n\n"); //$NON-NLS-1$
		}

		// List of rejected SIPs
		message.append(getString("IngestNotificationPlugin.EMAIL_LIST_OF_REJECTED_SIPS") + ":\n"); //$NON-NLS-1$//$NON-NLS-2$
		message.append(StringUtils.repeat("=", 60) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		for (SIPState sipState : rejectedSIPStates) {

			SIPStateTransition[] stateTransitions = sipState
					.getStateTransitions();
			SIPStateTransition firstTransition = stateTransitions[0];
			SIPStateTransition lastTransition = stateTransitions[stateTransitions.length - 1];

			message.append(StringUtils.rightPad(
					getString("IngestNotificationPlugin.EMAIL_SIP_ID"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ sipState.getId() + "\n"); //$NON-NLS-1$
			message.append(StringUtils.rightPad(
					getString("IngestNotificationPlugin.EMAIL_SIP_NAME"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ sipState.getOriginalFilename() + "\n"); //$NON-NLS-1$
			message.append(StringUtils
					.rightPad(
							getString("IngestNotificationPlugin.EMAIL_SUBMISSION_DATE"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ DateParser.getIsoDate(firstTransition.getDatetime())
					+ "\n"); //$NON-NLS-1$
			message.append(StringUtils
					.rightPad(
							getString("IngestNotificationPlugin.EMAIL_REJECT_DATE"), 20) + ": " //$NON-NLS-1$//$NON-NLS-2$
					+ DateParser.getIsoDate(lastTransition.getDatetime())
					+ "\n"); //$NON-NLS-1$
			message.append(StringUtils
					.rightPad(
							getString("IngestNotificationPlugin.EMAIL_REJECT_MOTIVE"), 20) //$NON-NLS-1$
					+ ": " + lastTransition.getDescription() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

			message.append("\n\n"); //$NON-NLS-1$
		}

		// message.append(String.format("Number of processed SIPs: %s\n",
		// sipStates.size()));
		//
		// for (SIPState sipState : sipStates) {
		// message
		// .append("----------------------------------------------------\n");
		// message.append(getSIPStateNotificationMessage(sipState));
		// message.append("\n");
		// }

		return message.toString();
	}

	private String getSIPStateNotificationMessage(SIPState sipState) {
		StringBuffer message = new StringBuffer();

		message.append(String.format("Id: %s\n", sipState.getId()));
		message.append(String.format("State: %s\n", sipState.getState()));
		message.append(String.format("SIP file: %s\n",
				sipState.getOriginalFilename()));
		message.append(String.format("Producer: %s\n", sipState.getUsername()));
		message.append(String.format("Parent PID: %s\n",
				sipState.getParentPID()));

		for (SIPStateTransition transition : sipState.getStateTransitions()) {

			message.append(String.format("== %s - %s\n",
					transition.getDatetime(), transition.getToState()));
			message.append(String.format("Successfull: %b\n",
					transition.isSuccess()));
			message.append(String.format("Description: %s\n",
					transition.getDescription()));
			if (transition.getTaskID() != null) {
				message.append(String.format("Task ID: %s\n",
						transition.getTaskID()));
			}
		}

		return message.toString();
	}

	private List<SIPState> getSIPs(List<SIPState> sipStates, String state) {
		List<SIPState> sips = new ArrayList<SIPState>();

		for (SIPState sipState : sipStates) {
			if (sipState.getState().equals(state)) {
				sips.add(sipState);
			}
		}

		return sips;
	}

	private List<SIPState> getProducerSIPStatesSinceDate(String producer,
			String datetime) throws PluginException {

		List<SIPState> sipStates = new ArrayList<SIPState>();

		Filter filterSIPsSinceLastNotification = new Filter(
				new FilterParameter[] {
						new SimpleFilterParameter("username", producer), //$NON-NLS-1$
						new OneOfManyFilterParameter("state", //$NON-NLS-1$
								COMPLETE_SIP_STATES) });

		if (!StringUtils.isBlank(datetime)) {
			filterSIPsSinceLastNotification.add(new RangeFilterParameter(
					"datetime", datetime, null)); //$NON-NLS-1$
		}

		try {

			SIPState[] sips = ingestMonitorService.getSIPs(new ContentAdapter(
					filterSIPsSinceLastNotification, null, null));

			if (sips != null) {
				sipStates.addAll(Arrays.asList(sips));
			}

			return sipStates;

		} catch (IngestMonitorException e) {
			logger.debug("Exception getting SIPStates - " + e.getMessage(), e); //$NON-NLS-1$
			throw new PluginException("Exception getting SIPStates - " //$NON-NLS-1$
					+ e.getMessage(), e);
		} catch (RemoteException e) {
			logger.debug("Exception getting SIPStates - " + e.getMessage(), e); //$NON-NLS-1$
			throw new PluginException("Exception getting SIPStates - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private Set<String> getProducers(String datetime) throws PluginException {

		Set<String> producers = new HashSet<String>();

		Iterator<String> producerIterator = this.notifications.getKeys();
		while (producerIterator.hasNext()) {
			producers.add(producerIterator.next());
		}
		producers.remove(LAST_NOTIFICATION_DATE_KEY);

		Filter filterSIPsSinceLastNotification = new Filter(
				new OneOfManyFilterParameter("state", COMPLETE_SIP_STATES)); //$NON-NLS-1$

		if (!StringUtils.isBlank(datetime)) {
			filterSIPsSinceLastNotification.add(new RangeFilterParameter(
					"datetime", datetime, null)); //$NON-NLS-1$
		}

		try {

			SIPState[] sips = ingestMonitorService.getSIPs(new ContentAdapter(
					filterSIPsSinceLastNotification, null, null));

			if (sips != null) {

				for (SIPState sipState : sips) {
					producers.add(sipState.getUsername());
				}

			}

			return producers;

		} catch (IngestMonitorException e) {
			logger.debug("Exception getting SIPStates - " + e.getMessage(), e); //$NON-NLS-1$
			throw new PluginException("Exception getting SIPStates - " //$NON-NLS-1$
					+ e.getMessage(), e);
		} catch (RemoteException e) {
			logger.debug("Exception getting SIPStates - " + e.getMessage(), e); //$NON-NLS-1$
			throw new PluginException("Exception getting SIPStates - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

	private void initRODAServices() throws PluginException {

		String rodaClientServiceUrl = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		String rodaClientUsername = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
		String rodaClientPassword = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());
		String casURL = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CAS_URL().getName());
		String coreURL = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL));

			this.rodaClient = new RODAClient(new URL(rodaClientServiceUrl),
					rodaClientUsername, rodaClientPassword,casUtility);
			this.ingestMonitorService = this.rodaClient
					.getIngestMonitorService();
			this.userBrowserService = this.rodaClient.getUserBrowserService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new PluginException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
