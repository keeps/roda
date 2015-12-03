/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.common.PremisUtils;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.v2.AgentPreservationObject;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.Fixity;
import org.roda.core.data.v2.PluginType;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
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
          EventPreservationObject epo = new EventPreservationObject();
          epo.setDatetime(new Date());
          epo.setEventType(EventPreservationObject.PRESERVATION_EVENT_TYPE_FIXITY_CHECK);
          epo.setEventDetail("Checksums recorded in PREMIS were compared with the files in the repository");
          epo.setAgentRole(EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK);
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
                epo.setOutcome("error");
                epo.setOutcomeDetailNote("Reason");
                StringBuilder sb = new StringBuilder();
                sb.append("<p>The following file have bad checksums:</p>");
                sb.append("<ul>");
                for (String s : koFileIDS) {
                  sb.append("<li>" + s + "</li>");
                }
                sb.append("</ul>");
                epo.setOutcomeDetailExtension(sb.toString());
                notifyUserOfFixityCheckError(representationID, okFileIDS, koFileIDS, epo);
              } else {
                LOGGER.debug("Fixity OK for representation " + representationID + " of AIP " + aip.getId());
                epo.setOutcome("success");
                epo.setOutcomeDetailNote(fileIDs.size() + " files checked successfully");
                epo.setOutcomeDetailExtension(fileIDs.toString());
                notifyUserOfFixityCheckSucess(representationID, okFileIDS, koFileIDS, epo);
              }
            }
          } catch (ModelServiceException | IOException | StorageServiceException e) {
            LOGGER.error("Error processing Representation " + representationID + " - " + e.getMessage(), e);
            epo.setOutcome("undetermined");
            epo.setOutcomeDetailNote("Reason");
            epo.setOutcomeDetailExtension("<p>" + e.getMessage() + "</p>");
            notifyUserOfFixityCheckUndetermined(representationID, epo, e.getMessage());
          }
          try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss.SSS");
            String name = "fixityCheck_" + format.format(new Date()) + ".premis.xml";
            epo.setId(name);
            epo.setAgentID(fixityAgent.getId());
            epo.setObjectIDs(new String[] {"?????"});
            byte[] serializedPremisEvent = new PremisEventHelper(epo).saveToByteArray();
            Path file = Files.createTempFile("preservation", ".xml");
            Files.copy(new ByteArrayInputStream(serializedPremisEvent), file, StandardCopyOption.REPLACE_EXISTING);
            Binary resource = (Binary) FSUtils.convertPathToResource(file.getParent(), file);
            model.createPreservationMetadata(aip.getId(), representationID, name, resource);
          } catch (ModelServiceException e) {
            LOGGER.error(e.getMessage(), e);
          } catch (PremisMetadataException e) {
            LOGGER.error(e.getMessage(), e);
          } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
          } catch (StorageServiceException e) {
            LOGGER.error(e.getMessage(), e);
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
