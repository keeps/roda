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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationFilePreservationObject;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixityPlugin implements Plugin<AIP> {
  private AgentPreservationObject fixityAgent;
  private static final Logger LOGGER = LoggerFactory.getLogger(FixityPlugin.class);

  private Map<String, String> parameters;

  @Override
  public void init() throws PluginException {
    fixityAgent = new AgentPreservationObject();
    fixityAgent.setAgentName(getName() + "/" + getVersion()); //$NON-NLS-1$
    fixityAgent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN);
    fixityAgent.setId("fixityCheck");
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Fixity check";
  }

  @Override
  public String getDescription() {
    return "Computes the fixity check on AIP files";
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
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    this.parameters = parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {
    for (AIP aip : list) {
      List<String> representationIds = aip.getRepresentationIds();
      if (representationIds != null && representationIds.size() > 0) {
        for (String representationID : representationIds) {
          LOGGER.debug("Checking fixity for files in representation " + representationID + " of AIP " + aip.getId());
          try {
            Representation r = model.retrieveRepresentation(aip.getId(), representationID);
            List<String> fileIDs = r.getFileIds();
            List<String> okFileIDS = new ArrayList<String>();
            List<String> koFileIDS = new ArrayList<String>();
            if (fileIDs != null && fileIDs.size() > 0) {
              for (String fileID : fileIDs) {
                LOGGER.debug("Checking fixity for " + fileID);
                File currentFile = model.retrieveFile(aip.getId(), representationID, fileID);
                Binary currentFileBinary = storage.getBinary(currentFile.getStoragePath());
                Path currentPath = Files.createTempFile("temp", "");
                currentFileBinary.getContent().writeToPath(currentPath);
                RepresentationFilePreservationObject rfpo = model.retrieveRepresentationFileObject(aip.getId(),
                  representationID, fileID);
                if (rfpo.getFixities() != null) {
                  boolean fixityOK = true;
                  for (Fixity f : rfpo.getFixities()) {
                    try {
                      Fixity currentFixity = PremisUtils.calculateFixity(currentFileBinary,
                        f.getMessageDigestAlgorithm(), "FixityCheck action");
                      if (!f.getMessageDigest().trim().equalsIgnoreCase(currentFixity.getMessageDigest().trim())) {
                        fixityOK = false;
                        break;
                      }
                    } catch (NoSuchAlgorithmException nsae) {
                      fixityOK = false;
                      break;
                    }
                  }
                  if (fixityOK) {
                    okFileIDS.add(fileID);
                  } else {
                    koFileIDS.add(fileID);
                  }
                }
              }
              if (okFileIDS.size() < fileIDs.size()) {
                LOGGER.debug("Fixity error for representation " + representationID + " of AIP " + aip.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("<p>The following file have bad checksums:</p>");
                sb.append("<ul>");
                for (String s : koFileIDS) {
                  sb.append("<li>" + s + "</li>");
                }
                sb.append("</ul>");
                EventPreservationObject epo = PluginHelper.createPluginEvent(aip.getId(), representationID, model,
                  EventPreservationObject.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
                  "Checksums recorded in PREMIS were compared with the files in the repository",
                  EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK, fixityAgent.getId(),
                  Arrays.asList(representationID), PluginState.FAILURE, "Reason", sb.toString());
                notifyUserOfFixityCheckError(representationID, okFileIDS, koFileIDS, epo);
              } else {
                LOGGER.debug("Fixity OK for representation " + representationID + " of AIP " + aip.getId());
                EventPreservationObject epo = PluginHelper.createPluginEvent(aip.getId(), representationID, model,
                  EventPreservationObject.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
                  "Checksums recorded in PREMIS were compared with the files in the repository",
                  EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK, fixityAgent.getId(),
                  Arrays.asList(representationID), PluginState.SUCCESS, fileIDs.size() + " files checked successfully",
                  fileIDs.toString());
                notifyUserOfFixityCheckSucess(representationID, okFileIDS, koFileIDS, epo);
              }
            }
          } catch (IOException | RODAException | PremisMetadataException e) {
            LOGGER.error("Error processing Representation " + representationID + " - " + e.getMessage(), e);
            EventPreservationObject epo;
            try {
              epo = PluginHelper.createPluginEvent(aip.getId(), representationID, model,
                EventPreservationObject.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
                "Checksums recorded in PREMIS were compared with the files in the repository",
                EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK, fixityAgent.getId(),
                Arrays.asList(representationID), PluginState.PARTIAL_SUCCESS, "Reason",
                "<p>" + e.getMessage() + "</p>");
              notifyUserOfFixityCheckUndetermined(representationID, epo, e.getMessage());
            } catch (PremisMetadataException | RODAException | IOException e1) {
              LOGGER
                .error("Error creating premis event for representation " + representationID + " of AIP " + aip.getId());
            }
          }

        }
      }
    }

    return null;
  }

  private void notifyUserOfFixityCheckUndetermined(String representationID, EventPreservationObject epo,
    String message) {
    // TODO Auto-generated method stub

  }

  private void notifyUserOfFixityCheckSucess(String representationID, List<String> okFileIDS, List<String> koFileIDS,
    EventPreservationObject epo) {
    // TODO Auto-generated method stub

  }

  private void notifyUserOfFixityCheckError(String representationID, List<String> okFileIDS, List<String> koFileIDS,
    EventPreservationObject epo) {
    // TODO Auto-generated method stub

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
    return new FixityPlugin();
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
