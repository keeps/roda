package pt.gov.dgarq.roda.plugins.ingest;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.IngestTaskException;
import pt.gov.dgarq.roda.core.common.InvalidIngestStateException;
import pt.gov.dgarq.roda.core.common.InvalidParameterException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.SIPState;
import pt.gov.dgarq.roda.core.data.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.ingest.IngestRegistryException;
import pt.gov.dgarq.roda.core.ingest.IngestTask;
import pt.gov.dgarq.roda.core.ingest.IngestTaskResult;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.plugins.PluginManager;
import pt.gov.dgarq.roda.core.plugins.PluginManagerException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Ingest;
import pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException;
import pt.gov.dgarq.roda.plugins.common.IngestTaskPlugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;

/**
 * This task uses migration plugins to perform normalization on representations
 * ingested.
 * 
 * @author Rui Castro
 */
public class NormalizationTaskPlugin extends IngestTaskPlugin {
	static final private Logger logger = Logger
			.getLogger(NormalizationTaskPlugin.class);

	private final String name = "Ingest/Normalize format"; //$NON-NLS-1$
	private final float version = 1.0f;
	private final String description = "Convert representations to normalized formats."; //$NON-NLS-1$

	private final static String PROPERTIES_FILE_KEY = NormalizationTaskPlugin.class
			.getName() + ".propertiesFile"; //$NON-NLS-1$

	private PluginManager pluginManager;
	private PropertiesConfiguration normalizationProperties;

	private RODAClient rodaClient = null;
	private Browser browserService = null;
	private Ingest ingestService = null;

	private String ingestedDOPID;

	/**
	 * @throws InvalidIngestStateException
	 * @throws IngestRegistryException
	 */
	public NormalizationTaskPlugin() throws InvalidIngestStateException,
			IngestRegistryException {
	}

	/**
	 * @see Plugin#init()
	 */
	@SuppressWarnings("unchecked")
	public void init() throws PluginException {

		// Get a reference to the PluginManager
		try {

			pluginManager = PluginManager.getDefaultPluginManager();

		} catch (PluginManagerException e) {
			logger.debug("Error getting Plugin Manager - " + e.getMessage(), e); //$NON-NLS-1$
			throw new PluginException("Error getting Plugin Manager - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}

		// Get the name of the properties file from the ingest task
		// configuration
		Configuration ingestManagerConfiguration = getIngestManagerConfiguration();

		// Read the normalization properties file
		String propertiesFile;
		if (ingestManagerConfiguration.containsKey(PROPERTIES_FILE_KEY)) {
			propertiesFile = ingestManagerConfiguration
					.getString(PROPERTIES_FILE_KEY);
			logger.info("Properties file is " + propertiesFile); //$NON-NLS-1$
		} else {
			propertiesFile = "normalization.properties"; //$NON-NLS-1$
			logger.warn("Properties file (" + PROPERTIES_FILE_KEY //$NON-NLS-1$
					+ ") is not defined!!! Reading default file " //$NON-NLS-1$
					+ propertiesFile);
		}

		this.normalizationProperties = new PropertiesConfiguration();

		try {

			String RODA_HOME = null;

			if (System.getProperty("roda.home") != null) {
				RODA_HOME = System.getProperty("roda.home");//$NON-NLS-1$
			} else if (System.getenv("RODA_HOME") != null) {
				RODA_HOME = System.getenv("RODA_HOME"); //$NON-NLS-1$
			} else {
				RODA_HOME = null;
			}

			if (StringUtils.isBlank(RODA_HOME)) {
				throw new PluginException(
						"RODA_HOME enviroment variable and ${roda.home} system property are not set.");
			}

			File RODA_PLUGINS_CONFIG_DIRECTORY = new File(new File(RODA_HOME,
					"config"), "plugins");

			File configFile = new File(RODA_PLUGINS_CONFIG_DIRECTORY,
					propertiesFile);

			logger.debug("Trying to load configuration file from " + configFile);

			if (configFile.isFile()) {
				this.normalizationProperties.load(configFile);
				logger.info("Loading configuration file from " + configFile);
			} else {
				this.normalizationProperties.load(getClass().getResourceAsStream(
						propertiesFile));
				logger.info("Loading default configuration file from resources");
			}

			if (logger.isDebugEnabled()) {
				Iterator<String> keyIterator = this.normalizationProperties
						.getKeys();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					logger.debug(key + "=" //$NON-NLS-1$
							+ this.normalizationProperties.getString(key));
				}
			}

		} catch (ConfigurationException e) {
			logger.debug(
					"Error reading plugin configuration - " + e.getMessage(), e);
			throw new PluginException("Error reading plugin configuration - "
					+ e.getMessage(), e);
		}

		logger.info("init OK"); //$NON-NLS-1$
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		logger.info("shutdown OK"); //$NON-NLS-1$
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
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME(),
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD());
	}

