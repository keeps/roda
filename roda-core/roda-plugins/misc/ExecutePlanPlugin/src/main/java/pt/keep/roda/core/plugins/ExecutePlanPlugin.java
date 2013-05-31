package pt.keep.roda.core.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.util.DateParser;
import org.xml.sax.SAXException;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.Downloader;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.RODAClient;
import pt.gov.dgarq.roda.core.UploadException;
import pt.gov.dgarq.roda.core.Uploader;
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
import pt.gov.dgarq.roda.core.plugins.AbstractPlugin;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.core.plugins.PluginException;
import pt.gov.dgarq.roda.ingest.siputility.builders.RepresentationBuilder;
import pt.gov.dgarq.roda.plugins.converters.common.LocalRepresentationObject;
import pt.gov.dgarq.roda.util.CommandException;
import pt.gov.dgarq.roda.util.CommandUtility;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Rui Castro <rcastro@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ExecutePlanPlugin extends AbstractPlugin {
  private static Logger logger = Logger.getLogger(ExecutePlanPlugin.class);
  private String RODA_HOME = null;
  private File planFile = null;

  private RODAClient rodaClient = null;
  private Uploader rodaUploader = null;
  private Downloader rodaDownloader = null;

  private static String CONFIGURATION_FILENAME = "ExecutePlan-misc.properties";
  private static String PLAN_FILENAME = "plan.xml";
  private static String WORKFLOW_FILENAME = "workflow.t2flow";
  /**
   * The path to the taverna command.
   */
  private String taverna_bin;
  /**
   * The XPath expression to select the file IDs from a plan.
   */
  private String xpathSelectIDs = null;
  /**
   * The XPath expression to select the workflow from a plan.
   */
  private String xpathSelectWorkflow = null;
  /**
   * The name of the workflow input port.
   */
  private String workflowInputPort = null;
  /**
   * The name of the workflow output port.
   */
  private String workflowOutputPort = null;
  /**
   * The names of the extra ports from the workflow.
   */
  private String[] workflowExtraPorts = null;

  /**
   * Constructs a new {@link ExecutePlanPlugin}.
   */
  public ExecutePlanPlugin() {
    super();
  }

  /**
   * @see Plugin#init()
   */
  public void init() throws PluginException {
    try {
      if (System.getProperty("roda.home") != null) {
        RODA_HOME = System.getProperty("roda.home");
      } else if (System.getenv("RODA_HOME") != null) {
        RODA_HOME = System.getenv("RODA_HOME");
      } else {
        RODA_HOME = null;
      }
      if (StringUtils.isBlank(RODA_HOME)) {
        throw new PluginException("RODA_HOME enviroment variable and ${roda.home} system property are not set.");
      }
      final File pluginsConfigDirectory = new File(new File(RODA_HOME, "config"), "plugins");

      final File configFile = new File(pluginsConfigDirectory, CONFIGURATION_FILENAME);
      final PropertiesConfiguration configuration = new PropertiesConfiguration();

      if (configFile.isFile()) {
        configuration.load(configFile);
        logger.info("Loading configuration file from " + configFile);
      } else {
        configuration.load(getClass().getResourceAsStream(CONFIGURATION_FILENAME));
        logger.info("Loading default configuration file from resources");
      }

      taverna_bin = configuration.getString("taverna_bin");
      logger.debug("taverna_bin=" + taverna_bin);

      xpathSelectIDs = configuration.getString("xpathSelectIDs");
      logger.debug("xpathSelectIDs=" + xpathSelectIDs);

      xpathSelectWorkflow = configuration.getString("xpathSelectWorkflow");
      logger.debug("xpathSelectWorkflow=" + xpathSelectWorkflow);

      workflowInputPort = configuration.getString("workflowInputPort");
      logger.debug("workflowInputPort=" + workflowInputPort);
      
      workflowOutputPort = configuration.getString("workflowOutputPort");
      logger.debug("workflowOutputPort=" + workflowOutputPort);

      workflowExtraPorts = configuration.getStringArray("workflowExtraPorts");
      logger.debug("workflowExtraPorts=" + Arrays.asList(workflowExtraPorts));

    } catch (ConfigurationException ex) {
      logger.debug("Error reading plugin configuration - " + ex.getMessage(), ex);
      throw new PluginException("Error reading plugin configuration - " + ex.getMessage(), ex);
    }

    planFile = new File(new File(RODA_HOME, "data"), PLAN_FILENAME);

    logger.debug("init() OK");
  }

  /**
   * Initialises the RODA client services.
   * 
   * @throws PluginException
   *           if an error occurred during initialisation
   */
  private void initClientServices() throws PluginException {

    final String rodaClientServiceUrl = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CORE_URL().getName());
    final String rodaClientUsername = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CORE_USERNAME().getName());
    final String rodaClientPassword = getParameterValues().get(AbstractPlugin.PARAMETER_RODA_CORE_PASSWORD().getName());

    try {

      rodaClient = new RODAClient(new URL(rodaClientServiceUrl), rodaClientUsername, rodaClientPassword);
      rodaUploader = new Uploader(new URL(rodaClientServiceUrl), rodaClientUsername, rodaClientPassword);
      rodaDownloader = rodaClient.getDownloader();

    } catch (RODAClientException e) {
      logger.debug("Exception creating RODA Client - " + e.getMessage(), e);
      throw new PluginException("Exception creating RODA Client - " + e.getMessage(), e);
    } catch (LoginException e) {
      logger.debug("Exception creating RODA Client - " + e.getMessage(), e);
      throw new PluginException("Exception creating RODA Client - " + e.getMessage(), e);
    } catch (MalformedURLException e) {
      logger.debug("Exception creating service URL - " + e.getMessage(), e);
      throw new PluginException("Exception creating service URL - " + e.getMessage(), e);
    } catch (DownloaderException e) {
      logger.debug("Exception creating RODA downloader - " + e.getMessage(), e);
      throw new PluginException("Exception creating service URL - " + e.getMessage(), e);
    }
  }

  /**
   * @see Plugin#shutdown()
   */
  public void shutdown() {
    logger.debug("shutdown() OK");
  }

  /**
   * @return The name of the plugin.
   * @see Plugin#getName()
   */
  public String getName() {
    return "Preservation/Execute plan";
  }

  /**
   * @return The version of the plugin.
   * @see Plugin#getVersion()
   */
  public float getVersion() {
    return 1.0f;
  }

  /**
   * @return The description of the plugin.
   * @see Plugin#getDescription()
   */
  public String getDescription() {
    return "Plugin that executes a plan created by the planning tool.";
  }

  /**
   * @return a {@link List} of {@link Parameter}
   * @see Plugin#getParameters()
   */
  public List<PluginParameter> getParameters() {
    return Arrays.asList(PARAMETER_RODA_CORE_URL(), PARAMETER_RODA_CORE_USERNAME(), PARAMETER_RODA_CORE_PASSWORD());
  }

  /**
   * Executes the plugin.
   * 
   * @return The execution {@link Report}
   * @throws PluginException
   *           if an error occurred during execution.
   * @see Plugin#execute()
   */
  public Report execute() throws PluginException {

    initClientServices();

    final Report report = new Report();
    report.setType(Report.TYPE_PLUGIN_REPORT);
    report.setTitle("Report of plugin " + getName());
    report.setAttributes(new Attribute[] {new Attribute("Agent name", getName()),
      new Attribute("Agent version", Float.toString(getVersion())),
      new Attribute("Start datetime", DateParser.getIsoDate(new Date()))});

    try {

      final Document plan = getPlanDocument(planFile);

      final List<String> fileURLs = getFileURLsFromPlan(plan);
      logger.debug("File URLs from plan: " + fileURLs);

      report.addAttribute(new Attribute("Number of files", new Integer(fileURLs.size()).toString()));

      final Map<String, List<String>> representationFiles = groupFileIDsByRepresentation(fileURLs);
      logger.debug("representationFiles: " + representationFiles);

      report
        .addAttribute(new Attribute("Number of representations", new Integer(representationFiles.size()).toString()));

      final File workflowFile = getWorkflowFile(plan);
      logger.debug("Workflow file: " + workflowFile);

      executeWorkflowInAllRepresentations(workflowFile, representationFiles, report);

      report.addAttribute(new Attribute("Successful", "yes"));
      report.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));

      // Report for the execution of this Plugin
      return report;

    } catch (PluginException e) {
      logger.error("Error executing plugin - " + e.getMessage(), e);
      logger.info("Setting report in exception and re-throwing");

      report.addAttribute(new Attribute("Successful", "no"));
      report.addAttribute(new Attribute("Error", e.getMessage()));
      report.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));

      e.setReport(report);

      throw e;
    }

  }

  /**
   * Parses the plan XML file and returns a {@link Document} with the plan.
   * 
   * @param planFile
   *          the File with the plan.
   * @return the {@link Document} containing the plan.
   * @throws PluginException
   *           if an error occurs reading, parsing or creating the
   *           {@link Document} from the plan XML file.
   */
  private Document getPlanDocument(final File planFile) throws PluginException {

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    try {

      final DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(planFile);

    } catch (ParserConfigurationException e) {
      logger.error("Error creating Document - " + e.getMessage());
      throw new PluginException("Error creating Document - " + e.getMessage(), e);
    } catch (SAXException e) {
      logger.error("Error parsing plan XML file - " + e.getMessage());
      throw new PluginException("Error parsing plan XML file - " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("Error reading plan XML file - " + e.getMessage());
      throw new PluginException("Error reading plan XML file - " + e.getMessage(), e);
    }
  }

  /**
   * Gets the URLs of files this plan applies to.
   * 
   * @param plan
   *          the plan
   * @return a {@link List} of file URLs.
   * @throws PluginException
   *           if an error occurred extracting URLs from the plan.
   */
  private List<String> getFileURLsFromPlan(final Document plan) throws PluginException {

    final List<String> fileURLs = new ArrayList<String>();
    final XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(new PlanNamespaceContext());

    try {

      final XPathExpression expr = xpath.compile(xpathSelectIDs);
      final NodeList nl = (NodeList) expr.evaluate(plan, XPathConstants.NODESET);

      for (int i = 0; i < nl.getLength(); i++) {
        final Node item = nl.item(i);
        fileURLs.add(item.getNodeValue());
      }

      return fileURLs;

    } catch (XPathExpressionException e) {
      logger.error("Error reading file URLs from plan - " + e.getMessage());
      throw new PluginException("Error reading file URLs from plan - " + e.getMessage(), e);
    }
  }

  /**
   * Groups file IDs by representation.
   * 
   * @param fileURLs
   *          a {@link List} of file URLs.
   * @return a {@link Map} from representation IDs to their respective file IDs.
   */
  private Map<String, List<String>> groupFileIDsByRepresentation(final List<String> fileURLs) {

    final Map<String, List<String>> map = new HashMap<String, List<String>>();

    if (fileURLs != null) {
      for (String s : fileURLs) {

        final int lastIndexOf = s.lastIndexOf("get/roda:");
        // 4 = "get/".length();
        final String representationPlusFile = s.substring(lastIndexOf + 4);
        final int slashIndex = representationPlusFile.indexOf('/');
        final String representation = representationPlusFile.substring(0, slashIndex);
        final String file = representationPlusFile.substring(slashIndex + 1);

        if (!map.containsKey(representation)) {
          map.put(representation, new ArrayList<String>());
        }

        map.get(representation).add(file);

      }
    }

    return map;
  }

  /**
   * Extracts the workflow from the plan.
   * 
   * @param plan
   *          the plan to extract the workflow from.
   * @return The workflow File
   * @throws PluginException
   *           if an error occurs extracting the workflow from the plan.
   */
  private File getWorkflowFile(final Document plan) throws PluginException {

    final XPathFactory xPathfactory = XPathFactory.newInstance();
    final XPath xpath = xPathfactory.newXPath();

    Writer writer = null;
    try {

      xpath.setNamespaceContext(new PlanNamespaceContext());
      final XPathExpression expr = xpath.compile(xpathSelectWorkflow);
      final Element elementWorkflow = (Element) expr.evaluate(plan, XPathConstants.NODE);

      // Prepare the DOM document for writing
      final Source source = new DOMSource(elementWorkflow);
      final File workflowFile = new File(new File(RODA_HOME, "data"), WORKFLOW_FILENAME);
      writer = new OutputStreamWriter(new FileOutputStream(workflowFile));
      final Result result = new StreamResult(writer);

      // Write the DOM document to the file
      final Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.transform(source, result);

      writer.close();

      return workflowFile;

    } catch (XPathExpressionException e) {
      logger.error("Error compiling XPATH expression or evaluating the plan - " + e.getMessage());
      throw new PluginException("Error compiling XPATH expression or evaluating the plan - " + e.getMessage(), e);
    } catch (FileNotFoundException e) {
      logger.error("Error opening output file for writting - " + e.getMessage());
      throw new PluginException("Error opening output file for writting - " + e.getMessage(), e);
    } catch (TransformerFactoryConfigurationError e) {
      logger.error("Error writing workflow file - " + e.getMessage());
      throw new PluginException("Error writing workflow file - " + e.getMessage(), e);
    } catch (TransformerException e) {
      logger.error("Error writing workflow file - " + e.getMessage());
      throw new PluginException("Error writing workflow file - " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("Error writing workflow file - " + e.getMessage());
      throw new PluginException("Error writing workflow file - " + e.getMessage(), e);
    } finally {

      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          logger.error("Error closing output writer - " + e.getMessage());
        }
      }

    }

  }

  /**
   * Executes the workflow against the files of all representations.
   * 
   * @param workflowFile
   *          the workflow File
   * @param mapOfRepresentationsAndFiles
   *          a {@link Map} of representation and their respective files
   * @param report
   *          the execution {@link Report}.
   */
  private void executeWorkflowInAllRepresentations(final File workflowFile,
    final Map<String, List<String>> mapOfRepresentationsAndFiles, final Report report) {

    for (String representation : mapOfRepresentationsAndFiles.keySet()) {

      final List<String> files = mapOfRepresentationsAndFiles.get(representation);

      ReportItem reportItem = executeWorkflowInRepresentation(workflowFile, representation, files);
      report.addItem(reportItem);

    }

  }

  /**
   * Executes the workflow for the given representation files.
   * 
   * @param workflowFile
   *          the workflow File.
   * @param representationID
   *          the representation ID.
   * @param fileIDs
   *          the file IDs.
   * @return a {@link ReportItem} with details from the execution of the
   *         workflow in the representation.
   */
  private ReportItem executeWorkflowInRepresentation(final File workflowFile, final String representationID,
    final List<String> fileIDs) {

    final ReportItem reportItem = new ReportItem(representationID);
    reportItem.addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));

    int fileCount = 0;

    try {

      final Map<String, WorkflowExecutionOutput> outputs = new HashMap<String, ExecutePlanPlugin.WorkflowExecutionOutput>();
      for (String fileID : fileIDs) {
        outputs.put(fileID, executeWorkflowInFile(workflowFile, representationID, fileID));
        fileCount++;
      }

      // Create new representation with the outputs
      logger.warn("Need to create new representation from outputs " + outputs);

      String roPID = createDerivedRepresentation(representationID, outputs, reportItem);

      // remove temporary files
      for (String fileID : fileIDs) {

        final WorkflowExecutionOutput output = outputs.get(fileID);

        final File outputDirFile = new File(output.outputDir);

        if (FileUtils.deleteQuietly(outputDirFile)) {
          logger.debug("Deleted temporary directory " + outputDirFile);
        } else {
          logger.warn("Error deleting temporary directory " + outputDirFile);
        }
      }

      reportItem.addAttribute(new Attribute("Successful", "yes"));
      reportItem.addAttribute(new Attribute("Files successfuly processed", new Integer(fileCount).toString()));
      reportItem.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));

    } catch (ExecutePlanException e) {
      logger.error("Error executing workflow on files of representation " + representationID + " - " + e.getMessage());

      reportItem.addAttribute(new Attribute("Successful", "no"));
      reportItem.addAttribute(new Attribute("Files successfuly processed", new Integer(fileCount).toString()));
      reportItem.addAttribute(new Attribute("Error", e.getMessage()));
      reportItem.addAttribute(new Attribute("Finish datetime", DateParser.getIsoDate(new Date())));
    }

    return reportItem;
  }

  private String createDerivedRepresentation(final String roPID, final Map<String, WorkflowExecutionOutput> outputs,
    final ReportItem reportItem) throws ExecutePlanException {

    logger.trace(String.format("createNewRepresentation(%s, %s)", roPID, outputs));

    RepresentationObject roOriginal = null;
    LocalRepresentationObject roLocalDerived = null;
    final StringBuilder sbExecDetails = new StringBuilder();

    try {

      roOriginal = rodaClient.getBrowserService().getRepresentationObject(roPID);

      roLocalDerived = downloadRepresentationToLocalDisk(roOriginal);
      roLocalDerived.setId(DateParser.getIsoDate(new Date()));
      roLocalDerived.setStatuses(new String[] {RepresentationObject.STATUS_NORMALIZED});

      sbExecDetails.append(String.format("<planExecutionDetails plan=\"%s\">%n", planFile.getName()));

      // Check if root file was changed
      if (outputs.containsKey(roLocalDerived.getRootFile().getId())) {
        final WorkflowExecutionOutput output = outputs.get(roLocalDerived.getRootFile().getId());
        updateFile(roLocalDerived.getRootFile(), output);
        sbExecDetails.append(getPlanExecutionDetailsForFile(roLocalDerived.getRootFile().getId(), output));
      }

      if (roLocalDerived.getPartFiles() != null) {
        for (RepresentationFile rFile : roLocalDerived.getPartFiles()) {
          if (outputs.containsKey(rFile.getId())) {
            final WorkflowExecutionOutput output = outputs.get(rFile.getId());
            updateFile(rFile, output);
            sbExecDetails.append(getPlanExecutionDetailsForFile(rFile.getId(), output));
          }
        }
      }

      sbExecDetails.append(String.format("</planExecutionDetails>%n"));

      roLocalDerived.setType(RepresentationObject.DIGITALIZED_WORK);
      final String subtype = RepresentationBuilder.getRepresentationSubtype(roLocalDerived);
      roLocalDerived.setSubType(subtype);

    } catch (RODAException e) {
      deleteTemporaryLocalRepresentation(roLocalDerived);
      logger.error(e.getMessage(), e);
      throw new ExecutePlanException(e.getMessage(), e);
    } catch (RemoteException e) {
      deleteTemporaryLocalRepresentation(roLocalDerived);
      logger.error(e.getMessage(), e);
      throw new ExecutePlanException(e.getMessage(), e);
    } catch (IOException e) {
      deleteTemporaryLocalRepresentation(roLocalDerived);
      logger.error(e.getMessage(), e);
      throw new ExecutePlanException(e.getMessage(), e);
    }

    String derivedROPID = null;
    try {

      derivedROPID = ingestRepresentation(roLocalDerived);
      reportItem.addAttribute(new Attribute("Derived representation PID", derivedROPID));

    } catch (IngestException e) {

      logger.error("Error ingesting new representation - " + e.getMessage(), e);
      throw new ExecutePlanException("Error ingesting new representation - " + e.getMessage(), e);

    } finally {

      deleteTemporaryLocalRepresentation(roLocalDerived);

    }

    try {

      final String epoPID = createPreservationEvent(roOriginal.getPid(), derivedROPID, sbExecDetails.toString(),
        reportItem);
      reportItem.addAttribute(new Attribute("Derivation event PID", epoPID));

    } catch (ExecutePlanException e) {
      logger.debug("Error registering convertion event - " + e.getMessage(), e);

      try {
        logger.warn("Error registering convertion event. Removing created object " + derivedROPID);

        this.rodaClient.getIngestService().removeObjects(new String[] {derivedROPID});

      } catch (RODAClientException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      } catch (RemoteException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      } catch (IngestException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      }

      throw new ExecutePlanException("Error registering convertion event - " + e.getMessage(), e, reportItem);
    }

    return derivedROPID;
  }

  /**
   * Delete temporary {@link LocalRepresentationObject}.
   * 
   * @param localRep
   *          the {@link LocalRepresentationObject} to delete.
   */
  private void deleteTemporaryLocalRepresentation(final LocalRepresentationObject localRep) {
    if (localRep != null && localRep.getDirectory() != null) {
      if (FileUtils.deleteQuietly(localRep.getDirectory())) {
        logger.debug("Deleted representation temporary directory " + localRep.getDirectory());
      } else {
        logger.warn("Error deleting representation temporary directory " + localRep.getDirectory());
      }
    } else {
      logger.debug("localRep is null or localRep.getDirectory is null.");
    }
  }

  private String getPlanExecutionDetailsForFile(final String fileID, final WorkflowExecutionOutput output) {

    final StringBuilder sBuilder = new StringBuilder();
    sBuilder.append(String.format("<file id=\"%s\">%n", fileID));

    if (workflowExtraPorts != null) {

      for (String port : workflowExtraPorts) {

        final File portFile = new File(output.outputDir, port);

        if (portFile.exists()) {
          try {

            final FileInputStream fisPort = new FileInputStream(portFile);
            final String portContent = IOUtils.toString(fisPort);

            sBuilder.append(String.format("<qa property=\"%s\">%s</qa>%n", port, portContent));

            fisPort.close();

          } catch (IOException e) {
            logger.error("Error reading workflow port file '" + portFile + "' - " + e.getMessage(), e);
          }
        }

      }

    }

    sBuilder.append(String.format("</file>%n"));
    return sBuilder.toString();
  }

  /**
   * Create a preservation event the plan execution task.
   * 
   * @param originalROPID
   *          the PID of the original representation.
   * @param roPID
   *          the new representation PID.
   * @param planExecutionDetails
   *          The plan execution details.
   * @param reportItem
   *          the {@link ReportItem}.
   * @return the PID of the created {@link EventPreservationObject}.
   * @throws ExecutePlanException
   *           if an error occurs creating the {@link EventPreservationObject}.
   */
  private String createPreservationEvent(final String originalROPID, final String roPID,
    final String planExecutionDetails, final ReportItem reportItem) throws ExecutePlanException {

    // Create agent
    final AgentPreservationObject agentPO = new AgentPreservationObject();
    agentPO.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_MIGRATOR);
    agentPO.setAgentName(getName() + "/" + getVersion() + " - SCAPE plan");

    try {

      final EventPreservationObject eventPO = new EventPreservationObject();

      eventPO.setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_MIGRATION);
      eventPO.setOutcome("success");
      eventPO.setOutcomeDetailNote("Plan workflow output");
      eventPO.setOutcomeDetailExtension(planExecutionDetails);

      logger.debug("Event is " + eventPO);

      // Register derivation event
      final String epoPID = this.rodaClient.getIngestService().registerDerivationEvent(originalROPID, roPID, eventPO,
        agentPO, true);

      logger.info("Event registration finnished. Derivation event is " + epoPID);

      reportItem.addAttribute(new Attribute("Register event - event PID", epoPID));

      return epoPID;

    } catch (NoSuchRODAObjectException e) {
      logger.debug("Error registering convertion event - " + e.getMessage(), e);
      throw new ExecutePlanException("Error registering convertion event - " + e.getMessage(), e, reportItem);
    } catch (IngestException e) {
      logger.debug("Error registering convertion event - " + e.getMessage(), e);
      throw new ExecutePlanException("Error registering convertion event - " + e.getMessage(), e, reportItem);
    } catch (RemoteException e) {
      logger.debug("Error registering convertion event - " + e.getMessage(), e);
      throw new ExecutePlanException("Error registering convertion event - " + e.getMessage(), e, reportItem);
    } catch (RODAClientException e) {
      logger.debug("Error registering convertion event - " + e.getMessage(), e);
      throw new ExecutePlanException("Error registering convertion event - " + e.getMessage(), e, reportItem);
    }

  }

  /**
   * 
   * @param rFile
   * @param fileOutput
   * @return
   * @throws ExecutePlanException
   */
  private RepresentationFile updateFile(final RepresentationFile rFile, final WorkflowExecutionOutput fileOutput)
    throws ExecutePlanException {

    final File outputPortFile = new File(fileOutput.outputDir, workflowOutputPort);
    if (!outputPortFile.exists()) {
      throw new ExecutePlanException("Output port '" + workflowOutputPort + "' for file " + rFile.getId()
        + " doesn't exist");
    }

    try {
      logger.debug("Reading output file from output port file " + outputPortFile);

      final FileInputStream fisOutputPort = new FileInputStream(outputPortFile);
      final String outputFilepath = IOUtils.toString(fisOutputPort);
      fisOutputPort.close();

      logger.debug("Output file is " + outputFilepath);

      final File outputFile = new File(outputFilepath);
      if (!outputFile.exists()) {
        throw new ExecutePlanException("Output file '" + outputFile + " doesn't exist");
      }

      rFile.setOriginalName(outputFile.getName());
      rFile.setSize(outputFile.length());
      rFile.setMimetype(FormatUtility.getMimetype(outputFile));

      final File rFilepath = new File(new URI(rFile.getAccessURL()));

      if (rFilepath.delete()) {
        logger.debug("Deleted previous representation file " + rFilepath);
      }
      logger.debug("Moving output file " + outputFile + " to representation file " + rFilepath);
      FileUtils.moveFile(outputFile, rFilepath);

      logger.debug("Output representation file is " + rFile);

      return rFile;

    } catch (IOException e) {
      logger.error("Error copying file - " + e.getMessage(), e);
      throw new ExecutePlanException("Error copying file - " + e.getMessage(), e);
    } catch (URISyntaxException e) {
      logger.error("Error copying file - " + e.getMessage(), e);
      throw new ExecutePlanException("Error copying file - " + e.getMessage(), e);
    }
  }

  /**
   * Get representation files and writes them in the local disk.
   * 
   * @param representation
   * @return
   * @throws IOException
   * @throws DownloaderException
   */
  protected LocalRepresentationObject downloadRepresentationToLocalDisk(RepresentationObject representation)
    throws IOException, DownloaderException {

    final File tempDirectory = TempDir.createUniqueDirectory("rodaSourceRep");

    logger.debug("Saving representation to " + tempDirectory);

    final LocalRepresentationObject localRepresentation = new LocalRepresentationObject(tempDirectory, representation);

    final RepresentationFile rootRepFile = representation.getRootFile();
    final File rootFile = this.rodaDownloader.saveTo(representation.getPid(), rootRepFile.getId(), tempDirectory);
    localRepresentation.getRootFile().setAccessURL(rootFile.toURI().toURL().toString());

    logger.trace("File " + rootRepFile.getId() + " saved to " + rootFile);

    for (RepresentationFile partRepFile : localRepresentation.getPartFiles()) {

      final File partFile = this.rodaDownloader
        .saveTo(localRepresentation.getPid(), partRepFile.getId(), tempDirectory);

      partRepFile.setAccessURL(partFile.toURI().toURL().toString());

      logger.trace("File " + partRepFile.getId() + " saved to " + partFile);
    }

    return localRepresentation;
  }

  /**
   * Ingest {@link RepresentationObject} into RODA.
   * 
   * @param rObject
   *          the {@link RepresentationObject} to ingest.
   * @return the PID of the ingested representation.
   * @throws IngestException
   *           if an error occurs ingesting the representation.
   */
  private String ingestRepresentation(final RepresentationObject rObject) throws IngestException {

    String roPID = null;
    try {

      roPID = this.rodaClient.getIngestService().createRepresentationObject(rObject);
      rObject.setPid(roPID);

      logger.info("RepresentationObject created with PID " + roPID);

    } catch (NoSuchRODAObjectException e) {
      logger.debug("Error creating representation object - " + e.getMessage(), e);
      throw new IngestException("Error creating representation object - " + e.getMessage(), e);
    } catch (RemoteException e) {
      logger.debug("Error creating representation object - " + e.getMessage(), e);
      throw new IngestException("Error creating representation object - " + e.getMessage(), e);
    } catch (RODAClientException e) {
      logger.debug("Error creating representation object - " + e.getMessage(), e);
      throw new IngestException("Error creating representation object - " + e.getMessage(), e);
    }

    try {

      // Upload root file
      this.rodaUploader.uploadRepresentationFile(roPID, rObject.getRootFile());

      logger.info("Root file " + rObject.getRootFile().getId() + " of representation " + roPID
        + " uploaded successfully.");

      // Upload part files
      if (rObject.getPartFiles() != null) {

        for (RepresentationFile partFile : rObject.getPartFiles()) {

          this.rodaUploader.uploadRepresentationFile(roPID, partFile);

          logger.info("Part file " + partFile.getId() + " of representation " + roPID + " uploaded successfully.");
        }

      }

      return roPID;

    } catch (FileNotFoundException e) {
      logger.debug("Error accessing representation file - " + e.getMessage(), e);

      try {
        logger.warn("Ingest of new representation failed. Removing created object " + roPID);

        this.rodaClient.getIngestService().removeObjects(new String[] {roPID});

      } catch (RODAClientException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      } catch (RemoteException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      }

      throw new IngestException("Error accessing representation file - " + e.getMessage(), e);
    } catch (UploadException e) {

      logger.debug("Error uploading representation file - " + e.getMessage(), e);

      try {
        logger.warn("Ingest of new representation failed. Removing created object " + roPID);

        this.rodaClient.getIngestService().removeObjects(new String[] {roPID});

      } catch (RODAClientException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      } catch (RemoteException e1) {
        logger.warn("Error removing representation " + roPID + " - " + e1.getMessage() + ". IGNORING", e1);
      }

      throw new IngestException("Error uploading representation file - " + e.getMessage(), e);
    }

  }

  /**
   * Executes the given workflow in the specified representation file.
   * 
   * @param workflowFile
   *          the workflow {@link File}
   * @param representationID
   *          the representation ID
   * @param fileID
   *          the file ID
   * @return a WorkflowExecutionOutput with the output directory and command
   *         output.
   * @throws ExecutePlanException
   *           if an error occurs during the execution
   */
  private WorkflowExecutionOutput executeWorkflowInFile(final File workflowFile, final String representationID,
    final String fileID) throws ExecutePlanException {

    logger.info("Executing workflow in file " + representationID + "/" + fileID);

    File inputValueFile = null;
    try {

      // Write the downloaded stream to a temporary file in the disk
      final InputStream inputStream = rodaDownloader.get(representationID, fileID);
      inputValueFile = File.createTempFile(representationID + "_" + fileID + "_", "");
      IOUtils.copyLarge(inputStream, new FileOutputStream(inputValueFile));

      final String outputDir = inputValueFile.getAbsolutePath() + "_taverna/";

      final String executionOutput = CommandUtility.execute(taverna_bin, "-outputdir", outputDir, "-inputvalue",
        workflowInputPort, inputValueFile.getAbsolutePath(), workflowFile.getAbsolutePath());

      logger.info("Workflow executed with sucess!");
      logger.debug("Command output: " + executionOutput);

      return new WorkflowExecutionOutput(outputDir, executionOutput);

    } catch (NoSuchRODAObjectException e) {
      logger.error("Error getting file - " + e.getMessage());
      throw new ExecutePlanException("Error getting file " + fileID + " - " + e.getMessage(), e);
    } catch (DownloaderException e) {
      logger.error("Error getting file - " + e.getMessage());
      throw new ExecutePlanException("Error getting file " + fileID + " - " + e.getMessage(), e);
    } catch (IOException e) {
      logger.error("Error getting file - " + e.getMessage());
      throw new ExecutePlanException("Error getting file " + fileID + " - " + e.getMessage(), e);
    } catch (CommandException e) {
      logger.error("Error executing taverna workflow in file " + fileID + " - " + e.getMessage());
      logger.debug("Command output: " + e.getOutput());
      throw new ExecutePlanException("Error executing taverna workflow in file " + fileID + " - " + e.getMessage(), e);
    } finally {

      if (inputValueFile != null) {
        if (FileUtils.deleteQuietly(inputValueFile)) {
          logger.debug("Deleted temporary file " + inputValueFile);
        } else {
          logger.warn("Error deleting temporary file " + inputValueFile);
        }
      }

    }

  }

  /**
   * The plan XML {@link NamespaceContext}.
   * 
   * @author Rui Castro
   */
  class PlanNamespaceContext implements NamespaceContext {

    @Override
    public Iterator getPrefixes(final String namespaceURI) {
      return null;
    }

    @Override
    public String getPrefix(final String namespaceURI) {
      return null;
    }

    @Override
    public String getNamespaceURI(final String prefix) {
      String uri;
      if ("plato".equals(prefix)) {
        uri = "http://ifs.tuwien.ac.at/dp/plato";
      } else if ("t2flow".equals(prefix)) {
        uri = "http://taverna.sf.net/2008/xml/t2flow";
      } else {
        uri = null;
      }
      return uri;
    }
  }

  class WorkflowExecutionOutput {

    String outputDir;
    String commandOutput;

    WorkflowExecutionOutput(final String outputDir, final String commandOutput) {
      this.outputDir = outputDir;
      this.commandOutput = commandOutput;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(outputDir=" + outputDir + ", commandOutput=" + commandOutput + ")";
    }
  }

  /**
   * Thrown to indicate that an exception occurred during plan execution.
   * 
   * @author Rui Castro
   */
  public class ExecutePlanException extends PluginException {
    private static final long serialVersionUID = -818982899159700088L;

    /**
     * Constructs a new {@link ExecutePlanException}.
     */
    public ExecutePlanException() {
    }

    /**
     * Constructs a new {@link ExecutePlanException} with the given error
     * message.
     * 
     * @param message
     *          the error message.
     */
    public ExecutePlanException(final String message) {
      super(message);
    }

    /**
     * Constructs a new {@link ExecutePlanException} with the given cause
     * exception.
     * 
     * @param cause
     *          the cause exception.
     */
    public ExecutePlanException(final Throwable cause) {
      super(cause);
    }

    /**
     * Constructs a new {@link ExecutePlanException} with the given error
     * message and cause exception.
     * 
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     */
    public ExecutePlanException(final String message, final Throwable cause) {
      super(message, cause);
    }

    /**
     * Constructs a new {@link ExecutePlanException} with the given error
     * message and cause exception.
     * 
     * @param message
     *          the error message.
     * @param cause
     *          the cause exception.
     * @param reportItem
     *          the {@link ReportItem} so far.
     */
    public ExecutePlanException(final String message, final Throwable cause, final ReportItem reportItem) {
      super(message, cause, null, reportItem);
    }

  }

}
