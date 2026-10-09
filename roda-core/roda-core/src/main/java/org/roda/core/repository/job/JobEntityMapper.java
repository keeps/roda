/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.job;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.db.jobs.Job;
import org.roda.core.data.v2.db.jobs.JobAttachment;
import org.roda.core.data.v2.db.jobs.JobPluginParameter;
import org.roda.core.data.v2.db.jobs.JobReport;
import org.roda.core.data.v2.db.jobs.JobReportStep;
import org.roda.core.data.v2.db.jobs.JobSourceSelection;
import org.roda.core.data.v2.db.jobs.JobStats;
import org.roda.core.data.v2.db.jobs.JobUser;
import org.roda.core.data.v2.db.jobs.PluginDescriptor;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.JobUserDetails;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;

/**
 * Converts between the model objects ({@link org.roda.core.data.v2.jobs.Job},
 * {@link Report}) and the database entities of {@code db.jobs}.
 *
 * A report's steps completed, completion percentage, plugin state, plugin
 * details and last update are not stored: they are rebuilt from its steps by
 * replaying them through {@link Report#addReport(Report, boolean)}, the same
 * aggregation the model applies when a step is added.
 *
 * @author RODA Development Team
 */
public final class JobEntityMapper {

  private JobEntityMapper() {
    // utility class
  }

  // ---------------------------------------------------------------- jobs

  /**
   * Copies the job's own attributes (not its stats or child rows) onto the
   * entity.
   */
  public static void toEntity(org.roda.core.data.v2.jobs.Job model, Job entity) {
    entity.setId(model.getId());
    entity.setName(model.getName());
    entity.setUsername(model.getUsername());
    entity.setState(model.getState() == null ? org.roda.core.data.v2.jobs.Job.JOB_STATE.CREATED : model.getState());
    entity.setStateDetails(model.getStateDetails());
    entity.setPlugin(model.getPlugin());
    entity.setPluginType(model.getPluginType());
    entity.setPriority(model.getPriority());
    entity.setParallelism(model.getParallelism());
    entity.setOutcomeObjectsClass(model.getOutcomeObjectsClass());
    entity.setInstanceId(model.getInstanceId());
    entity.setStartDate(model.getStartDate() == null ? new Date() : model.getStartDate());
    entity.setEndDate(model.getEndDate());
    entity.setFlushedAt(model.getFlushedAt());

    SelectedItems<?> source = model.getSourceObjects();
    entity.setSourceSelection(sourceSelection(source));
    entity.setSourceObjectsClass(source == null ? null : emptyToNull(source.getSelectedClass()));
    if (source instanceof SelectedItemsFilter<?> filter) {
      entity.setSourceFilter(
        JsonUtils.getJsonFromObject(filter.getFilter() == null ? new Filter() : filter.getFilter()));
      entity.setSourceJustActive(filter.justActive());
    } else {
      entity.setSourceFilter(null);
      entity.setSourceJustActive(null);
    }
  }

  public static void toEntity(org.roda.core.data.v2.jobs.JobStats model, JobStats entity) {
    entity.setCompletionPercentage(model.getCompletionPercentage());
    entity.setSourceObjectsCount(model.getSourceObjectsCount());
    entity.setBeingProcessed(model.getSourceObjectsBeingProcessed());
    entity.setWaitingToBeProcessed(model.getSourceObjectsWaitingToBeProcessed());
    entity.setProcessedSuccess(model.getSourceObjectsProcessedWithSuccess());
    entity.setProcessedPartialSuccess(model.getSourceObjectsProcessedWithPartialSuccess());
    entity.setProcessedFailure(model.getSourceObjectsProcessedWithFailure());
    entity.setProcessedSkipped(model.getSourceObjectsProcessedWithSkipped());
    entity.setManualIntervention(model.getOutcomeObjectsWithManualIntervention());
  }

  public static JobSourceSelection sourceSelection(SelectedItems<?> source) {
    if (source instanceof SelectedItemsList) {
      return JobSourceSelection.LIST;
    } else if (source instanceof SelectedItemsFilter) {
      return JobSourceSelection.FILTER;
    } else if (source instanceof SelectedItemsAll) {
      return JobSourceSelection.ALL;
    }
    return JobSourceSelection.NONE;
  }

  /**
   * @return the ids of a LIST source selection, without duplicates, or an empty
   *         list for any other selection
   */
  public static List<String> sourceObjectIds(org.roda.core.data.v2.jobs.Job model) {
    if (model.getSourceObjects() instanceof SelectedItemsList<?> list && list.getIds() != null) {
      return list.getIds().stream().filter(Objects::nonNull).distinct().toList();
    }
    return List.of();
  }

