/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpylyzerPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(JpylyzerPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Jpylyzer characterization action";
  }

  @Override
  public String getDescription() {
    return "Generates the Jpylyzer output for each file in the AIP";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    for (AIP aip : list) {
      LOGGER.debug("Processing AIP {}", aip.getId());
      boolean inotify = false;
      for (Representation representation : aip.getRepresentations()) {
        LOGGER.debug("Processing representation {} from AIP {}", representation.getId(), aip.getId());
        try {
          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
            representation.getId(), recursive);
          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();
              if (!file.isDirectory()) {
                // TODO check if file is JPEG2000
                try {
                  LOGGER.debug("Processing file: {}", file);
                  StoragePath storagePath = ModelUtils.getFileStoragePath(file);
                  Binary binary = storage.getBinary(storagePath);

                  String jpylyzerResults = JpylyzerPluginUtils.runJpylyzer(file, binary, getParameterValues());
                  ContentPayload payload = new StringContentPayload(jpylyzerResults);
                  model.createOtherMetadata(aip.getId(), representation.getId(), file.getPath(), file.getId(), ".xml",
                    "jpylyzer", payload, inotify);
                } catch (RODAException | IOException sse) {
                  LOGGER.error("Error processing AIP {}: {}", aip.getId(), sse.getMessage());
                }
              }
            } else {
              LOGGER.error("Cannot process AIP representation file", oFile.getCause());
            }
          }
          IOUtils.closeQuietly(allFiles);
        } catch (RODAException mse) {
          LOGGER.error("Error processing AIP {}: {}", aip.getId(), mse.getMessage());
        }
      }

      try {
        model.notifyAIPUpdated(aip.getId());
      } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
        LOGGER.error("Error notifying of AIP update", e);
      }
    }
    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new JpylyzerPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // TODO Auto-generated method stub
    return null;
  }

}
