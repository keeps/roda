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
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
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

public class DisposalHoldCollection extends AbstractSolrCollection<DisposalHold, DisposalHold> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalHoldCollection.class);

  @Override
  public Class<DisposalHold> getIndexClass() {
    return DisposalHold.class;
  }

  @Override
  public Class<DisposalHold> getModelClass() {
    return DisposalHold.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_DISPOSAL_HOLD;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_DISPOSAL_HOLD);
  }

  @Override
  public String getUniqueId(DisposalHold modelObject) {
    return modelObject.getUUID();
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DISPOSAL_HOLD_TITLE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_HOLD_DESCRIPTION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_HOLD_MANDATE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_HOLD_SCOPE_NOTES, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DISPOSAL_HOLD_STATE, Field.TYPE_STRING));

    return fields;
  }

  @Override
  public SolrInputDocument toSolrDocument(ModelService model, DisposalHold hold, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(model, hold, info);

    doc.addField(RodaConstants.DISPOSAL_HOLD_TITLE, hold.getTitle());
    doc.addField(RodaConstants.DISPOSAL_HOLD_DESCRIPTION, hold.getDescription());
    doc.addField(RodaConstants.DISPOSAL_HOLD_MANDATE, hold.getMandate());
    doc.addField(RodaConstants.DISPOSAL_HOLD_SCOPE_NOTES, hold.getScopeNotes());
    doc.addField(RodaConstants.DISPOSAL_HOLD_STATE, hold.getState().toString());

    return doc;
  }

  @Override
  public DisposalHold fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final DisposalHold hold = super.fromSolrDocument(doc, fieldsToReturn);

    hold.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_HOLD_TITLE), null));
    hold.setDescription(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_HOLD_DESCRIPTION), null));
    hold.setMandate(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_HOLD_MANDATE), null));
    hold.setScopeNotes(SolrUtils.objectToString(doc.get(RodaConstants.DISPOSAL_HOLD_SCOPE_NOTES), null));
    DisposalHoldState disposalHoldState = SolrUtils.objectToEnum(doc.get(RodaConstants.DISPOSAL_HOLD_STATE),
      DisposalHoldState.class, DisposalHoldState.getDefault());
    hold.setState(disposalHoldState);

    return hold;
  }
}
