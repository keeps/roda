/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeraPDFPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VeraPDFPlugin.class);
  private String profile;
  private boolean ignoreFiles = true;
  private boolean hasFeatures = false;
  private boolean hasPartialSuccessOnOutcome;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_PDF_PROFILE, "PDF Profile", PluginParameterType.STRING, "1b",
      VeraPDFPluginUtils.getProfileList(), true, false,
      "Validates files according to the specified PDF/A profile (valid options include: 1a, 1b, 2a, 2b, 2u, 3a, 3b, 3u)"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES, "Ignore non PDF files",
        PluginParameterType.BOOLEAN, "true", false, false, "Ignore files that are not identified as PDF."));
  }

  public VeraPDFPlugin() {
    super();
    profile = "1b";
    hasFeatures = Boolean
      .parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools", "verapdf", "hasFeatures"));
    hasPartialSuccessOnOutcome = Boolean.parseBoolean(RodaCoreFactory.getRodaConfigurationAsString("core", "tools",
      "convert", "allplugins", "hasPartialSuccessOnOutcome"));
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
    return "PDF/A format validation (VeraPDF)";
  }

  @Override
  public String getDescription() {
    return "This action validates PDF files to make sure they comply to the PDF/A specification.\nPDF/A is an ISO-standardized version of the Portable "
      + "Document Format (PDF) specialized for use in the archiving and long-term preservation of electronic documents. PDF/A differs from PDF by "
      + "prohibiting features ill-suited to long-term archiving, such as font linking (as opposed to font embedding) and encryption.\nThe specification "
      + "for PDF/A is a set of restrictions and requirements applied to the “base” PDF standards (PDF 1.4 for PDF/A-1 and ISO 32000 for PDF/A-2 and PDF/A-3) "
      + "plus a specific set of 3rd party standards. \nThe outcome of this action is the creation of a new technical metadata file in the Archival "
      + "Information Package (under the folder “metadata/other”) that records the output of the VeraPDF tool. A PREMIS event is also recorded after running "
      + "this task.\nFor more information about VeraPDF, please visit http://verapdf.org";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE)) {
      profile = parameters.get(RodaConstants.PLUGIN_PARAMS_PDF_PROFILE);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES)) {
      ignoreFiles = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_IGNORE_OTHER_FILES));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, liteList.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      List<T> list = PluginHelper.transformLitesIntoObjects(model, index, this, report, jobPluginInfo, liteList);

      if (!list.isEmpty()) {
        if (list.get(0) instanceof AIP) {
          report = executeOnAIP(index, model, storage, report, jobPluginInfo, (List<AIP>) list);
        } else if (list.get(0) instanceof Representation) {
          report = executeOnRepresentation(index, model, storage, report, jobPluginInfo, (List<Representation>) list);
        } else if (list.get(0) instanceof File) {
          report = executeOnFile(index, model, storage, report, jobPluginInfo, (List<File>) list);
        }
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  public Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list) throws PluginException {

    for (AIP aip : list) {
      LOGGER.debug("Processing AIP {}", aip.getId());
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.INGEST_PROCESSING);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
      PluginState pluginResultState = PluginState.SUCCESS;
      PluginState reportState = PluginState.SUCCESS;
      ValidationReport validationReport = new ValidationReport();
      boolean hasNonPdfFiles = false;
      List<File> resourceList = new ArrayList<File>();
      List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();

      for (Representation representation : aip.getRepresentations()) {
        // FIXME 20160516 hsilva: see how to set initial
        // initialOutcomeObjectState

        try {
          LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());

          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
            representation.getId(), recursive);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              LOGGER.debug("Processing file: {}", file);
              if (!file.isDirectory()) {
                IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
                String fileMimetype = ifile.getFileFormat().getMimeType();
                String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());
                String fileInfoPath = StringUtils.join(Arrays.asList(aip.getId(), representation.getId(),
                  StringUtils.join(file.getPath(), '/'), file.getId()), '/');

                if ("pdf".equalsIgnoreCase(fileFormat) || "application/pdf".equals(fileMimetype)) {
                  LOGGER.debug("Running veraPDF validator on {}", file.getId());
                  StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                  DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
                  Pair<StringContentPayload, Boolean> veraPDFResult = VeraPDFPluginUtils
                    .runVeraPDF(directAccess.getPath(), profile, hasFeatures);
                  sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(), file.getPath(),
                    file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

                  if (veraPDFResult.getFirst() != null) {
                    model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
                      ".xml", "VeraPDF", veraPDFResult.getFirst(), false);
                  }

                  if (veraPDFResult.getFirst() == null || !veraPDFResult.getSecond().booleanValue()) {
                    resourceList.add(file);
                    pluginResultState = PluginState.PARTIAL_SUCCESS;
                    reportItem.addPluginDetails(
                      "VeraPDF validation failed on " + fileInfoPath.replace("//", "/").replace("/[]/", "/") + ".\n");
                  }

                } else {
                  if (ignoreFiles) {
                    ValidationIssue issue = new ValidationIssue(
                      StringUtils.join(Arrays.asList(representation.getId(), file.getPath(), file.getId()), '/'));
                    validationReport.addIssue(issue);
                  } else {
                    pluginResultState = PluginState.FAILURE;
                    hasNonPdfFiles = true;
                  }
                }

              }
            } else {
              LOGGER.error("Cannot process AIP representation file", oFile.getCause());
            }
          }

          IOUtils.closeQuietly(allFiles);
          if (!pluginResultState.equals(PluginState.SUCCESS)) {
            reportState = PluginState.FAILURE;
          }

        } catch (GenericException | RuntimeException | NotFoundException | RequestNotValidException
          | AuthorizationDeniedException e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          pluginResultState = PluginState.FAILURE;
          reportState = PluginState.FAILURE;
          reportItem.addPluginDetails(
            "VeraPDF validation execution failed on representation " + representation.getId() + ".\n");
        }
      }

      LOGGER.debug("Creating veraPDF event on AIP {}", aip.getId());
      createEvent(model, index, aip.getId(), null, null, null, pluginResultState, "", resourceList, sources);

      jobPluginInfo.incrementObjectsProcessed(reportState);
      reportItem.setPluginState(reportState);

      if (reportState.equals(PluginState.SUCCESS)) {
        if (ignoreFiles && !validationReport.getIssues().isEmpty()) {
          reportItem.setHtmlPluginDetails(true)
            .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
        }
      }

      if (hasNonPdfFiles) {
        reportItem.setPluginDetails("Non PDF files were not ignored");
      }

      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
    }

    return report;
  }

  public Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<Representation> list) throws PluginException {

    try {
      for (Representation representation : list) {
        List<File> resourceList = new ArrayList<File>();
        List<LinkingIdentifier> sources = new ArrayList<LinkingIdentifier>();
        // FIXME 20160516 hsilva: see how to set initial
        // initialOutcomeObjectState
        Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
          Representation.class, AIPState.INGEST_PROCESSING);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
        PluginState pluginResultState = PluginState.SUCCESS;
        PluginState reportState = PluginState.SUCCESS;
        AIP aip = model.retrieveAIP(representation.getAipId());
        ValidationReport validationReport = new ValidationReport();
        boolean hasNonPdfFiles = false;

        try {
          LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());

          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
            representation.getId(), recursive);

          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              LOGGER.debug("Processing file: {}", file);
              if (!file.isDirectory()) {
                IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
                String fileMimetype = ifile.getFileFormat().getMimeType();
                String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());
                String fileInfoPath = StringUtils.join(
                  Arrays.asList(representation.getId(), StringUtils.join(file.getPath(), '/'), file.getId()), '/');

                if ("pdf".equalsIgnoreCase(fileFormat) || "application/pdf".equals(fileMimetype)) {
                  LOGGER.debug("Running veraPDF validator on {}", file.getId());
                  StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
                  DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
                  Pair<StringContentPayload, Boolean> veraPDFResult = VeraPDFPluginUtils
                    .runVeraPDF(directAccess.getPath(), profile, hasFeatures);
                  sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), representation.getId(), file.getPath(),
                    file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

                  if (veraPDFResult.getFirst() != null) {
                    model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
                      ".xml", "VeraPDF", veraPDFResult.getFirst(), false);
                  }

                  if (veraPDFResult.getFirst() == null || !veraPDFResult.getSecond().booleanValue()) {
                    resourceList.add(file);
                    pluginResultState = PluginState.PARTIAL_SUCCESS;
                    reportItem.addPluginDetails(
                      "VeraPDF validation failed on " + fileInfoPath.replace("//", "/").replace("/[]/", "/") + ".\n");
                  }

                  IOUtils.closeQuietly(directAccess);

                } else {
                  if (ignoreFiles) {
                    validationReport.addIssue(new ValidationIssue(file.getId()));
                  } else {
                    reportState = PluginState.FAILURE;
                    hasNonPdfFiles = true;
                  }
                }

              }
            } else {
              LOGGER.error("Cannot process AIP representation file", oFile.getCause());
            }
          }

          IOUtils.closeQuietly(allFiles);
          if (!pluginResultState.equals(PluginState.SUCCESS)) {
            reportState = PluginState.FAILURE;
          }

        } catch (GenericException | RuntimeException e) {
          LOGGER.error("Error processing AIP " + aip.getId() + ": " + e.getMessage(), e);
          pluginResultState = PluginState.FAILURE;
          reportState = PluginState.FAILURE;
          reportItem.setPluginDetails(e.getMessage());
        } finally {
          LOGGER.debug("Creating veraPDF event for the representation {}", representation.getId());
          createEvent(model, index, aip.getId(), representation.getId(), null, null, pluginResultState, "",
            resourceList, sources);
        }

        jobPluginInfo.incrementObjectsProcessed(reportState);
        reportItem.setPluginState(reportState);

        if (reportState.equals(PluginState.SUCCESS)) {
          if (ignoreFiles && !validationReport.getIssues().isEmpty()) {
            reportItem.setHtmlPluginDetails(true)
              .setPluginDetails(validationReport.toHtml(false, false, false, "Ignored files"));
          }
        }

        if (hasNonPdfFiles) {
          reportItem.setPluginDetails("Non PDF files were not ignored");
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
      }

    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error retrieving aip on VeraPDF representation validation");
    }

    return report;
  }

  public Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<File> list) throws PluginException {

    for (File file : list) {
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class,
        AIPState.INGEST_PROCESSING);
      PluginState reportState = PluginState.SUCCESS;
      String details = "";

      try {
        List<File> resourceList = new ArrayList<File>();
        // FIXME 20160516 hsilva: see how to set initial
        // initialOutcomeObjectState
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
        PluginState pluginResultState = PluginState.SUCCESS;

        LOGGER.debug("Processing file: {}", file);
        if (!file.isDirectory()) {
          IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
          String fileMimetype = ifile.getFileFormat().getMimeType();
          String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.') + 1, ifile.getId().length());

          if ("pdf".equalsIgnoreCase(fileFormat) || "application/pdf".equals(fileMimetype)) {
            LOGGER.debug("Running veraPDF validator on {}", file.getId());
            StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
            DirectResourceAccess directAccess = storage.getDirectAccess(fileStoragePath);
            Pair<StringContentPayload, Boolean> veraPDFResult = VeraPDFPluginUtils.runVeraPDF(directAccess.getPath(),
              profile, hasFeatures);

            if (veraPDFResult.getFirst() != null) {
              model.createOtherMetadata(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
                ".xml", "VeraPDF", veraPDFResult.getFirst(), false);

              if (!veraPDFResult.getSecond().booleanValue()) {
                resourceList.add(file);
                pluginResultState = PluginState.PARTIAL_SUCCESS;
                reportItem.addPluginDetails("VeraPDF validation failed on " + file.getId().replace("//", "/") + ".\n");
                details = veraPDFResult.getFirst().toString();
              }

            } else {
              pluginResultState = PluginState.PARTIAL_SUCCESS;
              reportItem.addPluginDetails("VeraPDF validation failed on " + file.getId().replace("//", "/") + ".\n");
            }

            IOUtils.closeQuietly(directAccess);
          } else {
            if (reportState.equals(PluginState.SUCCESS)) {
              if (ignoreFiles) {
                reportItem.setPluginDetails("This file was ignored.");
              } else {
                pluginResultState = PluginState.FAILURE;
                reportItem.setPluginDetails("This file format is not supported.");
              }
            }
          }
        }

        if (!pluginResultState.equals(PluginState.SUCCESS)) {
          reportState = PluginState.FAILURE;
        }

        createEvent(model, index, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          pluginResultState, details, resourceList, null);
        jobPluginInfo.incrementObjectsProcessed(reportState);

      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | IllegalArgumentException e) {
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        LOGGER.error("Could not run VeraPDF successfully");
        reportState = PluginState.FAILURE;
        reportItem.setPluginDetails(e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } finally {
        reportItem.setPluginState(reportState);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
      }
    }

    return report;
  }

  private void createEvent(ModelService model, IndexService index, String aipId, String representationId,
    List<String> filePath, String fileId, PluginState pluginState, String details, List<File> resourceList,
    List<LinkingIdentifier> sources) throws PluginException {
    String outcomeDetails = null;
    try {
      // building the detail extension for the plugin event
      StringBuilder noteStringBuilder = new StringBuilder();
      noteStringBuilder.append("The following files did not pass veraPDF's validation with success: ");

      for (File file : resourceList) {
        noteStringBuilder.append(file.getId() + ", ");
      }

      noteStringBuilder.setLength(noteStringBuilder.length() - 2);

      if (StringUtils.isNotBlank(details)) {
        StringBuilder detailsStringBuilder = new StringBuilder();
        detailsStringBuilder.append("\n\n<reportWrapper>");
        detailsStringBuilder.append(details);
        detailsStringBuilder.append("</reportWrapper>");
        noteStringBuilder.append(detailsStringBuilder);
      }

      // all file have passed the validation
      if (pluginState == PluginState.SUCCESS)
        noteStringBuilder.setLength(0);

      // veraPDF plugin did not run correctly
      if (pluginState == PluginState.FAILURE
        || (pluginState == PluginState.PARTIAL_SUCCESS && hasPartialSuccessOnOutcome == false)) {
        pluginState = PluginState.FAILURE;
        noteStringBuilder.setLength(0);
      }

      outcomeDetails = noteStringBuilder.toString();
    } catch (RuntimeException e) {
      pluginState = PluginState.FAILURE;
      outcomeDetails = e.getMessage();
      LOGGER.error("Error executing VeraPDF plugin: " + e.getMessage(), e);
    }

    try {
      PluginHelper.createPluginEvent(this, aipId, representationId, filePath, fileId, model, index, sources, null,
        pluginState, outcomeDetails, true);
    } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
      | ValidationException | AlreadyExistsException e) {
      LOGGER.error("Error creating event: " + e.getMessage(), e);
    }

  }

  @Override
  public Plugin<T> cloneMe() {
    return new VeraPDFPlugin<T>();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.FORMAT_VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked if PDF files were veraPDF valid.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "PDF files were veraPDF validated.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to validate the PDF files with veraPDF.";
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_CHARACTERIZATION);
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
