package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourceCollection extends AbstractSolrCollection<TransferredResource, TransferredResource> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourceCollection.class);

  @Override
  public Class<TransferredResource> getIndexClass() {
    return TransferredResource.class;
  }

  @Override
  public Class<TransferredResource> getModelClass() {
    return TransferredResource.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_TRANSFERRED_RESOURCE;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_TRANSFERRED_RESOURCE);
  }

  @Override
  public String getUniqueId(TransferredResource modelObject) {
    return modelObject.getUUID();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_DATE, Field.TYPE_DATE).setRequired(true));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, Field.TYPE_BOOLEAN));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_NAME, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.TRANSFERRED_RESOURCE_SIZE, Field.TYPE_LONG).setRequired(true));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(new CopyField(RodaConstants.TRANSFERRED_RESOURCE_NAME, Field.FIELD_SEARCH));
  }

  @Override
  public SolrInputDocument toSolrDocument(TransferredResource tr, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(tr, preCalculatedFields, accumulators, flags);

    doc.addField(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH, tr.getFullPath());
    if (tr.getParentId() != null) {
      doc.addField(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID, tr.getParentId());
      doc.addField(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID, IdUtils.createUUID(tr.getParentId()));
    }
    if (tr.getRelativePath() != null) {
      doc.addField(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH, tr.getRelativePath());
    }
    doc.addField(RodaConstants.TRANSFERRED_RESOURCE_DATE, SolrUtils.formatDate(tr.getCreationDate()));
    doc.addField(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, tr.isFile());
    doc.addField(RodaConstants.TRANSFERRED_RESOURCE_SIZE, tr.getSize());
    doc.addField(RodaConstants.TRANSFERRED_RESOURCE_NAME, tr.getName());
    if (tr.getAncestorsPaths() != null && !tr.getAncestorsPaths().isEmpty()) {
      doc.addField(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, tr.getAncestorsPaths());
    }
    doc.addField(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE,
      SolrUtils.formatDateWithMillis(tr.getLastScanDate()));

    return doc;
  }

  @Override
  public TransferredResource fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final TransferredResource tr = super.fromSolrDocument(doc, fieldsToReturn);

    String fullPath = SolrUtils.objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_FULLPATH), null);

    String parentId = null;
    String parentUUID = null;
    if (doc.containsKey(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID)) {
      parentId = SolrUtils.objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID), null);
      parentUUID = SolrUtils.objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_PARENT_UUID), null);
    }
    String relativePath = SolrUtils.objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH), null);

    Date d = SolrUtils.objectToDate(doc.get(RodaConstants.TRANSFERRED_RESOURCE_DATE));
    if (d == null) {
      // Could not have date if getting via lite fields
      LOGGER.trace("Error parsing transferred resource date. Setting date to current date.");
      d = new Date();
    }

    boolean isFile = SolrUtils.objectToBoolean(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ISFILE), Boolean.FALSE);
    long size = SolrUtils.objectToLong(doc.get(RodaConstants.TRANSFERRED_RESOURCE_SIZE), 0L);
    String name = SolrUtils.objectToString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_NAME), null);

    List<String> ancestorsPath = SolrUtils.objectToListString(doc.get(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS));

    Date lastScanDate = SolrUtils.objectToDateWithMillis(doc.get(RodaConstants.TRANSFERRED_RESOURCE_LAST_SCAN_DATE));

    tr.setFullPath(fullPath);
    tr.setCreationDate(d);
    tr.setName(name);
    tr.setRelativePath(relativePath);
    tr.setSize(size);
    tr.setParentId(parentId);
    tr.setParentUUID(parentUUID);
    tr.setFile(isFile);
    tr.setAncestorsPaths(ancestorsPath);
    tr.setLastScanDate(lastScanDate);

    return tr;

  }

}
