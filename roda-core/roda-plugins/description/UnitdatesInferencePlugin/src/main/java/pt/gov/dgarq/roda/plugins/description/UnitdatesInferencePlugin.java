package pt.gov.dgarq.roda.plugins.description;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.plugins.description.DescriptionTraverserPlugin;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class UnitdatesInferencePlugin
		extends
		DescriptionTraverserPlugin<UnitdateInferenceAgent, UnitdateInferenceResult> {
	private static Logger logger = Logger
			.getLogger(UnitdatesInferencePlugin.class);

	/**
	 * PID of the Fonds to traverse
	 */
	public static PluginParameter PARAMETER_FONDS_PID() {
		return new PluginParameter("fondsPID", PluginParameter.TYPE_STRING, //$NON-NLS-1$
				null, false, false, "Fonds PID"); //$NON-NLS-1$
	}

	private RODAClient rodaClient = null;
	private Browser browserService = null;
	private Editor editorService = null;

	/**
	 * Construct a new {@link UnitdatesInferencePlugin}.
	 */
	public UnitdatesInferencePlugin() {
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
		return "Description tools/Auto fill unit dates"; //$NON-NLS-1$
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
		return "Plugin that fills the description object unit dates based on the information of its descendants. (If parameter 'Fonds PID' is empty all fonds will be treatead)";
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(AbstractPlugin.PARAMETER_RODA_CORE_URL(),
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
						.PARAMETER_RODA_CORE_PASSWORD(), PARAMETER_FONDS_PID());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		if (getFondsPID() == null) {
			report.setTitle("Unitdate inference in all fonds");
		} else {
			report.setTitle("Unitdate inference in fonds " + getFondsPID());
		}
		report.addAttribute(new Attribute("Start datetime", DateParser
				.getIsoDate(new Date())));

		try {

			initRODAServices();

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

			for (SimpleDescriptionObject fondsSDO : fondsSDOs) {

				ReportItem fondsReportItem = new ReportItem();
				fondsReportItem.setTitle("Fonds " + fondsSDO.getId());
				fondsReportItem.addAttribute(new Attribute("Start datetime",
						DateParser.getIsoDate(new Date())));

				UnitdateInferenceResult fondsResult = traverseSDO(fondsSDO);

				fondsReportItem.addAttribute(new Attribute("Result",
						"Unitdates inferred successfully"));
				fondsReportItem.addAttribute(new Attribute(
						"Fonds initial date", fondsResult.getInitialDate()));
				fondsReportItem.addAttribute(new Attribute("Fonds final date",
						fondsResult.getFinalDate()));

				fondsReportItem.addAttribute(new Attribute("Finish datetime",
						DateParser.getIsoDate(new Date())));

				report.addItem(fondsReportItem);
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
	protected UnitdateInferenceAgent getDescriptionTraverserAgent() {

		return new UnitdateInferenceAgent(this.browserService,
				this.editorService);

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

	private String getFondsPID() {
		return getParameterValues().get(PARAMETER_FONDS_PID().getName());
	}
	
	private URL getCasURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				PARAMETER_RODA_CAS_URL().getName()));
	}
	private URL getCoreURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				PARAMETER_RODA_CORE_URL().getName()));
	}

	private void initRODAServices() throws PluginException {

		try {

			CASUtility casUtility = new CASUtility(getCasURL(), getCoreURL());
			this.rodaClient = new RODAClient(getRodaServicesURL(),
					getUsername(), getPassword(),casUtility);

			this.editorService = this.rodaClient.getEditorService();
			this.browserService = this.rodaClient.getBrowserService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new PluginException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}

}
