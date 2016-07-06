/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
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
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
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
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT)) {
      createsPluginEvent = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT));
    }
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
          LOGGER.debug("Processing AIP {}", aip.getId());
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class,
            AIPState.INGEST_PROCESSING);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);

          try {
            for (Representation representation : aip.getRepresentations()) {
              LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
              List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
              PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aip, representation.getId(),
                algorithms);
              model.notifyRepresentationUpdated(representation);
            }

            jobPluginInfo.incrementObjectsProcessedWithSuccess();
            reportItem.setPluginState(PluginState.SUCCESS);
          } catch (RODAException | XmlException | IOException e) {
            LOGGER.error("Error processing AIP " + aip.getId(), e);

            jobPluginInfo.incrementObjectsProcessedWithFailure();
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
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
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

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }

}
