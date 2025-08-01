/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.ExportType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExportAIPPlugin.class);

  public static final String PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER = "outputFolder";
  public static final String PLUGIN_PARAM_EXPORT_TYPE = "exportType";
  public static final String PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS = "removeIfAlreadyExists";

  private String outputFolder;
  private ExportType exportType;
  private boolean removeIfAlreadyExists;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER,
      PluginParameter.getBuilder(PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER, "Destination folder", PluginParameterType.STRING)
        .withDefaultValue("/tmp/export").withDescription("Folder where the exported AIPs will be stored.").build());

    pluginParameters.put(PLUGIN_PARAM_EXPORT_TYPE,
      PluginParameter.getBuilder(PLUGIN_PARAM_EXPORT_TYPE, "Type of export", PluginParameterType.STRING)
        .withDefaultValue("FOLDER")
        .withDescription("Type of export: ZIP – exports each AIP as a ZIP file; FOLDER – exports each AIP as a folder.")
        .build());

    pluginParameters.put(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS,
      PluginParameter
        .getBuilder(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS, "Overwrite files/folders",
          PluginParameterType.BOOLEAN)
        .withDefaultValue("true")
        .withDescription("Overwrites files and folders if they already exist on the destination folder.").build());
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
    return "AIP Batch Exporter";
  }

  @Override
  public String getDescription() {
    return "The AIP Batch Exporter is a powerful plugin that allows you to select a group of AIPs and export them as a single ZIP file or folder. "
      + "The outcome is saved on the server file system, which can be accessed by users with the necessary permissions.\nThis plugin is particularly "
      + "useful when you need to export a large number of AIPs based on specific search criteria. With just a few clicks, you can create a batch"
      + "export of AIPs and download them in a compressed format.\nThis saves time and effort and ensures that your AIPs are stored and transferred efficiently.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER));
    parameters.add(pluginParameters.get(PLUGIN_PARAM_EXPORT_TYPE));
    parameters.add(pluginParameters.get(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER)) {
      outputFolder = parameters.get(PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER);
    }

    if (parameters.containsKey(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS)) {
      removeIfAlreadyExists = Boolean
        .parseBoolean(getParameterValues().get(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS));
    }

    if (parameters.containsKey(PLUGIN_PARAM_EXPORT_TYPE)) {
      try {
        exportType = ExportType.valueOf(parameters.get(PLUGIN_PARAM_EXPORT_TYPE));
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        exportType = ExportType.FOLDER;
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<AIP>() {

      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<AIP> plugin, List<AIP> aips) {
        Path outputPath = Paths.get(FilenameUtils.normalize(outputFolder));
        String error = null;
        try {
          if (!FSUtils.exists(outputPath)) {
            Files.createDirectories(outputPath);
          }
          if (!Files.isWritable(outputPath)) {
            error = "No permissions to write to " + outputPath.toString();
          }
        } catch (IOException e) {
          LOGGER.error("Error creating base folder: {}", e.getMessage(), e);
          error = e.getMessage();
        }

        if (error == null && exportType == ExportType.ZIP) {
          report = exportMultiZip(aips, outputPath, report, model, index, jobPluginInfo, cachedJob);
        } else if (error == null && exportType == ExportType.FOLDER) {
          report = exportFolders(aips, model, index, report, jobPluginInfo, cachedJob);
        } else if (error != null) {
          jobPluginInfo.incrementObjectsProcessedWithFailure(aips.size());
          report.setCompletionPercentage(100);
          report.setPluginState(PluginState.FAILURE);
          report.setPluginDetails("Error exporting AIPs: " + error);
        }
      }
    }, index, model, liteList);

  }

  private Report exportFolders(List<AIP> aips, ModelService model, IndexService index,
    Report report, JobPluginInfo jobPluginInfo, Job job) {
    try {
      FileStorageService localStorage = new FileStorageService(Paths.get(FilenameUtils.normalize(outputFolder)), false,
        null, false);
      for (AIP aip : aips) {
        LOGGER.debug("Exporting AIP {} to folder", aip.getId());
        String error = null;
        try {
          model.exportObject(aip, localStorage, aip.getId());
        } catch (AlreadyExistsException e) {
          if (removeIfAlreadyExists) {
            try {
              localStorage.deleteResource(DefaultStoragePath.parse(aip.getId()));
              model.exportObject(aip, localStorage, aip.getId());
            } catch (AlreadyExistsException e2) {
              error = "Error removing/creating folder for AIP " + aip.getId();
            }
          } else {
            error = "Folder for AIP " + aip.getId() + " already exists.";
          }
        }

        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
        if (error != null) {
          reportItem.setPluginState(PluginState.FAILURE)
            .setPluginDetails("Export AIP did not end successfully: " + error);
          jobPluginInfo.incrementObjectsProcessedWithFailure();
        } else {
          reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails("Export AIP ended successfully");
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

        try {
          boolean notify = true;
          PluginHelper.createPluginEvent(this, aip.getId(), model, index, reportItem.getPluginState(), "", notify, job);
        } catch (ValidationException | RequestNotValidException | NotFoundException | GenericException
          | AuthorizationDeniedException | AlreadyExistsException e) {
          LOGGER.error("Error creating event: " + e.getMessage(), e);
        }
      }
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error(e.getMessage(), e);
    }
    return report;
  }

  private Report exportMultiZip(List<AIP> aips, Path outputPath, Report report, ModelService model, IndexService index,
    JobPluginInfo jobPluginInfo, Job job) {
    for (AIP aip : aips) {
      LOGGER.debug("Exporting AIP {} to ZIP", aip.getId());
      OutputStream os = null;
      String error = null;
      try {
        Path zip = outputPath.resolve(aip.getId() + ".zip");
        if (FSUtils.exists(zip) && removeIfAlreadyExists) {
          Files.delete(zip);
        } else if (FSUtils.exists(zip) && !removeIfAlreadyExists) {
          error = "File " + zip.toString() + " already exists";
        }
        if (error == null) {
          os = Files.newOutputStream(zip, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

          ConsumesOutputStream cos = model.exportObjectToStream(aip);
          cos.consumeOutputStream(os);
        }
      } catch (Exception e) {
        LOGGER.error("Error exporting AIP {}: {}", aip.getId(), e.getMessage(), e);
        error = e.getMessage();
      } finally {
        if (os != null) {
          IOUtils.closeQuietly(os);
        }
      }

      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
      if (error != null) {
        reportItem.setPluginState(PluginState.FAILURE)
          .setPluginDetails("Export AIP did not end successfully: " + error);
        jobPluginInfo.incrementObjectsProcessedWithFailure();
      } else {
        reportItem.setPluginState(PluginState.SUCCESS).setPluginDetails("Export AIP ended successfully");
        jobPluginInfo.incrementObjectsProcessedWithSuccess();
      }
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    }
    return report;
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    return new Report();
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new ExportAIPPlugin();
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
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Exports AIPS to a local folder";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The AIPs were successfully exported";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The AIPs were not exported";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT, RodaConstants.PLUGIN_CATEGORY_MAINTENANCE);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
