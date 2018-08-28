package org.roda.core.index.schema.collections;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileCollection extends AbstractSolrCollection<IndexedFile, File> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileCollection.class);

  private static final int TEN_MB_IN_BYTES = 10485760;

  @Override
  public Class<IndexedFile> getIndexClass() {
    return IndexedFile.class;
  }

  @Override
  public Class<File> getModelClass() {
    return File.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_FILE;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_FILE);
  }

  @Override
  public String getUniqueId(File modelObject) {
    return IdUtils.getFileId(modelObject);
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.FILE_PATH, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FILE_PARENT_UUID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_AIP_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_REPRESENTATION_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_REPRESENTATION_UUID, Field.TYPE_STRING));

    fields.add(new Field(RodaConstants.FILE_STORAGE_PATH, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_FORMAT_MIMETYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_FORMAT_VERSION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_PRONOM, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_ISENTRYPOINT, Field.TYPE_BOOLEAN));
    fields.add(new Field(RodaConstants.FILE_FILEFORMAT, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_FORMAT_DESIGNATION, Field.TYPE_STRING));

    fields.add(new Field(RodaConstants.FILE_ORIGINALNAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_SIZE, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.FILE_ISDIRECTORY, Field.TYPE_BOOLEAN));
    fields.add(new Field(RodaConstants.FILE_EXTENSION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_FULLTEXT, Field.TYPE_TEXT).setMultiValued(false).setStored(false));
    fields.add(new Field(RodaConstants.FILE_CREATING_APPLICATION_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_CREATING_APPLICATION_VERSION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.FILE_HASH, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FILE_ANCESTORS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.FILE_ANCESTORS_PATH, Field.TYPE_STRING).setMultiValued(true));

    // AIP
    fields.add(new Field(RodaConstants.INGEST_SIP_IDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.INGEST_JOB_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.INGEST_UPDATE_JOB_IDS, Field.TYPE_STRING).setMultiValued(true));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    List<CopyField> copyFields = new ArrayList<>();

    // TODO review the need of setting specific fields to be searchable (which
    // fields are being ignored and why?)
    copyFields.add(new CopyField("*_txt", Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_PATH, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.INDEX_ID, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_FORMAT_MIMETYPE, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_FORMAT_VERSION, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_FORMAT_DESIGNATION, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_PRONOM, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_FILEFORMAT, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_ORIGINALNAME, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_EXTENSION, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_FULLTEXT, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_CREATING_APPLICATION_NAME, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_CREATING_APPLICATION_VERSION, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_HASH, Field.FIELD_SEARCH));
    copyFields.add(new CopyField(RodaConstants.FILE_ANCESTORS_PATH, Field.FIELD_SEARCH));

    // TODO review the need for the following copy fields
    copyFields.add(new CopyField(RodaConstants.FILE_FORMAT_MIMETYPE, RodaConstants.FILE_FORMAT_MIMETYPE + "_txt"));
    copyFields.add(new CopyField(RodaConstants.FILE_FORMAT_VERSION, RodaConstants.FILE_FORMAT_VERSION + "_txt"));
    copyFields
      .add(new CopyField(RodaConstants.FILE_FORMAT_DESIGNATION, RodaConstants.FILE_FORMAT_DESIGNATION + "_txt"));
    copyFields.add(new CopyField(RodaConstants.FILE_PRONOM, RodaConstants.FILE_PRONOM + "_txt"));
    copyFields.add(new CopyField(RodaConstants.FILE_FILEFORMAT, RodaConstants.FILE_FILEFORMAT + "_txt"));
    copyFields.add(new CopyField(RodaConstants.FILE_EXTENSION, RodaConstants.FILE_EXTENSION + "_txt"));

    return copyFields;
  }

  @Override
  public SolrInputDocument toSolrDocument(File file, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(file, info);

    List<String> path = file.getPath();
    doc.addField(RodaConstants.FILE_PATH, path);
    if (path != null && !path.isEmpty()) {
      List<String> ancestorsPath = SolrUtils.getFileAncestorsPath(file.getAipId(), file.getRepresentationId(), path);
      if (!ancestorsPath.isEmpty()) {
        doc.addField(RodaConstants.FILE_PARENT_UUID, ancestorsPath.get(ancestorsPath.size() - 1));
        doc.addField(RodaConstants.FILE_ANCESTORS_PATH, ancestorsPath);
      }
    }
    doc.addField(RodaConstants.FILE_AIP_ID, file.getAipId());
    doc.addField(RodaConstants.FILE_REPRESENTATION_ID, file.getRepresentationId());
    doc.addField(RodaConstants.FILE_REPRESENTATION_UUID,
      IdUtils.getRepresentationId(file.getAipId(), file.getRepresentationId()));
    doc.addField(RodaConstants.FILE_ISDIRECTORY, file.isDirectory());

    // extra-fields
    try {
      StoragePath filePath = ModelUtils.getFileStoragePath(file);
      doc.addField(RodaConstants.FILE_STORAGEPATH,
        RodaCoreFactory.getStorageService().getStoragePathAsString(filePath, false));
    } catch (RequestNotValidException e) {
      LOGGER.warn("Could not index file storage path", e);
    }

    String fileId = file.getId();
    if (!file.isDirectory() && fileId.contains(".") && !fileId.startsWith(".")) {
      String extension = fileId.substring(fileId.lastIndexOf('.') + 1);
      doc.addField(RodaConstants.FILE_EXTENSION, extension);
    }

    Long sizeInBytes = 0L;

    // Add information from PREMIS
    Binary premisFile = getFilePremisFile(file);
    if (premisFile != null) {
      try {
        SolrInputDocument premisSolrDoc = PremisV3Utils.getSolrDocument(premisFile);
        doc.putAll(premisSolrDoc);
        sizeInBytes = SolrUtils.objectToLong(premisSolrDoc.get(RodaConstants.FILE_SIZE).getValue(), 0L);
      } catch (GenericException e) {
        LOGGER.warn("Could not index file PREMIS information", e);
      }
    }

    info.getAccumulators().put(RodaConstants.FILE_SIZE, sizeInBytes);

    // Add full text
    String fulltext = getFileFulltext(file);
    if (fulltext != null) {
      doc.addField(RodaConstants.FILE_FULLTEXT, fulltext);
    }

    return doc;
  }

  public static class Info extends IndexingAdditionalInfo {

    private final AIP aip;
    private final List<String> ancestors;

    public Info(AIP aip, List<String> ancestors) {
      super();
      this.aip = aip;
      this.ancestors = ancestors;
    }

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      Map<String, Object> preCalculatedFields = new HashMap<>();

      // indexing AIP inherited info
      preCalculatedFields.put(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(aip.getState()));
      preCalculatedFields.put(RodaConstants.INGEST_SIP_IDS, aip.getIngestSIPIds());
      preCalculatedFields.put(RodaConstants.INGEST_JOB_ID, aip.getIngestJobId());
      preCalculatedFields.put(RodaConstants.INGEST_UPDATE_JOB_IDS, aip.getIngestUpdateJobIds());
      preCalculatedFields.put(RodaConstants.FILE_ANCESTORS, ancestors);

      preCalculatedFields.putAll(SolrUtils.getPermissionsAsPreCalculatedFields(aip.getPermissions()));
      return preCalculatedFields;
    }

  }

  private Binary getFilePremisFile(File file) {
    Binary premisFile = null;
    try {
      premisFile = RodaCoreFactory.getModelService().retrievePreservationFile(file);
    } catch (NotFoundException e) {
      LOGGER.trace("Could not find PREMIS for file: {}", file);
    } catch (RODAException e) {
      LOGGER.warn("Could not load PREMIS for file: " + file, e);
    }
    return premisFile;
  }

  private String getFileFulltext(File file) {
    String fulltext = "";
    try {
      Binary fulltextBinary = RodaCoreFactory.getModelService().retrieveOtherMetadataBinary(file.getAipId(),
        file.getRepresentationId(), file.getPath(), file.getId(), RodaConstants.TIKA_FILE_SUFFIX_FULLTEXT,
        RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA);
      if (fulltextBinary.getSizeInBytes() < RodaCoreFactory.getRodaConfigurationAsInt(TEN_MB_IN_BYTES,
        "core.index.fulltext_threshold_in_bytes")) {
        try (InputStream inputStream = fulltextBinary.getContent().createInputStream()) {
          fulltext = IOUtils.toString(inputStream, Charset.forName(RodaConstants.DEFAULT_ENCODING));
        }
      }
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException | IOException e) {
      LOGGER.warn("Error getting fulltext for file: {}", file, e);
    } catch (NotFoundException e) {
      LOGGER.trace("Fulltext not found for file: {}", file, e);
    }
    return fulltext;
  }

  @Override
  public IndexedFile fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final IndexedFile ret = super.fromSolrDocument(doc, fieldsToReturn);

    String aipId = SolrUtils.objectToString(doc.get(RodaConstants.FILE_AIP_ID), null);
    String representationId = SolrUtils.objectToString(doc.get(RodaConstants.FILE_REPRESENTATION_ID), null);
    List<String> path = SolrUtils.objectToListString(doc.get(RodaConstants.FILE_PATH));

    String representationUUID = SolrUtils.objectToString(doc.get(RodaConstants.FILE_REPRESENTATION_UUID), null);
    String parentUUID = SolrUtils.objectToString(doc.get(RodaConstants.FILE_PARENT_UUID), null);
    List<String> ancestorsPath = SolrUtils.objectToListString(doc.get(RodaConstants.FILE_ANCESTORS_PATH));

    String originalName = SolrUtils.objectToString(doc.get(RodaConstants.FILE_ORIGINALNAME), null);
    List<String> hash = SolrUtils.objectToListString(doc.get(RodaConstants.FILE_HASH));
    long size = SolrUtils.objectToLong(doc.get(RodaConstants.FILE_SIZE), 0L);
    boolean isDirectory = SolrUtils.objectToBoolean(doc.get(RodaConstants.FILE_ISDIRECTORY), Boolean.FALSE);
    String storagePath = SolrUtils.objectToString(doc.get(RodaConstants.FILE_STORAGEPATH), null);

    // format
    String formatDesignationName = SolrUtils.objectToString(doc.get(RodaConstants.FILE_FILEFORMAT), null);
    String formatDesignationVersion = SolrUtils.objectToString(doc.get(RodaConstants.FILE_FORMAT_VERSION), null);
    String mimetype = SolrUtils.objectToString(doc.get(RodaConstants.FILE_FORMAT_MIMETYPE), null);
    String pronom = SolrUtils.objectToString(doc.get(RodaConstants.FILE_PRONOM), null);
    String extension = SolrUtils.objectToString(doc.get(RodaConstants.FILE_EXTENSION), null);
    // FIXME how to restore format registries
    Map<String, String> formatRegistries = new HashMap<>();

    // technical features
    String creatingApplicationName = SolrUtils.objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_NAME),
      null);
    String creatingApplicationVersion = SolrUtils
      .objectToString(doc.get(RodaConstants.FILE_CREATING_APPLICATION_VERSION), null);
    String dateCreatedByApplication = SolrUtils.objectToString(doc.get(RodaConstants.FILE_DATE_CREATED_BY_APPLICATION),
      null);
    final List<String> ancestors = SolrUtils.objectToListString(doc.get(RodaConstants.AIP_ANCESTORS));

    // handle other properties
    Map<String, List<String>> otherProperties = new HashMap<>();
    for (String fieldName : doc.getFieldNames()) {
      if (fieldName.endsWith("_txt")) {
        List<String> otherProperty = SolrUtils.objectToListString(doc.get(fieldName));
        otherProperties.put(fieldName, otherProperty);
      }

    }

    FileFormat fileFormat = new FileFormat(formatDesignationName, formatDesignationVersion, mimetype, pronom, extension,
      formatRegistries);

    Map<String, Object> indexedFields = new HashMap<>();
    for (String field : fieldsToReturn) {
      indexedFields.put(field, doc.get(field));
    }

    ret.setParentUUID(parentUUID);
    ret.setAipId(aipId);
    ret.setRepresentationId(representationId);
    ret.setRepresentationUUID(representationUUID);
    ret.setPath(path);
    ret.setAncestorsPath(ancestorsPath);
    ret.setFileFormat(fileFormat);
    ret.setOriginalName(originalName);
    ret.setSize(size);
    ret.setDirectory(isDirectory);
    ret.setCreatingApplicationName(creatingApplicationName);
    ret.setCreatingApplicationVersion(creatingApplicationVersion);
    ret.setDateCreatedByApplication(dateCreatedByApplication);
    ret.setHash(hash);
    ret.setStoragePath(storagePath);
    ret.setAncestors(ancestors);
    ret.setOtherProperties(otherProperties);
    ret.setFields(indexedFields);

    return ret;
  }

}
