/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.Attribute;
import org.roda.core.data.PluginParameter;
import org.roda.core.data.Report;
import org.roda.core.data.ReportItem;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.JobReport.PluginState;
import org.roda.core.data.v2.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginUtils;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoAcceptSIPPlugin implements Plugin<AIP> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoAcceptSIPPlugin.class);

  private Map<String, String> parameters;

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
    return "Auto accept SIP";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getDescription() {
    return "Automatically accepts SIPs ingested without manual validation";
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
    Report report = PluginUtils.createPluginReport(this);
    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = PluginUtils.createPluginReportItem(this, "Auto accept AIP " + aip.getId(), aip.getId(),
        null);
      String outcomeDetail = "";
      try {
        LOGGER.debug("Auto accepting AIP " + aip.getId());
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        Map<String, Set<String>> aipMetadata = storage.getMetadata(aipPath);
        ModelUtils.setAs(aipMetadata, RodaConstants.STORAGE_META_ACTIVE, true);
        storage.updateMetadata(aipPath, aipMetadata, true);
        model.updateAIP(aip.getId());
        state = PluginState.OK;
        reportItem.setItemId(aip.getId());
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
        LOGGER.debug("Done with auto accepting AIP " + aip.getId());
      } catch (RODAException e) {
        LOGGER.error("Error updating AIP (metadata attribute active=true)", e);
        outcomeDetail = "Error updating AIP (metadata attribute active=true): " + e.getMessage();
        state = PluginState.ERROR;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, outcomeDetail));
      }

      createEvent(outcomeDetail, state, aip, model);
      report.addItem(reportItem);
      try {
        PluginUtils.updateJobReport(model, index, this, reportItem, state, PluginUtils.getJobId(parameters),
          aip.getId());
      } catch (NotFoundException | GenericException | RequestNotValidException e) {
        LOGGER.error("Error updating job report", e);
      }
    }

    return report;
  }

  private void createEvent(String outcomeDetail, PluginState state, AIP aip, ModelService model)
    throws PluginException {

    try {
      boolean success = (state == PluginState.OK);

      for (String representationID : aip.getRepresentationIds()) {
        PluginUtils.createPluginEvent(aip.getId(), representationID, model,
          EventPreservationObject.PRESERVATION_EVENT_TYPE_INGESTION, "The SIP was successfully accepted.",
          EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, "AGENT ID",
          Arrays.asList(representationID), success ? "success" : "error", success ? "" : "Error", outcomeDetail);
      }
    } catch (PremisMetadataException | IOException | RODAException e) {
      throw new PluginException(e.getMessage(), e);
    }

    // TODO agent
    /*
     * DateFormat format = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss.SSS");
     * EventPreservationObject epo = new EventPreservationObject();
     * epo.setDatetime(new Date()); epo.setEventType(EventPreservationObject.
     * PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK); epo.setEventDetail(
     * "All the files from the SIP were verified against an antivirus.");
     * epo.setAgentRole(EventPreservationObject.
     * PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK); String name =
     * UUID.randomUUID().toString(); epo.setId(name); epo.setAgentID("AGENT ID"
     * ); epo.setObjectIDs(aip.getRepresentationIds().toArray(new
     * String[aip.getRepresentationIds().size()]));
     * epo.setOutcome(virusCheckResult.isClean()?"success":"error");
     * epo.setOutcomeDetailNote("Report");
     * epo.setOutcomeDetailExtension(virusCheckResult.getReport()); byte[]
     * serializedPremisEvent = new PremisEventHelper(epo).saveToByteArray();
     * Path file = Files.createTempFile("preservation", ".xml"); Files.copy(new
     * ByteArrayInputStream(serializedPremisEvent), file,
     * StandardCopyOption.REPLACE_EXISTING); Binary resource = (Binary)
     * FSUtils.convertPathToResource(file.getParent(), file);
     * model.createPreservationMetadata(aip.getId(), name, resource);
     */
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new AutoAcceptSIPPlugin();
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
