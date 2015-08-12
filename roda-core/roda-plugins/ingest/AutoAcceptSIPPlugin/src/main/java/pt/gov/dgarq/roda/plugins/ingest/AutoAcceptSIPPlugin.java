package pt.gov.dgarq.roda.plugins.ingest;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.AcceptSIPException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.IngestMonitorException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchSIPException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.AcceptSIP;
import pt.gov.dgarq.roda.core.stubs.IngestMonitor;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

/**
 * @author Rui Castro
 */
public class AutoAcceptSIPPlugin extends AbstractPlugin {
	private static final Logger logger = Logger
			.getLogger(AutoAcceptSIPPlugin.class);

	private final String name = "Ingest tools/Auto accept SIP";
	private final float version = 1.0f;
	private final String description = "Automatically accept SIPs.";

	/**
	 * Producer user name plugin parameter
	 */
	public static PluginParameter PARAMETER_PRODUCER_USERNAME() {
		return new PluginParameter("producerUsername",
				PluginParameter.TYPE_STRING, null, true, false,
				"Producer username");
	}

	private static final String INITIAL_SIP_STATE = "SIP_NORMALIZED";

	private RODAClient rodaClient = null;
	private IngestMonitor ingestMonitorService = null;
	private AcceptSIP acceptSIPService = null;

	/**
	 * @param parameters
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public AutoAcceptSIPPlugin() {
		// super(AbstractPlugin.PARAMETER_RODA_CORE_URL(), AbstractPlugin
		// .PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
		// .PARAMETER_RODA_CORE_PASSWORD(), PARAMETER_PRODUCER_USERNAME());
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
		logger.info("init OK");
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		logger.info("shutdown OK");
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
		return Arrays.asList(AbstractPlugin.PARAMETER_RODA_CORE_URL(),
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME(), AbstractPlugin
						.PARAMETER_RODA_CORE_PASSWORD(),
				PARAMETER_PRODUCER_USERNAME(),AbstractPlugin.PARAMETER_RODA_CAS_URL());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		initRODAServices();

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("AutoAccept Task report");
		report.addAttribute(new Attribute("start time", DateParser
				.getIsoDate(Calendar.getInstance().getTime())));

		try {

			List<SIPState> availableSIPs = getAvailableSIPs();
			for (SIPState sip : availableSIPs) {

				ReportItem reportItem = new ReportItem("SIP " + sip.getId());

				try {

					this.acceptSIPService.acceptSIP(sip.getId(), true, "");

					reportItem.addAttribute(new Attribute("accepted", sip
							.getId()));

				} catch (NoSuchSIPException e) {
					logger.warn("Exception trying to accept SIP " + sip.getId()
							+ " - " + e.getMessage(), e);
					reportItem.addAttribute(new Attribute("error", e
							.getMessage()));
				} catch (AcceptSIPException e) {
					logger.warn("Exception trying to accept SIP " + sip.getId()
							+ " - " + e.getMessage(), e);
					reportItem.addAttribute(new Attribute("error", e
							.getMessage()));
				} catch (IllegalOperationException e) {
					logger.warn("Exception trying to accept SIP " + sip.getId()
							+ " - " + e.getMessage(), e);
					reportItem.addAttribute(new Attribute("error", e
							.getMessage()));
				} catch (RemoteException e) {
					logger.warn("Exception trying to accept SIP " + sip.getId()
							+ " - " + e.getMessage(), e);
					reportItem.addAttribute(new Attribute("error", e
							.getMessage()));
				}

				reportItem.addAttribute(new Attribute("time", DateParser
						.getIsoDate(Calendar.getInstance().getTime())));

				report.addItem(reportItem);
			}

			report.addAttribute(new Attribute("finish time", DateParser
					.getIsoDate(Calendar.getInstance().getTime())));

			return report;

		} catch (IngestMonitorException e) {
			logger.error(
					"Exception getting available SIPs - " + e.getMessage(), e);
			throw new PluginException("Exception getting available SIPs - "
					+ e.getMessage(), e);
		} catch (RemoteException e) {
			logger.error(
					"Exception getting available SIPs - " + e.getMessage(), e);
			throw new PluginException("Exception getting available SIPs - "
					+ e.getMessage(), e);
		} catch (Throwable e) {
			logger.error("Unknown exception caught processing SIP - "
					+ e.getMessage(), e);
			throw new PluginException(
					"Unknown exception caught processing SIP - "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Returns a {@link List} of {@link SIPState} for SIPs waiting to be
	 * accepted.
	 * 
	 * @throws IngestMonitorException
	 * @throws RemoteException
	 */
	private List<SIPState> getAvailableSIPs() throws IngestMonitorException,
			RemoteException {

		String producerName = getParameterValues().get(
				PARAMETER_PRODUCER_USERNAME().getName());

		Filter availableSIPsFromProducer = new Filter();
		availableSIPsFromProducer.add(new SimpleFilterParameter("state", INITIAL_SIP_STATE));
		availableSIPsFromProducer.add(new SimpleFilterParameter("processing", "false"));
		availableSIPsFromProducer.add(new SimpleFilterParameter("username", producerName));
		Sorter sorterOlderFirst = new Sorter(
				new SortParameter[] { new SortParameter("datetime", false) });
		ContentAdapter firstAvailableSIP = new ContentAdapter(
				availableSIPsFromProducer, sorterOlderFirst, null);

		SIPState[] sips = this.ingestMonitorService.getSIPs(firstAvailableSIP);

		if (sips == null) {
			return new ArrayList<SIPState>();
		} else {
			return Arrays.asList(sips);
		}
	}

	private void initRODAServices() throws PluginException {

		String rodaClientServiceUrl = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		String rodaClientUsername = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
		String rodaClientPassword = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());
		
		
		String casURL = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CAS_URL().getName());
		String coreURL = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		try {
			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL));
			this.rodaClient = new RODAClient(new URL(rodaClientServiceUrl),
					rodaClientUsername, rodaClientPassword,casUtility);
			this.ingestMonitorService = this.rodaClient
					.getIngestMonitorService();
			this.acceptSIPService = this.rodaClient.getAcceptSIPService();

		} catch (RODAClientException e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(),
					e);
			throw new PluginException("Exception creating RODA Client - "
					+ e.getMessage(), e);

		} catch (LoginException e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(),
					e);
			throw new PluginException("Exception creating RODA Client - "
					+ e.getMessage(), e);

		} catch (MalformedURLException e) {

			logger.debug("Exception creating service URL - " + e.getMessage(),
					e);
			throw new PluginException("Exception creating service URL - "
					+ e.getMessage(), e);

		}

	}

}
