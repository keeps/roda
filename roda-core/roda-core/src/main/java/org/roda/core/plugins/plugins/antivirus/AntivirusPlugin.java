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
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);

    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);

      VirusCheckResult virusCheckResult = null;
      Exception exception = null;
      DirectResourceAccess directAccess = null;
      try {
        LOGGER.debug("Checking if AIP " + aip.getId() + " is clean of virus");
        StoragePath aipPath = ModelUtils.getAIPStoragePath(aip.getId());

        directAccess = storage.getDirectAccess(aipPath);
        virusCheckResult = getAntiVirus().checkForVirus(directAccess.getPath());

        state = virusCheckResult.isClean() ? PluginState.SUCCESS : PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, virusCheckResult.getReport()));

        LOGGER.debug("Done with checking if AIP " + aip.getId() + " has virus. Is clean of virus: "
          + virusCheckResult.isClean() + ". Virus check report: " + virusCheckResult.getReport());
      } catch (RuntimeException | RequestNotValidException | GenericException | NotFoundException
        | AuthorizationDeniedException e) {
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
        IOUtils.closeQuietly(directAccess);
      }

      try {
        LOGGER.info("Creating event");
        boolean notify = true;
        createEvent(virusCheckResult, exception, state, aip, model, notify);
        report.addItem(reportItem);

        LOGGER.info("Updating job report");
        PluginHelper.updateJobReport(model, index, this, reportItem, state, aip.getId());

        LOGGER.info("Done job report");
      } catch (Throwable e) {
        LOGGER.error("Error updating event and job", e);
      }
    }

    return report;
  }

  private void createEvent(VirusCheckResult virusCheckResult, Exception exception, PluginState state, AIP aip,
    ModelService model, boolean notify) throws PluginException {

    try {
      boolean inotify = false;
      boolean success = (virusCheckResult != null) && virusCheckResult.isClean();
      String representationId = null;
      String fileId = null;
      List<String> filePath = null;
      // TODO review below information and externalise strings
      String eventType = RodaConstants.PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK;
      String eventDetails = "All the files from the SIP were verified against an antivirus.";
      List<String> sourceObjects = Arrays.asList(IdUtils.getLinkingIdentifierId(aip.getId(), null, null, null));
      List<String> outcomeObjects = null;
      String outcome = success ? "success" : "failure";
      String outcomeDetailNote = success ? virusCheckResult.getReport()
        : virusCheckResult.getReport() + "\n" + exception.getClass().getName() + ": " + exception.getMessage();
      String outcomeDetailExtension = null;

      PluginHelper.createPluginEvent(this, aip.getId(), representationId, filePath, fileId, model, eventType,
        eventDetails, sourceObjects, outcomeObjects, outcome, outcomeDetailNote, outcomeDetailExtension, inotify);

      if (notify) {
        model.notifyAIPUpdated(aip.getId());
      }
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException("Error while creating the event", e);
    }
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
