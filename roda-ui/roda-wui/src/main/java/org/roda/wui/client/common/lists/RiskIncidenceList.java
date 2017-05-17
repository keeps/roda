/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

public class RiskIncidenceList extends BasicAsyncTableCell<RiskIncidence> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<RiskIncidence> objectColumn;
  private TextColumn<RiskIncidence> objectTypeColumn;
  private TextColumn<RiskIncidence> riskColumn;
  private Column<RiskIncidence, Date> detectedOnColumn;
  private TextColumn<RiskIncidence> detectedByColumn;
  private Column<RiskIncidence, SafeHtml> statusColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.RISK_INCIDENCE_ID, RodaConstants.RISK_INCIDENCE_AIP_ID,
    RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, RodaConstants.RISK_INCIDENCE_FILE_ID,
    RodaConstants.RISK_INCIDENCE_OBJECT_CLASS, RodaConstants.RISK_INCIDENCE_RISK_ID,
    RodaConstants.RISK_INCIDENCE_DETECTED_ON, RodaConstants.RISK_INCIDENCE_DETECTED_BY,
    RodaConstants.RISK_INCIDENCE_STATUS);

  public RiskIncidenceList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(RiskIncidence.class, filter, true, facets, summary, selectable, fieldsToReturn);
  }

  public RiskIncidenceList(Filter filter, Facets facets, String summary, boolean selectable, int initialPageSize,
    int pageSizeIncrement) {
    super(RiskIncidence.class, filter, true, facets, summary, selectable, initialPageSize, pageSizeIncrement,
      fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<RiskIncidence> display) {

    objectColumn = new TextColumn<RiskIncidence>() {

      @Override
      public String getValue(RiskIncidence incidence) {
        if (incidence != null) {
          if (incidence.getFileId() != null) {
            return incidence.getFileId();
          } else if (incidence.getRepresentationId() != null) {
            return incidence.getRepresentationId();
          } else if (incidence.getAipId() != null) {
            return incidence.getAipId();
          }
        }

        return null;
      }
    };

    objectTypeColumn = new TextColumn<RiskIncidence>() {

      @Override
      public String getValue(RiskIncidence incidence) {
        return incidence != null ? incidence.getObjectClass() : null;
      }
    };

    riskColumn = new TextColumn<RiskIncidence>() {

      @Override
      public String getValue(RiskIncidence incidence) {
        return incidence.getRiskId();
      }
    };

    detectedOnColumn = new Column<RiskIncidence, Date>(
      new DateCell(DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG))) {
      @Override
      public Date getValue(RiskIncidence incidence) {
        return incidence != null ? incidence.getDetectedOn() : null;
      }
    };

    detectedByColumn = new TextColumn<RiskIncidence>() {

      @Override
      public String getValue(RiskIncidence incidence) {
        return incidence != null ? incidence.getDetectedBy() : null;
      }
    };

    statusColumn = new Column<RiskIncidence, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(RiskIncidence incidence) {
        SafeHtml ret = null;
        if (incidence != null) {
          ret = HtmlSnippetUtils.getStatusDefinition(incidence.getStatus());
        }

        return ret;
      }
    };

    objectColumn.setSortable(true);
    objectTypeColumn.setSortable(true);
    riskColumn.setSortable(true);
    detectedOnColumn.setSortable(true);
    detectedByColumn.setSortable(true);
    statusColumn.setSortable(true);

    addColumn(objectTypeColumn, messages.riskIncidenceObjectType(), true, true, 8);
    addColumn(objectColumn, messages.riskIncidenceObjectId(), false, false);
    addColumn(riskColumn, messages.riskIncidenceRisk(), false, false);
    addColumn(detectedOnColumn, messages.riskIncidenceDetectedOn(), false, false);
    addColumn(detectedByColumn, messages.riskIncidenceDetectedBy(), false, false);
    addColumn(statusColumn, messages.riskIncidenceStatus(), false, false, 7);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(objectTypeColumn, true));
    addStyleName("my-collections-table");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<RiskIncidence, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(objectColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_AIP_ID,
      RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, RodaConstants.RISK_INCIDENCE_FILE_ID));
    columnSortingKeyMap.put(riskColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_RISK_ID));
    columnSortingKeyMap.put(detectedOnColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_DETECTED_ON));
    columnSortingKeyMap.put(detectedByColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_DETECTED_BY));
    columnSortingKeyMap.put(statusColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_STATUS));
    columnSortingKeyMap.put(objectTypeColumn, Arrays.asList(RodaConstants.RISK_INCIDENCE_OBJECT_CLASS));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
