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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.common.IdUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LinkingObjectUtils;
import org.roda.core.data.v2.LinkingObjectUtils.LinkingObjectType;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
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

  public static <T extends Serializable> void setPluginParameters(Plugin<T> plugin, Job job) {
    Map<String, String> parameters = new HashMap<String, String>(job.getPluginParameters());
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    try {
      plugin.setParameterValues(parameters);
    } catch (InvalidParameterException e) {
      LOGGER.error("Error setting plug-in parameters", e);
    }
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
        index.retrieve(IndexedAIP.class, parentId);
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
    String jobID = getJobId(plugin);
    if (jobID != null) {
      return index.retrieve(Job.class, jobID);
    } else {
      throw new NotFoundException("Job not found");
    }

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
        // XXX check this
        jobReport.setId(IdUtils.getJobReportId(jobId, reportItem.getItemId()));
        jobReport.addReport(reportItem);
      }

      jobReport.setDateUpdated(new Date());
      jobReport.addReport(reportItem);

      model.createOrUpdateJobReport(jobReport);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Error while updating Job Report", e);
    }

  }

  /**
   * For SIP > AIP
   */
  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, TransferredResource source, PluginState outcome,
    String outcomeDetailExtension, boolean notify, Date eventDate) throws RequestNotValidException, NotFoundException,
      GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(source, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
    List<LinkingIdentifier> outcomes = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipID, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));

    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, eventDate);
  }

  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, TransferredResource source, PluginState outcome,
    String outcomeDetailExtension, boolean notify) throws RequestNotValidException, NotFoundException, GenericException,
      AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, model, index, source, outcome, outcomeDetailExtension, notify, new Date());
  }

  /**
   * For AIP as source only
   */
  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, PluginState outcome, String outcomeDetailExtension, boolean notify)
      throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipID, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
    List<LinkingIdentifier> outcomes = null;
    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, PluginState outcome, String outcomeDetailExtension, boolean notify,
    Date eventDate) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    List<LinkingIdentifier> sources = Arrays
      .asList(PluginHelper.getLinkingIdentifier(aipID, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
    List<LinkingIdentifier> outcomes = null;
    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, outcomes, outcome,
      outcomeDetailExtension, notify, eventDate);
  }

  /**
   * For AIP
   */
  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    ModelService model, IndexService index, List<LinkingIdentifier> sources, List<LinkingIdentifier> targets,
    PluginState outcome, String outcomeDetailExtension, boolean notify) throws RequestNotValidException,
      NotFoundException, GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, null, null, null, model, index, sources, targets, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  /**
   * For REPRESENTATION
   */
  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, ModelService model, IndexService index, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcome, String outcomeDetailExtension, boolean notify)
      throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, representationID, null, null, model, index, sources, targets, outcome,
      outcomeDetailExtension, notify, new Date());
  }

  /**
   * For FILE
   */
  public static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, List<String> filePath, String fileID, ModelService model, IndexService index,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, PluginState outcome,
    String outcomeDetailExtension, boolean notify) throws RequestNotValidException, NotFoundException, GenericException,
      AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPluginEvent(plugin, aipID, representationID, filePath, fileID, model, index, sources, outcomes,
      outcome, outcomeDetailExtension, notify, new Date());
  }

  private static <T extends Serializable> PreservationMetadata createPluginEvent(Plugin<T> plugin, String aipID,
    String representationID, List<String> filePath, String fileID, ModelService model, IndexService index,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> outcomes, PluginState outcome,
    String outcomeDetailExtension, boolean notify, Date startDate) throws RequestNotValidException, NotFoundException,
      GenericException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {

    IndexedPreservationAgent agent = null;
    try {
      boolean notifyAgent = true;
      agent = PremisV3Utils.createPremisAgentBinary(plugin, model, notifyAgent);
    } catch (AlreadyExistsException e) {
      agent = PremisV3Utils.getPreservationAgent(plugin, model);
    } catch (RODAException e) {
      LOGGER.error("Error creating PREMIS agent", e);
    }

    IndexedPreservationAgent userAgent = null;
    try {
      boolean notifyAgent = true;
      userAgent = PremisV3Utils.createPremisUserAgentBinary(plugin, model, index, notifyAgent);
    } catch (AlreadyExistsException e) {
      userAgent = PremisV3Utils.getPreservationUserAgent(plugin, model, index);
    } catch (RODAException e) {
      LOGGER.error("Error creating PREMIS agent", e);
    }

    String id = UUID.randomUUID().toString();
    String outcomeDetailNote = (outcome == PluginState.SUCCESS) ? plugin.getPreservationEventSuccessMessage()
      : plugin.getPreservationEventFailureMessage();
    ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(id, startDate,
      plugin.getPreservationEventType().toString(), plugin.getPreservationEventDescription(), sources, outcomes,
      outcome.name(), outcomeDetailNote, outcomeDetailExtension,
      userAgent != null ? Arrays.asList(agent, userAgent) : Arrays.asList(agent));
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
      LOGGER.debug("New job completionPercentage: {}", newCompletionPercentage);
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
    li.setValue(LinkingObjectUtils.getLinkingIdentifierId(LinkingObjectType.TRANSFERRED_RESOURCE, transferredResource));
    li.setType("URN");
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipID, String role) {
    return getLinkingIdentifier(LinkingObjectType.AIP, aipID, role);
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipID, String representationID, String role) {
    return getLinkingIdentifier(LinkingObjectType.REPRESENTATION, IdUtils.getRepresentationId(aipID, representationID),
      role);
  }

  public static LinkingIdentifier getLinkingIdentifier(String aipID, String representationID, List<String> filePath,
    String fileID, String role) {
    return getLinkingIdentifier(LinkingObjectType.FILE, IdUtils.getFileId(aipID, representationID, filePath, fileID),
      role);
  }

  private static LinkingIdentifier getLinkingIdentifier(LinkingObjectType type, String uuid, String role) {
    LinkingIdentifier li = new LinkingIdentifier();
    li.setValue(LinkingObjectUtils.getLinkingIdentifierId(type, uuid));
    li.setType("URN");
    li.setRoles(Arrays.asList(role));
    return li;
  }

  public static List<LinkingIdentifier> getLinkingRepresentations(AIP aip, ModelService model, String role) {
    List<LinkingIdentifier> identifiers = new ArrayList<LinkingIdentifier>();
    if (aip.getRepresentations() != null && !aip.getRepresentations().isEmpty()) {
      for (Representation representation : aip.getRepresentations()) {
        identifiers.add(getLinkingIdentifier(aip.getId(), representation.getId(), role));
      }
    }
    return identifiers;
  }

  public static List<LinkingIdentifier> getLinkingIdentifiers(List<TransferredResource> resources, String role) {
    List<LinkingIdentifier> identifiers = new ArrayList<LinkingIdentifier>();
    if (resources != null && !resources.isEmpty()) {
      for (TransferredResource tr : resources) {
        identifiers.add(getLinkingIdentifier(tr, role));
      }
    }
    return identifiers;
  }

}
