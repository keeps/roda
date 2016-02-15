package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DigitalSignaturePlugin implements Plugin<Representation> {

  private static Logger LOGGER = LoggerFactory.getLogger(DigitalSignaturePlugin.class);
  private boolean doVerify;
  private boolean doExtract;
  private boolean doStrip;
  private boolean hasPartialSuccessOnOutcome;
  private List<String> applicableTo;
  private List<String> convertableTo;
  private Map<String, List<String>> pronomToExtension;
  private Map<String, List<String>> mimetypeToExtension;

  public static final String FILE_SUFFIX = ".txt";
  public static final String OTHER_METADATA_TYPE = "DigitalSignature";

  public DigitalSignaturePlugin() {
    doVerify = true;
    doExtract = true;
    doStrip = true;

    applicableTo = new ArrayList<>();
    convertableTo = new ArrayList<>();
    pronomToExtension = new HashMap<>();
    mimetypeToExtension = new HashMap<>();

    hasPartialSuccessOnOutcome = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  public boolean hasPartialSuccessOnOutcome() {
    return Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  public List<String> getApplicableTo() {
    return Arrays.asList("pdf");
  }

  public List<String> getConvertableTo() {
    return Arrays.asList("pdf");
  }

  public Map<String, List<String>> getPronomToExtension() {
    return PdfToPdfaPluginUtils.getPronomToExtension();
  }

  public Map<String, List<String>> getMimetypeToExtension() {
    Map<String, List<String>> mimes = new HashMap<String, List<String>>();
    mimes.put("application/pdf", Arrays.asList("pdf"));
    return mimes;
  }

  public boolean getDoVerify() {
    return doVerify;
  }

  public void setDoVerify(boolean verify) {
    doVerify = verify;
  }

  public boolean getDoExtract() {
    return doExtract;
  }

  public void setDoExtract(boolean extract) {
    doExtract = extract;
  }

  public boolean getDoStrip() {
    return doStrip;
  }

  public void getDoStrip(boolean strip) {
    doStrip = strip;
  }

  @Override
  public String getName() {
    return "Digital signature handler";
  }

  @Override
  public String getAgentType() {
    return RodaConstants.PRESERVATION_AGENT_TYPE_SOFTWARE;
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Plugin that can verify, extract and strip documents digital signature";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return null;
  }

  @Override
  public Map<String, String> getParameterValues() {
    return null;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // do the digital signature verification
    if (parameters.containsKey("doVerify")) {
      doVerify = Boolean.parseBoolean(parameters.get("doVerify"));
    }

    // do the digital signature information extraction
    if (parameters.containsKey("doExtract")) {
      doExtract = Boolean.parseBoolean(parameters.get("doExtract"));
    }

    // do the digital signature strip
    if (parameters.containsKey("doStrip")) {
      doStrip = Boolean.parseBoolean(parameters.get("doStrip"));
    }

  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Representation> list)
    throws PluginException {

    IndexedPreservationAgent agent = null;
    try {
      // Agent is detached from AIP
      boolean notify = true;
      agent = PremisUtils.createPremisAgentBinary(this, model, notify);
    } catch (AlreadyExistsException e) {
      agent = PremisUtils.getPreservationAgent(this, model);
    } catch (RODAException e) {
      LOGGER.error("Error running adding DigitalSignature plugin: " + e.getMessage(), e);
    }

    List<String> newRepresentations = new ArrayList<String>();
    String aipId = null;
    Report report = PluginHelper.createPluginReport(this);

    for (Representation representation : list) {
      List<File> unchangedFiles = new ArrayList<File>();
      String newRepresentationID = UUID.randomUUID().toString();
      List<File> alteredFiles = new ArrayList<File>();
      List<File> newFiles = new ArrayList<File>();
      aipId = representation.getAipId();
      int pluginResultState = 1;
      boolean notify = true;
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "Digital Signature treatment",
        representation.getId(), null);

      try {
        LOGGER.debug("Processing representation: " + representation);

        boolean recursive = true;
        CloseableIterable<File> allFiles = model.listFilesUnder(representation.getAipId(), representation.getId(),
          recursive);

        for (File file : allFiles) {
          LOGGER.debug("Processing file: " + file);

          if (!file.isDirectory()) {
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String filePronom = ifile.getFileFormat().getPronom();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);
            Map<String, List<String>> pronomExtensions = getPronomToExtension();

            if (((filePronom != null && pronomExtensions.containsKey(filePronom))
              || (fileMimetype != null && getMimetypeToExtension().containsKey(fileMimetype))
              || (getApplicableTo().contains(fileFormat)))) {

              if (applicableTo.size() > 0) {
                if (filePronom != null && !filePronom.isEmpty() && pronomToExtension.get(filePronom) != null
                  && !pronomToExtension.get(filePronom).contains(fileFormat)) {
                  fileFormat = pronomToExtension.get(filePronom).get(0);
                } else if (fileMimetype != null && !fileMimetype.isEmpty()
                  && mimetypeToExtension.get(fileMimetype) != null
                  && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
                  fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
                }
              }

              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

              // FIXME file that doesn't get deleted afterwards
              LOGGER.debug("Running DigitalSignaturePlugin on " + file.getId());

              if (doVerify) {
                LOGGER.debug("Verying digital signatures on " + file.getId());
                System.err
                  .println(DigitalSignaturePluginUtils.runDigitalSignatureVerify(directAccess.getPath(), fileFormat));
              }

              if (doExtract) {
                LOGGER.debug("Extracting digital signatures information of " + file.getId());

                Path extractResult = DigitalSignaturePluginUtils.runDigitalSignatureExtract(directAccess.getPath(),
                  fileFormat);
                ContentPayload payload = new FSPathContentPayload(extractResult);

                model.createOtherMetadata(representation.getAipId(), representation.getId(), file.getPath(),
                  file.getId().substring(0, file.getId().lastIndexOf('.')), DigitalSignaturePlugin.FILE_SUFFIX,
                  DigitalSignaturePlugin.OTHER_METADATA_TYPE, payload, true);
              }

              if (doStrip) {
                LOGGER.debug("Stripping digital signatures from " + file.getId());

                if (!representation.isOriginal()) {
                  newRepresentationID = representation.getId();
                  newRepresentations.add(representation.getId());
                  StoragePath representationPreservationPath = ModelUtils
                    .getRepresentationPreservationMetadataStoragePath(representation.getAipId(),
                      representation.getId());
                  storage.deleteResource(representationPreservationPath);
                }

                Path pluginResult = DigitalSignaturePluginUtils.runDigitalSignatureStrip(directAccess.getPath(),
                  fileFormat);

                if (pluginResult != null) {
                  ContentPayload payload = new FSPathContentPayload(pluginResult);

                  if (!newRepresentations.contains(newRepresentationID)) {
                    LOGGER.debug("Creating a new representation " + newRepresentationID + " on AIP " + aipId);
                    boolean original = false;
                    newRepresentations.add(newRepresentationID);
                    model.createRepresentation(aipId, newRepresentationID, original, notify);

                  }

                  // update file on new representation
                  if (!representation.isOriginal()) {
                    model.deleteFile(representation.getAipId(), newRepresentationID, file.getPath(), file.getId(),
                      notify);

                  }

                  // update file on new representation
                  String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + fileFormat);
                  File f = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload, notify);
                  alteredFiles.add(file);
                  newFiles.add(f);
                  IOUtils.closeQuietly(directAccess);

                  reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
                    new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.SUCCESS.toString()));

                } else {

                  LOGGER.debug("Process failed on file " + file.getId() + " of representation " + representation.getId()
                    + " from AIP " + aipId);
                  pluginResultState = 2;

                  reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
                    new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.PARTIAL_SUCCESS.toString()));

                }
              }
            } else {
              unchangedFiles.add(file);
            }
          }
        }
        IOUtils.closeQuietly(allFiles);

        // add unchanged files to the new representation
        if (alteredFiles.size() > 0) {
          for (File f : unchangedFiles) {
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
            Binary binary = storage.getBinary(fileStoragePath);
            Path uriPath = Paths.get(binary.getContent().getURI());
            ContentPayload payload = new FSPathContentPayload(uriPath);
            model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
          }

          if (!representation.isOriginal()) {
            model.deleteRepresentation(aipId, representation.getId());
          }

          boolean notifyReindex = false;
          AbstractConvertPluginUtils.reIndexingRepresentation(index, model, storage, aipId, newRepresentationID,
            notifyReindex);

          LOGGER.debug("Creating digital signature plugin event for the representation " + representation.getId());
          // boolean notifyEvent = false;
          // createEvent(alteredFiles, newFiles, model.retrieveAIP(aipId),
          // newRepresentationID, model, pluginResultState, agent, notifyEvent);
        }
      } catch (Throwable e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        pluginResultState = 0;

        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.FAILURE.toString()));
      }

      report.addItem(reportItem);
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error running creating agent for DigitalSignaturePlugin ", e);
    }

    return report;
  }

  private void createEventStrip(List<File> alteredFiles, List<File> newFiles, AIP aip, String newRepresentationID,
    ModelService model, int pluginResultState, IndexedPreservationAgent agent, boolean notify) throws PluginException {

    List<String> premisSourceFilesIdentifiers = new ArrayList<String>();
    List<String> premisTargetFilesIdentifiers = new ArrayList<String>();

    // building the detail extension for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();
    if (alteredFiles.size() == 0) {
      stringBuilder.append("No file was converted on this representation.");
    } else {
      stringBuilder.append("The following files were converted to a new format: ");

      for (File file : alteredFiles) {
        stringBuilder.append(file.getId() + ", ");
        premisSourceFilesIdentifiers.add(PremisUtils.createPremisFileIdentifier(file));
      }

      for (File file : newFiles) {
        premisTargetFilesIdentifiers.add(PremisUtils.createPremisFileIdentifier(file));
      }

      stringBuilder.setLength(stringBuilder.length() - 2);
    }

    // Conversion plugin did not run correctly
    if (pluginResultState == 0 || (pluginResultState == 2 && hasPartialSuccessOnOutcome == false)) {
      outcome = "failure";
      stringBuilder.setLength(0);
    }
    // some files were not converted
    if (pluginResultState == 2 && hasPartialSuccessOnOutcome == true) {
      outcome = "partial success";
    }

    // FIXME revise PREMIS generation
    try {
      PluginHelper.createPluginEvent(aip.getId(), newRepresentationID, null, model,
        RodaConstants.PRESERVATION_EVENT_TYPE_MIGRATION, "Some files were converted on a new representation",
        premisSourceFilesIdentifiers, premisTargetFilesIdentifiers, outcome, stringBuilder.toString(), null, agent,
        notify);
    } catch (IOException | RequestNotValidException | NotFoundException | GenericException
      | AuthorizationDeniedException | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
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
    return new DigitalSignaturePlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
