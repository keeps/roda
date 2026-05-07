package org.roda.wui.client.common.panels;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchFilters;
import org.roda.wui.client.common.search.SearchWrapper;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class TablePanel extends Composite {

  private FlowPanel mainPanel;

  public TablePanel() {
    FlowPanel rootPanel = new FlowPanel();
    rootPanel.setStyleName("");
    FlowPanel mainPanel = new FlowPanel();
    mainPanel.setStyleName("");
    rootPanel.add(mainPanel);
    initWidget(rootPanel);
  }

  public <T extends IsIndexed> TablePanel createList(ListBuilder<T> listBuilder) {
    return createListPanel(listBuilder);
  }

  public <T extends IsIndexed> TablePanel createListPanel(ListBuilder<T> listBuilder) {
    AsyncTableCell<T> list = listBuilder.build();

    Filter filter = list.getFilter();
    String allFilter = SearchFilters.searchField();
    boolean incremental = SearchFilters.shouldBeIncremental(filter);

    mainPanel.add(list);

    return this;
  }
}
