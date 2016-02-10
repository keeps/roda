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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.roda.core.common.MetadataUtils;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lc.xmlns.premisV2.File;

public class SiegfriedPlugin implements Plugin<AIP> {
  public static final String OTHER_METADATA_TYPE = "Siegfried";
  public static final String FILE_SUFFIX = ".json";

  private static final Logger LOGGER = LoggerFactory.getLogger(SiegfriedPlugin.class);

  private Map<String, String> parameters;
  private boolean createsPluginEvent = true;

  @Override
  public void init() throws PluginException {
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

    // updates the flag responsible to allow plugin event creation
    if (parameters.containsKey("createsPluginEvent")) {
      createsPluginEvent = Boolean.parseBoolean(parameters.get("createsPluginEvent"));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    Report report = PluginHelper.createPluginReport(this);
    PluginState state;
    IndexedPreservationAgent agent = null;
    try {
      agent = PremisUtils.createPremisAgentBinary(this, RodaConstants.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN,
        model);
    } catch (AlreadyExistsException e) {
      agent = PremisUtils.getPreservationAgent(this, RodaConstants.PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN,
        model);
    } catch (RODAException e) {
      LOGGER.error("Error running adding Siegfried plugin: " + e.getMessage(), e);
    }

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "File format identification", aip.getId(),
        null);

      LOGGER.debug("Processing AIP {}", aip.getId());
      try {
        for (Representation representation : aip.getRepresentations()) {
          LOGGER.debug("Processing representation {} of AIP {}", representation.getId(), aip.getId());

          // TODO run directly in storage
          Path data = Files.createTempDirectory("data");
          StorageService tempStorage = new FileStorageService(data);
          StoragePath representationPath = ModelUtils.getRepresentationPath(aip.getId(), representation.getId());
          tempStorage.copy(storage, representationPath, representationPath);
          Path representationFsPath = data.resolve(representationPath.asString());
          String siegfriedOutput = SiegfriedPluginUtils.runSiegfriedOnPath(representationFsPath);

          final JSONObject obj = new JSONObject(siegfriedOutput);
          JSONArray files = (JSONArray) obj.get("files");

          for (int i = 0; i < files.length(); i++) {
            JSONObject fileObject = files.getJSONObject(i);

            Path fullFsPath = Paths.get(fileObject.getString("filename"));
            Path relativeFsPath = representationFsPath.relativize(fullFsPath);

            String fileId = relativeFsPath.getFileName().toString();
            List<String> fileDirectoryPath = new ArrayList<>();
            for (int j = 0; j < relativeFsPath.getNameCount() - 1; j++) {
              fileDirectoryPath.add(relativeFsPath.getName(j).toString());
            }

            ContentPayload payload = new StringContentPayload(fileObject.toString());

            model.createOtherMetadata(aip.getId(), representation.getId(), fileDirectoryPath, fileId, FILE_SUFFIX,
              OTHER_METADATA_TYPE, payload);

            // Update PREMIS files
            JSONArray matches = (JSONArray) fileObject.get("matches");
            if (matches.length() > 0) {
              for (int j = 0; j < matches.length(); j++) {
                JSONObject match = (JSONObject) matches.get(j);
                if (match.getString("id").equalsIgnoreCase("pronom")) {
                  String format = match.getString("format");
                  String version = match.getString("version");
                  String pronom = match.getString("puid");
                  String mime = match.getString("mime");

                  try {
                    Binary premis_bin = model.retrievePreservationFile(aip.getId(), representation.getId(),
                      fileDirectoryPath, fileId);

                    File premis_file = PremisUtils.binaryToFile(premis_bin.getContent(), false);
                    PremisUtils.updateFileFormat(premis_file, format, version, pronom, mime);

                    PreservationMetadataType type = PreservationMetadataType.OBJECT_FILE;
                    String id = ModelUtils.generatePreservationMetadataId(type, aip.getId(), representation.getId(),
                      fileDirectoryPath, fileId);

                    ContentPayload premis_file_payload = PremisUtils.fileToBinary(premis_file);
                    model.updatePreservationMetadata(id, type, aip.getId(), representation.getId(), fileDirectoryPath,
                      fileId, premis_file_payload);

                  } catch (NotFoundException e) {
                    LOGGER.debug("Siegfried will not update PREMIS because it doesn't exist");
                  } catch (RODAException e) {
                    LOGGER.error("Siegfried will not update PREMIS due to an error", e);
                  }
                }
              }
            }
          }

          PluginHelper.createPremisEventPerRepresentation(model, aip, PluginState.SUCCESS,
            RodaConstants.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION,
            "The files of the representation were successfully identified.", siegfriedOutput, agent);

          FSUtils.deletePath(data);

        }

        state = PluginState.SUCCESS;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

      } catch (PluginException | IOException | NotFoundException | GenericException | RequestNotValidException
        | AuthorizationDeniedException | AlreadyExistsException e) {
        LOGGER.error("Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage(), e);

        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS,
            "Error running SIEGFRIED " + aip.getId() + ": " + e.getMessage()));
      }

      report.addItem(reportItem);

      // TODO Remove try catch... only added to run siegfried plugin via sh
      // script
      try {
        if (createsPluginEvent) {
          PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
            aip.getId());
        }
      } catch (Throwable t) {

      }
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