	/**
	 * @see IngestTask#doTask(SIPState)
	 */
	@Override
	protected IngestTaskResult doTask(SIPState sip) throws IngestTaskException {

		StringBuffer report = new StringBuffer();

		initRODAServices();

		// the ingested DO PID
		String doPID = sip.getIngestedPID();
		this.ingestedDOPID = sip.getIngestedPID();

		List<SimpleRepresentationObject> representations;
		try {

			representations = getRepresentations(doPID, null);

		} catch (RODAException e) {

			logger.error("Error getting representations of DO " + doPID + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ e.getMessage(), e);

			report.append(String.format(
					Messages.getString("NormalizationTaskPlugin.ERROR_GETTING_REPRESENTATIONS_OF_DO_X"), //$NON-NLS-1$
					doPID, e.getMessage()));

			// return new IngestTaskResult(false, report.toString());
			this.ingestedDOPID = null;
			throw new IngestTaskException(
					"Error getting representations of DO " + doPID + " - "
							+ e.getMessage(), e);
		}

		report.append(String.format(
				Messages.getString("NormalizationTaskPlugin.SIP_HAS_X_REPRESENTATIONS"),//$NON-NLS-1$
				representations.size()));

		logger.debug(String.format("SIP has %d representations %s\n", //$NON-NLS-1$
				representations.size(), representations));

		if (representations.size() == 0) {

			report.append(Messages
					.getString("NormalizationTaskPlugin.SIP_DOESNT_HAVE_REPRESENATATIONS")); //$NON-NLS-1$

			logger.debug("SIP doesn't have representations."); //$NON-NLS-1$

			sip.setIngestedPID(null);
			return new IngestTaskResult(false, report.toString());

		} else {

			for (SimpleRepresentationObject simpleRO : representations) {

				try {

					report.append(String.format(
							Messages.getString("NormalizationTaskPlugin.PROCESSING_REPRESENTATION"), simpleRO //$NON-NLS-1$
									.getPid()));

					String reportLines = processRepresentation(simpleRO);

					report.append(reportLines);
					report.append(String.format(
							Messages.getString("NormalizationTaskPlugin.REPRESENTATION_X_PROCESSED_SUCCESSFULLY"), simpleRO //$NON-NLS-1$
									.getPid()));

				} catch (PluginException e) {

					logger.error(e.getMessage(), e);

					report.append(String.format(
							Messages.getString("NormalizationTaskPlugin.ERROR_PROCESSING_REPRESENTATION_X"), //$NON-NLS-1$
							simpleRO.getPid() + " - " //$NON-NLS-1$
									+ e.getMessage()));

					if (e.getReport() != null) {
						report.append(String.format(
								Messages.getString("NormalizationTaskPlugin.NOMALIZATION_PLUGIN_IS_X"), //$NON-NLS-1$
								getReportAsString(e.getReport())));
					}

					if (e.getReportItem() != null) {
						report.append(String.format(
								Messages.getString("NormalizationTaskPlugin.NORMALIZATION_PLUGIN_REPORT_IS_X"), //$NON-NLS-1$
								getReportItemAsString(e.getReportItem())));
					}

					sip.setIngestedPID(null);
					return new IngestTaskResult(false, report.toString());
				}

			}
		}

		return new IngestTaskResult(true, report.toString());
	}

