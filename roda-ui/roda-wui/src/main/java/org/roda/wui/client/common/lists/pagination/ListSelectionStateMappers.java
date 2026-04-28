/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists.pagination;

import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.*;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.common.client.ClientLogger;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;

public class ListSelectionStateMappers {
  private static final IndexedPreservationEventMapper indexedPreservationEventMapper = GWT
    .create(IndexedPreservationEventMapper.class);
  private static final ClientLogger logger = new ClientLogger(ListSelectionUtils.class.getName());
  private static IndexedAIPMapper indexedAIPMapper = GWT.create(IndexedAIPMapper.class);
  private static IndexedRepresentationMapper indexedRepresentationMapper = GWT
    .create(IndexedRepresentationMapper.class);
  private static IndexedFileMapper indexedFileMapper = GWT.create(IndexedFileMapper.class);
  private static IndexedDIPMapper indexedDIPMapper = GWT.create(IndexedDIPMapper.class);
  private static DIPFileMapper dipFileMapper = GWT.create(DIPFileMapper.class);
  private static IndexedReportMapper indexedReportMapper = GWT.create(IndexedReportMapper.class);
  private static DisposalHoldMapper disposalHoldMapper = GWT.create(DisposalHoldMapper.class);
  private static DisposalScheduleMapper disposalScheduleMapper = GWT.create(DisposalScheduleMapper.class);
  private static DisposalRuleMapper disposalRuleMapper = GWT.create(DisposalRuleMapper.class);

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
    } else if (IndexedPreservationEvent.class.getName().equals(objectClass)) {
      ret = indexedPreservationEventMapper.write((ListSelectionState<IndexedPreservationEvent>) object);
    } else if (DisposalHold.class.getName().equals(objectClass)) {
      ret = disposalHoldMapper.write((ListSelectionState<DisposalHold>) object);
    } else if (DisposalSchedule.class.getName().equals(objectClass)) {
      ret = disposalScheduleMapper.write((ListSelectionState<DisposalSchedule>) object);
    } else if (DisposalRule.class.getName().equals(objectClass)) {
      ret = disposalRuleMapper.write((ListSelectionState<DisposalRule>) object);
    } else {
      ret = null;
    }
    return ret;
  }

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
    } else if (IndexedPreservationEvent.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) indexedPreservationEventMapper.read(json);
    } else if (DisposalHold.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) disposalHoldMapper.read(json);
    } else if (DisposalSchedule.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) disposalScheduleMapper.read(json);
    } else if (DisposalRule.class.getName().equals(objectClass)) {
      state = (ListSelectionState<T>) disposalRuleMapper.read(json);
    } else {
      state = null;
    }
    return state;
  }

  // IndexedAIP
  public interface IndexedAIPMapper extends ObjectMapper<ListSelectionState<IndexedAIP>> {
  }

  // IndexedRepresentation
  public interface IndexedRepresentationMapper extends ObjectMapper<ListSelectionState<IndexedRepresentation>> {
  }

  // IndexedFile
  public interface IndexedFileMapper extends ObjectMapper<ListSelectionState<IndexedFile>> {
  }

  // IndexedDIP
  public interface IndexedDIPMapper extends ObjectMapper<ListSelectionState<IndexedDIP>> {
  }

  // DIPFile
  public interface DIPFileMapper extends ObjectMapper<ListSelectionState<DIPFile>> {
  }

  // Job report
  public interface IndexedReportMapper extends ObjectMapper<ListSelectionState<IndexedReport>> {
  }

  // IndexedPreservationEvent
  public interface IndexedPreservationEventMapper extends ObjectMapper<ListSelectionState<IndexedPreservationEvent>> {
  }

  // IndexedAIP
  public interface IndexedAIPMapper extends ObjectMapper<ListSelectionState<IndexedAIP>> {
  }

  // IndexedRepresentation
  public interface IndexedRepresentationMapper extends ObjectMapper<ListSelectionState<IndexedRepresentation>> {
  }

  // IndexedFile
  public interface IndexedFileMapper extends ObjectMapper<ListSelectionState<IndexedFile>> {
  }

  // IndexedDIP
  public interface IndexedDIPMapper extends ObjectMapper<ListSelectionState<IndexedDIP>> {
  }

  // DIPFile
  public interface DIPFileMapper extends ObjectMapper<ListSelectionState<DIPFile>> {
  }

  // Job report
  public interface IndexedReportMapper extends ObjectMapper<ListSelectionState<IndexedReport>> {
  }

  // Disposal hold
  public interface DisposalHoldMapper extends ObjectMapper<ListSelectionState<DisposalHold>> {
  }

  // Disposal schedule
  public interface DisposalScheduleMapper extends ObjectMapper<ListSelectionState<DisposalSchedule>> {
  }

  // Disposal rule
  public interface DisposalRuleMapper extends ObjectMapper<ListSelectionState<DisposalRule>> {
  }

}
