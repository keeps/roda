/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.roda.core.common.MetadataFileUtils;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
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
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourceToAIPPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceToAIPPlugin.class);
  private static final String METADATA_TYPE = "key-value";
  private static final String METADATA_VERSION = null;

  public static String UNPACK_DESCRIPTION = "Extracted objects from package in file/folder format.";
  public static String UNPACK_SUCCESS_MESSAGE = "The SIP has been successfully unpacked.";
  public static String UNPACK_FAILURE_MESSAGE = "The ingest process failed to unpack the SIP.";
  public static String UNPACK_PARTIAL_MESSAGE = null;
  public static PreservationEventType UNPACK_EVENT_TYPE = PreservationEventType.UNPACKING;

  public static String WELLFORMED_DESCRIPTION = "Checked that the received SIP is well formed, complete and that no unexpected files were included.";
  public static String WELLFORMED_SUCCESS_MESSAGE = "The SIP was well formed and complete.";
  public static String WELLFORMED_FAILURE_MESSAGE = "The SIP was not well formed or some files were missing.";
  public static String WELLFORMED_PARTIAL_MESSAGE = null;
  public static PreservationEventType WELLFORMED_EVENT_TYPE = PreservationEventType.WELLFORMEDNESS_CHECK;

  private String successMessage;
  private String failureMessage;
  private PreservationEventType eventType;
  private String eventDescription;

  @Override
  public void init() throws PluginException {
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    String parentId = PluginHelper.getParentId(this, index, null);

    for (TransferredResource transferredResource : list) {
      Report reportItem = PluginHelper.createPluginReportItem(this, transferredResource);

      try {
        Path transferredResourcePath = Paths.get(transferredResource.getFullPath());
        LOGGER.debug("Converting {} to AIP", transferredResourcePath);

        boolean active = false;
        Permissions permissions = new Permissions();
        boolean notifyCreatedAIP = false;

        final AIP aip = model.createAIP(active, parentId, permissions, notifyCreatedAIP);
        final String representationId = UUID.randomUUID().toString();
        final boolean original = true;
        boolean notifyRepresentationCreated = false;

        model.createRepresentation(aip.getId(), representationId, original, notifyRepresentationCreated);

        // create files

        if (transferredResource.isFile()) {
          String fileId = transferredResource.getName();
          List<String> directoryPath = new ArrayList<>();
          ContentPayload payload = new FSPathContentPayload(transferredResourcePath);
          boolean notifyFileCreated = false;

          model.createFile(aip.getId(), representationId, directoryPath, fileId, payload, notifyFileCreated);
        } else {
          processTransferredResourceDirectory(model, transferredResourcePath, aip, representationId);
        }
        createUnpackingEventSuccess(model, index, transferredResource, aip);
        ContentPayload metadataPayload = MetadataFileUtils.getMetadataPayload(transferredResource);
        boolean notifyDescriptiveMetadataCreated = false;

        // TODO make the following strings constants
        model.createDescriptiveMetadata(aip.getId(), "metadata.xml", metadataPayload, METADATA_TYPE, METADATA_VERSION,
          notifyDescriptiveMetadataCreated);

        model.notifyAIPCreated(aip.getId());

        reportItem.setItemId(aip.getId()).setPluginState(PluginState.SUCCESS);

        createWellformedEventSuccess(model, index, transferredResource, aip);
        LOGGER.debug("Done with converting {} to AIP {}", transferredResourcePath, aip.getId());
      } catch (Throwable e) {
        LOGGER.error("Error converting " + transferredResource.getId() + " to AIP", e);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());

      }

      report.addReport(reportItem);
      PluginHelper.createJobReport(this, model, reportItem);

    }
    return report;
  }

  private void processTransferredResourceDirectory(ModelService model, Path transferredResourcePath, final AIP aip,
    final String representationId) throws IOException {
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    Files.walkFileTree(transferredResourcePath, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileId = file.getFileName().toString();
        List<String> directoryPath = extractDirectoryPath(transferredResourcePath, file);
        try {
          ContentPayload payload = new FSPathContentPayload(file);
          boolean notifyFileCreated = false;
          model.createFile(aip.getId(), representationId, directoryPath, fileId, payload, notifyFileCreated);
        } catch (RODAException e) {
          // TODO log or mark nothing to do
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new TransferredResourceToAIPPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.SIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return eventType;
  }

  @Override
  public String getPreservationEventDescription() {
    return eventDescription;
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return successMessage;
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return failureMessage;
  }

  public void setPreservationEventType(PreservationEventType t) {
    this.eventType = t;
  }

  public void setPreservationSuccessMessage(String message) {
    this.successMessage = message;
  }

  public void setPreservationFailureMessage(String message) {
    this.failureMessage = message;
  }

  public void setPreservationEventDescription(String description) {
    this.eventDescription = description;
  }

  private void createUnpackingEventSuccess(ModelService model, IndexService index,
    TransferredResource transferredResource, AIP aip) {
    setPreservationEventType(UNPACK_EVENT_TYPE);
    setPreservationSuccessMessage(UNPACK_SUCCESS_MESSAGE);
    setPreservationFailureMessage(UNPACK_FAILURE_MESSAGE);
    setPreservationEventDescription(UNPACK_DESCRIPTION);
    try {
      boolean notify = true;
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, transferredResource, PluginState.SUCCESS, "",
        notify);
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.warn("Error creating unpacking event: " + e.getMessage(), e);
    }
  }

  private void createWellformedEventSuccess(ModelService model, IndexService index,
    TransferredResource transferredResource, AIP aip) {
    setPreservationEventType(WELLFORMED_EVENT_TYPE);
    setPreservationSuccessMessage(WELLFORMED_SUCCESS_MESSAGE);
    setPreservationFailureMessage(WELLFORMED_FAILURE_MESSAGE);
    setPreservationEventDescription(WELLFORMED_DESCRIPTION);
    try {
      boolean notify = true;
      PluginHelper.createPluginEvent(this, aip.getId(), model, index, transferredResource, PluginState.SUCCESS, "",
        notify);
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.warn("Error creating unpacking event: " + e.getMessage(), e);
    }
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
}
