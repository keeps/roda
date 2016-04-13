/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.validation;

import java.util.List;

import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
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
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileFormatPlugin extends AbstractPlugin<Representation> {

  private static Logger LOGGER = LoggerFactory.getLogger(FileFormatPlugin.class);

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
    return "File format validation";
  }

  @Override
  public String getDescription() {
    return "Validates file formats and create related risks";
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.FORMAT_VALIDATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Validates file formats and creates associated risks";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Format validation run with success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Format validation failed";
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<Representation> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (Representation representation : list) {
      Report reportItem = PluginHelper.createPluginReportItem(this, representation.getId(), null);

      try {
        boolean recursive = true;
        CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(representation.getAipId(),
          representation.getId(), recursive);

        for (OptionalWithCause<File> oFile : allFiles) {
          if (oFile.isPresent()) {
            File file = oFile.get();
            IndexedFile ifile = index.retrieve(IndexedFile.class, IdUtils.getFileId(file));
            String filePronom = ifile.getFileFormat().getPronom();
            String fileMimetype = ifile.getFileFormat().getMimeType();
            String fileFormat = ifile.getId().substring(ifile.getId().lastIndexOf('.'));

            CloseableIterable<Resource> allFormats = storage
              .listResourcesUnderDirectory(ModelUtils.getFormatContainerPath(), true);

            for (Resource resource : allFormats) {
              String resourceName = resource.getStoragePath().getName();
              Format format = model.retrieveFormat(resourceName.substring(0, resourceName.lastIndexOf('.')));
              boolean hasAgent = false;

              if (format.getPronoms().contains(filePronom)) {
                if (!format.getMimetypes().contains(fileMimetype)) {
                  // create risk ?
                }

                if (!format.getExtensions().contains(fileFormat)) {
                  // create risk ?
                }

                if (model.retrieveAgentsFromFormat(format).size() > 0) {
                  hasAgent = true;
                }
              }

              if (format.getMimetypes().contains(fileMimetype)) {
                if (!format.getExtensions().contains(fileFormat)) {
                  // create risk ?
                }

                if (hasAgent == false && model.retrieveAgentsFromFormat(format).size() > 0) {
                  hasAgent = true;
                }
              }

              if (hasAgent == false) {
                // create risk ?
              }
            }
          } else {
            LOGGER.error("Cannot process representation file", oFile.getCause());
          }
        }

        reportItem.setPluginState(PluginState.SUCCESS);
      } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
        LOGGER.warn("File format plugin did not run properly");
        reportItem.setPluginState(PluginState.FAILURE);
      } finally {
        report.addReport(reportItem);
      }
    }

    return report;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public Plugin<Representation> cloneMe() {
    return new FileFormatPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
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

}
