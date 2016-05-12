/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.base;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.Fixity;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixityPlugin extends AbstractPlugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FixityPlugin.class);

  private String riskId;
  private String riskName;
  private String riskCategory;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_ID, "Risk identifier.", PluginParameterType.STRING, "",
        false, false, "Add the risks that will be associated with the objects above."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_NAME, new PluginParameter(
      RodaConstants.PLUGIN_PARAMS_RISK_NAME, "Risk name", PluginParameterType.STRING, "", false, false, "Risk name."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_CATEGORY,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_CATEGORY, "Risk category", PluginParameterType.STRING, "",
        false, false, "Risk category."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "Job finished notification",
        PluginParameterType.STRING, "", false, false,
        "Send a notification, after finishing the process, to one or more e-mail addresses (comma separated)"));
  }

  @Override
  public void init() {
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
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_ID)) {
      riskId = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_NAME)) {
      riskName = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_NAME);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_CATEGORY)) {
      riskCategory = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_CATEGORY);
    }
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<PluginParameter>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_NAME));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_CATEGORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION));
    return parameters;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<AIP> list)
    throws PluginException {

    for (AIP aip : list) {

      for (Representation r : aip.getRepresentations()) {
        boolean inotify = false;
        LOGGER.debug("Checking fixity for files in representation " + r.getId() + " of AIP " + aip.getId());
        try {
          boolean recursive = true;
          CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(), r.getId(), recursive);

          List<String> okFileIDS = new ArrayList<String>();
          List<String> koFileIDS = new ArrayList<String>();
          for (OptionalWithCause<File> oFile : allFiles) {
            if (oFile.isPresent()) {
              File file = oFile.get();

              StoragePath storagePath = ModelUtils.getFileStoragePath(file);
              Binary currentFileBinary = storage.getBinary(storagePath);
              Binary premisFile = model.retrievePreservationFile(file);
              List<Fixity> fixities = PremisV3Utils.extractFixities(premisFile);

              if (fixities != null) {
                boolean fixityOK = true;
                for (Fixity f : fixities) {
                  try {
                    Fixity currentFixity = PremisV3Utils.calculateFixity(currentFileBinary,
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
                  // TODO support file path
                  okFileIDS.add(file.getId());
                } else {
                  koFileIDS.add(file.getId());

                  if (riskId != null && !riskId.equals("")) {
                    try {
                      model.retrieveRisk(riskId);
                    } catch (NotFoundException e) {
                      Risk risk = new Risk();
                      risk.setName(riskName);
                      risk.setCategory(riskCategory);
                      model.createRisk(risk, riskId, false);
                    }

                    model.addRiskIncidence(riskId, file.getAipId(), file.getRepresentationId(), file.getPath(),
                      file.getId(), "RiskIncidence");
                  }
                }
              }

              if (koFileIDS.size() > 0) {
                LOGGER.debug("Fixity error for representation " + r.getId() + " of AIP " + aip.getId());
                StringBuilder sb = new StringBuilder();
                sb.append("<p>The following file have bad checksums:</p>");
                sb.append("<ul>");
                for (String s : koFileIDS) {
                  sb.append("<li>" + s + "</li>");
                }
                sb.append("</ul>");

                // TODO FIXE PREMIS EVENT CREATION
                /*
                 * PreservationMetadata pm =
                 * PluginHelper.createPluginEvent(this, aip.getId(), r.getId(),
                 * null, null, model,
                 * RodaConstants.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
                 * "Checksums recorded in PREMIS were compared with the files in the repository"
                 * , Arrays.asList(IdUtils.getLinkingIdentifierId(aip.getId(),
                 * r.getId(), null, null)), null, "failure", "Reason",
                 * sb.toString(), inotify);
                 * notifyUserOfFixityCheckError(r.getId(), okFileIDS, koFileIDS,
                 * pm);
                 */
              } else {
                /*
                 * LOGGER.debug("Fixity OK for representation " + r.getId() +
                 * " of AIP " + aip.getId()); PreservationMetadata pm =
                 * PluginHelper.createPluginEvent(this, aip.getId(), r.getId(),
                 * null, null, model,
                 * RodaConstants.PRESERVATION_EVENT_TYPE_FIXITY_CHECK,
                 * "Checksums recorded in PREMIS were compared with the files in the repository"
                 * , Arrays.asList(r.getId()), null, "success", okFileIDS.size()
                 * + " files checked successfully", okFileIDS.toString(),
                 * inotify); notifyUserOfFixityCheckSucess(r.getId(), okFileIDS,
                 * koFileIDS, pm);
                 */
              }
            } else {
              LOGGER.error("Cannot process File", oFile.getCause());
            }
          }
          IOUtils.closeQuietly(allFiles);
          model.notifyAIPUpdated(aip.getId());
        } catch (IOException | RODAException | XmlException e) {
          LOGGER.error("Error processing Representation " + r.getId() + " - " + e.getMessage(), e);
        }

      }
    }

    return null;
  }

  private void notifyUserOfFixityCheckUndetermined(String representationID, PreservationMetadata event,
    String message) {
    // TODO Auto-generated method stub
  }

  private void notifyUserOfFixityCheckSucess(String representationID, List<String> okFileIDS, List<String> koFileIDS,
    PreservationMetadata event) {
    // TODO Auto-generated method stub
  }

  private void notifyUserOfFixityCheckError(String representationID, List<String> okFileIDS, List<String> koFileIDS,
    PreservationMetadata event) {
    // TODO Auto-generated method stub
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

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

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return null;
  }

  @Override
  public String getPreservationEventDescription() {
    return "XXXXXXXXXX";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "XXXXXXXXXXXXXXXXXXXXXXXXXX";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }
}
