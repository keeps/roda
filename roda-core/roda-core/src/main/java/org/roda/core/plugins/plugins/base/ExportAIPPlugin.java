/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

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

import org.apache.commons.io.IOUtils;
import org.roda.core.common.tools.ZipEntryInfo;
import org.roda.core.common.tools.ZipTools;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.ExportType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
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
      new PluginParameter(PLUGIN_PARAM_EXPORT_FOLDER_PARAMETER, "Output folder", PluginParameterType.STRING,
        "/tmp", true, false, "Folder where the exported AIP will be sent."));
    pluginParameters.put(PLUGIN_PARAM_EXPORT_TYPE, new PluginParameter(PLUGIN_PARAM_EXPORT_TYPE, "Type of export",
      PluginParameterType.STRING, "FOLDER", true, false, "Type of exportation (MULTI_ZIP, FOLDER)"));
    pluginParameters.put(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS,
      new PluginParameter(PLUGIN_PARAM_EXPORT_REMOVE_IF_ALREADY_EXISTS, "Remove if already exists",
        PluginParameterType.BOOLEAN, "true", true, false, "Remove if already exists"));
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
    return "Export AIP(s)";
  }

  @Override
  public String getDescription() {
    return "Exports selected AIP(s) to a ZIP file on the server file system.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
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
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> aips)
    throws PluginException {
    OutputStream os = null;
    try {
      SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, aips.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      if (exportType == ExportType.MULTI_ZIP) {
        for (AIP aip : aips) {
          LOGGER.debug("Exporting AIP {}", aip.getId());
          try {
            List<ZipEntryInfo> zipEntries = ModelUtils.aipToZipEntry(aip);
            Path outputPath = Paths.get(outputFolder);
            Path zip = outputPath.resolve(aip.getId() + ".zip");
            if (Files.exists(zip) && removeIfAlreadyExists) {
              Files.delete(zip);
            }

            os = Files.newOutputStream(zip, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            ZipTools.zip(zipEntries, os);
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (Exception e) {
            LOGGER.error("Error exporting AIP " + aip.getId() + ": " + e.getMessage());
            jobPluginInfo.incrementObjectsProcessedWithFailure();
          }
        }
        jobPluginInfo.finalizeInfo();
        PluginHelper.updateJobInformation(this, jobPluginInfo);
      } else if (exportType == ExportType.FOLDER) {
        FileStorageService localStorage = new FileStorageService(Paths.get(outputFolder));
        for (AIP aip : aips) {
          LOGGER.debug("Exporting AIP {}", aip.getId());
          StoragePath aipPath = ModelUtils.getAIPStoragePath(aip.getId());
          try {
            localStorage.copy(storage, aipPath, DefaultStoragePath.parse(aip.getId()));
            jobPluginInfo.incrementObjectsProcessedWithSuccess();
          } catch (AlreadyExistsException e) {
            if (removeIfAlreadyExists) {
              try {
                localStorage.deleteResource(DefaultStoragePath.parse(aip.getId()));
                localStorage.copy(storage, aipPath, DefaultStoragePath.parse(aip.getId()));
                jobPluginInfo.incrementObjectsProcessedWithSuccess();
              } catch (AlreadyExistsException e2) {
                jobPluginInfo.incrementObjectsProcessedWithFailure();
              }
            } else {
              LOGGER.error("Already exist {}", aip.getId());
              jobPluginInfo.incrementObjectsProcessedWithFailure();
            }
          }
        }
        jobPluginInfo.finalizeInfo();
        PluginHelper.updateJobInformation(this, jobPluginInfo);
      }

    } catch (AuthorizationDeniedException | RequestNotValidException | GenericException | NotFoundException e) {
      throw new PluginException(e.getMessage(), e);
    } catch (JobException e) {
      LOGGER.error("Could not update Job information");
    } finally {
      IOUtils.closeQuietly(os);
    }
    return PluginHelper.initPluginReport(this);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MANAGEMENT);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Arrays.asList(AIP.class);
  }
}
