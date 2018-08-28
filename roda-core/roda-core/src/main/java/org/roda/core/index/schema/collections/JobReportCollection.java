package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobReportCollection extends AbstractSolrCollection<IndexedReport, Report> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobReportCollection.class);

  @Override
  public Class<IndexedReport> getIndexClass() {
    return IndexedReport.class;
  }

  @Override
  public Class<Report> getModelClass() {
    return Report.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_JOB_REPORT;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_JOB_REPORT);
  }

  @Override
  public String getUniqueId(Report modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.JOB_REPORT_JOB_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_TITLE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_DATE_CREATED, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.JOB_REPORT_DATE_UPDATED, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_REPORT_STEPS_COMPLETED, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_REPORT_TOTAL_STEPS, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.JOB_REPORT_PLUGIN, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_PLUGIN_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_PLUGIN_VERSION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_PLUGIN_STATE, Field.TYPE_STRING));
    fields
      .add(new Field(RodaConstants.JOB_REPORT_PLUGIN_DETAILS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));
    fields.add(new Field(RodaConstants.JOB_REPORT_HTML_PLUGIN_DETAILS, Field.TYPE_BOOLEAN).setIndexed(false)
      .setDocValues(false));
    fields.add(new Field(RodaConstants.JOB_REPORT_REPORTS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));
    fields.add(new Field(RodaConstants.JOB_REPORT_JOB_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.JOB_REPORT_JOB_PLUGIN_TYPE, Field.TYPE_STRING));
    fields.add(
      new Field(RodaConstants.JOB_REPORT_SUCCESSFUL_PLUGINS, Field.TYPE_STRING).setMultiValued(true).setStored(false));
    fields.add(new Field(RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS, Field.TYPE_STRING).setMultiValued(true)
      .setStored(false));
    fields.add(new Field(RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS_COUNTER, Field.TYPE_INT).setStored(false));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(Report jobReport, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(jobReport, info);

    doc.addField(RodaConstants.JOB_REPORT_JOB_ID, jobReport.getJobId());
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID, jobReport.getSourceObjectId());
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS, jobReport.getSourceObjectOriginalIds());
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME, jobReport.getSourceObjectOriginalName());
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID, jobReport.getOutcomeObjectId());
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE, jobReport.getOutcomeObjectState().toString());
    doc.addField(RodaConstants.JOB_REPORT_TITLE, jobReport.getTitle());
    doc.addField(RodaConstants.JOB_REPORT_DATE_CREATED, SolrUtils.formatDateWithMillis(jobReport.getDateCreated()));
    doc.addField(RodaConstants.JOB_REPORT_DATE_UPDATED, SolrUtils.formatDateWithMillis(jobReport.getDateUpdated()));
    doc.addField(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE, jobReport.getCompletionPercentage());
    doc.addField(RodaConstants.JOB_REPORT_STEPS_COMPLETED, jobReport.getStepsCompleted());
    doc.addField(RodaConstants.JOB_REPORT_TOTAL_STEPS, jobReport.getTotalSteps());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN, jobReport.getPlugin());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_NAME, jobReport.getPluginName());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_VERSION, jobReport.getPluginVersion());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_STATE, jobReport.getPluginState().toString());
    doc.addField(RodaConstants.JOB_REPORT_PLUGIN_DETAILS, jobReport.getPluginDetails());
    doc.addField(RodaConstants.JOB_REPORT_HTML_PLUGIN_DETAILS, jobReport.isHtmlPluginDetails());
    doc.addField(RodaConstants.JOB_REPORT_REPORTS, JsonUtils.getJsonFromObject(jobReport.getReports()));
    doc.addField(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS, jobReport.getSourceObjectClass());
    doc.addField(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS, jobReport.getOutcomeObjectClass());

    return doc;
  }

  public static class Info extends IndexingAdditionalInfo {

    private final Report jobReport;
    private final Job job;

    public Info(Report jobReport, Job job) {
      super();
      this.jobReport = jobReport;
      this.job = job;
    }

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      Map<String, Object> preCalculatedFields = new HashMap<>();

      SolrClient index = RodaCoreFactory.getIndexService().getSolrClient();

      preCalculatedFields.put(RodaConstants.JOB_REPORT_JOB_NAME, job.getName());
      preCalculatedFields.put(RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL,
        SolrUtils.getObjectLabel(index, jobReport.getSourceObjectClass(), jobReport.getSourceObjectId()));
      preCalculatedFields.put(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL,
        SolrUtils.getObjectLabel(index, jobReport.getOutcomeObjectClass(), jobReport.getOutcomeObjectId()));
      preCalculatedFields.put(RodaConstants.JOB_REPORT_JOB_PLUGIN_TYPE, SolrUtils.formatEnum(job.getPluginType()));

      List<String> successfulPlugins = new ArrayList<>();
      List<String> unsuccessfulPlugins = new ArrayList<>();

      if (job.getPluginType().equals(PluginType.INGEST)) {
        for (Report item : jobReport.getReports()) {
          if (item.getPluginState().equals(PluginState.SUCCESS)) {
            successfulPlugins.add(item.getPluginName());
          } else {
            unsuccessfulPlugins.add(item.getPluginName());
          }
        }

        preCalculatedFields.put(RodaConstants.JOB_REPORT_SUCCESSFUL_PLUGINS, successfulPlugins);
        preCalculatedFields.put(RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS, unsuccessfulPlugins);
        preCalculatedFields.put(RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS_COUNTER, unsuccessfulPlugins.size());
      }
      return preCalculatedFields;
    }

  }

  @Override
  public IndexedReport fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {
    final IndexedReport jobReport = super.fromSolrDocument(doc, fieldsToReturn);

    jobReport.setJobId(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_JOB_ID), null));
    String sourceObjectId = SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ID), null);
    String outcomeObjectId = SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_ID), null);
    jobReport.setSourceAndOutcomeObjectId(sourceObjectId, outcomeObjectId);
    jobReport
      .setSourceObjectClass(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_CLASS), null));
    jobReport.setSourceObjectOriginalIds(
      SolrUtils.objectToListString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS)));
    jobReport.setSourceObjectOriginalName(
      SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME), null));
    jobReport
      .setOutcomeObjectClass(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_CLASS), null));
    if (doc.containsKey(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE)) {
      jobReport.setOutcomeObjectState(AIPState.valueOf(SolrUtils
        .objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_STATE), AIPState.getDefault().toString())));
    }
    jobReport.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_TITLE), null));
    jobReport.setDateCreated(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.JOB_REPORT_DATE_CREATED)));
    jobReport.setDateUpdated(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.JOB_REPORT_DATE_UPDATED)));
    jobReport
      .setCompletionPercentage(SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_REPORT_COMPLETION_PERCENTAGE), 0));
    jobReport.setStepsCompleted(SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_REPORT_STEPS_COMPLETED), 0));
    jobReport.setTotalSteps(SolrUtils.objectToInteger(doc.get(RodaConstants.JOB_REPORT_TOTAL_STEPS), 0));
    jobReport.setPlugin(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN), null));
    jobReport.setPluginName(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_NAME), null));
    jobReport.setPluginVersion(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_VERSION), null));
    jobReport.setPluginState(
      PluginState.valueOf(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_STATE), null)));
    jobReport.setPluginDetails(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_PLUGIN_DETAILS), null));
    jobReport
      .setHtmlPluginDetails(SolrUtils.objectToBoolean(doc.get(RodaConstants.JOB_REPORT_HTML_PLUGIN_DETAILS), false));
    try {
      if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.JOB_REPORT_REPORTS)) {
        jobReport.setReports(JsonUtils
          .getListFromJson(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_REPORTS), ""), Report.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error parsing report in job report", e);
    }

    jobReport.setJobName(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_JOB_NAME), null));
    jobReport
      .setSourceObjectLabel(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_SOURCE_OBJECT_LABEL), null));
    jobReport
      .setOutcomeObjectLabel(SolrUtils.objectToString(doc.get(RodaConstants.JOB_REPORT_OUTCOME_OBJECT_LABEL), null));

    jobReport.setSuccessfulPlugins(SolrUtils.objectToListString(doc.get(RodaConstants.JOB_REPORT_SUCCESSFUL_PLUGINS)));
    jobReport
      .setUnsuccessfulPlugins(SolrUtils.objectToListString(doc.get(RodaConstants.JOB_REPORT_UNSUCCESSFUL_PLUGINS)));

    return jobReport;
  }

}
