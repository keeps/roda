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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.IndexingAdditionalInfo.Flags;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.IndexUtils;
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
    fields.add(new Field(RodaConstants.INDEX_INSTANCE_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.INDEX_INSTANCE_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FOLDERS, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_NUMBER_OF_SCHEMA_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.REPRESENTATION_ANCESTORS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.REPRESENTATION_HAS_SHALLOW_FILES, Field.TYPE_BOOLEAN));

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

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getSortCopyFieldOf(RodaConstants.AIP_TITLE),
      SolrCollection.getSortCopyFieldOf(RodaConstants.REPRESENTATION_TYPE));
  }

  @Override
  public SolrInputDocument toSolrDocument(Representation rep, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    boolean safemode = info.getFlags().contains(Flags.SAFE_MODE_ON);

    SolrInputDocument doc = super.toSolrDocument(rep, info);

    doc.addField(RodaConstants.REPRESENTATION_AIP_ID, rep.getAipId());
    doc.addField(RodaConstants.REPRESENTATION_ORIGINAL, rep.isOriginal());
    doc.addField(RodaConstants.REPRESENTATION_TYPE, rep.getType());
    doc.addField(RodaConstants.INDEX_INSTANCE_ID, rep.getInstanceId());
    doc.addField(RodaConstants.REPRESENTATION_HAS_SHALLOW_FILES, rep.getHasShallowFiles());

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

    String name = IndexUtils.giveNameFromLocalInstanceIdentifier(rep.getInstanceId());

    doc.addField(RodaConstants.INDEX_INSTANCE_NAME, name);
    return doc;
  }

  public static class Info extends IndexingAdditionalInfo {

    private final AIP aip;
    private final List<String> ancestors;
    private final Long sizeInBytes;
    private final Long numberOfDataFiles;
    private final Long numberOfDataFolders;

    private final boolean safemode;

    public Info(AIP aip, List<String> ancestors, Long sizeInBytes, Long numberOfDataFiles, Long numberOfDataFolders,
      boolean safemode) {
      super();
      this.aip = aip;
      this.ancestors = ancestors;
      this.sizeInBytes = sizeInBytes;
      this.numberOfDataFiles = numberOfDataFiles;
      this.numberOfDataFolders = numberOfDataFolders;
      this.safemode = safemode;
    }

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      Map<String, Object> preCalculatedFields = new HashMap<>();

      // indexing file size and number
      preCalculatedFields.put(RodaConstants.REPRESENTATION_SIZE_IN_BYTES, sizeInBytes);
      preCalculatedFields.put(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FILES, numberOfDataFiles);
      preCalculatedFields.put(RodaConstants.REPRESENTATION_NUMBER_OF_DATA_FOLDERS, numberOfDataFolders);

      // indexing active state and permissions
      preCalculatedFields.put(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(aip.getState()));
      preCalculatedFields.put(RodaConstants.INGEST_SIP_IDS, aip.getIngestSIPIds());
      preCalculatedFields.put(RodaConstants.INGEST_JOB_ID, aip.getIngestJobId());
      preCalculatedFields.put(RodaConstants.INGEST_UPDATE_JOB_IDS, aip.getIngestUpdateJobIds());

      preCalculatedFields.put(RodaConstants.AIP_ANCESTORS, ancestors);

      preCalculatedFields.putAll(SolrUtils.getPermissionsAsPreCalculatedFields(aip.getPermissions()));
      return preCalculatedFields;
    }

    @Override
    public List<Flags> getFlags() {
      return Arrays.asList(safemode ? Flags.SAFE_MODE_ON : Flags.SAFE_MODE_OFF);
    }

  }

  @Override
  public IndexedRepresentation fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final IndexedRepresentation ret = super.fromSolrDocument(doc, fieldsToReturn);

    ret.setAipId(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_AIP_ID), null));
    ret.setOriginal(
      Boolean.TRUE.equals(SolrUtils.objectToBoolean(doc.get(RodaConstants.REPRESENTATION_ORIGINAL), Boolean.FALSE)));
    ret.setType(SolrUtils.objectToString(doc.get(RodaConstants.REPRESENTATION_TYPE), null));
    ret.setInstanceId(SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_ID), null));
    ret.setInstanceName(SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_NAME), null));
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
    ret.setHasShallowFiles(
      SolrUtils.objectToBoolean(doc.get(RodaConstants.REPRESENTATION_HAS_SHALLOW_FILES), Boolean.FALSE));

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
