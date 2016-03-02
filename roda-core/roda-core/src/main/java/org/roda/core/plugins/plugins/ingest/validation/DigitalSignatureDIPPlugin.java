/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.plugins.plugins.ingest.migration.AbstractConvertPluginUtils;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignatureDIPPlugin extends AbstractPlugin<Representation> {

  private static Logger LOGGER = LoggerFactory.getLogger(DigitalSignatureDIPPlugin.class);
  private static final String OTHER_METADATA_TYPE = "DigitalSignatureDIP";

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
    return "Validation of digital signature";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Sign files on a DIP.";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Representation> list)
    throws PluginException {
    List<String> newRepresentations = new ArrayList<String>();
    String aipId = null;
    Report report = PluginHelper.createPluginReport(this);

    for (Representation representation : list) {
      String newRepresentationID = UUID.randomUUID().toString();
      aipId = representation.getAipId();
      boolean notify = true;
      Report reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);

      try {
        LOGGER.debug("Processing representation: " + representation);
        boolean recursive = true;
        CloseableIterable<File> allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(),
          recursive);
        Path representationZipFile = Files.createTempFile("rep", ".zip");

        LOGGER.debug("Creating a new representation " + newRepresentationID + " on AIP " + aipId);
        newRepresentations.add(newRepresentationID);
        model.createRepresentation(aipId, newRepresentationID, false, notify);
        List<String> filePath = null;

        for (File file : allFiles) {
          LOGGER.debug("Processing file: " + file);

          if (!file.isDirectory()) {
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
            DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

            LOGGER.debug("Running DigitalSignaturePluginDIP on " + file.getId());
            DigitalSignatureDIPPluginUtils.addElementToRepresentationZip(representationZipFile, directAccess.getPath(),
              (int) ifile.getSize());

            IOUtils.closeQuietly(directAccess);

            if (filePath == null)
              filePath = file.getPath();
          }
        }

        // add zip file on a new representation
        Path resultZipFile = DigitalSignatureDIPPluginUtils.runDigitalSigner(representationZipFile);
        ContentPayload payload = new FSPathContentPayload(resultZipFile);
        String newFileId = representation.getId().replaceFirst("[.][^.]+$", ".zip");
        model.createFile(aipId, newRepresentationID, filePath, newFileId, payload, notify);

        IOUtils.closeQuietly(allFiles);
        AbstractConvertPluginUtils.reIndexingRepresentationAfterConversion(index, model, storage, aipId,
          newRepresentationID, false);

        reportItem.setPluginState(PluginState.SUCCESS);

      } catch (Throwable e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      }

      report.addReport(reportItem);
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error notifying update of AIP on DigitalSignaturePlugin ", e);
    }

    return report;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public Plugin<Representation> cloneMe() {
    return new DigitalSignatureDIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.DIGITAL_SIGNATURE_VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Digitally signed files on a DIP.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The files were successfully signed.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to digitally sign some files.";
  }

}
