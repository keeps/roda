package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalRuleCollection extends AbstractSolrCollection<DisposalRule, DisposalRule> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalRuleCollection.class);

  @Override
  public Class<DisposalRule> getIndexClass() {
    return DisposalRule.class;
  }

  @Override
  public Class<DisposalRule> getModelClass() {
    return DisposalRule.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_DISPOSAL_RULE;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_DISPOSAL_RULE);
  }

  @Override
  public String getUniqueId(DisposalRule modelObject) {
    return modelObject.getUUID();
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DISPOSAL_RULE_ORDER, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.DISPOSAL_RULE_TITLE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_RULE_DESCRIPTION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_RULE_SELECTION_METHOD, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_RULE_CONDITION_KEY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_RULE_CONDITION_VALUE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_RULE_SCHEDULE_ID, Field.TYPE_STRING));

    return fields;
  }

  @Override
  public SolrInputDocument toSolrDocument(ModelService model, DisposalRule rule, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(model, rule, info);

    doc.addField(RodaConstants.DISPOSAL_RULE_ORDER, rule.getOrder());
    doc.addField(RodaConstants.DISPOSAL_RULE_TITLE, rule.getTitle());
    doc.addField(RodaConstants.DISPOSAL_RULE_DESCRIPTION, rule.getDescription());
    doc.addField(RodaConstants.DISPOSAL_RULE_SELECTION_METHOD, rule.getType().toString());
    doc.addField(RodaConstants.DISPOSAL_RULE_CONDITION_KEY, rule.getConditionKey());
    doc.addField(RodaConstants.DISPOSAL_RULE_CONDITION_VALUE, rule.getConditionValue());
    doc.addField(RodaConstants.DISPOSAL_RULE_SCHEDULE_ID, rule.getDisposalScheduleId());

    return doc;
  }

  @Override
  public DisposalRule fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final DisposalRule rule = super.fromSolrDocument(doc, fieldsToReturn);

    rule.setOrder(SolrUtils.objectToInteger(doc.get(RodaConstants.DISPOSAL_RULE_ORDER), null));
    rule.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_RULE_TITLE), null));
    rule.setDescription(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_RULE_DESCRIPTION), null));
    rule.setConditionKey(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_RULE_CONDITION_KEY), null));
    rule.setConditionValue(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_RULE_CONDITION_VALUE), null));
    rule.setDisposalScheduleId(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_RULE_SCHEDULE_ID), null));
    ConditionType conditionType = SolrUtils.objectToEnum(doc.get(RodaConstants.DISPOSAL_RULE_SELECTION_METHOD),
      ConditionType.class, ConditionType.getDefault());
    rule.setType(conditionType);

    return rule;
  }
}
