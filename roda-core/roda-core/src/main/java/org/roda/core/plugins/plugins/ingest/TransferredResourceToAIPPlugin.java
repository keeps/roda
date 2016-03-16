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

import org.apache.commons.lang.StringEscapeUtils;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourceToAIPPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceToAIPPlugin.class);
  private static final String METADATA_TYPE = "key-value";
  private static final String METADATA_VERSION = null;

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

        boolean active = false;
        AIPPermissions permissions = new AIPPermissions();
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

        ContentPayload metadataPayload = getMetadataPayload(transferredResource);
        boolean notifyDescriptiveMetadataCreated = false;

        // TODO make the following strings constants
        model.createDescriptiveMetadata(aip.getId(), "metadata.xml", metadataPayload, METADATA_TYPE, METADATA_VERSION,
          notifyDescriptiveMetadataCreated);

        model.notifyAIPCreated(aip.getId());

        reportItem.setItemId(aip.getId()).setPluginState(PluginState.SUCCESS);

        boolean notify = true;
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, transferredResource,
          reportItem.getPluginState(), "", notify);
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
          File createdFile = model.createFile(aip.getId(), representationId, directoryPath, fileId, payload,
            notifyFileCreated);
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

  private ContentPayload getMetadataPayload(TransferredResource transferredResource) {
    StringBuilder b = new StringBuilder();
    b.append("<metadata>");
    b.append("<field name='title'>" + StringEscapeUtils.escapeXml(transferredResource.getName()) + "</field>");
    b.append("</metadata>");

    return new StringContentPayload(b.toString());
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
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

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
    return PreservationEventType.UNPACKING;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Extracted objects from package in file/folder format.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The SIP has been successfuly unpacked.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The ingest process failed to unpack the SIP.";
  }

}
