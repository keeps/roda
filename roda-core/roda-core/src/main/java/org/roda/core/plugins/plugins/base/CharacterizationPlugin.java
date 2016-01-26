/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME check if this is really needed
public class CharacterizationPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(CharacterizationPlugin.class);

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
    Map<String, Map<String, Integer>> total = new HashMap<String, Map<String, Integer>>();
    for (AIP aip : list) {
      Map<String, List<String>> premisFiles = aip.getPreservationFileObjectsIds();
      for (Map.Entry<String, List<String>> entry : premisFiles.entrySet()) {
        String representationID = entry.getKey();
        for (String fileID : entry.getValue()) {
          try {
            StoragePath filePath = ModelUtils.getPreservationFilePath(aip.getId(), representationID,null, fileID);
            Binary binary = storage.getBinary(filePath);
            Map<String, String> characteristics = CharacterizationPluginUtils.getObjectCharacteristicsFields(
              aip.getId(), representationID, fileID, binary, RodaCoreFactory.getConfigPath());
            total = join(total, characteristics);
          } catch (RODAException mse) {
            LOGGER.error("Error processing :" + aip.getId() + "/" + representationID + "/" + fileID);
          }
        }
      }
    }
    // FIXME delete the following for cycle
    for (Map.Entry<String, Map<String, Integer>> entry : total.entrySet()) {
      System.out.println("Property: " + entry.getKey());
      for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
        System.out.println(entry2.getKey() + " - " + entry2.getValue());
      }
    }
    return null;
  }

  private Map<String, Map<String, Integer>> join(Map<String, Map<String, Integer>> total,
    Map<String, String> characteristics) {
    for (Map.Entry<String, String> entry : characteristics.entrySet()) {
      if (total.containsKey(entry.getKey())) {
        Map<String, Integer> c = total.get(entry.getKey());
        if (c.containsKey(entry.getValue())) {
          c.put(entry.getValue(), c.get(entry.getValue()) + 1);
        } else {
          c.put(entry.getValue(), 1);
        }
      } else {
        Map<String, Integer> c = new HashMap<String, Integer>();
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

  @Override
  public Plugin<AIP> cloneMe() {
    return new CharacterizationPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
