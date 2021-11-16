/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.SIPInformation;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractAIPComponentsPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegfriedPlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {
  public static final String FILE_SUFFIX = ".json";
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPlugin.class);

  /*
   * private SIPUpdateInformation sipUpdateInformation = new
   * SIPUpdateInformation();
   */

  public static String getStaticName() {
    return "File format identification (Siegfried)";
  }

  public static String getStaticDescription() {
    return "Identifies the file format and version of data files included in Information Packages using the Siegfried tool (a signature-based file format "
      + "identification tool that supports PRONOM identifiers and Mimetypes).\nThe task updates PREMIS objects metadata in the Information Package to store "
      + "the results of format identification. A PREMIS event is also recorded after the task is run.";
  }

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
    return getStaticName();
  }

  @Override
  public String getDescription() {
    return getStaticDescription();
  }

  @Override
  public String getVersionImpl() {
    return SiegfriedPluginUtils.getVersion();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SIP_UPDATE_INFORMATION)) {
      try {
        setSipInformation(JsonUtils
          .getObjectFromJson(parameters.get(RodaConstants.PLUGIN_PARAMS_SIP_UPDATE_INFORMATION), SIPInformation.class));
      } catch (GenericException e) {
        LOGGER.debug("Could not serializable SIP information from JSON", e);
      }
    }
  }

  @Override
  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<AIP> list, Job cachedJob) {
    try {
      for (AIP aip : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);

        LOGGER.debug("Processing AIP {}", aip.getId());
        List<LinkingIdentifier> sources = new ArrayList<>();
        Map<String, Map<String, List<String>>> updatedData = getSipInformation().getUpdatedData();

        if (!getSipInformation().hasUpdatedData() || !updatedData.containsKey(aip.getId())) {
          try {
            if (getSipInformation().isUpdate()) {
              // SIP UPDATE
              if (AIPState.INGEST_PROCESSING.equals(aip.getState())) {
                if( aip.getRepresentations() != null && !aip.getRepresentations().isEmpty()) {
                  for (Representation representation : aip.getRepresentations()) {
                    LOGGER.debug("Processing representation {} of AIP {}", representation.getId(),
                        aip.getId());
                    sources.addAll(
                        SiegfriedPluginUtils.runSiegfriedOnRepresentation(model, representation));
                    model.notifyRepresentationUpdated(representation).failOnError();
                  }

                  jobPluginInfo.incrementObjectsProcessedWithSuccess();
                  reportItem.setPluginState(PluginState.SUCCESS);
                }
                else {
                  reportItem.setPluginState(PluginState.SKIPPED).setPluginDetails("Skipped because no representation was found for this AIP.");
                  jobPluginInfo.incrementObjectsProcessed(PluginState.SKIPPED);
                }
              } else {
                reportItem.setPluginState(PluginState.SKIPPED).setPluginDetails("Executed on a SIP update context.");
                jobPluginInfo.incrementObjectsProcessed(PluginState.SKIPPED);
              }
            } else {
              // SIP CREATE
              if (aip.getRepresentations() != null && !aip.getRepresentations().isEmpty()) {
                for (Representation representation : aip.getRepresentations()) {
                  LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
                  sources.addAll(SiegfriedPluginUtils.runSiegfriedOnRepresentation(model, representation));
                  model.notifyRepresentationUpdated(representation).failOnError();
                }

                jobPluginInfo.incrementObjectsProcessedWithSuccess();
                reportItem.setPluginState(PluginState.SUCCESS);
              } else {
                jobPluginInfo.incrementObjectsProcessedWithSkipped();
                reportItem.setPluginState(PluginState.SKIPPED)
                    .setPluginDetails("Skipped because no representation was found for this AIP");
              }
            }
          } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
            | AuthorizationDeniedException e) {
            LOGGER.error("Error running Siegfried {}: {}", aip.getId(), e.getMessage(), e);

            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.setPluginState(PluginState.FAILURE)
              .setPluginDetails("Error running Siegfried " + aip.getId() + ": " + e.getMessage());
          }
        } else {
          Map<String, List<String>> aipData = updatedData.get(aip.getId());
          PluginState state = PluginState.SKIPPED;

          if (aipData.containsKey(RodaConstants.RODA_OBJECT_REPRESENTATION)) {
            if (aip.getRepresentations() != null && !aip.getRepresentations().isEmpty()) {

              List<Representation> filteredList = aip.getRepresentations().stream()
                  .filter(
                      r -> aipData.get(RodaConstants.RODA_OBJECT_REPRESENTATION)
                          .contains(IdUtils.getRepresentationId(r)))
                  .collect(Collectors.toList());

              for (Representation representation : filteredList) {
                try {
                  LOGGER.debug("Processing representation {} of AIP {}", representation.getId(),
                      aip.getId());
                  sources.addAll(
                      SiegfriedPluginUtils.runSiegfriedOnRepresentation(model, representation));
                  model.notifyRepresentationUpdated(representation).failOnError();
                  state = PluginState.SUCCESS;
                } catch (RODAException e) {
                  state = PluginState.FAILURE;
                  LOGGER.error("Error running Siegfried " + aip.getId(), e);
                }
              }
            } else {
              reportItem.setPluginState(state).setPluginDetails("Skipped because no representation was found for this AIP");
              jobPluginInfo.incrementObjectsProcessed(state);
            }
            reportItem.setPluginState(state).setPluginDetails("Executed on a SIP update context.");
            jobPluginInfo.incrementObjectsProcessed(state);
          }
        }

        try {
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, reportItem.getPluginState(),
            "", true, cachedJob);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: {}", e.getMessage(), e);
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
      }
    } catch (ClassCastException e) {
      LOGGER.error("Trying to execute an AIP-only plugin with other objects");
      jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
    }

    return report;
  }

  @Override
  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<Representation> list, Job cachedJob) {

    for (Representation representation : list) {
      List<LinkingIdentifier> sources = new ArrayList<>();

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), representation.getAipId());
      try {
        sources.addAll(SiegfriedPluginUtils.runSiegfriedOnRepresentation(model, representation));
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
        model.notifyRepresentationUpdated(representation).failOnError();
      } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException e) {
        LOGGER.error("Error running Siegfried {}: {}", representation.getAipId(), e.getMessage(), e);

        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error running Siegfried " + representation.getAipId() + ": " + e.getMessage());
      }

      try {
        PluginHelper.createPluginEvent(this, representation.getAipId(), representation.getId(), model, index, sources,
          null, reportItem.getPluginState(), "", true, cachedJob);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }

    return report;
  }

  @Override
  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<File> list, Job cachedJob) {

    for (File file : list) {
      List<LinkingIdentifier> sources = new ArrayList<>();

      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, cachedJob);
      LOGGER.debug("Processing file {} from representation {} of AIP {}", file.getId(), file.getRepresentationId(),
        file.getAipId());

      try {
        sources.addAll(SiegfriedPluginUtils.runSiegfriedOnFile(model, file));
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (PluginException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException e) {
        LOGGER.error("Error running Siegfried on file {}: {}", file.getId(), e.getMessage(), e);

        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Error running Siegfried on file " + file.getId() + ": " + e.getMessage());
      }

      try {
        List<LinkingIdentifier> outcomes = null;
        boolean notify = true;
        PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          model, index, sources, outcomes, reportItem.getPluginState(), "", notify, cachedJob);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, cachedJob);
    }

    return report;
  }

  @Override
  public Plugin<T> cloneMe() {
    SiegfriedPlugin<T> siegfriedPlugin = new SiegfriedPlugin<>();
    try {
      siegfriedPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing {} init", SiegfriedPlugin.class.getName(), e);
    }
    return siegfriedPlugin;
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
    return PreservationEventType.FORMAT_IDENTIFICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Identified the object's file formats and versions using Siegfried.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File formats were identified and recorded in PREMIS objects.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to identify file formats in the package.";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_FORMAT_IDENTIFICATION,
      RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
  }

}
