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
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    IndexedPreservationAgent agent = null;
    try {
      agent = PremisUtils.createPremisAgentBinary(this, RodaConstants.PRESERVATION_AGENT_TYPE_INGEST_TASK, model);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error running creating antivirus agent: " + e.getMessage(), e);
    }
    
    Report report = PluginHelper.createPluginReport(this);
    Path sourcePath = null;
    boolean deleteSourcePath = false;
    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "Check for virus", aip.getId(), null);

      VirusCheckResult virusCheckResult = null;
      Exception exception = null;
      try {
        LOGGER.debug("Checking if AIP " + aip.getId() + " is clean of virus");
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());

        if (storage instanceof FileStorageService) {
          sourcePath = ((FileStorageService) storage).resolve(aipPath);
          deleteSourcePath = false;
        } else {
          sourcePath = Files.createTempDirectory("temp");
          StorageService tempStorage = new FileStorageService(sourcePath);
          tempStorage.copy(storage, aipPath, aipPath);
          deleteSourcePath = true;
        }

        virusCheckResult = getAntiVirus().checkForVirus(sourcePath);

        state = virusCheckResult.isClean() ? PluginState.SUCCESS : PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, virusCheckResult.getReport()));

        LOGGER.debug("Done with checking if AIP " + aip.getId() + " has virus. Is clean of virus: "
          + virusCheckResult.isClean() + ". Virus check report: " + virusCheckResult.getReport());
      } catch (RuntimeException | IOException | RequestNotValidException | AlreadyExistsException | GenericException
        | NotFoundException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));

        exception = e;
        LOGGER.error("Error processing AIP " + aip.getId(), e);
      } catch (Throwable e) {
        LOGGER.error("Error processing AIP " + aip.getId(), e);
        throw new PluginException(e);
      } finally {
        try {
          if (deleteSourcePath && sourcePath != null) {
            FSUtils.deletePath(sourcePath);
          }
        } catch (GenericException | NotFoundException e) {
          LOGGER.error("Error removing temp storage", e);
        }
      }

      try {
        LOGGER.info("Creating event");
        createEvent(virusCheckResult, exception, state, aip, model,agent);
        report.addItem(reportItem);

        LOGGER.info("Updating job report");
        PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
          aip.getId());

        LOGGER.info("Done job report");
      } catch (Throwable e) {
        LOGGER.error("Error updating event and job", e);
      }
    }

    return report;
  }

  private void createEvent(VirusCheckResult virusCheckResult, Exception exception, PluginState state, AIP aip,
    ModelService model, IndexedPreservationAgent agent) throws PluginException {

    try {
      boolean success = (virusCheckResult != null) && virusCheckResult.isClean();

      for (Representation representation : aip.getRepresentations()) {
        PluginHelper.createPluginEvent(aip.getId(), representation.getId(), null, model,
          RodaConstants.PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK, "All the files from the SIP were verified against an antivirus.",
          Arrays.asList(representation.getId()), null, success ? "success" : "failure", success ? "" : "Error",
            success ? virusCheckResult.getReport() : exception.getMessage(), agent);
      }
    } catch (PremisMetadataException | IOException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException e) {
      throw new PluginException("Error while creating the event", e);
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
