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
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
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

  @Override
  public String getName() {
    return "Create file fixity information";
  }

  @Override
  public String getDescription() {
    return "Creates base PREMIS objects with file original name and file fixity information (SHA-256).";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    // updates the flag responsible to allow plugin event creation
    if (getParameterValues().containsKey("createsPluginEvent")) {
      createsPluginEvent = Boolean.parseBoolean(getParameterValues().get("createsPluginEvent"));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);

      try {
        for (Representation representation : aip.getRepresentations()) {

          LOGGER.debug("createPremisForRepresentation  " + representation.getId());
          LOGGER.debug("Processing representation " + representation.getId() + " from AIP " + aip.getId());

          PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, storage, aip, representation.getId());
        }
        model.notifyAIPUpdated(aip.getId());

        state = PluginState.SUCCESS;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
      } catch (RODAException | XmlException | IOException e) {
        LOGGER.error("Error processing AIP " + aip.getId(), e);

        state = PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
      }

      if (createsPluginEvent) {
        try {
          List<LinkingIdentifier> sources = PluginHelper.getLinkingRepresentations(aip, model);
          List<LinkingIdentifier> outcomes = null;
          boolean notify = true;
          PluginHelper.createPluginEvent(this, aip.getId(), null, null, null, model, sources, outcomes, state, "",
            notify);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: " + e.getMessage(), e);
        }

      }
      report.addItem(reportItem);

      PluginHelper.updateJobReport(model, index, this, reportItem, state, aip.getId());

    }
    return report;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

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

}
