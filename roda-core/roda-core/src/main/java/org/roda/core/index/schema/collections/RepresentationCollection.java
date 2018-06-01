package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;

public class RepresentationCollection extends AbstractSolrCollection<IndexedRepresentation, Representation> {

  @Override
  public Class<IndexedRepresentation> getIndexClass() {
    return IndexedRepresentation.class;
  }

  @Override
  public Class<Representation> getModelClass() {
    return Representation.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_REPRESENTATION;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_FILE);
  }

  @Override
  public String getUniqueId(Representation modelObject) {
    return IdUtils.getRepresentationId(modelObject);
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.REPRESENTATION_AIP_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_ORIGINAL, Field.TYPE_BOOLEAN));
    fields.add(new Field(RodaConstants.REPRESENTATION_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FOLDERS, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_SCHEMA_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_ANCESTORS, Field.TYPE_STRING).setMultiValued(true));

    fields.add(new Field(RodaConstants.REPRESENTATION_CREATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.REPRESENTATION_CREATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_UPDATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.REPRESENTATION_UPDATED_BY, Field.TYPE_STRING));

    fields.add(new Field(RodaConstants.REPRESENTATION_STATES, Field.TYPE_STRING).setMultiValued(true));

    // AIP
    fields.add(new Field(RodaConstants.INGEST_SIP_IDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.INGEST_JOB_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.INGEST_UPDATE_JOB_IDS, Field.TYPE_STRING).setMultiValued(true));

    // pataki@ START
    // TODO review better way to support default metadata types indexing in
    // representation
    fields.add(new Field(RodaConstants.AIP_LEVEL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_TITLE, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.AIP_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.AIP_DATE_INITIAL, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_DATE_FINAL, Field.TYPE_DATE));

    fields.add(SolrCollection.getSortFieldOf(RodaConstants.AIP_TITLE));
    fields.add(SolrCollection.getSortFieldOf(RodaConstants.REPRESENTATION_TYPE));
    // pataki@ END

    fields.add(SolrCollection.getSearchField());

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getSortCopyFieldOf(RodaConstants.AIP_TITLE),
      SolrCollection.getSortCopyFieldOf(RodaConstants.REPRESENTATION_TYPE));
  }

  @Override
  public SolrInputDocument toSolrDocument(Representation rep, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    boolean safemode = Arrays.asList(flags).contains(Flags.SAFE_MODE_ON);

    SolrInputDocument doc = super.toSolrDocument(rep, preCalculatedFields, accumulators, flags);

    doc.addField(RodaConstants.REPRESENTATION_AIP_ID, rep.getAipId());
    doc.addField(RodaConstants.REPRESENTATION_ORIGINAL, rep.isOriginal());
    doc.addField(RodaConstants.REPRESENTATION_TYPE, rep.getType());

    doc.addField(RodaConstants.REPRESENTATION_CREATED_ON, SolrUtils.formatDate(rep.getCreatedOn()));
    doc.addField(RodaConstants.REPRESENTATION_CREATED_BY, rep.getCreatedBy());
    doc.addField(RodaConstants.REPRESENTATION_UPDATED_ON, SolrUtils.formatDate(rep.getUpdatedOn()));
    doc.addField(RodaConstants.REPRESENTATION_UPDATED_BY, rep.getUpdatedBy());

    if (!rep.getRepresentationStates().isEmpty()) {
      doc.addField(RodaConstants.REPRESENTATION_STATES, rep.getRepresentationStates());
    } else if (rep.isOriginal()) {
      doc.addField(RodaConstants.REPRESENTATION_STATES, Arrays.asList(RepresentationState.ORIGINAL));
    }

    if (!safemode) {
      SolrUtils.indexDescriptiveMetadataFields(RodaCoreFactory.getModelService(), rep.getAipId(), rep.getId(),
        rep.getDescriptiveMetadata(), doc);
    }

    // Calculate number of documentation and schema files
    ModelService model = RodaCoreFactory.getModelService();
    StorageService storage = model.getStorage();
    Long numberOfDocumentationFiles;
    try {
      Directory documentationDirectory = model.getDocumentationDirectory(rep.getAipId(), rep.getId());
      numberOfDocumentationFiles = storage.countResourcesUnderDirectory(documentationDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfDocumentationFiles = 0L;
    }

    Long numberOfSchemaFiles;
    try {
      Directory schemasDirectory = model.getSchemasDirectory(rep.getAipId(), rep.getId());
      numberOfSchemaFiles = storage.countResourcesUnderDirectory(schemasDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfSchemaFiles = 0L;
    }

    doc.addField(RodaConstants.REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES, numberOfDocumentationFiles);
    doc.addField(RodaConstants.REPRESENTATION_NUMBER_OF_SCHEMA_FILES, numberOfSchemaFiles);

    return doc;
  }

  @Override
  public IndexedRepresentation fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final IndexedRepresentation ret = super.fromSolrDocument(doc, fieldsToReturn);

    ret.setAipId(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_AIP_ID), null));
    ret.setOriginal(
      Boolean.TRUE.equals(SolrUtils.objectToBoolean(doc.get(RodaConstants.REPRESENTATION_ORIGINAL), Boolean.FALSE)));
    ret.setType(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_TYPE), null));
    ret.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_TITLE), null));
    ret.setSizeInBytes(SolrUtils.objectToLong(doc.get(RodaConstants.REPRESENTATION_SIZE_IN_BYTES), 0L));
    ret.setNumberOfDataFiles(SolrUtils.objectToLong(doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES), 0L));
    ret
      .setNumberOfDataFolders(SolrUtils.objectToLong(doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FOLDERS), 0L));
    ret.setNumberOfDocumentationFiles(
      SolrUtils.objectToLong(doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES), 0L));
    ret
      .setNumberOfSchemaFiles(SolrUtils.objectToLong(doc.get(RodaConstants.REPRESENTATION_NUMBER_OF_SCHEMA_FILES), 0L));
    ret.setAncestors(SolrUtils.objectToListString(doc.get(RodaConstants.AIP_ANCESTORS)));

    ret.setCreatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.REPRESENTATION_CREATED_ON)));
    ret.setCreatedBy(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_CREATED_BY), ""));
    ret.setUpdatedOn(SolrUtils.objectToDate(doc.get(RodaConstants.REPRESENTATION_UPDATED_ON)));
    ret.setUpdatedBy(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_UPDATED_BY), ""));

    ret.setRepresentationStates(SolrUtils.objectToListString(doc.get(RodaConstants.REPRESENTATION_STATES)));

    Map<String, Object> indexedFields = new HashMap<>();
    for (String field : fieldsToReturn) {
      indexedFields.put(field, doc.get(field));
    }

    ret.setFields(indexedFields);

    return ret;
  }

}
