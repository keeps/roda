package pt.gov.dgarq.roda.plugins.description;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.UserBrowser;
import pt.gov.dgarq.roda.plugins.description.DescriptionTraverserPlugin;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;
import pt.gov.dgarq.roda.util.EmailUtility;

/**
 * @author Miguel Ferreira
 * @author Rui Castro
 */
public class AccessRestrictNotifierPlugin
		extends
		DescriptionTraverserPlugin<AccessRestrictNotifierAgent, AccessRestrictNotifierResult> {
	private static Logger logger = Logger
			.getLogger(AccessRestrictNotifierPlugin.class);

	/**
	 * PID of the Fonds to traverse
	 */
	public static PluginParameter PARAMETER_FONDS_PID() {
		return new PluginParameter(
				"fondsPID", PluginParameter.TYPE_STRING, null, false, false, //$NON-NLS-1$
				"Fonds PID"); //$NON-NLS-1$
	}

	/**
	 * 
	 */
	public static PluginParameter PARAMETER_NOTIFIER_EMAIL_ADDRESS() {
		return new PluginParameter(
				"notifierEmailAddress", PluginParameter.TYPE_STRING, //$NON-NLS-1$ 
				"access-restrict-notifier@roda.dgarq.gov.pt", true, false, //$NON-NLS-1$
				"Notifier's email address"); //$NON-NLS-1$
	}

	private RODAClient rodaClient = null;
	private Browser browserService = null;
	private UserBrowser userBrowserService = null;

	/**
	 * Construct a new {@link AccessRestrictNotifierPlugin}.
	 */
	public AccessRestrictNotifierPlugin() {
		super();
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
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
		return "Description tools/Notify access restrict expiration"; //$NON-NLS-1$
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
		return "Plugin that verifies the access restrict information of documents and notifies administrators. (If parameter 'Fonds PID' is empty all fonds will be treatead)";
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(AbstractPlugin.PARAMETER_RODA_CORE_URL(), AbstractPlugin
				.PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
				.PARAMETER_RODA_CORE_PASSWORD(), PARAMETER_FONDS_PID(),
				PARAMETER_NOTIFIER_EMAIL_ADDRESS(),AbstractPlugin.PARAMETER_RODA_CAS_URL());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		if (getFondsPID() == null) {
			report.setTitle("Access restrict verification in all fonds");
		} else {
			report.setTitle("Access restrict verification in fonds "
					+ getFondsPID());
		}
		report.addAttribute(new Attribute("Start datetime", DateParser
				.getIsoDate(new Date())));

		try {

			initRODAServices();

			User user = this.userBrowserService.getUser(getUsername());

			SimpleDescriptionObject[] fondsSDOs;

			if (getFondsPID() == null) {

				// All fonds will be traversed
				fondsSDOs = getBrowserService().getSimpleDescriptionObjects(
						new ContentAdapter(DescriptionObject.FONDS_FILTER,
								null, null));

				if (fondsSDOs == null) {
					fondsSDOs = new SimpleDescriptionObject[0];
				}

			} else {

				// Only the specified fonds will be traversed
				fondsSDOs = new SimpleDescriptionObject[] { getBrowserService()
						.getSimpleDescriptionObject(getFondsPID()) };
			}

			List<DescriptionObject> descriptionObjects = new ArrayList<DescriptionObject>();

			for (SimpleDescriptionObject fondsSDO : fondsSDOs) {

				ReportItem fondsReportItem = new ReportItem();
				fondsReportItem.setTitle("Fonds " + fondsSDO.getId());
				fondsReportItem.addAttribute(new Attribute("Start datetime",
						DateParser.getIsoDate(new Date())));

				AccessRestrictNotifierResult fondsResult = traverseSDO(fondsSDO);

				if (fondsResult.getDescriptionObjects().size() > 0) {
					descriptionObjects.addAll(fondsResult
							.getDescriptionObjects());
				}

				fondsReportItem.addAttribute(new Attribute("Result",
						fondsResult.getDescriptionObjects().size()
								+ " documents"));

				fondsReportItem.addAttribute(new Attribute("Finish datetime",
						DateParser.getIsoDate(new Date())));

				report.addItem(fondsReportItem);
			}

			if (descriptionObjects.size() > 0) {

				String notificationEmail = notifyUser(user, descriptionObjects,
						report);

				report.addAttribute(new Attribute("Notified user", String
						.format("%s (%s) <%s>", user.getFullName(), user
								.getName(), user.getEmail())));
				report.addAttribute(new Attribute("Notification email",
						notificationEmail));
			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			report.addAttribute(new Attribute("Error", e.getMessage()));
			report.addAttribute(new Attribute("Finish datetime", DateParser
					.getIsoDate(new Date())));

			throw new PluginException(e.getMessage(), e, report);
		}

		report.addAttribute(new Attribute("Finish datetime", DateParser
				.getIsoDate(new Date())));
		return report;
	}

	@Override
	protected Browser getBrowserService() {
		return this.browserService;
	}

	@Override
	protected AccessRestrictNotifierAgent getDescriptionTraverserAgent() {

		return new AccessRestrictNotifierAgent(this.browserService);

	}

	private String notifyUser(User user,
			List<DescriptionObject> descriptionObjects, Report report)
			throws MessagingException {

		String fromAddress = getParameterValues().get(
				PARAMETER_NOTIFIER_EMAIL_ADDRESS().getName());
		String toAddresses[] = new String[] { user.getEmail() };
		String subject = "Notificação de mudança de restrições de acesso";

		StringBuffer messageBody = new StringBuffer();

		// Header
		String headerLine1 = StringUtils.center("NOTIFICAÇÃO", 78);
		String headerLine2 = StringUtils.center(String.format("%s %tF",
				"produzido automaticamente em", new Date()), 78);

		messageBody.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		messageBody.append(String.format("*%s*%n", headerLine1)); //$NON-NLS-1$
		messageBody.append(String.format("*%s*%n", headerLine2)); //$NON-NLS-1$
		messageBody.append(StringUtils.repeat("*", 80) + "\n"); //$NON-NLS-1$//$NON-NLS-2$

		messageBody.append("\n\n"); //$NON-NLS-1$

		// List of documents
		messageBody.append("Lista de documentos" + ":\n");
		messageBody.append(StringUtils.repeat("=", 60) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		for (DescriptionObject dObject : descriptionObjects) {

			messageBody.append(StringUtils.rightPad("PID", 20) + ": "
					+ dObject.getPid() + "\n");

			messageBody.append(StringUtils.rightPad("Referência completa", 20)
					+ ": " + dObject.getCompleteReference() + "\n");

			messageBody.append(StringUtils.rightPad("Data de criação", 20)
					+ ": " + dObject.getDateFinal() + "\n");

			messageBody.append(StringUtils
					.rightPad("Restrições de accesso", 20)
					+ ": " + dObject.getAccessrestrict() + "\n");

			messageBody.append(StringUtils.rightPad("Handle", 20) + ": "
					+ dObject.getHandleURL() + "\n");

			messageBody.append("\n\n"); //$NON-NLS-1$
		}

		EmailUtility emailUtility = new EmailUtility("localhost");
		emailUtility.sendMail(fromAddress, toAddresses, subject, messageBody
				.toString());

		return messageBody.toString();
	}

	private URL getRodaServicesURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName()));
	}

	private String getUsername() {
		return getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
	}

	private String getPassword() {
		return getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());
	}

	private URL getCasURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CAS_URL().getName()));
	}
	private URL getCoreURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName()));
	}
	private String getFondsPID() {
		return getParameterValues().get(PARAMETER_FONDS_PID().getName());
	}

	private void initRODAServices() throws PluginException {

		try {

			CASUtility casUtility = new CASUtility(getCasURL(),  getCoreURL());
			this.rodaClient = new RODAClient(getRodaServicesURL(),
					getUsername(), getPassword(),casUtility);

			this.browserService = this.rodaClient.getBrowserService();
			this.userBrowserService = this.rodaClient.getUserBrowserService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new PluginException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}

}
