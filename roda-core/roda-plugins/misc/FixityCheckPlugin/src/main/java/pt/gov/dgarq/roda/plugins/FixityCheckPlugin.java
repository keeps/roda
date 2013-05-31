package pt.gov.dgarq.roda.plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.Downloader;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.UserManagementException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.RepresentationPreservationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.preservation.Fixity;
import pt.gov.dgarq.roda.core.data.preservation.RepresentationFilePreservationObject;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Ingest;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.util.EmailUtility;
import pt.gov.dgarq.roda.util.FileUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Rui Castro
 */
public class FixityCheckPlugin extends AbstractPlugin {
	private static Logger logger = Logger.getLogger(FixityCheckPlugin.class);

	/**
	 * @return the plugin parameter
	 */
	public static PluginParameter PARAMETER_NOTIFIER_EMAIL_ADDRESS() {
		return new PluginParameter("notifierEmailAddress",
				PluginParameter.TYPE_STRING,
				"roda-fixity-notifier@dgarq.gov.pt", true, false,
				"Notifier's email address");
	}

	/**
	 * @return the plugin parameter
	 */
	public static PluginParameter PARAMETER_RESPONSIBLE_USERNAME() {
		return new PluginParameter("reponsibleUsername",
				PluginParameter.TYPE_STRING, null, true, false,
				"Username of the user to be notified of failures");
	}

	private RODAClient rodaClient = null;
	private Downloader rodaDownloader = null;

	private Browser browserService = null;
	private Ingest ingestService = null;
	private UserBrowser userBrowserService = null;

	private EmailUtility emailUtility = null;

