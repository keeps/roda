package org.roda.action.characterization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.common.CharacterizationUtils;
import org.roda.common.RodaCoreFactory;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;

public class CharacterizationAction implements Plugin<AIP> {
  private static final Logger LOGGER = Logger.getLogger(CharacterizationAction.class);

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
    return "Characterization action";
  }

  @Override
  public String getDescription() {
    return "Computes some statistics based on index and stored files";
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
    Map<String,Map<String,Integer>> total = new HashMap<String,Map<String,Integer>>();
    for (AIP aip : list) {
      Map<String, List<String>> premisFiles = aip.getPreservationFileObjectsIds();
      for (Map.Entry<String, List<String>> entry : premisFiles.entrySet()) {
        String representationID = entry.getKey();
        for (String fileID : entry.getValue()) {
          try {
            StoragePath filePath = ModelUtils.getPreservationFilePath(aip.getId(), representationID, fileID);
            Binary binary = storage.getBinary(filePath);
            Map<String, String> characteristics = CharacterizationUtils.getObjectCharacteristicsFields(aip.getId(),
              representationID, fileID, binary, RodaCoreFactory.getConfigPath());
            total = join(total,characteristics);
          } catch (StorageServiceException | IndexServiceException mse) {
            LOGGER.error("Error processing :" + aip.getId() + "/" + representationID + "/" + fileID);
          }
        }
      }
    }
    for(Map.Entry<String, Map<String,Integer>> entry : total.entrySet()){
      System.out.println("Property: "+entry.getKey());
      for(Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()){
        System.out.println(entry2.getKey()+" - "+entry2.getValue());
      }
    }
    return null;
  }

  private Map<String, Map<String, Integer>> join(Map<String, Map<String, Integer>> total, Map<String, String> characteristics) {
    for(Map.Entry<String, String> entry : characteristics.entrySet()){
      if(total.containsKey(entry.getKey())){
        Map<String,Integer> c = total.get(entry.getKey());
        if(c.containsKey(entry.getValue())){
          c.put(entry.getValue(), c.get(entry.getValue())+1);
        }else{
          c.put(entry.getValue(), 1);
        }
      }else{
        Map<String,Integer> c = new HashMap<String,Integer>();
        c.put(entry.getValue(), 1);
        total.put(entry.getKey(), c);
      }
    }
    return total;
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
