/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.Attribute;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginUtils;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;

public class AntivirusPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AntivirusPlugin.class);

  private Map<String, String> parameters;

  private String antiVirusClassName;
  private AntiVirus antiVirus = null;

  @Override
  public void init() throws PluginException {
    antiVirusClassName = RodaCoreFactory.getRodaConfiguration().getString(
      "core.plugins.internal.virus_check.antiVirusClassname", "org.roda.core.plugins.plugins.antivirus.ClamAntiVirus");

    try {
      LOGGER.info("Loading antivirus class " + antiVirusClassName);
      setAntiVirus((AntiVirus) Class.forName(antiVirusClassName).newInstance());
      LOGGER.info("Using antivirus " + getAntiVirus().getClass().getName());
    } catch (ClassNotFoundException e) {
      LOGGER.warn("Antivirus class " + antiVirusClassName + " not found - " + e.getMessage());
    } catch (InstantiationException e) {
      // not possible to create a new instance of the class
      LOGGER.warn("Antivirus class " + antiVirusClassName + " instantiation exception - " + e.getMessage());
    } catch (IllegalAccessException e) {
      // not possible to create a new instance of the class
      LOGGER.warn("Antivirus class " + antiVirusClassName + " illegal access exception - " + e.getMessage());
    }

    if (getAntiVirus() == null) {
      setAntiVirus(new AVGAntiVirus());
      LOGGER.info("Using default antivirus " + getAntiVirus().getClass().getName());
    }

    LOGGER.info("init OK");
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Virus check";
  }

  @Override
  public String getDescription() {
    return "Verifies if a SIP is free of virus.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginUtils.createPluginReport(this);
    Path tempDirectory = null;
    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = new ReportItem("Checking " + aip.getId() + " for virus");
      reportItem.setItemId(aip.getId());
      reportItem.addAttribute(new Attribute("Agent name", getName()))
        .addAttribute(new Attribute("Agent version", getVersion()))
        .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));
      VirusCheckResult virusCheckResult = null;
      Exception exception = null;
      try {
        LOGGER.debug("Checking if AIP " + aip.getId() + " is clean of virus");
        tempDirectory = Files.createTempDirectory("temp");
        StorageService tempStorage = new FileStorageService(tempDirectory);
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        tempStorage.copy(storage, aipPath, aipPath);

        virusCheckResult = getAntiVirus().checkForVirus(tempDirectory);
        reportItem
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME,
            virusCheckResult.isClean() ? RodaConstants.REPORT_ATTR_OUTCOME_SUCCESS
              : RodaConstants.REPORT_ATTR_OUTCOME_FAILURE))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, virusCheckResult.getReport()));
        state = virusCheckResult.isClean() ? PluginState.OK : PluginState.ERROR;
        LOGGER.debug("Done with checking if AIP " + aip.getId() + " has virus. Is clean of virus: "
          + virusCheckResult.isClean() + ". Virus check report: " + virusCheckResult.getReport());
      } catch (RuntimeException e) {
        reportItem
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, RodaConstants.REPORT_ATTR_OUTCOME_FAILURE))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        state = PluginState.ERROR;
        exception = e;
        LOGGER.error("Error processing AIP " + aip.getId(), e);
      } catch (StorageServiceException e) {
        reportItem
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, RodaConstants.REPORT_ATTR_OUTCOME_FAILURE))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        state = PluginState.ERROR;
        exception = e;
        LOGGER.error("Error processing AIP " + aip.getId(), e);
      } catch (IOException e) {
        reportItem
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, RodaConstants.REPORT_ATTR_OUTCOME_FAILURE))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        state = PluginState.ERROR;
        exception = e;
        LOGGER.error("Error creating temp folder for AIP " + aip.getId(), e);
      } finally {
        try {
          if (tempDirectory != null) {
            FSUtils.deletePath(tempDirectory);
          }
        } catch (StorageServiceException e) {
          LOGGER.error("Error removing temp storage", e);
        }
      }

      createEvent(virusCheckResult, exception, state, aip, model);
      report.addItem(reportItem);

      try {
        PluginUtils.updateJobReport(model, index, this, reportItem, state, PluginUtils.getJobId(parameters),
          aip.getId());
      } catch (IndexServiceException | NotFoundException e) {
        LOGGER.error("", e);
      }
    }

    return report;
  }

  private void createEvent(VirusCheckResult virusCheckResult, Exception exception, PluginState state, AIP aip,
    ModelService model) throws PluginException {

    try {
      boolean success = (virusCheckResult != null) && virusCheckResult.isClean();

      for (String representationID : aip.getRepresentationIds()) {
        PluginUtils.createPluginEvent(aip.getId(), representationID, model,
          EventPreservationObject.PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK,
          "All the files from the SIP were verified against an antivirus.",
          EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, "AGENT ID",
          Arrays.asList(representationID), success ? "success" : "error", success ? "Report" : "Error",
          success ? virusCheckResult.getReport() : exception.getMessage());
      }
    } catch (PremisMetadataException | IOException | StorageServiceException | ModelServiceException e) {
      throw new PluginException(e.getMessage(), e);
    }

    // TODO agent
    /*
     * DateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss.SSS");
     * EventPreservationObject epo = new EventPreservationObject();
     * epo.setDatetime(new Date()); epo.setEventType(EventPreservationObject.
     * PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK); epo.setEventDetail(
     * "All the files from the SIP were verified against an antivirus.");
     * epo.setAgentRole(EventPreservationObject.
     * PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK); String name =
     * UUID.randomUUID().toString(); epo.setId(name); epo.setAgentID("AGENT ID"
     * ); epo.setObjectIDs(aip.getRepresentationIds().toArray(new
     * String[aip.getRepresentationIds().size()]));
     * epo.setOutcome(virusCheckResult.isClean()?"success":"error");
     * epo.setOutcomeDetailNote("Report");
     * epo.setOutcomeDetailExtension(virusCheckResult.getReport()); byte[]
     * serializedPremisEvent = new PremisEventHelper(epo).saveToByteArray();
     * Path file = Files.createTempFile("preservation", ".xml"); Files.copy(new
     * ByteArrayInputStream(serializedPremisEvent), file,
     * StandardCopyOption.REPLACE_EXISTING); Binary resource = (Binary)
     * FSUtils.convertPathToResource(file.getParent(), file);
     * model.createPreservationMetadata(aip.getId(), name, resource);
     */
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  public String getAntiVirusClassName() {
    return antiVirusClassName;
  }

  public void setAntiVirusClassName(String antiVirusClassName) {
    this.antiVirusClassName = antiVirusClassName;
  }

  public AntiVirus getAntiVirus() {
    return antiVirus;
  }

  public void setAntiVirus(AntiVirus antiVirus) {
    this.antiVirus = antiVirus;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    AntivirusPlugin antivirusPlugin = new AntivirusPlugin();
    antivirusPlugin.setAntiVirus(getAntiVirus());
    return antivirusPlugin;
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
