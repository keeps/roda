package org.roda.wui.client.common.search;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.wui.client.common.lists.utils.BasicAsyncTableCell;

public interface EntitySearch {

    BasicAsyncTableCell<?> getList();

    void setFilter(Filter filter);

    void refresh();
}
