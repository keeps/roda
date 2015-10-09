package org.roda.action.ingest.deepCharacterization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.ingest.deepCharacterization.utils.CharacterizationUtils;
import org.roda.action.ingest.fastCharacterization.utils.DroidException;
import org.roda.action.ingest.fastCharacterization.utils.DroidUtils;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.common.PremisUtils;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.FileFormat;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.RepresentationFile;
import org.roda.core.data.v2.Fixity;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;


public class DeepCharacterizationAction implements Plugin<AIP> {
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
    return "Deep characterization action";
  }

  @Override
  public String getDescription() {
    return "Update the premis files with the object characterization";
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
    try{
      Path temp = Files.createTempDirectory("temp");
        for(AIP aip : list){
          logger.debug("Deep characterization for AIP "+aip.getId());
          try{
            for(String representationID : aip.getRepresentationIds()){
              Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
              for(String fileID : representation.getFileIds()){
                String fileName = fileID+".premis.xml";
                File file = model.retrieveFile(aip.getId(), representationID, fileID);
                Binary binary = storage.getBinary(file.getStoragePath());
                
                RepresentationFilePreservationObject premisObject = PremisUtils.getPremisFile(storage,aip.getId(),representationID,fileName);
                premisObject = CharacterizationUtils.deepCharacterization(premisObject, file, binary, getParameterValues());
                                                
                Path premis = Files.createTempFile(file.getId(), ".premis.xml");
                PremisFileObjectHelper helper = new PremisFileObjectHelper(premisObject);
                helper.saveToFile(premis.toFile());
                model.createPreservationMetadata(aip.getId(), representationID, fileName, (Binary)FSUtils.convertPathToResource(premis.getParent(), premis));
              }
            }
          }catch(ModelServiceException mse){
            logger.error("Error processing AIP "+aip.getId()+": "+mse.getMessage(),mse);
          } catch (StorageServiceException sse) {
            logger.error("Error processing AIP "+aip.getId()+": "+sse.getMessage(),sse);
          } catch (PremisMetadataException pme) {
            logger.error("Error processing AIP "+aip.getId()+": "+pme.getMessage(),pme);
          }
        }
    }catch(IOException ioe){
      logger.error("Error executing FastCharacterizationAction: "+ioe.getMessage(),ioe);
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
