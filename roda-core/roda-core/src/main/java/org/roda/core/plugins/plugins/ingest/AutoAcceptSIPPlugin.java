/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.Arrays;
import java.util.List;

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
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoAcceptSIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoAcceptSIPPlugin.class);

  public static final String FAILURE_MESSAGE = "Failed to add the AIP to the repository's inventory.";
  public static final String SUCCESS_MESSAGE = "The AIP was successfully added to the repository's inventory.";
  public static final String DESCRIPTION = "Added package to the inventory. After this point, the responsibility for the digital content’s preservation is passed on to the repository.";

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Auto accept";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Adds information package to the inventory without any human appraisal. After this point, the responsibility for the digital content’s preservation is passed on to the repository.";
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      String username = PluginHelper.getJobUsername(this, index);

      Job job = PluginHelper.getJob(this, model);
      List<AIP> list = PluginHelper.transformLitesIntoObjects(model, index, this, report, null, liteList, job);

      for (AIP aip : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);

        String outcomeDetail = "";
        try {
          LOGGER.debug("Auto accepting AIP {}", aip.getId());

          aip.setState(AIPState.ACTIVE);
          aip = model.updateAIPState(aip, username);
          reportItem.setPluginState(PluginState.SUCCESS).setOutcomeObjectState(AIPState.ACTIVE);
          LOGGER.debug("Done with auto accepting AIP {}", aip.getId());
        } catch (RODAException e) {
          LOGGER.error("Error updating AIP (metadata attribute state=ACTIVE)", e);
          outcomeDetail = "Error updating AIP (metadata attribute state=ACTIVE): " + e.getMessage();
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(outcomeDetail)
            .setOutcomeObjectState(AIPState.UNDER_APPRAISAL);
        }

        try {
          boolean notify = true;
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(), outcomeDetail,
            notify);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: " + e.getMessage(), e);
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
      }
    } catch (RODAException e) {
      LOGGER.error("Error getting job from plugin", e);
    }

    return report;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AutoAcceptSIPPlugin();
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
    return PreservationEventType.ACCESSION;
  }

  @Override
  public String getPreservationEventDescription() {
    return DESCRIPTION;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return SUCCESS_MESSAGE;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return FAILURE_MESSAGE;
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
