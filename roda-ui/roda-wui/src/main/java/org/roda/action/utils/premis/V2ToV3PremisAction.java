package org.roda.action.utils.premis;

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

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.common.CommonUtils;
import org.roda.common.PremisUtils;
import org.roda.common.RodaCoreFactory;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.PreservationMetadata;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.xml.sax.SAXException;
// TODO IMPROVE...
public class V2ToV3PremisAction implements Plugin<AIP> {
  private final Logger logger = Logger.getLogger(getClass());

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
                   model.updatePreservationMetadata(aip.getId(),representationID, pm.getId(), binary,true);
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
        CommonUtils.deleteNonEmptyFolder(temp);
      } catch (IOException e) {
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

}
