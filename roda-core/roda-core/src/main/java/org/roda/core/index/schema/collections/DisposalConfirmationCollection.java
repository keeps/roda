package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmation;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalConfirmationCollection extends AbstractSolrCollection<DisposalConfirmation, DisposalConfirmation> {
  @Override
  public Class<DisposalConfirmation> getIndexClass() {
    return DisposalConfirmation.class;
  }

  @Override
  public Class<DisposalConfirmation> getModelClass() {
    return DisposalConfirmation.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_DISPOSAL_CONFIRMATION;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Collections.singletonList(RodaConstants.INDEX_DISPOSAL_CONFIRMATION);
  }

  @Override
  public String getUniqueId(DisposalConfirmation modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_TITLE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_EXTRA_INFO, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_EXECUTED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_EXECUTED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_RESTORED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_RESTORED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_STATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_STORAGE_SIZE, Field.TYPE_LONG));

    return fields;
  }

  @Override
  public SolrInputDocument toSolrDocument(DisposalConfirmation confirmation, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(confirmation, info);

    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_TITLE, confirmation.getTitle());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_EXTRA_INFO,
      JsonUtils.getJsonFromObject(confirmation.getExtraFields()));
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY, confirmation.getCreatedBy());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON,
      SolrUtils.formatDateWithMillis(confirmation.getCreatedOn()));
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_EXECUTED_BY, confirmation.getExecutedBy());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_EXECUTED_ON,
      SolrUtils.formatDateWithMillis(confirmation.getExecutedOn()));
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_RESTORED_BY, confirmation.getRestoredBy());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_RESTORED_ON,
      SolrUtils.formatDateWithMillis(confirmation.getRestoredOn()));
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_STATE, confirmation.getState().toString());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS, confirmation.getNumberOfAIPs());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_STORAGE_SIZE, confirmation.getSize());

    return doc;
  }

  @Override
  public DisposalConfirmation fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final DisposalConfirmation confirmation = super.fromSolrDocument(doc, fieldsToReturn);

    confirmation.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_TITLE), null));

    if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.DISPOSAL_CONFIRMATION_EXTRA_INFO)) {
      confirmation.setExtraFields(JsonUtils
        .getMapFromJson(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_EXTRA_INFO), "")));
    }

    confirmation
      .setCreatedOn(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON)));
    confirmation.setCreatedBy(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY), null));
    confirmation
      .setExecutedOn(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_EXECUTED_ON)));
    confirmation
      .setExecutedBy(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_EXECUTED_BY), null));
    confirmation
      .setRestoredOn(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_RESTORED_ON)));
    confirmation
      .setRestoredBy(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_RESTORED_BY), null));
    if (doc.containsKey(RodaConstants.JOB_STATE)) {
      confirmation.setState(DisposalConfirmationState
        .valueOf(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_STATE), null)));
    }
    confirmation
      .setNumberOfAIPs(SolrUtils.objectToLong(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS), 0L));
    confirmation.setSize(SolrUtils.objectToLong(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_STORAGE_SIZE), 0L));

    return confirmation;
  }
}
