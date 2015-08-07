package pt.gov.dgarq.roda.plugins.maintenance;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.fedora.FedoraClientException;
import pt.gov.dgarq.roda.core.fedora.FedoraClientUtility;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class UpgradePreservedByRelationshipPlugin extends AbstractPlugin {
	private static Logger logger = Logger
			.getLogger(UpgradePreservedByRelationshipPlugin.class);
	
	private boolean initOK = false;

	public static PluginParameter PARAMETER_FEDORA_URL() {
		return new PluginParameter("fedoraURL", PluginParameter.TYPE_STRING, //$NON-NLS-1$
				"http://localhost:8080/fedora", true, false, "Fedora URL"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static PluginParameter PARAMETER_FEDORAGSEARCH_URL() {
		return new PluginParameter(
				"fedoraGSearchURL", //$NON-NLS-1$
				PluginParameter.TYPE_STRING,
				"http://localhost:8080/fedoragsearch", true, false, //$NON-NLS-1$
				"Fedora GSearch URL"); //$NON-NLS-1$
	}

	private FedoraClientUtility fedoraClientUtility = null;

	/**
	 * Constructs a new {@link UpgradePreservedByRelationshipPlugin}.
	 */
	public UpgradePreservedByRelationshipPlugin() {
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
		return "Maintenance/Upgrade 'preserved-by' relationships"; //$NON-NLS-1$
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
		return "This plugin transforms <RO, 'preserved-by', RPO> relationships in <RPO, 'preservation-of', RO>."; //$NON-NLS-1$
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(PARAMETER_RODA_CORE_URL(),
				PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD(),
				PARAMETER_FEDORA_URL(), PARAMETER_FEDORAGSEARCH_URL());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		if(!initOK){
			initRODAServices();
		}
		
		
		
		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("Report of plugin " + getName()); //$NON-NLS-1$
		report.setAttributes(new Attribute[] {
				new Attribute("Agent name", getName()), //$NON-NLS-1$
				new Attribute("Agent version", Float.toString(getVersion())), //$NON-NLS-1$
				new Attribute("Start datetime", DateParser //$NON-NLS-1$
						.getIsoDate(new Date())) });
		
		try {

			initRODAServices();

		} catch (PluginException e) {
			logger.debug("Error in initRODAServices - " //$NON-NLS-1$
					+ e.getMessage(), e);

			report.addAttribute(new Attribute("Error", e.getMessage())); //$NON-NLS-1$
			report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
					.getIsoDate(new Date())));

			e.setReport(report);
			throw e;
		}

		int countFound = 0;
		int countUpdated = 0;
		int countError = 0;

		Map<String, String> mapPreservedBy = null;
		try {

			mapPreservedBy = this.fedoraClientUtility
					.getPreservedByRelationships();

		} catch (FedoraClientException e) {

			logger.debug("Error getting 'preserved-by' relationships - " //$NON-NLS-1$
					+ e.getMessage(), e);

			report.addAttribute(new Attribute("Error", e.getMessage())); //$NON-NLS-1$
			report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
					.getIsoDate(new Date())));

			throw new PluginException(e.getMessage(), e, report);
		}

		countFound = mapPreservedBy.size();

		for (String roPID : mapPreservedBy.keySet()) {

			String rpoPID = mapPreservedBy.get(roPID);

			ReportItem reportItem = new ReportItem(
					"<" + roPID + ", preserved-by, " + rpoPID + ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			try {

				this.fedoraClientUtility.addPreservationOfRelationship(rpoPID,
						roPID);

				logger.debug("Relationship created <" + rpoPID //$NON-NLS-1$
						+ ", preservation-of," + roPID + ">"); //$NON-NLS-1$ //$NON-NLS-2$

				reportItem.addAttribute(new Attribute("Outcome", "success")); //$NON-NLS-1$ //$NON-NLS-2$
				reportItem.addAttribute(new Attribute("Action", //$NON-NLS-1$
						"Created relationship <" + rpoPID //$NON-NLS-1$
								+ ", preservation-of," + roPID + ">")); //$NON-NLS-1$ //$NON-NLS-2$

				countUpdated++;

			} catch (FedoraClientException e) {

				logger.debug("Error creating relationship <" + rpoPID //$NON-NLS-1$
						+ ", preservation-of," + roPID + "> - " //$NON-NLS-1$ //$NON-NLS-2$
						+ e.getMessage(), e);

				reportItem.addAttribute(new Attribute("Outcome", "error")); //$NON-NLS-1$ //$NON-NLS-2$
				reportItem.addAttribute(new Attribute("Error", e.getMessage())); //$NON-NLS-1$

				countError++;
			}

			report.addItem(reportItem);

		}

		report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
				.getIsoDate(new Date())));
		report.addAttribute(new Attribute("Relationships found", Integer //$NON-NLS-1$
				.toString(countFound)));
		report.addAttribute(new Attribute("Relationships updated", Integer //$NON-NLS-1$
				.toString(countUpdated)));
		report.addAttribute(new Attribute("Relationships not updated (errors)", //$NON-NLS-1$
				Integer.toString(countError)));

		// Report for the execution of this Plugin
		return report;
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

	private String getFedoraURL() {
		return getParameterValues().get(PARAMETER_FEDORA_URL().getName());
	}

	private String getFedoraGSearchURL() {
		return getParameterValues()
				.get(PARAMETER_FEDORAGSEARCH_URL().getName());
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
			// FIXME empty string
			CASUserPrincipal cup = casUtility.getCASUserPrincipal(getUsername(), getPassword(),"");
			new RODAClient(getRodaServicesURL(),cup,casUtility);

			this.fedoraClientUtility = new FedoraClientUtility(getFedoraURL(),getFedoraGSearchURL(), cup, casUtility);
			initOK=true;
		} catch (Exception e) {
			logger.debug("Error creating RODA client services - " //$NON-NLS-1$
					+ e.getMessage(), e);
			throw new PluginException("Error creating RODA client services - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

}
