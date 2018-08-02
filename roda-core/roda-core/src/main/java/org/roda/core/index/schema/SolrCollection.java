package org.roda.core.index.schema;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.HasPermissionFilters;
import org.roda.core.data.v2.ip.HasPermissions;
import org.roda.core.data.v2.ip.HasState;
import org.roda.core.data.v2.ip.HasStateFilter;
import org.roda.core.data.v2.ip.SetsUUID;

public interface SolrCollection<I extends IsIndexed, M extends IsModelObject> {

  static Field getSearchField() {
    return new Field(Field.FIELD_SEARCH, Field.TYPE_SEARCH).setStored(false).setMultiValued(true);
  }

  static CopyField getCopyAllToSearchField() {
    return new CopyField(RodaConstants.INDEX_WILDCARD, Field.FIELD_SEARCH);
  }

  static Field getSortFieldOf(String field) {
    return new Field(field + "_sort", Field.TYPE_STRING).setMultiValued(true);
  }

  static CopyField getSortCopyFieldOf(String field) {
    return new CopyField(field, field + "_sort");
  }

  static CopyField getTextCopyFieldOf(String field) {
    return new CopyField(field, field + "_txt");
  }

  static List<DynamicField> getPermissionDynamicFields(boolean stored) {
    return Arrays.asList(
      new DynamicField(DynamicField.DYNAMIC_FIELD_PERMISSION_USERS, Field.TYPE_STRING).setMultiValued(true)
        .setStored(stored),
      new DynamicField(DynamicField.DYNAMIC_FIELD_PERMISSION_GROUPS, Field.TYPE_STRING).setMultiValued(true)
        .setStored(stored));
  }

  static <T> boolean hasPermissionFilters(Class<T> resultClass) {
    return HasPermissionFilters.class.isAssignableFrom(resultClass);
  }

  static <T> boolean hasPermissions(Class<T> resultClass) {
    return HasPermissions.class.isAssignableFrom(resultClass);
  }

  static <T> boolean hasId(Class<T> resultClass) {
    return HasId.class.isAssignableFrom(resultClass);
  }

  static <I extends IsIndexed> boolean setsUUID(Class<I> resultClass) {
    return SetsUUID.class.isAssignableFrom(resultClass);
  }

  static <T> boolean hasStateFilter(Class<T> resultClass) {
    return HasStateFilter.class.isAssignableFrom(resultClass);
  }

  static <T> boolean hasState(Class<T> resultClass) {
    return HasState.class.isAssignableFrom(resultClass);
  }

  public enum Flags {
    SAFE_MODE_ON, SAFE_MODE_OFF
  }

  Class<I> getIndexClass();

  Class<M> getModelClass();

  String getIndexName();

  List<String> getCommitIndexNames();

  String getUniqueId(M modelObject);

  List<Field> getFields();

  List<CopyField> getCopyFields();

  List<DynamicField> getDynamicFields();

  /**
   * Map an model object to a Solr Document ready to index
   * 
   * @param object
   *          The model object
   * @param preCalculatedFields
   *          Fields that are pre-calculated because they can be shared for
   *          several objects, like the list of ancestors for sibling objects.
   * @param accumulators
   *          Fields to be added on upper-level collections that are calculated by
   *          accumulating values on every lower-level collection.
   * @param flags
   *          Flags to control indexing, as {@link Flags#SAFE_MODE_ON}.
   * @return the Solr Document ready to index.
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   */
  SolrInputDocument toSolrDocument(M object, Map<String, Object> preCalculatedFields, Map<String, Object> accumulators,
    Flags... flags) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException;

  I fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException;

}
