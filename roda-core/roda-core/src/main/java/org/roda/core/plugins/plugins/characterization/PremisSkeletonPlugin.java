/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.xmlbeans.XmlException;
import org.roda.core.RodaCoreFactory;
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
import org.roda.core.data.v2.ip.SIPUpdateInformation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractAIPComponentsPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PremisSkeletonPlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  private SIPUpdateInformation sipUpdateInformation = new SIPUpdateInformation();

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public static String getStaticName() {
    return "Fixity information computation";
  }

  @Override
  public String getName() {
    return getStaticName();
  }

  public static String getStaticDescription() {
    return "Computes file fixity information (also known as checksum) for all data files within an AIP, representation or file and stores this information in PREMIS objects "
      + "within the corresponding entity. This task uses SHA-256 as the default checksum algorithm, however, other algorithms can be configured in “roda-core.properties”."
      + "\nFile fixity is the property of a digital file being fixed, or unchanged. “AIP corruption risk assessment” is the process of validating that a file has not changed or been "
      + "altered from a previous state. In order to validate the fixity of an AIP or file, fixity information has to be generated beforehand.";
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

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_SIP_UPDATE_INFORMATION)) {
      try {
        sipUpdateInformation = JsonUtils.getObjectFromJson(
          parameters.get(RodaConstants.PLUGIN_PARAMS_SIP_UPDATE_INFORMATION), SIPUpdateInformation.class);
      } catch (GenericException e) {
        LOGGER.debug("Could not serializable SIP Update information from JSON", e);
      }
    }
  }

  @Override
  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException {

    try {
      List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
      for (AIP aip : list) {
        LOGGER.debug("Processing AIP {}", aip.getId());
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
        Map<String, Map<String, List<String>>> updatedData = sipUpdateInformation.getUpdatedData();

        if (!sipUpdateInformation.hasUpdatedData() || !updatedData.containsKey(aip.getId())) {
          try {
            for (Representation representation : aip.getRepresentations()) {
              LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
              PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aip.getId(), representation.getId(),
                algorithms);
              // notify is not failing because it is not crucial
              model.notifyRepresentationUpdated(representation);
            }

            jobPluginInfo.incrementObjectsProcessedWithSuccess();
            reportItem.setPluginState(PluginState.SUCCESS);
          } catch (RODAException | XmlException | IOException e) {
            LOGGER.error("Error processing AIP {}", aip.getId(), e);
            jobPluginInfo.incrementObjectsProcessedWithFailure();
            reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
          }

          try {
            PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(), "", true);
          } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
            | AuthorizationDeniedException | AlreadyExistsException e) {
            LOGGER.error("Error creating event: {}", e.getMessage(), e);
          }
        } else {
          Map<String, List<String>> aipData = updatedData.get(aip.getId());
          PluginState state = PluginState.SUCCESS;

          if (aipData.containsKey(RodaConstants.RODA_OBJECT_REPRESENTATION)) {
            List<Representation> filteredList = aip.getRepresentations().stream()
              .filter(
                r -> aipData.get(RodaConstants.RODA_OBJECT_REPRESENTATION).contains(IdUtils.getRepresentationId(r)))
              .collect(Collectors.toList());

            for (Representation representation : filteredList) {
              try {
                LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
                PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, aip.getId(),
                  representation.getId(), algorithms);
                model.notifyRepresentationUpdated(representation);
              } catch (RODAException | XmlException | IOException e) {
                state = PluginState.FAILURE;
                LOGGER.error("Failed to execute premis skeleton plugin on representation {} from AIP {}",
                  representation.getId(), representation.getAipId(), e);
              }
            }
          }

          if (aipData.containsKey(RodaConstants.RODA_OBJECT_FILE)) {
            for (String fileString : aipData.get(RodaConstants.RODA_OBJECT_FILE)) {
              List<String> filePath = Arrays.asList(fileString.split("/"));
              String representationId = filePath.remove(0);
              String fileId = filePath.remove(filePath.size() - 1);

              try {
                File file = model.retrieveFile(aip.getId(), representationId, filePath, fileId);
                PremisSkeletonPluginUtils.createPremisSkeletonOnFile(model, file, algorithms);
                jobPluginInfo.incrementObjectsProcessedWithSuccess();
              } catch (RODAException | XmlException | IOException e) {
                state = PluginState.FAILURE;
                LOGGER.error("Failed to execute premis skeleton plugin on file {} from AIP {}", fileString, aip.getId(),
                  e);
              }
            }
          }

          reportItem.setPluginState(state).setPluginDetails("Executed on a SIP update context.");
          jobPluginInfo.incrementObjectsProcessed(state);
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    } catch (ClassCastException e) {
      LOGGER.error("Trying to execute an AIP-only plugin with other objects", e);
      jobPluginInfo.incrementObjectsProcessedWithFailure(list.size());
    }

    return report;
  }

  @Override
  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {

    List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
    for (Representation representation : list) {
      LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), representation.getAipId());
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class, AIPState.ACTIVE);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      reportItem.setPluginState(PluginState.SUCCESS);

      try {
        PremisSkeletonPluginUtils.createPremisSkeletonOnRepresentation(model, representation.getAipId(),
          representation.getId(), algorithms);
        model.notifyRepresentationUpdated(representation);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } catch (RODAException | XmlException | IOException e) {
        LOGGER.error("Error processing representation {}", representation.getId(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE).addPluginDetails(e.getMessage() + "\n");
      }

      try {
        boolean notify = true;
        PluginHelper.createPluginEvent(this, representation.getAipId(), representation.getId(), model, index, null,
          null, reportItem.getPluginState(), "", notify);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    }

    return report;
  }

  @Override
  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {

    List<String> algorithms = RodaCoreFactory.getFixityAlgorithms();
    for (File file : list) {
      LOGGER.debug("Processing file {} from representation {} from AIP {}", file.getId(), file.getRepresentationId(),
        file.getAipId());
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class, AIPState.ACTIVE);
      PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
      reportItem.setPluginState(PluginState.SUCCESS);

      try {
        PremisSkeletonPluginUtils.createPremisSkeletonOnFile(model, file, algorithms);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } catch (RODAException | XmlException | IOException e) {
        LOGGER.error("Error processing file {}", file.getId(), e);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        reportItem.setPluginState(PluginState.FAILURE).addPluginDetails(e.getMessage() + "\n");
      }

      try {
        boolean notify = true;
        PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          model, index, null, null, reportItem.getPluginState(), "", notify);
      } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error creating event: {}", e.getMessage(), e);
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    }

    return report;
  }

  @Override
  public Plugin<T> cloneMe() {
    return new PremisSkeletonPlugin<>();
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

}
