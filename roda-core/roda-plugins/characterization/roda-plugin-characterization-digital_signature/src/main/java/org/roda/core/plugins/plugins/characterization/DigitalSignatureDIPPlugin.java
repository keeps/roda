/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
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
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignatureDIPPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {

  private static Logger LOGGER = LoggerFactory.getLogger(DigitalSignatureDIPPlugin.class);
  private boolean doEmbeddedSignature;

  public DigitalSignatureDIPPlugin() {
    super();
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
    return "Digital sign entities";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Creates a new Dissemination Information Package (DIP) for this AIP containing all the files in a given representation and appends a digital signature to each of these files.\nThe digital signature (in PKCS#7 format) is an external file with the same name as the original one but with a .p7s extension.\nDigital signatures are generated based on the digital certificate installed under “/config/certificates/”.";
  }

  public String getDIPTitle() {
    return "Digital Signature DIP title";
  }

  public String getDIPDescription() {
    return "Digital Signature DIP description";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<T> list)
    throws PluginException {

    if (!list.isEmpty()) {
      if (list.get(0) instanceof AIP) {
        return executeOnAIP(index, model, storage, (List<AIP>) list);
      } else if (list.get(0) instanceof Representation) {
        return executeOnRepresentation(index, model, storage, (List<Representation>) list);
      } else if (list.get(0) instanceof File) {
        return executeOnFile(index, model, storage, (List<File>) list);
      }
    }

    return new Report();
  }

  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (AIP aip : list) {
        // FIXME 20160516 hsilva: see how to set initial
        // initialOutcomeObjectState
        PluginState pluginState = PluginState.SUCCESS;
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);

        for (Representation representation : aip.getRepresentations()) {
          try {
            LOGGER.debug("Processing representation {}", representation);
            boolean recursive = true;
            CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
              representation.getId(), recursive);

            for (OptionalWithCause<File> oFile : allFiles) {
              if (oFile.isPresent()) {
                File file = oFile.get();

                FileLink fileLink = new FileLink(representation.getAipId(), representation.getId(), file.getPath(),
                  file.getId());
                List<FileLink> links = new ArrayList<FileLink>();
                links.add(fileLink);

                DIP dip = new DIP();
                dip.setFileIds(links);
                dip.setPermissions(aip.getPermissions());
                dip.setTitle(getDIPTitle());
                dip.setDescription(getDIPDescription());
                dip = model.createDIP(dip, false);

                manageFileSigning(model, index, storage, file, dip.getId());

                model.notifyDIPCreated(dip, true);
              } else {
                LOGGER.error("Cannot process representation file", oFile.getCause());
              }
            }

            IOUtils.closeQuietly(allFiles);

          } catch (Exception e) {
            LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
            reportItem.setPluginDetails(e.getMessage());
            pluginState = PluginState.FAILURE;
          } finally {
            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
          }
        }

        if (pluginState.equals(PluginState.SUCCESS)) {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        }

        reportItem.setPluginState(pluginState);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    }

    return report;
  }

  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    List<Representation> list) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (Representation representation : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
          Representation.class);

        try {
          // FIXME 20160516 hsilva: see how to set initial
          // initialOutcomeObjectState
          LOGGER.debug("Processing representation {}", representation);
          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
            representation.getId(), recursive);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();

              Permissions aipPermissions = model.retrieveAIP(representation.getAipId()).getPermissions();

              FileLink fileLink = new FileLink(representation.getAipId(), representation.getId(), file.getPath(),
                file.getId());
              List<FileLink> links = new ArrayList<FileLink>();
              links.add(fileLink);

              DIP dip = new DIP();
              dip.setFileIds(links);
              dip.setPermissions(aipPermissions);
              dip.setTitle(getDIPTitle());
              dip.setDescription(getDIPDescription());
              dip = model.createDIP(dip, false);

              manageFileSigning(model, index, storage, file, dip.getId());

              model.notifyDIPCreated(dip, true);
            } else {
              LOGGER.error("Cannot process representation file", oFile.getCause());
            }
          }

          IOUtils.closeQuietly(allFiles);
          reportItem.setPluginState(PluginState.SUCCESS);
          jobPluginInfo.incrementObjectsProcessedWithSuccess();

        } catch (Exception e) {
          LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        } finally {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
        }
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    }

    return report;
  }

  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (File file : list) {
        try {
          Permissions aipPermissions = model.retrieveAIP(file.getAipId()).getPermissions();
          FileLink fileLink = new FileLink(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
          List<FileLink> links = new ArrayList<>();
          links.add(fileLink);

          DIP dip = new DIP();
          dip.setFileIds(links);
          dip.setPermissions(aipPermissions);
          dip.setTitle(getDIPTitle());
          dip.setDescription(getDIPDescription());
          dip = model.createDIP(dip, false);

          // FIXME 20160516 hsilva: see how to set initial
          // initialOutcomeObjectState
          Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class);
          reportItem.setOutcomeObjectId(dip.getId()).setOutcomeObjectClass(DIP.class.getName());
          reportItem.setPluginState(PluginState.SUCCESS);

          try {
            if (!file.isDirectory()) {
              manageFileSigning(model, index, storage, file, dip.getId());
            } else {
              CloseableIterable<OptionalWithCause<File>> fileIterable = model.listFilesUnder(file.getAipId(),
                file.getRepresentationId(), file.getPath(), file.getId(), true);
              for (OptionalWithCause<File> ofileUnder : fileIterable) {
                if (ofileUnder.isPresent()) {
                  File fileUnder = ofileUnder.get();
                  manageFileSigning(model, index, storage, fileUnder, dip.getId());
                }
              }
            }
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (Exception e) {
            LOGGER.error("Error processing File " + file.getId() + ": " + e.getMessage(), e);
            reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
          } finally {
            report.addReport(reportItem);
            PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
            model.notifyDIPCreated(dip, true);
          }
        } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e1) {
          LOGGER.error("Error creating DIP for file " + file.getId());
        }
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    }

    return report;
  }

  private void manageFileSigning(ModelService model, IndexService index, StorageService storage, File file,
    String dipId) throws Exception {
    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
    DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

    if (!file.isDirectory()) {
      LOGGER.debug("Processing file {}", file);
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
      String fileMimetype = ifile.getFileFormat().getMimeType();
      String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

      LOGGER.debug("Running DigitalSignaturePluginDIP on {}", file.getId());

      if (doEmbeddedSignature == true) {
        Path embeddedFile = DigitalSignatureDIPPluginUtils.addEmbeddedSignature(directAccess.getPath(), fileFormat,
          fileMimetype);
        if (embeddedFile != null) {
          ContentPayload payload = new FSPathContentPayload(embeddedFile);
          model.createDIPFile(dipId, file.getPath(), file.getId(), embeddedFile.toFile().length(), payload, false);
        } else {
          addDIPFileSignature(model, storage, dipId, file.getPath(), file.getId(), directAccess.getPath());
        }
      } else {
        addDIPFileSignature(model, storage, dipId, file.getPath(), file.getId(), directAccess.getPath());
      }
    } else {
      ContentPayload payload = new FSPathContentPayload(directAccess.getPath());
      model.createDIPFile(dipId, file.getPath(), file.getId(), 0L, payload, false);
    }

    IOUtils.closeQuietly(directAccess);
  }

  private void addDIPFileSignature(ModelService model, StorageService storage, String dipId, List<String> filePath,
    String fileId, Path inputFile) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    ContentPayload payload = new FSPathContentPayload(inputFile);
    DIPFile dipFile = model.createDIPFile(dipId, filePath, fileId, inputFile.toFile().length(), payload, false);
    DirectResourceAccess dipFileDirectAccess = storage.getDirectAccess(ModelUtils.getDIPFileStoragePath(dipFile));
    DigitalSignatureDIPPluginUtils.addDetachedSignature(dipFileDirectAccess.getPath());
    IOUtils.closeQuietly(dipFileDirectAccess);
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public Plugin<T> cloneMe() {
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
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_DISSEMINATION);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }

}
