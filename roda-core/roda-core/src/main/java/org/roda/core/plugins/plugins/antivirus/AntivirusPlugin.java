/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
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
      "core.plugins.internal.virus_check.antiVirusClassname", "org.roda.core.plugins.plugins.antivirus.ClamAntiVirus");

    try {
      LOGGER.debug("Loading antivirus class {}", antiVirusClassName);
      setAntiVirus((AntiVirus) Class.forName(antiVirusClassName).newInstance());
      LOGGER.debug("Using antivirus {}", getAntiVirus().getClass().getName());
    } catch (ClassNotFoundException e) {
      LOGGER.warn("Antivirus class {} not found - {}", antiVirusClassName, e.getMessage());
    } catch (InstantiationException e) {
      // not possible to create a new instance of the class
      LOGGER.warn("Antivirus class {} instantiation exception - {}", antiVirusClassName, e.getMessage());
    } catch (IllegalAccessException e) {
      // not possible to create a new instance of the class
      LOGGER.warn("Antivirus class {} illegal access exception - {}", antiVirusClassName, e.getMessage());
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
    return "Virus check";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Scans an information package for malicious software using the Antivirus application ClamAV.";
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      try {
        for (AIP aip : list) {
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class,
            AIPState.INGEST_PROCESSING);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
          PluginState reportState = PluginState.SUCCESS;

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
          } catch (Exception e) {
            reportState = PluginState.FAILURE;
            reportItem.setPluginState(reportState).setPluginDetails(e.getMessage());
            jobPluginInfo.incrementObjectsProcessedWithFailure();

            exception = e;
            LOGGER.error("Error processing AIP " + aip.getId(), e);
          } catch (Throwable e) {
            LOGGER.error("Error processing AIP " + aip.getId(), e);
            throw new PluginException(e);
          } finally {
            IOUtils.closeQuietly(directAccess);
          }

          // FIXME 20160314 hsilva: perhaps the following code should be put
          // inside the above finally block, because if an error occurs the
          // event
          // creation will never happen
          try {
            jobPluginInfo.incrementObjectsProcessed(reportState);
            boolean notify = true;
            createEvent(virusCheckResult, exception, reportItem.getPluginState(), aip, model, index, notify);
            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
          } catch (Throwable e) {
            LOGGER.error("Error updating event and job", e);
          }
        }
      } catch (ClassCastException e) {
        LOGGER.error("Trying to execute an AIP-only plugin with other objects");
        jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  private void createEvent(VirusCheckResult virusCheckResult, Exception exception, PluginState state, AIP aip,
    ModelService model, IndexService index, boolean notify) throws PluginException {

    try {
      StringBuilder outcomeDetailExtension = new StringBuilder(virusCheckResult.getReport());
      if (state != PluginState.SUCCESS && exception != null) {
        outcomeDetailExtension.append("\n").append(exception.getClass().getName()).append(": ")
          .append(exception.getMessage());
      }

      PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, outcomeDetailExtension.toString(), notify);

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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_VALIDATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
