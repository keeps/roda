/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.index.IndexService;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.PreservationMetadata;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

// TODO IMPROVE...
public class V2ToV3PremisPlugin implements Plugin<AIP> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "V2 to V3 update action";
  }

  @Override
  public String getDescription() {
    return "This action replaces V2 premis files with his V3 version.";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public Map<String, String> getParameterValues() {
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    Path configPath = null;
    Path temp = null;
    try {
      configPath = RodaCoreFactory.getConfigPath();
      temp = Files.createTempDirectory("temp");
      for (AIP aip : list) {
        logger.debug("Processing AIP " + aip.getId());
        try {
          for (String representationID : aip.getRepresentationIds()) {
            logger.debug("Processing representation " + representationID + " of AIP " + aip.getId());
            ClosableIterable<PreservationMetadata> preservationMetadata = model
              .listPreservationMetadataBinaries(aip.getId(), representationID);
            try {
              for (PreservationMetadata pm : preservationMetadata) {
                logger.debug("Processing preservation metadata " + pm.getStoragePath().getName() + " of representation "
                  + representationID + " of AIP " + aip.getId());
                try {
                  Binary binary = storage.getBinary(pm.getStoragePath());
                  Path pathFile = Paths.get(temp.toString(), pm.getStoragePath().getName());
                  Files.copy(binary.getContent().createInputStream(), pathFile, StandardCopyOption.REPLACE_EXISTING);
                  binary = PremisUtils.updatePremisToV3IfNeeded(binary, configPath);
                  model.updatePreservationMetadata(aip.getId(), representationID, pm.getId(), binary, true);
                } catch (StorageServiceException sse) {
                  logger.error(
                    "Error processing premis metadata " + pm.getStoragePath().asString() + ": " + sse.getMessage(),
                    sse);
                } catch (SAXException se) {
                  logger.error(
                    "Error processing premis metadata " + pm.getStoragePath().asString() + ": " + se.getMessage(), se);
                } catch (TransformerException te) {
                  logger.error(
                    "Error processing premis metadata " + pm.getStoragePath().asString() + ": " + te.getMessage(), te);
                }
              }
            } finally {
              try {
                preservationMetadata.close();
              } catch (IOException e) {
                logger.error("Error freeing resources: " + e.getMessage(), e);
              }
            }

          }
        } catch (ModelServiceException mse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
        }
      }
    } catch (IOException e) {

    } finally {
      try {
        FSUtils.deletePath(temp);
      } catch (StorageServiceException e) {
        logger.error("Error removing temp files: " + e.getMessage(), e);
      }
    }
    return null;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new V2ToV3PremisPlugin();
  }

}
