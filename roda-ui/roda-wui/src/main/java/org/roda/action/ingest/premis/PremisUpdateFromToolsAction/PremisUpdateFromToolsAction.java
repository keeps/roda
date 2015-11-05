/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.ingest.premis.PremisUpdateFromToolsAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.common.PremisUtils;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.Directory;
import org.roda.storage.Resource;
import org.roda.storage.StorageService;

public class PremisUpdateFromToolsAction implements Plugin<AIP> {
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
    return "Premis update action";
  }

  @Override
  public String getDescription() {
    return "Updates the premis based on the tools output";
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
      for (AIP aip : list) {
        logger.debug("Processing AIP " + aip.getId());
        ClosableIterable<Resource> tools = storage
          .listResourcesUnderContainer(ModelUtils.getOtherMetadataDirectory(aip.getId()));
        Iterator<Resource> it = tools.iterator();
        while (it.hasNext()) {
          Resource r = it.next();
          if (r instanceof Directory) {
            for (String representationID : aip.getRepresentationIds()) {
              logger.debug("Processing representation " + representationID + " from AIP " + aip.getId());
              Representation representation = model.retrieveRepresentation(aip.getId(), representationID);
              for (String fileID : representation.getFileIds()) {
                RepresentationFilePreservationObject premisFile = model.retrieveRepresentationFileObject(aip.getId(),
                  representationID, fileID);

                Binary toolOutput = storage.getBinary(ModelUtils.getToolMetadataPath(aip.getId(), representationID,
                  fileID + ".xml", r.getStoragePath().getName()));
                premisFile = PremisUtils.updatePremisFile(premisFile, r.getStoragePath().getName(), toolOutput);

              }
            }
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error updating PREMIS: " + e.getMessage(), e);
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
