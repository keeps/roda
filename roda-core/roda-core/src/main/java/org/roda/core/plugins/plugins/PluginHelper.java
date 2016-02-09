/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
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
import org.roda.core.metadata.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.ContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;

import com.google.common.collect.Sets;

public final class PluginHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginHelper.class);

  private PluginHelper() {
  }

  public static Report createPluginReport(Plugin<?> plugin) {
    Report report = new Report();
    report.setType(Report.TYPE_PLUGIN_REPORT);
    report.setTitle("Report of plugin " + plugin.getName());

    report.addAttribute(new Attribute("Agent name", plugin.getName()))
      .addAttribute(new Attribute("Agent version", plugin.getVersion()))
      .addAttribute(new Attribute("Start datetime", DateParser.getIsoDate(new Date())));

    return report;
  }

  public static ReportItem createPluginReportItem(TransferredResource transferredResource, Plugin<?> plugin) {
    return createPluginReportItem(plugin, "Extract SIP", null, transferredResource.getId());
  }

  public static ReportItem createPluginReportItem(Plugin<?> plugin, String title, String itemId, String otherId) {
    ReportItem reportItem = new ReportItem(title);
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

  public static String getJobId(Map<String, String> pluginParameters) {
    return pluginParameters.get(RodaConstants.PLUGIN_PARAMS_JOB_ID);
  }

  public static boolean getBooleanFromParameters(Map<String, String> pluginParameters,
    PluginParameter pluginParameter) {
    return verifyIfStepShouldBePerformed(pluginParameters, pluginParameter);
  }

  public static String getStringFromParameters(Map<String, String> pluginParameters, PluginParameter pluginParameter) {
    return pluginParameters.getOrDefault(pluginParameter.getId(), pluginParameter.getDefaultValue());
  }

  public static String getParentIdFromParameters(Map<String, String> pluginParameters) {
    return pluginParameters.get(RodaConstants.PLUGIN_PARAMS_PARENT_ID);
  }

  public static boolean getForceParentIdFromParameters(Map<String, String> pluginParameters) {
    return new Boolean(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
  }

  public static boolean verifyIfStepShouldBePerformed(Map<String, String> pluginParameters,
    PluginParameter pluginParameter) {
    String paramValue = getStringFromParameters(pluginParameters, pluginParameter);
    return Boolean.parseBoolean(paramValue);
  }

  public static String getParentId(String sipParentId, String jobDefinedParentId, boolean jobDefinedForceParentId) {
    String ret = sipParentId;

    if ((StringUtils.isBlank(sipParentId) && StringUtils.isNotBlank(jobDefinedParentId)) || jobDefinedForceParentId) {
      ret = jobDefinedParentId;
    }

    return ret;
  }

  public static Job getJobFromIndex(IndexService index, Map<String, String> pluginParameters)
    throws NotFoundException, GenericException {
    return index.retrieve(Job.class, getJobId(pluginParameters));
  }

  public static void createJobReport(ModelService model, Plugin<?> plugin, ReportItem reportItem,
    PluginState pluginState, String jobId) {
    JobReport jobReport = new JobReport();
    jobReport.setId(ModelUtils.getJobReportId(jobId, reportItem.getItemId()));
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
      model.createJobReport(jobReport);
    } catch (GenericException e) {
      LOGGER.error("Error creating Job Report", e);
    }
  }

  public static void updateJobReport(ModelService model, IndexService index, Plugin<?> plugin, ReportItem reportItem,
    PluginState pluginState, String jobId, String aipId) {

    try {
      JobReport jobReport;

      try {
        jobReport = model.retrieveJobReport(jobId, aipId);
      } catch (NotFoundException e) {
        jobReport = new JobReport();
        jobReport.setId(ModelUtils.getJobReportId(jobId, reportItem.getItemId()));
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

      model.updateJobReport(jobReport);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error while updating Job Report", e);
    }

  }

  public static PreservationMetadata createPluginEvent(String aipID, String representationID, String fileID,
    ModelService model, String eventType, String eventDetails, List<String> sources, List<String> targets,
    String outcome, String detailNote, String detailExtension, IndexedPreservationAgent agent)
      throws PremisMetadataException, IOException, RequestNotValidException, NotFoundException, GenericException,
      AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    String id = UUID.randomUUID().toString();
    ContentPayload premisEvent = PremisUtils.createPremisEventBinary(id, new Date(), eventType, eventDetails, sources,
      targets, outcome, detailNote, detailExtension, Arrays.asList(agent));
    model.createPreservationMetadata(PreservationMetadataType.EVENT, id, aipID, representationID, premisEvent);
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipID);
    pm.setRepresentationId(representationID);
    pm.setId(id);
    pm.setType(PreservationMetadataType.EVENT);
    return pm;
  }

  /**
   * @deprecated This method shouldn't be needed
   */
  @Deprecated
  public static void createDirectories(ModelService model, String aipId, String representationID) {
    try {
      model.getStorage().createDirectory(ModelUtils.getRepresentationsPath(aipId));
    } catch (RODAException sse) {
      LOGGER.error("Error creating directories", sse);
    }
    try {
      model.getStorage().createDirectory(ModelUtils.getRepresentationPath(aipId, representationID));
    } catch (RODAException sse) {
      LOGGER.error("Error creating directories", sse);
    }
    try {
      model.getStorage().createDirectory(ModelUtils.getMetadataPath(aipId));
    } catch (RODAException sse) {
      LOGGER.error("Error creating directories", sse);
    }
    try {
      model.getStorage().createDirectory(ModelUtils.getDescriptiveMetadataPath(aipId));
    } catch (RODAException sse) {
      LOGGER.error("Error creating directories", sse);
    }
    try {
      model.getStorage().createDirectory(ModelUtils.getAIPPreservationMetadataPath(aipId));
    } catch (RODAException sse) {
      LOGGER.error("Error creating directories", sse);
    }
    try {
      model.getStorage().createDirectory(ModelUtils.getAIPRepresentationPreservationPath(aipId, representationID));
    } catch (RODAException sse) {
      LOGGER.error("Error creating directories", sse);
    }

  }

  @Deprecated
  private static Map<String, Set<String>> getRepresentationMetadata(String representationId) {
    SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
    String dateString = iso8601DateFormat.format(new Date());
    Map<String, Set<String>> data = new HashMap<String, Set<String>>();
    data.put("active", Sets.newHashSet("true"));
    data.put("date.created", Sets.newHashSet(dateString));
    data.put("date.modified", Sets.newHashSet(dateString));
    data.put("representation.type", Sets.newHashSet(""));
    data.put("representation.content.model", Sets.newHashSet(""));
    data.put("representation.dObject.pid", Sets.newHashSet(""));
    data.put("representation.id", Sets.newHashSet(representationId));
    data.put("representation.label", Sets.newHashSet(""));
    data.put("representation.pid", Sets.newHashSet(""));
    data.put("representation.state", Sets.newHashSet(""));
    data.put("representation.subtype", Sets.newHashSet(""));
    data.put("representation.type", Sets.newHashSet(""));
    data.put("representation.statuses", Sets.newHashSet("original"));
    return data;
  }

  public static int updateJobStatus(IndexService index, ModelService model, int currentCompletionPercentage,
    int completionPercentageStep, Map<String, String> pluginParameters) {
    int newCurrentCompletionPercentage = currentCompletionPercentage + completionPercentageStep;
    updateJobStatus(index, model, newCurrentCompletionPercentage, pluginParameters);

    return newCurrentCompletionPercentage;
  }

  public static void updateJobStatus(IndexService index, ModelService model, int newCompletionPercentage,
    Map<String, String> pluginParameters) {
    try {
      LOGGER.debug("New job completionPercentage: " + newCompletionPercentage);
      Job job = PluginHelper.getJobFromIndex(index, pluginParameters);
      job.setCompletionPercentage(newCompletionPercentage);

      if (newCompletionPercentage == 0) {
        job.setState(JOB_STATE.STARTED);
      } else if (newCompletionPercentage == 100) {
        job.setState(JOB_STATE.COMPLETED);
        job.setEndDate(new Date());
      }

      model.updateJob(job);
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Unable to get or update Job from index", e);
    }
  }

  public static void createPremisEventPerRepresentation(ModelService model, AIP aip, PluginState state,
    String eventType, String eventDetails, String detailExtension, IndexedPreservationAgent agent)
      throws PluginException {
    String outcome = "";
    switch (state) {
      case SUCCESS:
        outcome = "success";
        break;
      case PARTIAL_SUCCESS:
        outcome = "partial success";
        break;
      case FAILURE:
      default:
        outcome = "failure";
        break;
    }
    try {
      boolean success = (state == PluginState.SUCCESS);
      for (Representation representation : aip.getRepresentations()) {
        createPluginEvent(aip.getId(), representation.getId(), null, model, eventType, eventDetails,
          Arrays.asList(representation.getId()), null, outcome, success ? "" : "Error", detailExtension, agent);
      }
    } catch (IOException | RODAException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  public static PreservationMetadata createPluginAgent(ModelService model, String agentId, String agentName,
    String agentType) throws GenericException, NotFoundException, RequestNotValidException,
      AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    ContentPayload premisAgent = PremisUtils.createPremisAgentBinary(agentId, agentName, agentType);
    model.createPreservationMetadata(PreservationMetadataType.AGENT, agentId, premisAgent);
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(null);
    pm.setRepresentationId(null);
    pm.setId(agentId);
    pm.setType(PreservationMetadataType.AGENT);
    return pm;

  }
}
