package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DIPFileCollection extends AbstractSolrCollection<DIPFile, DIPFile> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DIPFileCollection.class);

  @Override
  public Class<DIPFile> getIndexClass() {
    return DIPFile.class;
  }

  @Override
  public Class<DIPFile> getModelClass() {
    return DIPFile.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_DIP_FILE;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_DIP_FILE);
  }

  @Override
  public String getUniqueId(DIPFile modelObject) {
    return IdUtils.getDIPFileId(modelObject);
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DIPFILE_DIP_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIPFILE_PATH, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.DIPFILE_PARENT_UUID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIPFILE_ANCESTORS_UUIDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.DIPFILE_IS_DIRECTORY, Field.TYPE_BOOLEAN));
    fields.add(new Field(RodaConstants.DIPFILE_SIZE, Field.TYPE_LONG).setRequired(true));
    fields.add(new Field(RodaConstants.DIPFILE_STORAGE_PATH, Field.TYPE_STRING));

    fields.add(SolrCollection.getSearchField());
    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(new CopyField(RodaConstants.INDEX_ID, Field.FIELD_SEARCH),
      new CopyField(RodaConstants.DIPFILE_PATH, Field.FIELD_SEARCH));
  }

  @Override
  public SolrInputDocument toSolrDocument(DIPFile file, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = super.toSolrDocument(file, preCalculatedFields, accumulators, flags);

    List<String> path = file.getPath();
    doc.addField(RodaConstants.DIPFILE_PATH, path);
    if (path != null && !path.isEmpty()) {
      List<String> ancestorsPath = getDIPFileAncestorsPath(file.getDipId(), path);
      if (!ancestorsPath.isEmpty()) {
        doc.addField(RodaConstants.DIPFILE_ANCESTORS_UUIDS, ancestorsPath);
      }

      doc.addField(RodaConstants.DIPFILE_PARENT_UUID,
        IdUtils.getDIPFileId(file.getDipId(), path.subList(0, path.size() - 1), path.get(path.size() - 1)));
    }

    doc.addField(RodaConstants.DIPFILE_DIP_ID, file.getDipId());
    doc.addField(RodaConstants.DIPFILE_IS_DIRECTORY, file.isDirectory());
    doc.addField(RodaConstants.DIPFILE_SIZE, Long.toString(file.getSize()));

    // extra-fields
    try {
      StoragePath filePath = ModelUtils.getDIPFileStoragePath(file);
      doc.addField(RodaConstants.DIPFILE_STORAGE_PATH,
        RodaCoreFactory.getStorageService().getStoragePathAsString(filePath, false));
    } catch (RequestNotValidException e) {
      LOGGER.warn("Could not index DIP file storage path", e);
    }

    return doc;
  }

  private static List<String> getDIPFileAncestorsPath(String dipId, List<String> path) {
    List<String> parentFileDirectoryPath = new ArrayList<>();
    List<String> ancestorsPath = new ArrayList<>();
    parentFileDirectoryPath.addAll(path);

    while (!parentFileDirectoryPath.isEmpty()) {
      int lastElementIndex = parentFileDirectoryPath.size() - 1;
      String parentFileId = parentFileDirectoryPath.get(lastElementIndex);
      parentFileDirectoryPath.remove(lastElementIndex);
      ancestorsPath.add(0, IdUtils.getDIPFileId(dipId, parentFileDirectoryPath, parentFileId));
    }

    return ancestorsPath;
  }

  @Override
  public DIPFile fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {
    DIPFile file = super.fromSolrDocument(doc, fieldsToReturn);
    file.setDipId(SolrUtils.objectToString(doc.get(RodaConstants.DIPFILE_DIP_ID), null));
    file.setPath(SolrUtils.objectToListString(doc.get(RodaConstants.DIPFILE_PATH)));
    file.setAncestorsUUIDs(SolrUtils.objectToListString(doc.get(RodaConstants.DIPFILE_ANCESTORS_UUIDS)));
    file.setDirectory(SolrUtils.objectToBoolean(doc.get(RodaConstants.DIPFILE_IS_DIRECTORY), Boolean.FALSE));
    file.setStoragePath(SolrUtils.objectToString(doc.get(RodaConstants.DIPFILE_STORAGE_PATH), null));
    file.setSize(SolrUtils.objectToLong(doc.get(RodaConstants.DIPFILE_SIZE), 0L));
    return file;
  }

}