  /**
   * A cheap fingerprint of the job's child rows (plugin parameters, LIST source
   * objects, users, attachments), used to skip syncing them when unchanged.
   */
  public static int childrenFingerprint(org.roda.core.data.v2.jobs.Job model) {
    List<String> users = new ArrayList<>();
    if (model.getJobUsersDetails() != null) {
      for (JobUserDetails user : model.getJobUsersDetails()) {
        users.add(user.getUsername() + '\u0000' + user.getRole() + '\u0000' + user.getFullname() + '\u0000'
          + user.getEmail());
      }
    }
    return Objects.hash(model.getPluginParameters(), sourceObjectIds(model), users, model.getAttachmentsList());
  }

  /**
   * The values of a job's child rows (plugin parameters, LIST source objects,
   * users, attachments): written with the job and rarely changed afterwards.
   * Immutable, so it can be shared; {@link #toModel} hands out copies.
   */
  public record JobChildren(Map<String, String> parameters, List<String> sourceObjectIds,
    List<JobUserDetails> users, List<String> attachments) {

    public static JobChildren of(org.roda.core.data.v2.jobs.Job model) {
      List<JobUserDetails> users = new ArrayList<>();
      if (model.getJobUsersDetails() != null) {
        model.getJobUsersDetails().forEach(user -> users.add(new JobUserDetails(user)));
      }
      Map<String, String> parameters = new HashMap<>();
      if (model.getPluginParameters() != null) {
        parameters.putAll(model.getPluginParameters());
      }
      List<String> attachments = model.getAttachmentsList() == null ? List.of()
        : model.getAttachmentsList().stream().filter(Objects::nonNull).distinct().toList();
      return new JobChildren(Collections.unmodifiableMap(parameters), JobEntityMapper.sourceObjectIds(model),
        Collections.unmodifiableList(users), attachments);
    }

    public static JobChildren fromRows(List<JobPluginParameter> parameters, List<String> sourceObjectIds,
      List<JobUser> users, List<JobAttachment> attachments) {
      Map<String, String> modelParameters = new HashMap<>();
      for (JobPluginParameter parameter : parameters) {
        modelParameters.put(parameter.getName(), parameter.getValue());
      }
      List<JobUserDetails> modelUsers = new ArrayList<>();
      for (JobUser user : users) {
        JobUserDetails details = new JobUserDetails();
        details.setUsername(user.getUsername());
        details.setRole(emptyToNull(user.getRole()));
        details.setFullname(user.getFullName());
        details.setEmail(user.getEmail());
        modelUsers.add(details);
      }
      return new JobChildren(Collections.unmodifiableMap(modelParameters), List.copyOf(sourceObjectIds),
        Collections.unmodifiableList(modelUsers), attachments.stream().map(JobAttachment::getFileName).toList());
    }
  }

  public static org.roda.core.data.v2.jobs.Job toModel(Job entity, JobStats stats, JobChildren children)
    throws GenericException {
    org.roda.core.data.v2.jobs.Job model = new org.roda.core.data.v2.jobs.Job();
    model.setId(entity.getId());
    model.setName(entity.getName());
    model.setUsername(entity.getUsername());
    model.setState(entity.getState());
    model.setStateDetails(entity.getStateDetails() == null ? "" : entity.getStateDetails());
    model.setPlugin(entity.getPlugin());
    model.setPluginType(entity.getPluginType());
    model.setPriority(entity.getPriority());
    model.setParallelism(entity.getParallelism());
    model.setOutcomeObjectsClass(entity.getOutcomeObjectsClass() == null ? "" : entity.getOutcomeObjectsClass());
    model.setInstanceId(entity.getInstanceId());
    model.setStartDate(plainDate(entity.getStartDate()));
    model.setEndDate(plainDate(entity.getEndDate()));
    model.setFlushedAt(plainDate(entity.getFlushedAt()));
    model.setSourceObjects(toSelectedItems(entity, children.sourceObjectIds()));

    if (stats != null) {
      org.roda.core.data.v2.jobs.JobStats modelStats = model.getJobStats();
      modelStats.setCompletionPercentage(stats.getCompletionPercentage());
      modelStats.setSourceObjectsCount(stats.getSourceObjectsCount());
      modelStats.setSourceObjectsBeingProcessed(stats.getBeingProcessed());
      modelStats.setSourceObjectsWaitingToBeProcessed(stats.getWaitingToBeProcessed());
      modelStats.setSourceObjectsProcessedWithSuccess(stats.getProcessedSuccess());
      modelStats.setSourceObjectsProcessedWithPartialSuccess(stats.getProcessedPartialSuccess());
      modelStats.setSourceObjectsProcessedWithFailure(stats.getProcessedFailure());
      modelStats.setSourceObjectsProcessedWithSkipped(stats.getProcessedSkipped());
      modelStats.setOutcomeObjectsWithManualIntervention(stats.getManualIntervention());
    }

    // copies: callers may change the job's collections
    model.setPluginParameters(new HashMap<>(children.parameters()));
    List<JobUserDetails> users = new ArrayList<>();
    children.users().forEach(user -> users.add(new JobUserDetails(user)));
    model.setJobUsersDetails(users);
    model.setAttachmentsList(new ArrayList<>(children.attachments()));
    return model;
  }

