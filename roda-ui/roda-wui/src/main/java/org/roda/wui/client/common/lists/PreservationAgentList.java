/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class PreservationAgentList extends AsyncTableCell<IndexedPreservationAgent> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedPreservationAgent> nameColumn;
  private TextColumn<IndexedPreservationAgent> typeColumn;
  private TextColumn<IndexedPreservationAgent> versionColumn;

  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.PRESERVATION_AGENT_ID, RodaConstants.PRESERVATION_AGENT_NAME, RodaConstants.PRESERVATION_AGENT_TYPE,
    RodaConstants.PRESERVATION_AGENT_VERSION, RodaConstants.PRESERVATION_AGENT_CREATED_ON);

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedPreservationAgent> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedPreservationAgent> display) {
    nameColumn = new TextColumn<IndexedPreservationAgent>() {
      @Override
      public String getValue(IndexedPreservationAgent agent) {
        return agent != null ? agent.getName() : null;
      }
    };

    typeColumn = new TextColumn<IndexedPreservationAgent>() {
      @Override
      public String getValue(IndexedPreservationAgent agent) {
        return agent != null ? agent.getType() : null;
      }
    };

    versionColumn = new TextColumn<IndexedPreservationAgent>() {
      @Override
      public String getValue(IndexedPreservationAgent agent) {
        return agent != null ? agent.getVersion() : null;
      }
    };

    nameColumn.setSortable(true);
    typeColumn.setSortable(true);
    versionColumn.setSortable(true);

    addColumn(nameColumn, messages.preservationAgentName(), true, false, 13);
    addColumn(typeColumn, messages.preservationAgentType(), false, false, 11);
    addColumn(versionColumn, messages.preservationAgentVersion(), false, false, 11);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(nameColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedPreservationAgent, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_NAME));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_TYPE));
    columnSortingKeyMap.put(versionColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_VERSION));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
