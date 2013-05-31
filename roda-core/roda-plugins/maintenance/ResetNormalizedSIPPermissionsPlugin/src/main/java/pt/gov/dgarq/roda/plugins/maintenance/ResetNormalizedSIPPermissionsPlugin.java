package pt.gov.dgarq.roda.plugins.maintenance;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.RODAObjectPermissions;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Editor;
import pt.gov.dgarq.roda.core.stubs.IngestMonitor;

/**
 * @author Rui Castro
 */
public class ResetNormalizedSIPPermissionsPlugin extends AbstractPlugin {
	private static Logger logger = Logger
			.getLogger(ResetNormalizedSIPPermissionsPlugin.class);

	private RODAClient rodaClient = null;
	private IngestMonitor ingestMonitorService = null;
	private Editor editorService = null;
	private Browser browserService = null;

	/**
	 * Constructs a new {@link ResetNormalizedSIPPermissionsPlugin}.
	 */
	public ResetNormalizedSIPPermissionsPlugin() {
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
		return "Maintenance/Reset permissions of normalized SIP objects"; //$NON-NLS-1$
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
		return "Reset the permissions of created objects for SIPs in state 'SIP_NORMALIZED'"; //$NON-NLS-1$
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

		ContentAdapter contentAdapter = new ContentAdapter();
		contentAdapter.setFilter(new Filter(new FilterParameter[] {
				new SimpleFilterParameter("state", "SIP_NORMALIZED"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleFilterParameter("processing", "false") })); //$NON-NLS-1$ //$NON-NLS-2$

		SIPState[] normalizedSIPStates = null;
		try {

			normalizedSIPStates = this.ingestMonitorService
					.getSIPs(contentAdapter);

			if (normalizedSIPStates == null) {
				normalizedSIPStates = new SIPState[0];
			}

			countFound = normalizedSIPStates.length;

		} catch (Exception e) {

			logger.debug("Error getting SIPs with state SIP_NORMALIZED - " //$NON-NLS-1$
					+ e.getMessage(), e);

			report.addAttribute(new Attribute("Error", e.getMessage())); //$NON-NLS-1$
			report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
					.getIsoDate(new Date())));

			throw new PluginException(e.getMessage(), e, report);
		}

		for (SIPState sipState : normalizedSIPStates) {

			ReportItem reportItem = new ReportItem("SIP " + sipState.getId() //$NON-NLS-1$
					+ " - DO " + sipState.getIngestedPID()); //$NON-NLS-1$

			try {

				RODAObjectPermissions permissions = this.browserService
						.getRODAObjectPermissions(sipState.getIngestedPID());

				// Simply set the same permissions again. The defaults will be
				// restored.
				this.editorService.setRODAObjectPermissions(permissions, true);

				logger.debug("Setting permissions " + permissions);

				reportItem.addAttribute(new Attribute("Outcome", "success"));
				reportItem.addAttribute(new Attribute("Action",
						"Permissions set " + permissions));

				countUpdated++;

			} catch (Exception e) {
				logger.debug("Error setting permissions of DO " //$NON-NLS-1$
						+ sipState.getIngestedPID() + " - " //$NON-NLS-1$
						+ e.getMessage(), e);

				reportItem.addAttribute(new Attribute("Outcome", "error")); //$NON-NLS-1$ //$NON-NLS-2$
				reportItem.addAttribute(new Attribute("Error", e.getMessage())); //$NON-NLS-1$

				countError++;
			}

			report.addItem(reportItem);
		}

		report.addAttribute(new Attribute("Finish datetime", DateParser //$NON-NLS-1$
				.getIsoDate(new Date())));
		report.addAttribute(new Attribute("SIPs found", Integer //$NON-NLS-1$
				.toString(countFound)));
		report.addAttribute(new Attribute("SIPs updated", Integer //$NON-NLS-1$
				.toString(countUpdated)));
		report.addAttribute(new Attribute("SIPs not updated (errors)", //$NON-NLS-1$
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

	private void initRODAServices() throws PluginException {

		try {

			this.rodaClient = new RODAClient(getRodaServicesURL(),
					getUsername(), getPassword());

			this.browserService = this.rodaClient.getBrowserService();
			this.editorService = this.rodaClient.getEditorService();
			this.ingestMonitorService = this.rodaClient
					.getIngestMonitorService();

		} catch (Exception e) {
			logger.debug("Error creating RODA client services - " //$NON-NLS-1$
					+ e.getMessage(), e);
			throw new PluginException("Error creating RODA client services - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

}
