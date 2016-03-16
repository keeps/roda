/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.util.List;
import java.util.Map;

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

public class TikaFullTextPlugin extends AbstractPlugin<AIP> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TikaFullTextPlugin.class);

  public static final String FILE_SUFFIX = ".html";
  public static final String OTHER_METADATA_TYPE_FULLTEXT = "ApacheTikaFullText";
  public static final String OTHER_METADATA_TYPE_METADATA = "ApacheTikaMetadata";
  
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
    return "Full-text extraction action";
  }

  @Override
  public String getDescription() {
    return "Extracts the full-text from the representation files";
  }

  @Override
  public String getVersionImpl() {
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

    for (AIP aip : list) {
      Report reportItem = PluginHelper.createPluginReportItem(this, aip.getId(), null);

      LOGGER.debug("Processing AIP " + aip.getId());
      try {
        for (Representation representation : aip.getRepresentations()) {
          LOGGER.debug("Processing representation " + representation.getId() + " of AIP " + aip.getId());
          TikaFullTextPluginUtils.runTikaFullTextOnRepresentation(index, model, storage, aip, representation);
          model.notifyRepresentationUpdated(representation);
        }
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (RODAException e) {
        LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);

        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error running Tika " + aip.getId() + ": " + e.getMessage());
      }

      report.addReport(reportItem);

      PluginHelper.updateJobReport(this, model, index, reportItem);

      if (createsPluginEvent) {
        try {
          boolean notify = true;
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(), "", notify);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: " + e.getMessage(), e);
        }

      }
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
    TikaFullTextPlugin tikaPlugin = new TikaFullTextPlugin();
    try {
      tikaPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + TikaFullTextPlugin.class.getName() + "init", e);
    }
    return tikaPlugin;
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
    return PreservationEventType.METADATA_EXTRACTION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Extraction of technical metadata using Apache Tika.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Successfully extracted technical metadata from file.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to extract technical metadata from file.";
  }

}
