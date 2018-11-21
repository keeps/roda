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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent.PreservationMetadataEventClass;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.storage.Binary;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.LinkingObjectIdentifierComplexType;

public class PreservationEventCollection
  extends AbstractSolrCollection<IndexedPreservationEvent, PreservationMetadata> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreservationEventCollection.class);

  @Override
  public Class<IndexedPreservationEvent> getIndexClass() {
    return IndexedPreservationEvent.class;
  }

  @Override
  public Class<PreservationMetadata> getModelClass() {
    return PreservationMetadata.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_PRESERVATION_EVENTS;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_PRESERVATION_EVENTS);
  }

  @Override
  public String getUniqueId(PreservationMetadata modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_AIP_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_FILE_UUID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_DATETIME, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_DETAIL, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_OUTCOME, Field.TYPE_STRING));
    fields.add(
      new Field(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER, Field.TYPE_STRING)
      .setMultiValued(true));
    fields.add(new Field(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER, Field.TYPE_STRING)
      .setMultiValued(true));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(PreservationMetadata pm, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(pm, info);

    String objectClass = PreservationMetadataEventClass.REPOSITORY.toString();

    if (StringUtils.isNotBlank(pm.getAipId())) {
      doc.addField(RodaConstants.PRESERVATION_EVENT_AIP_ID, pm.getAipId());
      objectClass = PreservationMetadataEventClass.AIP.toString();
    }

    if (StringUtils.isNotBlank(pm.getRepresentationId())) {
      doc.addField(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID,
        IdUtils.getRepresentationId(pm.getAipId(), pm.getRepresentationId()));
      objectClass = PreservationMetadataEventClass.REPRESENTATION.toString();
    }

    if (StringUtils.isNotBlank(pm.getFileId())) {
      doc.addField(RodaConstants.PRESERVATION_EVENT_FILE_UUID,
        IdUtils.getFileId(pm.getAipId(), pm.getRepresentationId(), pm.getFileDirectoryPath(), pm.getFileId()));
      objectClass = PreservationMetadataEventClass.FILE.toString();
    }

    if (objectClass.equals(PreservationMetadataEventClass.REPOSITORY.toString())) {
      doc.addField(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(AIPState.ACTIVE));

      Permissions permissions = new Permissions();
      List<String> users = RodaCoreFactory.getRodaConfigurationAsList("core.permission.repository_events.user");
      List<String> groups = RodaCoreFactory.getRodaConfigurationAsList("core.permission.repository_events.group");

      for (String user : users) {
        permissions.setUserPermissions(user, Sets.newHashSet(PermissionType.READ));
      }

      for (String group : groups) {
        permissions.setGroupPermissions(group, Sets.newHashSet(PermissionType.READ));
      }

      SolrUtils.setPermissions(permissions, doc);
    }

    doc.addField(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS, objectClass);
    Binary binary = RodaCoreFactory.getModelService().retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(),
      pm.getFileDirectoryPath(), pm.getFileId(), pm.getId());

    boolean validate = false;
    try {
      EventComplexType event = PremisV3Utils.binaryToEvent(binary.getContent(), validate);

      doc.addField(RodaConstants.PRESERVATION_EVENT_DATETIME, event.getEventDateTime());

      if (event.getEventDetailInformationArray().length > 0) {
        doc.addField(RodaConstants.PRESERVATION_EVENT_DETAIL,
          event.getEventDetailInformationArray()[0].getEventDetail());
      }

      doc.addField(RodaConstants.PRESERVATION_EVENT_TYPE, event.getEventType().getStringValue());

      if (event.getEventOutcomeInformationArray().length > 0) {
        doc.addField(RodaConstants.PRESERVATION_EVENT_OUTCOME,
          event.getEventOutcomeInformationArray(0).getEventOutcome().getStringValue());
      }

      if (event.getLinkingAgentIdentifierArray() != null && event.getLinkingAgentIdentifierArray().length > 0) {
        for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifierArray()) {
          LinkingIdentifier li = new LinkingIdentifier();
          li.setType(laict.getLinkingAgentIdentifierType().getStringValue());
          li.setValue(laict.getLinkingAgentIdentifierValue());
          li.setRoles(PremisV3Utils.toStringList(laict.getLinkingAgentRoleArray()));

          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER, JsonUtils.getJsonFromObject(li));
        }
      }

      if (event.getLinkingObjectIdentifierArray() != null && event.getLinkingObjectIdentifierArray().length > 0) {
        for (LinkingObjectIdentifierComplexType loict : event.getLinkingObjectIdentifierArray()) {
          LinkingIdentifier li = new LinkingIdentifier();
          li.setType(loict.getLinkingObjectIdentifierType().getStringValue());
          li.setValue(loict.getLinkingObjectIdentifierValue());
          li.setRoles(PremisV3Utils.toStringList(loict.getLinkingObjectRoleArray()));

          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER,
            JsonUtils.getJsonFromObject(li));
        }
      }

      if (event.getLinkingObjectIdentifierArray() != null && event.getLinkingObjectIdentifierArray().length > 0) {
        for (LinkingObjectIdentifierComplexType loict : event.getLinkingObjectIdentifierArray()) {
          LinkingIdentifier li = new LinkingIdentifier();
          li.setType(loict.getLinkingObjectIdentifierType().getStringValue());
          li.setValue(loict.getLinkingObjectIdentifierValue());
          li.setRoles(PremisV3Utils.toStringList(loict.getLinkingObjectRoleArray()));

          doc.addField(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER,
            JsonUtils.getJsonFromObject(li));
        }
      }

    } catch (ValidationException e) {
      throw new GenericException(e);
    }

    return doc;
  }

  public static class Info extends IndexingAdditionalInfo {

    private final AIP aip;

    public Info(AIP aip) {
      super();
      this.aip = aip;
    }

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      Map<String, Object> preCalculatedFields = new HashMap<>();

      if (aip != null) {
        preCalculatedFields.put(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(aip.getState()));
        preCalculatedFields.putAll(SolrUtils.getPermissionsAsPreCalculatedFields(aip.getPermissions()));
      } else {
        preCalculatedFields.put(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(AIPState.ACTIVE));
      }

      return preCalculatedFields;
    }

  }

  @Override
  public IndexedPreservationEvent fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn)
    throws GenericException {

    final IndexedPreservationEvent ipe = super.fromSolrDocument(doc, fieldsToReturn);

    final String aipId = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_AIP_ID), null);
    final String representationUUID = SolrUtils
      .objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_REPRESENTATION_UUID), null);
    final String fileUUID = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_FILE_UUID), null);

    ipe.setAipID(aipId);
    ipe.setRepresentationUUID(representationUUID);
    ipe.setFileUUID(fileUUID);

    String objectClass = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS), null);
    if (StringUtils.isNotBlank(objectClass)) {
      ipe.setObjectClass(PreservationMetadataEventClass.valueOf(objectClass.toUpperCase()));
    }

    final Date eventDateTime = SolrUtils.objectToDate(doc.get(RodaConstants.PRESERVATION_EVENT_DATETIME));
    final String eventDetail = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_DETAIL), "");
    final String eventType = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_TYPE), "");
    final String eventOutcome = SolrUtils.objectToString(doc.get(RodaConstants.PRESERVATION_EVENT_OUTCOME), "");

    final List<String> agents = SolrUtils
      .objectToListString(doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER));
    final List<String> outcomes = SolrUtils
      .objectToListString(doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER));
    final List<String> sources = SolrUtils
      .objectToListString(doc.get(RodaConstants.PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER));

    ipe.setEventDateTime(eventDateTime);
    ipe.setEventDetail(eventDetail);
    ipe.setEventType(eventType);
    ipe.setEventOutcome(eventOutcome);

    try {
      List<LinkingIdentifier> ids = new ArrayList<>();
      for (String source : sources) {
        ids.add(JsonUtils.getObjectFromJson(source, LinkingIdentifier.class));
      }
      ipe.setSourcesObjectIds(ids);
    } catch (GenericException | RuntimeException e) {
      LOGGER.error("Error setting event linking source", e);
    }
    try {
      List<LinkingIdentifier> ids = new ArrayList<>();
      for (String outcome : outcomes) {
        ids.add(JsonUtils.getObjectFromJson(outcome, LinkingIdentifier.class));
      }
      ipe.setOutcomeObjectIds(ids);
    } catch (GenericException | RuntimeException e) {
      LOGGER.error("Error setting event linking outcome", e);
    }
    try {
      List<LinkingIdentifier> ids = new ArrayList<>();
      for (String agent : agents) {
        ids.add(JsonUtils.getObjectFromJson(agent, LinkingIdentifier.class));
      }
      ipe.setLinkingAgentIds(ids);
    } catch (GenericException | RuntimeException e) {
      LOGGER.error("Error setting event linking agents", e);
    }
    return ipe;

  }

}
