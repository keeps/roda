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
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.ContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PluginHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(PluginHelper.class);

  private PluginHelper() {
  }

  public static <T extends Serializable> Report createPluginReport(Plugin<T> plugin) {
    return createPluginReportItem(plugin, null, null);
  }

  public static <T extends Serializable> Report createPluginReportItem(Plugin<T> plugin,
    TransferredResource transferredResource) {
    return createPluginReportItem(plugin, null, transferredResource.getId());
  }

  public static <T extends Serializable> Report createPluginReportItem(Plugin<T> plugin, String itemId,
    String otherId) {
    Report reportItem = new Report();
    reportItem.setItemId(itemId);
    reportItem.setOtherId(otherId);
    reportItem.setTitle(plugin.getName());
    reportItem.setPlugin(plugin.getClass().getCanonicalName());
    reportItem.setDateCreated(new Date());
    reportItem.setTotalSteps(getTotalStepsFromParameters(plugin));

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

  private static <T extends Serializable> String getParentIdFromParameters(Plugin<T> plugin) {
    return plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_PARENT_ID);
  }

  private static <T extends Serializable> boolean getForceParentIdFromParameters(Plugin<T> plugin) {
    return new Boolean(plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID));
  }

  public static <T extends Serializable> int getTotalStepsFromParameters(Plugin<T> plugin) {
    int totalSteps = 1;
    String totalStepsString = plugin.getParameterValues().get(RodaConstants.PLUGIN_PARAMS_TOTAL_STEPS);
    if (totalStepsString != null) {
      try {
        totalSteps = Integer.parseInt(totalStepsString);
      } catch (NumberFormatException e) {
        // return default value
      }
    }
    return totalSteps;
  }

  public static <T extends Serializable> boolean verifyIfStepShouldBePerformed(Plugin<T> plugin,
    PluginParameter pluginParameter) {
    String paramValue = getStringFromParameters(plugin, pluginParameter);
    return Boolean.parseBoolean(paramValue);
  }

  public static <T extends Serializable> String getParentId(Plugin<T> plugin, IndexService index, String sipParentId) {
    String parentId = sipParentId;
    String jobDefinedParentId = getParentIdFromParameters(plugin);
    boolean jobDefinedForceParentId = getForceParentIdFromParameters(plugin);

    // lets see if parentId should be overwritten
    if ((StringUtils.isBlank(sipParentId) && StringUtils.isNotBlank(jobDefinedParentId)) || jobDefinedForceParentId) {
      parentId = jobDefinedParentId;
    }

    // last check: see if parent exist in the index
    if (StringUtils.isNotBlank(parentId)) {
      try {
        index.retrieve(AIP.class, parentId);
      } catch (NotFoundException | GenericException e) {
        // could not find parent id
        parentId = null;
      }
    } else {
      parentId = null;
    }

    return parentId;
  }

  public static <T extends Serializable> Job getJobFromIndex(Plugin<T> plugin, IndexService index)
    throws NotFoundException, GenericException {
    return index.retrieve(Job.class, getJobId(plugin));
  }

  public static <T extends Serializable> void createJobReport(Plugin<T> plugin, ModelService model, Report reportItem) {
    String jobId = getJobId(plugin);
    Report jobReport = new Report(reportItem);
    jobReport.setId(IdUtils.getJobReportId(jobId, reportItem.getItemId()));
    jobReport.setJobId(jobId);
    if (reportItem.getTotalSteps() != 0) {
      jobReport.setTotalSteps(reportItem.getTotalSteps());
    } else {
      jobReport.setTotalSteps(getTotalStepsFromParameters(plugin));
    }
    jobReport.addReport(reportItem);

    try {
      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException e) {
      LOGGER.error("Error creating Job Report", e);
    }
  }

  public static <T extends Serializable> void updateJobReport(Plugin<T> plugin, ModelService model, IndexService index,
    Report reportItem) {
    String jobId = getJobId(plugin);
    try {
      Report jobReport;
      try {
        jobReport = model.retrieveJobReport(jobId, reportItem.getItemId());
      } catch (NotFoundException e) {
        jobReport = createPluginReportItem(plugin, reportItem.getItemId(), reportItem.getOtherId());
        jobReport.addReport(reportItem);
      }

      jobReport.setDateUpdated(new Date());
      jobReport.addReport(reportItem);

      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error while updating Job Report", e);
    }

  }

  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, List<String> filePath, String fileID, ModelService model, List<LinkingIdentifier> sources,
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

    if (plugin.getToolOutput() != null) {
      outcomeDetailNote += "\n" + plugin.getToolOutput();
    }
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

  public static <T extends Serializable> int updateJobStatus(Plugin<T> plugin, IndexService index, ModelService model,
    int stepsCompleted, int totalSteps) {
    int newStepsCompleted = stepsCompleted + 1;
    int percentage = (int) ((100f / totalSteps) * newStepsCompleted);

    updateJobStatus(plugin, index, model, percentage);

    return newStepsCompleted;
  }

  public static <T extends Serializable> void updateJobStatus(Plugin<T> plugin, IndexService index, ModelService model,
    int newCompletionPercentage) {
    try {
      LOGGER.debug("New job completionPercentage: " + newCompletionPercentage);
      Job job = PluginHelper.getJobFromIndex(plugin, index);
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

  public static LinkingIdentifier getLinkingIdentifier(TransferredResource transferredResource, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(IdUtils.getLinkingIdentifierId(LinkingObjectType.TRANSFERRED_RESOURCE, transferredResource));
    li.setType("URN");
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static LinkingIdentifier getLinkingIdentifier(LinkingObjectType type, String aipID, String representationID,
    List<String> filePath, String fileID, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(IdUtils.getLinkingIdentifierId(type, aipID, representationID, filePath, fileID));
    li.setType("URN");
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static List<LinkingIdentifier> getLinkingRepresentations(AIP aip, ModelService model, String role) {
    List<LinkingIdentifier> identifiers = new ArrayList<LinkingIdentifier>();
    if (aip.getRepresentations() != null && aip.getRepresentations().size() > 0) {
      for (Representation representation : aip.getRepresentations()) {
        identifiers.add(getLinkingIdentifier(LinkingObjectType.REPRESENTATION, aip.getId(), representation.getId(),
          null, null, role));
      }
    }
    return identifiers;
  }

}
