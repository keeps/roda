/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FITSPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FITSPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "AIP feature extraction (FITS)";
  }

  @Override
  public String getDescription() {
    return "The File Information Tool Set (FITS) identifies, validates and extracts technical metadata for a wide range of file formats. "
      + "It acts as a wrapper, invoking and managing the output from several other open source tools. Output from these tools are converted "
      + "into a common format, compared to one another and consolidated into a single XML output file.\nThe tools used in the latest version"
      + " of FITS are: ADL Tool, Apache Tika, DROID, Exiftool, FFIdent, File Utility (windows port), Jhove, MediaInfo, National Library of "
      + "New Zealand Metadata Extractor, OIS Audio Information, OIS File Information, OIS XML Information.\nThe task updates PREMIS objects"
      + " metadata in the Archival Information Package (AIP) to store the results of the characterization process. A PREMIS event is also "
      + "recorded after the task is run.\nMore information on this tool can be found at http://projects.iq.harvard.edu/fits ";
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
      LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());
      CloseableIterable<OptionalWithCause<File>> allFiles = null;
      DirectResourceAccess directAccess = null;
      try {
        StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(aip.getId(),
          representation.getId());
        directAccess = storage.getDirectAccess(representationDataPath);
        Path output = Files.createTempDirectory("output");

        FITSPluginUtils.runFITSOnPath(directAccess.getPath(), output);

        boolean recursive = true;
        allFiles = model.listFilesUnder(aip.getId(), representation.getId(), recursive);

        for (OptionalWithCause<File> oFile : allFiles) {
          if (oFile.isPresent()) {
            File file = oFile.get();
            if (!file.isDirectory() && file.getPath().isEmpty()) {
              try {
                LOGGER.debug("Creating other metadata (AIP: {}, REPRESENTATION: {}, FILE: {})", aip.getId(),
                  representation.getId(), file.getId());

                Path p = output.resolve(file.getId() + ".fits.xml");
                ContentPayload payload = new FSPathContentPayload(p);
                model.createOrUpdateOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(),
                  ".xml", RodaConstants.OTHER_METADATA_TYPE_FITS, payload, inotify);

                sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(), file.getPath(),
                  file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
              } catch (RODAException e) {
                LOGGER.error("Error creating other metadata for file {}", file.getId(), e);
                reportState = PluginState.FAILURE;
                validationReport.addIssue(new ValidationIssue(e.getMessage()));
              }
            }
          } else {
            LOGGER.error("Cannot process AIP representation file", oFile.getCause());
          }
        }

        FSUtils.deletePath(output);
      } catch (RODAException | IOException e) {
        LOGGER.error("Error processing AIP {}: {}", aip.getId(), e.getMessage());
        reportState = PluginState.FAILURE;
        validationReport.addIssue(new ValidationIssue(e.getMessage()));
      } finally {
        IOUtils.closeQuietly(directAccess);
        IOUtils.closeQuietly(allFiles);
      }
    }

    try {
      model.notifyAipUpdated(aip.getId());
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
    return new FITSPlugin();
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
    return "Extracted metadata using FITS";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Extracted metadata using FITS successfully";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Extracted metadata using FITS with failures";
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
