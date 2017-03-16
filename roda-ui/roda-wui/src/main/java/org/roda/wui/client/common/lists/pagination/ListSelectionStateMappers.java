/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.pagination;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.jobs.IndexedReport;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

public class ListSelectionStateMappers {

  // IndexedAIP
  public static interface IndexedAIPMapper extends ObjectMapper<ListSelectionState<IndexedAIP>> {
  }

  private static IndexedAIPMapper indexedAIPMapper = GWT.create(IndexedAIPMapper.class);

  // IndexedRepresentation
  public static interface IndexedRepresentationMapper extends ObjectMapper<ListSelectionState<IndexedRepresentation>> {
  }

  private static IndexedRepresentationMapper indexedRepresentationMapper = GWT
    .create(IndexedRepresentationMapper.class);

  // IndexedFile
  public static interface IndexedFileMapper extends ObjectMapper<ListSelectionState<IndexedFile>> {
  }

  private static IndexedFileMapper indexedFileMapper = GWT.create(IndexedFileMapper.class);

  // IndexedDIP
  public static interface IndexedDIPMapper extends ObjectMapper<ListSelectionState<IndexedDIP>> {
  }

  private static IndexedDIPMapper indexedDIPMapper = GWT.create(IndexedDIPMapper.class);

  // DIPFile
  public static interface DIPFileMapper extends ObjectMapper<ListSelectionState<DIPFile>> {
  }

  private static DIPFileMapper dipFileMapper = GWT.create(DIPFileMapper.class);

  // Job report
  public static interface IndexedReportMapper extends ObjectMapper<ListSelectionState<IndexedReport>> {
  }

  private static IndexedReportMapper indexedReportMapper = GWT.create(IndexedReportMapper.class);

  private ListSelectionStateMappers() {
    // do nothing
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> String getJson(String objectClass, ListSelectionState<T> object) {
    String ret;
    if (IndexedAIP.class.getName().equals(objectClass)) {
      ret = indexedAIPMapper.write((ListSelectionState<IndexedAIP>) object);
    } else if (IndexedRepresentation.class.getName().equals(objectClass)) {
      ret = indexedRepresentationMapper.write((ListSelectionState<IndexedRepresentation>) object);
    } else if (IndexedFile.class.getName().equals(objectClass)) {
      ret = indexedFileMapper.write((ListSelectionState<IndexedFile>) object);
    } else if (IndexedDIP.class.getName().equals(objectClass)) {
      ret = indexedDIPMapper.write((ListSelectionState<IndexedDIP>) object);
    } else if (DIPFile.class.getName().equals(objectClass)) {
      ret = dipFileMapper.write((ListSelectionState<DIPFile>) object);
    } else if (IndexedReport.class.getName().equals(objectClass)) {
      ret = indexedReportMapper.write((ListSelectionState<IndexedReport>) object);
    } else {
      ret = null;
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static <T extends IsIndexed> ListSelectionState<T> getObject(String objectClass, String json) {
    ListSelectionState<T> state;
    if (IndexedAIP.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) indexedAIPMapper.read(json);
    } else if (IndexedRepresentation.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) indexedRepresentationMapper.read(json);
    } else if (IndexedFile.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) indexedFileMapper.read(json);
    } else if (IndexedDIP.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) indexedDIPMapper.read(json);
    } else if (DIPFile.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) dipFileMapper.read(json);
    } else if (IndexedReport.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) indexedReportMapper.read(json);
    } else {
      state = null;
    }
    return state;
  }

}
