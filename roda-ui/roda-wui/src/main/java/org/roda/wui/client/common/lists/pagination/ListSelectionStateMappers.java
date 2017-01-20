package org.roda.wui.client.common.lists.pagination;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

public class ListSelectionStateMappers {

  // IndexedAIP
  public static interface IndexedAIP_MAPPER extends ObjectMapper<ListSelectionState<IndexedAIP>> {
  }

  private static IndexedAIP_MAPPER IndexedAIP_MAPPER = GWT.create(IndexedAIP_MAPPER.class);

  // IndexedRepresentation

  public static interface IndexedRepresentation_MAPPER extends ObjectMapper<ListSelectionState<IndexedRepresentation>> {
  }

  private static IndexedRepresentation_MAPPER IndexedRepresentation_MAPPER = GWT
    .create(IndexedRepresentation_MAPPER.class);

  // IndexedFile

  public static interface IndexedFile_MAPPER extends ObjectMapper<ListSelectionState<IndexedFile>> {
  }

  private static IndexedFile_MAPPER IndexedFile_MAPPER = GWT.create(IndexedFile_MAPPER.class);

  // IndexedDIP

  public static interface IndexedDIP_MAPPER extends ObjectMapper<ListSelectionState<IndexedDIP>> {
  }

  private static IndexedDIP_MAPPER IndexedDIP_MAPPER = GWT.create(IndexedDIP_MAPPER.class);

  // DIPFile

  public static interface DIPFile_MAPPER extends ObjectMapper<ListSelectionState<DIPFile>> {
  }

  private static DIPFile_MAPPER DIPFile_MAPPER = GWT.create(DIPFile_MAPPER.class);

  private ListSelectionStateMappers() {

  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> String getJson(String objectClass, ListSelectionState<T> object) {
    String ret;
    if (IndexedAIP.class.getName().equals(objectClass)) {
      ret = IndexedAIP_MAPPER.write((ListSelectionState<IndexedAIP>) object);
    } else if (IndexedRepresentation.class.getName().equals(objectClass)) {
      ret = IndexedRepresentation_MAPPER.write((ListSelectionState<IndexedRepresentation>) object);
    } else if (IndexedFile.class.getName().equals(objectClass)) {
      ret = IndexedFile_MAPPER.write((ListSelectionState<IndexedFile>) object);
    } else if (IndexedDIP.class.getName().equals(objectClass)) {
      ret = IndexedDIP_MAPPER.write((ListSelectionState<IndexedDIP>) object);
    } else if (DIPFile.class.getName().equals(objectClass)) {
      ret = DIPFile_MAPPER.write((ListSelectionState<DIPFile>) object);
    } else {
      ret = null;
    }

    return ret;
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> ListSelectionState<T> getObject(String objectClass, String json) {
    ListSelectionState<T> state;
    if (IndexedAIP.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) IndexedAIP_MAPPER.read(json);
    } else if (IndexedRepresentation.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) IndexedRepresentation_MAPPER.read(json);
    } else if (IndexedFile.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) IndexedFile_MAPPER.read(json);
    } else if (IndexedDIP.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) IndexedDIP_MAPPER.read(json);
    } else if (DIPFile.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) DIPFile_MAPPER.read(json);
    } else {
      state = null;
    }
    return state;
  }

}
