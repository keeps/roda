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
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourceToAIPPlugin implements Plugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceToAIPPlugin.class);

  private Map<String, String> parameters;

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Uploaded file/directory";
  }

  @Override
  public String getDescription() {
    return "Understands a file/directory as an SIP";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    String jobDefinedParentId = PluginHelper.getParentIdFromParameters(parameters);

    for (TransferredResource transferredResource : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(transferredResource, this);

      try {
        Path transferredResourcePath = Paths.get(transferredResource.getFullPath());

        boolean active = false;
        String parentId = jobDefinedParentId;
        AIPPermissions permissions = new AIPPermissions();
        boolean notify = true;

        final AIP aip = model.createAIP(active, parentId, permissions, notify);

        final String representationId = UUID.randomUUID().toString();
        final boolean original = true;

        // TODO create descriptive metadata and representations via model
        // TODO update AIP metadata

        model.createRepresentation(aip.getId(), representationId, original, false);

        PluginHelper.createDirectories(model, aip.getId(), representationId);

        // create files

        if (transferredResource.isFile()) {
          String fileId = transferredResource.getName();
          List<String> directoryPath = new ArrayList<>();
          ContentPayload payload = new FSPathContentPayload(transferredResourcePath);

          model.createFile(aip.getId(), representationId, directoryPath, fileId, payload);
        } else {
          EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
          Files.walkFileTree(transferredResourcePath, opts, Integer.MAX_VALUE, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
              try {
                Path relativePath = transferredResourcePath.relativize(file);

                String fileId = file.getFileName().toString();
                List<String> directoryPath = new ArrayList<>();
                for (int i = 0; i < relativePath.getNameCount() - 1; i++) {
                  directoryPath.add(relativePath.getName(i).toString());
                }

                ContentPayload payload = new FSPathContentPayload(transferredResourcePath);

                model.createFile(aip.getId(), representationId, directoryPath, fileId, payload);
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

        StringBuilder b = new StringBuilder();
        b.append("<metadata>");
        b.append("<field name='title'>" + StringEscapeUtils.escapeXml(transferredResource.getName()) + "</field>");
        b.append("</metadata>");

        ContentPayload metadataPayload = new StringContentPayload(b.toString());

        // TODO make the following strings constants
        model.createDescriptiveMetadata(aip.getId(), "metadata.xml", metadataPayload, "key-value");

        state = PluginState.SUCCESS;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aip.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

      } catch (Throwable e) {
        LOGGER.error("Error converting " + transferredResource.getId() + " to AIP", e);
        state = PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, null,
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
      }

      report.addItem(reportItem);
      PluginHelper.createJobReport(model, this, reportItem, state, PluginHelper.getJobId(parameters));

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

}
