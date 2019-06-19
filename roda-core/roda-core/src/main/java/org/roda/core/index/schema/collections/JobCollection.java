/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobCollection extends AbstractSolrCollection<Job, Job> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobCollection.class);

  @Override
  public Class<Job> getIndexClass() {
    return Job.class;
  }

  @Override
  public Class<Job> getModelClass() {
    return Job.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_JOB;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_JOB, RodaConstants.INDEX_JOB_REPORT);
  }

  @Override
  public String getUniqueId(Job modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.JOB_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_USERNAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_START_DATE, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.JOB_END_DATE, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.JOB_STATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_STATE_DETAILS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));
    fields.add(new Field(RodaConstants.JOB_COMPLETION_PERCENTAGE, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_PLUGIN, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_PLUGIN_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_PLUGIN_PARAMETERS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));
    fields.add(new Field(RodaConstants.JOB_SOURCE_OBJECTS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));
    fields.add(new Field(RodaConstants.JOB_OUTCOME_OBJECTS_CLASS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_SOURCE_OBJECTS_COUNT, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_SOURCE_OBJECTS_BEING_PROCESSED, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_HAS_FAILURES, Field.TYPE_BOOLEAN).setStored(false));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getTextCopyFieldOf(RodaConstants.JOB_NAME));
  }

  @Override
  public SolrInputDocument toSolrDocument(Job job, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(job, info);

    doc.addField(RodaConstants.JOB_NAME, job.getName());
    doc.addField(RodaConstants.JOB_USERNAME, job.getUsername());
    doc.addField(RodaConstants.JOB_START_DATE, SolrUtils.formatDateWithMillis(job.getStartDate()));
    doc.addField(RodaConstants.JOB_END_DATE, SolrUtils.formatDateWithMillis(job.getEndDate()));
    doc.addField(RodaConstants.JOB_STATE, job.getState().toString());
    doc.addField(RodaConstants.JOB_STATE_DETAILS, job.getStateDetails());
    JobStats jobStats = job.getJobStats();
    doc.addField(RodaConstants.JOB_COMPLETION_PERCENTAGE, jobStats.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_COUNT, jobStats.getSourceObjectsCount());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED,
      jobStats.getSourceObjectsWaitingToBeProcessed());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_BEING_PROCESSED, jobStats.getSourceObjectsBeingProcessed());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS,
      jobStats.getSourceObjectsProcessedWithSuccess());
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE,
      jobStats.getSourceObjectsProcessedWithFailure());
    doc.addField(RodaConstants.JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION,
      jobStats.getOutcomeObjectsWithManualIntervention());
    doc.addField(RodaConstants.JOB_PLUGIN_TYPE, job.getPluginType().toString());
    doc.addField(RodaConstants.JOB_PLUGIN, job.getPlugin());
    doc.addField(RodaConstants.JOB_PLUGIN_PARAMETERS, JsonUtils.getJsonFromObject(job.getPluginParameters()));
    doc.addField(RodaConstants.JOB_SOURCE_OBJECTS, JsonUtils.getJsonFromObject(job.getSourceObjects()));
    doc.addField(RodaConstants.JOB_OUTCOME_OBJECTS_CLASS, job.getOutcomeObjectsClass());
    doc.addField(RodaConstants.JOB_HAS_FAILURES,
      jobStats.getSourceObjectsProcessedWithFailure() > 0
        || (jobStats.getSourceObjectsCount() > jobStats.getSourceObjectsProcessedWithSuccess()
          + jobStats.getSourceObjectsBeingProcessed() + jobStats.getSourceObjectsWaitingToBeProcessed()));

    return doc;
  }

  @Override
  public Job fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final Job job = super.fromSolrDocument(doc, fieldsToReturn);

    job.setName(SolrUtils.objectToString(doc.get(RodaConstants.JOB_NAME), null));
    job.setUsername(SolrUtils.objectToString(doc.get(RodaConstants.JOB_USERNAME), null));
    job.setStartDate(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.JOB_START_DATE)));
    job.setEndDate(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.JOB_END_DATE)));
    if (doc.containsKey(RodaConstants.JOB_STATE)) {
      job.setState(JOB_STATE.valueOf(SolrUtils.objectToString(doc.get(RodaConstants.JOB_STATE), null)));
    }
    job.setStateDetails(SolrUtils.objectToString(doc.get(RodaConstants.JOB_STATE_DETAILS), null));
    JobStats jobStats = job.getJobStats();
    jobStats.setCompletionPercentage(SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_COMPLETION_PERCENTAGE), 0));
    jobStats.setSourceObjectsCount(SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_COUNT), 0));
    jobStats.setSourceObjectsWaitingToBeProcessed(
      SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED), 0));
    jobStats.setSourceObjectsBeingProcessed(
      SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_BEING_PROCESSED), 0));
    jobStats.setSourceObjectsProcessedWithSuccess(
      SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS), 0));
    jobStats.setSourceObjectsProcessedWithFailure(
      SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE), 0));
    jobStats.setOutcomeObjectsWithManualIntervention(
      SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION), 0));
    if (doc.containsKey(RodaConstants.JOB_PLUGIN_TYPE)) {
      job.setPluginType(PluginType.valueOf(SolrUtils.objectToString(doc.get(RodaConstants.JOB_PLUGIN_TYPE), null)));
    }
    job.setPlugin(SolrUtils.objectToString(doc.get(RodaConstants.JOB_PLUGIN), null));
    if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.JOB_PLUGIN_PARAMETERS)) {
      job.setPluginParameters(
        JsonUtils.getMapFromJson(SolrUtils.objectToString(doc.get(RodaConstants.JOB_PLUGIN_PARAMETERS), "")));
    }

    try {
      if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.JOB_SOURCE_OBJECTS)) {
        job.setSourceObjects(JsonUtils.getObjectFromJson(
          SolrUtils.objectToString(doc.get(RodaConstants.JOB_SOURCE_OBJECTS), ""), SelectedItems.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error parsing report in job objects", e);
    }
    job.setOutcomeObjectsClass(SolrUtils.objectToString(doc.get(RodaConstants.JOB_OUTCOME_OBJECTS_CLASS), null));

    return job;
  }

}
