/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.migration;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.xmlbeans.XmlException;
import org.roda.core.RodaCoreFactory;
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
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.CommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public abstract class AbstractConvertPlugin<T extends Serializable> extends AbstractPlugin<T> {

  private static Logger LOGGER = LoggerFactory.getLogger(AbstractConvertPlugin.class);
  private String inputFormat;
  private String outputFormat;

  protected AbstractConvertPlugin() {
    inputFormat = "";
    outputFormat = "";
  }

  public void init() throws PluginException {
    // do nothing
  }

  public void shutdown() {
    // do nothing
  }

  public boolean hasPartialSuccessOnOutcome() {
    return Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("tools", "allplugins", "hasPartialSuccessOnOutcome"));
  }

  public abstract List<String> getApplicableTo();

  public abstract List<String> getConvertableTo();

  public abstract Map<String, List<String>> getPronomToExtension();

  public abstract Map<String, List<String>> getMimetypeToExtension();

  public String getInputFormat() {
    return this.inputFormat;
  }

  public String getOutputFormat() {
    return this.outputFormat;
  }

  public void setInputFormat(String format) {
    this.inputFormat = format;
  }

  public void setOutputFormat(String format) {
    this.outputFormat = format;
  }

  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public List<PluginParameter> getParameters() {
    List<PluginParameter> params = new ArrayList<PluginParameter>();

    PluginParameter outputParam = new PluginParameter("outputParams", "Output parameters", PluginParameterType.STRING,
      "", getConvertableTo(), true, true, "Lists the possible output formats");

    params.add(outputParam);
    return params;
  }

  @Override
  public Map<String, String> getParameterValues() {
    Map<String, String> parametersMap = new HashMap<String, String>();
    parametersMap.put("inputFormat", getInputFormat());
    parametersMap.put("outputFormat", getOutputFormat());
    parametersMap.put("hasPartialSuccessOnOutcome", Boolean.toString(hasPartialSuccessOnOutcome()));
    return parametersMap;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // input image format
    if (parameters.containsKey("inputFormat")) {
      setInputFormat(parameters.get("inputFormat"));
    }

    // output image format
    if (parameters.containsKey("outputFormat")) {
      setOutputFormat(parameters.get("outputFormat"));
    }
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

  private Report executeOnAIP(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);
    String detailExtension = "";

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP " + aip.getId());
      List<String> newRepresentations = new ArrayList<String>();
      String newRepresentationID = null;
      boolean notify = true;

      for (Representation representation : aip.getRepresentations()) {
        List<File> alteredFiles = new ArrayList<File>();
        List<File> newFiles = new ArrayList<File>();
        List<File> unchangedFiles = new ArrayList<File>();
        newRepresentationID = UUID.randomUUID().toString();
        int pluginResultState = 1;
        ReportItem reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);

        try {
          LOGGER.debug("Processing representation: " + representation);
          boolean recursive = true;
          CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), representation.getId(), recursive);

          for (File file : allFiles) {
            LOGGER.debug("Processing file: " + file);

            if (!file.isDirectory()) {
              IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
              String fileMimetype = ifile.getFileFormat().getMimeType();
              String filePronom = ifile.getFileFormat().getPronom();
              String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());
              List<String> applicableTo = getApplicableTo();
              List<String> convertableTo = getConvertableTo();
              Map<String, List<String>> pronomToExtension = getPronomToExtension();
              Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();

              if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo, pronomToExtension,
                mimetypeToExtension)) {

                fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo, pronomToExtension,
                  mimetypeToExtension);

                StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

                // FIXME file that doesn't get deleted afterwards
                LOGGER.debug("Running a ConvertPlugin (" + fileFormat + " to " + outputFormat + ") on " + file.getId());
                try {
                  Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
                  String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

                  ContentPayload payload = new FSPathContentPayload(pluginResult);

                  // create a new representation if it does not exist
                  if (!newRepresentations.contains(newRepresentationID)) {
                    LOGGER.debug("Creating a new representation " + newRepresentationID + " on AIP " + aip.getId());
                    boolean original = false;
                    newRepresentations.add(newRepresentationID);
                    model.createRepresentation(aip.getId(), newRepresentationID, original, notify);
                  }

                  String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                  File f = model.createFile(aip.getId(), newRepresentationID, file.getPath(), newFileId, payload,
                    notify);
                  alteredFiles.add(file);
                  newFiles.add(f);
                  IOUtils.closeQuietly(directAccess);

                  reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
                    new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.SUCCESS.toString()),
                    new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, result));

                } catch (CommandException e) {
                  detailExtension += file.getId() + ": " + e.getOutput();
                  pluginResultState = 2;

                  reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
                    new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.PARTIAL_SUCCESS.toString()),

                  new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));

                  LOGGER.debug("Conversion (" + fileFormat + " to " + outputFormat + ") failed on file " + file.getId()
                    + " of representation " + representation.getId() + " from AIP " + aip.getId());
                }

              } else {
                unchangedFiles.add(file);
              }
            }
          }
          IOUtils.closeQuietly(allFiles);

          // add unchanged files to the new representation if created
          if (alteredFiles.size() > 0) {
            createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, notify);
          }

        } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
          | AuthorizationDeniedException | IOException | AlreadyExistsException e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          pluginResultState = 0;

          reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
            new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.FAILURE.toString()),
            new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        }

        LOGGER.debug("Creating convert plugin event for the representation " + representation.getId());
        boolean notifyEvent = false;
        createEvent(alteredFiles, newFiles, aip.getId(), newRepresentationID, model, outputFormat, pluginResultState,
          detailExtension, notifyEvent);
        report.addItem(reportItem);
      }

      try {
        for (String repId : newRepresentations) {
          boolean inotify = false;
          AbstractConvertPluginUtils.reIndexingRepresentationAfterConversion(index, model, storage, aip.getId(), repId,
            inotify);
        }

        model.notifyAIPUpdated(aip.getId());
      } catch (Exception e) {
        LOGGER.debug("Error re-indexing new representation " + newRepresentationID);
      }

    }

    return report;
  }

  private Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    List<Representation> list) throws PluginException {

    List<String> newRepresentations = new ArrayList<String>();
    String aipId = null;
    Report report = PluginHelper.createPluginReport(this);
    String detailExtension = "";

    for (Representation representation : list) {
      List<File> unchangedFiles = new ArrayList<File>();
      String newRepresentationID = UUID.randomUUID().toString();
      List<File> alteredFiles = new ArrayList<File>();
      List<File> newFiles = new ArrayList<File>();
      aipId = representation.getAipId();
      int pluginResultState = 1;
      boolean notify = true;
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);

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
            List<String> applicableTo = getApplicableTo();
            List<String> convertableTo = getConvertableTo();
            Map<String, List<String>> pronomToExtension = getPronomToExtension();
            Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();

            if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo, pronomToExtension,
              mimetypeToExtension)) {

              fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo, pronomToExtension,
                mimetypeToExtension);

              StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
              DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

              // FIXME file that doesn't get deleted afterwards
              LOGGER.debug("Running a ConvertPlugin (" + fileFormat + " to " + outputFormat + ") on " + file.getId());
              try {
                Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
                String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);
                ContentPayload payload = new FSPathContentPayload(pluginResult);

                if (!newRepresentations.contains(newRepresentationID)) {
                  LOGGER.debug("Creating a new representation " + newRepresentationID + " on AIP " + aipId);
                  boolean original = false;
                  newRepresentations.add(newRepresentationID);
                  model.createRepresentation(aipId, newRepresentationID, original, notify);
                }

                String newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
                File newFile = model.createFile(aipId, newRepresentationID, file.getPath(), newFileId, payload, notify);
                alteredFiles.add(file);
                newFiles.add(newFile);
                IOUtils.closeQuietly(directAccess);

                reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
                  new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.SUCCESS.toString()),
                  new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, result));

              } catch (CommandException e) {
                detailExtension += file.getId() + ": " + e.getOutput();
                pluginResultState = 2;

                reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
                  new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.PARTIAL_SUCCESS.toString()),
                  new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));

                LOGGER.debug("Conversion (" + fileFormat + " to " + outputFormat + ") failed on file " + file.getId()
                  + " of representation " + representation.getId() + " from AIP " + representation.getAipId());
              }
            } else {
              unchangedFiles.add(file);
            }
          }
        }
        IOUtils.closeQuietly(allFiles);
        report.addItem(reportItem);

        // add unchanged files to the new representation
        if (alteredFiles.size() > 0) {
          createNewFilesOnRepresentation(storage, model, unchangedFiles, newRepresentationID, notify);

          boolean notifyReindex = false;
          AbstractConvertPluginUtils.reIndexingRepresentationAfterConversion(index, model, storage, aipId,
            newRepresentationID, notifyReindex);
        }

      } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | IOException | AlreadyExistsException | ValidationException
        | InvalidParameterException | SAXException | TikaException | XmlException e) {
        LOGGER.error("Error processing Representation " + representation.getId() + ": " + e.getMessage(), e);
        pluginResultState = 0;

        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, representation.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.FAILURE.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
        report.addItem(reportItem);
      }

      LOGGER.debug("Creating convert plugin event for the representation " + representation.getId());
      boolean notifyEvent = false;
      createEvent(alteredFiles, newFiles, aipId, newRepresentationID, model, outputFormat, pluginResultState,
        detailExtension, notifyEvent);
    }

    try {
      model.notifyAIPUpdated(aipId);
    } catch (RODAException e) {
      LOGGER.error("Error running creating agent for AbstractConvertPlugin", e);
    }

    return report;
  }

  private Report executeOnFile(IndexService index, ModelService model, StorageService storage, List<File> list)
    throws PluginException {

    int pluginResultState = 1;
    Set<String> aipSet = new HashSet<String>();
    boolean notify = true;
    String newRepresentationID = null;
    String newFileId = null;
    ArrayList<File> newFiles = new ArrayList<File>();
    String detailExtension = "";
    Report report = PluginHelper.createPluginReport(this);
    ReportItem reportItem = null;

    for (File file : list) {
      try {
        LOGGER.debug("Processing file " + file.getId());

        newRepresentationID = UUID.randomUUID().toString();
        reportItem = PluginHelper.createPluginReportItem(this, file.getId(), null);

        if (!file.isDirectory()) {
          IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
          String fileMimetype = ifile.getFileFormat().getMimeType();
          String filePronom = ifile.getFileFormat().getPronom();
          String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1);
          List<String> applicableTo = getApplicableTo();
          List<String> convertableTo = getConvertableTo();
          Map<String, List<String>> pronomToExtension = getPronomToExtension();
          Map<String, List<String>> mimetypeToExtension = getMimetypeToExtension();

          if (doPluginExecute(fileFormat, filePronom, fileMimetype, applicableTo, convertableTo, pronomToExtension,
            mimetypeToExtension)) {

            fileFormat = getNewFileFormat(fileFormat, filePronom, fileMimetype, applicableTo, pronomToExtension,
              mimetypeToExtension);

            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
            DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);

            // FIXME file that doesn't get deleted afterwards
            LOGGER.debug("Running a ConvertPlugin (" + fileFormat + " to " + outputFormat + ") on " + file.getId());
            try {
              Path pluginResult = Files.createTempFile("converted", "." + getOutputFormat());
              String result = executePlugin(directAccess.getPath(), pluginResult, fileFormat);

              ContentPayload payload = new FSPathContentPayload(pluginResult);
              StoragePath storagePath = ModelUtils.getRepresentationStoragePath(file.getAipId(),
                file.getRepresentationId());

              // create a new representation if it does not exist
              LOGGER.debug("Creating a new representation " + newRepresentationID + " on AIP " + file.getAipId());
              boolean original = false;
              model.createRepresentation(file.getAipId(), newRepresentationID, original, model.getStorage(),
                storagePath);

              // update file on new representation
              newFileId = file.getId().replaceFirst("[.][^.]+$", "." + outputFormat);
              model.deleteFile(file.getAipId(), newRepresentationID, file.getPath(), file.getId(), notify);
              File f = model.createFile(file.getAipId(), newRepresentationID, file.getPath(), newFileId, payload,
                notify);
              newFiles.add(f);
              aipSet.add(file.getAipId());

              reportItem = PluginHelper.setPluginReportItemInfo(reportItem, file.getId(),
                new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.SUCCESS.toString()),
                new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, result));

            } catch (CommandException e) {
              detailExtension += file.getId() + ": " + e.getOutput();
              pluginResultState = 2;

              reportItem = PluginHelper.setPluginReportItemInfo(reportItem, file.getId(),
                new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.PARTIAL_SUCCESS.toString()),
                new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));

              LOGGER.debug("Conversion (" + fileFormat + " to " + outputFormat + ") failed on file " + file.getId()
                + " of representation " + file.getRepresentationId() + " from AIP " + file.getAipId());
            }
          }
        }

      } catch (RuntimeException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | ValidationException | IOException | AlreadyExistsException e) {
        LOGGER.error("Error processing File " + file.getId() + ": " + e.getMessage(), e);
        pluginResultState = 0;

        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, file.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, PluginState.FAILURE.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));
      }

      boolean notifyEvent = false;
      createEvent(Arrays.asList(file), newFiles, file.getAipId(), newRepresentationID, model, outputFormat,
        pluginResultState, detailExtension, notifyEvent);
      report.addItem(reportItem);
    }

    try {
      AbstractConvertPluginUtils.reIndexPlugins(model, aipSet);
    } catch (Throwable e) {
      LOGGER.debug("Error re-indexing AIPs after conversion.");
    }

    return report;
  }

  public abstract String executePlugin(Path inputPath, Path outputPath, String fileFormat)
    throws UnsupportedOperationException, IOException, CommandException;

  private void createEvent(List<File> alteredFiles, List<File> newFiles, String aipId, String newRepresentationID,
    ModelService model, String outputFormat, int pluginResultState, String detailExtension, boolean notify)
      throws PluginException {

    List<String> premisSourceFilesIdentifiers = new ArrayList<String>();
    List<String> premisTargetFilesIdentifiers = new ArrayList<String>();

    // building the detail for the plugin event
    String outcome = "success";
    StringBuilder stringBuilder = new StringBuilder();

    if (alteredFiles.size() == 0) {
      stringBuilder
        .append("No file was successfully converted on this representation due to plugin or command line issues.");
    } else {
      for (File file : alteredFiles)
        premisSourceFilesIdentifiers
          .add(IdUtils.getLinkingIdentifierId(aipId, file.getRepresentationId(), file.getPath(), file.getId()));

      for (File file : newFiles)
        premisTargetFilesIdentifiers
          .add(IdUtils.getLinkingIdentifierId(aipId, file.getRepresentationId(), file.getPath(), file.getId()));

      stringBuilder.append("The source files were converted to a new format (." + outputFormat + ")");
    }

    // Conversion plugin did not run correctly
    if (pluginResultState == 0 || (pluginResultState == 2 && hasPartialSuccessOnOutcome() == false)) {
      outcome = "failure";
      stringBuilder.setLength(0);
    }

    // some files were not converted
    if (pluginResultState == 2 && hasPartialSuccessOnOutcome() == true) {
      outcome = "partial success";
    }

    // FIXME revise PREMIS generation
    try {
      PluginHelper.createPluginEvent(this, aipId, null, null, null, model,
        RodaConstants.PRESERVATION_EVENT_TYPE_MIGRATION,
        "Some files may have been format converted on a new representation", premisSourceFilesIdentifiers,
        premisTargetFilesIdentifiers, outcome, stringBuilder.toString(), detailExtension, notify);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
      | ValidationException | AlreadyExistsException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  private boolean doPluginExecute(String fileFormat, String filePronom, String fileMimetype, List<String> applicableTo,
    List<String> convertableTo, Map<String, List<String>> pronomToExtension,
    Map<String, List<String>> mimetypeToExtension) {
    if (((!getInputFormat().isEmpty() && fileFormat.equalsIgnoreCase(getInputFormat())) || (getInputFormat().isEmpty()))
      && (applicableTo.size() == 0 || (filePronom != null && pronomToExtension.containsKey(filePronom))
        || (fileMimetype != null && mimetypeToExtension.containsKey(fileMimetype))
        || (applicableTo.contains(fileFormat)))
      && (convertableTo.size() == 0 || convertableTo.contains(outputFormat)))
      return true;
    else
      return false;
  }

  private String getNewFileFormat(String fileFormat, String filePronom, String fileMimetype, List<String> applicableTo,
    Map<String, List<String>> pronomToExtension, Map<String, List<String>> mimetypeToExtension) {

    if (applicableTo.size() > 0) {
      if (filePronom != null && !filePronom.isEmpty() && pronomToExtension.get(filePronom) != null
        && !pronomToExtension.get(filePronom).contains(fileFormat)) {
        fileFormat = pronomToExtension.get(filePronom).get(0);
      } else if (fileMimetype != null && !fileMimetype.isEmpty() && mimetypeToExtension.get(fileMimetype) != null
        && !mimetypeToExtension.get(fileMimetype).contains(fileFormat)) {
        fileFormat = mimetypeToExtension.get(fileMimetype).get(0);
      }
    }

    return fileFormat;
  }

  private void createNewFilesOnRepresentation(StorageService storage, ModelService model, List<File> unchangedFiles,
    String newRepresentationID, boolean notify) throws RequestNotValidException, GenericException, NotFoundException,
      AuthorizationDeniedException, UnsupportedOperationException, IOException, AlreadyExistsException {

    for (File f : unchangedFiles) {
      StoragePath fileStoragePath = ModelUtils.getFileStoragePath(f);
      Binary binary = storage.getBinary(fileStoragePath);
      Path uriPath = Paths.get(binary.getContent().getURI());
      ContentPayload payload = new FSPathContentPayload(uriPath);
      model.createFile(f.getAipId(), newRepresentationID, f.getPath(), f.getId(), payload, notify);
    }
  }

  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    return new Report();
  }

}
