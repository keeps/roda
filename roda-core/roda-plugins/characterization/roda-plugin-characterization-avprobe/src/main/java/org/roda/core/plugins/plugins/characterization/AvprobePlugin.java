/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvprobePlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AvprobePlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "AIP feature extraction (avprobe)";
  }

  @Override
  public String getDescription() {
    return "avprobe extracts information from multimedia streams and prints it in human and machine-readable fashion. For example, it can be used to check "
      + "the format of the container used by a multimedia stream and the format and type of each media stream contained in it.\nThe task updates PREMIS "
      + "objects metadata in the Archival Information Package (AIP) to store the results of the characterization process. A PREMIS event is also recorded"
      + " after the task is run.\nFor more information of the avprobe tool, please visit https://libav.org/documentation/avprobe.html";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        processAIP(index, model, storage, report, jobPluginInfo, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, Job job, AIP aip) {
    LOGGER.debug("Processing AIP {}", aip.getId());
    boolean inotify = false;
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);
    PluginState reportState = PluginState.SUCCESS;
    ValidationReport validationReport = new ValidationReport();
    List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

    for (Representation representation : aip.getRepresentations()) {
      LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
      try {
        boolean recursive = true;
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), representation.getId(),
          recursive);
        for (OptionalWithCause<File> oFile : allFiles) {
          if (oFile.isPresent()) {
            File file = oFile.get();
            if (!file.isDirectory()) {
              String fileFormat = file.getId().substring(file.getId().lastIndexOf('.') + 1, file.getId().length());

              sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(), file.getPath(),
                file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

              // TODO check if file is a video
              StoragePath storagePath = ModelUtils.getFileStoragePath(file);
              Binary binary = storage.getBinary(storagePath);

              String probeResults = AvprobePluginUtils.runAvprobe(binary, fileFormat, getParameterValues());
              ContentPayload payload = new StringContentPayload(probeResults);
              model.createOrUpdateOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
                "." + AvprobePluginUtils.AVPROBE_METADATA_FORMAT, RodaConstants.OTHER_METADATA_TYPE_AVPROBE, payload,
                inotify);
            }
          } else {
            LOGGER.error("Cannot process AIP representation file", oFile.getCause());
          }
        }
        IOUtils.closeQuietly(allFiles);
      } catch (RODAException | IOException e) {
        LOGGER.error("Error processing AIP {}: {}", aip.getId(), e.getMessage());
        reportState = PluginState.FAILURE;
        validationReport.addIssue(new ValidationIssue(e.getMessage()));
      }

    }

    try {
      model.notifyAIPUpdated(aip.getId());
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error notifying of AIP update", e);
    }

    if (reportState.equals(PluginState.SUCCESS)) {
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      reportItem.setPluginState(PluginState.SUCCESS);
    } else {
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setHtmlPluginDetails(true).setPluginState(PluginState.FAILURE);
      reportItem.setPluginDetails(validationReport.toHtml(false, false, false, "Error list"));
    }

    try {
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, sources, null, reportItem.getPluginState(), "",
        true);
    } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | AlreadyExistsException e) {
      LOGGER.error("Error creating event: {}", e.getMessage(), e);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AvprobePlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.METADATA_EXTRACTION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
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
