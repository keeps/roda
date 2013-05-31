package pt.gov.dgarq.roda.plugins.common;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.common.InvalidParameterException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTask;
import pt.gov.dgarq.roda.core.ingest.SIPAlreadyProcessingException;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;

/**
 * @author Rui Castro
 */
public abstract class IngestTaskPlugin extends IngestTask implements Plugin {
	private static Logger logger = Logger.getLogger(IngestTaskPlugin.class);

	protected Map<String, String> parameterMap = new HashMap<String, String>();

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public IngestTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
		super();
	}

	/**
	 * @see Plugin#getParameterValues()
	 */
	public Map<String, String> getParameterValues() {
		return parameterMap;
	}

	/**
	 * @see Plugin#setParameterValues(Map)
	 */
	public void setParameterValues(Map<String, String> parameters)
			throws InvalidParameterException {
		parameterMap.clear();
		if (parameters != null) {
			parameterMap.putAll(parameters);
		}
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("Report for ingest task " + getName());
		report.addAttribute(new Attribute("Start time", DateParser
				.getIsoDate(Calendar.getInstance().getTime())));

		List<SIPState> availableSIPs;
		try {

			availableSIPs = getAvailableSIPs();

		} catch (IngestRegistryException e) {

			logger.error("Error getting list of available SIPs - "
					+ e.getMessage(), e);

			report
					.addAttribute(new Attribute("Error",
							"Error getting list of available SIPs - "
									+ e.getMessage()));

			report.addAttribute(new Attribute("Finnish time", DateParser
					.getIsoDate(new Date())));

			throw new PluginException("Error getting list of available SIPs - "
					+ e.getMessage(), e, report);
		}

		for (SIPState sipState : availableSIPs) {

			ReportItem reportItem = new ReportItem("SIP " + sipState.getId());
			reportItem.addAttribute(new Attribute("Start time", DateParser
					.getIsoDate(new Date())));

			try {

				processSIP(sipState);

				logger.info("SIP " + sipState.getId()
						+ " processed successfully.");

				reportItem.addAttribute(new Attribute("Result", "success"));

			} catch (SIPAlreadyProcessingException e) {

				logger.error("SIP " + sipState.getId()
						+ " is already being processed - " + e.getMessage(), e);

				reportItem.addAttribute(new Attribute("Error", "SIP "
						+ sipState.getId() + " is already being processed - "
						+ e.getMessage()));

			} catch (IngestTaskException e) {

				logger.error("Ingest task error processing SIP "
						+ sipState.getId() + " - " + e.getMessage(), e);

				reportItem.addAttribute(new Attribute("Error",
						"Ingest task error processing SIP " + sipState.getId()
								+ " - " + e.getMessage()));

			} catch (Throwable e) {

				logger.error("Unknown exception caught processing SIP - "
						+ e.getMessage(), e);

				reportItem.addAttribute(new Attribute("Error",
						"Unknown exception caught processing SIP - "
								+ e.getMessage()));
			}

			reportItem.addAttribute(new Attribute("Finnish time", DateParser
					.getIsoDate(new Date())));

			report.addItem(reportItem);
		}

		report.addAttribute(new Attribute("Summary", availableSIPs.size()
				+ " SIPs treated"));
		report.addAttribute(new Attribute("Finnish time", DateParser
				.getIsoDate(Calendar.getInstance().getTime())));

		return report;
	}

}
