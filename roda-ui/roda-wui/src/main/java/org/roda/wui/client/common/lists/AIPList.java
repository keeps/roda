/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;

import config.i18n.client.ClientMessages;

/**
 * 
 * @deprecated Use {@link ConfigurableAsyncTableCell} instead.
 */
@Deprecated
public class AIPList extends AsyncTableCell<IndexedAIP> {

  private final ClientLogger logger = new ClientLogger(getClass().getName());
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final SafeHtml HAS_REPRESENTATIONS_ICON = SafeHtmlUtils.fromSafeConstant(
    "<i class='fa fa-paperclip' title='" + messages.aipHasRepresentations() + "' aria-hidden='true'></i>");

  private Column<IndexedAIP, SafeHtml> levelColumn;
  private TextColumn<IndexedAIP> titleColumn;
  private TextColumn<IndexedAIP> datesColumn;
  private Column<IndexedAIP, SafeHtml> hasRepresentationsColumn;

  private static final List<String> fieldsToReturn = new ArrayList<>(RodaConstants.AIP_PERMISSIONS_FIELDS_TO_RETURN);
  static {
    fieldsToReturn.addAll(Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_LEVEL, RodaConstants.AIP_TITLE,
      RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL, RodaConstants.AIP_HAS_REPRESENTATIONS,
      RodaConstants.AIP_STATE));
  }

  @Override
  protected void adjustOptions(AsyncTableCellOptions<IndexedAIP> options) {
    options.withFieldsToReturn(fieldsToReturn);
  }

  @Override
  protected void configureDisplay(CellTable<IndexedAIP> display) {
    levelColumn = new Column<IndexedAIP, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedAIP aip) {
        SafeHtml ret;
        if (aip == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else {
          ret = DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), true);
        }
        return ret;
      }
    };

    titleColumn = new TextColumn<IndexedAIP>() {
      @Override
      public String getValue(IndexedAIP aip) {
        return aip != null ? aip.getTitle() : null;
      }
    };

    datesColumn = new TextColumn<IndexedAIP>() {
      @Override
      public String getValue(IndexedAIP aip) {
        return Humanize.getDatesText(aip.getDateInitial(), aip.getDateFinal(), false);
      }
    };

    hasRepresentationsColumn = new Column<IndexedAIP, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(IndexedAIP aip) {
        SafeHtml ret;
        if (aip == null) {
          logger.error("Trying to display a NULL item");
          ret = null;
        } else if (aip.getHasRepresentations()) {
          // TODO set title and aria
          ret = HAS_REPRESENTATIONS_ICON;
        } else {
          ret = null;
        }
        return ret;
      }
    };

    levelColumn.setSortable(true);
    titleColumn.setSortable(true);
    datesColumn.setSortable(true);
    hasRepresentationsColumn.setSortable(true);

    addColumn(levelColumn, SafeHtmlUtils.fromSafeConstant("<i class='fa fa-tag'></i>&nbsp;" + messages.aipLevel()),
      true);
    addColumn(titleColumn, messages.aipGenericTitle(), false);
    addColumn(datesColumn, messages.aipDates(), true);
    addColumn(hasRepresentationsColumn, HAS_REPRESENTATIONS_ICON, false);

    // define default sorting
    display.getColumnSortList().push(new ColumnSortInfo(datesColumn, true));

    display.setColumnWidth(levelColumn, 7.0, Unit.EM);
    display.setColumnWidth(datesColumn, 14.0, Unit.EM);
    display.setColumnWidth(hasRepresentationsColumn, 3.0, Unit.EM);

    addStyleName("my-collections-table");
  }

  @Override
  protected Sorter getSorter(ColumnSortList columnSortList) {
    Map<Column<IndexedAIP, ?>, List<String>> columnSortingKeyMap = new HashMap<>();
    // setting secondary sorter to title
    columnSortingKeyMap.put(levelColumn, Arrays.asList(RodaConstants.AIP_LEVEL, RodaConstants.AIP_TITLE_SORT));
    columnSortingKeyMap.put(titleColumn, Arrays.asList(RodaConstants.AIP_TITLE_SORT));
    columnSortingKeyMap.put(datesColumn,
      Arrays.asList(RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL, RodaConstants.AIP_TITLE_SORT));
    columnSortingKeyMap.put(hasRepresentationsColumn,
      Arrays.asList(RodaConstants.AIP_HAS_REPRESENTATIONS, RodaConstants.AIP_TITLE_SORT));

    return createSorter(columnSortList, columnSortingKeyMap);
  }

}
