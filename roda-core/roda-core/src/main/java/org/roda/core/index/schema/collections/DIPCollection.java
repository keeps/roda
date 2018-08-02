package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.common.dips.DIPUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DIPCollection extends AbstractSolrCollection<IndexedDIP, DIP> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DIPCollection.class);

  @Override
  public Class<IndexedDIP> getIndexClass() {
    return IndexedDIP.class;
  }

  @Override
  public Class<DIP> getModelClass() {
    return DIP.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_DIP;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_DIP, RodaConstants.INDEX_DIP_FILE);
  }

  @Override
  public String getUniqueId(DIP modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.DIP_TITLE, Field.TYPE_TEXT).setRequired(true).setMultiValued(false));
    fields.add(new Field(RodaConstants.DIP_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.DIP_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIP_AIP_IDS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIP_REPRESENTATION_IDS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIP_FILE_IDS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIP_DATE_CREATED, Field.TYPE_DATE).setRequired(true));
    fields.add(new Field(RodaConstants.DIP_LAST_MODIFIED, Field.TYPE_DATE));
    fields
      .add(new Field(RodaConstants.DIP_IS_PERMANENT, Field.TYPE_BOOLEAN).setDefaultValue("false").setRequired(true));
    fields.add(new Field(RodaConstants.DIP_OPEN_EXTERNAL_URL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIP_PROPERTIES, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.DIP_AIP_UUIDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.DIP_REPRESENTATION_UUIDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.DIP_FILE_UUIDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.DIP_ALL_AIP_UUIDS, Field.TYPE_STRING).setStored(false).setMultiValued(true));
    fields.add(
      new Field(RodaConstants.DIP_ALL_REPRESENTATION_UUIDS, Field.TYPE_STRING).setStored(false).setMultiValued(true));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(DIP dip, Map<String, Object> preCalculatedFields,
    Map<String, Object> accumulators, Flags... flags)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = super.toSolrDocument(dip, preCalculatedFields, accumulators, flags);

    doc.addField(RodaConstants.DIP_TITLE, dip.getTitle());
    doc.addField(RodaConstants.DIP_DESCRIPTION, dip.getDescription());
    doc.addField(RodaConstants.DIP_TYPE, dip.getType());
    doc.addField(RodaConstants.DIP_DATE_CREATED, SolrUtils.formatDate(dip.getDateCreated()));
    doc.addField(RodaConstants.DIP_LAST_MODIFIED, SolrUtils.formatDate(dip.getLastModified()));
    doc.addField(RodaConstants.DIP_IS_PERMANENT, dip.getIsPermanent());
    doc.addField(RodaConstants.DIP_PROPERTIES, JsonUtils.getJsonFromObject(dip.getProperties()));

    doc.addField(RodaConstants.DIP_AIP_IDS, JsonUtils.getJsonFromObject(dip.getAipIds()));
    doc.addField(RodaConstants.DIP_REPRESENTATION_IDS, JsonUtils.getJsonFromObject(dip.getRepresentationIds()));
    doc.addField(RodaConstants.DIP_FILE_IDS, JsonUtils.getJsonFromObject(dip.getFileIds()));

    List<String> allAipUUIDs = new ArrayList<>();
    List<String> allRepresentationUUIDs = new ArrayList<>();

    List<String> aipUUIDs = new ArrayList<>();
    for (AIPLink aip : dip.getAipIds()) {
      aipUUIDs.add(aip.getAipId());
    }

    allAipUUIDs.addAll(aipUUIDs);

    List<String> representationUUIDs = new ArrayList<>();
    for (RepresentationLink rep : dip.getRepresentationIds()) {
      representationUUIDs.add(IdUtils.getRepresentationId(rep));
      if (!allAipUUIDs.contains(rep.getAipId())) {
        allAipUUIDs.add(rep.getAipId());
      }
    }

    allRepresentationUUIDs.addAll(representationUUIDs);

    List<String> fileUUIDs = new ArrayList<>();
    for (FileLink file : dip.getFileIds()) {
      fileUUIDs.add(IdUtils.getFileId(file));
      if (!allAipUUIDs.contains(file.getAipId())) {
        allAipUUIDs.add(file.getAipId());
      }

      String repUUID = IdUtils.getRepresentationId(file.getAipId(), file.getRepresentationId());
      if (!allRepresentationUUIDs.contains(repUUID)) {
        allRepresentationUUIDs.add(repUUID);
      }
    }

    doc.addField(RodaConstants.DIP_AIP_UUIDS, aipUUIDs);
    doc.addField(RodaConstants.DIP_REPRESENTATION_UUIDS, representationUUIDs);
    doc.addField(RodaConstants.DIP_FILE_UUIDS, fileUUIDs);

    doc.addField(RodaConstants.DIP_ALL_AIP_UUIDS, allAipUUIDs);
    doc.addField(RodaConstants.DIP_ALL_REPRESENTATION_UUIDS, allRepresentationUUIDs);

    OptionalWithCause<String> openURL = DIPUtils.getCompleteOpenExternalURL(dip);
    if (openURL.isPresent()) {
      doc.addField(RodaConstants.DIP_OPEN_EXTERNAL_URL, openURL.get());
    } else if (openURL.getCause() != null) {
      LOGGER.error("Error indexing DIP open external URL", openURL.getCause());
    }

    return doc;
  }

  @Override
  public IndexedDIP fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {
    IndexedDIP dip = super.fromSolrDocument(doc, fieldsToReturn);
    dip.setTitle(SolrUtils.objectToString(doc.get(RodaConstants.DIP_TITLE), null));
    dip.setDescription(SolrUtils.objectToString(doc.get(RodaConstants.DIP_DESCRIPTION), null));
    dip.setType(SolrUtils.objectToString(doc.get(RodaConstants.DIP_TYPE), null));
    dip.setDateCreated(SolrUtils.objectToDate(doc.get(RodaConstants.DIP_DATE_CREATED)));
    dip.setLastModified(SolrUtils.objectToDate(doc.get(RodaConstants.DIP_LAST_MODIFIED)));
    dip.setIsPermanent(SolrUtils.objectToBoolean(doc.get(RodaConstants.DIP_IS_PERMANENT), Boolean.FALSE));

    boolean emptyFields = fieldsToReturn.isEmpty();

    if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_PROPERTIES)) {
      dip.setProperties(JsonUtils.getMapFromJson(SolrUtils.objectToString(doc.get(RodaConstants.DIP_PROPERTIES), "")));
    }

    try {
      if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_AIP_IDS)) {
        String aipIds = SolrUtils.objectToString(doc.get(RodaConstants.DIP_AIP_IDS), null);
        dip.setAipIds(JsonUtils.getListFromJson(aipIds == null ? "" : aipIds, AIPLink.class));
      }

      if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_REPRESENTATION_IDS)) {
        String representationIds = SolrUtils.objectToString(doc.get(RodaConstants.DIP_REPRESENTATION_IDS), null);
        dip.setRepresentationIds(
          JsonUtils.getListFromJson(representationIds == null ? "" : representationIds, RepresentationLink.class));
      }

      if (emptyFields || fieldsToReturn.contains(RodaConstants.DIP_FILE_IDS)) {
        String fileIds = SolrUtils.objectToString(doc.get(RodaConstants.DIP_FILE_IDS), null);
        dip.setFileIds(JsonUtils.getListFromJson(fileIds == null ? "" : fileIds, FileLink.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error getting related ids from DIP index");
    }

    dip.setPermissions(SolrUtils.getPermissions(doc));
    dip.setOpenExternalURL(SolrUtils.objectToString(doc.get(RodaConstants.DIP_OPEN_EXTERNAL_URL), null));
    return dip;
  }

}
