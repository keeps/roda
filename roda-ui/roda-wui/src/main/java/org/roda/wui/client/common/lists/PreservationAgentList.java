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
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;

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
public class PreservationAgentList extends BasicAsyncTableCell<IndexedPreservationAgent> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private TextColumn<IndexedPreservationAgent> idColumn;
  private TextColumn<IndexedPreservationAgent> nameColumn;
  private TextColumn<IndexedPreservationAgent> typeColumn;
  private TextColumn<IndexedPreservationAgent> versionColumn;

  public PreservationAgentList() {
    this(null, null, null, false);
  }

  public PreservationAgentList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(IndexedPreservationAgent.class, filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedPreservationAgent> display) {

    idColumn = new TextColumn<IndexedPreservationAgent>() {
      @Override
      public String getValue(IndexedPreservationAgent agent) {
        return agent != null ? agent.getId() : null;
      }
    };

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

    idColumn.setSortable(true);
    nameColumn.setSortable(true);
    typeColumn.setSortable(true);
    versionColumn.setSortable(true);

    addColumn(nameColumn, messages.preservationAgentName(), true, false, 11);
    addColumn(nameColumn, messages.preservationAgentName(), true, false, 13);
    addColumn(typeColumn, messages.preservationAgentType(), false, false, 11);
    addColumn(versionColumn, messages.preservationAgentVersion(), false, false, 11);

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(nameColumn, false));
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedPreservationAgent, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<IndexedPreservationAgent, ?>, List<String>>();
    columnSortingKeyMap.put(idColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_ID));
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_NAME));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_TYPE));
    columnSortingKeyMap.put(versionColumn, Arrays.asList(RodaConstants.PRESERVATION_AGENT_VERSION));
    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
