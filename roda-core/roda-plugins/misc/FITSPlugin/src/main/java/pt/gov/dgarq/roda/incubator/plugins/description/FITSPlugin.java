package pt.gov.dgarq.roda.incubator.plugins.description;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.Downloader;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.common.BrowserException;
import pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException;
import pt.gov.dgarq.roda.core.data.Attribute;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.ReportItem;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.data.SimpleRepresentationObject;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.DateRangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter;
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.core.stubs.Browser;
import pt.gov.dgarq.roda.servlet.cas.CASUtility;

public class FITSPlugin extends AbstractPlugin {
	private final static Logger LOGGER = Logger.getLogger(FITSPlugin.class);

	private Downloader rodaDownloader = null;
	private Browser browserService = null;
	private File fitsStateFile;
	private PropertiesConfiguration lastExecutionState;
	private Date lastPluginExecDate = null;
	private Date dateAfterRetrievingRepresentationsList;
	private String RODA_HOME;
	private String fits_bin;
	private String fits_output_dir;
	private final static String LAST_EXEC_DATE = "last_execution_date";
	private final static String CONFIGURATION_FILENAME = "FITSPlugin-misc.properties";

	public FITSPlugin() {
		super();
	}

	@Override
	public void init() throws PluginException {
		if (System.getProperty("roda.home") != null) {
			RODA_HOME = System.getProperty("roda.home");
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = System.getenv("RODA_HOME");
		} else {
			RODA_HOME = null;
		}
		if (StringUtils.isBlank(RODA_HOME)) {
			throw new PluginException("RODA_HOME enviroment variable is not set.");
		}

		File RODA_PLUGINS_CONFIG_DIRECTORY = new File(new File(RODA_HOME, "config"), "plugins");

		File configFile = new File(RODA_PLUGINS_CONFIG_DIRECTORY, CONFIGURATION_FILENAME);
		PropertiesConfiguration configuration = new PropertiesConfiguration();
		try {
			if (configFile.isFile()) {
				configuration.load(configFile);
				LOGGER.info("Loading configuration file from " + configFile);
			} else {
				configuration.load(getClass().getResourceAsStream(CONFIGURATION_FILENAME));
				LOGGER.info("Loading default configuration file from resources");
			}
		} catch (ConfigurationException ex) {
			LOGGER.debug("Error reading plugin configuration - " + ex.getMessage(), ex);
			throw new PluginException("Error reading plugin configuration - " + ex.getMessage(), ex);
		}
		fits_bin = configuration.getString("fits_bin");
		fits_output_dir = configuration.getString("fits_output_dir");
		LOGGER.debug("init() OK");
	}

	@Override
	public void shutdown() {

	}

	@Override
	public String getName() {
		return "Misc/FITS plugin";
	}

	@Override
	public float getVersion() {
		return 1.0f;
	}

	@Override
	public String getDescription() {
		return "Plugin that executes FITS tool against all newer representations";
	}

	@Override
	public List<PluginParameter> getParameters() {
		return Arrays.asList(PARAMETER_RODA_CORE_URL(), PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD(),
				PARAMETER_RODA_CAS_URL());
	}

	@Override
	public Report execute() throws PluginException {

		Report report = new Report();
		report.setType(Report.TYPE_PLUGIN_REPORT);
		report.setTitle("Report of plugin " + getName());
		report.setAttributes(new Attribute[] { new Attribute("Agent name", getName()),
				new Attribute("Agent version", Float.toString(getVersion())),
				new Attribute("Start datetime", DateParser.getIsoDate(new Date())) });
		try {
			// initialize RODA services and load state information
			initRODAServicesAndCreateStateInfo();

		} catch (PluginException e) {
			LOGGER.debug("Error in initRODAServices - " + e.getMessage(), e);

			report.addAttribute(new Attribute("Error", e.getMessage()));
			report.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));

