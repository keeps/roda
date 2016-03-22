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
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
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
    Report report = PluginHelper.createPluginReport(this);

    for (Representation representation : list) {
      String newRepresentationID = UUID.randomUUID().toString();
      aipId = representation.getAipId();
      boolean notify = true;
      Report reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);

      try {

        Format format = new Format();
        format.setName("Portable Document Format");
        format.setDefinition("PDF definition");
        format.setCategory("Page Layout Files");
        format.setLatestVersion("1.7");
        format.setPopularity(4);
        format.setDeveloper("Adobe Systems");
        format.setInitialRelease(new Date());
        format.setStandard("ISO 32000-1");
        format.setOpenFormat(true);
        format.setWebsite("https://www.adobe.com/devnet/pdf/pdf_reference_archive.html");
        format.setProvenanceInformation("https://en.wikipedia.org/wiki/Portable_Document_Format");

        List<String> extensions2 = new ArrayList<String>();
        extensions2.add(".pdf");
        format.setExtensions(extensions2);

        List<String> mimetypes2 = new ArrayList<String>();
        mimetypes2.add("application/pdf");
        mimetypes2.add("application/x-pdf");
        format.setMimetypes(mimetypes2);

        List<String> pronoms2 = new ArrayList<String>();
        pronoms2.add("fmt/100");
        pronoms2.add("fmt/226");
        format.setPronoms(pronoms2);

        List<String> utis2 = new ArrayList<String>();
        utis2.add("com.adobe.pdf");
        format.setUtis(utis2);

        model.createFormat(format, true);

        Agent agent = new Agent();
        agent.setName("Acrobat reader");
        agent.setType("Software");
        agent.setDescription("Agent description");
        agent.setCategory("Desktop publishing");
        agent.setVersion("1.7");
        agent.setLicense("Proprietary");
        agent.setPopularity(5);
        agent.setDeveloper("Adobe Systems");
        agent.setInitialRelease(new Date());
        agent.setWebsite("acrobat.adobe.com");
        agent.setDownload("https://get.adobe.com/br/reader/");
        agent.setProvenanceInformation("https://en.wikipedia.org/wiki/Adobe_Acrobat");

        List<String> platforms = new ArrayList<String>();
        platforms.add("Windows");
        platforms.add("MAC OS X");
        platforms.add("Linux");
        agent.setPlatforms(platforms);

        List<String> extensions = new ArrayList<String>();
        extensions.add(".pdf");
        agent.setExtensions(extensions);

        List<String> mimetypes = new ArrayList<String>();
        mimetypes.add("application/pdf");
        mimetypes.add("application/x-pdf");
        agent.setMimetypes(mimetypes);

        List<String> pronoms = new ArrayList<String>();
        pronoms.add("fmt/100");
        pronoms.add("fmt/226");
        agent.setPronoms(pronoms);

        List<String> utis = new ArrayList<String>();
        utis.add("com.adobe.pdf");
        agent.setUtis(utis);

        List<String> formatIds = new ArrayList<String>();
        formatIds.add(format.getId());
        agent.setFormatIds(formatIds);

        model.createAgent(agent, true);

        LOGGER.debug("Processing representation: " + representation);
        boolean recursive = true;
        CloseableIterable<File> allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(),
          recursive);

        newRepresentations.add(newRepresentationID);
        model.createRepresentation(aipId, newRepresentationID, false, notify);
        List<String> filePath = null;
        Path resultFile = null;
        List<File> fileList = IteratorUtils.toList(allFiles.iterator());
        int countFiles = fileList.size();
        String newFileId = representation.getId() + ".zip";

        if (countFiles > 1) {
          Path representationZipFile = Files.createTempFile("rep", ".zip");
          OutputStream os = new FileOutputStream(representationZipFile.toString());
          ZipOutputStream zout = new ZipOutputStream(os);

          for (File file : fileList) {
            LOGGER.debug("Processing file: " + file);
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);

            if (!file.isDirectory()) {
              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
              LOGGER.debug("Running DigitalSignaturePluginDIP on " + file.getId());

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
          }

          zout.finish();
          IOUtils.closeQuietly(zout);
          IOUtils.closeQuietly(os);
          resultFile = DigitalSignatureDIPPluginUtils.runZipDigitalSigner(representationZipFile);

        } else if (countFiles == 1) {
          File file = fileList.get(0);
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
        }

        // add zip file (or single file) on a new representation
        LOGGER.debug("Running digital signer on representation");
        ContentPayload payload = new FSPathContentPayload(resultFile);
        model.createFile(aipId, newRepresentationID, filePath, newFileId, payload, notify);

        IOUtils.closeQuietly(allFiles);
        reportItem.setPluginState(PluginState.SUCCESS);
        AbstractConvertPluginUtils.reIndexingRepresentationAfterConversion(this, index, model, storage, aipId,
          newRepresentationID);

      } catch (Throwable e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      }

      report.addReport(reportItem);
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error notifying update of AIP on DigitalSignatureDIPPlugin ", e);
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
