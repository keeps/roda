/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.antivirus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AntivirusPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AntivirusPlugin.class);

  private String antiVirusClassName;
  private AntiVirus antiVirus = null;

  @Override
  public void init() throws PluginException {
    antiVirusClassName = RodaCoreFactory.getRodaConfiguration().getString(
      "core.plugins.internal.virus_check.antiVirusClassname", "org.roda.core.plugins.internal.antivirus.ClamAntiVirus");

    try {
      LOGGER.debug("Loading antivirus class {}", antiVirusClassName);
      setAntiVirus((AntiVirus) Class.forName(antiVirusClassName).newInstance());
      LOGGER.debug("Using antivirus {}", getAntiVirus().getClass().getName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      LOGGER.warn("Error loading antivirus", e);
    }

    if (getAntiVirus() == null) {
      setAntiVirus(new AVGAntiVirus());
      LOGGER.warn("Using default antivirus {}", getAntiVirus().getClass().getName());
    }

    LOGGER.debug("init OK");
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Malware detector";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "This plugin provides robust security features by leveraging the ClamAV antivirus engine to scan files for potential threats, including trojans, "
      + "viruses, malware, and other malicious content.\nClamAV is a trusted, open-source (GPL) antivirus engine that is widely used in the industry for its "
      + "exceptional accuracy and effectiveness in detecting threats";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    if (antiVirus != null) {
      return antiVirus.getVersion();
    }
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        preProcessAIP(index, model, storage, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void preProcessAIP(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job job, AIP aip) {
    PluginState reportState = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);

    if (aip.getHasShallowFiles() != null && aip.getHasShallowFiles()) {
      StorageService tmpStorageService = null;
      try {
        tmpStorageService = ModelUtils.resolveTemporaryResourceShallow(job.getId(), storage,
          ModelUtils.getAIPStoragePath(aip.getId()));
        processAIP(index, model, tmpStorageService, report, jobPluginInfo, job, aip, reportItem, reportState);
      } catch (RequestNotValidException | GenericException e) {
        LOGGER.error("Error processing AIP " + aip.getId(), e);
        reportState = PluginState.FAILURE;
        reportItem.setPluginState(reportState).setPluginDetails(e.getMessage());
      } finally {
        try {
          if (!job.getPluginType().equals(PluginType.INGEST)) {
            ModelUtils.removeTemporaryResourceShallow(job.getId(), ModelUtils.getAIPStoragePath(aip.getId()));
          }
        } catch (RequestNotValidException | IOException e) {
          LOGGER.error("Error on removing temporary AIP " + aip.getId(), e);
        }
      }
    } else {
      processAIP(index, model, storage, report, jobPluginInfo, job, aip, reportItem, reportState);
    }
  }

  private void processAIP(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job job, AIP aip, Report reportItem, PluginState reportState) {
    PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);

    VirusCheckResult virusCheckResult = null;
    Exception exception = null;
    DirectResourceAccess directAccess = null;

    try {
      LOGGER.debug("Checking if AIP {} is clean of virus", aip.getId());
      StoragePath aipPath = ModelUtils.getAIPStoragePath(aip.getId());

      directAccess = storage.getDirectAccess(aipPath);
      virusCheckResult = getAntiVirus().checkForVirus(directAccess.getPath());
      reportState = virusCheckResult.isClean() ? PluginState.SUCCESS : PluginState.FAILURE;
      reportItem.setPluginState(reportState).setPluginDetails(virusCheckResult.getReport());

      LOGGER.debug("Done with checking if AIP {} has virus. Is clean of virus: {}", aip.getId(),
        virusCheckResult.isClean());
    } catch (RODAException | RuntimeException e) {
      LOGGER.error("Error processing AIP " + aip.getId(), e);
      reportState = PluginState.FAILURE;
      reportItem.setPluginState(reportState).setPluginDetails(e.getMessage());
      exception = e;
    } finally {
      IOUtils.closeQuietly(directAccess);
      jobPluginInfo.incrementObjectsProcessed(reportState);

      try {
        createEvent(virusCheckResult, exception, reportItem.getPluginState(), aip, model, index, true, job);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      } catch (PluginException e) {
        LOGGER.error("Error updating event and job", e);
      }
    }

  }

  private void createEvent(VirusCheckResult virusCheckResult, Exception exception, PluginState state, AIP aip,
    ModelService model, IndexService index, boolean notify, Job job) throws PluginException {
    try {
      StringBuilder outcomeDetailExtension = new StringBuilder();
      outcomeDetailExtension.append(virusCheckResult == null ? "" : virusCheckResult.getReport());
      if (state != PluginState.SUCCESS && exception != null) {
        outcomeDetailExtension.append("\n").append(exception.getClass().getName()).append(": ")
          .append(exception.getMessage());
      }

      PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, outcomeDetailExtension.toString(), notify,
        job);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException("Error while creating the event", e);
    }
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

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.VIRUS_CHECK;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Scanned package for malicious programs using ClamAV.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The package does not contain any known malicious programs.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "A malicious program was detected inside the package.";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_VALIDATION, RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
