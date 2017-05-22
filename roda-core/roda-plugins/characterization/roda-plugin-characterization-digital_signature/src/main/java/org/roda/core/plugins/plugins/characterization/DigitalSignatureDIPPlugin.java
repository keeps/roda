/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.bouncycastle.cms.CMSException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
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
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractAIPComponentsPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;

public class DigitalSignatureDIPPlugin<T extends IsRODAObject> extends AbstractAIPComponentsPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalSignatureDIPPlugin.class);
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
    return "Creates a new Dissemination Information Package (DIP) for this AIP containing all the files in a given representation and "
      + "appends a digital signature to each of these files.\nThe digital signature (in PKCS#7 format) is an external file with the"
      + " same name as the original one but with a .p7s extension.\nDigital signatures are generated based on the digital certificate "
      + "installed under “/config/certificates/”.";
  }

  private String getDIPTitle() {
    return "Digital signed dissemination";
  }

  private String getDIPDescription() {
    return "Digital signed dissemination of a file, possibly embedded";
  }

  @Override
  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException {

    for (AIP aip : list) {
      PluginState pluginState = PluginState.SUCCESS;
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);

      for (Representation representation : aip.getRepresentations()) {
        String dipId = IdUtils.createUUID();

        try {
          RepresentationLink representationLink = new RepresentationLink(representation.getAipId(),
            representation.getId());
          List<RepresentationLink> links = new ArrayList<>();
          links.add(representationLink);

          DIP dip = new DIP();
          dip.setId(dipId);
          dip.setRepresentationIds(links);
          dip.setPermissions(aip.getPermissions());
          dip.setTitle(getDIPTitle());
          dip.setDescription(getDIPDescription());
          dip.setType(RodaConstants.DIP_TYPE_DIGITAL_SIGNATURE);
          dip = model.createDIP(dip, false);

          LOGGER.debug("Processing representation {}", representation);
          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
            representation.getId(), recursive);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent() && !oFile.get().isDirectory()) {
              manageFileSigning(model, index, storage, oFile.get(), dip.getId());
            } else {
              LOGGER.error("Cannot process representation file", oFile.getCause());
            }
          }

          IOUtils.closeQuietly(allFiles);
          model.notifyDIPCreated(dip, true);

        } catch (Exception | LinkageError e) {
          LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
          reportItem.setPluginDetails(e.getMessage());
          pluginState = PluginState.FAILURE;
          try {
            model.deleteDIP(dipId);
          } catch (GenericException | NotFoundException | AuthorizationDeniedException e1) {
            // do nothing
          }
        } finally {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
        }
      }

      if (pluginState.equals(PluginState.SUCCESS)) {
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      } else {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      }

      reportItem.setPluginState(pluginState);
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    }

    return report;
  }

  @Override
  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException {

    for (Representation representation : list) {
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class);
      String dipId = IdUtils.createUUID();

      try {
        Permissions aipPermissions = model.retrieveAIP(representation.getAipId()).getPermissions();

        RepresentationLink representationLink = new RepresentationLink(representation.getAipId(),
          representation.getId());
        List<RepresentationLink> links = new ArrayList<>();
        links.add(representationLink);

        DIP dip = new DIP();
        dip.setId(dipId);
        dip.setRepresentationIds(links);
        dip.setPermissions(aipPermissions);
        dip.setTitle(getDIPTitle());
        dip.setDescription(getDIPDescription());
        dip.setType(RodaConstants.DIP_TYPE_DIGITAL_SIGNATURE);
        dip = model.createDIP(dip, false);

        LOGGER.debug("Processing representation {}", representation);
        boolean recursive = true;
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
          representation.getId(), recursive);

        for (OptionalWithCause<File> oFile : allFiles) {
          if (oFile.isPresent() && !oFile.get().isDirectory()) {
            manageFileSigning(model, index, storage, oFile.get(), dip.getId());
          } else {
            LOGGER.error("Cannot process representation file", oFile.getCause());
          }
        }

        model.notifyDIPCreated(dip, true);
        IOUtils.closeQuietly(allFiles);
        reportItem.setPluginState(PluginState.SUCCESS);
        jobPluginInfo.incrementObjectsProcessedWithSuccess();

      } catch (Exception | LinkageError e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        try {
          model.deleteDIP(dipId);
        } catch (GenericException | NotFoundException | AuthorizationDeniedException e1) {
          // do nothing
        }
      } finally {
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }

    return report;
  }

  @Override
  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException {

    for (File file : list) {
      String dipId = IdUtils.createUUID();

      try {
        Permissions aipPermissions = model.retrieveAIP(file.getAipId()).getPermissions();
        FileLink fileLink = new FileLink(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
        List<FileLink> links = new ArrayList<>();
        links.add(fileLink);

        DIP dip = new DIP();
        dip.setId(dipId);
        dip.setFileIds(links);
        dip.setPermissions(aipPermissions);
        dip.setTitle(getDIPTitle());
        dip.setDescription(getDIPDescription());
        dip.setType(RodaConstants.DIP_TYPE_DIGITAL_SIGNATURE);
        dip = model.createDIP(dip, false);

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
          model.notifyDIPCreated(dip, true);
        } catch (Exception | LinkageError e) {
          LOGGER.error("Error processing File " + file.getId() + ": " + e.getMessage(), e);
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          model.deleteDIP(dipId);
        } finally {
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
        }
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e1) {
        LOGGER.error("Error creating DIP for file " + file.getId());
      }
    }

    return report;
  }

  private void manageFileSigning(ModelService model, IndexService index, StorageService storage, File file,
    String dipId) throws NotFoundException, GenericException, InvalidFormatException, RequestNotValidException,
    AuthorizationDeniedException, IOException, GeneralSecurityException, DocumentException, XMLSignatureException,
    MarshalException, AlreadyExistsException, CMSException {
    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
    DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

    if (!file.isDirectory()) {
      LOGGER.debug("Processing file {}", file);
      IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file),
        RodaConstants.FILE_FORMAT_FIELDS_TO_RETURN);
      String fileMimetype = ifile.getFileFormat().getMimeType();
      String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

      LOGGER.debug("Running DigitalSignaturePluginDIP on {}", file.getId());

      if (doEmbeddedSignature) {
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
    String fileId, Path inputFile)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException, IOException, GeneralSecurityException, DocumentException, CMSException {
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
    return new DigitalSignatureDIPPlugin<>();
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

}