  private static SelectedItems<?> toSelectedItems(Job entity, List<String> sourceObjectIds)
    throws GenericException {
    String selectedClass = entity.getSourceObjectsClass();
    return switch (entity.getSourceSelection()) {
      case LIST -> new SelectedItemsList<>(new ArrayList<>(sourceObjectIds), selectedClass);
      case FILTER -> new SelectedItemsFilter<>(JsonUtils.getObjectFromJson(entity.getSourceFilter(), Filter.class),
        selectedClass, entity.getSourceJustActive());
      case ALL -> new SelectedItemsAll<>(selectedClass);
      default -> SelectedItemsNone.create();
    };
  }

  // ------------------------------------------------------------- reports

  /**
   * Copies the report's own attributes (not its steps or original ids) onto the
   * entity. The business id is generated by the database from the job, source
   * and outcome ids.
   */
  public static void toEntity(Report model, JobReport entity, Integer pluginId) {
    entity.setJobId(model.getJobId());
    entity.setSourceObjectId(
      model.getSourceObjectId() == null ? Report.NO_SOURCE_OBJECT_ID : model.getSourceObjectId());
    entity.setSourceObjectClass(model.getSourceObjectClass());
    entity.setSourceObjectOriginalName(model.getSourceObjectOriginalName());
    entity.setOutcomeObjectId(
      model.getOutcomeObjectId() == null ? Report.NO_OUTCOME_OBJECT_ID : model.getOutcomeObjectId());
    entity.setOutcomeObjectClass(model.getOutcomeObjectClass());
    entity.setOutcomeObjectState(model.getOutcomeObjectState());
    entity.setPluginId(pluginId);
    entity.setTotalSteps(model.getTotalSteps() == null ? 0 : model.getTotalSteps());
    entity.setTitle(model.getTitle());
    entity.setIngestType(model.getIngestType());
    entity.setTransactionId(model.getTransactionId());
    entity.setDateCreated(model.getDateCreated() == null ? new Date() : model.getDateCreated());
  }

  /**
   * @return the step as it would be stored, so it can be compared with a stored
   *         step (see {@link #sameStep(Object[], JobReportStep)})
   */
  public static JobReportStep toStepEntity(Report step, Long reportPk, Integer pluginId) {
    JobReportStep entity = new JobReportStep();
    entity.setReportPk(reportPk);
    entity.setPluginId(pluginId);
    entity.setPluginState(step.getPluginState() == null ? PluginState.RUNNING : step.getPluginState());
    entity.setPluginIsMandatory(step.getPluginIsMandatory() == null || step.getPluginIsMandatory());
    entity.setPluginDetails(emptyToNull(step.getPluginDetails()));
    entity.setHtmlPluginDetails(step.isHtmlPluginDetails());
    entity.setOutcomeObjectState(step.getOutcomeObjectState());
    entity.setDateCreated(step.getDateCreated());
    Date updated = step.getDateUpdated() != null ? step.getDateUpdated() : step.getDateCreated();
    entity.setDateUpdated(updated == null ? new Date() : updated);
    return entity;
  }

  /**
   * Whether a stored step, read as the projection {@code seq, pluginId,
   * pluginState, pluginIsMandatory, htmlPluginDetails, outcomeObjectState,
   * dateCreated, dateUpdated, md5(pluginDetails)}, holds the same values as a
   * step about to be stored (dates compared by instant, as stored ones come back
   * as {@link java.sql.Timestamp}; details by their MD5 digest, which Hibernate
   * returns as the raw digest bytes on PostgreSQL, or else as hex text).
   */
  public static boolean sameStep(Object[] stored, JobReportStep incoming) {
    return Objects.equals(stored[1], incoming.getPluginId()) && stored[2] == incoming.getPluginState()
      && Boolean.TRUE.equals(stored[3]) == incoming.isPluginIsMandatory()
      && Boolean.TRUE.equals(stored[4]) == incoming.isHtmlPluginDetails()
      && stored[5] == incoming.getOutcomeObjectState() && sameInstant((Date) stored[6], incoming.getDateCreated())
      && sameInstant((Date) stored[7], incoming.getDateUpdated()) && sameDigest(stored[8], incoming.getPluginDetails());
  }

