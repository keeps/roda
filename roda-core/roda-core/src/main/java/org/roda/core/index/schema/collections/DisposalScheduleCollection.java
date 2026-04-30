package org.roda.core.index.schema.collections;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalScheduleCollection extends AbstractSolrCollection<DisposalSchedule, DisposalSchedule> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalScheduleCollection.class);

  @Override
  public Class<DisposalSchedule> getIndexClass() {
    return DisposalSchedule.class;
  }

  @Override
  public Class<DisposalSchedule> getModelClass() {
    return DisposalSchedule.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_DISPOSAL_SCHEDULE;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_DISPOSAL_SCHEDULE);
  }

  @Override
  public String getUniqueId(DisposalSchedule modelObject) {
    return modelObject.getUUID();
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_TITLE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_DESCRIPTION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_MANDATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_SCOPE_NOTES, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_STATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_ACTION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_INTERVAL_CODE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_DURATION, Field.TYPE_INT));

    return fields;
  }

  @Override
  public SolrInputDocument toSolrDocument(ModelService model, DisposalSchedule disposalSchedule,
    IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(model, disposalSchedule, info);

    doc.addField(RodaConstants.DISPOSAL_SCHEDULE_TITLE, disposalSchedule.getTitle());
    doc.addField(RodaConstants.DISPOSAL_SCHEDULE_DESCRIPTION, disposalSchedule.getDescription());
    doc.addField(RodaConstants.DISPOSAL_SCHEDULE_MANDATE, disposalSchedule.getMandate());
    doc.addField(RodaConstants.DISPOSAL_SCHEDULE_SCOPE_NOTES, disposalSchedule.getScopeNotes());
    if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(disposalSchedule.getActionCode())) {
      doc.addField(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_INTERVAL_CODE,
              disposalSchedule.getRetentionPeriodIntervalCode().toString());
      doc.addField(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_DURATION,
              disposalSchedule.getRetentionPeriodDuration());
    }
    doc.addField(RodaConstants.DISPOSAL_SCHEDULE_STATE, disposalSchedule.getState().toString());
    doc.addField(RodaConstants.DISPOSAL_SCHEDULE_ACTION, disposalSchedule.getActionCode().toString());

    return doc;
  }

  @Override
  public DisposalSchedule fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final DisposalSchedule disposalSchedule = super.fromSolrDocument(doc, fieldsToReturn);

    disposalSchedule.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_SCHEDULE_TITLE), null));
    disposalSchedule
      .setDescription(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_SCHEDULE_DESCRIPTION), null));
    disposalSchedule.setMandate(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_SCHEDULE_MANDATE), null));
    disposalSchedule
      .setScopeNotes(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_SCHEDULE_SCOPE_NOTES), null));
    DisposalScheduleState disposalScheduleState = SolrUtils.objectToEnum(doc.get(RodaConstants.DISPOSAL_SCHEDULE_STATE),
      DisposalScheduleState.class, DisposalScheduleState.getDefault());
    disposalSchedule.setState(disposalScheduleState);
    disposalSchedule.setRetentionPeriodDuration(
      SolrUtils.objectToInteger(doc.get(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_DURATION), null));
    RetentionPeriodIntervalCode retentionPeriodIntervalCode = SolrUtils.objectToEnum(
      doc.get(RodaConstants.DISPOSAL_SCHEDULE_RETENTION_PERIOD_INTERVAL_CODE), RetentionPeriodIntervalCode.class,
      RetentionPeriodIntervalCode.getDefault());
    disposalSchedule.setRetentionPeriodIntervalCode(retentionPeriodIntervalCode);
    DisposalActionCode actionCode = SolrUtils.objectToEnum(doc.get(RodaConstants.DISPOSAL_SCHEDULE_ACTION),
      DisposalActionCode.class, null);
    disposalSchedule.setActionCode(actionCode);

    return disposalSchedule;
  }
}
