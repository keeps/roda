/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.core.data.Attribute;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.AgentPreservationObject;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.FileFormat;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.PluginType;
import org.roda.core.data.v2.SimpleFile;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiegfriedPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPlugin.class);

  private Map<String, String> parameters;

  private AgentPreservationObject agent;

  @Override
  public void init() throws PluginException {
    agent = new AgentPreservationObject();
    agent.setAgentName(getName() + "/" + getVersion());
    agent.setAgentType(AgentPreservationObject.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN);
    agent.setId("characterization-siegfried");
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Siegfried characterization action";
  }

  @Override
  public String getDescription() {
    return "Update the premis files with the object characterization";
  }

  @Override
  public String getVersion() {
    return SiegfriedPluginUtils.getVersion();
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

    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    PluginHelper.createPremisAgentIfInexistent(model, agent);

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "File format identification", aip.getId(),
        null);

      LOGGER.debug("Processing AIP {}", aip.getId());
      try {
        for (String representationID : aip.getRepresentationIds()) {
          LOGGER.debug("Processing representation {} of AIP {}", representationID, aip.getId());

          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representationID);
          tempStorage.copy(storage, representationPath, representationPath);
          String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(data.resolve(representationPath.asString()));

          final JSONObject obj = new JSONObject(siegfriedOutput);
          JSONArray files = (JSONArray) obj.get("files");
          List<SimpleFile> updatedFiles = new ArrayList<SimpleFile>();
          for (int i = 0; i < files.length(); i++) {
            JSONObject fileObject = files.getJSONObject(i);

            String fileName = fileObject.getString("filename");
            fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1);
            long fileSize = fileObject.getLong("filesize");

            Path p = Files.createTempFile("temp", ".temp");
            Files.write(p, fileObject.toString().getBytes());
            Binary resource = (Binary) FSUtils.convertPathToResource(p.getParent(), p);
            LOGGER.debug("Creating other metadata (AIP: " + aip.getId() + ", REPRESENTATION: " + representationID
              + ", FILE: " + fileName + ")");

            model.createOtherMetadata(aip.getId(), representationID, fileName + ".json", "Siegfried", resource);

            p.toFile().delete();

            JSONArray matches = (JSONArray) fileObject.get("matches");
            if (matches.length() > 0) {
              for (int j = 0; j < matches.length(); j++) {
                JSONObject match = (JSONObject) matches.get(j);
                if (match.getString("id").equalsIgnoreCase("pronom")) {
                  String format = match.getString("format");
                  String pronom = match.getString("puid");
                  String mime = match.getString("mime");
                  String version = match.getString("version");
                  String extension = "";
                  if (fileName.contains(".")) {
                    extension = fileName.substring(fileName.lastIndexOf('.'));
                  }
                  SimpleFile f = index.retrieve(SimpleFile.class,
                    SolrUtils.getId(aip.getId(), representationID, fileName));
                  FileFormat ff = new org.roda.core.data.v2.FileFormat();
                  ff.setFormatDesignationName(format);
                  ff.setFormatDesignationVersion(version);
                  ff.setPronom(pronom);
                  ff.setMimeType(mime);
                  ff.setExtension(extension);
                  f.setFileFormat(ff);
                  f.setSize(fileSize);
                  f.setOriginalName(fileName);
                  updatedFiles.add(f);
                }
              }
            }
          }
          model.updateFileFormats(updatedFiles);

          PluginHelper.createPremisEventPerRepresentation(model, aip, PluginState.SUCCESS, agent,
            EventPreservationObject.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION,
            "The files of the representation were successfully identified.",
            EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, siegfriedOutput);

          FSUtils.deletePath(data);

        }

        state = PluginState.SUCCESS;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

      } catch (PluginException | IOException | ModelServiceException | NotFoundException | GenericException
        | RequestNotValidException | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage(), e);

        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS,
            "Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage()));
      }

      report.addItem(reportItem);

      PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
        aip.getId());
    }
    return report;
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
    SiegfriedPlugin siegfriedPlugin = new SiegfriedPlugin();
    try {
      siegfriedPlugin.init();
    } catch (PluginException e) {
      LOGGER.error("Error doing " + SiegfriedPlugin.class.getName() + "init", e);
    }
    return siegfriedPlugin;
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
