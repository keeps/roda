/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
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

public class ExifToolPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExifToolPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "AIP feature extraction (ExifTool)";
  }

  @Override
  public String getDescription() {
    return "ExifTool is a platform-independent application capable of reading technical metadata from a wide variety of file formats."
      + "\nExifTool supports many different metadata formats including EXIF, GPS, IPTC, XMP, JFIF, GeoTIFF, ICC Profile, Photoshop IRB, "
      + "FlashPix, AFCP and ID3, as well as the maker notes of many digital cameras by Canon, Casio, DJI, FLIR, FujiFilm, GE, HP, JVC/Victor, "
      + "Kodak, Leaf, Minolta/Konica-Minolta, Motorola, Nikon, Nintendo, Olympus/Epson, Panasonic/Leica, Pentax/Asahi, Phase One, Reconyx, Ricoh, "
      + "Samsung, Sanyo, Sigma/Foveon and Sony.\nThe task creates a new file under the [AIP_ID]/representation/metadata/other/ExifTool. This information is not yet added to "
      + "PREMIS or indexed but it can be inspected by downloading the AIP. A PREMIS event is recorded after the task is run."
      + "\nFor a full list of supported file formats and metadata types, please visit http://www.sno.phy.queensu.ca/~phil/exiftool/#supported ";
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

      DirectResourceAccess directAccess = null;
      try {
        StoragePath representationDataPath = ModelUtils.getRepresentationDataStoragePath(aip.getId(),
          representation.getId());
        directAccess = storage.getDirectAccess(representationDataPath);

        sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(),
          RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

        CloseableIterable<OptionalWithCause<org.roda.core.data.v2.ip.File>> allFiles = model.listFilesUnder(aip.getId(),
          representation.getId(), true);

        if (!CloseableIterables.isEmpty(allFiles)) {
          Path metadata = Files.createTempDirectory("metadata");
          ExifToolPluginUtils.runExifToolOnPath(directAccess.getPath(), metadata);

          try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(metadata)) {
            for (Path path : directoryStream) {
              ContentPayload payload = new FSPathContentPayload(path);
              List<String> fileDirectoryPath = new ArrayList<>();

              Path relativePath = metadata.relativize(path);
              for (int i = 0; i < relativePath.getNameCount() - 2; i++) {
                fileDirectoryPath.add(relativePath.getName(i).toString());
              }

              String fileId = path.getFileName().toString();
              model.createOrUpdateOtherMetadata(aip.getId(), representation.getId(), fileDirectoryPath, fileId, ".xml",
                RodaConstants.OTHER_METADATA_TYPE_EXIFTOOL, payload, inotify);
            }
          }

          FSUtils.deletePath(metadata);
        }
      } catch (RODAException | IOException e) {
        LOGGER.error("Error processing AIP {}: {}", aip.getId(), e.getMessage());
        reportState = PluginState.FAILURE;
        validationReport.addIssue(new ValidationIssue(e.getMessage()));
      } finally {
        IOUtils.closeQuietly(directAccess);
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
    return new ExifToolPlugin();
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
    return "Extracted technical metadata from file.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Technical metadata stored under [AIP_ID]/representation/metadata/other/ExifTool";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Could not extract technical metadata";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION, RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
