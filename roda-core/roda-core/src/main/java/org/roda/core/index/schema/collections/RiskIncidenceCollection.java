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

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;

public class RiskIncidenceCollection extends AbstractSolrCollection<RiskIncidence, RiskIncidence> {

  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(RiskCollection.class);

  @Override
  public Class<RiskIncidence> getIndexClass() {
    return RiskIncidence.class;
  }

  @Override
  public Class<RiskIncidence> getModelClass() {
    return RiskIncidence.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_RISK_INCIDENCE;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_RISK_INCIDENCE);
  }

  @Override
  public String getUniqueId(RiskIncidence modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.RISK_INCIDENCE_AIP_ID, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_FILE_PATH, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_FILE_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_RISK_ID, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_BYPLUGIN, Field.TYPE_BOOLEAN).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_STATUS, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_SEVERITY, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_DETECTED_ON, Field.TYPE_DATE).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_DETECTED_BY, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_MITIGATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_MITIGATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED, Field.TYPE_STRING).setStored(false));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(RiskIncidence incidence, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = super.toSolrDocument(incidence, info);

    doc.addField(RodaConstants.RISK_INCIDENCE_AIP_ID, incidence.getAipId());
    doc.addField(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, incidence.getRepresentationId());
    doc.addField(RodaConstants.RISK_INCIDENCE_FILE_PATH, incidence.getFilePath());
    doc.addField(RodaConstants.RISK_INCIDENCE_FILE_ID, incidence.getFileId());
    doc.addField(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS, incidence.getObjectClass());
    doc.addField(RodaConstants.RISK_INCIDENCE_RISK_ID, incidence.getRiskId());
    doc.addField(RodaConstants.RISK_INCIDENCE_DESCRIPTION, incidence.getDescription());
    doc.addField(RodaConstants.RISK_INCIDENCE_BYPLUGIN, incidence.isByPlugin());
    doc.addField(RodaConstants.RISK_INCIDENCE_STATUS, incidence.getStatus().toString());
    doc.addField(RodaConstants.RISK_INCIDENCE_SEVERITY, incidence.getSeverity().toString());
    doc.addField(RodaConstants.RISK_INCIDENCE_DETECTED_ON, SolrUtils.formatDate(incidence.getDetectedOn()));
    doc.addField(RodaConstants.RISK_INCIDENCE_DETECTED_BY, incidence.getDetectedBy());
    doc.addField(RodaConstants.RISK_INCIDENCE_MITIGATED_ON, SolrUtils.formatDate(incidence.getMitigatedOn()));
    doc.addField(RodaConstants.RISK_INCIDENCE_MITIGATED_BY, incidence.getMitigatedBy());
    doc.addField(RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION, incidence.getMitigatedDescription());
    doc.addField(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED,
      StringUtils.join(incidence.getFilePath(), RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR));

    return doc;
  }

  @Override
  public RiskIncidence fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {
    final RiskIncidence incidence = super.fromSolrDocument(doc, fieldsToReturn);

    incidence.setAipId(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_AIP_ID), null));
    incidence
      .setRepresentationId(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID), null));
    incidence.setFilePath(SolrUtils.objectToListString(doc.get(RodaConstants.RISK_INCIDENCE_FILE_PATH)));
    incidence.setFileId(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_FILE_ID), null));
    incidence.setObjectClass(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS), null));
    incidence.setRiskId(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_RISK_ID), null));
    incidence.setDescription(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_DESCRIPTION), null));
    if (doc.containsKey(RodaConstants.RISK_INCIDENCE_STATUS)) {
      incidence.setStatus(IncidenceStatus.valueOf(SolrUtils
        .objectToString(doc.get(RodaConstants.RISK_INCIDENCE_STATUS), IncidenceStatus.UNMITIGATED.toString())));
    }
    if (doc.containsKey(RodaConstants.RISK_INCIDENCE_SEVERITY)) {
      incidence.setSeverity(SeverityLevel.valueOf(
        SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_SEVERITY), SeverityLevel.MODERATE.toString())));
    }
    incidence.setDetectedOn(SolrUtils.objectToDate(doc.get(RodaConstants.RISK_INCIDENCE_DETECTED_ON)));
    incidence.setDetectedBy(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_DETECTED_BY), null));
    incidence.setMitigatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.RISK_INCIDENCE_MITIGATED_ON)));
    incidence.setMitigatedBy(SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_MITIGATED_BY), null));
    incidence.setMitigatedDescription(
      SolrUtils.objectToString(doc.get(RodaConstants.RISK_INCIDENCE_MITIGATED_DESCRIPTION), null));

    return incidence;
  }

}
