/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.IdUtils.LinkingObjectType;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.ContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;

public final class PluginHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginHelper.class);

  private PluginHelper() {
  }

  public static <T extends Serializable> Report createPluginReport(Plugin<T> plugin) {
    Report report = new Report();
    report.setType(Report.TYPE_PLUGIN_REPORT);
    report.setTitle("Report of plugin " + plugin.getName());

    report.addAttribute(new Attribute("Agent name", plugin.getName()))
      .addAttribute(new Attribute("Agent version", plugin.getVersion()))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));

    return report;
  }

  public static <T extends Serializable> ReportItem createPluginReportItem(TransferredResource transferredResource,
    Plugin<T> plugin) {
    return createPluginReportItem(plugin, null, transferredResource.getId());
  }

  public static <T extends Serializable> ReportItem createPluginReportItem(Plugin<T> plugin, String itemId,
    String otherId) {
    ReportItem reportItem = new ReportItem(plugin.getName());
    if (itemId != null) {
      reportItem.setItemId(itemId);
    }
    if (otherId != null) {
      reportItem.setOtherId(otherId);
    }
    reportItem.addAttribute(new Attribute("Agent name", plugin.getName()))
      .addAttribute(new Attribute("Agent version", plugin.getVersion()))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));
    return reportItem;
  }

  public static ReportItem setPluginReportItemInfo(ReportItem reportItem, String itemId, Attribute... attributes) {
    reportItem.setItemId(itemId);
    for (Attribute attribute : attributes) {
      reportItem.addAttribute(attribute);
    }
    return reportItem;
  }

  private static <T extends Serializable> String getJobId(Plugin<T> plugin) {
    return plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_JOB_ID);
  }

  public static <T extends Serializable> boolean getBooleanFromParameters(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    return verifyIfStepShouldBePerformed(plugin, pluginParameter);
  }

  public static <T extends Serializable> String getStringFromParameters(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    return plugin.getParameterValues().getOrDefault(pluginParameter.getId(), pluginParameter.getDefaultValue());
  }

  public static String getParentIdFromParameters(Map<String, String> pluginParameters) {
    return pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PARENT_ID);
  }

  public static boolean getForceParentIdFromParameters(Map<String, String> pluginParameters) {
    return new Boolean(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
  }

  public static <T extends Serializable> boolean verifyIfStepShouldBePerformed(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    String paramValue = getStringFromParameters(plugin, pluginParameter);
    return Boolean.parseBoolean(paramValue);
  }

  public static String getParentId(String sipParentId, String jobDefinedParentId, boolean jobDefinedForceParentId) {
    String ret = sipParentId;

    if ((StringUtils.isBlank(sipParentId) && StringUtils.isNotBlank(jobDefinedParentId)) || jobDefinedForceParentId) {
      ret = jobDefinedParentId;
    }

    return ret;
  }

  public static <T extends Serializable> Job getJobFromIndex(IndexService index, Plugin<T> plugin)
    throws NotFoundException, GenericException {
    return index.retrieve(Job.class, getJobId(plugin));
  }

  public static <T extends Serializable> void createJobReport(ModelService model, Plugin<T> plugin,
    ReportItem reportItem, PluginState pluginState) {
    String jobId = getJobId(plugin);
    JobReport jobReport = new JobReport();
    jobReport.setId(IdUtils.getJobReportId(jobId, reportItem.getItemId()));
    jobReport.setJobId(jobId);
    jobReport.setAipId(reportItem.getItemId());
    jobReport.setObjectId(reportItem.getOtherId());
    Date currentDate = new Date();
    jobReport.setDateCreated(currentDate);
    jobReport.setDateUpdated(currentDate);
    jobReport.setLastPluginRan(plugin.getClass().getName());
    jobReport.setLastPluginRanState(pluginState);

    Report report = new Report();
    report.addItem(reportItem);
    jobReport.setReport(report);

    try {
      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException e) {
      LOGGER.error("Error creating Job Report", e);
    }
  }

  public static <T extends Serializable> void updateJobReport(ModelService model, IndexService index, Plugin<T> plugin,
    ReportItem reportItem, PluginState pluginState, String aipId) {
    String jobId = getJobId(plugin);
    try {
      JobReport jobReport;
      try {
        jobReport = model.retrieveJobReport(jobId, aipId);
      } catch (NotFoundException e) {
        jobReport = new JobReport();
        jobReport.setId(IdUtils.getJobReportId(jobId, reportItem.getItemId()));
        jobReport.setJobId(jobId);
        jobReport.setAipId(reportItem.getItemId());
        jobReport.setObjectId(reportItem.getOtherId());
        jobReport.setDateCreated(new Date());
        Report report = new Report();
        jobReport.setReport(report);
      }

      jobReport.setLastPluginRan(plugin.getClass().getName());
      jobReport.setLastPluginRanState(pluginState);
      jobReport.getReport().addItem(reportItem);
      jobReport.setDateUpdated(new Date());

      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error while updating Job Report", e);
    }

  }

  public static PreservationMetadata createPluginEvent(Plugin<?> plugin, String aipID, String representationID,
    List<String> filePath, String fileID, ModelService model, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcome, String outcomeDetailExtension, boolean notify)
      throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {

    IndexedPreservationAgent agent = null;
    try {
      boolean notifyAgent = true;
      agent = PremisUtils.createPremisAgentBinary(plugin, model, notifyAgent);
    } catch (AlreadyExistsException e) {
      agent = PremisUtils.getPreservationAgent(plugin, model);
    } catch (RODAException e) {
      LOGGER.error("Error running adding Siegfried plugin: " + e.getMessage(), e);
    }

    String id = UUID.randomUUID().toString();
    // TODO 3 states... only 2 messages
    String outcomeDetailNote = (outcome == PluginState.SUCCESS) ? plugin.getPreservationEventSuccessMessage()
      : plugin.getPreservationEventFailureMessage();

    ContentPayload premisEvent = PremisUtils.createPremisEventBinary(id, new Date(),
      plugin.getPreservationEventType().toString(), plugin.getPreservationEventDescription(), sources, targets,
      outcome.name(), outcomeDetailNote, outcomeDetailExtension, Arrays.asList(agent));
    model.createPreservationMetadata(PreservationMetadataType.EVENT, id, aipID, representationID, filePath, fileID,
      premisEvent, notify);
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipID);
    pm.setRepresentationId(representationID);
    pm.setId(id);
    pm.setType(PreservationMetadataType.EVENT);
    return pm;
  }

  public static <T extends Serializable> int updateJobStatus(IndexService index, ModelService model,
    int currentCompletionPercentage, int completionPercentageStep, Plugin<T> plugin) {
    int newCurrentCompletionPercentage = currentCompletionPercentage + completionPercentageStep;
    updateJobStatus(index, model, newCurrentCompletionPercentage, plugin);

    return newCurrentCompletionPercentage;
  }

  public static <T extends Serializable> void updateJobStatus(IndexService index, ModelService model,
    int newCompletionPercentage, Plugin<T> plugin) {
    try {
      LOGGER.debug("New job completionPercentage: " + newCompletionPercentage);
      Job job = PluginHelper.getJobFromIndex(index, plugin);
      job.setCompletionPercentage(newCompletionPercentage);

      if (newCompletionPercentage == 0) {
        job.setState(JOB_STATE.STARTED);
      } else if (newCompletionPercentage == 100) {
        job.setState(JOB_STATE.COMPLETED);
        job.setEndDate(new Date());
      }

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Unable to get or update Job from index", e);
    }
  }

  public static LinkingIdentifier getLinkingIdentifier(TransferredResource transferredResource) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(IdUtils.getLinkingIdentifierId(LinkingObjectType.TRANSFERRED_RESOURCE, transferredResource));
    li.setType("URN");
    return li;
  }

  public static LinkingIdentifier getLinkingIdentifier(LinkingObjectType type, String aipID, String representationID,
    List<String> filePath, String fileID) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(IdUtils.getLinkingIdentifierId(type, aipID, representationID, filePath, fileID));
    li.setType("URN");
    return li;
  }

  public static List<LinkingIdentifier> getLinkingRepresentations(AIP aip, ModelService model) {
    List<LinkingIdentifier> identifiers = new ArrayList<LinkingIdentifier>();
    if (aip.getRepresentations() != null && aip.getRepresentations().size() > 0) {
      for (Representation representation : aip.getRepresentations()) {
        identifiers
          .add(getLinkingIdentifier(LinkingObjectType.REPRESENTATION, aip.getId(), representation.getId(), null, null));
      }
    }
    return identifiers;
  }

}
