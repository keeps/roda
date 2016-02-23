/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
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

public class AutoAcceptSIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoAcceptSIPPlugin.class);

  private static final String EVENT_DESCRIPTION = "Added package to the inventory. After this point, the responsibility for the digital content’s preservation is passed on to the repository. ";
  private static final String EVENT_SUCESS_MESSAGE = "The AIP was successfully added to the repository's inventory.";
  private static final String EVENT_FAILURE_MESSAGE = "Failed to add the AIP to the repository's inventory.";

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Auto accept";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Adds package to the inventory without any human appraisal. After this point, the responsibility for the digital content’s preservation is passed on to the repository.";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);
      String outcomeDetail = "";
      try {
        LOGGER.debug("Auto accepting AIP " + aip.getId());

        aip.setActive(true);
        aip = model.updateAIP(aip);
        state = PluginState.SUCCESS;
        reportItem.setItemId(aip.getId());
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
        LOGGER.debug("Done with auto accepting AIP " + aip.getId());
      } catch (RODAException e) {
        LOGGER.error("Error updating AIP (metadata attribute active=true)", e);
        outcomeDetail = "Error updating AIP (metadata attribute active=true): " + e.getMessage();
        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, outcomeDetail));
      }

      createEvent(outcomeDetail, state, aip, model);
      report.addItem(reportItem);

      PluginHelper.updateJobReport(model, index, this, reportItem, state, aip.getId());
    }

    return report;
  }

  private void createEvent(String outcomeDetail, PluginState state, AIP aip, ModelService model)
    throws PluginException {

    try {
      List<LinkingIdentifier> sources = PluginHelper.getLinkingRepresentations(aip, model);
      List<LinkingIdentifier> outcomes = null;
      boolean notify = true;
      String eventType = RodaConstants.PRESERVATION_EVENT_TYPE_ACCESSION;
      String outcome = state.name();
      PluginHelper.createPluginEvent(this, aip.getId(), null, null, null, model, eventType, EVENT_DESCRIPTION, sources,
        outcomes, outcome, state == PluginState.SUCCESS ? EVENT_SUCESS_MESSAGE : EVENT_FAILURE_MESSAGE, "", notify);
    } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | AlreadyExistsException e) {
      LOGGER.error("Error creating event: " + e.getMessage(), e);
    }
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
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

}
