package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;

public class RiskCollection extends AbstractSolrCollection<IndexedRisk, Risk> {

  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(RiskCollection.class);

  @Override
  public Class<IndexedRisk> getIndexClass() {
    return IndexedRisk.class;
  }

  @Override
  public Class<Risk> getModelClass() {
    return Risk.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_RISK;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_RISK, RodaConstants.INDEX_RISK_INCIDENCE);
  }

  @Override
  public String getUniqueId(Risk modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.RISK_NAME, Field.TYPE_TEXT).setRequired(true).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_IDENTIFIED_ON, Field.TYPE_DATE).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_IDENTIFIED_BY, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_CATEGORIES, Field.TYPE_STRING).setRequired(true).setMultiValued(true));
    fields.add(new Field(RodaConstants.RISK_NOTES, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, Field.TYPE_INT).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_PRE_MITIGATION_IMPACT, Field.TYPE_INT).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_PRE_MITIGATION_SEVERITY, Field.TYPE_INT).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_PRE_MITIGATION_NOTES, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_POST_MITIGATION_PROBABILITY, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.RISK_POST_MITIGATION_IMPACT, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.RISK_POST_MITIGATION_SEVERITY, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_POST_MITIGATION_NOTES, Field.TYPE_TEXT).setMultiValued(false));
    fields
      .add(new Field(RodaConstants.RISK_CURRENT_SEVERITY_LEVEL, Field.TYPE_STRING).setStored(false).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_MITIGATION_STRATEGY, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_MITIGATION_OWNER_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_MITIGATION_OWNER, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_CREATED_ON, Field.TYPE_DATE).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_CREATED_BY, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_UPDATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.RISK_UPDATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_INCIDENCES_COUNT, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT, Field.TYPE_INT));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(Risk risk, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(risk, info);

    doc.addField(RodaConstants.RISK_NAME, risk.getName());
    doc.addField(RodaConstants.RISK_DESCRIPTION, risk.getDescription());
    doc.addField(RodaConstants.RISK_IDENTIFIED_ON, SolrUtils.formatDate(risk.getIdentifiedOn()));
    doc.addField(RodaConstants.RISK_IDENTIFIED_BY, risk.getIdentifiedBy());
    doc.addField(RodaConstants.RISK_CATEGORIES, risk.getCategories());
    doc.addField(RodaConstants.RISK_NOTES, risk.getNotes());

    doc.addField(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY, risk.getPreMitigationProbability());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_IMPACT, risk.getPreMitigationImpact());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_SEVERITY, risk.getPreMitigationSeverity());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL, risk.getPreMitigationSeverityLevel().toString());
    doc.addField(RodaConstants.RISK_PRE_MITIGATION_NOTES, risk.getPreMitigationNotes());

    doc.addField(RodaConstants.RISK_POST_MITIGATION_PROBABILITY, risk.getPostMitigationProbability());
    doc.addField(RodaConstants.RISK_POST_MITIGATION_IMPACT, risk.getPostMitigationImpact());
    doc.addField(RodaConstants.RISK_POST_MITIGATION_SEVERITY, risk.getPostMitigationSeverity());

    if (risk.getPostMitigationSeverityLevel() != null) {
      doc.addField(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL, risk.getPostMitigationSeverityLevel().toString());
    }

    doc.addField(RodaConstants.RISK_CURRENT_SEVERITY_LEVEL, risk.getCurrentSeverityLevel().toString());
    doc.addField(RodaConstants.RISK_POST_MITIGATION_NOTES, risk.getPostMitigationNotes());

    doc.addField(RodaConstants.RISK_MITIGATION_STRATEGY, risk.getMitigationStrategy());
    doc.addField(RodaConstants.RISK_MITIGATION_OWNER_TYPE, risk.getMitigationOwnerType());
    doc.addField(RodaConstants.RISK_MITIGATION_OWNER, risk.getMitigationOwner());
    doc.addField(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE,
      risk.getMitigationRelatedEventIdentifierType());
    doc.addField(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE,
      risk.getMitigationRelatedEventIdentifierValue());

    doc.addField(RodaConstants.RISK_CREATED_ON, SolrUtils.formatDate(risk.getCreatedOn()));
    doc.addField(RodaConstants.RISK_CREATED_BY, risk.getCreatedBy());
    doc.addField(RodaConstants.RISK_UPDATED_ON, SolrUtils.formatDate(risk.getUpdatedOn()));
    doc.addField(RodaConstants.RISK_UPDATED_BY, risk.getUpdatedBy());

    // TODO calculate incidences count here

    return doc;
  }

  public static class Info extends IndexingAdditionalInfo {

    private final Risk risk;
    private final int incidences;

    public Info(Risk risk, int incidences) {
      super();
      this.risk = risk;
      this.incidences = incidences;
    }

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      Map<String, Object> preCalculatedFields = new HashMap<>();
      if (risk instanceof IndexedRisk) {
        preCalculatedFields.put(RodaConstants.RISK_INCIDENCES_COUNT, ((IndexedRisk) risk).getIncidencesCount());
        preCalculatedFields.put(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT,
          ((IndexedRisk) risk).getUnmitigatedIncidencesCount());
      } else {
        preCalculatedFields.put(RodaConstants.RISK_INCIDENCES_COUNT, incidences);
        preCalculatedFields.put(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT, incidences);
      }
      return preCalculatedFields;
    }

  }

  @Override
  public IndexedRisk fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final IndexedRisk risk = super.fromSolrDocument(doc, fieldsToReturn);

    risk.setName(SolrUtils.objectToString(doc.get(RodaConstants.RISK_NAME), null));
    risk.setDescription(SolrUtils.objectToString(doc.get(RodaConstants.RISK_DESCRIPTION), null));
    risk.setIdentifiedOn(SolrUtils.objectToDate(doc.get(RodaConstants.RISK_IDENTIFIED_ON)));
    risk.setIdentifiedBy(SolrUtils.objectToString(doc.get(RodaConstants.RISK_IDENTIFIED_BY), null));
    risk.setCategories(SolrUtils.objectToListString(doc.get(RodaConstants.RISK_CATEGORIES)));
    risk.setNotes(SolrUtils.objectToString(doc.get(RodaConstants.RISK_NOTES), null));

    risk.setPreMitigationProbability(
      SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_PROBABILITY), 0));
    risk.setPreMitigationImpact(SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_IMPACT), 0));
    risk.setPreMitigationSeverity(SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_PRE_MITIGATION_SEVERITY), 0));
    if (doc.containsKey(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL)) {
      risk.setPreMitigationSeverityLevel(SolrUtils
        .objectToEnum(doc.get(RodaConstants.RISK_PRE_MITIGATION_SEVERITY_LEVEL), Risk.SEVERITY_LEVEL.class, null));
    }
    risk.setPreMitigationNotes(SolrUtils.objectToString(doc.get(RodaConstants.RISK_PRE_MITIGATION_NOTES), null));

    risk.setPostMitigationProbability(
      SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_POST_MITIGATION_PROBABILITY), 0));
    risk.setPostMitigationImpact(SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_POST_MITIGATION_IMPACT), 0));
    risk.setPostMitigationSeverity(SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_POST_MITIGATION_SEVERITY), 0));
    if (doc.containsKey(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL)) {
      risk.setPostMitigationSeverityLevel(SolrUtils
        .objectToEnum(doc.get(RodaConstants.RISK_POST_MITIGATION_SEVERITY_LEVEL), Risk.SEVERITY_LEVEL.class, null));
    }
    risk.setPostMitigationNotes(SolrUtils.objectToString(doc.get(RodaConstants.RISK_POST_MITIGATION_NOTES), null));

    risk.setMitigationStrategy(SolrUtils.objectToString(doc.get(RodaConstants.RISK_MITIGATION_STRATEGY), null));
    risk.setMitigationOwnerType(SolrUtils.objectToString(doc.get(RodaConstants.RISK_MITIGATION_OWNER_TYPE), null));
    risk.setMitigationOwner(SolrUtils.objectToString(doc.get(RodaConstants.RISK_MITIGATION_OWNER), null));
    risk.setMitigationRelatedEventIdentifierType(
      SolrUtils.objectToString(doc.get(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE), null));
    risk.setMitigationRelatedEventIdentifierValue(
      SolrUtils.objectToString(doc.get(RodaConstants.RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE), null));

    risk.setCreatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.RISK_CREATED_ON)));
    risk.setCreatedBy(SolrUtils.objectToString(doc.get(RodaConstants.RISK_CREATED_BY), null));
    risk.setUpdatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.RISK_UPDATED_ON)));
    risk.setUpdatedBy(SolrUtils.objectToString(doc.get(RodaConstants.RISK_UPDATED_BY), null));

    risk.setIncidencesCount(SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_INCIDENCES_COUNT), 0));
    risk.setUnmitigatedIncidencesCount(
      SolrUtils.objectToInteger(doc.get(RodaConstants.RISK_UNMITIGATED_INCIDENCES_COUNT), 0));

    return risk;

  }

}