	/**
	 * Constructs a new {@link FixityCheckPlugin}.
	 */
	public FixityCheckPlugin() {
		super();
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {

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
		return "Preservation/Fixity check"; //$NON-NLS-1$
	}

	/**
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return 1.0f;
	}

	/**
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return "Plugin that checks the integrity of the representation file checksums."; //$NON-NLS-1$
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(PARAMETER_RODA_CORE_URL(),
				PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD(),
				PARAMETER_NOTIFIER_EMAIL_ADDRESS(),
				PARAMETER_RESPONSIBLE_USERNAME());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		initRODAServices();

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("Report of plugin " + getName()); //$NON-NLS-1$
		report.setAttributes(new Attribute[] {
				new Attribute("Agent name", getName()), //$NON-NLS-1$
				new Attribute("Agent version", Float.toString(getVersion())), //$NON-NLS-1$
				new Attribute("Start datetime", DateParser //$NON-NLS-1$
						.getIsoDate(new Date())) });

		SimpleRepresentationObject[] rObjects = null;
		try {

			rObjects = this.browserService.getSimpleRepresentationObjects(null);

		} catch (Exception e) {
			logger.debug("Error getting Representation Objects - " //$NON-NLS-1$
					+ e.getMessage(), e);
			throw new PluginException("Error getting Representation Objects - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}

		int countOk = 0;
		int countFail = 0;
		int countError = 0;

		if (rObjects != null) {
			for (SimpleRepresentationObject sro : rObjects) {

				try {

					RepresentationPreservationObject rPObject = this.browserService
							.getROPreservationObject(sro.getPid());

					// Perform the checksum check
					EventPreservationObject fixityEventPO = checkFixity(
							rPObject, sro);

					AgentPreservationObject agent = new AgentPreservationObject();
					agent.setAgentName(getName() + "/" + getVersion()); //$NON-NLS-1$
					agent
							.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN);

					this.ingestService.registerEvent(rPObject.getPid(),
							fixityEventPO, agent);

					report
							.addItem(new ReportItem(
									"Fixity verification for representation "
											+ rPObject.getID() + " - "
											+ fixityEventPO.getOutcome(),
									new Attribute[] {
											new Attribute(
													"datetime",
													DateParser
															.getIsoDate(new Date())),
											new Attribute("outcome",
													fixityEventPO.getOutcome()),
											new Attribute(
													"outcomeDetails",
													fixityEventPO
															.getOutcomeDetailExtension()) }));

					if ("success".equalsIgnoreCase(fixityEventPO.getOutcome())) {
						countOk++;
					} else if ("failure".equalsIgnoreCase(fixityEventPO
							.getOutcome())) {
						countFail++;
					} else if ("undetermined".equalsIgnoreCase(fixityEventPO
							.getOutcome())) {
						countError++;
					}

				} catch (BrowserException e) {

					logger.debug("Error processing RO " + sro.getPid() + " - "
							+ e.getMessage(), e);

					report.addItem(new ReportItem(
							"Fixity verification for representation "
									+ sro.getPid() + " - " + "Undetermined",
							new Attribute[] {
									new Attribute("datetime", DateParser
											.getIsoDate(new Date())),
									new Attribute("outcome", "Undetermined"),
									new Attribute("reason", e.getMessage()) }));
					countError++;
					notifyUserOfFixityCheckUndetermined(sro, e.getMessage());

				} catch (NoSuchRODAObjectException e) {

					logger.debug("Error processing RO " + sro.getPid() + " - "
							+ e.getMessage(), e);

					report.addItem(new ReportItem(
							"Fixity verification for representation "
									+ sro.getPid() + " - " + "Undetermined",
							new Attribute[] {
									new Attribute("datetime", DateParser
											.getIsoDate(new Date())),
									new Attribute("outcome", "Undetermined"),
									new Attribute("reason", e.getMessage()) }));
					countError++;
					notifyUserOfFixityCheckUndetermined(sro, e.getMessage());

				} catch (IngestException e) {

					logger.debug("Error processing RO " + sro.getPid() + " - "
							+ e.getMessage(), e);

					report.addItem(new ReportItem(
							"Fixity verification for representation "
									+ sro.getPid() + " - " + "Undetermined",
							new Attribute[] {
									new Attribute("datetime", DateParser
											.getIsoDate(new Date())),
									new Attribute("outcome", "Undetermined"),
									new Attribute("reason", e.getMessage()) }));
					countError++;
					notifyUserOfFixityCheckUndetermined(sro, e.getMessage());

				} catch (RemoteException e) {

					logger.debug("Error processing RO " + sro.getPid() + " - "
							+ e.getMessage(), e);

					report.addItem(new ReportItem(
							"Fixity verification for representation "
									+ sro.getPid() + " - " + "Undetermined",
							new Attribute[] {
									new Attribute("datetime", DateParser
											.getIsoDate(new Date())),
									new Attribute("outcome", "Undetermined"),
									new Attribute("reason", e.getMessage()) }));
					countError++;
					notifyUserOfFixityCheckUndetermined(sro, e.getMessage());

				} catch (Exception e) {

					logger.debug("Error processing RO " + sro.getPid() + " - "
							+ e.getMessage(), e);

					report.addItem(new ReportItem(
							"Fixity verification for representation "
									+ sro.getPid() + " - " + "Undetermined",
							new Attribute[] {
									new Attribute("datetime", DateParser
											.getIsoDate(new Date())),
									new Attribute("outcome", "Undetermined"),
									new Attribute("reason", e.getMessage()) }));
					countError++;
					notifyUserOfFixityCheckUndetermined(sro, e.getMessage());

				} catch (Throwable e) {

					logger.debug("Error processing RO " + sro.getPid() + " - "
							+ e.getMessage(), e);

					report.addItem(new ReportItem(
							"Fixity verification for representation "
									+ sro.getPid() + " - " + "Undetermined",
							new Attribute[] {
									new Attribute("datetime", DateParser
											.getIsoDate(new Date())),
									new Attribute("outcome", "Undetermined"),
									new Attribute("reason", e.getMessage()) }));
					countError++;
					notifyUserOfFixityCheckUndetermined(sro, e.getMessage());

				}

			}

		}

		report.addAttribute(new Attribute("Finish datetime", DateParser
				.getIsoDate(new Date())));
		report.addAttribute(new Attribute("Representations valid", Integer
				.toString(countOk)));
		report.addAttribute(new Attribute("Representations invalid", Integer
				.toString(countFail)));
		report.addAttribute(new Attribute("Undetermined", Integer
				.toString(countError)));

		// Report for the execution of this Plugin
		return report;
	}

	private EventPreservationObject checkFixity(
			RepresentationPreservationObject rPObject,
			SimpleRepresentationObject simpleRO) {

		EventPreservationObject eventPO = new EventPreservationObject();
		eventPO.setDatetime(new Date());
		eventPO
				.setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_FIXITY_CHECK);
		eventPO
				.setEventDetail("Checksums recorded in PREMIS were compared with the files in the repository");
		eventPO
				.setAgentRole(EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK);

		try {

			List<String> fileIDs = new ArrayList<String>();

			RepresentationFilePreservationObject rootFile = rPObject
					.getRootFile();
			verifyFixity(rPObject, rootFile);

			fileIDs.add(rootFile.getID());

			if (rPObject.getPartFiles() != null) {
				for (RepresentationFilePreservationObject partFile : rPObject
						.getPartFiles()) {
					verifyFixity(rPObject, partFile);

					fileIDs.add(partFile.getID());
				}
			}

			eventPO.setOutcome("success");
			eventPO.setOutcomeDetailNote("files checked");
			eventPO.setOutcomeDetailExtension(fileIDs.toString());

		} catch (DownloaderException e) {

			logger.debug("Error processing RO " + simpleRO.getPid() + " - "
					+ e.getMessage(), e);

			eventPO.setOutcome("undetermined");
			eventPO.setOutcomeDetailNote("Reason");
			eventPO.setOutcomeDetailExtension("<p>" + e.getMessage() + "</p>");

			notifyUserOfFixityCheckUndetermined(simpleRO, e.getMessage());

		} catch (NoSuchAlgorithmException e) {

			logger.debug("Error processing RO " + simpleRO.getPid() + " - "
					+ e.getMessage(), e);

			eventPO.setOutcome("undetermined");
			eventPO.setOutcomeDetailNote("Reason");
			eventPO.setOutcomeDetailExtension("<p>" + e.getMessage() + "</p>");

			notifyUserOfFixityCheckUndetermined(simpleRO, e.getMessage());

		} catch (IOException e) {

			logger.debug("Error processing RO " + simpleRO.getPid() + " - "
					+ e.getMessage(), e);

			eventPO.setOutcome("undetermined");
			eventPO.setOutcomeDetailNote("Reason");
			eventPO.setOutcomeDetailExtension("<p>" + e.getMessage() + "</p>");

			notifyUserOfFixityCheckUndetermined(simpleRO, e.getMessage());

		} catch (Exception e) {

			logger.debug("Error processing RO " + simpleRO.getPid() + " - "
					+ e.getMessage(), e);

			eventPO.setOutcome("failure");
			eventPO.setOutcomeDetailNote("failure reason");
			eventPO.setOutcomeDetailExtension("<p>" + e.getMessage() + "</p>");

			notifyUserOfFixityCheckFail(simpleRO, e.getMessage());
		}

		return eventPO;
	}

	private void verifyFixity(
			RepresentationPreservationObject representationPO,
			RepresentationFilePreservationObject rFilePO) throws IOException,
			DownloaderException, NoSuchAlgorithmException, Exception {

		Fixity[] fixities = rFilePO.getFixities();

		File tempDir = TempDir.createUniqueDirectory("rep");
		File file = this.rodaDownloader.saveTo(representationPO.getID(),
				rFilePO.getID(), tempDir);

		if (fixities != null) {

			for (Fixity fixity : fixities) {

				String hash = FileUtility.calculateChecksumInHex(file, fixity
						.getMessageDigestAlgorithm());

				if (hash.equalsIgnoreCase(fixity.getMessageDigest())) {
					// OK
					logger.debug(fixity.getMessageDigestAlgorithm()
							+ " checksum of file " + representationPO.getID() //$NON-NLS-1$
							+ "/" + rFilePO.getID() + ": OK"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {

					logger.warn(fixity.getMessageDigestAlgorithm()
							+ " checksum of file " + representationPO.getID() //$NON-NLS-1$
							+ "/" + rFilePO.getID() + ": FAILED"); //$NON-NLS-1$ //$NON-NLS-2$

					throw new Exception("File " + rFilePO.getID() //$NON-NLS-1$
							+ " checksum doesn't match PREMIS checksum value."); //$NON-NLS-1$
				}

			}
		}

		FileUtils.deleteDirectory(tempDir);

	}

	private void notifyUserOfFixityCheckFail(
			SimpleRepresentationObject simpleRO, String message) {

		try {

			DescriptionObject dObject = this.browserService
					.getDescriptionObject(simpleRO.getDescriptionObjectPID());

			String fromAddress = getParameterValues().get(
					PARAMETER_NOTIFIER_EMAIL_ADDRESS().getName());
			String messageSubject = String.format(
					"RODA - Erro de verificação de integridade - %s", simpleRO
							.getPid());

			String messageBody = getFailureMessageBody(simpleRO, dObject);

			String username = getParameterValues().get(
					PARAMETER_RESPONSIBLE_USERNAME().getName());
			User user = this.userBrowserService.getUser(username);

			if (user == null) {

				logger
						.error(String
								.format(
										"User %s doesn't exist. No notification message will be sent.",
										username));

			} else if (StringUtils.isBlank(user.getEmail())) {

				logger
						.error(String
								.format(
										"User %s doesn't have an email address. No notification message will be sent.",
										username));
			} else {

				logger.debug("Sending email to " + user.getEmail() //$NON-NLS-1$
						+ " with subject '" + messageSubject + "'"); //$NON-NLS-1$ //$NON-NLS-2$

				logger.debug("Email message:\n" + messageBody); //$NON-NLS-1$

				this.emailUtility.sendMail(fromAddress, new String[] { user
						.getEmail() }, messageSubject, messageBody);
			}

			logger.info("Sent notification email to " + user.getEmail()
					+ " with body\n" + messageBody);

		} catch (MessagingException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (UserManagementException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (BrowserException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (NoSuchRODAObjectException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (RemoteException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		}

	}

	private void notifyUserOfFixityCheckUndetermined(
			SimpleRepresentationObject simpleRO, String message) {

		try {

			DescriptionObject dObject = this.browserService
					.getDescriptionObject(simpleRO.getDescriptionObjectPID());

			String fromAddress = getParameterValues().get(
					PARAMETER_NOTIFIER_EMAIL_ADDRESS().getName());
			String messageSubject = String.format(
					"RODA - Erro de verificação de integridade - %s", simpleRO
							.getPid());

			String messageBody = getErrorMessageBody(simpleRO, dObject, message);

			String username = getParameterValues().get(
					PARAMETER_RESPONSIBLE_USERNAME().getName());
			User user = this.userBrowserService.getUser(username);

			if (user == null) {

				logger
						.error(String
								.format(
										"User %s doesn't exist. No notification message will be sent.",
										username));

			} else if (StringUtils.isBlank(user.getEmail())) {
				logger.debug("Sending email to " + user.getEmail() //$NON-NLS-1$
						+ " with subject '" + messageSubject + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				logger.debug("Email message:\n" + messageBody); //$NON-NLS-1$

				logger
						.error(String
								.format(
										"User %s doesn't have an email address. No notification message will be sent.",
										username));
			} else {
				this.emailUtility.sendMail(fromAddress, new String[] { user
						.getEmail() }, messageSubject, messageBody);
			}

			logger.info("Sent notification email to " + user.getEmail()
					+ " with body\n" + messageBody);

		} catch (MessagingException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (UserManagementException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (BrowserException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (NoSuchRODAObjectException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		} catch (RemoteException e) {
			logger.warn("Error sending notification email - " + e.getMessage(),
					e);
		}

	}

	private String getErrorMessageBody(SimpleRepresentationObject simpleRO,
			DescriptionObject dObject, String message) {
		StringBuffer messageBody = new StringBuffer();

		// Header
		String headerLine1 = StringUtils.center("ERRO INTEGRIDADE", 78);
		String headerLine2 = StringUtils.center(String.format(
				"Produzido por %s em %tF", getName(), new Date()), 78);

		messageBody.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		messageBody.append(String.format("*%s*%n", headerLine1)); //$NON-NLS-1$
		messageBody.append(String.format("*%s*%n", headerLine2)); //$NON-NLS-1$
		messageBody.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$//$NON-NLS-2$

		messageBody.append("\n\n"); //$NON-NLS-1$

		if (dObject != null) {
			messageBody
					.append(String
							.format(
									"A verificação de integridade da representação %s "
											+ "pertencente ao documento %s (%s) não foi executada por falha técnica. ",
									simpleRO.getPid(), dObject
											.getCompleteReference(), dObject
											.getHandleURL()));
		} else {

			messageBody.append(String.format(
					"A verificação de integridade da representação %s "
							+ "não foi executada por falha técnica. ", simpleRO
							.getPid()));
		}

		messageBody.append(String.format(
				"O erro do sistema que causou a falha foi '%s'. ", message));

		messageBody
				.append("Diligencias deverão então ser tomadas no sentido de detectar e resolver o problema para que a representação possa ser verificada.");
		return messageBody.toString();
	}

	private String getFailureMessageBody(SimpleRepresentationObject simpleRO,
			DescriptionObject dObject) {
		StringBuffer messageBody = new StringBuffer();

		// Header
		String headerLine1 = StringUtils.center("ERRO INTEGRIDADE", 78);
		String headerLine2 = StringUtils.center(String.format(
				"Produzido por %s em %tF", getName(), new Date()), 78);

		messageBody.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		messageBody.append(String.format("*%s*%n", headerLine1)); //$NON-NLS-1$
		messageBody.append(String.format("*%s*%n", headerLine2)); //$NON-NLS-1$
		messageBody.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$//$NON-NLS-2$

		messageBody.append("\n\n"); //$NON-NLS-1$

		messageBody
				.append(String
						.format(
								"A verificação de integridade da representação %s "
										+ "pertencente ao documento %s (%s) falhou a verificação de integridade. ",
								simpleRO.getPid(), dObject
										.getCompleteReference(), dObject
										.getHandleURL()));
		messageBody
				.append("Diligencias deverão então ser tomadas no sentido de repor a representação a partir do último backup realizado.");

		return messageBody.toString();
	}

	private URL getRodaServicesURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				PARAMETER_RODA_CORE_URL().getName()));
	}

	private String getUsername() {
		return getParameterValues().get(
				PARAMETER_RODA_CORE_USERNAME().getName());
	}

	private String getPassword() {
		return getParameterValues().get(
				PARAMETER_RODA_CORE_PASSWORD().getName());
	}

	private void initRODAServices() throws PluginException {

		try {

			this.rodaClient = new RODAClient(getRodaServicesURL(),
					getUsername(), getPassword());
			this.rodaDownloader = new Downloader(getRodaServicesURL(),
					getUsername(), getPassword());

			this.browserService = this.rodaClient.getBrowserService();
			this.ingestService = this.rodaClient.getIngestService();
			this.userBrowserService = this.rodaClient.getUserBrowserService();

		} catch (Exception e) {
			logger.debug("Error creating RODA client - " + e.getMessage(), e); //$NON-NLS-1$
			throw new PluginException("Error creating RODA client - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

}
