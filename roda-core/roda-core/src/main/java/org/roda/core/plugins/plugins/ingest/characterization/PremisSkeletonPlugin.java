/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
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

public class PremisSkeletonPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  private boolean createsPluginEvent = true;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Create file fixity information";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Creates base PREMIS objects with file original name and file fixity information (SHA-256).";
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
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // updates the flag responsible to allow plugin event creation
    if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT)) {
      createsPluginEvent = Boolean
        .parseBoolean(getParameterValues().get(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP {}", aip.getId());
      Report reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);
      PluginHelper.updateJobReport(this, model, index, reportItem, false);

      try {
        for (Representation representation : aip.getRepresentations()) {
          LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
          PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, storage, aip, representation.getId());
          model.notifyRepresentationUpdated(representation);
        }

        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (RODAException | XmlException | IOException e) {
        LOGGER.error("Error processing AIP " + aip.getId(), e);

        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      }

      if (createsPluginEvent) {
        try {
          boolean notify = true;
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(), "", notify);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: " + e.getMessage(), e);
        }

      }
      report.addReport(reportItem);

      PluginHelper.updateJobReport(this, model, index, reportItem, true);

    }
    return report;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new PremisSkeletonPlugin();
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
    return PreservationEventType.MESSAGE_DIGEST_CALCULATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Created base PREMIS objects with file original name and file fixity information (SHA-256).";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "PREMIS objects were successfully created.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to create PREMIS objects from files.";
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

}
