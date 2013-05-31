package pt.gov.dgarq.roda.plugins.converters.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.Downloader;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.UploadException;
import pt.gov.dgarq.roda.core.Uploader;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.IngestException;
import pt.gov.dgarq.roda.core.common.LoginException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.common.RODAClientException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.core.stubs.Ingest;
import pt.gov.dgarq.roda.migrator.MigratorClient;
import pt.gov.dgarq.roda.migrator.MigratorClientException;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;
import pt.gov.dgarq.roda.migrator.stubs.SynchronousConverter;
import pt.gov.dgarq.roda.plugins.converters.common.LocalRepresentationObject;
import pt.gov.dgarq.roda.util.StringUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * This is a {@link RepresentationConverterPlugin} that uses RODA Migrator
 * conversion services to convert representations.
 * 
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public abstract class AbstractRodaMigratorPlugin extends
		RepresentationConverterPlugin {

	private static Logger logger = Logger
			.getLogger(AbstractRodaMigratorPlugin.class);

	public static PluginParameter PARAMETER_PIDS() {
		return new PluginParameter("representationPIDs",
				PluginParameter.TYPE_STRING, null, false, true,
				"PIDs of representations to convert");
	}

	public static PluginParameter PARAMETER_CONVERTED_PIDS() {
		return new PluginParameter("convertedPIDs",
				PluginParameter.TYPE_STRING, null, false, true,
				"PIDs of representations already converted");
	}

	public static PluginParameter PARAMETER_MAKE_OBJECTS_ACTIVE() {
		return new PluginParameter("markActive", PluginParameter.TYPE_BOOLEAN,
				"true", true, true,
				"Mark creted objects as active in the end of migration");
	}

	public static PluginParameter PARAMETER_FAIL_AT_FIRST_ERROR() {
		return new PluginParameter("failAtFirstError",
				PluginParameter.TYPE_BOOLEAN, "false", true, true,
				"Plugin fails at first convertion error");
	}

	private RODAClient rodaClient = null;
	private Uploader rodaUploader = null;
	protected Downloader rodaDownloader = null;

	private MigratorClient migratorClient = null;

	private Browser browserService = null;
	private Ingest ingestService = null;

	private String converterServiceURL = null;

	private String[] representationSubTypes = null;

	/**
	 * Create a new {@link AbstractRodaMigratorPlugin}.
	 */
	public AbstractRodaMigratorPlugin() {
		super();
	}

	/**
	 * @see Plugin#init()
	 */
	public void init() throws PluginException {
		PropertiesConfiguration configuration = new PropertiesConfiguration();
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
					getConfigurationFile());

			logger.debug("Trying to load configuration file from " + configFile);

			if (configFile.isFile()) {
				configuration.load(configFile);
				logger.info("Loading configuration file from " + configFile);
			} else {
				configuration.load(getClass().getResourceAsStream(
						getConfigurationFile()));
				logger.info("Loading default configuration file from resources");
			}

			this.converterServiceURL = configuration
					.getString("converterServiceURL");

			this.representationSubTypes = configuration
					.getStringArray("representationSubTypes");
			if (this.representationSubTypes == null) {
				this.representationSubTypes = new String[0];
			}

		} catch (ConfigurationException e) {
			logger.debug(
					"Error reading plugin configuration - " + e.getMessage(), e);
			throw new PluginException("Error reading plugin configuration - "
					+ e.getMessage(), e);
		}
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {

		this.rodaClient = null;
		this.browserService = null;
		this.ingestService = null;
		this.rodaUploader = null;
	}

	/**
	 * @see RepresentationConverterPlugin#convert(RepresentationObject)
	 */
	@Override
	public ConversionResult convert(RepresentationObject rObject)
			throws RepresentationConverterException {

		try {

			SynchronousConverter converter = getConverter();

			if (converter == null) {

				logger.error("Error creating Migrator client - converter is null");
				throw new RepresentationConverterException(
						"Error creating Migrator client - converter is null");

			} else {
				return converter.convert(rObject);
			}

		} catch (MigratorClientException e) {
			logger.debug("Error creating Migrator client - " + e.getMessage(),
					e);
			throw new RepresentationConverterException(
					"Error creating Migrator client - " + e.getMessage(), e);
		} catch (RepresentationAlreadyConvertedException e) {
			logger.debug("Error converting representation - " + e.getMessage(),
					e);
			throw new RepresentationConverterException(
					"Error converting representation - " + e.getMessage(), e);
		} catch (ConverterException e) {
			logger.debug("Error converting representation - " + e.getMessage(),
					e);
			throw new RepresentationConverterException(
					"Error converting representation - " + e.getMessage(), e);
		} catch (RemoteException e) {
			logger.debug("Error converting representation - " + e.getMessage(),
					e);
			throw new RepresentationConverterException(
					"Error converting representation - " + e.getMessage(), e);
		}
	}

	/**
	 * @see RepresentationConverterPlugin#isRepresentationConverted(RepresentationObject)
	 */
	@Override
	public boolean isRepresentationConverted(RepresentationObject object)
			throws RepresentationConverterException {
		// By default a representation is never converted.
		return false;
	}

	/**
	 * @see Plugin#getParameters()
	 */
	public List<PluginParameter> getParameters() {
		return Arrays.asList(PARAMETER_RODA_CORE_URL(),
				PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD(),
				PARAMETER_PIDS(), PARAMETER_CONVERTED_PIDS(),
				PARAMETER_MAKE_OBJECTS_ACTIVE());
	}

	/**
	 * @see Plugin#execute()
	 */
	public Report execute() throws PluginException {

		Report executionReport = new Report();
		executionReport.setType(Report.TYPE_PLUGIN_REPORT);
		executionReport.setTitle("Plugin " + getName() + " (version "
				+ getVersion() + ") execution report.");
		executionReport.addAttribute(new Attribute("Start datetime", DateParser
				.getIsoDate(new Date())));

		// Creates RODAClient, Uploader Browser and Ingest
		initClientServices();

		boolean terminated = false;
		String representationPID;
		try {

			representationPID = getNextRepresentationPID();
			terminated = representationPID == null;

			logger.info("next representation is " + representationPID
					+ ". terminated=" + terminated);

		} catch (RODAException e) {
			logger.debug(
					"Error getting next representation PID - " + e.getMessage(),
					e);

			executionReport
					.addAttribute(new Attribute("Error", e.getMessage()));
			executionReport.addAttribute(new Attribute("Finish datetime",
					DateParser.getIsoDate(new Date())));

			throw new PluginException(
					"Error getting next representation PID - " + e.getMessage(),
					e, executionReport);
		}

		while (!terminated) {

			try {

				ReportItem reportItem = executeOn(representationPID);

				logger.info("adding ReportItem for representation "
						+ representationPID + " " + reportItem);

				executionReport.addItem(reportItem);

			} catch (PluginException e) {

				logger.debug("Error converting representation "
						+ representationPID + " - " + e.getMessage(), e);

				if (e.getReportItem() != null) {

					ReportItem reportItem = e.getReportItem();

					reportItem.addAttribute(new Attribute("Error", e
							.getMessage()));

					reportItem.addAttribute(new Attribute("Finish datetime",
							DateParser.getIsoDate(new Date())));

					executionReport.addItem(e.getReportItem());
				}

				if (getParameterFailAtFirstError()) {

					throw new MigratorPluginException(
							"Error executing migration - " + e.getMessage(), e,
							executionReport);
				}

			}

			addConvertedRepresentationPID(representationPID);

			try {

				representationPID = getNextRepresentationPID();
				terminated = representationPID == null;

				logger.info("next representation is " + representationPID
						+ ". terminated=" + terminated);

			} catch (RODAException e) {
				logger.debug(
						"Error getting next representation PID - "
								+ e.getMessage(), e);

				executionReport.addAttribute(new Attribute("Error", e
						.getMessage()));
				executionReport.addAttribute(new Attribute("Finish datetime",
						DateParser.getIsoDate(new Date())));

				throw new PluginException(
						"Error getting next representation PID - "
								+ e.getMessage(), e, executionReport);
			}
		}

		executionReport.addAttribute(new Attribute("Finish datetime",
				DateParser.getIsoDate(new Date())));

		return executionReport;
	}

	protected abstract String getConfigurationFile();

	protected abstract String getRepresentationType();

	protected abstract boolean isNormalization();

	private SynchronousConverter getConverter() throws MigratorClientException {
		return new MigratorClient().getSynchronousConverterService(
				converterServiceURL, getParameterRodaServicesUsername(),
				getParameterRodaServicesPassword());
	}

	private List<String> getRepresentationSubtypes() {
		return Arrays.asList(representationSubTypes);
	}

	private ReportItem executeOn(String representationPID)
			throws PluginException {

		ReportItem reportItem = new ReportItem("Convertion of representation "
				+ representationPID);
		reportItem.addAttribute(new Attribute("start datetime", DateParser
				.getIsoDate(new Date())));

		RepresentationObject originalRObject = null;
		try {

			reportItem.addAttribute(new Attribute(
					"Download original representation - start datetime",
					DateParser.getIsoDate(new Date())));

			// Get original representation from RODA Core
			originalRObject = this.browserService
					.getRepresentationObject(representationPID);

			logger.info("Original representation is " + originalRObject);

			reportItem.addAttribute(new Attribute(
					"Download original representation - finnish datetime",
					DateParser.getIsoDate(new Date())));

		} catch (BrowserException e) {
			logger.debug("Error accessing representation " + representationPID
					+ " - " + e.getMessage(), e);
			throw new PluginException("Error accessing representation "
					+ representationPID + " - " + e.getMessage(), e, reportItem);
		} catch (NoSuchRODAObjectException e) {
			logger.debug("Error accessing representation " + representationPID
					+ " - " + e.getMessage(), e);
			throw new PluginException("Error accessing representation "
					+ representationPID + " - " + e.getMessage(), e, reportItem);
		} catch (RemoteException e) {
			logger.debug("Error accessing representation " + representationPID
					+ " - " + e.getMessage(), e);
			throw new PluginException("Error accessing representation "
					+ representationPID + " - " + e.getMessage(), e, reportItem);
		}

		// Download representation files and verify if the files are already
		// normalised

		if (isRepresentationConverted(originalRObject)) {
			// No need to call the convert plugin

			try {

				String normalizedROPID = this.ingestService
						.setDONormalizedRepresentation(
								originalRObject.getDescriptionObjectPID(),
								originalRObject.getPid());

				logger.info("Marked representation " + normalizedROPID //$NON-NLS-1$
						+ " as normalized"); //$NON-NLS-1$

				reportItem.addAttributes(new Attribute("Action",
						"Representation was marked as normalized"));

			} catch (NoSuchRODAObjectException e) {
				reportItem.addAttributes(new Attribute("Error",
						"Error setting representation status to normalized ("
								+ e.getMessage() + ")"));
				throw new PluginException(
						"Error setting representation status to normalized - "
								+ e.getMessage(), e, reportItem);
			} catch (IngestException e) {
				reportItem.addAttributes(new Attribute("Error",
						"Error setting representation status to normalized ("
								+ e.getMessage() + ")"));
				throw new PluginException(
						"Error setting representation status to normalized - "
								+ e.getMessage(), e, reportItem);
			} catch (RemoteException e) {
				RODAException exception = RODAClient.parseRemoteException(e);
				reportItem.addAttributes(new Attribute("Error",
						"Error setting representation status to normalized ("
								+ e.getMessage() + ")"));
				throw new PluginException(
						"Error setting representation status to normalized - "
								+ exception.getMessage(), exception, reportItem);
			}

			reportItem.addAttributes(new Attribute("finnish datetime",
					DateParser.getIsoDate(new Date())));

			return reportItem;

		} else {

			// Representation is not normalized and we need to call the
			// converter. Continue...

		}

		ConversionResult convertResult = null;
		RepresentationObject convertedRObject = null;
		try {

			reportItem.addAttribute(new Attribute(
					"Conversion - start datetime", DateParser
							.getIsoDate(new Date())));

			logger.info("Converting representation " + originalRObject.getPid());

			// Calling converter
			convertResult = convert(originalRObject);
			convertedRObject = convertResult.getRepresentation();

			logger.info("Convert " + originalRObject.getPid() + " finished");

			reportItem.addAttribute(new Attribute(
					"Conversion - finnish datetime", DateParser
							.getIsoDate(new Date())));
			reportItem.addAttribute(new Attribute(
					"Conversion - converted representation", convertResult
							.getRepresentation().toString()));

			if (convertResult.getMigrationEvent() == null) {

				logger.warn("Migration event is null");

			} else {

				logger.info("Conversion outcome is "
						+ convertResult.getMigrationEvent()
								.getOutcomeDetailExtension());

				logger.info("Converted representation is " + convertedRObject);

				reportItem.addAttribute(new Attribute(
						"Conversion - outcome details", convertResult
								.getMigrationEvent()
								.getOutcomeDetailExtension()));
			}

		} catch (RepresentationConverterException e) {
			logger.debug("Error converting representation " + representationPID
					+ " - " + e.getMessage(), e);
			e.setReportItem(reportItem);
			throw e;
		}

		RepresentationObject writtenRO = null;
		String roPID = null;
		try {

			// Create temporary directory
			File temporaryDirectory = TempDir
					.createUniqueTemporaryDirectory("rep");

			reportItem.addAttributes(new Attribute(
					"Download converted representation - temporary directory",
					temporaryDirectory.toString()), new Attribute(
					"Download converted representation - start datetime",
					DateParser.getIsoDate(new Date())));

			logger.info("Writting converted representation to "
					+ temporaryDirectory);

			// Write representation to temporary directory
			writtenRO = this.migratorClient.writeRepresentationObject(
					convertedRObject, temporaryDirectory);

			logger.info("Representation written");

			reportItem.addAttribute(new Attribute(
					"Download converted representation - finnish datetime",
					DateParser.getIsoDate(new Date())));

			reportItem.addAttribute(new Attribute("Ingest - start datetime",
					DateParser.getIsoDate(new Date())));

			logger.info("Ingesting converted representation");

			if (isNormalization()) {
				logger.info("This is a normalization process. Setting representation status to "
						+ RepresentationObject.STATUS_NORMALIZED);
				writtenRO
						.setStatuses(new String[] { RepresentationObject.STATUS_NORMALIZED });
			} else {
				logger.info("This is NOT a normalization process. Setting representation status to "
						+ RepresentationObject.STATUS_ALTERNATIVE);
				writtenRO
						.setStatuses(new String[] { RepresentationObject.STATUS_ALTERNATIVE });
			}

			// Ingest converted representation
			roPID = ingestRepresentation(writtenRO);

			logger.info("Representation ingested with PID " + roPID);

			reportItem.addAttribute(new Attribute("Ingest - finnish datetime",
					DateParser.getIsoDate(new Date())));
			reportItem.addAttribute(new Attribute(
					"Ingest - ingested representation PID", roPID));

		} catch (MigratorClientException e) {
			logger.debug(
					"Error downloading converted representation - "
							+ e.getMessage(), e);
			throw new PluginException(
					"Error downloading converted representation  - "
							+ e.getMessage(), e, reportItem);
		} catch (IOException e) {
			logger.debug(
					"Error downloading converted representation - "
							+ e.getMessage(), e);
			throw new PluginException(
					"Error downloading converted representation  - "
							+ e.getMessage(), e, reportItem);
		} catch (IngestException e) {
			logger.debug(
					"Error downloading converted representation - "
							+ e.getMessage(), e);
			throw new PluginException(
					"Error downloading converted representation  - "
							+ e.getMessage(), e, reportItem);
		}

		AgentPreservationObject agentPO = null;
		try {

			logger.info("Registering derivation event");

			reportItem.addAttribute(new Attribute(
					"Register event - start datetime", DateParser
							.getIsoDate(new Date())));

			// Getting converter Agent
			AgentPreservationObject migratorAgent = getConverter().getAgent();

			reportItem.addAttribute(new Attribute(
					"Register event - converter agent", migratorAgent
							.toString()));

			agentPO = new AgentPreservationObject();
			agentPO.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_MIGRATOR);
			agentPO.setAgentName(getName() + "/" + getVersion() + " - "
					+ migratorAgent.getAgentName());

			logger.info("Agent is " + agentPO);

			reportItem.addAttribute(new Attribute(
					"Register event - event agent", agentPO.toString()));

		} catch (Exception e) {
			// getConverter().getAgent();
			logger.debug("Error getting converter agent - " + e.getMessage(), e);

			// Delete roPID
			// delete(roPID);
			// try {
			// logger
			// .warn("Ingest of new representation failed. Removing created object "
			// + roPID);
			//
			// this.ingestService.removeObjects(new String[] { roPID });
			//
			// } catch (RemoteException e1) {
			// logger.warn("Error removing representation " + roPID + " - "
			// + e1.getMessage() + ". IGNORING", e1);
			// }

			throw new PluginException("Error getting converter agent - "
					+ e.getMessage(), e, reportItem);
		}

		try {

			EventPreservationObject eventPO = convertResult.getMigrationEvent();

			if (eventPO == null) {
				eventPO = new EventPreservationObject();
			}

			if (isNormalization()) {
				eventPO.setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_NORMALIZATION);
			} else {
				eventPO.setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_MIGRATION);
			}
			if (StringUtils.isBlank(eventPO.getOutcome())) {
				eventPO.setOutcome("success");
			}
			if (StringUtils.isBlank(eventPO.getOutcomeDetailNote())) {
				eventPO.setOutcomeDetailNote("Converter details");
			}
			if (StringUtils.isBlank(eventPO.getOutcomeDetailExtension())) {
				eventPO.setOutcomeDetailExtension("no details");
			}

			logger.info("Event is " + eventPO);

			// reportItem.addAttribute(new Attribute(
			// "Register event - event outcome details", eventPO
			// .getOutcomeDetailExtension()));

			logger.info("Calling registerDerivationEvent(...)");

			// Register derivation event
			String epoPID = this.ingestService.registerDerivationEvent(
					originalRObject.getPid(), roPID, eventPO, agentPO,
					getParameterMakeObjectsActive());

			logger.info("Event registration finnished. Derivation event is "
					+ epoPID);

			reportItem.addAttributes(new Attribute(
					"Register event - event PID", epoPID), new Attribute(
					"finnish datetime", DateParser.getIsoDate(new Date())));

			return reportItem;

		} catch (NoSuchRODAObjectException e) {
			// registerDerivationEvent(...)
			logger.debug(
					"Error registering convertion event - " + e.getMessage(), e);
			throw new PluginException("Error registering convertion event - "
					+ e.getMessage(), e, reportItem);
		} catch (IngestException e) {
			// registerDerivationEvent(...)
			logger.debug(
					"Error registering convertion event - " + e.getMessage(), e);
			throw new PluginException("Error registering convertion event - "
					+ e.getMessage(), e, reportItem);
		} catch (RemoteException e) {
			// registerDerivationEvent(...)
			logger.debug(
					"Error registering convertion event - " + e.getMessage(), e);
			throw new PluginException("Error registering convertion event - "
					+ e.getMessage(), e, reportItem);
		}

	}

	private String getNextRepresentationPID() throws RODAException {

		List<String> representationPIDs = getParameterRepresentationPIDs();

		if (representationPIDs == null || representationPIDs.size() == 0) {

			logger.info("parameter representationPIDs is " + representationPIDs
					+ ". Getting list of representations from Browser");

			// Get the list of PIDs from the Browser service.
			representationPIDs = getRepresentationPIDsFromBrowser(
					getRepresentationType(), getRepresentationSubtypes());

			setParameterRepresentationPIDs(representationPIDs);

		} else {
			// RepresentationPIDs are already calculated
		}

		List<String> convertedPIDs = getParameterConvertedRepresentationPIDs();

		ArrayList<String> remainingPIDs = new ArrayList<String>(
				representationPIDs);
		remainingPIDs.removeAll(convertedPIDs);

		logger.debug("List of all PIDs is " + representationPIDs);
		logger.debug("List of converted PIDs is " + convertedPIDs);
		logger.debug("List of remaining PIDs is " + remainingPIDs);

		String nextPID = null;
		if (remainingPIDs.size() > 0) {
			nextPID = remainingPIDs.get(0);
		} else {
			nextPID = null;
		}

		logger.debug("Next PID is " + nextPID);

		return nextPID;
	}

	private List<String> getRepresentationPIDsFromBrowser(String type,
			List<String> subTypes) throws RODAException {

		Filter filter = new Filter();
		if (!StringUtils.isBlank(type)) {

			filter.add(new SimpleFilterParameter("type", type));

			// No sense to use subType without type
			if (subTypes != null && subTypes.size() > 0) {

				String[] subTypesArray = subTypes.toArray(new String[subTypes
						.size()]);

				filter.add(new OneOfManyFilterParameter("subtype",
						subTypesArray));
			}
		}

		Sorter sorter = new Sorter();
		sorter.add(new SortParameter("pid", false));

		try {

			SimpleRepresentationObject[] simpleROs = this.browserService
					.getSimpleRepresentationObjects(new ContentAdapter(filter,
							sorter, null));
			List<String> pids = new ArrayList<String>();
			if (simpleROs != null) {
				for (SimpleRepresentationObject simpleRO : simpleROs) {
					pids.add(simpleRO.getPid());
				}
			}

			return pids;

		} catch (BrowserException e) {
			logger.debug(
					"Error getting representations with specified type and subType - "
							+ e.getMessage(), e);
			throw new BrowserException(
					"Error getting representations with specified type and subType - "
							+ e.getMessage(), e);
		} catch (RemoteException e) {
			RODAException rodaException = RODAClient.parseRemoteException(e);
			logger.debug(
					"Error getting representations with specified type and subType - "
							+ rodaException.getMessage(), rodaException);
			throw rodaException;
		}
	}

	private String ingestRepresentation(RepresentationObject rObject)
			throws IngestException {

		String roPID = null;
		try {

			roPID = this.ingestService.createRepresentationObject(rObject);
			rObject.setPid(roPID);

			logger.info("RepresentationObject created with PID " + roPID);

		} catch (NoSuchRODAObjectException e) {
			logger.debug(
					"Error creating representation object - " + e.getMessage(),
					e);
			throw new IngestException("Error creating representation object - "
					+ e.getMessage(), e);
		} catch (RemoteException e) {
			logger.debug(
					"Error creating representation object - " + e.getMessage(),
					e);
			throw new IngestException("Error creating representation object - "
					+ e.getMessage(), e);
		}

		try {

			// Upload root file
			this.rodaUploader.uploadRepresentationFile(roPID,
					rObject.getRootFile());

			logger.info("Root file " + rObject.getRootFile().getId()
					+ " of representation " + roPID + " uploaded successfully.");

			// Upload part files
			if (rObject.getPartFiles() != null) {

				for (RepresentationFile partFile : rObject.getPartFiles()) {

					this.rodaUploader.uploadRepresentationFile(roPID, partFile);

					logger.info("Part file " + partFile.getId()
							+ " of representation " + roPID
							+ " uploaded successfully.");
				}

			}

			return roPID;

		} catch (FileNotFoundException e) {
			logger.debug(
					"Error accessing representation file - " + e.getMessage(),
					e);

			try {
				logger.warn("Ingest of new representation failed. Removing created object "
						+ roPID);

				this.ingestService.removeObjects(new String[] { roPID });

			} catch (RemoteException e1) {
				logger.warn("Error removing representation " + roPID + " - "
						+ e1.getMessage() + ". IGNORING", e1);
			}

			throw new IngestException("Error accessing representation file - "
					+ e.getMessage(), e);
		} catch (UploadException e) {

			logger.debug(
					"Error uploading representation file - " + e.getMessage(),
					e);

			try {
				logger.warn("Ingest of new representation failed. Removing created object "
						+ roPID);

				this.ingestService.removeObjects(new String[] { roPID });

			} catch (RemoteException e1) {
				logger.warn("Error removing representation " + roPID + " - "
						+ e1.getMessage() + ". IGNORING", e1);
			}

			throw new IngestException("Error uploading representation file - "
					+ e.getMessage(), e);
		}

	}

	private void initClientServices() throws PluginException {
		try {

			this.rodaClient = new RODAClient(getParameterRodaServicesURL(),
					getParameterRodaServicesUsername(),
					getParameterRodaServicesPassword());
			this.rodaUploader = new Uploader(getParameterRodaServicesURL(),
					getParameterRodaServicesUsername(),
					getParameterRodaServicesPassword());
			this.rodaDownloader = new Downloader(getParameterRodaServicesURL(),
					getParameterRodaServicesUsername(),
					getParameterRodaServicesPassword());

		} catch (RODAClientException e) {
			logger.debug("Error creating RODA client - " + e.getMessage(), e);
			throw new PluginException("Error creating RODA client - "
					+ e.getMessage(), e);
		} catch (LoginException e) {
			logger.debug("Error creating RODA client - " + e.getMessage(), e);
			throw new PluginException("Error creating RODA client - "
					+ e.getMessage(), e);
		} catch (MalformedURLException e) {
			logger.debug("Error creating RODA client - " + e.getMessage(), e);
			throw new PluginException("Error creating RODA client - "
					+ e.getMessage(), e);
		} catch (DownloaderException e) {
			logger.debug("Error creating RODA downloader - " + e.getMessage(),
					e);
			throw new PluginException("Error creating RODA downloader - "
					+ e.getMessage(), e);
		}

		try {

			this.browserService = this.rodaClient.getBrowserService();

		} catch (RODAClientException e) {
			logger.debug("Error accessing Browser service - " + e.getMessage(),
					e);
			throw new PluginException("Error accessing Browser service - "
					+ e.getMessage(), e);
		}

		try {

			this.ingestService = this.rodaClient.getIngestService();

		} catch (RODAClientException e) {
			logger.debug("Error accessing Ingest service - " + e.getMessage(),
					e);
			throw new PluginException("Error accessing Ingest service - "
					+ e.getMessage(), e);
		}

		this.migratorClient = new MigratorClient();
	}

	private URL getParameterRodaServicesURL() throws MalformedURLException {
		return new URL(getParameterValues().get(
				PARAMETER_RODA_CORE_URL().getName()));
	}

	private String getParameterRodaServicesUsername() {
		return getParameterValues().get(
				PARAMETER_RODA_CORE_USERNAME().getName());
	}

	private String getParameterRodaServicesPassword() {
		return getParameterValues().get(
				PARAMETER_RODA_CORE_PASSWORD().getName());
	}

	private boolean getParameterMakeObjectsActive() {
		return Boolean.parseBoolean(getParameterValues().get(
				PARAMETER_MAKE_OBJECTS_ACTIVE().getName()));
	}

	private boolean getParameterFailAtFirstError() {
		return Boolean.parseBoolean(getParameterValues().get(
				PARAMETER_FAIL_AT_FIRST_ERROR().getName()));
	}

	private List<String> getParameterRepresentationPIDs() {
		String pidsValue = getParameterValues().get(PARAMETER_PIDS().getName());

		String[] pids = null;

		if (!StringUtils.isBlank(pidsValue)) {
			pids = pidsValue.split(",\\s*");
		} else {
			pids = new String[0];
		}

		return Arrays.asList(pids);
	}

	private void setParameterRepresentationPIDs(List<String> pids) {

		logger.info("setting parameter " + PARAMETER_PIDS().getName() + " to "
				+ pids);

		getParameterValues().put(PARAMETER_PIDS().getName(),
				StringUtility.join(pids, ","));
	}

	private List<String> getParameterConvertedRepresentationPIDs() {
		String pidsValue = getParameterValues().get(
				PARAMETER_CONVERTED_PIDS().getName());

		String[] pids = null;

		if (!StringUtils.isBlank(pidsValue)) {
			pids = pidsValue.split(",\\s*");
		} else {
			pids = new String[0];
		}

		return Arrays.asList(pids);
	}

	private void setParameterConvertedRepresentationPIDs(List<String> pids) {

		logger.info("setting parameter " + PARAMETER_CONVERTED_PIDS().getName()
				+ " to " + pids);

		getParameterValues().put(PARAMETER_CONVERTED_PIDS().getName(),
				StringUtility.join(pids, ","));
	}

	private void addConvertedRepresentationPID(String representationPID) {

		logger.info("adding " + representationPID + " to parameter "
				+ PARAMETER_CONVERTED_PIDS().getName());

		ArrayList<String> convertedPIDs = new ArrayList<String>(
				getParameterConvertedRepresentationPIDs());

		convertedPIDs.add(representationPID);

		setParameterConvertedRepresentationPIDs(convertedPIDs);
	}

	/**
	 * Get representation files and writes them in the local disk
	 * 
	 * @param representation
	 * @return
	 * @throws IOException
	 * @throws DownloaderException
	 */
	protected LocalRepresentationObject downloadRepresentationToLocalDisk(
			RepresentationObject representation) throws IOException,
			DownloaderException {

		File tempDirectory = TempDir.createUniqueDirectory("rodaSourceRep");

		logger.debug("Saving representation to " + tempDirectory);

		LocalRepresentationObject localRepresentation = new LocalRepresentationObject(
				tempDirectory, representation);

		RepresentationFile rootRepFile = representation.getRootFile();
		File rootFile = this.rodaDownloader.saveTo(representation.getPid(),
				rootRepFile.getId(), tempDirectory);
		localRepresentation.getRootFile().setAccessURL(
				rootFile.toURI().toURL().toString());

		logger.trace("File " + rootRepFile.getId() + " saved to " + rootFile);

		for (RepresentationFile partRepFile : localRepresentation
				.getPartFiles()) {

			File partFile = this.rodaDownloader.saveTo(
					localRepresentation.getPid(), partRepFile.getId(),
					tempDirectory);

			partRepFile.setAccessURL(partFile.toURI().toURL().toString());

			logger.trace("File " + partRepFile.getId() + " saved to "
					+ partFile);
		}

		return localRepresentation;
	}

}
