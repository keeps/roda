package org.roda.action.fixity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.PluginException;
import org.roda.core.common.InvalidParameterException;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.v2.AgentPreservationObject;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.Representation;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.index.IndexService;
import org.roda.model.AIP;
import org.roda.model.File;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.Binary;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;

public class FixityAction implements Plugin<AIP> {
  private AgentPreservationObject fixityAgent;
  private static final Logger LOGGER = Logger.getLogger(FixityAction.class);

  @Override
  public void init() throws PluginException {
    fixityAgent = new AgentPreservationObject();
    fixityAgent.setAgentName(getName() + "/" + getVersion()); //$NON-NLS-1$
    fixityAgent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN);
    fixityAgent.setID("fixityCheck");
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
            epo.setAgentID(fixityAgent.getID());
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

}