  private static boolean sameDigest(Object storedDigest, String details) {
    byte[] digest = md5(details);
    if (storedDigest instanceof byte[] bytes) {
      return Arrays.equals(bytes, digest);
    }
    return storedDigest instanceof String hex && HexFormat.of().formatHex(digest).equalsIgnoreCase(hex);
  }

  /**
   * @return the MD5 digest of the text's UTF-8 bytes, as PostgreSQL's
   *         {@code md5(text)} computes it ({@code null} as empty text)
   */
  static byte[] md5(String text) {
    try {
      return MessageDigest.getInstance("MD5").digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 is not available", e);
    }
  }

  /**
   * Rebuilds the model report from its rows.
   *
   * @param plugins
   *          resolves a plugin id to its descriptor
   */
  public static Report toModel(JobReport entity, List<String> originalIds, List<JobReportStep> steps,
    String instanceId, IntFunction<PluginDescriptor> plugins) {
    Report model = new Report();
    model.injectLineSeparator(System.lineSeparator());
    model.setId(entity.getId());
    model.setJobId(entity.getJobId());
    model.setSourceAndOutcomeObjectId(entity.getSourceObjectId(), entity.getOutcomeObjectId());
    if (entity.getSourceObjectClass() != null) {
      model.setSourceObjectClass(entity.getSourceObjectClass());
    }
    if (entity.getOutcomeObjectClass() != null) {
      model.setOutcomeObjectClass(entity.getOutcomeObjectClass());
    }
    if (entity.getSourceObjectOriginalName() != null) {
      model.setSourceObjectOriginalName(entity.getSourceObjectOriginalName());
    }
    model.setSourceObjectOriginalIds(new ArrayList<>(originalIds));
    model.setTitle(entity.getTitle() == null ? "" : entity.getTitle());
    model.setIngestType(entity.getIngestType() == null ? "" : entity.getIngestType());
    model.setTransactionId(entity.getTransactionId());
    model.setInstanceId(instanceId);
    model.setTotalSteps(entity.getTotalSteps());
    model.setDateCreated(plainDate(entity.getDateCreated()));
    model.setDateUpdated(plainDate(entity.getDateCreated()));

    if (entity.getPluginId() != null) {
      PluginDescriptor plugin = plugins.apply(entity.getPluginId());
      model.setPlugin(plugin.getClassName());
      model.setPluginName(plugin.getName());
      model.setPluginVersion(plugin.getVersion());
    }

    for (JobReportStep step : steps) {
      model.addReport(toStepModel(entity, step, plugins), false);
    }

    // stored on the report itself (it can be changed without adding a step)
    if (entity.getOutcomeObjectState() != null) {
      model.setOutcomeObjectState(entity.getOutcomeObjectState());
    }
    return model;
  }

  private static Report toStepModel(JobReport report, JobReportStep entity, IntFunction<PluginDescriptor> plugins) {
    Report step = new Report();
    step.injectLineSeparator(System.lineSeparator());
    step.setId(report.getId());
    step.setJobId(report.getJobId());
    step.setSourceAndOutcomeObjectId(report.getSourceObjectId(), report.getOutcomeObjectId());
    if (entity.getPluginId() != null) {
      PluginDescriptor plugin = plugins.apply(entity.getPluginId());
      step.setPlugin(plugin.getClassName());
      step.setPluginName(plugin.getName());
      step.setPluginVersion(plugin.getVersion());
      step.setTitle(plugin.getName());
    }
    step.setPluginState(entity.getPluginState());
    step.setPluginIsMandatory(entity.isPluginIsMandatory());
    step.setPluginDetails(entity.getPluginDetails() == null ? "" : entity.getPluginDetails());
    step.setHtmlPluginDetails(entity.isHtmlPluginDetails());
    if (entity.getOutcomeObjectState() != null) {
      step.setOutcomeObjectState(entity.getOutcomeObjectState());
    }
    step.setDateCreated(plainDate(entity.getDateCreated()));
    step.setDateUpdated(plainDate(entity.getDateUpdated()));
    return step;
  }

  // -------------------------------------------------------------- helpers

  private static boolean sameInstant(Date a, Date b) {
    return a == null ? b == null : b != null && a.getTime() == b.getTime();
  }

  private static Date plainDate(Date date) {
    return date == null ? null : new Date(date.getTime());
  }

  private static String emptyToNull(String value) {
    return value == null || value.isEmpty() ? null : value;
  }
}
