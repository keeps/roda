/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportAIPPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExportAIPPlugin.class);

  public static final String EXPORT_FOLDER_PARAMETER = "outputFolder";
  public static final String EXPORT_TYPE = "exportType";

  private String outputFolder;
  private ExportType exportType;

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
    return "Export AIP";
  }

  @Override
  public String getDescription() {
    return "Export AIPs to a Zip file";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(EXPORT_FOLDER_PARAMETER)) {
      outputFolder = parameters.get(EXPORT_FOLDER_PARAMETER);
    }
    if (parameters.containsKey(EXPORT_TYPE)) {
      try {
        exportType = ExportType.valueOf(parameters.get(EXPORT_TYPE));
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        exportType = ExportType.SINGLE_ZIP;
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> aips)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);
    FileOutputStream fos = null;
    // FIXME 20160418 hsilva: change all java.io to nio based code
    // FIXME 20160419 hsilva: when exporting single ZIP, name the file something
    // uniq, e.g. using job id as prefix
    try {
      if (exportType == ExportType.SINGLE_ZIP) {
        List<ZipEntryInfo> zipEntries = ModelUtils.zipAIP(aips);
        java.io.File outputFolderFile = new java.io.File(outputFolder);
        fos = new FileOutputStream(new java.io.File(outputFolderFile, "aips.zip"));
        ZipTools.zip(zipEntries, fos);
      } else if (exportType == ExportType.MULTI_ZIP) {
        for (AIP aip : aips) {
          List<ZipEntryInfo> zipEntries = ModelUtils.aipToZipEntry(aip);
          java.io.File outputFolderFile = new java.io.File(outputFolder);
          fos = new FileOutputStream(new java.io.File(outputFolderFile, aip.getId() + ".zip"));
          ZipTools.zip(zipEntries, fos);
        }
      } else if (exportType == ExportType.FOLDER) {
        FileStorageService localStorage = new FileStorageService(Paths.get(outputFolder));
        for (AIP aip : aips) {
          try {
            StoragePath aipPath = ModelUtils.getAIPStoragePath(aip.getId());
            localStorage.copy(storage, aipPath, DefaultStoragePath.parse(aip.getId()));
          } catch (AlreadyExistsException e) {
            LOGGER.error("Already exist {}", aip.getId());
          }
        }
      }

      return report;
    } catch (AuthorizationDeniedException | RequestNotValidException | GenericException | NotFoundException
      | IOException e) {
      throw new PluginException(e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(fos);
    }
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
