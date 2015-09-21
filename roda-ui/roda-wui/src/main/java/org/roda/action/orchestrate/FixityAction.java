package org.roda.action.orchestrate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;

import pt.gov.dgarq.roda.core.common.InvalidParameterException;
import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.PluginParameter;
import pt.gov.dgarq.roda.core.data.Report;
import pt.gov.dgarq.roda.core.data.v2.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.Representation;

public class FixityAction implements Plugin<AIP> {
  private AgentPreservationObject agent;
  private final Logger logger = Logger.getLogger(getClass());

  @Override
  public void init() throws PluginException {
    agent = new AgentPreservationObject();
    agent.setAgentName(getName() + "/" + getVersion()); //$NON-NLS-1$
    agent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN);
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Fixity";
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
    return new HashMap<>();
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    // no params
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {
      List<String> representationIds = aip.getRepresentationIds();
      if (representationIds != null && representationIds.size() > 0) {
        for (String representationID : representationIds) {
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
                File f = model.retrieveFile(aip.getId(), representationID, fileID);
                Binary b = storage.getBinary(f.getStoragePath());
                Path p = Files.createTempFile("temp", "");
                b.getContent().writeToPath(p);
                String currentSHA1 = FSUtils.computeContentDigestSHA1(p);
                String originalSHA1 = b.getContentDigest().get(RodaConstants.STORAGE_META_DIGEST_SHA1);
                if (currentSHA1.trim().equalsIgnoreCase(originalSHA1.trim())) {
                  okFileIDS.add(fileID);
                } else {
                  koFileIDS.add(fileID);
                }
              }
              if (okFileIDS.size() < fileIDs.size()) {
                epo.setOutcome("error");
                epo.setOutcomeDetailNote("Reason");
                epo.setOutcomeDetailExtension("<p>Some files failed...</p>");
                notifyUserOfFixityCheckError(representationID, okFileIDS, koFileIDS, epo);
              } else {
                epo.setOutcome("success");
                epo.setOutcomeDetailNote("files checked");
                epo.setOutcomeDetailExtension(fileIDs.toString());
                notifyUserOfFixityCheckSucess(representationID, okFileIDS, koFileIDS, epo);
              }
            }
          } catch (ModelServiceException | IOException | StorageServiceException e) {
            logger.debug("Error processing Representation " + representationID + " - " + e.getMessage(), e);
            epo.setOutcome("undetermined");
            epo.setOutcomeDetailNote("Reason");
            epo.setOutcomeDetailExtension("<p>" + e.getMessage() + "</p>");
            notifyUserOfFixityCheckUndetermined(representationID, epo, e.getMessage());
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

}