	@Override
	protected void doCleanup(SIPState sip, IngestTaskResult taskResult) {

		if (taskResult.isPassed()) {
			// Normalization went ok. Do clean
			logger.info("doCleanup for SIP " + sip.getId() + ". Nothing to cleanup"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {

			// Normalization failed. Remove all objects
			logger.info("doCleanup for SIP " + sip.getId() //$NON-NLS-1$
					+ ". Removing created objects"); //$NON-NLS-1$

			try {

				this.ingestService.removeDescriptionObject(this.ingestedDOPID);

			} catch (Exception e) {
				logger.warn(
						"Error removing ingested objects - " + e.getMessage(),
						e);
			}

		}

		this.ingestedDOPID = null;
	}

	@Override
	protected void undoTask(SIPState sip, IngestTaskResult taskResult) {
		logger.warn("undoTask for SIP " + sip.getId() + ". Nothing to undo"); //$NON-NLS-1$ //$NON-NLS-2$
		this.ingestedDOPID = null;
	}

	private String processRepresentation(SimpleRepresentationObject simpleRO)
			throws PluginException {

		StringBuffer report = new StringBuffer();

		if (this.normalizationProperties
				.containsKey(simpleRO.getContentModel())) {

			String normalizationPlugin = this.normalizationProperties
					.getString(simpleRO.getContentModel());

			if (StringUtils.isBlank(normalizationPlugin)) {
				// The mimetype is known, but there's no plugin assigned.

				// It means the representation is already normalised
				String reportLine = String
						.format(Messages
								.getString("NormalizationTaskPlugin.REPRESENTATION_X_IS_ALREADY_NORMALIZED"), //$NON-NLS-1$
								simpleRO.getPid(), simpleRO.getContentModel());
				report.append(reportLine);
				logger.debug(reportLine);

				try {

					String normalizedROPID = this.ingestService
							.setDONormalizedRepresentation(
									simpleRO.getDescriptionObjectPID(),
									simpleRO.getPid());

					logger.info("Marked representation " + normalizedROPID //$NON-NLS-1$
							+ " as normalized"); //$NON-NLS-1$

				} catch (NoSuchRODAObjectException e) {
					throw new PluginException(e.getMessage(), e);
				} catch (IngestException e) {
					throw new PluginException(e.getMessage(), e);
				} catch (RemoteException e) {
					RODAException exception = RODAClient
							.parseRemoteException(e);
					throw new PluginException(exception.getMessage(), exception);
				}

			} else {
				// We have a normalization plugin. Let's call it.

				String reportLine = String
						.format(Messages
								.getString("NormalizationTaskPlugin.REPRESENTATION_X_PROCESSED_BY_PLUGIN_X"),//$NON-NLS-1$
								simpleRO.getPid(), normalizationPlugin);
				report.append(reportLine);

				logger.debug(reportLine);

				logger.info("Normalization plugin for RO " + simpleRO.getPid()//$NON-NLS-1$
						+ " is " + normalizationPlugin); //$NON-NLS-1$

				Report pluginReport = executeNormalizationPlugin(
						normalizationPlugin, simpleRO);

				report.append(String.format(
						Messages.getString("NormalizationTaskPlugin.DETAILS_OF_PLUGIN_X"), //$NON-NLS-1$
						normalizationPlugin));
				report.append(getReportAsString(pluginReport));
			}

		} else {
			// We don't know this content model

			String reportLine = String
					.format(Messages
							.getString("NormalizationTaskPlugin.REPRESENTATION_X_HAS_UNKOWN_CMODEL_X_NO_ACTION"),//$NON-NLS-1$
							simpleRO.getPid(), simpleRO.getContentModel());

			report.append(reportLine);
			logger.debug(reportLine);
		}

		return report.toString();
	}

	private String getReportAsString(Report pluginReport) {

		StringBuffer reportLines = new StringBuffer();

		// Make 1 line for each report attribute
		for (Attribute attribute : pluginReport.getAttributes()) {
			reportLines.append("\t" + attribute.getName() + ": " //$NON-NLS-1$ //$NON-NLS-2$
					+ attribute.getValue() + "\n"); //$NON-NLS-1$
		}

		for (ReportItem reportItem : pluginReport.getItems()) {
			// Make 1 line for each reportItem attribute
			reportLines.append(getReportItemAsString(reportItem));
		}

		return reportLines.toString();
	}

	private String getReportItemAsString(ReportItem pluginReportItem) {

		StringBuffer reportLines = new StringBuffer();

		for (Attribute attribute : pluginReportItem.getAttributes()) {
			reportLines.append("\t" + pluginReportItem.getTitle() + " > " //$NON-NLS-1$ //$NON-NLS-2$
					+ attribute.getName() + ": " + attribute.getValue() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return reportLines.toString();
	}

	private Report executeNormalizationPlugin(String pluginClassname,
			SimpleRepresentationObject simpleRO) throws PluginException {

		Report report = null;

		// Get the plugin with the given name
		Plugin convertionPlugin = pluginManager.getPlugin(pluginClassname);

		if (convertionPlugin == null) {
			throw new PluginException(pluginClassname
					+ " is not a valid plugin class name"); //$NON-NLS-1$
		}

		// Set the plugin parameters
		try {

			logger.debug("Setting plugin " + convertionPlugin.getName() //$NON-NLS-1$
					+ " parameters " + getParameterValues()); //$NON-NLS-1$

			convertionPlugin.setParameterValues(getPluginParameters(
					convertionPlugin, simpleRO.getPid()));

		} catch (InvalidParameterException e) {
			logger.debug("Error setting plugin parameters - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new PluginException("Error setting plugin parameters - " //$NON-NLS-1$
					+ e.getMessage(), e);
		}

		// Execute the conversion plugin
		try {

			logger.debug("Starting execution of plugin " //$NON-NLS-1$
					+ convertionPlugin.getName() + " with parameters " //$NON-NLS-1$
					+ getParameterValues());

			// Execute the plugin and collect the report.
			report = convertionPlugin.execute();

			logger.info("Plugin terminated without error."); //$NON-NLS-1$
			logger.info("Execution report is " + report); //$NON-NLS-1$

		} catch (PluginException e) {

			if (e.getCause() instanceof RepresentationAlreadyConvertedException) {

				logger.debug(
						"RepresentationAlreadyConvertedConverterException executing plugin - " //$NON-NLS-1$
								+ e.getMessage(), e);
				throw new PluginException("Error executing plugin - " //$NON-NLS-1$
						+ e.getMessage(), e);

			} else {

				logger.debug("Error executing plugin - " + e.getMessage(), e); //$NON-NLS-1$
				throw new PluginException("Error executing plugin - " //$NON-NLS-1$
						+ e.getMessage(), e, e.getReport(), e.getReportItem());

			}
		}

		// Check if operation was done
		try {

			RepresentationObject normalizedRepresentation = this.browserService
					.getDONormalizedRepresentation(simpleRO
							.getDescriptionObjectPID());

			if (normalizedRepresentation == null) {
				// No normalized representation. Operation was not
				// done!!

				throw new PluginException("DO " //$NON-NLS-1$
						+ simpleRO.getDescriptionObjectPID()
						+ " doesn't have normalized representation.", null, //$NON-NLS-1$
						report);
			}

		} catch (Exception e) {

			logger.debug("Error getting normalized representation for DO " //$NON-NLS-1$
					+ simpleRO.getDescriptionObjectPID() + " - " //$NON-NLS-1$
					+ e.getMessage(), e);
			throw new PluginException(
					"Error getting normalized representation for DO " //$NON-NLS-1$
							+ simpleRO.getDescriptionObjectPID() + " - " //$NON-NLS-1$
							+ e.getMessage(), e, report);
		}

		return report;
	}

	private Map<String, String> getPluginParameters(Plugin plugin, String roPID)
			throws InvalidParameterException {

		Map<String, String> parameterValues = new HashMap<String, String>(
				getParameterValues());

		parameterValues.put(AbstractRodaMigratorPlugin.PARAMETER_PIDS()
				.getName(), roPID);
		parameterValues.put(AbstractRodaMigratorPlugin
				.PARAMETER_MAKE_OBJECTS_ACTIVE().getName(), Boolean
				.toString(false));
		parameterValues.put(AbstractRodaMigratorPlugin
				.PARAMETER_FAIL_AT_FIRST_ERROR().getName(), Boolean
				.toString(true));

		return parameterValues;
	}

	private void initRODAServices() throws IngestTaskException {

		String rodaClientServiceUrl = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
		String rodaClientUsername = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
		String rodaClientPassword = getParameterValues().get(
				AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());

		try {

			this.rodaClient = new RODAClient(new URL(rodaClientServiceUrl),
					rodaClientUsername, rodaClientPassword);

			this.browserService = this.rodaClient.getBrowserService();
			this.ingestService = this.rodaClient.getIngestService();

		} catch (Exception e) {

			logger.debug("Exception creating RODA Client - " + e.getMessage(), //$NON-NLS-1$
					e);
			throw new IngestTaskException("Exception creating RODA Client - " //$NON-NLS-1$
					+ e.getMessage(), e);

		}

	}

	private List<SimpleRepresentationObject> getRepresentations(String doPID,
			List<SimpleRepresentationObject> rObjects) throws RODAException {

		try {

			if (rObjects == null) {
				rObjects = new ArrayList<SimpleRepresentationObject>();
			}
			// Get the representations of the given DO
			Filter filterDOReps = new Filter(
					new FilterParameter[] { new SimpleFilterParameter(
							"descriptionObjectPID", doPID) }); //$NON-NLS-1$
			SimpleRepresentationObject[] simpleROs = this.browserService
					.getSimpleRepresentationObjects(new ContentAdapter(
							filterDOReps, null, null));

			if (simpleROs != null) {
				rObjects.addAll(Arrays.asList(simpleROs));
			}

			// Get the child DOs of the given DO
			Filter childFilter = new Filter(new SimpleFilterParameter(
					"parentPID", doPID)); //$NON-NLS-1$
			ContentAdapter childContentAdapter = new ContentAdapter(
					childFilter, null, null);
			SimpleDescriptionObject[] childSDOs = this.browserService
					.getSimpleDescriptionObjects(childContentAdapter);

			// for each child, get their representations
			if (childSDOs != null) {
				for (SimpleDescriptionObject childSDO : childSDOs) {
					rObjects = getRepresentations(childSDO.getPid(), rObjects);
				}
			}

			return rObjects;

		} catch (RemoteException e) {
			throw RODAClient.parseRemoteException(e);
		}
	}

}