			e.setReport(report);
			throw e;
		}

		File fits_out_dir = new File(fits_output_dir);
		if (!fits_out_dir.exists() && !fits_out_dir.mkdir()) {
			report.addAttribute(new Attribute("Outcome", "error"));
			report.addAttribute(new Attribute("Error", "Error creating directory \"" + fits_out_dir + "\""));

			LOGGER.error("	Error creating directory \"" + fits_out_dir + "\"");
			throw new PluginException("Error creating directory \"" + fits_out_dir + "\"", null, report);
		}

		// execute fits and save state information
		processRepresentationsWithFITS(fits_output_dir, fits_bin,
				getParameterValues().get(PARAMETER_RODA_CORE_URL().getName()), report);

		try {

			// save finish date (state) to a properties file
			lastExecutionState.clearProperty(LAST_EXEC_DATE);
			lastExecutionState.addProperty(LAST_EXEC_DATE,
					DateParser.getIsoDate(dateAfterRetrievingRepresentationsList));
			lastExecutionState.save(fitsStateFile);

		} catch (ConfigurationException e) {
			LOGGER.error("Saving finish date", e);

			report.addAttribute(new Attribute("Error", e.getMessage()));
			report.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));

			throw new PluginException("Error while saving plugin state to a properties file", e, report);
		}

		// set report attributes in the end of the plugin execution
		report.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));

		return report;
	}

	private void initRODAServicesAndCreateStateInfo() throws PluginException {
		try {
			// instantiate all the RODA Core objects
			String rodaClientServiceUrl = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
			String rodaClientUsername = getParameterValues()
					.get(AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
			String rodaClientPassword = getParameterValues()
					.get(AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());
			String casURL = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CAS_URL().getName());
			String coreURL = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());

			CASUtility casUtility = new CASUtility(new URL(casURL), new URL(coreURL));

			RODAClient rodaClient = new RODAClient(new URL(rodaClientServiceUrl), rodaClientUsername,
					rodaClientPassword, casUtility);
			rodaDownloader = rodaClient.getDownloader();
			browserService = rodaClient.getBrowserService();

			// verify if a properties file containing the state of the last
			// execution of the plugin exists
			fitsStateFile = new File(RODA_HOME, "data/fits.state");
			lastExecutionState = new PropertiesConfiguration();
			if (!fitsStateFile.exists()) {

				// if it doesn't exist, create and save it, setting
				// lastPluginExecDate to null
				LOGGER.info("FITS plugin state file doesn't exist. A new one will be created in " + fitsStateFile);
				try {

					lastExecutionState.save(fitsStateFile);
					lastPluginExecDate = null;
				} catch (ConfigurationException e) {
					LOGGER.debug(
							"Exception writting notifications state file - " + fitsStateFile + " - " + e.getMessage(),
							e);
					throw new PluginException(
							"Exception writting notifications state file - " + fitsStateFile + " - " + e.getMessage(),
							e);
				}

			} else {

				// if it does exists, load it and read lastPluginExecDate value
				try {
					lastExecutionState.load(fitsStateFile);
					String dateString = lastExecutionState.getString(LAST_EXEC_DATE, null);
					lastPluginExecDate = DateParser.parse(dateString);
				} catch (ConfigurationException e) {
					LOGGER.debug("Exception reading FITS plugin state file - " + fitsStateFile + " - " + e.getMessage(),
							e);
					throw new PluginException(
							"Exception reading FITS plugin state file - " + fitsStateFile + " - " + e.getMessage(), e);
				}
			}

		} catch (Exception e) {
			LOGGER.debug("Error creating RODA client services - " + e.getMessage(), e);
			throw new PluginException("Error creating RODA client services - " + e.getMessage(), e);
		}
	}

	private URL getRodaServicesURL() throws MalformedURLException {
		return new URL(getParameterValues().get(PARAMETER_RODA_CORE_URL().getName()));
	}

	private String getUsername() {
		return getParameterValues().get(PARAMETER_RODA_CORE_USERNAME().getName());
	}

	private String getPassword() {
		return getParameterValues().get(PARAMETER_RODA_CORE_PASSWORD().getName());
	}

	private URL getCasURL() throws MalformedURLException {
		return new URL(getParameterValues().get(PARAMETER_RODA_CAS_URL().getName()));
	}

	private URL getCoreURL() throws MalformedURLException {
		return new URL(getParameterValues().get(PARAMETER_RODA_CORE_URL().getName()));
	}

	public void processRepresentationsWithFITS(String outputDirectory, String fitsSh, String rodaCoreURL,
			Report report) {
		try {
			LOGGER.info("Getting simple representations...");
			// SimpleRepresentationObject[] rObjects = browserService
			// .getSimpleRepresentationObjects(null);

			// create a content adapter with a filter to obtain only the
			// representations that weren't already analyzed with FITS
			ContentAdapter adapter = new ContentAdapter();
			adapter.setFilter(new Filter(
					new DateRangeFilterParameter("createdDate", lastPluginExecDate, null) ));
			SimpleRepresentationObject[] sros = browserService.getSimpleRepresentationObjects(adapter);
			dateAfterRetrievingRepresentationsList = new Date();

			// process each representation
			for (SimpleRepresentationObject sro : sros) {
				processSimpleRepresentationObjectWithFITS(sro, outputDirectory, fitsSh, rodaCoreURL, report);
			}

		} catch (BrowserException e) {
			LOGGER.error(e);
		} catch (RemoteException e) {
			LOGGER.error(e);
		}
	}

	private void processSimpleRepresentationObjectWithFITS(SimpleRepresentationObject sro, String outputDirectory,
			String fitsSh, String rodaCoreURL, Report report) {
		try {
			LOGGER.info("	Getting representation object (repPID=" + sro.getPid() + ") to execute FITS...");
			// create report item
			ReportItem reportItem = new ReportItem("Representation \"" + sro.getPid() + "\"");

			// grab representation object
			RepresentationObject representationObject = browserService.getRepresentationObject(sro.getPid());
			String roPID = representationObject.getPid();
			String newDirectory = outputDirectory + roPID;

			// determine if representation is original or not
			List<String> sroStatuses = Arrays.asList(sro.getStatuses());
			boolean isSroOriginal = sroStatuses.contains(SimpleRepresentationObject.STATUS_ORIGINAL);

			// create a new directory to contain fits output for this
			// representation
			File newDirectoryFile = new File(newDirectory);
			if (newDirectoryFile.exists() || newDirectoryFile.mkdir()) {
				// get root file
				LOGGER.debug("	Getting root file...");
				RepresentationFile rootFile = representationObject.getRootFile();

				// process the root file and store the result in the report
				String outcome = processRepresentationFileWithFITS(rootFile, roPID, outputDirectory, newDirectory,
						fitsSh, rodaCoreURL, isSroOriginal);
				if (outcome != null) {
					reportItem.addAttribute(new Attribute(rootFile.getId(), outcome));
				} else {
					reportItem.addAttribute(new Attribute(rootFile.getId(), "success"));
				}

				// get all the other files
				LOGGER.debug("	Getting part files...");
				RepresentationFile[] partFiles = representationObject.getPartFiles();

				// process all the other files and store the result in the
				// report
				for (RepresentationFile partFile : partFiles) {
					outcome = processRepresentationFileWithFITS(partFile, roPID, outputDirectory, newDirectory, fitsSh,
							rodaCoreURL, isSroOriginal);
					if (outcome != null) {
						reportItem.addAttribute(new Attribute(partFile.getId(), outcome));
					} else {
						reportItem.addAttribute(new Attribute(partFile.getId(), "success"));
					}
				}

				// // add general representation result
				// reportItem.addAttribute(new Attribute("Outcome", "success"));
				// reportItem.addAttribute(new Attribute("Action",
				// "Run FITS agains all files from representation with PID \""
				// + roPID + "\""));

				LOGGER.info("	Done!");
			} else {
				reportItem.addAttribute(new Attribute("Outcome", "error"));
				reportItem.addAttribute(new Attribute("Error", "Error creating directory \"" + newDirectory + "\""));

				LOGGER.error("	Error creating directory \"" + newDirectory + "\"");
			}
			report.addItem(reportItem);
		} catch (BrowserException e) {
			LOGGER.error(e);
		} catch (RemoteException e) {
			LOGGER.error(e);
		} catch (NoSuchRODAObjectException e) {
			LOGGER.error(e);
		}
	}

	private String processRepresentationFileWithFITS(RepresentationFile repFile, String pid, String outputDirectory,
			String newDirectory, String fitsSh, String rodaCoreURL, boolean isSroOriginal) {
		String executionResult = null;
		File tempFile = null;
		try {
			// grab file original name, access url and size
			String originalName = repFile.getOriginalName();
			String accessURL = repFile.getAccessURL();
			long size = repFile.getSize();
			if ("".equals(originalName) || originalName.length() < 3) {
				originalName = "_" + new Date().getTime() + "_";
			}

			// download file to a temporary location in order to execute FITS
			LOGGER.debug("		Downloading \"" + originalName + "\" (s=" + size + ",pid=" + pid + ")...");
			InputStream inputStream = rodaDownloader.get(accessURL);
			tempFile = File.createTempFile(originalName, null);
			IOUtils.copyLarge(inputStream, new FileOutputStream(tempFile));
			String fitsIntermediateFile = outputDirectory + pid + "/" + repFile.getId() + ".fits";

			// execute FITS
			String command = "bash " + fitsSh + " -i " + tempFile.getAbsolutePath() + " -o " + fitsIntermediateFile;
			LOGGER.debug("		Executing \"" + command + "\"");
			Process exec = Runtime.getRuntime().exec(command);
			exec.waitFor();
			int exitValue = exec.exitValue();
			if (exitValue == 0) {
				// if FITS was successfully executed, change FITS information to
				// comply with SCAPE needs
				executionResult = changeInfoInFITSOutput(fitsIntermediateFile,
						fitsIntermediateFile.replaceFirst("fits$", "xml"), rodaCoreURL + accessURL, isSroOriginal);
			} else {
				executionResult = "Error executing FITS script on file with ID \"" + repFile.getId() + "\"";
			}
			LOGGER.debug("		Done! (exitValue=" + exitValue + ")\n");
		} catch (RemoteException e) {
			LOGGER.error(e);
			executionResult = "Error processing representation file with PID \"" + pid + "\"";
		} catch (IOException e) {
			LOGGER.error(e);
			executionResult = "Error processing representation file with PID \"" + pid + "\"";
		} catch (DownloaderException e) {
			LOGGER.error(e);
			executionResult = "Error processing representation file with PID \"" + pid + "\"";
		} catch (InterruptedException e) {
			LOGGER.error(e);
			executionResult = "Error processing representation file with PID \"" + pid + "\"";
		} finally {
			if (tempFile != null && !tempFile.delete() && executionResult == null) {
				executionResult = "Error deleting file \"" + tempFile + "\"";
			}
		}
		return executionResult;
	}

	private String changeInfoInFITSOutput(String inFile, String outFile, String accessURL, boolean isSroOriginal) {
		LOGGER.debug("		Processing file \"" + inFile + "\" to substitute the filepath element content with \""
				+ accessURL + "\" and storing it in file \"" + outFile
				+ "\" (also adding element //representationinfo/original)");
		String executionResult = null;
		File fitsInFile = new File(inFile);
		File fitsOutFile = new File(outFile);
		boolean insideFileInfo = false;
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fitsOutFile));

			LineIterator lineIterator = IOUtils.lineIterator(new FileInputStream(fitsInFile), null);
			while (lineIterator.hasNext()) {
				String nextLine = lineIterator.nextLine();
				if (nextLine.matches("^.*<fileinfo>.*$")) {
					insideFileInfo = true;
					bos.write((nextLine + "\n").getBytes());
				} else if (nextLine.matches("^.*<filepath [^>]+>.*$") && insideFileInfo) {
					nextLine = nextLine.replaceFirst("^(.*<filepath [^>]+>)[^<]+", "$1" + accessURL);
					bos.write((nextLine + "\n").getBytes());
					insideFileInfo = false;
				} else if (nextLine.matches("^.*</fileinfo>.*$")) {
					bos.write((nextLine + "\n<representationinfo>\n<original toolname=\"RODA\" toolversion=\"1.0.0\">"
							+ isSroOriginal + "</original>\n</representationinfo>\n").getBytes());
				} else {
					bos.write((nextLine + "\n").getBytes());
				}
			}
			bos.close();
			fitsInFile.delete();
		} catch (FileNotFoundException e) {
			LOGGER.error(e);
			executionResult = "An error occurred while changing FITS output";
		} catch (IOException e) {
			LOGGER.error(e);
			executionResult = "An error occurred while changing FITS output";
		}
		return executionResult;
	}

	public static void main(String[] args) throws ParseException, InvalidDateException {
		String date = "2012-08-14T15:27:39.20Z";
		// Date parse = DateFormat.getInstance().parse(date);
		Date parse = DateParser.parse(date);
		System.out.println(parse.toString());
	}
}
