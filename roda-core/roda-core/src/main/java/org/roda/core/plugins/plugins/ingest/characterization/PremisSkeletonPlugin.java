/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisUtils;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.RepresentationPreservationObject;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.core.model.AIP;
import org.roda.core.model.File;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PremisSkeletonPlugin implements Plugin<AIP> {
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
    return "Premis skeleton action";
  }

  @Override
  public String getDescription() {
    return "Create the premis related files with the basic information";
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
    try {
      Path temp = Files.createTempDirectory("temp");
      for (AIP aip : list) {
        logger.debug("Processing AIP " + aip.getId());
        try {
          for (String representationID : aip.getRepresentationIds()) {
            logger.debug("Processing representation " + representationID + " from AIP " + aip.getId());
            RepresentationPreservationObject pObject = new RepresentationPreservationObject();
            pObject.setId(representationID);
            pObject.setPreservationLevel("");
            Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
            List<RepresentationFilePreservationObject> pObjectPartFiles = new ArrayList<RepresentationFilePreservationObject>();
            for (String fileID : representation.getFileIds()) {
              logger.debug("Processing file " + fileID + " from " + representationID + " of AIP " + aip.getId());
              File file = model.retrieveFile(aip.getId(), representationID, fileID);
              Binary binary = storage.getBinary(file.getStoragePath());
              Path pathFile = Paths.get(temp.toString(), file.getStoragePath().getName());
              Files.copy(binary.getContent().createInputStream(), pathFile, StandardCopyOption.REPLACE_EXISTING);
              RepresentationFilePreservationObject premisObject = PremisUtils.createPremisFromFile(file, binary,
                "PremisSkeletonAction");
              Path premis = Files.createTempFile(file.getId(), ".premis.xml");
              PremisFileObjectHelper helper = new PremisFileObjectHelper(premisObject);
              helper.saveToFile(premis.toFile());
              model.createPreservationMetadata(aip.getId(), representationID, file.getId() + ".premis.xml",
                (Binary) FSUtils.convertPathToResource(premis.getParent(), premis));
              if (pObject.getRootFile() == null) {
                pObject.setRootFile(premisObject);
              } else {
                pObjectPartFiles.add(premisObject);
              }
              FSUtils.deletePath(premis);
            }
            pObject.setPartFiles(
              pObjectPartFiles.toArray(new RepresentationFilePreservationObject[pObjectPartFiles.size()]));
            Path premisRepresentation = Files.createTempFile("representation", ".premis.xml");
            PremisRepresentationObjectHelper helper = new PremisRepresentationObjectHelper(pObject);
            helper.saveToFile(premisRepresentation.toFile());
            model.createPreservationMetadata(aip.getId(), representationID, "representation.premis.xml",
              (Binary) FSUtils.convertPathToResource(premisRepresentation.getParent(), premisRepresentation));
            FSUtils.deletePath(premisRepresentation);
          }
        } catch (ModelServiceException mse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + mse.getMessage(), mse);
        } catch (StorageServiceException sse) {
          logger.error("Error processing AIP " + aip.getId() + ": " + sse.getMessage(), sse);
        } catch (PremisMetadataException pme) {
          logger.error("Error processing AIP " + aip.getId() + ": " + pme.getMessage(), pme);
        }
      }
    } catch (IOException ioe) {
      logger.error("Error executing FastCharacterizationAction: " + ioe.getMessage(), ioe);
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
    return new PremisSkeletonPlugin();
  }

}
