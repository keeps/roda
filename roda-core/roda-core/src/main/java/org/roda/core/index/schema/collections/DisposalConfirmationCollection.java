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
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
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
public class DisposalConfirmationCollection extends AbstractSolrCollection<DisposalConfirmationMetadata, DisposalConfirmationMetadata> {
  @Override
  public Class<DisposalConfirmationMetadata> getIndexClass() {
    return DisposalConfirmationMetadata.class;
  }

  @Override
  public Class<DisposalConfirmationMetadata> getModelClass() {
    return DisposalConfirmationMetadata.class;
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
  public String getUniqueId(DisposalConfirmationMetadata modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_UPDATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_UPDATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_APPROVER, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_STATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_COLLECTIONS, Field.TYPE_LONG));

    return fields;
  }

  @Override
  public SolrInputDocument toSolrDocument(DisposalConfirmationMetadata confirmation, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(confirmation, info);

    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY, confirmation.getCreatedBy());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON,
      SolrUtils.formatDateWithMillis(confirmation.getCreatedOn()));
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_UPDATED_BY, confirmation.getUpdatedBy());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_UPDATED_ON,
      SolrUtils.formatDateWithMillis(confirmation.getUpdatedOn()));
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_STATE, confirmation.getState().toString());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_APPROVER, confirmation.getApprover());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS, confirmation.getNumberOfAIPs());
    doc.addField(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_COLLECTIONS, confirmation.getNumberOfCollections());

    return doc;
  }

  @Override
  public DisposalConfirmationMetadata fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final DisposalConfirmationMetadata confirmation = super.fromSolrDocument(doc, fieldsToReturn);

    confirmation
      .setCreatedOn(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_ON)));
    confirmation.setCreatedBy(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_CREATED_BY), null));
    confirmation
      .setUpdatedOn(SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_UPDATED_ON)));
    confirmation.setUpdatedBy(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_UPDATED_BY), null));
    confirmation.setApprover(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_APPROVER), null));
    if (doc.containsKey(RodaConstants.JOB_STATE)) {
      confirmation.setState(DisposalConfirmationState
        .valueOf(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_STATE), null)));
    }
    confirmation
      .setNumberOfAIPs(SolrUtils.objectToLong(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS), 0L));
    confirmation.setNumberOfCollections(
      SolrUtils.objectToLong(doc.get(RodaConstants.DISPOSAL_CONFIRMATION_NUMBER_OF_COLLECTIONS), 0L));

    return confirmation;
  }
}
