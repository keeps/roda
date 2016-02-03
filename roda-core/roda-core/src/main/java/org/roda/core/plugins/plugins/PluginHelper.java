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
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
import org.roda.core.index.IndexService;
import org.roda.core.metadata.v2.premis.PremisAgentHelper;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StringContentPayload;
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
    jobReport.setId(UUID.randomUUID().toString());
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
      Filter filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, jobId));
      filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_AIP_ID, aipId));
      Sorter sorter = null;
      Sublist sublist = new Sublist();
      IndexResult<JobReport> find = index.find(JobReport.class, filter, sorter, sublist);
      if (!find.getResults().isEmpty()) {

        JobReport jobReport = find.getResults().get(0);

        jobReport.setLastPluginRan(plugin.getClass().getName());
        jobReport.setLastPluginRanState(pluginState);
        jobReport.getReport().addItem(reportItem);
        jobReport.setDateUpdated(new Date());

        model.updateJobReport(jobReport);
      } else {
        LOGGER.error("Cannot find Job Report using filter {}", filter);
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error while updating Job Report", e);
    }

  }

  public static EventPreservationObject createPluginEvent(String aipID, String representationID, String fileID,
    ModelService model, String eventType, String eventDetails, String agentRole, String agentID, List<String> objectIDs,
    PluginState outcome, String detailNote, String detailExtension) throws PremisMetadataException, IOException,
      RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    EventPreservationObject epo = new EventPreservationObject();
    epo.setDatetime(new Date());
    epo.setEventType(eventType);
    epo.setEventDetail(eventDetails);
    epo.setAgentRole(agentRole);
    String name = UUID.randomUUID().toString();
    epo.setId(name);
    epo.setAgentID(agentID);
    epo.setObjectIDs(objectIDs.toArray(new String[objectIDs.size()]));
    switch (outcome) {
      case SUCCESS:
        epo.setOutcome("success");
        break;
      case PARTIAL_SUCCESS:
        epo.setOutcome("partial success");
        break;
      case FAILURE:
      default:
        epo.setOutcome("failure");
        break;
    }

    epo.setOutcomeDetailNote(detailNote);
    epo.setOutcomeDetailExtension(detailExtension);
    String serializedPremisEvent = new PremisEventHelper(epo).saveToString();
    ContentPayload premisEventPayload = new StringContentPayload(serializedPremisEvent);
    model.createPreservationMetadata(PreservationMetadataType.EVENT, aipID, representationID, name, premisEventPayload);
    return epo;
  }

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

  public static void createPremisAgentIfInexistent(ModelService model, AgentPreservationObject agent) {
    try {
      model.getAgentPreservationObject(agent.getId());
    } catch (NotFoundException e) {
      try {
        String serializedPremisAgent = new PremisAgentHelper(agent).saveToString();
        ContentPayload premisAgentPayload = new StringContentPayload(serializedPremisAgent);
        model.createPreservationMetadata(PreservationMetadataType.AGENT, null, null, agent.getId(), premisAgentPayload);
      } catch (RequestNotValidException | PremisMetadataException | GenericException | NotFoundException
        | AuthorizationDeniedException ee) {
        LOGGER.error("Error creating PREMIS agent", e);
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error getting PREMIS agent", e);
    }
  }

  public static void createPremisEventPerRepresentation(ModelService model, AIP aip, PluginState state,
    AgentPreservationObject agent, String eventType, String eventDetails, String agentRole, String detailExtension)
      throws PluginException {

    try {
      boolean success = (state == PluginState.SUCCESS);

      for (Representation representation : aip.getRepresentations()) {

        PluginHelper.createPluginEvent(aip.getId(), representation.getId(), null, model, eventType, eventDetails,
          agentRole, agent.getId(), Arrays.asList(representation.getId()), state, success ? "" : "Error",
          detailExtension);
      }
    } catch (IOException | RODAException e) {
      throw new PluginException(e.getMessage(), e);
    }
  }

  public static void createPremisEventIfInexistent(ModelService model, EventPreservationObject event,
    String representationID) {
    try {
      model.getEventPreservationObject(event.getAipId(), representationID, null, event.getId());
    } catch (NotFoundException e) {
      try {
        String serializedPremisEvent = new PremisEventHelper(event).saveToString();
        ContentPayload premisEventPayload = new StringContentPayload(serializedPremisEvent);

        if (representationID == null) { // "AIP Event"
          model.createPreservationMetadata(PreservationMetadataType.EVENT, event.getAipId(), null, event.getId(),
            premisEventPayload);
        } else { // "Representation Event"
          model.createPreservationMetadata(PreservationMetadataType.EVENT, event.getAipId(), representationID,
            event.getId(), premisEventPayload);
        }

      } catch (RequestNotValidException | PremisMetadataException | NotFoundException | GenericException
        | AuthorizationDeniedException ee) {
        LOGGER.error("Error creating PREMIS event", e);
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error getting PREMIS event", e);
    }
  }

  // FIXME refactor this method (using others, from this class, that have this
  // logic)
  public static void createPluginEventAndAgent(String aipID, String representationID, ModelService model, String type,
    String details, String agentRole, String agentID, List<String> objectIDs, String outcome, String detailNote,
    String detailExtension, String agentName, String agentType)
      throws PremisMetadataException, IOException, RequestNotValidException, NotFoundException, GenericException,
      AlreadyExistsException, AuthorizationDeniedException {

    String name = UUID.randomUUID().toString();

    EventPreservationObject epo = new EventPreservationObject();
    epo.setDatetime(new Date());
    epo.setAipId(aipID);
    epo.setEventType(type);
    epo.setEventDetail(details);
    epo.setAgentRole(agentRole);
    epo.setId(name);
    epo.setAgentID(agentID);
    epo.setObjectIDs(objectIDs.toArray(new String[objectIDs.size()]));
    epo.setOutcome(outcome);
    epo.setOutcomeDetailNote(detailNote);
    epo.setOutcomeDetailExtension(detailExtension);

    AgentPreservationObject apo = new AgentPreservationObject();
    apo.setAgentName(agentName);
    apo.setAgentType(agentType);
    apo.setId(agentID);

    createPremisAgentIfInexistent(model, apo);
    createPremisEventIfInexistent(model, epo, representationID);

  }
}
