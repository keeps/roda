/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.HasInstanceID;
import org.roda.core.data.v2.ip.HasInstanceName;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.core.data.v2.ip.HasState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.SetsUUID;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.utils.IndexUtils;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.storage.utils.RODAInstanceUtils;

public abstract class AbstractSolrCollection<I extends IsIndexed, M extends IsModelObject>
  implements SolrCollection<I, M> {

  @Override
  public List<Field> getFields() {
    List<Field> ret = new ArrayList<>();

    if (SolrCollection.hasId(getIndexClass())) {
      ret.add(new Field(RodaConstants.INDEX_ID, Field.TYPE_STRING));
    }

    if (SolrCollection.hasStateFilter(getIndexClass())) {
      boolean stored = SolrCollection.hasState(getIndexClass());
      ret.add(new Field(RodaConstants.INDEX_STATE, Field.TYPE_STRING).setStored(stored)
        .setDefaultValue(AIPState.getDefault().toString()));
    }

    if (SolrCollection.hasInstanceId(getIndexClass())) {
      ret.add(new Field(RodaConstants.INDEX_INSTANCE_ID, Field.TYPE_STRING));
    }

    if (SolrCollection.hasInstanceName(getIndexClass())) {
      ret.add(new Field(RodaConstants.INDEX_INSTANCE_NAME, Field.TYPE_STRING));
    }

    ret.add(SolrCollection.getSearchField());

    return ret;
  }

  @Override
  public List<DynamicField> getDynamicFields() {
    List<DynamicField> ret;
    if (SolrCollection.hasPermissionFilters(getIndexClass())) {
      boolean stored = SolrCollection.hasPermissions(getIndexClass());
      ret = SolrCollection.getPermissionDynamicFields(stored);
    } else {
      ret = new ArrayList<>();
    }

    ret.add(new DynamicField("*_txt", Field.TYPE_TEXT).setIndexed(true).setStored(true).setMultiValued(true));
    ret.add(new DynamicField("*_t", Field.TYPE_TEXT).setIndexed(true).setStored(true).setMultiValued(false));

    return ret;
  }

  @Override
  public SolrInputDocument toSolrDocument(M object, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = new SolrInputDocument();

    if (object != null) {
      doc.addField(RodaConstants.INDEX_UUID, getUniqueId(object));

      if (SolrCollection.hasId(object.getClass())) {
        doc.addField(RodaConstants.INDEX_ID, ((HasId) object).getId());
      }

      if (SolrCollection.hasState(object.getClass())) {
        doc.addField(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(((HasState) object).getState()));
      }

      if (SolrCollection.hasPermissions(object.getClass())) {
        SolrUtils.setPermissions(((HasPermissions) object).getPermissions(), doc);
      }

      if (SolrCollection.hasInstanceId(object.getClass()) && RODAInstanceUtils.isConfiguredAsDistributedMode()) {
        String instanceId = ((HasInstanceID) object).getInstanceId();
        if (StringUtils.isEmpty(instanceId)) {
          instanceId = RODAInstanceUtils.getLocalInstanceIdentifier();
        }
        doc.addField(RodaConstants.INDEX_INSTANCE_ID, instanceId);
        doc.addField(RodaConstants.INDEX_INSTANCE_NAME, IndexUtils.giveNameFromLocalInstanceIdentifier(instanceId));
      }
    }

    if (info.getPreCalculatedFields() != null) {
      info.getPreCalculatedFields().forEach(doc::addField);
    }

    return doc;
  }

  @Override
  public I fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {
    I ret = null;
    try {
      ret = getIndexClass().getDeclaredConstructor().newInstance();

      if (ret instanceof HasId hasId) {
        String id = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_ID),
          SolrUtils.objectToString(doc.get(RodaConstants.INDEX_UUID), null));
        hasId.setId(id);
      }

      if (ret instanceof SetsUUID setsUUID) {
        String uuid = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_UUID), null);
        setsUUID.setUUID(uuid);
      }

      if (ret instanceof HasState hasState && doc.containsKey(RodaConstants.INDEX_STATE)) {
        AIPState state = SolrUtils.objectToEnum(doc.get(RodaConstants.INDEX_STATE), AIPState.class,
          AIPState.getDefault());
        hasState.setState(state);
      }

      if (ret instanceof HasPermissions hasPermissions) {
        Permissions permissions = SolrUtils.getPermissions(doc);
        hasPermissions.setPermissions(permissions);
      }

      if (ret instanceof HasInstanceID hasInstanceID) {
        String instanceID = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_ID), null);
        hasInstanceID.setInstanceId(instanceID);
      }

      if (ret instanceof HasInstanceName hasInstanceName) {
        String instanceName = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_NAME), null);
        hasInstanceName.setInstanceName(instanceName);
      }

      Map<String, Object> indexedFields = new HashMap<>();
      for (String field : fieldsToReturn) {
        indexedFields.put(field, doc.get(field));
      }
      ret.setFields(indexedFields);

    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new GenericException(e);
    }

    return ret;
  }
}
