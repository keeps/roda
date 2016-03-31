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

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ProvidesKey;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class AgentList extends AsyncTableCell<Agent> {

  private static final int PAGE_SIZE = 20;

  // private final ClientLogger logger = new ClientLogger(getClass().getName());
  // private static final BrowseMessages messages =
  // GWT.create(BrowseMessages.class);

  private TextColumn<Agent> nameColumn;
  private TextColumn<Agent> typeColumn;

  public AgentList() {
    this(null, null, null, false);
  }

  public AgentList(Filter filter, Facets facets, String summary, boolean selectable) {
    super(filter, facets, summary, selectable);
  }

  @Override
  protected void configureDisplay(CellTable<Agent> display) {

    nameColumn = new TextColumn<Agent>() {

      @Override
      public String getValue(Agent agent) {
        return agent != null ? agent.getName() : null;
      }
    };

    typeColumn = new TextColumn<Agent>() {

      @Override
      public String getValue(Agent agent) {
        return agent != null ? agent.getType() : null;
      }
    };

    nameColumn.setSortable(true);
    typeColumn.setSortable(true);

    // TODO externalize strings into constants
    display.addColumn(nameColumn, "Name");
    display.addColumn(typeColumn, "Type");

    Label emptyInfo = new Label("No items to display");
    display.setEmptyTableWidget(emptyInfo);
    display.setColumnWidth(nameColumn, "100%");

    // default sorting
    display.getColumnSortList().push(new ColumnSortInfo(nameColumn, false));

    typeColumn.setCellStyleNames("nowrap");
  }

  @Override
  protected void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<Agent>> callback) {

    Filter filter = getFilter();

    Map<Column<Agent, ?>, List<String>> columnSortingKeyMap = new HashMap<Column<Agent, ?>, List<String>>();
    columnSortingKeyMap.put(nameColumn, Arrays.asList(RodaConstants.AGENT_NAME));
    columnSortingKeyMap.put(typeColumn, Arrays.asList(RodaConstants.AGENT_TYPE));

    Sorter sorter = createSorter(columnSortList, columnSortingKeyMap);

    BrowserService.Util.getInstance().find(Agent.class.getName(), filter, sorter, sublist, getFacets(),
      LocaleInfo.getCurrentLocale().getLocaleName(), callback);
  }

  @Override
  protected ProvidesKey<Agent> getKeyProvider() {
    return new ProvidesKey<Agent>() {

      @Override
      public Object getKey(Agent item) {
        return item.getId();
      }
    };
  }

  @Override
  protected int getInitialPageSize() {
    return PAGE_SIZE;
  }

}
