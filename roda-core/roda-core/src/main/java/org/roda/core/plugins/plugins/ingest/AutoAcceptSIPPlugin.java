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

import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
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
    Report report = PluginHelper.createPluginReport(this);
    PluginState state;

    for (AIP aip : list) {
      ReportItem reportItem = PluginHelper.createPluginReportItem(this, "Auto accept SIP", aip.getId(), null);
      String outcomeDetail = "";
      try {
        LOGGER.debug("Auto accepting AIP " + aip.getId());
        StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
        Map<String, Set<String>> aipMetadata = storage.getMetadata(aipPath);
        ModelUtils.setAs(aipMetadata, RodaConstants.STORAGE_META_ACTIVE, true);
        storage.updateMetadata(aipPath, aipMetadata, true);
        model.updateAIP(aip.getId());
        state = PluginState.SUCCESS;
        reportItem.setItemId(aip.getId());
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));
        LOGGER.debug("Done with auto accepting AIP " + aip.getId());
      } catch (RODAException e) {
        LOGGER.error("Error updating AIP (metadata attribute active=true)", e);
        outcomeDetail = "Error updating AIP (metadata attribute active=true): " + e.getMessage();
        state = PluginState.FAILURE;
        reportItem.addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()))
          .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, outcomeDetail));
      }

      createEvent(outcomeDetail, state, aip, model);
      report.addItem(reportItem);

      PluginHelper.updateJobReport(model, index, this, reportItem, state, PluginHelper.getJobId(parameters),
        aip.getId());
    }

    return report;
  }

  private void createEvent(String outcomeDetail, PluginState state, AIP aip, ModelService model)
    throws PluginException {

    try {
      boolean success = (state == PluginState.SUCCESS);

      for (String representationID : aip.getRepresentationIds()) {
        PluginHelper.createPluginEvent(aip.getId(), representationID, model,
          EventPreservationObject.PRESERVATION_EVENT_TYPE_INGESTION, "The SIP was successfully accepted.",
          EventPreservationObject.PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK, "AGENT ID",
          Arrays.asList(representationID), state, success ? "" : "Error", outcomeDetail);
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
