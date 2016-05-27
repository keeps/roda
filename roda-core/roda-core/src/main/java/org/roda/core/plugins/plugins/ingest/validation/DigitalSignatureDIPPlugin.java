/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIPState;
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
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignatureDIPPlugin extends AbstractPlugin<Representation> {

  private static Logger LOGGER = LoggerFactory.getLogger(DigitalSignatureDIPPlugin.class);
  private boolean doEmbeddedSignature;

  public DigitalSignatureDIPPlugin() {
    doEmbeddedSignature = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "signature", "doEmbeddedSignature"));
  }

  public boolean getDoEmbeddedSignature() {
    return doEmbeddedSignature;
  }

  public void setDoEmbeddedSignature(boolean doEmbedded) {
    doEmbeddedSignature = doEmbedded;
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
    return "Digital sign files on a DIP.";
  }

  @Override
  public String getVersionImpl() {
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
    Report report = PluginHelper.initPluginReport(this);

    for (Representation representation : list) {
      String newRepresentationID = UUID.randomUUID().toString();
      aipId = representation.getAipId();
      boolean notify = true;
      // FIXME 20160516 hsilva: see how to set initial initialOutcomeObjectState
      Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), AIPState.ACTIVE);

      try {
        LOGGER.debug("Processing representation {}", representation);
        boolean recursive = true;
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
          representation.getId(), recursive);

        newRepresentations.add(newRepresentationID);

        model.createRepresentation(aipId, newRepresentationID, false, representation.getType(), notify);
        List<String> filePath = null;
        Path resultFile = null;
        List<OptionalWithCause<File>> fileList = IteratorUtils.toList(allFiles.iterator());
        int countFiles = fileList.size();
        String newFileId = representation.getId() + ".zip";

        if (countFiles > 1) {
          Path representationZipFile = Files.createTempFile("rep", ".zip");
          OutputStream os = new FileOutputStream(representationZipFile.toString());
          ZipOutputStream zout = new ZipOutputStream(os);

          for (OptionalWithCause<File> oFile : fileList) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              LOGGER.debug("Processing file {}", file);
              IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
              String fileMimetype = ifile.getFileFormat().getMimeType();
              String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

              if (!file.isDirectory()) {
                StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
                LOGGER.debug("Running DigitalSignaturePluginDIP on {}", file.getId());

                if (doEmbeddedSignature == true) {
                  Path embeddedFile = DigitalSignatureDIPPluginUtils.addEmbeddedSignature(directAccess.getPath(),
                    fileFormat, fileMimetype);
                  DigitalSignatureDIPPluginUtils.addElementToRepresentationZip(zout, embeddedFile,
                    ifile.getOriginalName());
                } else {
                  DigitalSignatureDIPPluginUtils.addElementToRepresentationZip(zout, directAccess.getPath(),
                    ifile.getOriginalName());
                }

                IOUtils.closeQuietly(directAccess);
                if (filePath == null)
                  filePath = file.getPath();
              }
            } else {
              LOGGER.error("Cannot process representation file", oFile.getCause());
            }
          }

          zout.finish();
          IOUtils.closeQuietly(zout);
          IOUtils.closeQuietly(os);
          resultFile = DigitalSignatureDIPPluginUtils.runZipDigitalSigner(representationZipFile);

        } else if (countFiles == 1) {
          OptionalWithCause<File> oFile = fileList.get(0);
          if (oFile.isPresent()) {
            File file = oFile.get();
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
            DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
            filePath = file.getPath();

            if (doEmbeddedSignature == true) {
              newFileId = file.getId();
              resultFile = DigitalSignatureDIPPluginUtils.addEmbeddedSignature(directAccess.getPath(), fileFormat,
                fileMimetype);
            } else {
              resultFile = DigitalSignatureDIPPluginUtils.runZipDigitalSigner(directAccess.getPath());
            }

            IOUtils.closeQuietly(directAccess);
          } else {
            LOGGER.error("Cannot process representation file", oFile.getCause());
          }
        }

        // add zip file (or single file) on a new representation
        LOGGER.debug("Running digital signer on representation");
        ContentPayload payload = new FSPathContentPayload(resultFile);
        model.createFile(aipId, newRepresentationID, filePath, newFileId, payload, notify);

        IOUtils.closeQuietly(allFiles);
        reportItem.setPluginState(PluginState.SUCCESS);
        // AbstractConvertPluginUtils.reIndexingRepresentationAfterConversion(this,
        // index, model, storage, aipId, newRepresentationID);

      } catch (Throwable e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      } finally {
        report.addReport(reportItem);
      }
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error notifying update of AIP on DigitalSignatureDIPPlugin ", e);
    }

    return report;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

}
