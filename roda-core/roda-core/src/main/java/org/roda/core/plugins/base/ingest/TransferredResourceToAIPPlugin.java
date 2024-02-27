/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.common.MetadataFileUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourceToAIPPlugin extends SIPToAIPPlugin {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceToAIPPlugin.class);

  private static final String METADATA_TYPE = "key-value";
  private static final String METADATA_VERSION = null;
  private static final String METADATA_FILE = "metadata.xml";
  private static final String UNPACK_DESCRIPTION = "Extracted objects from package in file/folder format.";

  private boolean createSubmission = false;
  private Optional<String> computedSearchScope;

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
    return "Uploaded file/folder";
  }

  @Override
  public String getDescription() {
    return "Treats a file/folder as a SIP.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_CREATE_SUBMISSION)) {
      createSubmission = Boolean.parseBoolean(getParameterValues().get(RodaConstants.PLUGIN_PARAMS_CREATE_SUBMISSION));
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    computedSearchScope = PluginHelper.getSearchScopeFromParameters(this, model);

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<TransferredResource>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<TransferredResource> plugin, TransferredResource object) {
        processTransferredResource(index, model, report, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processTransferredResource(IndexService index, ModelService model, Report report, Job job,
    TransferredResource transferredResource) {
    Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);

    try {
      Path transferredResourcePath = Paths.get(FilenameUtils.normalize(transferredResource.getFullPath()));
      LOGGER.debug("Converting {} to AIP", transferredResourcePath);
      AIPState state = AIPState.INGEST_PROCESSING;
      String aipType = RodaConstants.AIP_TYPE_MIXED;
      Permissions permissions = new Permissions();

      final AIP aip = model.createAIP(state, computedSearchScope.orElse(null), aipType, permissions,
        transferredResource.getUUID(), Arrays.asList(transferredResource.getName()), job.getId(), false,
        job.getUsername());

      PluginHelper.createSubmission(model, createSubmission, transferredResourcePath, aip.getId());

      final String representationId = IdUtils.createUUID();
      String representationType = RodaConstants.REPRESENTATION_TYPE_MIXED;
      model.createRepresentation(aip.getId(), representationId, true, representationType, false, job.getUsername());

      // create files
      if (transferredResource.isFile()) {
        String fileId = transferredResource.getName();
        List<String> directoryPath = new ArrayList<>();
        ContentPayload payload = new FSPathContentPayload(transferredResourcePath);
        model.createFile(aip.getId(), representationId, directoryPath, fileId, payload, job.getUsername(), false);
      } else {
        processTransferredResourceDirectory(model, transferredResourcePath, aip, representationId, job.getUsername());
      }

      createUnpackingEventSuccess(model, index, transferredResource, aip, UNPACK_DESCRIPTION, job);
      ContentPayload metadataPayload = MetadataFileUtils.getMetadataPayload(transferredResource);
      model.createDescriptiveMetadata(aip.getId(), METADATA_FILE, metadataPayload, METADATA_TYPE, METADATA_VERSION,
        job.getUsername(), false);

      // FIXME 20160516 hsilva: put "SIP" inside the AIP???

      model.notifyAipCreated(aip.getId());

      reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(), aip.getId())
        .setPluginState(PluginState.SUCCESS);
      createWellformedEventSuccess(model, index, transferredResource, aip, job);
      LOGGER.debug("Done with converting {} to AIP {}", transferredResourcePath, aip.getId());
    } catch (RODAException | IOException | RuntimeException e) {
      LOGGER.error("Error converting " + transferredResource.getId() + " to AIP", e);
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
    }

    report.addReport(reportItem);
    PluginHelper.createJobReport(this, model, reportItem, job);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  private void processTransferredResourceDirectory(ModelService model, Path transferredResourcePath, final AIP aip,
    final String representationId, String username) throws IOException {
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    Files.walkFileTree(transferredResourcePath, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String fileId = file.getFileName().toString();
        List<String> directoryPath = extractDirectoryPath(transferredResourcePath, file);
        try {
          ContentPayload payload = new FSPathContentPayload(file);
          boolean notifyFileCreated = false;
          model.createFile(aip.getId(), representationId, directoryPath, fileId, payload, username, notifyFileCreated);
        } catch (RODAException e) {
          LOGGER.error("Could not create file on {}", file.toString(), e);
        }

        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private List<String> extractDirectoryPath(Path transferredResourcePath, Path file) {
    List<String> directoryPath = new ArrayList<>();
    Path relativePath = transferredResourcePath.relativize(file);
    for (int i = 0; i < relativePath.getNameCount() - 1; i++) {
      directoryPath.add(relativePath.getName(i).toString());
    }
    return directoryPath;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new TransferredResourceToAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Arrays.asList(TransferredResource.class);
  }

}
